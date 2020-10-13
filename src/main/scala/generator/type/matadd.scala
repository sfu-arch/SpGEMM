package dataflow

import FPU._
import accel._
import arbiters._
import chisel3._
import chisel3.util._
import chisel3.Module._
import chisel3.testers._
import chisel3.iotesters._
import config._
import control._
import interfaces._
import junctions._
import loop._
import memory._
import muxes._
import node._
import org.scalatest._
import regfile._
import stack._
import util._

/* ================================================================== *
 *                   PRINTING PORTS DEFINITION                        *
 * ================================================================== */

class mataddDFIO(implicit p: Parameters) extends CoreBundle {
  val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
  val MemResp = Flipped(Valid(new MemResp))
  val MemReq = Decoupled(new MemReq)
  val out = Decoupled(new Call(List()))
}

class mataddDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new mataddDFIO())

  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val StackFile = Module(new TypeStackFile(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteTypMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 1))
  (RControl = new ReadTypMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2)))

  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1)))
  InputSplitter.io.In <> io.in

  /**
    * Build kernel modules
    */

  //    val matadd = Module(new mataddDF()(p))

  val mat_bb = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 0))

  val LoadA = Module(new TypLoad(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 0, RouteID = 0))
  val LoadB = Module(new TypLoad(NumPredOps = 0, NumSuccOps = 1, NumOuts = 1, ID = 0, RouteID = 1))
  val StoreType = Module(new TypStore(NumPredOps = 2, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0))
  val typadd = Module(new TypCompute(NumOuts = 1, ID = 0, "Add")(true)(new matNxN(N = 2)))

  mat_bb.io.predicateIn <> InputSplitter.io.Out.enable

  LoadA.io.enable <> mat_bb.io.Out(0)
  LoadB.io.enable <> mat_bb.io.Out(1)
  StoreType.io.enable <> mat_bb.io.Out(2)
  typadd.io.enable <> mat_bb.io.Out(3)

  StackFile.io.ReadIn(0) <> LoadA.io.memReq
  LoadA.io.memResp <> StackFile.io.ReadOut(0)

  StackFile.io.ReadIn(1) <> LoadB.io.memReq
  LoadB.io.memResp <> StackFile.io.ReadOut(1)

  StackFile.io.WriteIn(0) <> StoreType.io.memReq
  StoreType.io.memResp <> StackFile.io.WriteOut(0)

  typadd.io.LeftIO <> LoadA.io.Out(0)
  typadd.io.RightIO <> LoadB.io.Out(0)


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlock(NumIns = List(1, 3, 1, 1), NumOuts = 0, NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlock(NumIns = List(1, 1, 1), NumOuts = 0, NumExits = 1, ID = 1))


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new LoopHead(NumOuts = 5, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_3 = Module(new LoopHead(NumOuts = 5, NumPhi = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 11, BID = 4))

  val bb_5 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 5))

  val bb_6 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 6))

  val bb_7 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 7))

  val bb_8 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 8))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %4
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %.01 = phi i32 [ 0, %3 ], [ %21, %20 ]
  val phi_011 = Module(new PhiNode(NumInputs = 2, NumOuts = 3, ID = 1))

  //  %5 = icmp slt i32 %.01, 8
  val icmp_2 = Module(new IcmpNode(NumOuts = 1, ID = 2, opCode = "ult")(sign = false))

  //  br i1 %5, label %6, label %22
  val br_3 = Module(new CBranchNode(ID = 3))

  //  br label %7
  val br_4 = Module(new UBranchNode(ID = 4))

  //  %.0 = phi i32 [ 0, %6 ], [ %18, %17 ]
  val phi_05 = Module(new PhiNode(NumInputs = 2, NumOuts = 5, ID = 5))

  //  %8 = icmp slt i32 %.0, 8
  val icmp_6 = Module(new IcmpNode(NumOuts = 1, ID = 6, opCode = "ult")(sign = false))

  //  br i1 %8, label %9, label %19
  val br_7 = Module(new CBranchNode(ID = 7))

  //  %10 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %0, i32 %.01
  val Gep_8 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 8)(ElementSize = 16, ArraySize = List()))

  //  %11 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %10, i32 0, i32 %.0
  val Gep_9 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 9)(ElementSize = 16, ArraySize = List()))

  //  %12 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %1, i32 %.01
  val Gep_10 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 10)(ElementSize = 16, ArraySize = List()))

  //  %13 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %12, i32 0, i32 %.0
  val Gep_11 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 11)(ElementSize = 16, ArraySize = List()))

  //  %14 = call %struct.block* @_Z7addTileP5blockS0_(%struct.block* %11, %struct.block* %13)
  //  val call_12_out = Module(new CallOutNode(ID = 12, NumSuccOps = 0, argTypes = List(32, 32)))
  //  val call_12_in = Module(new CallInNode(ID = 12, argTypes = List()))

  //  %15 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %2, i32 %.01
  val Gep_13 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 13)(ElementSize = 16, ArraySize = List()))

  //  %16 = getelementptr inbounds [8 x %struct.block], [8 x %struct.block]* %15, i32 0, i32 %.0
  val Gep_14 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 14)(ElementSize = 16, ArraySize = List()))

  //  call void @_Z9storeTileP5blockS0_(%struct.block* %14, %struct.block* %16)
  //  val call_15_out = Module(new CallOutNode(ID = 15, NumSuccOps = 0, argTypes = List(32, 32)))
  //  val call_15_in = Module(new CallInNode(ID = 15, argTypes = List()))

  //  br label %17
  val br_16 = Module(new UBranchNode(ID = 16))

  //  %18 = add nsw i32 %.0, 1
  val binaryOp_17 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "add")(sign = false))

  //  br label %7
  val br_18 = Module(new UBranchNode(NumOuts = 2, ID = 18))

  //  br label %20
  val br_19 = Module(new UBranchNode(ID = 19))

  //  %21 = add nsw i32 %.01, 1
  val binaryOp_20 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "add")(sign = false))

  //  br label %4
  val br_21 = Module(new UBranchNode(NumOuts = 2, ID = 21))

  //  ret void
  val ret_22 = Module(new RetNode2(retTypes = List(), ID = 22))


  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstNode(NumOuts = 1, value = 0, ID = 0))

  //i32 8
  val const1 = Module(new ConstNode(NumOuts = 1, value = 8, ID = 1))

  //i32 0
  val const2 = Module(new ConstNode(NumOuts = 1, value = 0, ID = 2))

  //i32 8
  val const3 = Module(new ConstNode(NumOuts = 1, value = 8, ID = 3))

  //i32 0
  val const4 = Module(new ConstNode(NumOuts = 1, value = 0, ID = 4))

  //i32 0
  val const5 = Module(new ConstNode(NumOuts = 1, value = 0, ID = 5))

  //i32 0
  val const6 = Module(new ConstNode(NumOuts = 1, value = 0, ID = 6))

  //i32 1
  val const7 = Module(new ConstNode(NumOuts = 1, value = 1, ID = 7))

  //i32 1
  val const8 = Module(new ConstNode(NumOuts = 1, value = 1, ID = 8))


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_1.io.activate <> Loop_1.io.activate

  bb_1.io.loopBack <> br_21.io.Out(0)

  bb_2.io.predicateIn <> br_3.io.Out(0)

  bb_3.io.activate <> Loop_0.io.activate

  bb_3.io.loopBack <> br_18.io.Out(0)

  bb_4.io.predicateIn <> br_7.io.Out(0)

  bb_5.io.predicateIn <> br_16.io.Out(0)

  bb_6.io.predicateIn <> Loop_0.io.endEnable

  bb_7.io.predicateIn <> br_19.io.Out(0)

  bb_8.io.predicateIn <> Loop_1.io.endEnable


  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_4.io.Out(0)

  Loop_0.io.latchEnable <> br_18.io.Out(1)

  Loop_0.io.loopExit(0) <> br_7.io.Out(1)

  Loop_1.io.enable <> br_0.io.Out(0)

  Loop_1.io.latchEnable <> br_21.io.Out(1)

  Loop_1.io.loopExit(0) <> br_3.io.Out(1)


  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.In(0) <> Loop_1.io.liveIn.elements("field0")(0)

  Loop_0.io.In(1) <> phi_011.io.Out(1)

  Loop_0.io.In(2) <> Loop_1.io.liveIn.elements("field1")(0)

  Loop_0.io.In(3) <> Loop_1.io.liveIn.elements("field2")(0)

  Loop_1.io.In(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_1.io.In(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_1.io.In(2) <> InputSplitter.io.Out.data.elements("field2")(0)


  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_8.io.baseAddress <> Loop_0.io.liveIn.elements("field0")(0)

  Gep_8.io.idx(0) <> Loop_0.io.liveIn.elements("field1")(0)

  Gep_10.io.idx(0) <> Loop_0.io.liveIn.elements("field1")(1)

  Gep_13.io.idx(0) <> Loop_0.io.liveIn.elements("field1")(2)

  Gep_10.io.baseAddress <> Loop_0.io.liveIn.elements("field2")(0)

  Gep_13.io.baseAddress <> Loop_0.io.liveIn.elements("field3")(0)


  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */


  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  const0.io.enable <> bb_1.io.Out(0)

  const1.io.enable <> bb_1.io.Out(1)

  phi_011.io.enable <> bb_1.io.Out(2)

  icmp_2.io.enable <> bb_1.io.Out(3)

  br_3.io.enable <> bb_1.io.Out(4)


  br_4.io.enable <> bb_2.io.Out(0)


  const2.io.enable <> bb_3.io.Out(0)

  const3.io.enable <> bb_3.io.Out(1)

  phi_05.io.enable <> bb_3.io.Out(2)

  icmp_6.io.enable <> bb_3.io.Out(3)

  br_7.io.enable <> bb_3.io.Out(4)


  const4.io.enable <> bb_4.io.Out(0)

  const5.io.enable <> bb_4.io.Out(1)

  const6.io.enable <> bb_4.io.Out(2)

  Gep_8.io.enable <> bb_4.io.Out(3)

  Gep_9.io.enable <> bb_4.io.Out(4)

  Gep_10.io.enable <> bb_4.io.Out(5)

  Gep_11.io.enable <> bb_4.io.Out(6)

  mat_bb.io.predicateIn <> bb_4.io.Out(7)

  Gep_13.io.enable <> bb_4.io.Out(8)

  Gep_14.io.enable <> bb_4.io.Out(9)

  br_16.io.enable <> bb_4.io.Out(10)


  const7.io.enable <> bb_5.io.Out(0)

  binaryOp_17.io.enable <> bb_5.io.Out(1)

  br_18.io.enable <> bb_5.io.Out(2)


  br_19.io.enable <> bb_6.io.Out(0)


  const8.io.enable <> bb_7.io.Out(0)

  binaryOp_20.io.enable <> bb_7.io.Out(1)

  br_21.io.enable <> bb_7.io.Out(2)


  ret_22.io.In.enable <> bb_8.io.Out(0)


  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi_011.io.Mask <> bb_1.io.MaskBB(0)

  phi_05.io.Mask <> bb_3.io.MaskBB(0)


  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi_011.io.InData(0) <> const0.io.Out(0)

  icmp_2.io.RightIO <> const1.io.Out(0)

  phi_05.io.InData(0) <> const2.io.Out(0)

  icmp_6.io.RightIO <> const3.io.Out(0)

  Gep_9.io.idx(0) <> const4.io.Out(0)

  Gep_11.io.idx(0) <> const5.io.Out(0)

  Gep_14.io.idx(0) <> const6.io.Out(0)

  binaryOp_17.io.RightIO <> const7.io.Out(0)

  binaryOp_20.io.RightIO <> const8.io.Out(0)

  icmp_2.io.LeftIO <> phi_011.io.Out(0)

  binaryOp_20.io.LeftIO <> phi_011.io.Out(2)

  br_3.io.CmpIO <> icmp_2.io.Out(0)

  icmp_6.io.LeftIO <> phi_05.io.Out(0)

  Gep_9.io.idx(1) <> phi_05.io.Out(1)

  Gep_11.io.idx(1) <> phi_05.io.Out(2)

  Gep_14.io.idx(1) <> phi_05.io.Out(3)

  binaryOp_17.io.LeftIO <> phi_05.io.Out(4)

  br_7.io.CmpIO <> icmp_6.io.Out(0)

  Gep_9.io.baseAddress <> Gep_8.io.Out(0)

  Gep_11.io.baseAddress <> Gep_10.io.Out(0)


  Gep_14.io.baseAddress <> Gep_13.io.Out(0)


  phi_05.io.InData(1) <> binaryOp_17.io.Out(0)

  phi_011.io.InData(1) <> binaryOp_20.io.Out(0)


  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  LoadA.io.GepAddr <> Gep_9.io.Out(0)
  LoadB.io.GepAddr <> Gep_11.io.Out(0)

  StoreType.io.GepAddr <> Gep_14.io.Out(0)
  StoreType.io.inData <> typadd.io.Out(0)

  StoreType.io.PredOp(0) <> LoadA.io.SuccOp(0)
  StoreType.io.PredOp(1) <> LoadB.io.SuccOp(0)

  StoreType.io.Out(0).ready := true.B

  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_22.io.Out

}

import java.io.{File, FileWriter}

object mataddMain extends App {
  val dir = new File("RTL/matadd");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new mataddDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
