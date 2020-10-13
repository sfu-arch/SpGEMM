
package dnn.memory

import Chisel.Enum
import chisel3._
import chisel3.util.{Decoupled, is, switch}
import config._
import dnnnode.WeightShapeTransformer
import interfaces.{TensorReadReq, TensorReadResp}
import node.{Shapes, vecN}
import shell._
//import vta.util.config._
import dnn.memory.ISA._


/** TensorLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class inDMA_actIO[gen <: Shapes](NumRows: Int, NumOuts: Int, memTensorType: String = "none")(memShape: => gen)(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val baddr = Input(UInt(mp.addrBits.W))
    val rowWidth = Input(UInt(mp.addrBits.W))
    val vme_rd = Vec(NumRows, new VMEReadMaster)
    val ReadIn  = Vec(NumRows, Vec(NumOuts, Flipped(Decoupled(new TensorReadReq()))))
    val ReadOut = Vec(NumRows, Vec(NumOuts, Output(new TensorReadResp(memShape.getWidth))))
  })
}

class inDMA_act[L <: Shapes](NumRows: Int, NumOuts: Int, memTensorType: String = "none")(memShape: => L)(implicit p: Parameters)
  extends inDMA_actIO(NumRows, NumOuts, memTensorType)(memShape)(p) {

  val tensorLoad = for (i <- 0 until NumRows) yield {
    val tensorL = Module(new TensorLoad(memTensorType))
    tensorL
  }
  val readTensorCtrl = for (i <- 0 until NumRows) yield {
    val readTensorController = Module(new ReadTensorController(1, memTensorType)(memShape))
    readTensorController
  }
  val doneR = for (i <- 0 until NumRows) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  io.done := doneR.reduceLeft(_ && _)

  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
  }

  for (i <- 0 until NumRows) yield{
    when (tensorLoad(i).io.done) {
      doneR(i) := true.B
    }
  }

  val tl_Inst = Wire(new MemDecode)
  val memTensorRows = Mux(io.rowWidth % tp.tensorWidth.U === 0.U, io.rowWidth / tp.tensorWidth.U, (io.rowWidth /tp.tensorWidth.U) + 1.U)

  tl_Inst.xpad_0 := 0.U
  tl_Inst.xpad_1 := 0.U
  tl_Inst.ypad_0 := 0.U
  tl_Inst.ypad_1 := 0.U
  tl_Inst.xstride := memTensorRows
  tl_Inst.xsize := memTensorRows
  tl_Inst.ysize := 1.U
  tl_Inst.empty_0 := 0.U
  tl_Inst.dram_offset := 0.U
  tl_Inst.sram_offset := 0.U
  tl_Inst.id := 3.U
  tl_Inst.push_next := 0.U
  tl_Inst.push_prev := 0.U
  tl_Inst.pop_next := 0.U
  tl_Inst.pop_prev := 0.U
  tl_Inst.op := 0.U

  for (i <- 0 until NumRows) {
    tensorLoad(i).io.start := io.start
    tensorLoad(i).io.inst := tl_Inst.asTypeOf(UInt(INST_BITS.W))
    tensorLoad(i).io.baddr := io.baddr + (i.U * io.rowWidth)
    tensorLoad(i).io.tensor <> readTensorCtrl(i).io.tensor
    io.vme_rd(i) <> tensorLoad(i).io.vme_rd
  }

  for (i <- 0 until NumRows) {
    for (j <- 0 until NumOuts) {
      readTensorCtrl(i).io.ReadIn(j) <> io.ReadIn(i)(j)
      io.ReadOut(i)(j) <> readTensorCtrl(i).io.ReadOut(j)
    }
  }




}
