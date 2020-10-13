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

abstract class test04DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test04DF(implicit p: Parameters) extends test04DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(2, 1, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(2, 1, 1), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_preheader1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 9, NumPhi = 2, BID = 2))

  val bb_loopexit3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 2, NumPhi = 1, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %4 = icmp sgt i32 %2, 0, !UID !3
  val icmp_0 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "ugt")(sign = false))

  //  br i1 %4, label %.preheader, label %12, !UID !4, !BB_UID !5
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  br label %5
  val br_2 = Module(new UBranchNode(ID = 2))

  //  %6 = phi i32 [ %10, %5 ], [ 0, %.preheader ], !UID !6
  val phi3 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 3, Res = true))

  //  %7 = phi i32 [ %9, %5 ], [ %0, %.preheader ], !UID !7
  val phi4 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 4, Res = true))

  //  %8 = add i32 %7, %0, !UID !8
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "add")(sign = false))

  //  %9 = mul i32 %8, %1, !UID !9
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 2, ID = 6, opCode = "mul")(sign = false))

  //  %10 = add nuw nsw i32 %6, 1, !UID !10
  val binaryOp_7 = Module(new ComputeNode(NumOuts = 2, ID = 7, opCode = "add")(sign = false))

  //  %11 = icmp eq i32 %10, %2, !UID !11
  val icmp_8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "eq")(sign = false))

  //  br i1 %11, label %.loopexit, label %5, !UID !12, !BB_UID !13
  val br_9 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 9))

  //  br label %12
  val br_10 = Module(new UBranchNode(ID = 10))

  //  %13 = phi i32 [ %0, %3 ], [ %9, %.loopexit ], !UID !14
  val phi11 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 11, Res = true))

  //  ret i32 %13, !UID !15, !BB_UID !16
  val ret_12 = Module(new RetNode2(retTypes = List(32), ID = 12))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 1
  val const2 = Module(new ConstFastNode(value = 1, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_preheader1.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_4.io.predicateIn(1) <> br_1.io.FalseOutput(0)

  bb_4.io.predicateIn(0) <> br_10.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_2.io.predicateIn(0) <> Loop_0.io.activate_loop_start

  bb_2.io.predicateIn(1) <> Loop_0.io.activate_loop_back

  bb_loopexit3.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_2.io.Out(0)

  Loop_0.io.loopBack(0) <> br_9.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_9.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phi4.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  binaryOp_5.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(1)

  binaryOp_6.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  icmp_8.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_0.io.InLiveOut(0) <> binaryOp_6.io.Out(1)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phi11.io.InData(1) <> Loop_0.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_7.io.Out(0)

  Loop_0.io.CarryDepenIn(1) <> binaryOp_6.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi3.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi4.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field1")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_0.io.Out(0)

  icmp_0.io.enable <> bb_0.io.Out(1)

  br_1.io.enable <> bb_0.io.Out(2)


  br_2.io.enable <> bb_preheader1.io.Out(0)


  const1.io.enable <> bb_2.io.Out(0)

  const2.io.enable <> bb_2.io.Out(1)

  phi3.io.enable <> bb_2.io.Out(2)

  phi4.io.enable <> bb_2.io.Out(3)

  binaryOp_5.io.enable <> bb_2.io.Out(4)

  binaryOp_6.io.enable <> bb_2.io.Out(5)

  binaryOp_7.io.enable <> bb_2.io.Out(6)

  icmp_8.io.enable <> bb_2.io.Out(7)

  br_9.io.enable <> bb_2.io.Out(8)


  br_10.io.enable <> bb_loopexit3.io.Out(0)


  phi11.io.enable <> bb_4.io.Out(0)

  ret_12.io.In.enable <> bb_4.io.Out(1)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi3.io.Mask <> bb_2.io.MaskBB(0)

  phi4.io.Mask <> bb_2.io.MaskBB(1)

  phi11.io.Mask <> bb_4.io.MaskBB(0)



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

  phi3.io.InData(1) <> const1.io.Out

  binaryOp_7.io.RightIO <> const2.io.Out

  br_1.io.CmpIO <> icmp_0.io.Out(0)

  binaryOp_7.io.LeftIO <> phi3.io.Out(0)

  binaryOp_5.io.LeftIO <> phi4.io.Out(0)

  binaryOp_6.io.LeftIO <> binaryOp_5.io.Out(0)

  icmp_8.io.LeftIO <> binaryOp_7.io.Out(1)

  br_9.io.CmpIO <> icmp_8.io.Out(0)

  ret_12.io.In.data("field0") <> phi11.io.Out(0)

  phi11.io.InData(0) <> InputSplitter.io.Out.data.elements("field0")(1)

  icmp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(0)



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_12.io.Out

}

import java.io.{File, FileWriter}

object test04Top extends App {
  val dir = new File("RTL/test04Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test04DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
