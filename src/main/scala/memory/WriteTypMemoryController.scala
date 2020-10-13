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

/**
  * @brief Read Table Entry
  * @details [long description]
  * @param ID [Read table IDs]
  * @return [description]
  */
class WriteTypTableEntry
(id: Int)
(implicit p: Parameters)
  extends WriteEntryIO( )(p)
    with UniformPrintfs {

  val ID              = RegInit(id.U)
  val request_R       = RegInit(WriteReq.default)
  val request_valid_R = RegInit(false.B)
  // Data buffers for misaligned accesses
  // Mask for final ANDing and output of data
  val bitmask         = RegInit(0.U(((Beats) * xlen).W))
  // Send word mask for tracking how many words need to be written
  val sendbytemask    = RegInit(0.U(((Beats) * xlen / 8).W))

  // Is the request valid and request to memory
  val ReqAddress = RegInit(0.U(xlen.W))

  // Can optimize to be a shift bit.
  val inptr      = RegInit(0.U((log2Ceil(Beats) + 20).W))
  val sendptr    = RegInit(0.U((log2Ceil(Beats) + 1).W))
  val recvptr    = RegInit(0.U((log2Ceil(Beats) + 1).W))
  val linebuffer = RegInit(VecInit(Seq.fill(Beats)(0.U(xlen.W))))
  val linemask   = RegInit(VecInit(Seq.fill(Beats)(0.U((xlen / 8).W))))
  val xlen_bytes = xlen / 8

  // State machine
  val s_idle :: s_SENDING :: s_RECEIVING :: s_Done :: Nil = Enum(4)
  val state                                               = RegInit(s_idle)

  // Check if entry free.
  /*================================================
  =            Indicate Table State                =
  =================================================*/


  // Table entry indicates free to outside world
  io.free := (inptr =/= (Beats).U)
  // Table entry ready to latch new requests
  io.NodeReq.ready := (inptr =/= (Beats).U)
  // Table entry to output demux
  io.done := (state === s_Done)

  /*=================================================================
  =            Default values for external communication            =
  ==================================================================*/
  io.output.valid := 0.U
  io.MemReq.bits.addr := ReqAddress + Cat(sendptr, 0.U(log2Ceil(xlen_bytes).W))
  // Sending data; pick word from linebuffer
  io.MemReq.bits.data := linebuffer(sendptr)
  // MSHR ID
  io.MemReq.bits.tag := ID
  // Write word mask
  io.MemReq.bits.mask := linemask(sendptr)
  // Valid request
  io.MemReq.valid := false.B
  // Is a write?
  io.MemReq.bits.iswrite := true.B
  io.MemReq.bits.taskID := request_R.taskID
  io.MemReq.bits.tile := 0.U


  /*=======================================================
  =            Latch Inputs. Calculate masks              =
  ========================================================*/
  when(io.NodeReq.fire( ) && (inptr =/= (Beats).U)) {
    request_R := io.NodeReq.bits
    // Base word address
    ReqAddress := (io.NodeReq.bits.address >> log2Ceil(xlen_bytes)) << log2Ceil(xlen_bytes)
    // Move data to line buffer.
    linebuffer(inptr) := io.NodeReq.bits.data
    // data mask
    linemask(inptr) := io.NodeReq.bits.mask
    // Move to receive next word
    inptr := inptr + 1.U
    // Next State
    // state := s_SENDING
  }


  /*===========================================================
  =            Sending values to the cache request            =
  ===========================================================*/


  when(sendptr =/= inptr) {
    io.MemReq.valid := true.B
    // io.MemReq.ready means arbitration succeeded and memory op has been passed on
    when(io.MemReq.fire( )) {
      // Increment ptr to next entry in linebuffer (for next write)
      sendptr := sendptr + 1.U
      // Move to receiving data
      // state := s_RECEIVING
    }
  }


  /*============================================================
  =            Receiving values from cache response            =
  =============================================================*/

  when(io.MemResp.valid === true.B) {
    // Check if more data needs to be sent
    recvptr := recvptr + 1.U
    val y = (recvptr === (Beats - 1).U)
    state := Mux(y, s_Done, s_SENDING)
  }


  /*============================================================
  =            Cleanup and send output                         =
  =============================================================*/

  io.output.bits.RouteID := request_R.RouteID
  io.output.valid := false.B
  io.output.bits.done := false.B
  io.output.bits.valid := true.B
  when(state === s_Done) {
    // For the demux
    io.output.valid := 1.U
    io.output.bits.done := true.B
    // Output driver demux tree has forwarded output (may not have reached receiving node yet)
    when(io.output.fire( )) {
      state := s_idle
      inptr := 0.U
      sendptr := 0.U
      recvptr := 0.U
      request_valid_R := false.B
    }
  }

  override val printfSigil = "WR MSHR(" + ID + "," + Typ_SZ + ")"
  if ((log == true) && (comp contains "WRMSHR")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {
        printf(p"Wr MSHR Time $x: Inptr: $inptr Sendptr: $sendptr  Recvptr: $recvptr");
        printf(p"linebuffer: ${linebuffer} nodereq: $io.NodeReq")
      }
      case "med" => {
        printf(p"Wr MSHR Time $x: Memresp: ${io.MemResp}")
      }
      case "low" => {
        printf(p"Wr MSHR Time $x: $io.MemReq")
      }
    }
  }
}

class WriteTypMemoryController
(NumOps: Int, BaseSize: Int, NumEntries: Int)
(implicit p: Parameters)
  extends WController(NumOps, BaseSize, NumEntries)(p) {

  require(NumEntries >= 0)
  // Number of MLP entries
  val MLPSize   = NumEntries
  // Input arbiter
  val in_arb    = Module(new ArbiterTree(BaseSize = BaseSize, NumOps = NumOps, new WriteReq( ), Locks = Beats))
  // MSHR allocator
  val alloc_arb = Module(new LockingRRArbiter(Bool( ), MLPSize, count = Beats))

  // Memory request
  val cachereq_arb    = Module(new RRArbiter(new MemReq, MLPSize))
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

    alloc_arb.io.in(i).bits <> false.B
  }

  /*=============================================
  =           Declare Read Table                =
  =============================================*/

  // Create WriteTable
  val WriteTable = for (i <- 0 until MLPSize) yield {
    val write_entry = Module(new WriteTypTableEntry(i))
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
    // val MSHR = Module(new WriteTableEntry(i))
    // Allocator wireup with table entries
    alloc_arb.io.in(i).valid := WriteTable(i).io.free
    WriteTable(i).io.NodeReq.valid := alloc_arb.io.in(i).fire( ) && in_arb.io.out.fire( )
    WriteTable(i).io.NodeReq.bits := in_arb.io.out.bits

    // Table entries -> MemReq arbiter.
    cachereq_arb.io.in(i) <> WriteTable(i).io.MemReq

    // MemResp -> Table entries Demux
    WriteTable(i).io.MemResp <> cacheresp_demux.io.outputs(i)

    // Table entries -> Output arbiter
    out_arb.io.in(i) <> WriteTable(i).io.output
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
