package dnn.memory

import chisel3.util._
import chisel3.{Module, _}
import config._
import interfaces._
import muxes.Demux
import node._

abstract class WTController[gen <: Shapes](NumOps: Int, tensorType: String = "none")(shape: => gen)(implicit val p: Parameters)
  extends Module {
  val io = IO(new Bundle {
    val WriteIn  = Vec(NumOps, Flipped(Decoupled(new TensorWriteReq(shape.getWidth))))
    val WriteOut = Vec(NumOps, Output(new TensorWriteResp()))
    val tensor = new TensorMaster(tensorType)
  })
}


class WriteTensorController[L <: Shapes] (NumOps: Int, tensorType: String = "none")(shape: => L)(implicit p: Parameters)
  extends WTController(NumOps, tensorType)(shape)(p) {

  val arbiter = Module(new RRArbiter(new TensorWriteReq(shape.getWidth), NumOps))
  val demux = Module(new Demux(new TensorWriteResp, NumOps))

  // Wire up inputs with the arbiter and outputs with demux
  for (i <- 0 until NumOps) {
    arbiter.io.in(i) <> io.WriteIn(i)
    io.WriteOut(i) <> demux.io.outputs(i)
  }

  val arb_valid_r = RegInit(false.B)

  val chosen_reg = RegInit(0.U)
  when(arbiter.io.out.fire){
    chosen_reg := arbiter.io.chosen
    arb_valid_r := true.B
  }

  io.tensor.wr.valid := arbiter.io.out.valid
  io.tensor.wr.bits.data := arbiter.io.out.bits.data.asTypeOf(io.tensor.wr.bits.data)
  io.tensor.wr.bits.idx := arbiter.io.out.bits.index
  io.tensor.rd <> DontCare

  arbiter.io.out.ready := true.B

  demux.io.sel := chosen_reg
  demux.io.en := arb_valid_r //arbiter.io.out.valid
  demux.io.input.valid := arb_valid_r //arbiter.io.out.valid
  demux.io.input.RouteID := io.WriteIn(arbiter.io.chosen).bits.RouteID
  demux.io.input.done := arb_valid_r //arbiter.io.out.valid

}

