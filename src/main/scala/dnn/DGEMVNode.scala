package dnnnode

import FPU.FType
import chisel3._
import chisel3.util._
import config._
import dnn.{MacNode, ReduceNode}
import dnn.memory.TensorParams
import dnn.types.{OperatorDot, OperatorReduction}
import interfaces.{ControlBundle, CustomDataBundle, TensorReadReq, TensorReadResp}
import node.{FPvecN, HandShakingIONPS, HandShakingNPS, Shapes, vecN}
import shell._
//import vta.util.config._


/** TensorLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class DGEMVNodeIO[gen <: Shapes](NumRows: Int, NumOuts: Int)
                                            (vecShape: => gen)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt((p(XLEN) * NumRows).W))) {
  val mat = Vec(NumRows ,Flipped(Decoupled(new CustomDataBundle(UInt(vecShape.getWidth.W)))))
  val vec = Flipped(Decoupled(new CustomDataBundle(UInt(vecShape.getWidth.W))))

  override def cloneType = new DGEMVNodeIO(NumRows, NumOuts)(vecShape).asInstanceOf[this.type]
}

class DGEMVNode[L <: Shapes: OperatorDot: OperatorReduction](NumRows: Int, NumOuts: Int)(vecShape: => L)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, 0)(new CustomDataBundle(UInt((p(XLEN) * NumRows).W)))(p) {
  override lazy val io = IO(new DGEMVNodeIO(NumRows, NumOuts)(vecShape))

  val mac = for (i <- 0 until NumRows) yield {
    val macNode = Module(new MacNode(NumOuts = 1, ID = 0, lanes = vecShape.getLength())(vecShape))
    macNode
  }

  for (i <- 0 until NumRows) {

    mac(i).io.enable.bits <> ControlBundle.active()
    mac(i).io.enable.valid := true.B

    mac(i).io.LeftIO <> io.mat(i)
    mac(i).io.RightIO <> io.vec

    mac(i).io.Out(0).ready := io.Out.map(_.ready).reduceLeft(_ && _)
  }

  for(i <- 0 until NumOuts){
    io.Out(i).bits.data := VecInit(mac.map(_.io.Out(0).bits.data)).asTypeOf(CustomDataBundle(UInt((NumRows * xlen).W)))
    io.Out(i).bits.valid := mac.map(_.io.Out(0).bits.valid).reduceLeft(_ && _)
    io.Out(i).bits.predicate := true.B
    io.Out(i).bits.taskID := 0.U
    io.Out(i).valid := mac.map(_.io.Out(0).valid).reduceLeft(_ && _)
  }

}

