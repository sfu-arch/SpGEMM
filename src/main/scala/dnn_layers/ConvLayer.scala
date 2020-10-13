package dnn_layers

import arbiters._
import chisel3._
import chisel3.util.{Decoupled, Valid}
import chisel3.{Module, UInt, printf, _}
import config.{CoreBundle, CoreParams, Parameters}
import control.BasicBlockNoMaskNode
import dnn.types.OperatorDot
import dnn.{DotIO, DotNode, ReduceNode}
import interfaces.{Call, CustomDataBundle, MemReq, MemResp}
import junctions.SplitCallNew
import memory.{ReadTypMemoryController, WriteTypMemoryController}
import node.{FXmatNxN, TypCompute, TypLoad, TypStore, matNxN}
//import javafx.scene.chart.PieChart.Data
import node.{HandShakingNPS, Shapes}
/* ================================================================== *
 *                   PRINTING PORTS DEFINITION                        *
 * ================================================================== */

class convLayerIO(implicit p: Parameters) extends CoreBundle {
  val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
  val MemResp = Flipped(Valid(new MemResp))
  val MemReq = Decoupled(new MemReq)
  val out = Decoupled(new Call(List()))
}

class convLayer(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new convLayerIO())
  val shape = new matNxN(2, false)

  val StackFile = Module(new TypeStackFile(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteTypMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 1))
  (RControl = new ReadTypMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2)))

  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1)))
  InputSplitter.io.In <> io.in


  val conv_bb = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val LoadA = Module(new TypLoad(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 0, RouteID = 0))
  val LoadB = Module(new TypLoad(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 0, RouteID = 1))
  val StoreType = Module(new TypStore(NumPredOps = 2, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0))

  val dotNode = Module(new DotNode(NumOuts = 1, ID = 0, 4, "Mul")(shape))
  val reduceNode = Module(new ReduceNode(NumOuts = 1, ID = 1, false, "Add")(shape))

  conv_bb.io.predicateIn <> InputSplitter.io.Out.enable
  /* ================================================================== *
   *                          Enable signals                            *
   * ================================================================== */

  LoadA.io.enable <> conv_bb.io.Out(0)
  LoadB.io.enable <> conv_bb.io.Out(1)
  StoreType.io.enable <> conv_bb.io.Out(2)
  dotNode.io.enable <> conv_bb.io.Out(3)
  reduceNode.io.enable <> conv_bb.io.Out(4)


  StackFile.io.ReadIn(0) <> LoadA.io.memReq
  LoadA.io.memResp <> StackFile.io.ReadOut(0)

  StackFile.io.ReadIn(1) <> LoadB.io.memReq
  LoadB.io.memResp <> StackFile.io.ReadOut(1)

  StackFile.io.WriteIn(0) <> StoreType.io.memReq
  StoreType.io.memResp <> StackFile.io.WriteOut(0)

  dotNode.io.LeftIO <> LoadA.io.Out(0)
  dotNode.io.RightIO <> LoadB.io.Out(0)

  reduceNode.io.LeftIO <> dotNode.io.Out(0)


}


