package memory

// Generic Packages
import chisel3._
import chisel3.Module
import chisel3.util._
import org.scalacheck.Prop.False

// Modules needed
import arbiters._
import muxes._

// Config
import config._
import utility._
import interfaces._
import node._

// Cache requests
import accel._

// Memory constants
import Constants._

abstract class WriteEntryIO()(implicit val p: Parameters)
  extends Module
    with CoreParams {

  val io = IO(new Bundle {
    // Read Request Type
    val NodeReq = Flipped(Decoupled(Input(new WriteReq)))
    //    val NodeResp = Decoupled(new WriteResp)

    //Memory interface
    val MemReq  = Decoupled(new MemReq)
    val MemResp = Input(new MemResp)

    // val Output 
    val output = Decoupled(new WriteResp)

    val free = Output(Bool( ))
    val done = Output(Bool( ))
  })
}

/**
  * @brief Read Table Entry
  * @details [long description]
  * @param ID [Read table IDs]
  * @return [description]
  */
class WriteTableEntry(id: Int)(implicit p: Parameters) extends WriteEntryIO( )(p) {
  val ID              = RegInit(id.U)
  val request_R       = RegInit(WriteReq.default)
  val request_valid_R = RegInit(false.B)
  // Data buffers for misaligned accesses

  // Mask for final ANDing and output of data
  val bitmask      = RegInit(0.U(((2) * xlen).W))
  // Send word mask for tracking how many words need to be read
  val sendbytemask = RegInit(0.U(((2) * xlen / 8).W))

  // Is the request valid and request to memory
  val ReqValid   = RegInit(false.B)
  val ReqAddress = RegInit(0.U(xlen.W))

  // Incoming data valid and data operand.
  val DataValid  = RegInit(false.B)
  // Can optimize to be a shift bit.
  val ptr        = RegInit(0.U(log2Ceil(2).W))
  val linebuffer = RegInit(VecInit(Seq.fill(2)(0.U(xlen.W))))
  val xlen_bytes = xlen / 8

  // State machine
  val s_idle :: s_SENDING :: s_RECEIVING :: s_Done :: Nil = Enum(4)
  val state                                               = RegInit(s_idle)

  // Check if entry free.
  /*================================================
  =            Indicate Table State                =
  =================================================*/

  // printf(p"\n State: $state Request: ${request_R} MemReq: ${io.MemReq}")
  // Table entry indicates free to outside world
  io.free := (state === s_idle)
  // Table entry ready to latch new requests
  io.NodeReq.ready := (state === s_idle)
  // Table entry to output demux
  io.done := (state === s_Done)

  /*=================================================================
  =            Default values for external communication            =
  ==================================================================*/
  // @todo: (done and valid are redundant. Need to cleanup at some point in the future)
  io.output.valid := 0.U
  io.output.bits.done := true.B
  io.output.bits.valid := true.B
  io.output.bits.RouteID := request_R.RouteID
  io.MemReq.valid := 0.U
  io.MemReq.bits.addr := ReqAddress + Cat(ptr, 0.U(log2Ceil(xlen_bytes).W))
  // Sending data; pick word from linebuffer
  io.MemReq.bits.data := linebuffer(ptr)
  io.MemReq.bits.mask := sendbytemask(xlen / 8 - 1, 0)
  // *** Note: Chisel seems to be screwing up these constants in the arbiter.
  // Use reg's for now to force it to keep them.
  val myID = RegNext(ID, 0.U)
  io.MemReq.bits.tag := myID
  val isWrite = RegNext(true.B, init = false.B)
  io.MemReq.bits.iswrite := isWrite
  io.MemReq.bits.taskID := request_R.taskID
  io.MemReq.bits.tile := 0.U


  /*=======================================================
  =            Latch Inputs. Calculate masks              =
  ========================================================*/
  when(io.NodeReq.fire( )) {
    request_R := io.NodeReq.bits
    // Calculate things to start the sending process
    // Base word address
    ReqAddress := (io.NodeReq.bits.address >> log2Ceil(xlen_bytes)) << log2Ceil(xlen_bytes)
    // Bitmask of data  for final ANDing
    bitmask := ReadBitMask(io.NodeReq.bits.Typ, io.NodeReq.bits.address, xlen)
    // Bytemask of bytes within words that need to be fetched.
    sendbytemask := ReadByteMask(io.NodeReq.bits.Typ, io.NodeReq.bits.address, xlen)
    // Move data to line buffer. 
    linebuffer := (io.NodeReq.bits.data << Cat(io.NodeReq.bits.address(log2Ceil(xlen_bytes) - 1, 0), 0.U(3.W))).asTypeOf(Vec(2, UInt(xlen.W)))
    // Next State
    state := s_SENDING
  }


  // printf(p"\n MSHR $ID State: $state MemReq ${io.MemReq}")

  /*===========================================================
  =            Sending values to the cache request            =
  ===========================================================*/

  when((state === s_SENDING) && (sendbytemask =/= 0.asUInt((xlen / 4).W))) {
    io.MemReq.valid := 1.U
    // io.MemReq.ready means arbitration succeeded and memory op has been passed on
    when(io.MemReq.fire( )) {
      // Shift right by word length on machine.
      sendbytemask := sendbytemask >> (xlen / 8)
      // Increment ptr to next entry in linebuffer (for next write)
      ptr := ptr + 1.U
      // Move to receiving data
      state := s_RECEIVING
    }
  }

  /*============================================================
  =            Receiving values from cache response            =
  =============================================================*/

  when((state === s_RECEIVING) && (io.MemResp.valid === true.B)) {
    // Check if more data needs to be sent 
    val y = (sendbytemask === 0.asUInt((xlen / 4).W))
    state := Mux(y, s_Done, s_SENDING)
  }

  /*============================================================
  =            Cleanup and send output                         =
  =============================================================*/

  when(state === s_Done) {
    // For the demux
    io.output.valid := 1.U
    ptr := 0.U
    // Valid write 
    io.output.bits.valid := true.B
    // Output driver demux tree has forwarded output (may not have reached receiving node yet)
    when(io.output.fire( )) {
      state := s_idle
      request_valid_R := false.B
    }
  }
}

abstract class WController(NumOps: Int, BaseSize: Int, NumEntries: Int)(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val WriteIn  = Vec(NumOps, Flipped(Decoupled(new WriteReq( ))))
    val WriteOut = Vec(NumOps, Output(new WriteResp( )))
    val MemReq   = Decoupled(new MemReq)
    val MemResp  = Flipped(Valid(new MemResp))
  })
}

class WriteMemoryController(NumOps: Int, BaseSize: Int, NumEntries: Int)(implicit p: Parameters) extends WController(NumOps, BaseSize, NumEntries)(p) {
  require(NumEntries >= 0)
  // Number of MLP entries
  val MLPSize   = NumEntries
  // Input arbiter
  val in_arb    = Module(new ArbiterTree(BaseSize = BaseSize, NumOps = NumOps, new WriteReq( ), Locks = 1))
  // MSHR allocator
  val alloc_arb = Module(new Arbiter(Bool( ), MLPSize))

  // Memory request
  val cachereq_arb    = Module(new Arbiter(new MemReq, MLPSize))
  // Memory response Demux
  val cacheresp_demux = Module(new Demux(new MemResp, MLPSize))

  // Output arbiter and demuxes
  val out_arb   = Module(new RRArbiter(new WriteResp, MLPSize))
  val out_demux = Module(new DeMuxTree(BaseSize = BaseSize, NumOps = NumOps, new WriteResp( )))

  /*=====================================================================
  =            Wire up incoming reads from nodes to ReadMSHR            =
  =====================================================================*/

  // Wire up input with in_arb
  for (i <- 0 until NumOps) {
    in_arb.io.in(i) <> io.WriteIn(i)
    io.WriteOut(i) <> out_demux.io.outputs(i)
  }

  /*=============================================
  =           Declare Read Table                =
  =============================================*/

  // Create WriteTable
  val WriteTable = for (i <- 0 until MLPSize) yield {
    val write_entry = Module(new WriteTableEntry(i))
    // val MSHR = Module(new WriteTableEntry(i))
    // Allocator wireup with table entries
    alloc_arb.io.in(i).valid := write_entry.io.free
    alloc_arb.io.in(i).bits <> DontCare
    write_entry.io.NodeReq.valid := alloc_arb.io.in(i).fire( )
    write_entry.io.NodeReq.bits := in_arb.io.out.bits

    // Table entries -> MemReq arbiter.
    cachereq_arb.io.in(i) <> write_entry.io.MemReq

    // MemResp -> Table entries Demux
    write_entry.io.MemResp <> cacheresp_demux.io.outputs(i)

    // Table entries -> Output arbiter
    out_arb.io.in(i) <> write_entry.io.output

    write_entry
  }

  /*=========================================================================
  =            Wire up arbiters and demux to Write table entries
               1. Allocator arbiter
               2. Output arbiter
               3. Output demux
               4. Cache request arbiter
               5. Cache response demux                                                             =
  =========================================================================*/

  for (i <- 0 until MLPSize) {
  }

  //  Handshaking input arbiter with allocator
  in_arb.io.out.ready := alloc_arb.io.out.valid
  alloc_arb.io.out.ready := in_arb.io.out.valid

  // Cache request arbiter
  // cachereq_arb.io.out.ready := io.MemReq.ready
  io.MemReq <> cachereq_arb.io.out

  // Cache response Demux
  cacheresp_demux.io.en := io.MemResp.valid
  cacheresp_demux.io.input := io.MemResp.bits
  cacheresp_demux.io.sel := io.MemResp.bits.tag

  // Output arbiter -> Demux
  out_arb.io.out.ready := true.B
  out_demux.io.enable := out_arb.io.out.fire( )
  out_demux.io.input := out_arb.io.out.bits

}
