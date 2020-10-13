package dnn

import chisel3.util.Decoupled
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import dnn.types.{OperatorDot, OperatorReduction}
import interfaces.CustomDataBundle
import node.{HandShakingIONPS, HandShakingNPS, Shapes}

class MacIO[gen <: Shapes](NumOuts: Int)(shape: => gen)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt(p(XLEN).W))) {
  val LeftIO = Flipped(Decoupled(new CustomDataBundle(UInt(shape.getWidth.W))))
  val RightIO = Flipped(Decoupled(new CustomDataBundle(UInt(shape.getWidth.W))))
  override def cloneType = new MacIO(NumOuts)(shape).asInstanceOf[this.type]
}

class MacNode[L <: Shapes : OperatorDot : OperatorReduction](NumOuts: Int, ID: Int, lanes: Int)(shape: => L)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt(p(XLEN).W)))(p) {
  override lazy val io = IO(new MacIO(NumOuts)(shape))

  val dotNode = Module(new DotNode(NumOuts = 1, ID = ID, lanes, "Mul")(shape))
  val reduceNode = Module(new ReduceNode(NumOuts = 1, ID = ID, false, "Add")(shape))

  // Connect IO to dotNode
  dotNode.io.enable <> io.enable
  dotNode.io.LeftIO <> io.LeftIO
  dotNode.io.RightIO <> io.RightIO

  reduceNode.io.LeftIO <> dotNode.io.Out(0)
  reduceNode.io.enable <> io.enable

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i) <> reduceNode.io.Out(i)
  }
//  printf(p"\n Left ${io.LeftIO.bits.data} Right: ${io.RightIO.bits.data} Output: ${reduceNode.io.Out(0).bits.data}")
}


