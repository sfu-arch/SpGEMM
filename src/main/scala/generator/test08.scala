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

abstract class test08DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test08DF(implicit p: Parameters) extends test08DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 3)
  (WControl = new WriteMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(3, 5)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 2), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1, 2), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 11, BID = 0))

  val bb_for_cond_cleanup1 = Module(new BasicBlockNode(NumInputs = 1, NumOuts = 4, NumPhi = 1, BID = 1))

  val bb_for_body2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 5, NumPhi = 2, BID = 2))

  val bb_for_body4_preheader3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_for_cond_cleanup3_loopexit4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))

  val bb_for_cond_cleanup35 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 10, BID = 5))

  val bb_for_body46 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 6))

  val bb_for_body8_preheader7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 7))

  val bb_for_cond_cleanup7_loopexit8 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 8))

  val bb_for_cond_cleanup79 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 8, BID = 9))

  val bb_for_body810 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 11, NumPhi = 1, BID = 10))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %cmp240 = icmp eq i32 %n, 0, !UID !34
  val icmp_cmp2400 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "eq")(sign = false))

  //  %cmp638 = icmp eq i32 %n, 0, !UID !35
  val icmp_cmp6381 = Module(new IcmpNode(NumOuts = 1, ID = 1, opCode = "eq")(sign = false))

  //  %sub = add i32 %n, -1, !UID !36
  val binaryOp_sub2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "add")(sign = false))

  //  %arrayidx10 = getelementptr inbounds i32, i32* %a, i32 %sub, !UID !37
  val Gep_arrayidx103 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 3)(ElementSize = 4, ArraySize = List()))

  //  %sub15 = add i32 %n, -1, !UID !38
  val binaryOp_sub154 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "add")(sign = false))

  //  %arrayidx16 = getelementptr inbounds i32, i32* %a, i32 %sub15, !UID !39
  val Gep_arrayidx165 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 5)(ElementSize = 4, ArraySize = List()))

  //  br label %for.body, !dbg !40, !UID !41, !BB_UID !42
  val br_6 = Module(new UBranchNode(ID = 6))

  //  %add.lcssa = phi i32 [ %add, %for.cond.cleanup3 ], !UID !43
  val phiadd_lcssa7 = Module(new PhiFastNode(NumInputs = 1, NumOutputs = 1, ID = 7, Res = false))

  //  %div = sdiv i32 %add.lcssa, 2, !dbg !44, !UID !45
  val binaryOp_div8 = Module(new ComputeNode(NumOuts = 1, ID = 8, opCode = "sdiv")(sign = false))

  //  ret i32 %div, !dbg !46, !UID !47, !BB_UID !48
  val ret_9 = Module(new RetNode2(retTypes = List(32), ID = 9))

  //  %i.043 = phi i32 [ 0, %entry ], [ %inc19, %for.cond.cleanup3 ], !UID !49
  val phii_04310 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 10, Res = true))

  //  %result.042 = phi i32 [ 0, %entry ], [ %add, %for.cond.cleanup3 ], !UID !50
  val phiresult_04211 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 11, Res = true))

  //  br i1 %cmp240, label %for.cond.cleanup3, label %for.body4.preheader, !dbg !52, !UID !53, !BB_UID !54
  val br_12 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 12))

  //  br label %for.body4, !dbg !55, !UID !56, !BB_UID !57
  val br_13 = Module(new UBranchNode(ID = 13))

  //  br label %for.cond.cleanup3, !dbg !58
  val br_14 = Module(new UBranchNode(ID = 14))

  //  %0 = load i32, i32* %arrayidx16, align 4, !dbg !58, !tbaa !59, !UID !63
  val ld_15 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 15, RouteID = 0))

  //  %inc17 = add i32 %0, 1, !dbg !58, !UID !64
  val binaryOp_inc1716 = Module(new ComputeNode(NumOuts = 1, ID = 16, opCode = "add")(sign = false))

  //  store i32 %inc17, i32* %arrayidx16, align 4, !dbg !58, !tbaa !59, !UID !65
  val st_17 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 17, RouteID = 0))

  //  %add = add i32 %0, %result.042, !dbg !66, !UID !67
  val binaryOp_add18 = Module(new ComputeNode(NumOuts = 2, ID = 18, opCode = "add")(sign = false))

  //  %inc19 = add nuw nsw i32 %i.043, 1, !dbg !68, !UID !69
  val binaryOp_inc1919 = Module(new ComputeNode(NumOuts = 2, ID = 19, opCode = "add")(sign = false))

  //  %exitcond45 = icmp eq i32 %inc19, 3, !dbg !70, !UID !71
  val icmp_exitcond4520 = Module(new IcmpNode(NumOuts = 1, ID = 20, opCode = "eq")(sign = false))

  //  br i1 %exitcond45, label %for.cond.cleanup, label %for.body, !dbg !40, !llvm.loop !72, !UID !74, !BB_UID !75
  val br_21 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 21))

  //  %j.041 = phi i32 [ %inc13, %for.cond.cleanup7 ], [ 0, %for.body4.preheader ], !UID !76
  val phij_04122 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 22, Res = false))

  //  br i1 %cmp638, label %for.cond.cleanup7, label %for.body8.preheader, !dbg !55, !UID !78, !BB_UID !79
  val br_23 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 23))

  //  br label %for.body8, !dbg !80, !UID !83, !BB_UID !84
  val br_24 = Module(new UBranchNode(ID = 24))

  //  br label %for.cond.cleanup7, !dbg !85
  val br_25 = Module(new UBranchNode(ID = 25))

  //  %1 = load i32, i32* %arrayidx10, align 4, !dbg !85, !tbaa !59, !UID !86
  val ld_26 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 26, RouteID = 1))

  //  %inc11 = add i32 %1, 1, !dbg !85, !UID !87
  val binaryOp_inc1127 = Module(new ComputeNode(NumOuts = 1, ID = 27, opCode = "add")(sign = false))

  //  store i32 %inc11, i32* %arrayidx10, align 4, !dbg !85, !tbaa !59, !UID !88
  val st_28 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 28, RouteID = 1))

  //  %inc13 = add nuw i32 %j.041, 1, !dbg !89, !UID !90
  val binaryOp_inc1329 = Module(new ComputeNode(NumOuts = 2, ID = 29, opCode = "add")(sign = false))

  //  %exitcond44 = icmp eq i32 %inc13, %n, !dbg !91, !UID !92
  val icmp_exitcond4430 = Module(new IcmpNode(NumOuts = 1, ID = 30, opCode = "eq")(sign = false))

  //  br i1 %exitcond44, label %for.cond.cleanup3.loopexit, label %for.body4, !dbg !52, !llvm.loop !93, !UID !95, !BB_UID !96
  val br_31 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 31))

  //  %k.039 = phi i32 [ %inc, %for.body8 ], [ 0, %for.body8.preheader ], !UID !97
  val phik_03932 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 32, Res = false))

  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %k.039, !dbg !80, !UID !98
  val Gep_arrayidx33 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 33)(ElementSize = 4, ArraySize = List()))

  //  %2 = load i32, i32* %arrayidx, align 4, !dbg !80, !tbaa !59, !UID !99
  val ld_34 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 34, RouteID = 2))

  //  %mul = shl i32 %2, 1, !dbg !100, !UID !101
  val binaryOp_mul35 = Module(new ComputeNode(NumOuts = 1, ID = 35, opCode = "shl")(sign = false))

  //  store i32 %mul, i32* %arrayidx, align 4, !dbg !102, !tbaa !59, !UID !103
  val st_36 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 36, RouteID = 2))

  //  %inc = add nuw i32 %k.039, 1, !dbg !104, !UID !105
  val binaryOp_inc37 = Module(new ComputeNode(NumOuts = 2, ID = 37, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, %n, !dbg !106, !UID !107
  val icmp_exitcond38 = Module(new IcmpNode(NumOuts = 1, ID = 38, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup7.loopexit, label %for.body8, !dbg !55, !llvm.loop !108, !UID !110, !BB_UID !111
  val br_39 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 39))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 -1
  val const2 = Module(new ConstFastNode(value = -1, ID = 2))

  //i32 -1
  val const3 = Module(new ConstFastNode(value = -1, ID = 3))

  //i32 2
  val const4 = Module(new ConstFastNode(value = 2, ID = 4))

  //i32 0
  val const5 = Module(new ConstFastNode(value = 0, ID = 5))

  //i32 0
  val const6 = Module(new ConstFastNode(value = 0, ID = 6))

  //i32 1
  val const7 = Module(new ConstFastNode(value = 1, ID = 7))

  //i32 1
  val const8 = Module(new ConstFastNode(value = 1, ID = 8))

  //i32 3
  val const9 = Module(new ConstFastNode(value = 3, ID = 9))

  //i32 0
  val const10 = Module(new ConstFastNode(value = 0, ID = 10))

  //i32 1
  val const11 = Module(new ConstFastNode(value = 1, ID = 11))

  //i32 1
  val const12 = Module(new ConstFastNode(value = 1, ID = 12))

  //i32 0
  val const13 = Module(new ConstFastNode(value = 0, ID = 13))

  //i32 1
  val const14 = Module(new ConstFastNode(value = 1, ID = 14))

  //i32 1
  val const15 = Module(new ConstFastNode(value = 1, ID = 15))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_for_body4_preheader3.io.predicateIn(0) <> br_12.io.FalseOutput(0)

  bb_for_cond_cleanup35.io.predicateIn(1) <> br_12.io.TrueOutput(0)

  bb_for_cond_cleanup35.io.predicateIn(0) <> br_14.io.Out(0)

  bb_for_body8_preheader7.io.predicateIn(0) <> br_23.io.FalseOutput(0)

  bb_for_cond_cleanup79.io.predicateIn(1) <> br_23.io.TrueOutput(0)

  bb_for_cond_cleanup79.io.predicateIn(0) <> br_25.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_for_cond_cleanup1.io.predicateIn(0) <> Loop_2.io.loopExit(0)

  bb_for_body2.io.predicateIn(1) <> Loop_2.io.activate_loop_start

  bb_for_body2.io.predicateIn(0) <> Loop_2.io.activate_loop_back

  bb_for_cond_cleanup3_loopexit4.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_for_body46.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_for_body46.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_for_cond_cleanup7_loopexit8.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_for_body810.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_for_body810.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_24.io.Out(0)

  Loop_0.io.loopBack(0) <> br_39.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_39.io.TrueOutput(0)

  Loop_1.io.enable <> br_13.io.Out(0)

  Loop_1.io.loopBack(0) <> br_31.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_31.io.TrueOutput(0)

  Loop_2.io.enable <> br_6.io.Out(0)

  Loop_2.io.loopBack(0) <> br_21.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_21.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_0.io.InLiveIn(1) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(0) <> Loop_2.io.OutLiveIn.elements("field3")(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field4")(0)

  Loop_2.io.InLiveIn(0) <> icmp_cmp2400.io.Out(0)

  Loop_2.io.InLiveIn(1) <> icmp_cmp6381.io.Out(0)

  Loop_2.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_2.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_2.io.InLiveIn(4) <> Gep_arrayidx103.io.Out(0)

  Loop_2.io.InLiveIn(5) <> Gep_arrayidx165.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  icmp_exitcond38.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  Gep_arrayidx33.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field1")(0)

  icmp_exitcond4430.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(1)

  br_23.io.CmpIO <> Loop_1.io.OutLiveIn.elements("field2")(0)

  ld_26.io.GepAddr <> Loop_1.io.OutLiveIn.elements("field3")(0)

  st_28.io.GepAddr <> Loop_1.io.OutLiveIn.elements("field3")(1)

  br_12.io.CmpIO <> Loop_2.io.OutLiveIn.elements("field0")(0)

  ld_15.io.GepAddr <> Loop_2.io.OutLiveIn.elements("field5")(0)

  st_17.io.GepAddr <> Loop_2.io.OutLiveIn.elements("field5")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_2.io.InLiveOut(0) <> binaryOp_add18.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phiadd_lcssa7.io.InData(0) <> Loop_2.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc37.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_inc1329.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_inc1919.io.Out(0)

  Loop_2.io.CarryDepenIn(1) <> binaryOp_add18.io.Out(1)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phik_03932.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phij_04122.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phii_04310.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phiresult_04211.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field1")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  const1.io.enable <> bb_entry0.io.Out(1)

  const2.io.enable <> bb_entry0.io.Out(2)

  const3.io.enable <> bb_entry0.io.Out(3)

  icmp_cmp2400.io.enable <> bb_entry0.io.Out(4)


  icmp_cmp6381.io.enable <> bb_entry0.io.Out(5)


  binaryOp_sub2.io.enable <> bb_entry0.io.Out(6)


  Gep_arrayidx103.io.enable <> bb_entry0.io.Out(7)


  binaryOp_sub154.io.enable <> bb_entry0.io.Out(8)


  Gep_arrayidx165.io.enable <> bb_entry0.io.Out(9)


  br_6.io.enable <> bb_entry0.io.Out(10)


  const4.io.enable <> bb_for_cond_cleanup1.io.Out(0)

  phiadd_lcssa7.io.enable <> bb_for_cond_cleanup1.io.Out(1)


  binaryOp_div8.io.enable <> bb_for_cond_cleanup1.io.Out(2)


  ret_9.io.In.enable <> bb_for_cond_cleanup1.io.Out(3)


  const5.io.enable <> bb_for_body2.io.Out(0)

  const6.io.enable <> bb_for_body2.io.Out(1)

  phii_04310.io.enable <> bb_for_body2.io.Out(2)


  phiresult_04211.io.enable <> bb_for_body2.io.Out(3)


  br_12.io.enable <> bb_for_body2.io.Out(4)


  br_13.io.enable <> bb_for_body4_preheader3.io.Out(0)


  br_14.io.enable <> bb_for_cond_cleanup3_loopexit4.io.Out(0)


  const7.io.enable <> bb_for_cond_cleanup35.io.Out(0)

  const8.io.enable <> bb_for_cond_cleanup35.io.Out(1)

  const9.io.enable <> bb_for_cond_cleanup35.io.Out(2)

  ld_15.io.enable <> bb_for_cond_cleanup35.io.Out(3)


  binaryOp_inc1716.io.enable <> bb_for_cond_cleanup35.io.Out(4)


  st_17.io.enable <> bb_for_cond_cleanup35.io.Out(5)


  binaryOp_add18.io.enable <> bb_for_cond_cleanup35.io.Out(6)


  binaryOp_inc1919.io.enable <> bb_for_cond_cleanup35.io.Out(7)


  icmp_exitcond4520.io.enable <> bb_for_cond_cleanup35.io.Out(8)


  br_21.io.enable <> bb_for_cond_cleanup35.io.Out(9)


  const10.io.enable <> bb_for_body46.io.Out(0)

  phij_04122.io.enable <> bb_for_body46.io.Out(1)


  br_23.io.enable <> bb_for_body46.io.Out(2)


  br_24.io.enable <> bb_for_body8_preheader7.io.Out(0)


  br_25.io.enable <> bb_for_cond_cleanup7_loopexit8.io.Out(0)


  const11.io.enable <> bb_for_cond_cleanup79.io.Out(0)

  const12.io.enable <> bb_for_cond_cleanup79.io.Out(1)

  ld_26.io.enable <> bb_for_cond_cleanup79.io.Out(2)


  binaryOp_inc1127.io.enable <> bb_for_cond_cleanup79.io.Out(3)


  st_28.io.enable <> bb_for_cond_cleanup79.io.Out(4)


  binaryOp_inc1329.io.enable <> bb_for_cond_cleanup79.io.Out(5)


  icmp_exitcond4430.io.enable <> bb_for_cond_cleanup79.io.Out(6)


  br_31.io.enable <> bb_for_cond_cleanup79.io.Out(7)


  const13.io.enable <> bb_for_body810.io.Out(0)

  const14.io.enable <> bb_for_body810.io.Out(1)

  const15.io.enable <> bb_for_body810.io.Out(2)

  phik_03932.io.enable <> bb_for_body810.io.Out(3)


  Gep_arrayidx33.io.enable <> bb_for_body810.io.Out(4)


  ld_34.io.enable <> bb_for_body810.io.Out(5)


  binaryOp_mul35.io.enable <> bb_for_body810.io.Out(6)


  st_36.io.enable <> bb_for_body810.io.Out(7)


  binaryOp_inc37.io.enable <> bb_for_body810.io.Out(8)


  icmp_exitcond38.io.enable <> bb_for_body810.io.Out(9)


  br_39.io.enable <> bb_for_body810.io.Out(10)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phiadd_lcssa7.io.Mask <> bb_for_cond_cleanup1.io.MaskBB(0)

  phii_04310.io.Mask <> bb_for_body2.io.MaskBB(0)

  phiresult_04211.io.Mask <> bb_for_body2.io.MaskBB(1)

  phij_04122.io.Mask <> bb_for_body46.io.MaskBB(0)

  phik_03932.io.Mask <> bb_for_body810.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_15.io.memReq

  ld_15.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> st_17.io.memReq

  st_17.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(1) <> ld_26.io.memReq

  ld_26.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(1) <> st_28.io.memReq

  st_28.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.ReadIn(2) <> ld_34.io.memReq

  ld_34.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(2) <> st_36.io.memReq

  st_36.io.memResp <> MemCtrl.io.WriteOut(2)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  icmp_cmp2400.io.RightIO <> const0.io.Out

  icmp_cmp6381.io.RightIO <> const1.io.Out

  binaryOp_sub2.io.RightIO <> const2.io.Out

  binaryOp_sub154.io.RightIO <> const3.io.Out

  binaryOp_div8.io.RightIO <> const4.io.Out

  phii_04310.io.InData(0) <> const5.io.Out

  phiresult_04211.io.InData(0) <> const6.io.Out

  binaryOp_inc1716.io.RightIO <> const7.io.Out

  binaryOp_inc1919.io.RightIO <> const8.io.Out

  icmp_exitcond4520.io.RightIO <> const9.io.Out

  phij_04122.io.InData(1) <> const10.io.Out

  binaryOp_inc1127.io.RightIO <> const11.io.Out

  binaryOp_inc1329.io.RightIO <> const12.io.Out

  phik_03932.io.InData(1) <> const13.io.Out

  binaryOp_mul35.io.RightIO <> const14.io.Out

  binaryOp_inc37.io.RightIO <> const15.io.Out

  Gep_arrayidx103.io.idx(0) <> binaryOp_sub2.io.Out(0)

  Gep_arrayidx165.io.idx(0) <> binaryOp_sub154.io.Out(0)

  binaryOp_div8.io.LeftIO <> phiadd_lcssa7.io.Out(0)

  ret_9.io.In.data("field0") <> binaryOp_div8.io.Out(0)

  binaryOp_inc1919.io.LeftIO <> phii_04310.io.Out(0)

  binaryOp_add18.io.RightIO <> phiresult_04211.io.Out(0)

  binaryOp_inc1716.io.LeftIO <> ld_15.io.Out(0)

  binaryOp_add18.io.LeftIO <> ld_15.io.Out(1)

  st_17.io.inData <> binaryOp_inc1716.io.Out(0)

  icmp_exitcond4520.io.LeftIO <> binaryOp_inc1919.io.Out(1)

  br_21.io.CmpIO <> icmp_exitcond4520.io.Out(0)

  binaryOp_inc1329.io.LeftIO <> phij_04122.io.Out(0)

  binaryOp_inc1127.io.LeftIO <> ld_26.io.Out(0)

  st_28.io.inData <> binaryOp_inc1127.io.Out(0)

  icmp_exitcond4430.io.LeftIO <> binaryOp_inc1329.io.Out(1)

  br_31.io.CmpIO <> icmp_exitcond4430.io.Out(0)

  Gep_arrayidx33.io.idx(0) <> phik_03932.io.Out(0)

  binaryOp_inc37.io.LeftIO <> phik_03932.io.Out(1)

  ld_34.io.GepAddr <> Gep_arrayidx33.io.Out(0)

  st_36.io.GepAddr <> Gep_arrayidx33.io.Out(1)

  binaryOp_mul35.io.LeftIO <> ld_34.io.Out(0)

  st_36.io.inData <> binaryOp_mul35.io.Out(0)

  icmp_exitcond38.io.LeftIO <> binaryOp_inc37.io.Out(1)

  br_39.io.CmpIO <> icmp_exitcond38.io.Out(0)

  Gep_arrayidx103.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_arrayidx165.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  icmp_cmp2400.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(1)

  icmp_cmp6381.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(2)

  binaryOp_sub2.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(3)

  binaryOp_sub154.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(4)

  st_17.io.Out(0).ready := true.B

  st_28.io.Out(0).ready := true.B

  st_36.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_9.io.Out

}

import java.io.{File, FileWriter}

object test08Top extends App {
  val dir = new File("RTL/test08Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test08DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
