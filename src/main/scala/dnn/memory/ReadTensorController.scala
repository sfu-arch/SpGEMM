package dnn.memory

import chisel3.{Module, _}
import chisel3.util._
import muxes._
import config._
import interfaces._
import node._

abstract class RTController[gen <: Shapes](NumOps: Int, tensorType: String = "none")(shape: => gen)(implicit val p: Parameters)
  extends Module {
  val io = IO(new Bundle {
    val ReadIn  = Vec(NumOps, Flipped(Decoupled(new TensorReadReq())))
    val ReadOut = Vec(NumOps, Output(new TensorReadResp(shape.getWidth)))
    val tensor = new TensorMaster(tensorType)
  })
}

class ReadTensorController[L <: Shapes] (NumOps: Int, tensorType: String = "none")(shape: => L)(implicit p: Parameters)
  extends RTController(NumOps, tensorType)(shape)(p) {

  val arbiter = Module(new RRArbiter(new TensorReadReq, NumOps))
  val demux = Module(new Demux(new TensorReadResp(shape.getWidth), NumOps))

  // Wire up inputs with the arbiter and outputs with demux
  for (i <- 0 until NumOps) {
    arbiter.io.in(i) <> io.ReadIn(i)
    io.ReadOut(i) <> demux.io.outputs(i)
  }

  val chosen_reg = RegInit(0.U)
  when(arbiter.io.out.fire){
    chosen_reg := arbiter.io.chosen
  }

  demux.io.sel := chosen_reg
  //demux.io.sel := arbiter.io.chosen
  demux.io.en := io.tensor.rd.data.valid
  demux.io.input.data := io.tensor.rd.data.bits.asUInt()
  demux.io.input.valid := io.tensor.rd.data.valid
  // We don't have RouteID in TensorMaster interface !!!!
  demux.io.input.RouteID := arbiter.io.chosen

  io.tensor.rd.idx.bits := arbiter.io.out.bits.index
  io.tensor.rd.idx.valid := arbiter.io.out.valid
  arbiter.io.out.ready := true.B

  val s_idle:: s_wait :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state){
    is(s_idle){
      arbiter.io.out.ready := true.B
      when(arbiter.io.out.fire){
        state := s_wait
      }
    }
    is(s_wait){
      arbiter.io.out.ready := false.B
      when(io.tensor.rd.data.valid){
        state := s_idle
      }
    }
  }


  io.tensor.wr <> DontCare
}

