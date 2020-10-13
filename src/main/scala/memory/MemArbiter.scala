package memory


import chisel3._
import chisel3.Module
import chisel3.util._


// Config
import config._
import utility._
import interfaces._


class MemArbiterIO(NumPorts:Int)(implicit val p: Parameters)
  extends Module with CoreParams  with UniformPrintfs {
  val io = IO(new Bundle {
    val cpu = new Bundle {
      val MemReq   = Vec(NumPorts, Flipped(Decoupled(new MemReq)))
      val MemResp  = Vec(NumPorts, Output(Valid(new MemResp)))
    }
    val cache = new Bundle {
      val MemReq   = Decoupled(new MemReq)
      val MemResp  = Input(Valid(new MemResp))
      val chosen   = Output(UInt(log2Ceil(NumPorts).W))
    }
  })
}

class MemArbiter(NumPorts:Int)(implicit p: Parameters) extends MemArbiterIO(NumPorts)(p) {

  val reqArb  = Module(new RRArbiter(new MemReq, NumPorts))
  reqArb.io.in <> io.cpu.MemReq
  val chosen_reg = RegInit(0.U)
  when (reqArb.io.out.fire()) {
    chosen_reg := reqArb.io.chosen
  }
  io.cache.MemReq <> reqArb.io.out
  io.cache.chosen := reqArb.io.chosen

  // Response Demux
  for(i <- 0 until NumPorts) {
    io.cpu.MemResp(i).bits  := io.cache.MemResp.bits
    io.cpu.MemResp(i).valid := false.B  // default
  }
  io.cpu.MemResp(chosen_reg).valid := io.cache.MemResp.valid

}
