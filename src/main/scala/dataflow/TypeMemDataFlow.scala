package dataflow

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._
import FPU.FType._

class TypeMemDataFlow(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val dummy = Input(UInt {
      32.W
    })
  })

  val StackFile = Module(new TypeStackFile(ID = 0, Size = 32, NReads = 2, NWrites = 2)
  (WControl = new WriteTypMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadTypMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2)))

  val Store = Module(new TypStore(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 0, RouteID = 0))
  val Load = Module(new TypLoad(NumPredOps = 1, NumSuccOps = 0, NumOuts = 2, ID = 0, RouteID = 0))
  val Store1 = Module(new TypStore(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 1, RouteID = 1))
  val Load1 = Module(new TypLoad(NumPredOps = 1, NumSuccOps = 0, NumOuts = 2, ID = 1, RouteID = 1))


  StackFile.io.ReadIn(0) <> Load.io.memReq
  Load.io.memResp <> StackFile.io.ReadOut(0)

  StackFile.io.WriteIn(0) <> Store.io.memReq
  Store.io.memResp <> StackFile.io.WriteOut(0)


  Store.io.GepAddr.bits.data := 8.U
  Store.io.GepAddr.bits.predicate := true.B
  Store.io.GepAddr.valid := true.B
  Store.io.GepAddr.bits.taskID := 0.U

  Store.io.inData.bits.data := 0x4400440044004400L.U
  Store.io.inData.bits.predicate := true.B
  Store.io.inData.valid := true.B
  Store.io.inData.bits.taskID := 0.U
  Store.io.inData.bits.valid := false.B

  Store.io.enable.bits.control := true.B
  Store.io.enable.valid := true.B
  Store.io.enable.bits.taskID := 0.U
  Store.io.Out(0).ready := true.B


  Load.io.GepAddr.bits.data := 8.U
  Load.io.GepAddr.bits.predicate := true.B
  Load.io.GepAddr.valid := true.B
  Load.io.GepAddr.bits.taskID := 0.U

  Load.io.enable.bits.control := true.B
  Load.io.enable.valid := true.B
  Load.io.enable.bits.taskID := 0.U
  Load.io.Out(0).ready := true.B

  Load.io.PredOp(0) <> Store.io.SuccOp(0)


  /*   Connect up second pair of ops */

  StackFile.io.ReadIn(1) <> Load1.io.memReq
  Load1.io.memResp <> StackFile.io.ReadOut(1)

  StackFile.io.WriteIn(1) <> Store1.io.memReq
  Store1.io.memResp <> StackFile.io.WriteOut(1)


  Store1.io.GepAddr.bits.data := 16.U
  Store1.io.GepAddr.bits.predicate := true.B
  Store1.io.GepAddr.valid := true.B
  Store1.io.GepAddr.bits.taskID := 0.U

  Store1.io.inData.bits.data := 0x4c0044004c004400L.U
  Store1.io.inData.bits.predicate := true.B
  Store1.io.inData.valid := true.B
  Store1.io.inData.bits.taskID := 0.U
  Store1.io.inData.bits.valid := true.B

  Store1.io.enable.bits.control := true.B
  Store1.io.enable.valid := true.B
  Store1.io.enable.bits.taskID := 0.U
  Store1.io.Out(0).ready := true.B

  Load1.io.GepAddr.bits.data := 16.U
  Load1.io.GepAddr.bits.predicate := true.B
  Load1.io.GepAddr.valid := true.B
  Load1.io.GepAddr.bits.taskID := 0.U

  Load1.io.enable.bits.control := true.B
  Load1.io.enable.valid := true.B
  Load1.io.enable.bits.taskID := 0.U
  Load1.io.Out(0).ready := true.B

  Load1.io.PredOp(0) <> Store1.io.SuccOp(0)

  val typadd = Module(new TypCompute(NumOuts = 1, ID = 0, "Mul")(true)(new FPmatNxN(N = 2, t = H)))
  typadd.io.enable.bits.control := true.B
  typadd.io.enable.bits.taskID := 0.U
  typadd.io.enable.valid := true.B
  typadd.io.Out(0).ready := true.B
  typadd.io.LeftIO <> Load.io.Out(1)
  typadd.io.RightIO <> Load1.io.Out(1)
}

