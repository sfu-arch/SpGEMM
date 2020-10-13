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

abstract class ms_mergesortDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32))))
    val call_11_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_11_in = Flipped(Decoupled(new Call(List())))
    val call_13_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_13_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class ms_mergesortDF(implicit p: Parameters) extends ms_mergesortDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(2, 1, 2), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 5, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 9, NumPhi = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 4))

  val bb_5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 4, BID = 5))

  val bb_6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 6))

  val bb_7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 7))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %2, !dbg !77, !UID !79, !BB_UID !80
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %3 = phi i32 [ 1, %1 ], [ %18, %17 ], !UID !81
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 1, Res = false))

  //  %4 = shl i32 %3, 1, !UID !83
  val binaryOp_2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "shl")(sign = false))

  //  br label %5, !dbg !84, !UID !88, !BB_UID !89
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %6 = phi i32 [ 0, %2 ], [ %15, %14 ], !UID !90
  val phi4 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 4, Res = false))

  //  %7 = add nsw i32 %6, %3, !dbg !92, !UID !95
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 2, ID = 5, opCode = "add")(sign = false))

  //  %8 = add nsw i32 %7, -1, !dbg !96, !UID !97
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 2, ID = 6, opCode = "add")(sign = false))

  //  %9 = add nsw i32 %7, %3, !dbg !99, !UID !100
  val binaryOp_7 = Module(new ComputeNode(NumOuts = 2, ID = 7, opCode = "add")(sign = false))

  //  %10 = icmp slt i32 %9, 2049, !dbg !102, !UID !104
  val icmp_8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "ult")(sign = false))

  //  br i1 %10, label %11, label %13, !dbg !105, !UID !106, !BB_UID !107
  val br_9 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 9))

  //  %12 = add nsw i32 %9, -1, !dbg !108, !UID !109
  val binaryOp_10 = Module(new ComputeNode(NumOuts = 1, ID = 10, opCode = "add")(sign = false))

  //  tail call void @merge(i64* %0, i32 %6, i32 %8, i32 %12), !dbg !110, !UID !112
  val call_11_out = Module(new CallOutNode(ID = 11, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_11_in = Module(new CallInNode(ID = 11, argTypes = List()))

  //  br label %14, !dbg !113, !UID !114, !BB_UID !115
  val br_12 = Module(new UBranchNode(NumPredOps=1, ID = 12))

  //  tail call void @merge(i64* %0, i32 %6, i32 %8, i32 2048), !dbg !116, !UID !118
  val call_13_out = Module(new CallOutNode(ID = 13, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_13_in = Module(new CallInNode(ID = 13, argTypes = List()))

  //  br label %14, !UID !119, !BB_UID !120
  val br_14 = Module(new UBranchNode(NumPredOps=1, ID = 14))

  //  %15 = add nsw i32 %6, %4, !dbg !121, !UID !122
  val binaryOp_15 = Module(new ComputeNode(NumOuts = 2, ID = 15, opCode = "add")(sign = false))

  //  %16 = icmp slt i32 %15, 2048, !dbg !123, !UID !124
  val icmp_16 = Module(new IcmpNode(NumOuts = 1, ID = 16, opCode = "ult")(sign = false))

  //  br i1 %16, label %5, label %17, !dbg !84, !llvm.loop !125, !UID !127, !BB_UID !128
  val br_17 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 17))

  //  %18 = shl nsw i32 %3, 1, !dbg !129, !UID !130
  val binaryOp_18 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "shl")(sign = false))

  //  %19 = icmp slt i32 %3, 1024, !dbg !131, !UID !132
  val icmp_19 = Module(new IcmpNode(NumOuts = 1, ID = 19, opCode = "ult")(sign = false))

  //  br i1 %19, label %2, label %20, !dbg !77, !llvm.loop !133, !UID !135, !BB_UID !136
  val br_20 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 20))

  //  ret void, !dbg !137, !UID !138, !BB_UID !139
  val ret_21 = Module(new RetNode2(retTypes = List(), ID = 21))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 1
  val const0 = Module(new ConstFastNode(value = 1, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 -1
  val const3 = Module(new ConstFastNode(value = -1, ID = 3))

  //i32 2049
  val const4 = Module(new ConstFastNode(value = 2049, ID = 4))

  //i32 -1
  val const5 = Module(new ConstFastNode(value = -1, ID = 5))

  //i32 2048
  val const6 = Module(new ConstFastNode(value = 2048, ID = 6))

  //i32 2048
  val const7 = Module(new ConstFastNode(value = 2048, ID = 7))

  //i32 1
  val const8 = Module(new ConstFastNode(value = 1, ID = 8))

  //i32 1024
  val const9 = Module(new ConstFastNode(value = 1024, ID = 9))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_3.io.predicateIn(0) <> br_9.io.TrueOutput(0)

  bb_4.io.predicateIn(0) <> br_9.io.FalseOutput(0)

  bb_5.io.predicateIn(0) <> br_12.io.Out(0)

  bb_5.io.predicateIn(1) <> br_14.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_1.io.predicateIn(0) <> Loop_1.io.activate_loop_start

  bb_1.io.predicateIn(1) <> Loop_1.io.activate_loop_back

  bb_2.io.predicateIn(0) <> Loop_0.io.activate_loop_start

  bb_2.io.predicateIn(1) <> Loop_0.io.activate_loop_back

  bb_6.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_7.io.predicateIn(0) <> Loop_1.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_3.io.Out(0)

  Loop_0.io.loopBack(0) <> br_17.io.TrueOutput(0)

  Loop_0.io.loopFinish(0) <> br_17.io.FalseOutput(0)

  Loop_1.io.enable <> br_0.io.Out(0)

  Loop_1.io.loopBack(0) <> br_20.io.TrueOutput(0)

  Loop_1.io.loopFinish(0) <> br_20.io.FalseOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> phi1.io.Out(0)

  Loop_0.io.InLiveIn(1) <> binaryOp_2.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_5.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  binaryOp_7.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(1)

  binaryOp_15.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  call_11_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field2")(0)

  call_13_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field2")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_15.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_18.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi4.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi1.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  const0.io.enable <> bb_1.io.Out(0)

  const1.io.enable <> bb_1.io.Out(1)

  phi1.io.enable <> bb_1.io.Out(2)


  binaryOp_2.io.enable <> bb_1.io.Out(3)


  br_3.io.enable <> bb_1.io.Out(4)


  const2.io.enable <> bb_2.io.Out(0)

  const3.io.enable <> bb_2.io.Out(1)

  const4.io.enable <> bb_2.io.Out(2)

  phi4.io.enable <> bb_2.io.Out(3)


  binaryOp_5.io.enable <> bb_2.io.Out(4)


  binaryOp_6.io.enable <> bb_2.io.Out(5)


  binaryOp_7.io.enable <> bb_2.io.Out(6)


  icmp_8.io.enable <> bb_2.io.Out(7)


  br_9.io.enable <> bb_2.io.Out(8)


  const5.io.enable <> bb_3.io.Out(0)

  binaryOp_10.io.enable <> bb_3.io.Out(1)


  call_11_in.io.enable <> bb_3.io.Out(3)

  call_11_out.io.enable <> bb_3.io.Out(2)


  br_12.io.enable <> bb_3.io.Out(4)


  const6.io.enable <> bb_4.io.Out(0)

  call_13_in.io.enable <> bb_4.io.Out(2)

  call_13_out.io.enable <> bb_4.io.Out(1)


  br_14.io.enable <> bb_4.io.Out(3)


  const7.io.enable <> bb_5.io.Out(0)

  binaryOp_15.io.enable <> bb_5.io.Out(1)


  icmp_16.io.enable <> bb_5.io.Out(2)


  br_17.io.enable <> bb_5.io.Out(3)


  const8.io.enable <> bb_6.io.Out(0)

  const9.io.enable <> bb_6.io.Out(1)

  binaryOp_18.io.enable <> bb_6.io.Out(2)


  icmp_19.io.enable <> bb_6.io.Out(3)


  br_20.io.enable <> bb_6.io.Out(4)


  ret_21.io.In.enable <> bb_7.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi4.io.Mask <> bb_2.io.MaskBB(0)



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

  phi1.io.InData(0) <> const0.io.Out

  binaryOp_2.io.RightIO <> const1.io.Out

  phi4.io.InData(0) <> const2.io.Out

  binaryOp_6.io.RightIO <> const3.io.Out

  icmp_8.io.RightIO <> const4.io.Out

  binaryOp_10.io.RightIO <> const5.io.Out

  call_13_out.io.In.elements("field3") <> const6.io.Out

  icmp_16.io.RightIO <> const7.io.Out

  binaryOp_18.io.RightIO <> const8.io.Out

  icmp_19.io.RightIO <> const9.io.Out

  binaryOp_2.io.LeftIO <> phi1.io.Out(1)

  binaryOp_18.io.LeftIO <> phi1.io.Out(2)

  icmp_19.io.LeftIO <> phi1.io.Out(3)

  binaryOp_5.io.LeftIO <> phi4.io.Out(0)

  call_11_out.io.In.elements("field1") <> phi4.io.Out(1)

  call_13_out.io.In.elements("field1") <> phi4.io.Out(2)

  binaryOp_15.io.LeftIO <> phi4.io.Out(3)

  binaryOp_6.io.LeftIO <> binaryOp_5.io.Out(0)

  binaryOp_7.io.LeftIO <> binaryOp_5.io.Out(1)

  call_11_out.io.In.elements("field2") <> binaryOp_6.io.Out(0)

  call_13_out.io.In.elements("field2") <> binaryOp_6.io.Out(1)

  icmp_8.io.LeftIO <> binaryOp_7.io.Out(0)

  binaryOp_10.io.LeftIO <> binaryOp_7.io.Out(1)

  br_9.io.CmpIO <> icmp_8.io.Out(0)

  call_11_out.io.In.elements("field3") <> binaryOp_10.io.Out(0)

  icmp_16.io.LeftIO <> binaryOp_15.io.Out(1)

  br_17.io.CmpIO <> icmp_16.io.Out(0)

  br_20.io.CmpIO <> icmp_19.io.Out(0)



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_11_in.io.In <> io.call_11_in

  io.call_11_out <> call_11_out.io.Out(0)

  br_12.io.PredOp(0) <> call_11_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_13_in.io.In <> io.call_13_in

  io.call_13_out <> call_13_out.io.Out(0)

  br_14.io.PredOp(0) <> call_13_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_21.io.Out

}

import java.io.{File, FileWriter}

object ms_mergesortTop extends App {
  val dir = new File("RTL/ms_mergesortTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new ms_mergesortDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
