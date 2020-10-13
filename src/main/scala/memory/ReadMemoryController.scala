package memory

// Generic Packages
import chisel3._
import chisel3.Module
import chisel3.util._

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

abstract class ReadEntryIO()(implicit val p: Parameters)
  extends Module
    with CoreParams {

  val io = IO(new Bundle {
    // Read Request Type
    val NodeReq = Flipped(Decoupled(Input(new ReadReq)))
    //    val NodeResp = Decoupled(new ReadResp)

    //Memory interface
    val MemReq  = Decoupled(new MemReq)
    val MemResp = Input(new MemResp)

    // val Output
    val output = Decoupled(new ReadResp)

    val free = Output(Bool( ))
    val done = Output(Bool( ))
  })
}

/**
  * @brief Read Table Entry
  * @details [long description]
  * @param ID [Read taSwitchInControl() p * @return [description]
  */
class ReadTableEntry(id: Int)(implicit p: Parameters) extends ReadEntryIO( )(p) with UniformPrintfs {

  val ID = RegInit(~id.U)
  ID := id.U
  val request_R       = RegInit(ReadReq.default)
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
  val ptr        = RegInit(0.U(log2Ceil(2).W))
  val linebuffer = RegInit(VecInit(Seq.fill(2)(0.U(xlen.W))))
  val xlen_bytes = xlen / 8
  val output     = WireInit(0.U(xlen.W))

  // State machine
  val s_idle :: s_SENDING :: s_RECEIVING :: s_Done :: Nil = Enum(4)
  val state                                               = RegInit(s_idle)

  // Check if entry free.
  /*================================================
  =            Indicate Table State                =
  =================================================*/

  // Table entry indicates free to outside world
  io.free := (state === s_idle)
  // Table entry ready to latch new requests
  io.NodeReq.ready := (state === s_idle)
  // Table entry to output demux
  io.done := (state === s_Done)

  /*=================================================================
  =            Default values for external communication            =
  ==================================================================*/
  io.output.valid := 0.U
  io.output.bits.RouteID := request_R.RouteID
  io.output.bits.valid := true.B
  io.output.bits.data := 0.U

  io.MemReq.valid := 0.U
  io.MemReq.bits.addr := ReqAddress + Cat(ptr, 0.U(log2Ceil(xlen_bytes).W))
  // *** Note: Chisel seems to be screwing up constants in the arbiter.
  // Use reg's for now to force it to keep them.
  io.MemReq.bits.tag := ID
  val isWrite = RegNext(false.B, init = true.B)
  io.MemReq.bits.iswrite := isWrite
  io.MemReq.bits.data := 0.U
  io.MemReq.bits.mask := 0.U
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
    // Next State
    //state := s_SENDING
  }

  // printf("\nMSHR %d: Inputs are Ready %d", ID, request_R.address)
  // printf("\n MSHR %d State :%d RouteID %d ", ID, state, request_R.RouteID)
  // printf("\n  linebuffer %x & bitmask: %x", linebuffer.asUInt, bitmask)

  /*===========================================================
  =            Sending values to the cache request            =
  ===========================================================*/


  switch(state) {
    is(s_idle) {
      when(io.NodeReq.fire( )) {
        state := s_SENDING
      }
    }
    is(s_SENDING) {
      io.MemReq.valid := 1.U
      // io.MemReq.ready means arbitration succeeded and memory op has been passed on
      when(io.MemReq.ready) {
        // Shift right by word length on machine.
        sendbytemask := sendbytemask >> (xlen / 8)
        // Move to receiving data
        state := s_RECEIVING
      }
    }
    is(s_RECEIVING) {
      when(io.MemResp.valid) {
        // Received data; concatenate into linebuffer
        linebuffer(ptr) := io.MemResp.data
        // Increment ptr to next entry in linebuffer (for next read)
        ptr := ptr + 1.U
        // Check if more data needs to be sent
        when(sendbytemask === 0.asUInt((xlen / 4).W)) {
          state := s_Done
        }.otherwise {
          state := s_SENDING
        }
      }
    }
    is(s_Done) {
      io.output.valid := 1.U
      output := (linebuffer.asUInt & bitmask) >> Cat(request_R.address(log2Ceil(xlen_bytes) - 1, 0), 0.U(3.W))
      // @error: To handle doubles this has to change.
      if (xlen == 32) {
        io.output.bits.data := Data2Sign(output, request_R.Typ, xlen)
      }
      if (xlen == 16) {
        io.output.bits.data := Data2Sign16b(output, request_R.Typ, xlen)
      }
      io.output.bits.valid := true.B
      ptr := 0.U
      // Output driver demux tree has forwarded output (may not have reached receiving node yet)
      when(io.output.ready) {
        state := s_idle
        request_valid_R := false.B
      }
    }
  }


  /*============================================================
  =            Cleanup and send output                         =
  =============================================================*/

  override val printfSigil = "UnTyp RD MSHR(" + ID + ")"
  if ((log == true) && (comp contains "RDMSHR")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    //    verb match {
    //      case "high"  => { printf(p"\nUNTYP RD MSHR Time $x: Nodereq: $request_R "); printf(p"linebuffer: ${linebuffer}") }
    //      case "med"   => { printf(p"\nUNTYP RD MSHR Time $x: $io.MemReq"); printf(p"linebuffer: ${linebuffer}") }
    //      case "low"   => { printf(p"\nUNTYP RD MSHR Time $x: ") ; printf(p"Output bits : ${io.output.bits} Output Valid : ${io.output.valid}") }
    //    }
  }
}

abstract class RController(NumOps: Int, BaseSize: Int, NumEntries: Int)(implicit val p: Parameters)
  extends Module with CoreParams {
  val io = IO(new Bundle {
    val ReadIn  = Vec(NumOps, Flipped(Decoupled(new ReadReq( ))))
    val ReadOut = Vec(NumOps, Output(new ReadResp( )))
    val MemReq  = Decoupled(new MemReq)
    val MemResp = Flipped(Valid(new MemResp))
  })
}


class ReadMemoryController
(NumOps: Int,
 BaseSize: Int, NumEntries: Int)
(implicit p: Parameters)
  extends RController(NumOps, BaseSize, NumEntries)(p) {
  require(NumEntries >= 0)
  // Number of MLP entries
  val MLPSize   = NumEntries
  // Input arbiter
  val in_arb    = Module(new ArbiterTree(BaseSize = BaseSize, NumOps = NumOps, new ReadReq( ), Locks = 1))
  // MSHR allocator
  val alloc_arb = Module(new Arbiter(Bool( ), MLPSize))

  // Memory request
  val cachereq_arb    = Module(new Arbiter(new MemReq, MLPSize))
  // Memory response Demux
  val cacheresp_demux = Module(new Demux(new MemResp, MLPSize))

  // Output arbiter and demuxes
  val out_arb   = Module(new RRArbiter(new ReadResp, MLPSize))
  val out_demux = Module(new DeMuxTree(BaseSize = BaseSize, NumOps = NumOps, new ReadResp( )))

  /*=====================================================================
  =            Wire up incoming reads from nodes to ReadMSHR            =
  =====================================================================*/

  // Wire up input with in_arb
  for (i <- 0 until NumOps) {
    in_arb.io.in(i) <> io.ReadIn(i)
    io.ReadOut(i) <> out_demux.io.outputs(i)
  }

  /*=============================================
  =           Declare Read Table                =
  =============================================*/

  // Create ReadTable

  val ReadTable = for (i <- 0 until MLPSize) yield {
    val read_entry = Module(new ReadTableEntry(i))
    // val MSHR = Module(new ReadTableEntry(i))
    // Allocator wireup with table entries
    alloc_arb.io.in(i).valid := read_entry.io.free
    alloc_arb.io.in(i).bits <> DontCare
    read_entry.io.NodeReq.valid := alloc_arb.io.in(i).fire( )
    read_entry.io.NodeReq.bits := in_arb.io.out.bits

    // Table entries -> MemReq arbiter.
    cachereq_arb.io.in(i).valid := read_entry.io.MemReq.valid
    cachereq_arb.io.in(i).bits := read_entry.io.MemReq.bits
    read_entry.io.MemReq.ready := cachereq_arb.io.in(i).ready

    // MemResp -> Table entries Demux
    read_entry.io.MemResp <> cacheresp_demux.io.outputs(i)

    // Table entries -> Output arbiter
    out_arb.io.in(i) <> read_entry.io.output
    read_entry
  }


  /*=========================================================================
  =            Wire up arbiters and demux to read table entries
               1. Allocator arbiter
               2. Output arbiter
               3. Output demux
               4. Cache request arbiter
               5. Cache response demux                                                             =
  =========================================================================*/

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

  // printf(p"\n Read Demux Out: ${out_demux.io.outputs}")

}

