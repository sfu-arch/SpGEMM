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

abstract class test_09_conv2d_a_ir_4DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class test_09_conv2d_a_ir_4DF(implicit p: Parameters) extends test_09_conv2d_a_ir_4DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 5, NWrites = 2)
  (WControl = new WriteMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 5, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,2, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1, 1, 1, 1), NumOuts = List(1, 1), NumCarry = List(1, 1, 1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(1), NumCarry = List(1, 1, 1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 4))

  val Loop_4 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 5))

  val Loop_5 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 6))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_convolution_loop_body_dim_1_lr_ph0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 9, BID = 0))

  val bb_convolution_loop_body_dim_2_lr_ph1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 1))

  val bb_convolution_loop_body_dim_3_lr_ph2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 1, BID = 2))

  val bb_convolution_inner_loop_body_k0_lr_ph3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 3))

  val bb_convolution_inner_loop_body_k1_lr_ph4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 14, NumPhi = 3, BID = 4))

  val bb_convolution_inner_loop_exit_iz_preheader5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 5))

  val bb_convolution_inner_loop_body_iz_lr_ph_us_preheader6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 6))

  val bb_convolution_inner_loop_exit_iz_us7 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 2, BID = 7))

  val bb_convolution_inner_loop_body_iz_lr_ph_us8 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 8, NumPhi = 3, BID = 8))

  val bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 24, BID = 9))

  val bb_convolution_inner_loop_exit_iz10 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 1, BID = 10))

  val bb_convolution_inner_loop_exit_k1_loopexit11 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 11))

  val bb_convolution_inner_loop_exit_k1_loopexit1212 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 12))

  val bb_convolution_inner_loop_exit_k113 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 2, BID = 13))

  val bb_convolution_inner_loop_exit_k014 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 8, BID = 14))

  val bb_convolution_loop_exit_dim_315 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 15))

  val bb_convolution_loop_exit_dim_216 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 16))

  val bb_convolution_loop_exit_dim_017 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 17))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %arg0.untyped = load i8*, i8** %params, align 8, !dereferenceable !1, !align !2, !UID !3
  val ld_0 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0))

  //  %0 = bitcast i8* %arg0.untyped to [1 x [8 x [8 x [1 x float]]]]*, !UID !4
  val bitcast_1 = Module(new BitCastNode(NumOuts = 1, ID = 1))

  //  %1 = getelementptr inbounds i8*, i8** %params, i64 1, !UID !5
  val Gep_2 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 2)(ElementSize = 8, ArraySize = List()))

  //  %arg1.untyped = load i8*, i8** %1, align 8, !dereferenceable !6, !align !2, !UID !7
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 1))

  //  %2 = bitcast i8* %arg1.untyped to [3 x [3 x [1 x [2 x float]]]]*, !UID !8
  val bitcast_4 = Module(new BitCastNode(NumOuts = 1, ID = 4))

  //  %3 = load i8*, i8** %temps, align 8, !dereferenceable !9, !align !10, !UID !11
  val ld_5 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 5, RouteID = 2))

  //  %convolution = bitcast i8* %3 to [1 x [8 x [8 x [2 x float]]]]*, !UID !12
  val bitcast_convolution6 = Module(new BitCastNode(NumOuts = 1, ID = 6))

  //  br label %convolution.loop_body.dim.2.lr.ph, !UID !13, !BB_UID !14
  val br_7 = Module(new UBranchNode(ID = 7))

  //  %convolution.indvar.dim.126 = phi i64 [ 0, %convolution.loop_body.dim.1.lr.ph ], [ %invar.inc1, %convolution.loop_exit.dim.2 ], !UID !15
  val phiconvolution_indvar_dim_1268 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 8, Res = true))

  //  br label %convolution.loop_body.dim.3.lr.ph, !UID !16, !BB_UID !17
  val br_9 = Module(new UBranchNode(ID = 9))

  //  %convolution.indvar.dim.223 = phi i64 [ 0, %convolution.loop_body.dim.2.lr.ph ], [ %invar.inc2, %convolution.loop_exit.dim.3 ], !UID !18
  val phiconvolution_indvar_dim_22310 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 10, Res = true))

  //  %tmp8 = getelementptr [1 x [8 x [8 x [2 x float]]]], [1 x [8 x [8 x [2 x float]]]]* %convolution, i64 0, i64 0
  val Gep_tmp811 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 11)(ElementSize = 512, ArraySize = List(512)))

  //  %tmp9 = getelementptr [8 x [8 x [2 x float]]], [8 x [8 x [2 x float]]]* %tmp8, i64 0, i64 %convolution.indvar.dim.126
  val Gep_tmp912 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 12)(ElementSize = 64, ArraySize = List(512)))

  //  %tmp10 = getelementptr [8 x [2 x float]], [8 x [2 x float]]* %tmp9, i64 0, i64 %convolution.indvar.dim.223
  val Gep_tmp1013 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 13)(ElementSize = 8, ArraySize = List(64)))

  //  br label %convolution.inner.loop_body.k0.lr.ph, !UID !19, !BB_UID !20
  val br_14 = Module(new UBranchNode(ID = 14))

  //  %convolution.indvar.dim.320 = phi i64 [ 0, %convolution.loop_body.dim.3.lr.ph ], [ %invar.inc3, %convolution.inner.loop_exit.k0 ], !UID !21
  val phiconvolution_indvar_dim_32015 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 15, Res = true))

  //  br label %convolution.inner.loop_body.k1.lr.ph, !UID !22, !BB_UID !23
  val br_16 = Module(new UBranchNode(ID = 16))

  //  %4 = phi float [ 0.000000e+00, %convolution.inner.loop_body.k0.lr.ph ], [ %22, %convolution.inner.loop_exit.k1 ], !UID !24
  val phi17 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 17, Res = true))

  //  %5 = phi float [ 0.000000e+00, %convolution.inner.loop_body.k0.lr.ph ], [ %23, %convolution.inner.loop_exit.k1 ], !UID !25
  val phi18 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 18, Res = true))

  //  %convolution.inner.indvar.k016 = phi i64 [ 0, %convolution.inner.loop_body.k0.lr.ph ], [ %invar.inc4, %convolution.inner.loop_exit.k1 ], !UID !26
  val phiconvolution_inner_indvar_k01619 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 19, Res = true))

  //  %6 = add i64 %convolution.indvar.dim.126, -1, !UID !27
  val binaryOp_20 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "add")(sign = false))

  //  %7 = add i64 %6, %convolution.inner.indvar.k016, !UID !28
  val binaryOp_21 = Module(new ComputeNode(NumOuts = 2, ID = 21, opCode = "add")(sign = false))

  //  %8 = icmp ult i64 %7, 8, !UID !29
  val icmp_22 = Module(new IcmpNode(NumOuts = 1, ID = 22, opCode = "ult")(sign = false))

  //  %9 = add i64 %convolution.indvar.dim.223, -1, !UID !30
  val binaryOp_23 = Module(new ComputeNode(NumOuts = 1, ID = 23, opCode = "add")(sign = false))

  //  br i1 %8, label %convolution.inner.loop_body.iz.lr.ph.us.preheader, label %convolution.inner.loop_exit.iz.preheader, !UID !31, !BB_UID !32
  val br_24 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 24))

  //  br label %convolution.inner.loop_exit.iz
  val br_25 = Module(new UBranchNode(ID = 25))

  //  br label %convolution.inner.loop_body.iz.lr.ph.us
  val br_26 = Module(new UBranchNode(ID = 26))

  //  %10 = phi float [ %20, %convolution.inner.loop_header.iz.convolution.inner.loop_exit.iz_crit_edge.us-lcssa.us.us ], [ %13, %convolution.inner.loop_body.iz.lr.ph.us ], !UID !33
  val phi27 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 27, Res = false))

  //  %11 = phi float [ %20, %convolution.inner.loop_header.iz.convolution.inner.loop_exit.iz_crit_edge.us-lcssa.us.us ], [ %14, %convolution.inner.loop_body.iz.lr.ph.us ], !UID !34
  val phi28 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 28, Res = false))

  //  %invar.inc5.us = add nuw nsw i64 %convolution.inner.indvar.k14.us, 1, !UID !35
  val binaryOp_invar_inc5_us29 = Module(new ComputeNode(NumOuts = 2, ID = 29, opCode = "add")(sign = false))

  //  %12 = icmp ugt i64 %invar.inc5.us, 2, !UID !36
  val icmp_30 = Module(new IcmpNode(NumOuts = 1, ID = 30, opCode = "ugt")(sign = false))

  //  br i1 %12, label %convolution.inner.loop_exit.k1.loopexit, label %convolution.inner.loop_body.iz.lr.ph.us, !UID !37, !BB_UID !38
  val br_31 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 31))

  //  %13 = phi float [ %10, %convolution.inner.loop_exit.iz.us ], [ %4, %convolution.inner.loop_body.iz.lr.ph.us.preheader ], !UID !39
  val phi32 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 32, Res = true))

  //  %14 = phi float [ %11, %convolution.inner.loop_exit.iz.us ], [ %5, %convolution.inner.loop_body.iz.lr.ph.us.preheader ], !UID !40
  val phi33 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 33, Res = true))

  //  %convolution.inner.indvar.k14.us = phi i64 [ %invar.inc5.us, %convolution.inner.loop_exit.iz.us ], [ 0, %convolution.inner.loop_body.iz.lr.ph.us.preheader ], !UID !41
  val phiconvolution_inner_indvar_k14_us34 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 34, Res = true))

  //  %15 = add i64 %9, %convolution.inner.indvar.k14.us, !UID !42
  val binaryOp_35 = Module(new ComputeNode(NumOuts = 2, ID = 35, opCode = "add")(sign = false))

  //  %16 = icmp ult i64 %15, 8, !UID !43
  val icmp_36 = Module(new IcmpNode(NumOuts = 1, ID = 36, opCode = "ult")(sign = false))

  //  br i1 %16, label %convolution.inner.loop_header.iz.convolution.inner.loop_exit.iz_crit_edge.us-lcssa.us.us, label %convolution.inner.loop_exit.iz.us, !UID !44, !BB_UID !45
  val br_37 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 37))

  //  %tmp = getelementptr [3 x [3 x [1 x [2 x float]]]], [3 x [3 x [1 x [2 x float]]]]* %2, i64 0, i64 %convolution.inner.indvar.k016, !UID !46
  val Gep_tmp38 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 38)(ElementSize = 24, ArraySize = List(72)))

  //  %tmp1 = getelementptr [3 x [1 x [2 x float]]], [3 x [1 x [2 x float]]]* %tmp, i64 0, i64 %convolution.inner.indvar.k14.us, !UID !47
  val Gep_tmp139 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 39)(ElementSize = 8, ArraySize = List(24)))

  //  %tmp2 = getelementptr [1 x [2 x float]], [1 x [2 x float]]* %tmp1, i64 0, i64 0, !UID !48
  val Gep_tmp240 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 40)(ElementSize = 8, ArraySize = List(8)))

  //  %tmp3 = getelementptr [2 x float], [2 x float]* %tmp2, i64 0, i64 %convolution.indvar.dim.320, !UID !49
  val Gep_tmp341 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 41)(ElementSize = 4, ArraySize = List(8)))

  //  %17 = load float, float* %tmp3, align 4, !UID !50
  val ld_42 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 42, RouteID = 3))

  //  %tmp4 = getelementptr [1 x [8 x [8 x [1 x float]]]], [1 x [8 x [8 x [1 x float]]]]* %0, i64 0, i64 0, !UID !51
  val Gep_tmp443 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 43)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp5 = getelementptr [8 x [8 x [1 x float]]], [8 x [8 x [1 x float]]]* %tmp4, i64 0, i64 %7, !UID !52
  val Gep_tmp544 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 44)(ElementSize = 32, ArraySize = List(256)))

  //  %tmp6 = getelementptr [8 x [1 x float]], [8 x [1 x float]]* %tmp5, i64 0, i64 %15, !UID !53
  val Gep_tmp645 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 45)(ElementSize = 4, ArraySize = List(32)))

  //  %tmp7 = getelementptr [1 x float], [1 x float]* %tmp6, i64 0, i64 0, !UID !54
  val Gep_tmp746 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 46)(ElementSize = 4, ArraySize = List(4)))

  //  %18 = load float, float* %tmp7, align 4, !UID !55
  val ld_47 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 47, RouteID = 4))

  //  %19 = fmul float %17, %18, !UID !56
  //val FP_48 = Module(new FPComputeNode(NumOuts = 1, ID = 48, opCode = "fmul")(t = p(FTYP)))
  val FP_48 = Module(new FPCustomMultiplierNode(NumOuts = 1, ID = 48, opCode = "fmul")(t = p(FTYP)))

  //  %20 = fadd float %13, %19, !UID !57
  //val FP_49 = Module(new FPComputeNode(NumOuts = 2, ID = 49, opCode = "fadd")(t = p(FTYP)))
  val FP_49 = Module(new FPCustomAdderNode(NumOuts = 2, ID = 49, opCode = "fadd")(t = p(FTYP)))

  //  br label %convolution.inner.loop_exit.iz.us, !UID !58, !BB_UID !59
  val br_50 = Module(new UBranchNode(ID = 50))

  //  %convolution.inner.indvar.k14 = phi i64 [ %invar.inc5, %convolution.inner.loop_exit.iz ], [ 0, %convolution.inner.loop_exit.iz.preheader ], !UID !60
  val phiconvolution_inner_indvar_k1451 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 51, Res = true))

  //  %invar.inc5 = add nuw nsw i64 %convolution.inner.indvar.k14, 1, !UID !61
  val binaryOp_invar_inc552 = Module(new ComputeNode(NumOuts = 2, ID = 52, opCode = "add")(sign = false))

  //  %21 = icmp ugt i64 %invar.inc5, 2, !UID !62
  val icmp_53 = Module(new IcmpNode(NumOuts = 1, ID = 53, opCode = "ugt")(sign = false))

  //  br i1 %21, label %convolution.inner.loop_exit.k1.loopexit12, label %convolution.inner.loop_exit.iz, !UID !63, !BB_UID !64
  val br_54 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 54))

  //  br label %convolution.inner.loop_exit.k1
  val br_55 = Module(new UBranchNode(ID = 55))

  //  br label %convolution.inner.loop_exit.k1
  val br_56 = Module(new UBranchNode(ID = 56))

  //  %22 = phi float [ %10, %convolution.inner.loop_exit.k1.loopexit ], [ %4, %convolution.inner.loop_exit.k1.loopexit12 ], !UID !65
  val phi57 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 57, Res = true))

  //  %23 = phi float [ %11, %convolution.inner.loop_exit.k1.loopexit ], [ %5, %convolution.inner.loop_exit.k1.loopexit12 ], !UID !66
  val phi58 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 58, Res = true))

  //  %invar.inc4 = add nuw nsw i64 %convolution.inner.indvar.k016, 1, !UID !67
  val binaryOp_invar_inc459 = Module(new ComputeNode(NumOuts = 2, ID = 59, opCode = "add")(sign = false))

  //  %24 = icmp ugt i64 %invar.inc4, 2, !UID !68
  val icmp_60 = Module(new IcmpNode(NumOuts = 1, ID = 60, opCode = "ugt")(sign = false))

  //  br i1 %24, label %convolution.inner.loop_exit.k0, label %convolution.inner.loop_body.k1.lr.ph, !UID !69, !BB_UID !70
  val br_61 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 61))

  //  %tmp11 = getelementptr [2 x float], [2 x float]* %tmp10, i64 0, i64 %convolution.indvar.dim.320, !UID !71
  val Gep_tmp1162 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 62)(ElementSize = 4, ArraySize = List(8)))

  //  store float %23, float* %tmp11, align 4, !UID !72
  val st_63 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 63, RouteID = 0))

  //  %invar.inc3 = add nuw nsw i64 %convolution.indvar.dim.320, 1, !UID !73
  val binaryOp_invar_inc364 = Module(new ComputeNode(NumOuts = 1, ID = 64, opCode = "add")(sign = false))

  //  %25 = icmp eq i64 %convolution.indvar.dim.320, 0, !UID !74
  val icmp_65 = Module(new IcmpNode(NumOuts = 1, ID = 65, opCode = "eq")(sign = false))

  //  br i1 %25, label %convolution.inner.loop_body.k0.lr.ph, label %convolution.loop_exit.dim.3, !UID !75, !BB_UID !76
  val br_66 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 66))

  //  %invar.inc2 = add nuw nsw i64 %convolution.indvar.dim.223, 1, !UID !77
  val binaryOp_invar_inc267 = Module(new ComputeNode(NumOuts = 2, ID = 67, opCode = "add")(sign = false))

  //  %26 = icmp ugt i64 %invar.inc2, 7, !UID !78
  val icmp_68 = Module(new IcmpNode(NumOuts = 1, ID = 68, opCode = "ugt")(sign = false))

  //  br i1 %26, label %convolution.loop_exit.dim.2, label %convolution.loop_body.dim.3.lr.ph, !UID !79, !BB_UID !80
  val br_69 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 69))

  //  %invar.inc1 = add nuw nsw i64 %convolution.indvar.dim.126, 1, !UID !81
  val binaryOp_invar_inc170 = Module(new ComputeNode(NumOuts = 2, ID = 70, opCode = "add")(sign = false))

  //  %27 = icmp ugt i64 %invar.inc1, 7, !UID !82
  val icmp_71 = Module(new IcmpNode(NumOuts = 1, ID = 71, opCode = "ugt")(sign = false))

  //  br i1 %27, label %convolution.loop_exit.dim.0, label %convolution.loop_body.dim.2.lr.ph, !UID !83, !BB_UID !84
  val br_72 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 72))

  //  %28 = bitcast i8* %retval to i8**, !UID !85
  val bitcast_73 = Module(new BitCastNode(NumOuts = 1, ID = 73))

  //  store i8* %3, i8** %28, align 8, !UID !86
  val st_74 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 74, RouteID = 1))

  //  ret void, !UID !87, !BB_UID !88
  val ret_75 = Module(new RetNode2(retTypes = List(), ID = 75))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 1
  val const0 = Module(new ConstFastNode(value = 1, ID = 0))

  //i64 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i64 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

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

  //i64 -1
  val const9 = Module(new ConstFastNode(value = -1, ID = 9))

  //i64 8
  val const10 = Module(new ConstFastNode(value = 8, ID = 10))

  //i64 -1
  val const11 = Module(new ConstFastNode(value = -1, ID = 11))

  //i64 1
  val const12 = Module(new ConstFastNode(value = 1, ID = 12))

  //i64 2
  val const13 = Module(new ConstFastNode(value = 2, ID = 13))

  //i64 0
  val const14 = Module(new ConstFastNode(value = 0, ID = 14))

  //i64 8
  val const15 = Module(new ConstFastNode(value = 8, ID = 15))

  //i64 0
  val const16 = Module(new ConstFastNode(value = 0, ID = 16))

  //i64 0
  val const17 = Module(new ConstFastNode(value = 0, ID = 17))

  //i64 0
  val const18 = Module(new ConstFastNode(value = 0, ID = 18))

  //i64 0
  val const19 = Module(new ConstFastNode(value = 0, ID = 19))

  //i64 0
  val const20 = Module(new ConstFastNode(value = 0, ID = 20))

  //i64 0
  val const21 = Module(new ConstFastNode(value = 0, ID = 21))

  //i64 0
  val const22 = Module(new ConstFastNode(value = 0, ID = 22))

  //i64 0
  val const23 = Module(new ConstFastNode(value = 0, ID = 23))

  //i64 0
  val const24 = Module(new ConstFastNode(value = 0, ID = 24))

  //i64 0
  val const25 = Module(new ConstFastNode(value = 0, ID = 25))

  //i64 0
  val const26 = Module(new ConstFastNode(value = 0, ID = 26))

  //i64 0
  val const27 = Module(new ConstFastNode(value = 0, ID = 27))

  //i64 1
  val const28 = Module(new ConstFastNode(value = 1, ID = 28))

  //i64 2
  val const29 = Module(new ConstFastNode(value = 2, ID = 29))

  //i64 1
  val const30 = Module(new ConstFastNode(value = 1, ID = 30))

  //i64 2
  val const31 = Module(new ConstFastNode(value = 2, ID = 31))

  //i64 0
  val const32 = Module(new ConstFastNode(value = 0, ID = 32))

  //i64 1
  val const33 = Module(new ConstFastNode(value = 1, ID = 33))

  //i64 0
  val const34 = Module(new ConstFastNode(value = 0, ID = 34))

  //i64 1
  val const35 = Module(new ConstFastNode(value = 1, ID = 35))

  //i64 7
  val const36 = Module(new ConstFastNode(value = 7, ID = 36))

  //i64 1
  val const37 = Module(new ConstFastNode(value = 1, ID = 37))

  //i64 7
  val const38 = Module(new ConstFastNode(value = 7, ID = 38))

  //float 0.000000e+00
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))

  //float 0.000000e+00
  val constf1 = Module(new ConstNode(value = 0x0, ID = 1))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_convolution_loop_body_dim_1_lr_ph0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_convolution_inner_loop_exit_iz_preheader5.io.predicateIn(0) <> br_24.io.FalseOutput(0)

  bb_convolution_inner_loop_body_iz_lr_ph_us_preheader6.io.predicateIn(0) <> br_24.io.TrueOutput(0)

  bb_convolution_inner_loop_exit_iz_us7.io.predicateIn(1) <> br_37.io.FalseOutput(0)

  bb_convolution_inner_loop_exit_iz_us7.io.predicateIn(0) <> br_50.io.Out(0)

  bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.predicateIn(0) <> br_37.io.TrueOutput(0)

  bb_convolution_inner_loop_exit_k113.io.predicateIn(1) <> br_55.io.Out(0)

  bb_convolution_inner_loop_exit_k113.io.predicateIn(0) <> br_56.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_convolution_loop_body_dim_2_lr_ph1.io.predicateIn(1) <> Loop_5.io.activate_loop_start

  bb_convolution_loop_body_dim_2_lr_ph1.io.predicateIn(0) <> Loop_5.io.activate_loop_back

  bb_convolution_loop_body_dim_3_lr_ph2.io.predicateIn(1) <> Loop_4.io.activate_loop_start

  bb_convolution_loop_body_dim_3_lr_ph2.io.predicateIn(0) <> Loop_4.io.activate_loop_back

  bb_convolution_inner_loop_body_k0_lr_ph3.io.predicateIn(1) <> Loop_3.io.activate_loop_start

  bb_convolution_inner_loop_body_k0_lr_ph3.io.predicateIn(0) <> Loop_3.io.activate_loop_back

  bb_convolution_inner_loop_body_k1_lr_ph4.io.predicateIn(1) <> Loop_2.io.activate_loop_start

  bb_convolution_inner_loop_body_k1_lr_ph4.io.predicateIn(0) <> Loop_2.io.activate_loop_back

  bb_convolution_inner_loop_body_iz_lr_ph_us8.io.predicateIn(0) <> Loop_1.io.activate_loop_start

  bb_convolution_inner_loop_body_iz_lr_ph_us8.io.predicateIn(1) <> Loop_1.io.activate_loop_back

  bb_convolution_inner_loop_exit_iz10.io.predicateIn(0) <> Loop_0.io.activate_loop_start

  bb_convolution_inner_loop_exit_iz10.io.predicateIn(1) <> Loop_0.io.activate_loop_back

  bb_convolution_inner_loop_exit_k1_loopexit11.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_convolution_inner_loop_exit_k1_loopexit1212.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_convolution_inner_loop_exit_k014.io.predicateIn(0) <> Loop_2.io.loopExit(0)

  bb_convolution_loop_exit_dim_315.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_convolution_loop_exit_dim_216.io.predicateIn(0) <> Loop_4.io.loopExit(0)

  bb_convolution_loop_exit_dim_017.io.predicateIn(0) <> Loop_5.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_25.io.Out(0)

  Loop_0.io.loopBack(0) <> br_54.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_54.io.TrueOutput(0)

  Loop_1.io.enable <> br_26.io.Out(0)

  Loop_1.io.loopBack(0) <> br_31.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_31.io.TrueOutput(0)

  Loop_2.io.enable <> br_16.io.Out(0)

  Loop_2.io.loopBack(0) <> br_61.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_61.io.TrueOutput(0)

  Loop_3.io.enable <> br_14.io.Out(0)

  Loop_3.io.loopBack(0) <> br_66.io.TrueOutput(0)

  Loop_3.io.loopFinish(0) <> br_66.io.FalseOutput(0)

  Loop_4.io.enable <> br_9.io.Out(0)

  Loop_4.io.loopBack(0) <> br_69.io.FalseOutput(0)

  Loop_4.io.loopFinish(0) <> br_69.io.TrueOutput(0)

  Loop_5.io.enable <> br_7.io.Out(0)

  Loop_5.io.loopBack(0) <> br_72.io.FalseOutput(0)

  Loop_5.io.loopFinish(0) <> br_72.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_1.io.InLiveIn(0) <> phi17.io.Out(0)

  Loop_1.io.InLiveIn(1) <> phi18.io.Out(0)

  Loop_1.io.InLiveIn(2) <> binaryOp_23.io.Out(0)

  Loop_1.io.InLiveIn(3) <> phiconvolution_inner_indvar_k01619.io.Out(0)

  Loop_1.io.InLiveIn(4) <> binaryOp_21.io.Out(0)

  Loop_1.io.InLiveIn(5) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(6) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(7) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_2.io.InLiveIn(0) <> phiconvolution_indvar_dim_32015.io.Out(0)

  Loop_2.io.InLiveIn(1) <> Loop_3.io.OutLiveIn.elements("field2")(0)

  Loop_2.io.InLiveIn(2) <> Loop_3.io.OutLiveIn.elements("field4")(0)

  Loop_2.io.InLiveIn(3) <> Loop_3.io.OutLiveIn.elements("field0")(0)

  Loop_2.io.InLiveIn(4) <> Loop_3.io.OutLiveIn.elements("field3")(0)

  Loop_3.io.InLiveIn(0) <> phiconvolution_indvar_dim_22310.io.Out(0)

  Loop_3.io.InLiveIn(1) <> Gep_tmp1013.io.Out(0)

  Loop_3.io.InLiveIn(2) <> Loop_4.io.OutLiveIn.elements("field1")(0)

  Loop_3.io.InLiveIn(3) <> Loop_4.io.OutLiveIn.elements("field0")(0)

  Loop_3.io.InLiveIn(4) <> Loop_4.io.OutLiveIn.elements("field2")(0)

  Loop_4.io.InLiveIn(0) <> phiconvolution_indvar_dim_1268.io.Out(0)

  Loop_4.io.InLiveIn(1) <> Loop_5.io.OutLiveIn.elements("field2")(0)

  Loop_4.io.InLiveIn(2) <> Loop_5.io.OutLiveIn.elements("field1")(0)

  Loop_4.io.InLiveIn(3) <> Loop_5.io.OutLiveIn.elements("field0")(0)

  Loop_5.io.InLiveIn(0) <> bitcast_convolution6.io.Out(0)

  Loop_5.io.InLiveIn(1) <> bitcast_4.io.Out(0)

  Loop_5.io.InLiveIn(2) <> bitcast_1.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phi32.io.InData(1) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  phi33.io.InData(1) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  binaryOp_35.io.LeftIO <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Gep_tmp38.io.idx(1) <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Gep_tmp544.io.idx(1) <> Loop_1.io.OutLiveIn.elements("field4")(0)

  Gep_tmp443.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field5")(0)

  Gep_tmp341.io.idx(1) <> Loop_1.io.OutLiveIn.elements("field6")(0)

  Gep_tmp38.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field7")(0)

  binaryOp_23.io.LeftIO <> Loop_2.io.OutLiveIn.elements("field3")(0)

  binaryOp_20.io.LeftIO <> Loop_2.io.OutLiveIn.elements("field4")(0)

  Gep_tmp1162.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field1")(0)

  Gep_tmp912.io.idx(1) <> Loop_4.io.OutLiveIn.elements("field0")(1)

  Gep_tmp811.io.baseAddress <> Loop_4.io.OutLiveIn.elements("field3")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_1.io.InLiveOut(0) <> phi27.io.Out(0)

  Loop_1.io.InLiveOut(1) <> phi28.io.Out(0)

  Loop_2.io.InLiveOut(0) <> phi58.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phi57.io.InData(0) <> Loop_1.io.OutLiveOut.elements("field0")(0)

  phi58.io.InData(0) <> Loop_1.io.OutLiveOut.elements("field1")(0)

  st_63.io.inData <> Loop_2.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_invar_inc552.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> phi28.io.Out(1)

  Loop_1.io.CarryDepenIn(1) <> binaryOp_invar_inc5_us29.io.Out(1)

  Loop_1.io.CarryDepenIn(2) <> phi27.io.Out(1)

  Loop_2.io.CarryDepenIn(0) <> phi57.io.Out(0)

  Loop_2.io.CarryDepenIn(1) <> binaryOp_invar_inc459.io.Out(0)

  Loop_2.io.CarryDepenIn(2) <> phi58.io.Out(1)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_invar_inc364.io.Out(0)

  Loop_4.io.CarryDepenIn(0) <> binaryOp_invar_inc267.io.Out(0)

  Loop_5.io.CarryDepenIn(0) <> binaryOp_invar_inc170.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phiconvolution_inner_indvar_k1451.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi33.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phiconvolution_inner_indvar_k14_us34.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field1")(0)

  phi32.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field2")(0)

  phi17.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phiconvolution_inner_indvar_k01619.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field1")(0)

  phi18.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field2")(0)

  phiconvolution_indvar_dim_32015.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phiconvolution_indvar_dim_22310.io.InData(1) <> Loop_4.io.CarryDepenOut.elements("field0")(0)

  phiconvolution_indvar_dim_1268.io.InData(1) <> Loop_5.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(0)

  ld_0.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(1)


  bitcast_1.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(2)


  Gep_2.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(3)


  ld_3.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(4)


  bitcast_4.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(5)


  ld_5.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(6)


  bitcast_convolution6.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(7)


  br_7.io.enable <> bb_convolution_loop_body_dim_1_lr_ph0.io.Out(8)


  const1.io.enable <> bb_convolution_loop_body_dim_2_lr_ph1.io.Out(0)

  phiconvolution_indvar_dim_1268.io.enable <> bb_convolution_loop_body_dim_2_lr_ph1.io.Out(1)


  br_9.io.enable <> bb_convolution_loop_body_dim_2_lr_ph1.io.Out(2)


  const2.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(0)

  const3.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(1)

  const4.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(2)

  const5.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(3)

  const6.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(4)

  phiconvolution_indvar_dim_22310.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(5)


  Gep_tmp811.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(6)


  Gep_tmp912.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(7)


  Gep_tmp1013.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(8)


  br_14.io.enable <> bb_convolution_loop_body_dim_3_lr_ph2.io.Out(9)


  const7.io.enable <> bb_convolution_inner_loop_body_k0_lr_ph3.io.Out(0)

  phiconvolution_indvar_dim_32015.io.enable <> bb_convolution_inner_loop_body_k0_lr_ph3.io.Out(1)


  br_16.io.enable <> bb_convolution_inner_loop_body_k0_lr_ph3.io.Out(2)


  constf0.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(0)

  constf1.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(1)

  const8.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(2)

  const9.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(3)

  const10.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(4)

  const11.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(5)

  phi17.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(6)


  phi18.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(7)


  phiconvolution_inner_indvar_k01619.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(8)


  binaryOp_20.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(9)


  binaryOp_21.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(10)


  icmp_22.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(11)


  binaryOp_23.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(12)


  br_24.io.enable <> bb_convolution_inner_loop_body_k1_lr_ph4.io.Out(13)


  br_25.io.enable <> bb_convolution_inner_loop_exit_iz_preheader5.io.Out(0)


  br_26.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us_preheader6.io.Out(0)


  const12.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(0)

  const13.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(1)

  phi27.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(2)


  phi28.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(3)


  binaryOp_invar_inc5_us29.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(4)


  icmp_30.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(5)


  br_31.io.enable <> bb_convolution_inner_loop_exit_iz_us7.io.Out(6)


  const14.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(0)

  const15.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(1)

  phi32.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(2)


  phi33.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(3)


  phiconvolution_inner_indvar_k14_us34.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(4)


  binaryOp_35.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(5)


  icmp_36.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(6)


  br_37.io.enable <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.Out(7)


  const16.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(0)

  const17.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(1)

  const18.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(2)

  const19.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(3)

  const20.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(4)

  const21.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(5)

  const22.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(6)

  const23.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(7)

  const24.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(8)

  const25.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(9)

  const26.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(10)

  Gep_tmp38.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(11)


  Gep_tmp139.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(12)


  Gep_tmp240.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(13)


  Gep_tmp341.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(14)


  ld_42.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(15)


  Gep_tmp443.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(16)


  Gep_tmp544.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(17)


  Gep_tmp645.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(18)


  Gep_tmp746.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(19)


  ld_47.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(20)


  FP_48.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(21)


  FP_49.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(22)


  br_50.io.enable <> bb_convolution_inner_loop_header_iz_convolution_inner_loop_exit_iz_crit_edge_us_lcssa_us_us9.io.Out(23)


  const27.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(0)

  const28.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(1)

  const29.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(2)

  phiconvolution_inner_indvar_k1451.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(3)


  binaryOp_invar_inc552.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(4)


  icmp_53.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(5)


  br_54.io.enable <> bb_convolution_inner_loop_exit_iz10.io.Out(6)


  br_55.io.enable <> bb_convolution_inner_loop_exit_k1_loopexit11.io.Out(0)


  br_56.io.enable <> bb_convolution_inner_loop_exit_k1_loopexit1212.io.Out(0)


  const30.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(0)

  const31.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(1)

  phi57.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(2)


  phi58.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(3)


  binaryOp_invar_inc459.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(4)


  icmp_60.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(5)


  br_61.io.enable <> bb_convolution_inner_loop_exit_k113.io.Out(6)


  const32.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(0)

  const33.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(1)

  const34.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(2)

  Gep_tmp1162.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(3)


  st_63.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(4)


  binaryOp_invar_inc364.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(5)


  icmp_65.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(6)


  br_66.io.enable <> bb_convolution_inner_loop_exit_k014.io.Out(7)


  const35.io.enable <> bb_convolution_loop_exit_dim_315.io.Out(0)

  const36.io.enable <> bb_convolution_loop_exit_dim_315.io.Out(1)

  binaryOp_invar_inc267.io.enable <> bb_convolution_loop_exit_dim_315.io.Out(2)


  icmp_68.io.enable <> bb_convolution_loop_exit_dim_315.io.Out(3)


  br_69.io.enable <> bb_convolution_loop_exit_dim_315.io.Out(4)


  const37.io.enable <> bb_convolution_loop_exit_dim_216.io.Out(0)

  const38.io.enable <> bb_convolution_loop_exit_dim_216.io.Out(1)

  binaryOp_invar_inc170.io.enable <> bb_convolution_loop_exit_dim_216.io.Out(2)


  icmp_71.io.enable <> bb_convolution_loop_exit_dim_216.io.Out(3)


  br_72.io.enable <> bb_convolution_loop_exit_dim_216.io.Out(4)


  bitcast_73.io.enable <> bb_convolution_loop_exit_dim_017.io.Out(0)


  st_74.io.enable <> bb_convolution_loop_exit_dim_017.io.Out(1)


  ret_75.io.In.enable <> bb_convolution_loop_exit_dim_017.io.Out(2)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phiconvolution_indvar_dim_1268.io.Mask <> bb_convolution_loop_body_dim_2_lr_ph1.io.MaskBB(0)

  phiconvolution_indvar_dim_22310.io.Mask <> bb_convolution_loop_body_dim_3_lr_ph2.io.MaskBB(0)

  phiconvolution_indvar_dim_32015.io.Mask <> bb_convolution_inner_loop_body_k0_lr_ph3.io.MaskBB(0)

  phi17.io.Mask <> bb_convolution_inner_loop_body_k1_lr_ph4.io.MaskBB(0)

  phi18.io.Mask <> bb_convolution_inner_loop_body_k1_lr_ph4.io.MaskBB(1)

  phiconvolution_inner_indvar_k01619.io.Mask <> bb_convolution_inner_loop_body_k1_lr_ph4.io.MaskBB(2)

  phi27.io.Mask <> bb_convolution_inner_loop_exit_iz_us7.io.MaskBB(0)

  phi28.io.Mask <> bb_convolution_inner_loop_exit_iz_us7.io.MaskBB(1)

  phi32.io.Mask <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.MaskBB(0)

  phi33.io.Mask <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.MaskBB(1)

  phiconvolution_inner_indvar_k14_us34.io.Mask <> bb_convolution_inner_loop_body_iz_lr_ph_us8.io.MaskBB(2)

  phiconvolution_inner_indvar_k1451.io.Mask <> bb_convolution_inner_loop_exit_iz10.io.MaskBB(0)

  phi57.io.Mask <> bb_convolution_inner_loop_exit_k113.io.MaskBB(0)

  phi58.io.Mask <> bb_convolution_inner_loop_exit_k113.io.MaskBB(1)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_0.io.memReq

  ld_0.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_3.io.memReq

  ld_3.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_5.io.memReq

  ld_5.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_42.io.memReq

  ld_42.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_47.io.memReq

  ld_47.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.WriteIn(0) <> st_63.io.memReq

  st_63.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_74.io.memReq

  st_74.io.memResp <> MemCtrl.io.WriteOut(1)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_2.io.idx(0) <> const0.io.Out

  phiconvolution_indvar_dim_1268.io.InData(0) <> const1.io.Out

  phiconvolution_indvar_dim_22310.io.InData(0) <> const2.io.Out

  Gep_tmp811.io.idx(0) <> const3.io.Out

  Gep_tmp811.io.idx(1) <> const4.io.Out

  Gep_tmp912.io.idx(0) <> const5.io.Out

  Gep_tmp1013.io.idx(0) <> const6.io.Out

  phiconvolution_indvar_dim_32015.io.InData(0) <> const7.io.Out

  phiconvolution_inner_indvar_k01619.io.InData(0) <> const8.io.Out

  binaryOp_20.io.RightIO <> const9.io.Out

  icmp_22.io.RightIO <> const10.io.Out

  binaryOp_23.io.RightIO <> const11.io.Out

  binaryOp_invar_inc5_us29.io.RightIO <> const12.io.Out

  icmp_30.io.RightIO <> const13.io.Out

  phiconvolution_inner_indvar_k14_us34.io.InData(1) <> const14.io.Out

  icmp_36.io.RightIO <> const15.io.Out

  Gep_tmp38.io.idx(0) <> const16.io.Out

  Gep_tmp139.io.idx(0) <> const17.io.Out

  Gep_tmp240.io.idx(0) <> const18.io.Out

  Gep_tmp240.io.idx(1) <> const19.io.Out

  Gep_tmp341.io.idx(0) <> const20.io.Out

  Gep_tmp443.io.idx(0) <> const21.io.Out

  Gep_tmp443.io.idx(1) <> const22.io.Out

  Gep_tmp544.io.idx(0) <> const23.io.Out

  Gep_tmp645.io.idx(0) <> const24.io.Out

  Gep_tmp746.io.idx(0) <> const25.io.Out

  Gep_tmp746.io.idx(1) <> const26.io.Out

  phiconvolution_inner_indvar_k1451.io.InData(1) <> const27.io.Out

  binaryOp_invar_inc552.io.RightIO <> const28.io.Out

  icmp_53.io.RightIO <> const29.io.Out

  binaryOp_invar_inc459.io.RightIO <> const30.io.Out

  icmp_60.io.RightIO <> const31.io.Out

  Gep_tmp1162.io.idx(0) <> const32.io.Out

  binaryOp_invar_inc364.io.RightIO <> const33.io.Out

  icmp_65.io.RightIO <> const34.io.Out

  binaryOp_invar_inc267.io.RightIO <> const35.io.Out

  icmp_68.io.RightIO <> const36.io.Out

  binaryOp_invar_inc170.io.RightIO <> const37.io.Out

  icmp_71.io.RightIO <> const38.io.Out

  phi17.io.InData(0) <> constf0.io.Out(0)

  phi18.io.InData(0) <> constf1.io.Out(0)

  bitcast_1.io.Input <> ld_0.io.Out(0)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  bitcast_4.io.Input <> ld_3.io.Out(0)

  bitcast_convolution6.io.Input <> ld_5.io.Out(0)

  st_74.io.inData <> ld_5.io.Out(1)

  binaryOp_invar_inc170.io.LeftIO <> phiconvolution_indvar_dim_1268.io.Out(1)

  Gep_tmp1013.io.idx(1) <> phiconvolution_indvar_dim_22310.io.Out(1)

  binaryOp_invar_inc267.io.LeftIO <> phiconvolution_indvar_dim_22310.io.Out(2)

  Gep_tmp912.io.baseAddress <> Gep_tmp811.io.Out(0)

  Gep_tmp1013.io.baseAddress <> Gep_tmp912.io.Out(0)

  Gep_tmp1162.io.idx(1) <> phiconvolution_indvar_dim_32015.io.Out(1)

  binaryOp_invar_inc364.io.LeftIO <> phiconvolution_indvar_dim_32015.io.Out(2)

  icmp_65.io.LeftIO <> phiconvolution_indvar_dim_32015.io.Out(3)

  phi57.io.InData(1) <> phi17.io.Out(1)

  phi58.io.InData(1) <> phi18.io.Out(1)

  binaryOp_21.io.RightIO <> phiconvolution_inner_indvar_k01619.io.Out(1)

  binaryOp_invar_inc459.io.LeftIO <> phiconvolution_inner_indvar_k01619.io.Out(2)

  binaryOp_21.io.LeftIO <> binaryOp_20.io.Out(0)

  icmp_22.io.LeftIO <> binaryOp_21.io.Out(1)

  br_24.io.CmpIO <> icmp_22.io.Out(0)

  icmp_30.io.LeftIO <> binaryOp_invar_inc5_us29.io.Out(0)

  br_31.io.CmpIO <> icmp_30.io.Out(0)

  phi27.io.InData(1) <> phi32.io.Out(0)

  FP_49.io.LeftIO <> phi32.io.Out(1)

  phi28.io.InData(1) <> phi33.io.Out(0)

  binaryOp_invar_inc5_us29.io.LeftIO <> phiconvolution_inner_indvar_k14_us34.io.Out(0)

  binaryOp_35.io.RightIO <> phiconvolution_inner_indvar_k14_us34.io.Out(1)

  Gep_tmp139.io.idx(1) <> phiconvolution_inner_indvar_k14_us34.io.Out(2)

  icmp_36.io.LeftIO <> binaryOp_35.io.Out(0)

  Gep_tmp645.io.idx(1) <> binaryOp_35.io.Out(1)

  br_37.io.CmpIO <> icmp_36.io.Out(0)

  Gep_tmp139.io.baseAddress <> Gep_tmp38.io.Out(0)

  Gep_tmp240.io.baseAddress <> Gep_tmp139.io.Out(0)

  Gep_tmp341.io.baseAddress <> Gep_tmp240.io.Out(0)

  ld_42.io.GepAddr <> Gep_tmp341.io.Out(0)

  FP_48.io.LeftIO <> ld_42.io.Out(0)

  Gep_tmp544.io.baseAddress <> Gep_tmp443.io.Out(0)

  Gep_tmp645.io.baseAddress <> Gep_tmp544.io.Out(0)

  Gep_tmp746.io.baseAddress <> Gep_tmp645.io.Out(0)

  ld_47.io.GepAddr <> Gep_tmp746.io.Out(0)

  FP_48.io.RightIO <> ld_47.io.Out(0)

  FP_49.io.RightIO <> FP_48.io.Out(0)

  phi27.io.InData(0) <> FP_49.io.Out(0)

  phi28.io.InData(0) <> FP_49.io.Out(1)

  binaryOp_invar_inc552.io.LeftIO <> phiconvolution_inner_indvar_k1451.io.Out(0)

  icmp_53.io.LeftIO <> binaryOp_invar_inc552.io.Out(1)

  br_54.io.CmpIO <> icmp_53.io.Out(0)

  icmp_60.io.LeftIO <> binaryOp_invar_inc459.io.Out(1)

  br_61.io.CmpIO <> icmp_60.io.Out(0)

  st_63.io.GepAddr <> Gep_tmp1162.io.Out(0)

  br_66.io.CmpIO <> icmp_65.io.Out(0)

  icmp_68.io.LeftIO <> binaryOp_invar_inc267.io.Out(1)

  br_69.io.CmpIO <> icmp_68.io.Out(0)

  icmp_71.io.LeftIO <> binaryOp_invar_inc170.io.Out(1)

  br_72.io.CmpIO <> icmp_71.io.Out(0)

  st_74.io.GepAddr <> bitcast_73.io.Out(0)

  bitcast_73.io.Input <> InputSplitter.io.Out.data.elements("field0")(0)

  ld_0.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(1)

  ld_5.io.GepAddr <> InputSplitter.io.Out.data.elements("field2")(0)

  st_63.io.Out(0).ready := true.B

  st_74.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_75.io.Out

}

import java.io.{File, FileWriter}

abstract class softmax09aTopIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val out = Decoupled(new Call(List()))
  })
}


class softmax09aMain(implicit p: Parameters) extends softmax09aTopIO {

  // Wire up the cache and modules under test.
  val test = Module(new test_09_conv2d_a_ir_4DF())
  val Stack = Module(new StackMem((1 << tlen) * 4))

  //Put an arbiter infront of cache

  // Connect input signals to cache
  Stack.io.req <> test.io.MemReq
  test.io.MemResp <> Stack.io.resp

  //Connect in/out ports
  test.io.in <> io.in
  io.out <> test.io.out

}


object test_09_conv2d_a_ir_4Top extends App {
  val dir = new File("RTL/test_09_conv2d_a_ir_4Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new softmax09aMain()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
