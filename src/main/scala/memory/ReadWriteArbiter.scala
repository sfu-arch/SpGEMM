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


abstract class RWController(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val ReadMemReq  = Flipped(Decoupled(new MemReq))
    val WriteMemReq = Flipped(Decoupled(new MemReq))
    val MemResp     = Flipped(Valid(new MemResp))

    val ReadMemResp  = Valid(new MemResp)
    val WriteMemResp = Valid(new MemResp)
    val MemReq       = Decoupled(new MemReq)

  })
}


class ReadWriteArbiter()
                      (implicit p: Parameters)
  extends RWController( )(p) {

  //ToDo : Need to remove this 
  val MLPSize = 2
  val RdIdx   = 0
  val WrIdx   = 1


  // Memory request
  val cachereq_arb    = Module(new RRArbiter(new MemReq, MLPSize))
  // Memory response Demux
  val cacheresp_demux = Module(new Demux(new MemResp, MLPSize))

  override val printfSigil = "ReadWriteArbiter: "

  //-----------------------------------

  // Table entries -> MemReq arbiter.
  cachereq_arb.io.in(RdIdx) <> io.ReadMemReq
  cachereq_arb.io.in(WrIdx) <> io.WriteMemReq
  // Cache request arbiter
  cachereq_arb.io.out.ready := io.MemReq.ready
  io.MemReq.bits := cachereq_arb.io.out.bits
  io.MemReq.valid := cachereq_arb.io.out.valid


  //-----------------------------------
  // MemResp -> Table entries Demux
  // cacheresp_demux.io.outputs.bits.isSt is an extra field not in Rd/WrMemResp
  io.ReadMemResp.bits <> cacheresp_demux.io.outputs(RdIdx)
  io.ReadMemResp.valid := cacheresp_demux.io.outputs(RdIdx).valid
  io.WriteMemResp.bits <> cacheresp_demux.io.outputs(WrIdx)
  io.WriteMemResp.valid := cacheresp_demux.io.outputs(WrIdx).valid

  //-----------------------------------
  // Driver Circuit
  // Cache response Demux
  cacheresp_demux.io.en := io.MemResp.valid
  cacheresp_demux.io.input := io.MemResp.bits
  //Note RdIdx == 0 , so is isSt for Loads
  //ToDO this could be dangerous - fix this
  cacheresp_demux.io.sel := io.MemResp.bits.iswrite
  //-----------------------------------

  //  assert(!io.MemResp.valid, " CACHE RESPONSE IS VALID ")


  //  verb match {
  //      case "high"  => {
  //
  //        printfInfo(s" INPUT.READREQ: valid: %d ready: %d addr: %d data: %d, iswrite: %d \n", io.ReadMemReq.valid,
  //          io.ReadMemReq.ready, io.ReadMemReq.bits.addr, io.ReadMemReq.bits.data, io.ReadMemReq.bits.iswrite)
  //        printfInfo(s"INPUT.WRITEREQ: valid: %d ready:%d addr: %d data:%d iswrite:%d \n", io.WriteMemReq.valid,
  //          io.WriteMemReq.ready, io.WriteMemReq.bits.addr, io.WriteMemReq.bits.data, io.WriteMemReq.bits.iswrite)
  //
  //        printfInfo(s" OUTPUT Req valid: %d addr: %d data:%d  tag: %d  ready:%d iswrite:%d \n", io.MemReq.valid ,
  //          io.MemReq.bits.addr,io.MemReq.bits.data ,io.MemReq.bits.tag, io.MemReq.ready, io.MemReq.bits.iswrite )
  //
  //
  //        printfInfo(s" OUTPUT Resp valid: %d isSt: %d  tag: %d \n", io.MemResp.valid ,io.MemResp.bits.isSt, io.MemResp.bits.tag )
  //
  //
  //
  //      }
  //    }

}
