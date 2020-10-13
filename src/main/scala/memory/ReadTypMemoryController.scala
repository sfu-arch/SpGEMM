package memory

// Generic Packages
import arbiters.ArbiterTree
import chisel3._
import chisel3.Module
import chisel3.util._
import org.scalacheck.Prop.False

// Modules needed
import arbiters._
import muxes._
import arbiters._
// Config
import config._
import utility._
import interfaces._
import node._

// Cache requests
import accel._

// Memory constants
import Constants._

class ReadTypTableEntry(id: Int)(implicit p: Parameters)
  extends ReadEntryIO( )(p)
    with UniformPrintfs {

  val ID              = RegInit(id.U)
  val request_R       = RegInit(ReadReq.default)
  val request_valid_R = RegInit(false.B)
  // Data buffers for misaligned accesses
  // Mask for final ANDing and output of data
  val bitmask         = RegInit(0.U(((Beats) * xlen).W))
  // Send word mask for tracking how many words need to be written
  val sendbytemask    = RegInit(0.U(((Beats) * xlen / 8).W))

  // Is the request valid and request to memory
  val ReqAddress = RegInit(0.U(xlen.W))

  // Can optimize to be a shift bit.
  val outptr     = RegInit(0.U(log2Ceil(Beats + 1).W))
  val sendptr    = RegInit(0.U(log2Ceil(Beats + 1).W))
  val recvptr    = RegInit(0.U(log2Ceil(Beats + 1).W))
  val linebuffer = RegInit(VecInit(Seq.fill(Beats + 1)(0.U(xlen.W))))
  val xlen_bytes = xlen / 8

  // State machine
  val s_idle :: s_SENDING :: s_RECEIVING :: s_Done :: Nil = Enum(4)
  val state                                               = RegInit(s_idle)

  // Check if entry free.
  /*================================================
  =            Indicate Table State                =
  =================================================*/

  // Table entry indicates free to outside world
  io.free := ~request_valid_R
  // Table entry ready to latch new requests
  io.NodeReq.ready := ~request_valid_R
  // Table entry to output demux
  io.done := (state === s_Done)

  /*=================================================================
  =            Default values for external communication            =
  ==================================================================*/
  io.output.valid := 0.U
  io.output.bits.valid := true.B
  io.MemReq.valid := 0.U

  /*=======================================================
=            Latch Inputs. Calculate masks              =
========================================================*/

  when(io.NodeReq.fire( )) {
    // Request address
    request_R := io.NodeReq.bits
    // request valid
    request_valid_R := true.B
    // Base word address
    ReqAddress := (io.NodeReq.bits.address >> log2Ceil(xlen_bytes)) << log2Ceil(xlen_bytes)
  }

  /*===========================================================
  =            Sending values to the cache request            =
  ===========================================================*/
  // Request address
  io.MemReq.bits.addr := ReqAddress + Cat(sendptr, 0.U(log2Ceil(xlen_bytes).W))
  // MSHR ID
  io.MemReq.bits.tag := ID
  io.MemReq.bits.data := 0.U
  io.MemReq.bits.iswrite := false.B
  io.MemReq.bits.mask := ~0.U
  io.MemReq.bits.taskID := request_R.taskID
  io.MemReq.bits.tile := 0.U

  // Memreq valid
  io.MemReq.valid := false.B

  when((sendptr =/= Beats.U) && (request_valid_R === true.B)) {
    io.MemReq.valid := true.B
    // io.MemReq.ready means arbitration succeeded and memory op has been passed on
    when(io.MemReq.fire( )) {
      // Increment the address
      sendptr := sendptr + 1.U
    }
  }

  when(io.MemResp.valid === true.B) {
    // Sending data; pick word from linebuffer
    linebuffer(recvptr) := io.MemResp.data
    // Increment to next word
    recvptr := recvptr + 1.U
  }
  // val y  = (recvptr === (Beats-1).U)
  // state := Mux(y, s_Done, s_SENDING)

  /*============================================================
  =            Cleanup and send output                         =
  =============================================================*/
  // Reg outputvalid = 
  io.output.bits.RouteID := request_R.RouteID
  io.output.bits.data := linebuffer(outptr)
  when(outptr =/= recvptr) {
    // For the demux
    io.output.valid := 1.U
    // Output driver demux tree has forwarded output (may not have reached receiving node yet)

    when(io.output.fire( ) && (outptr =/= (Beats - 1).U)) {
      outptr := outptr + 1.U
    }.elsewhen(io.output.fire( ) && (outptr === (Beats - 1).U)) {
      outptr := 0.U
      sendptr := 0.U
      recvptr := 0.U
      request_valid_R := false.B
      // linebuffer(0)   := 0.U
      // linebuffer(1)   := 0.U
    }
  }

  override val printfSigil = "RD MSHR(" + ID + "," + Typ_SZ + ")"
  if ((log == true) && (comp contains "RDMSHR")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {
        printf(p"Wr MSHR Time $x: Sendptr: $sendptr  Recvptr: $recvptr");
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

class ReadTypMemoryController(NumOps: Int,
                              BaseSize: Int, NumEntries: Int)(implicit p: Parameters)
  extends RController(NumOps, BaseSize, NumEntries)(p) {

  require(NumEntries >= 0)
  // Number of MLP entries
  val MLPSize   = NumEntries
  // Input arbiter
  val in_arb    = Module(new ArbiterTree(BaseSize = BaseSize, NumOps = NumOps, new ReadReq( ), Locks = 1))
  // MSHR allocator
  val alloc_arb = Module(new Arbiter(Bool( ), MLPSize))

  // Memory request
  val cachereq_arb    = Module(new LockingRRArbiter(new MemReq, MLPSize, count = Beats))
  // Memory response Demux
  val cacheresp_demux = Module(new Demux(new MemResp, MLPSize))

  // Output arbiter and demuxes
  val out_arb   = Module(new LockingRRArbiter(new ReadResp, MLPSize, count = Beats))
  val out_demux = Module(new DeMuxTree(BaseSize = BaseSize, NumOps = NumOps, new ReadResp( )))

  /*=====================================================================
=            Wire up incoming reads from nodes to ReadMSHR            =
=====================================================================*/

  // Wire up input with in_arb. Output with ReadOut
  for (i <- 0 until NumOps) {
    in_arb.io.in(i) <> io.ReadIn(i)
    io.ReadOut(i) <> out_demux.io.outputs(i)
    alloc_arb.io.in(i).bits := false.B
  }


  /*=============================================
=           Declare Read Table                =
=============================================*/

  // Create ReadTable
  val ReadTable = for (i <- 0 until MLPSize) yield {
    val read_entry = Module(new ReadTypTableEntry(i))
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

  for (i <- 0 until MLPSize) {
    // val MSHR = Module(new ReadTableEntry(i))
    // Allocator wireup with table entries
    alloc_arb.io.in(i).valid := ReadTable(i).io.free
    ReadTable(i).io.NodeReq.valid := alloc_arb.io.in(i).ready
    ReadTable(i).io.NodeReq.bits := in_arb.io.out.bits

    // Table entries -> MemReq arbiter.
    cachereq_arb.io.in(i) <> ReadTable(i).io.MemReq

    // MemResp -> Table entries Demux
    ReadTable(i).io.MemResp <> cacheresp_demux.io.outputs(i)

    // Table entries -> Output arbiter
    out_arb.io.in(i) <> ReadTable(i).io.output
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

  // printf(p"\n Demux output: ${cacheresp_demux.io.outputs}")

}
