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

abstract class test06DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test06DF(implicit p: Parameters) extends test06DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(3, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1), NumOuts = List(1, 1), NumCarry = List(1, 1, 1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_preheader1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_loopexit2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 4, NumPhi = 2, BID = 3))

  val bb_4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 15, NumPhi = 3, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %3 = icmp sgt i32 %0, 0, !UID !3
  val icmp_0 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "ugt")(sign = false))

  //  br i1 %3, label %.preheader, label %4, !UID !4, !BB_UID !5
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  br label %8
  val br_2 = Module(new UBranchNode(ID = 2))

  //  br label %4
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %5 = phi i32 [ %0, %2 ], [ %14, %.loopexit ], !UID !6
  val phi4 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 4, Res = true))

  //  %6 = phi i32 [ %1, %2 ], [ %16, %.loopexit ], !UID !7
  val phi5 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 5, Res = true))

  //  %7 = mul nsw i32 %6, %5, !UID !8
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "mul")(sign = false))

  //  ret i32 %7, !UID !9, !BB_UID !10
  val ret_7 = Module(new RetNode2(retTypes = List(32), ID = 7))

  //  %9 = phi i32 [ %17, %8 ], [ 0, %.preheader ], !UID !11
  val phi8 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 8, Res = true))

  //  %10 = phi i32 [ %16, %8 ], [ %1, %.preheader ], !UID !12
  val phi9 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 9, Res = true))

  //  %11 = phi i32 [ %14, %8 ], [ %0, %.preheader ], !UID !13
  val phi10 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 10, Res = true))

  //  %12 = icmp slt i32 %10, %11, !UID !14
  val icmp_11 = Module(new IcmpNode(NumOuts = 2, ID = 11, opCode = "ult")(sign = false))

  //  %13 = select i1 %12, i32 %10, i32 0, !UID !15
  val select_12 = Module(new SelectNode(NumOuts = 1, ID = 12))

  //  %14 = sub nsw i32 %11, %13, !UID !16
  val binaryOp_13 = Module(new ComputeNode(NumOuts = 3, ID = 13, opCode = "sub")(sign = false))

  //  %15 = select i1 %12, i32 0, i32 %11, !UID !17
  val select_14 = Module(new SelectNode(NumOuts = 1, ID = 14))

  //  %16 = sub nsw i32 %10, %15, !UID !18
  val binaryOp_15 = Module(new ComputeNode(NumOuts = 2, ID = 15, opCode = "sub")(sign = false))

  //  %17 = add nuw nsw i32 %9, 1, !UID !19
  val binaryOp_16 = Module(new ComputeNode(NumOuts = 2, ID = 16, opCode = "add")(sign = false))

  //  %18 = icmp slt i32 %17, %14, !UID !20
  val icmp_17 = Module(new IcmpNode(NumOuts = 1, ID = 17, opCode = "ult")(sign = false))

  //  br i1 %18, label %8, label %.loopexit, !UID !21, !BB_UID !22
  val br_18 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 18))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i32 1
  val const4 = Module(new ConstFastNode(value = 1, ID = 4))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_preheader1.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_3.io.predicateIn(1) <> br_1.io.FalseOutput(0)

  bb_3.io.predicateIn(0) <> br_3.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_loopexit2.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_4.io.predicateIn(0) <> Loop_0.io.activate_loop_start

  bb_4.io.predicateIn(1) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_2.io.Out(0)

  Loop_0.io.loopBack(0) <> br_18.io.TrueOutput(0)

  Loop_0.io.loopFinish(0) <> br_18.io.FalseOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(2)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phi10.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  phi9.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_0.io.InLiveOut(0) <> binaryOp_13.io.Out(0)

  Loop_0.io.InLiveOut(1) <> binaryOp_15.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phi4.io.InData(1) <> Loop_0.io.OutLiveOut.elements("field0")(0)

  phi5.io.InData(1) <> Loop_0.io.OutLiveOut.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_13.io.Out(1)

  Loop_0.io.CarryDepenIn(1) <> binaryOp_15.io.Out(1)

  Loop_0.io.CarryDepenIn(2) <> binaryOp_16.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi10.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi9.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field1")(0)

  phi8.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field2")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_0.io.Out(0)

  icmp_0.io.enable <> bb_0.io.Out(1)

  br_1.io.enable <> bb_0.io.Out(2)


  br_2.io.enable <> bb_preheader1.io.Out(0)


  br_3.io.enable <> bb_loopexit2.io.Out(0)


  phi4.io.enable <> bb_3.io.Out(0)

  phi5.io.enable <> bb_3.io.Out(1)

  binaryOp_6.io.enable <> bb_3.io.Out(2)

  ret_7.io.In.enable <> bb_3.io.Out(3)


  const1.io.enable <> bb_4.io.Out(0)

  const2.io.enable <> bb_4.io.Out(1)

  const3.io.enable <> bb_4.io.Out(2)

  const4.io.enable <> bb_4.io.Out(3)

  phi8.io.enable <> bb_4.io.Out(4)

  phi9.io.enable <> bb_4.io.Out(5)

  phi10.io.enable <> bb_4.io.Out(6)

  icmp_11.io.enable <> bb_4.io.Out(7)

  select_12.io.enable <> bb_4.io.Out(8)

  binaryOp_13.io.enable <> bb_4.io.Out(9)

  select_14.io.enable <> bb_4.io.Out(10)

  binaryOp_15.io.enable <> bb_4.io.Out(11)

  binaryOp_16.io.enable <> bb_4.io.Out(12)

  icmp_17.io.enable <> bb_4.io.Out(13)

  br_18.io.enable <> bb_4.io.Out(14)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi4.io.Mask <> bb_3.io.MaskBB(0)

  phi5.io.Mask <> bb_3.io.MaskBB(1)

  phi8.io.Mask <> bb_4.io.MaskBB(0)

  phi9.io.Mask <> bb_4.io.MaskBB(1)

  phi10.io.Mask <> bb_4.io.MaskBB(2)



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

  icmp_0.io.RightIO <> const0.io.Out

  phi8.io.InData(1) <> const1.io.Out

  select_12.io.InData2 <> const2.io.Out

  select_14.io.InData1 <> const3.io.Out

  binaryOp_16.io.RightIO <> const4.io.Out

  br_1.io.CmpIO <> icmp_0.io.Out(0)

  binaryOp_6.io.RightIO <> phi4.io.Out(0)

  binaryOp_6.io.LeftIO <> phi5.io.Out(0)

  ret_7.io.In.data("field0") <> binaryOp_6.io.Out(0)

  binaryOp_16.io.LeftIO <> phi8.io.Out(0)

  icmp_11.io.LeftIO <> phi9.io.Out(0)

  select_12.io.InData1 <> phi9.io.Out(1)

  binaryOp_15.io.LeftIO <> phi9.io.Out(2)

  icmp_11.io.RightIO <> phi10.io.Out(0)

  binaryOp_13.io.LeftIO <> phi10.io.Out(1)

  select_14.io.InData2 <> phi10.io.Out(2)

  select_12.io.Select <> icmp_11.io.Out(0)

  select_14.io.Select <> icmp_11.io.Out(1)

  binaryOp_13.io.RightIO <> select_12.io.Out(0)

  icmp_17.io.RightIO <> binaryOp_13.io.Out(2)

  binaryOp_15.io.RightIO <> select_14.io.Out(0)

  icmp_17.io.LeftIO <> binaryOp_16.io.Out(1)

  br_18.io.CmpIO <> icmp_17.io.Out(0)

  icmp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(0)

  phi4.io.InData(0) <> InputSplitter.io.Out.data.elements("field0")(1)

  phi5.io.InData(0) <> InputSplitter.io.Out.data.elements("field1")(0)



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}

object test06Top extends App {
  val dir = new File("RTL/test06Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test06DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
