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

abstract class test_05_dense_b_ir_4DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class test_05_dense_b_ir_4DF(implicit p: Parameters) extends test_05_dense_b_ir_4DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 9, NWrites = 3)
  (WControl = new WriteMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 9, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 3, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_dot_loop_body_rhs_1_lr_ph0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 14, BID = 0))

  val bb_dot_loop_exit_reduction1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 24, NumPhi = 1, BID = 1))

  val bb_fusion_loop_body_dim_0_lr_ph2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 2))

  val bb_fusion_loop_body_dim_03 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 20, NumPhi = 1, BID = 3))

  val bb_fusion_loop_exit_dim_04 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds i8*, i8** %params, i64 1, !UID !1
  val Gep_0 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 0)(ElementSize = 8, ArraySize = List()))

  //  %arg1.untyped = load i8*, i8** %0, align 8, !dereferenceable !2, !align !3, !UID !4
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 1, RouteID = 0))

  //  %1 = bitcast i8* %arg1.untyped to [1 x [64 x float]]*, !UID !5
  val bitcast_2 = Module(new BitCastNode(NumOuts = 1, ID = 2))

  //  %arg0.untyped = load i8*, i8** %params, align 8, !dereferenceable !6, !align !3, !UID !7
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 1))

  //  %2 = getelementptr inbounds i8*, i8** %params, i64 2, !UID !8
  val Gep_4 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 4)(ElementSize = 8, ArraySize = List()))

  //  %arg2.untyped = load i8*, i8** %2, align 8, !dereferenceable !2, !align !3, !UID !9
  val ld_5 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 5, RouteID = 2))

  //  %3 = bitcast i8* %arg2.untyped to [64 x float]*, !UID !10
  val bitcast_6 = Module(new BitCastNode(NumOuts = 1, ID = 6))

  //  %4 = getelementptr inbounds i8*, i8** %temps, i64 5, !UID !11
  val Gep_7 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 7)(ElementSize = 8, ArraySize = List()))

  //  %5 = load i8*, i8** %4, align 8, !dereferenceable !2, !align !3, !UID !12
  val ld_8 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 8, RouteID = 3))

  //  %dot = bitcast i8* %5 to [1 x [64 x float]]*, !UID !13
  val bitcast_dot9 = Module(new BitCastNode(NumOuts = 2, ID = 9))

  //  br label %dot.loop_exit.reduction, !UID !14, !BB_UID !15
  val br_10 = Module(new UBranchNode(ID = 10))

  //  %dot.indvar.rhs.16 = phi i64 [ 0, %dot.loop_body.rhs.1.lr.ph ], [ %invar.inc1, %dot.loop_exit.reduction ], !UID !16
  val phidot_indvar_rhs_1611 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 11, Res = true))

  //  %tmp = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %1, i64 0, i64 0, !UID !17
  val Gep_tmp12 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 12)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp1 = getelementptr [64 x float], [64 x float]* %tmp, i64 0, i64 %dot.indvar.rhs.16, !UID !18
  val Gep_tmp113 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 13)(ElementSize = 4, ArraySize = List(256)))

  //  %.pre = load float, float* %tmp1, align 4, !UID !19
  val ld_14 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 14, RouteID = 4))

  //  %.phi.trans.insert9 = bitcast i8* %arg0.untyped to float*, !UID !20
  val bitcast__phi_trans_insert915 = Module(new BitCastNode(NumOuts = 1, ID = 15))

  //  %.pre10 = load float, float* %.phi.trans.insert9, align 4, !UID !21
  val ld_16 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 16, RouteID = 5))

  //  %6 = fmul float %.pre10, %.pre, !UID !22
  //val FP_17 = Module(new FPComputeNode(NumOuts = 1, ID = 17, opCode = "fmul")(t = p(FTYP)))
  val FP_17 = Module(new FPCustomMultiplierNode(NumOuts = 1, ID = 17, opCode = "fmul")(t = p(FTYP)))

  //  %7 = fadd float %6, 0.000000e+00, !UID !23
  //val FP_18 = Module(new FPComputeNode(NumOuts = 1, ID = 18, opCode = "fadd")(t = p(FTYP)))
  val FP_18 = Module(new FPCustomAdderNode(NumOuts = 1, ID = 18, opCode = "fadd")(t = p(FTYP)))

  //  %tmp2 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %dot, i64 0, i64 0, !UID !24
  val Gep_tmp219 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 19)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp3 = getelementptr [64 x float], [64 x float]* %tmp2, i64 0, i64 %dot.indvar.rhs.16, !UID !25
  val Gep_tmp320 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 20)(ElementSize = 4, ArraySize = List(256)))

  //  store float %7, float* %tmp3, align 4, !UID !26
  val st_21 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 21, RouteID = 0))

  //  %invar.inc1 = add nuw nsw i64 %dot.indvar.rhs.16, 1, !UID !27
  val binaryOp_invar_inc122 = Module(new ComputeNode(NumOuts = 2, ID = 22, opCode = "add")(sign = false))

  //  %8 = icmp ugt i64 %invar.inc1, 63, !UID !28
  val icmp_23 = Module(new IcmpNode(NumOuts = 1, ID = 23, opCode = "ugt")(sign = false))

  //  br i1 %8, label %fusion.loop_body.dim.0.lr.ph, label %dot.loop_exit.reduction, !UID !29, !BB_UID !30
  val br_24 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 24))

  //  %9 = load i8*, i8** %temps, align 8, !dereferenceable !2, !align !3, !UID !31
  val ld_25 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 25, RouteID = 6))

  //  %fusion = bitcast i8* %9 to [64 x float]*, !UID !32
  val bitcast_fusion26 = Module(new BitCastNode(NumOuts = 1, ID = 26))

  //  br label %fusion.loop_body.dim.0, !UID !33, !BB_UID !34
  val br_27 = Module(new UBranchNode(ID = 27))

  //  %fusion.indvar.dim.02 = phi i64 [ 0, %fusion.loop_body.dim.0.lr.ph ], [ %invar.inc3, %fusion.loop_body.dim.0 ], !UID !35
  val phifusion_indvar_dim_0228 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 28, Res = true))

  //  %10 = getelementptr inbounds [64 x float], [64 x float]* %3, i64 0, i64 %fusion.indvar.dim.02, !UID !36
  val Gep_29 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 29)(ElementSize = 4, ArraySize = List(256)))

  //  %11 = load float, float* %10, align 4, !UID !37
  val ld_30 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 30, RouteID = 7))

  //  %tmp4 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %dot, i64 0, i64 0, !UID !38
  val Gep_tmp431 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 31)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp5 = getelementptr [64 x float], [64 x float]* %tmp4, i64 0, i64 %fusion.indvar.dim.02, !UID !39
  val Gep_tmp532 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 32)(ElementSize = 4, ArraySize = List(256)))

  //  %12 = load float, float* %tmp5, align 4, !UID !40
  val ld_33 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 33, RouteID = 8))

  //  %13 = fadd float %11, %12, !UID !41
  //val FP_34 = Module(new FPComputeNode(NumOuts = 1, ID = 34, opCode = "fadd")(t = p(FTYP)))
  val FP_34 = Module(new FPCustomAdderNode(NumOuts = 1, ID = 34, opCode = "fadd")(t = p(FTYP)))

  //  %14 = getelementptr inbounds [64 x float], [64 x float]* %fusion, i64 0, i64 %fusion.indvar.dim.02, !UID !42
  val Gep_35 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 35)(ElementSize = 4, ArraySize = List(256)))

  //  store float %13, float* %14, align 4, !UID !43
  val st_36 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 36, RouteID = 1))

  //  %invar.inc3 = add nuw nsw i64 %fusion.indvar.dim.02, 1, !UID !44
  val binaryOp_invar_inc337 = Module(new ComputeNode(NumOuts = 2, ID = 37, opCode = "add")(sign = false))

  //  %15 = icmp ugt i64 %invar.inc3, 63, !UID !45
  val icmp_38 = Module(new IcmpNode(NumOuts = 1, ID = 38, opCode = "ugt")(sign = false))

  //  br i1 %15, label %fusion.loop_exit.dim.0, label %fusion.loop_body.dim.0, !UID !46, !BB_UID !47
  val br_39 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 39))

  //  %16 = bitcast i8* %retval to i8**, !UID !48
  val bitcast_40 = Module(new BitCastNode(NumOuts = 1, ID = 40))

  //  store i8* %9, i8** %16, align 8, !UID !49
  val st_41 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 41, RouteID = 2))

  //  ret void, !UID !50, !BB_UID !51
  val ret_42 = Module(new RetNode2(retTypes = List(), ID = 42))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 1
  val const0 = Module(new ConstFastNode(value = 1, ID = 0))

  //i64 2
  val const1 = Module(new ConstFastNode(value = 2, ID = 1))

  //i64 5
  val const2 = Module(new ConstFastNode(value = 5, ID = 2))

  //i64 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i64 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i64 0
  val const5 = Module(new ConstFastNode(value = 0, ID = 5))

  //i64 0
  val const6 = Module(new ConstFastNode(value = 0, ID = 6))

  //i64 0
  val const7 = Module(new ConstFastNode(value = 0, ID = 7))

  //i64 0
  val const8 = Module(new ConstFastNode(value = 0, ID = 8))

  //i64 0
  val const9 = Module(new ConstFastNode(value = 0, ID = 9))

  //i64 1
  val const10 = Module(new ConstFastNode(value = 1, ID = 10))

  //i64 63
  val const11 = Module(new ConstFastNode(value = 63, ID = 11))

  //i64 0
  val const12 = Module(new ConstFastNode(value = 0, ID = 12))

  //i64 0
  val const13 = Module(new ConstFastNode(value = 0, ID = 13))

  //i64 0
  val const14 = Module(new ConstFastNode(value = 0, ID = 14))

  //i64 0
  val const15 = Module(new ConstFastNode(value = 0, ID = 15))

  //i64 0
  val const16 = Module(new ConstFastNode(value = 0, ID = 16))

  //i64 0
  val const17 = Module(new ConstFastNode(value = 0, ID = 17))

  //i64 1
  val const18 = Module(new ConstFastNode(value = 1, ID = 18))

  //i64 63
  val const19 = Module(new ConstFastNode(value = 63, ID = 19))

  //float 0.000000e+00
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_dot_loop_body_rhs_1_lr_ph0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_dot_loop_exit_reduction1.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_dot_loop_exit_reduction1.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_fusion_loop_body_dim_0_lr_ph2.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_fusion_loop_body_dim_03.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_fusion_loop_body_dim_03.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_fusion_loop_exit_dim_04.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_27.io.Out(0)

  Loop_0.io.loopBack(0) <> br_39.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_39.io.TrueOutput(0)

  Loop_1.io.enable <> br_10.io.Out(0)

  Loop_1.io.loopBack(0) <> br_24.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_24.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> bitcast_6.io.Out(0)

  Loop_0.io.InLiveIn(1) <> bitcast_dot9.io.Out(0)

  Loop_0.io.InLiveIn(2) <> bitcast_fusion26.io.Out(0)

  Loop_1.io.InLiveIn(0) <> bitcast_2.io.Out(0)

  Loop_1.io.InLiveIn(1) <> ld_3.io.Out(0)

  Loop_1.io.InLiveIn(2) <> bitcast_dot9.io.Out(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_29.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field0")(0)

  Gep_tmp431.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field1")(0)

  Gep_35.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_tmp12.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field0")(0)

  bitcast__phi_trans_insert915.io.Input <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Gep_tmp219.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_invar_inc337.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_invar_inc122.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phifusion_indvar_dim_0228.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phidot_indvar_rhs_1611.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(0)

  const1.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(1)

  const2.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(2)

  Gep_0.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(3)


  ld_1.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(4)


  bitcast_2.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(5)


  ld_3.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(6)


  Gep_4.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(7)


  ld_5.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(8)


  bitcast_6.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(9)


  Gep_7.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(10)


  ld_8.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(11)


  bitcast_dot9.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(12)


  br_10.io.enable <> bb_dot_loop_body_rhs_1_lr_ph0.io.Out(13)


  constf0.io.enable <> bb_dot_loop_exit_reduction1.io.Out(4)

  const3.io.enable <> bb_dot_loop_exit_reduction1.io.Out(0)

  const4.io.enable <> bb_dot_loop_exit_reduction1.io.Out(1)

  const5.io.enable <> bb_dot_loop_exit_reduction1.io.Out(2)

  const6.io.enable <> bb_dot_loop_exit_reduction1.io.Out(3)

  const7.io.enable <> bb_dot_loop_exit_reduction1.io.Out(5)

  const8.io.enable <> bb_dot_loop_exit_reduction1.io.Out(6)

  const9.io.enable <> bb_dot_loop_exit_reduction1.io.Out(7)

  const10.io.enable <> bb_dot_loop_exit_reduction1.io.Out(8)

  const11.io.enable <> bb_dot_loop_exit_reduction1.io.Out(9)

  phidot_indvar_rhs_1611.io.enable <> bb_dot_loop_exit_reduction1.io.Out(10)


  Gep_tmp12.io.enable <> bb_dot_loop_exit_reduction1.io.Out(11)


  Gep_tmp113.io.enable <> bb_dot_loop_exit_reduction1.io.Out(12)


  ld_14.io.enable <> bb_dot_loop_exit_reduction1.io.Out(13)


  bitcast__phi_trans_insert915.io.enable <> bb_dot_loop_exit_reduction1.io.Out(14)


  ld_16.io.enable <> bb_dot_loop_exit_reduction1.io.Out(15)


  FP_17.io.enable <> bb_dot_loop_exit_reduction1.io.Out(16)


  FP_18.io.enable <> bb_dot_loop_exit_reduction1.io.Out(17)


  Gep_tmp219.io.enable <> bb_dot_loop_exit_reduction1.io.Out(18)


  Gep_tmp320.io.enable <> bb_dot_loop_exit_reduction1.io.Out(19)


  st_21.io.enable <> bb_dot_loop_exit_reduction1.io.Out(20)


  binaryOp_invar_inc122.io.enable <> bb_dot_loop_exit_reduction1.io.Out(21)


  icmp_23.io.enable <> bb_dot_loop_exit_reduction1.io.Out(22)


  br_24.io.enable <> bb_dot_loop_exit_reduction1.io.Out(23)


  ld_25.io.enable <> bb_fusion_loop_body_dim_0_lr_ph2.io.Out(0)


  bitcast_fusion26.io.enable <> bb_fusion_loop_body_dim_0_lr_ph2.io.Out(1)


  br_27.io.enable <> bb_fusion_loop_body_dim_0_lr_ph2.io.Out(2)


  const12.io.enable <> bb_fusion_loop_body_dim_03.io.Out(0)

  const13.io.enable <> bb_fusion_loop_body_dim_03.io.Out(1)

  const14.io.enable <> bb_fusion_loop_body_dim_03.io.Out(2)

  const15.io.enable <> bb_fusion_loop_body_dim_03.io.Out(3)

  const16.io.enable <> bb_fusion_loop_body_dim_03.io.Out(4)

  const17.io.enable <> bb_fusion_loop_body_dim_03.io.Out(5)

  const18.io.enable <> bb_fusion_loop_body_dim_03.io.Out(6)

  const19.io.enable <> bb_fusion_loop_body_dim_03.io.Out(7)

  phifusion_indvar_dim_0228.io.enable <> bb_fusion_loop_body_dim_03.io.Out(8)


  Gep_29.io.enable <> bb_fusion_loop_body_dim_03.io.Out(9)


  ld_30.io.enable <> bb_fusion_loop_body_dim_03.io.Out(10)


  Gep_tmp431.io.enable <> bb_fusion_loop_body_dim_03.io.Out(11)


  Gep_tmp532.io.enable <> bb_fusion_loop_body_dim_03.io.Out(12)


  ld_33.io.enable <> bb_fusion_loop_body_dim_03.io.Out(13)


  FP_34.io.enable <> bb_fusion_loop_body_dim_03.io.Out(14)


  Gep_35.io.enable <> bb_fusion_loop_body_dim_03.io.Out(15)


  st_36.io.enable <> bb_fusion_loop_body_dim_03.io.Out(16)


  binaryOp_invar_inc337.io.enable <> bb_fusion_loop_body_dim_03.io.Out(17)


  icmp_38.io.enable <> bb_fusion_loop_body_dim_03.io.Out(18)


  br_39.io.enable <> bb_fusion_loop_body_dim_03.io.Out(19)


  bitcast_40.io.enable <> bb_fusion_loop_exit_dim_04.io.Out(0)


  st_41.io.enable <> bb_fusion_loop_exit_dim_04.io.Out(1)


  ret_42.io.In.enable <> bb_fusion_loop_exit_dim_04.io.Out(2)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phidot_indvar_rhs_1611.io.Mask <> bb_dot_loop_exit_reduction1.io.MaskBB(0)

  phifusion_indvar_dim_0228.io.Mask <> bb_fusion_loop_body_dim_03.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_1.io.memReq

  ld_1.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_3.io.memReq

  ld_3.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_5.io.memReq

  ld_5.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_8.io.memReq

  ld_8.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_16.io.memReq

  ld_16.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.WriteIn(0) <> st_21.io.memReq

  st_21.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(6) <> ld_25.io.memReq

  ld_25.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.ReadIn(7) <> ld_30.io.memReq

  ld_30.io.memResp <> MemCtrl.io.ReadOut(7)

  MemCtrl.io.ReadIn(8) <> ld_33.io.memReq

  ld_33.io.memResp <> MemCtrl.io.ReadOut(8)

  MemCtrl.io.WriteIn(1) <> st_36.io.memReq

  st_36.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_41.io.memReq

  st_41.io.memResp <> MemCtrl.io.WriteOut(2)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_0.io.idx(0) <> const0.io.Out

  Gep_4.io.idx(0) <> const1.io.Out

  Gep_7.io.idx(0) <> const2.io.Out

  phidot_indvar_rhs_1611.io.InData(0) <> const3.io.Out

  Gep_tmp12.io.idx(0) <> const4.io.Out

  Gep_tmp12.io.idx(1) <> const5.io.Out

  Gep_tmp113.io.idx(0) <> const6.io.Out

  Gep_tmp219.io.idx(0) <> const7.io.Out

  Gep_tmp219.io.idx(1) <> const8.io.Out

  Gep_tmp320.io.idx(0) <> const9.io.Out

  binaryOp_invar_inc122.io.RightIO <> const10.io.Out

  icmp_23.io.RightIO <> const11.io.Out

  phifusion_indvar_dim_0228.io.InData(0) <> const12.io.Out

  Gep_29.io.idx(0) <> const13.io.Out

  Gep_tmp431.io.idx(0) <> const14.io.Out

  Gep_tmp431.io.idx(1) <> const15.io.Out

  Gep_tmp532.io.idx(0) <> const16.io.Out

  Gep_35.io.idx(0) <> const17.io.Out

  binaryOp_invar_inc337.io.RightIO <> const18.io.Out

  icmp_38.io.RightIO <> const19.io.Out

  FP_18.io.RightIO <> constf0.io.Out(0)

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  bitcast_2.io.Input <> ld_1.io.Out(0)

  ld_5.io.GepAddr <> Gep_4.io.Out(0)

  bitcast_6.io.Input <> ld_5.io.Out(0)

  ld_8.io.GepAddr <> Gep_7.io.Out(0)

  bitcast_dot9.io.Input <> ld_8.io.Out(0)

  Gep_tmp113.io.idx(1) <> phidot_indvar_rhs_1611.io.Out(0)

  Gep_tmp320.io.idx(1) <> phidot_indvar_rhs_1611.io.Out(1)

  binaryOp_invar_inc122.io.LeftIO <> phidot_indvar_rhs_1611.io.Out(2)

  Gep_tmp113.io.baseAddress <> Gep_tmp12.io.Out(0)

  ld_14.io.GepAddr <> Gep_tmp113.io.Out(0)

  FP_17.io.RightIO <> ld_14.io.Out(0)

  ld_16.io.GepAddr <> bitcast__phi_trans_insert915.io.Out(0)

  FP_17.io.LeftIO <> ld_16.io.Out(0)

  FP_18.io.LeftIO <> FP_17.io.Out(0)

  st_21.io.inData <> FP_18.io.Out(0)

  Gep_tmp320.io.baseAddress <> Gep_tmp219.io.Out(0)

  st_21.io.GepAddr <> Gep_tmp320.io.Out(0)

  icmp_23.io.LeftIO <> binaryOp_invar_inc122.io.Out(1)

  br_24.io.CmpIO <> icmp_23.io.Out(0)

  bitcast_fusion26.io.Input <> ld_25.io.Out(0)

  st_41.io.inData <> ld_25.io.Out(1)

  Gep_29.io.idx(1) <> phifusion_indvar_dim_0228.io.Out(0)

  Gep_tmp532.io.idx(1) <> phifusion_indvar_dim_0228.io.Out(1)

  Gep_35.io.idx(1) <> phifusion_indvar_dim_0228.io.Out(2)

  binaryOp_invar_inc337.io.LeftIO <> phifusion_indvar_dim_0228.io.Out(3)

  ld_30.io.GepAddr <> Gep_29.io.Out(0)

  FP_34.io.LeftIO <> ld_30.io.Out(0)

  Gep_tmp532.io.baseAddress <> Gep_tmp431.io.Out(0)

  ld_33.io.GepAddr <> Gep_tmp532.io.Out(0)

  FP_34.io.RightIO <> ld_33.io.Out(0)

  st_36.io.inData <> FP_34.io.Out(0)

  st_36.io.GepAddr <> Gep_35.io.Out(0)

  icmp_38.io.LeftIO <> binaryOp_invar_inc337.io.Out(1)

  br_39.io.CmpIO <> icmp_38.io.Out(0)

  st_41.io.GepAddr <> bitcast_40.io.Out(0)

  bitcast_40.io.Input <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(0)

  ld_3.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_4.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_7.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  ld_25.io.GepAddr <> InputSplitter.io.Out.data.elements("field2")(1)

  st_21.io.Out(0).ready := true.B

  st_36.io.Out(0).ready := true.B

  st_41.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_42.io.Out

}

import java.io.{File, FileWriter}

abstract class dense05bTopIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val out = Decoupled(new Call(List()))
  })
}


class dense05bMain(implicit p: Parameters) extends dense05bTopIO {

  // Wire up the cache and modules under test.
  val test = Module(new test_05_dense_b_ir_4DF())
  val Stack = Module(new StackMem((1 << tlen) * 4))

  //Put an arbiter infront of cache

  // Connect input signals to cache
  Stack.io.req <> test.io.MemReq
  test.io.MemResp <> Stack.io.resp

  //Connect in/out ports
  test.io.in <> io.in
  io.out <> test.io.out

}


object test_05_dense_b_ir_4Top extends App {
  val dir = new File("RTL/test_05_dense_b_ir_4Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new dense05bMain()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
