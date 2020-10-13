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

abstract class test07DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test07DF(implicit p: Parameters) extends test07DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 1, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(3, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_for_body4_lr_ph_preheader1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_for_cond_cleanup_loopexit2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_for_cond_cleanup3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 2, BID = 3))

  val bb_for_body4_lr_ph4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 4, NumPhi = 1, BID = 4))

  val bb_for_cond_cleanup35 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 5))

  val bb_for_body46 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 12, NumPhi = 1, BID = 6))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %cmp24 = icmp eq i32 %n, 0, !UID !10
  val icmp_cmp240 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "eq")(sign = false))

  //  br i1 %cmp24, label %for.cond.cleanup, label %for.body4.lr.ph.preheader, !UID !11, !BB_UID !12
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  br label %for.body4.lr.ph, !UID !13, !BB_UID !14
  val br_2 = Module(new UBranchNode(ID = 2))

  //  br label %for.cond.cleanup
  val br_3 = Module(new UBranchNode(ID = 3))

  //  ret i32 0, !UID !15, !BB_UID !16
  val ret_4 = Module(new RetNode2(retTypes = List(32), ID = 4))

  //  %j.025 = phi i32 [ %inc8, %for.cond.cleanup3 ], [ 0, %for.body4.lr.ph.preheader ], !UID !17
  val phij_0255 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 5, Res = false))

  //  %mul = mul i32 %j.025, %n, !UID !18
  val binaryOp_mul6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "mul")(sign = false))

  //  br label %for.body4, !UID !19, !BB_UID !20
  val br_7 = Module(new UBranchNode(ID = 7))

  //  %inc8 = add nuw i32 %j.025, 1, !UID !21
  val binaryOp_inc88 = Module(new ComputeNode(NumOuts = 2, ID = 8, opCode = "add")(sign = false))

  //  %exitcond26 = icmp eq i32 %inc8, %n, !UID !22
  val icmp_exitcond269 = Module(new IcmpNode(NumOuts = 1, ID = 9, opCode = "eq")(sign = false))

  //  br i1 %exitcond26, label %for.cond.cleanup.loopexit, label %for.body4.lr.ph, !UID !23, !BB_UID !24
  val br_10 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 10))

  //  %k.023 = phi i32 [ 0, %for.body4.lr.ph ], [ %inc, %for.body4 ], !UID !25
  val phik_02311 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 11, Res = true))

  //  %add = add i32 %k.023, %mul, !UID !26
  val binaryOp_add12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign = false))

  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %add, !UID !27
  val Gep_arrayidx13 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 13)(ElementSize = 4, ArraySize = List()))

  //  %0 = load i32, i32* %arrayidx, align 4, !tbaa !28, !UID !32
  val ld_14 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 14, RouteID = 0))

  //  %mul5 = shl i32 %0, 1, !UID !33
  val binaryOp_mul515 = Module(new ComputeNode(NumOuts = 1, ID = 15, opCode = "shl")(sign = false))

  //  store i32 %mul5, i32* %arrayidx, align 4, !tbaa !28, !UID !34
  val st_16 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 16, RouteID = 0))

  //  %inc = add nuw i32 %k.023, 1, !UID !35
  val binaryOp_inc17 = Module(new ComputeNode(NumOuts = 2, ID = 17, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, %n, !UID !36
  val icmp_exitcond18 = Module(new IcmpNode(NumOuts = 1, ID = 18, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup3, label %for.body4, !UID !37, !BB_UID !38
  val br_19 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 19))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))

  //i32 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i32 1
  val const5 = Module(new ConstFastNode(value = 1, ID = 5))

  //i32 1
  val const6 = Module(new ConstFastNode(value = 1, ID = 6))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_for_body4_lr_ph_preheader1.io.predicateIn(0) <> br_1.io.FalseOutput(0)

  bb_for_cond_cleanup3.io.predicateIn(1) <> br_1.io.TrueOutput(0)

  bb_for_cond_cleanup3.io.predicateIn(0) <> br_3.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_for_cond_cleanup_loopexit2.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_for_body4_lr_ph4.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_for_body4_lr_ph4.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_for_cond_cleanup35.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_for_body46.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_for_body46.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_7.io.Out(0)

  Loop_0.io.loopBack(0) <> br_19.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_19.io.TrueOutput(0)

  Loop_1.io.enable <> br_2.io.Out(0)

  Loop_1.io.loopBack(0) <> br_10.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_10.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> binaryOp_mul6.io.Out(0)

  Loop_0.io.InLiveIn(1) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_1.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_add12.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  icmp_exitcond18.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  Gep_arrayidx13.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  binaryOp_mul6.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(1)

  icmp_exitcond269.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(2)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc17.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_inc88.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phik_02311.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phij_0255.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  icmp_cmp240.io.enable <> bb_entry0.io.Out(1)

  br_1.io.enable <> bb_entry0.io.Out(2)


  br_2.io.enable <> bb_for_body4_lr_ph_preheader1.io.Out(0)


  br_3.io.enable <> bb_for_cond_cleanup_loopexit2.io.Out(0)


  const1.io.enable <> bb_for_cond_cleanup3.io.Out(0)

  ret_4.io.In.enable <> bb_for_cond_cleanup3.io.Out(1)


  const2.io.enable <> bb_for_body4_lr_ph4.io.Out(0)

  phij_0255.io.enable <> bb_for_body4_lr_ph4.io.Out(1)

  binaryOp_mul6.io.enable <> bb_for_body4_lr_ph4.io.Out(2)

  br_7.io.enable <> bb_for_body4_lr_ph4.io.Out(3)


  const3.io.enable <> bb_for_cond_cleanup35.io.Out(0)

  binaryOp_inc88.io.enable <> bb_for_cond_cleanup35.io.Out(1)

  icmp_exitcond269.io.enable <> bb_for_cond_cleanup35.io.Out(2)

  br_10.io.enable <> bb_for_cond_cleanup35.io.Out(3)


  const4.io.enable <> bb_for_body46.io.Out(0)

  const5.io.enable <> bb_for_body46.io.Out(1)

  const6.io.enable <> bb_for_body46.io.Out(2)

  phik_02311.io.enable <> bb_for_body46.io.Out(3)

  binaryOp_add12.io.enable <> bb_for_body46.io.Out(4)

  Gep_arrayidx13.io.enable <> bb_for_body46.io.Out(5)

  ld_14.io.enable <> bb_for_body46.io.Out(6)

  binaryOp_mul515.io.enable <> bb_for_body46.io.Out(7)

  st_16.io.enable <> bb_for_body46.io.Out(8)

  binaryOp_inc17.io.enable <> bb_for_body46.io.Out(9)

  icmp_exitcond18.io.enable <> bb_for_body46.io.Out(10)

  br_19.io.enable <> bb_for_body46.io.Out(11)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phij_0255.io.Mask <> bb_for_body4_lr_ph4.io.MaskBB(0)

  phik_02311.io.Mask <> bb_for_body46.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> st_16.io.memReq

  st_16.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  icmp_cmp240.io.RightIO <> const0.io.Out

  ret_4.io.In.data("field0") <> const1.io.Out

  phij_0255.io.InData(1) <> const2.io.Out

  binaryOp_inc88.io.RightIO <> const3.io.Out

  phik_02311.io.InData(0) <> const4.io.Out

  binaryOp_mul515.io.RightIO <> const5.io.Out

  binaryOp_inc17.io.RightIO <> const6.io.Out

  br_1.io.CmpIO <> icmp_cmp240.io.Out(0)

  binaryOp_mul6.io.LeftIO <> phij_0255.io.Out(0)

  binaryOp_inc88.io.LeftIO <> phij_0255.io.Out(1)

  icmp_exitcond269.io.LeftIO <> binaryOp_inc88.io.Out(1)

  br_10.io.CmpIO <> icmp_exitcond269.io.Out(0)

  binaryOp_add12.io.LeftIO <> phik_02311.io.Out(0)

  binaryOp_inc17.io.LeftIO <> phik_02311.io.Out(1)

  Gep_arrayidx13.io.idx(0) <> binaryOp_add12.io.Out(0)

  ld_14.io.GepAddr <> Gep_arrayidx13.io.Out(0)

  st_16.io.GepAddr <> Gep_arrayidx13.io.Out(1)

  binaryOp_mul515.io.LeftIO <> ld_14.io.Out(0)

  st_16.io.inData <> binaryOp_mul515.io.Out(0)

  icmp_exitcond18.io.LeftIO <> binaryOp_inc17.io.Out(1)

  br_19.io.CmpIO <> icmp_exitcond18.io.Out(0)

  icmp_cmp240.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(1)

  st_16.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_4.io.Out

}

import java.io.{File, FileWriter}

object test07Top extends App {
  val dir = new File("RTL/test07Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test07DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
