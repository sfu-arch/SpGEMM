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

abstract class test_07_softmax_b_ir_4DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class test_07_softmax_b_ir_4DF(implicit p: Parameters) extends test_07_softmax_b_ir_4DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 9, NWrites = 5)
  (WControl = new WriteMemoryController(NumOps = 5, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 9, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  //val SharedFPU = Module(new SharedFPU(NumOps = 1, PipeDepth = 32)(t = p(FTYP)))

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 4))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 0))

  val bb_reduce_inner_loop_body_reduction_dim_11 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 20, NumPhi = 2, BID = 1))

  val bb_fusion_1_loop_body_dim_1_lr_ph2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 7, BID = 2))

  val bb_fusion_1_loop_body_dim_13 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 21, NumPhi = 1, BID = 3))

  val bb_reduce_1_inner_loop_body_reduction_dim_1_lr_ph4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 4))

  val bb_reduce_1_inner_loop_body_reduction_dim_15 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 16, NumPhi = 2, BID = 5))

  val bb_fusion_loop_body_dim_0_lr_ph6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 6))

  val bb_fusion_loop_body_dim_07 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 18, NumPhi = 1, BID = 7))

  val bb_fusion_loop_exit_dim_08 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 8))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %arg0.untyped = load i8*, i8** %params, align 8, !dereferenceable !1, !align !2, !UID !3
  val ld_0 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0))

  //  %bitcast = bitcast i8* %arg0.untyped to [1 x [64 x float]]*, !UID !4
  val bitcast_bitcast1 = Module(new BitCastNode(NumOuts = 2, ID = 1))

  //  %0 = load i8*, i8** %temps, align 8, !dereferenceable !1, !align !2, !UID !5
  val ld_2 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 2, RouteID = 1))

  //  br label %reduce.inner.loop_body.reduction_dim.1, !UID !6, !BB_UID !7
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %1 = phi float [ 0xFFF0000000000000, %reduce.inner.loop_body.reduction_dim.1.lr.ph ], [ %6, %reduce.inner.loop_body.reduction_dim.1 ], !UID !8
  val phi4 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 4, Res = true))

  //  %reduce.inner.indvar.reduction_dim.112 = phi i64 [ 0, %reduce.inner.loop_body.reduction_dim.1.lr.ph ], [ %invar.inc1, %reduce.inner.loop_body.reduction_dim.1 ], !UID !9
  val phireduce_inner_indvar_reduction_dim_1125 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 5, Res = true))

  //  %tmp = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %bitcast, i64 0, i64 0, !UID !10
  val Gep_tmp6 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 6)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp1 = getelementptr [64 x float], [64 x float]* %tmp, i64 0, i64 %reduce.inner.indvar.reduction_dim.112, !UID !11
  val Gep_tmp17 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 7)(ElementSize = 4, ArraySize = List(256)))

  //  %2 = load float, float* %tmp1, align 4, !UID !12
  val ld_8 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 8, RouteID = 2))

  //  %3 = fcmp oge float %1, %2, !UID !13
  val FPCMP_9 = Module(new FPCompareNode(NumOuts = 1, ID = 9, opCode = ">GT")(t = p(FTYP)))

  //  %4 = fcmp uno float %1, 0.000000e+00, !UID !14
  val FPCMP_10 = Module(new FPCompareNode(NumOuts = 1, ID = 10, opCode = "=EQ")(t = p(FTYP)))

  //  %5 = or i1 %3, %4, !UID !15
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "or")(sign = false))

  //  %6 = select i1 %5, float %1, float %2, !UID !16
  val select_12 = Module(new SelectNode(NumOuts = 2, ID = 12))

  //  %invar.inc1 = add nuw nsw i64 %reduce.inner.indvar.reduction_dim.112, 1, !UID !17
  val binaryOp_invar_inc113 = Module(new ComputeNode(NumOuts = 2, ID = 13, opCode = "add")(sign = false))

  //  %7 = icmp ugt i64 %invar.inc1, 63, !UID !18
  val icmp_14 = Module(new IcmpNode(NumOuts = 1, ID = 14, opCode = "ugt")(sign = false))

  //  br i1 %7, label %fusion.1.loop_body.dim.1.lr.ph, label %reduce.inner.loop_body.reduction_dim.1, !UID !19, !BB_UID !20
  val br_15 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 15))

  //  %8 = bitcast i8* %0 to float*, !UID !21
  val bitcast_16 = Module(new BitCastNode(NumOuts = 2, ID = 16))

  //  store float %6, float* %8, align 4, !UID !22
  val st_17 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 17, RouteID = 0))

  //  %9 = getelementptr inbounds i8*, i8** %temps, i64 9, !UID !23
  val Gep_18 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 18)(ElementSize = 8, ArraySize = List()))

  //  %10 = load i8*, i8** %9, align 8, !dereferenceable !24, !align !2, !UID !25
  val ld_19 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 19, RouteID = 3))

  //  %fusion.1 = bitcast i8* %10 to [1 x [64 x float]]*, !UID !26
  val bitcast_fusion_120 = Module(new BitCastNode(NumOuts = 3, ID = 20))

  //  br label %fusion.1.loop_body.dim.1, !UID !27, !BB_UID !28
  val br_21 = Module(new UBranchNode(ID = 21))

  //  %fusion.1.indvar.dim.18 = phi i64 [ 0, %fusion.1.loop_body.dim.1.lr.ph ], [ %invar.inc3, %fusion.1.loop_body.dim.1 ], !UID !29
  val phifusion_1_indvar_dim_1822 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 22, Res = true))

  //  %tmp2 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %bitcast, i64 0, i64 0, !UID !30
  val Gep_tmp223 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 23)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp3 = getelementptr [64 x float], [64 x float]* %tmp2, i64 0, i64 %fusion.1.indvar.dim.18, !UID !31
  val Gep_tmp324 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 24)(ElementSize = 4, ArraySize = List(256)))

  //  %11 = load float, float* %tmp3, align 4, !UID !32
  val ld_25 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 25, RouteID = 4))

  //  %12 = load float, float* %8, align 4, !UID !33
  val ld_26 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 26, RouteID = 5))

  //  %13 = fsub float %11, %12, !UID !34
  //val FP_27 = Module(new FPComputeNode(NumOuts = 0, ID = 27, opCode = "fsub")(t = p(FTYP)))
  val FP_27 = Module(new FPCustomSubtractorNode(NumOuts = 1, ID = 27, opCode = "fsub")(t = p(FTYP)))

  //  %tmp4 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %fusion.1, i64 0, i64 0, !UID !35
  val Gep_tmp428 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 28)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp5 = getelementptr [64 x float], [64 x float]* %tmp4, i64 0, i64 %fusion.1.indvar.dim.18, !UID !36
  val Gep_tmp529 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 29)(ElementSize = 4, ArraySize = List(256)))

  //  store float %14, float* %tmp5, align 4, !UID !37
  val st_30 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 30, RouteID = 1))

  //  %invar.inc3 = add nuw nsw i64 %fusion.1.indvar.dim.18, 1, !UID !38
  val binaryOp_invar_inc331 = Module(new ComputeNode(NumOuts = 2, ID = 31, opCode = "add")(sign = false))

  //  %15 = icmp ugt i64 %invar.inc3, 63, !UID !39
  val icmp_32 = Module(new IcmpNode(NumOuts = 1, ID = 32, opCode = "ugt")(sign = false))

  //  br i1 %15, label %reduce.1.inner.loop_body.reduction_dim.1.lr.ph, label %fusion.1.loop_body.dim.1, !UID !40, !BB_UID !41
  val br_33 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 33))

  //  %16 = getelementptr inbounds i8, i8* %10, i64 256, !UID !42
  val Gep_34 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 34)(ElementSize = 1, ArraySize = List()))

  //  br label %reduce.1.inner.loop_body.reduction_dim.1, !UID !43, !BB_UID !44
  val br_35 = Module(new UBranchNode(ID = 35))

  //  %17 = phi float [ 0.000000e+00, %reduce.1.inner.loop_body.reduction_dim.1.lr.ph ], [ %19, %reduce.1.inner.loop_body.reduction_dim.1 ], !UID !45
  val phi36 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 36, Res = true))

  //  %reduce.1.inner.indvar.reduction_dim.14 = phi i64 [ 0, %reduce.1.inner.loop_body.reduction_dim.1.lr.ph ], [ %invar.inc6, %reduce.1.inner.loop_body.reduction_dim.1 ], !UID !46
  val phireduce_1_inner_indvar_reduction_dim_1437 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 37, Res = true))

  //  %tmp6 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %fusion.1, i64 0, i64 0, !UID !47
  val Gep_tmp638 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 38)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp7 = getelementptr [64 x float], [64 x float]* %tmp6, i64 0, i64 %reduce.1.inner.indvar.reduction_dim.14, !UID !48
  val Gep_tmp739 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 39)(ElementSize = 4, ArraySize = List(256)))

  //  %18 = load float, float* %tmp7, align 4, !UID !49
  val ld_40 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 40, RouteID = 6))

  //  %19 = fadd float %17, %18, !UID !50
  //val FP_41 = Module(new FPComputeNode(NumOuts = 2, ID = 41, opCode = "fadd")(t = p(FTYP)))
  val FP_41 = Module(new FPCustomAdderNode(NumOuts = 2, ID = 41, opCode = "fadd")(t = p(FTYP)))

  //  %invar.inc6 = add nuw nsw i64 %reduce.1.inner.indvar.reduction_dim.14, 1, !UID !51
  val binaryOp_invar_inc642 = Module(new ComputeNode(NumOuts = 2, ID = 42, opCode = "add")(sign = false))

  //  %20 = icmp ugt i64 %invar.inc6, 63, !UID !52
  val icmp_43 = Module(new IcmpNode(NumOuts = 1, ID = 43, opCode = "ugt")(sign = false))

  //  br i1 %20, label %fusion.loop_body.dim.0.lr.ph, label %reduce.1.inner.loop_body.reduction_dim.1, !UID !53, !BB_UID !54
  val br_44 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 44))

  //  %21 = bitcast i8* %16 to float*, !UID !55
  val bitcast_45 = Module(new BitCastNode(NumOuts = 2, ID = 45))

  //  store float %19, float* %21, align 4, !UID !56
  val st_46 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 46, RouteID = 2))

  //  %fusion = bitcast i8* %0 to [64 x float]*, !UID !57
  val bitcast_fusion47 = Module(new BitCastNode(NumOuts = 1, ID = 47))

  //  br label %fusion.loop_body.dim.0, !UID !58, !BB_UID !59
  val br_48 = Module(new UBranchNode(ID = 48))

  //  %fusion.indvar.dim.02 = phi i64 [ 0, %fusion.loop_body.dim.0.lr.ph ], [ %invar.inc12, %fusion.loop_body.dim.0 ], !UID !60
  val phifusion_indvar_dim_0249 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 49, Res = true))

  //  %tmp8 = getelementptr [1 x [64 x float]], [1 x [64 x float]]* %fusion.1, i64 0, i64 0, !UID !61
  val Gep_tmp850 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 50)(ElementSize = 256, ArraySize = List(256)))

  //  %tmp9 = getelementptr [64 x float], [64 x float]* %tmp8, i64 0, i64 %fusion.indvar.dim.02, !UID !62
  val Gep_tmp951 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 51)(ElementSize = 4, ArraySize = List(256)))

  //  %22 = load float, float* %tmp9, align 4, !UID !63
  val ld_52 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 52, RouteID = 7))

  //  %23 = load float, float* %21, align 4, !UID !64
  val ld_53 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 53, RouteID = 8))

  //  %24 = fdiv float %22, %23, !UID !65
  //val FP_54 = Module(new FPDivSqrtNode(NumOuts = 1, ID = 54, RouteID = 0, opCode = "fdiv")(t = p(FTYP)))
  val FP_54 = Module(new FPCustomDividerNode(NumOuts = 1, ID = 54, opCode = "fdiv")(t = p(FTYP)))

  //  %25 = getelementptr inbounds [64 x float], [64 x float]* %fusion, i64 0, i64 %fusion.indvar.dim.02, !UID !66
  val Gep_55 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 55)(ElementSize = 4, ArraySize = List(256)))

  //  store float %24, float* %25, align 4, !UID !67
  val st_56 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 56, RouteID = 3))

  //  %invar.inc12 = add nuw nsw i64 %fusion.indvar.dim.02, 1, !UID !68
  val binaryOp_invar_inc1257 = Module(new ComputeNode(NumOuts = 2, ID = 57, opCode = "add")(sign = false))

  //  %26 = icmp ugt i64 %invar.inc12, 63, !UID !69
  val icmp_58 = Module(new IcmpNode(NumOuts = 1, ID = 58, opCode = "ugt")(sign = false))

  //  br i1 %26, label %fusion.loop_exit.dim.0, label %fusion.loop_body.dim.0, !UID !70, !BB_UID !71
  val br_59 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 59))

  //  %27 = bitcast i8* %retval to i8**, !UID !72
  val bitcast_60 = Module(new BitCastNode(NumOuts = 1, ID = 60))

  //  store i8* %0, i8** %27, align 8, !UID !73
  val st_61 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 61, RouteID = 4))

  //  ret void, !UID !74, !BB_UID !75
  val ret_62 = Module(new RetNode2(retTypes = List(), ID = 62))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i64 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i64 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i64 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i64 1
  val const4 = Module(new ConstFastNode(value = 1, ID = 4))

  //i64 63
  val const5 = Module(new ConstFastNode(value = 63, ID = 5))

  //i64 9
  val const6 = Module(new ConstFastNode(value = 9, ID = 6))

  //i64 0
  val const7 = Module(new ConstFastNode(value = 0, ID = 7))

  //i64 0
  val const8 = Module(new ConstFastNode(value = 0, ID = 8))

  //i64 0
  val const9 = Module(new ConstFastNode(value = 0, ID = 9))

  //i64 0
  val const10 = Module(new ConstFastNode(value = 0, ID = 10))

  //i64 0
  val const11 = Module(new ConstFastNode(value = 0, ID = 11))

  //i64 0
  val const12 = Module(new ConstFastNode(value = 0, ID = 12))

  //i64 0
  val const13 = Module(new ConstFastNode(value = 0, ID = 13))

  //i64 1
  val const14 = Module(new ConstFastNode(value = 1, ID = 14))

  //i64 63
  val const15 = Module(new ConstFastNode(value = 63, ID = 15))

  //i64 256
  val const16 = Module(new ConstFastNode(value = 256, ID = 16))

  //i64 0
  val const17 = Module(new ConstFastNode(value = 0, ID = 17))

  //i64 0
  val const18 = Module(new ConstFastNode(value = 0, ID = 18))

  //i64 0
  val const19 = Module(new ConstFastNode(value = 0, ID = 19))

  //i64 0
  val const20 = Module(new ConstFastNode(value = 0, ID = 20))

  //i64 1
  val const21 = Module(new ConstFastNode(value = 1, ID = 21))

  //i64 63
  val const22 = Module(new ConstFastNode(value = 63, ID = 22))

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

  //i64 63
  val const29 = Module(new ConstFastNode(value = 63, ID = 29))

  //float 0xFFF0000000000000
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))

  //float 0.000000e+00
  val constf1 = Module(new ConstNode(value = 0x0, ID = 1))

  //float 0.000000e+00
  val constf2 = Module(new ConstNode(value = 0x0, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_reduce_inner_loop_body_reduction_dim_11.io.predicateIn(1) <> Loop_3.io.activate_loop_start

  bb_reduce_inner_loop_body_reduction_dim_11.io.predicateIn(0) <> Loop_3.io.activate_loop_back

  bb_fusion_1_loop_body_dim_1_lr_ph2.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_fusion_1_loop_body_dim_13.io.predicateIn(1) <> Loop_2.io.activate_loop_start

  bb_fusion_1_loop_body_dim_13.io.predicateIn(0) <> Loop_2.io.activate_loop_back

  bb_reduce_1_inner_loop_body_reduction_dim_1_lr_ph4.io.predicateIn(0) <> Loop_2.io.loopExit(0)

  bb_reduce_1_inner_loop_body_reduction_dim_15.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_reduce_1_inner_loop_body_reduction_dim_15.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_fusion_loop_body_dim_0_lr_ph6.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_fusion_loop_body_dim_07.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_fusion_loop_body_dim_07.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_fusion_loop_exit_dim_08.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_48.io.Out(0)

  Loop_0.io.loopBack(0) <> br_59.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_59.io.TrueOutput(0)

  Loop_1.io.enable <> br_35.io.Out(0)

  Loop_1.io.loopBack(0) <> br_44.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_44.io.TrueOutput(0)

  Loop_2.io.enable <> br_21.io.Out(0)

  Loop_2.io.loopBack(0) <> br_33.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_33.io.TrueOutput(0)

  Loop_3.io.enable <> br_3.io.Out(0)

  Loop_3.io.loopBack(0) <> br_15.io.FalseOutput(0)

  Loop_3.io.loopFinish(0) <> br_15.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> bitcast_fusion_120.io.Out(0)

  Loop_0.io.InLiveIn(1) <> bitcast_45.io.Out(0)

  Loop_0.io.InLiveIn(2) <> bitcast_fusion47.io.Out(0)

  Loop_1.io.InLiveIn(0) <> bitcast_fusion_120.io.Out(1)

  Loop_2.io.InLiveIn(0) <> bitcast_bitcast1.io.Out(0)

  Loop_2.io.InLiveIn(1) <> bitcast_16.io.Out(0)

  Loop_2.io.InLiveIn(2) <> bitcast_fusion_120.io.Out(2)

  Loop_3.io.InLiveIn(0) <> bitcast_bitcast1.io.Out(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_tmp850.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field0")(0)

  ld_53.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(0)

  Gep_55.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_tmp638.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Gep_tmp223.io.baseAddress <> Loop_2.io.OutLiveIn.elements("field0")(0)

  ld_26.io.GepAddr <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Gep_tmp428.io.baseAddress <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Gep_tmp6.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_1.io.InLiveOut(0) <> FP_41.io.Out(0)

  Loop_3.io.InLiveOut(0) <> select_12.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  st_46.io.inData <> Loop_1.io.OutLiveOut.elements("field0")(0)

  st_17.io.inData <> Loop_3.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_invar_inc1257.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> FP_41.io.Out(1)

  Loop_1.io.CarryDepenIn(1) <> binaryOp_invar_inc642.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_invar_inc331.io.Out(0)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_invar_inc113.io.Out(0)

  Loop_3.io.CarryDepenIn(1) <> select_12.io.Out(1)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phifusion_indvar_dim_0249.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi36.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phireduce_1_inner_indvar_reduction_dim_1437.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field1")(0)

  phifusion_1_indvar_dim_1822.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phireduce_inner_indvar_reduction_dim_1125.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phi4.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field1")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  ld_0.io.enable <> bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0.io.Out(0)


  bitcast_bitcast1.io.enable <> bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0.io.Out(1)


  ld_2.io.enable <> bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0.io.Out(2)


  br_3.io.enable <> bb_reduce_inner_loop_body_reduction_dim_1_lr_ph0.io.Out(3)


  constf0.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(0)

  constf1.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(5)

  const0.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(1)

  const1.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(2)

  const2.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(3)

  const3.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(4)

  const4.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(6)

  const5.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(7)

  phi4.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(8)


  phireduce_inner_indvar_reduction_dim_1125.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(9)


  Gep_tmp6.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(10)


  Gep_tmp17.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(11)


  ld_8.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(12)


  FPCMP_9.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(13)


  FPCMP_10.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(14)


  binaryOp_11.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(15)


  select_12.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(16)


  binaryOp_invar_inc113.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(17)


  icmp_14.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(18)


  br_15.io.enable <> bb_reduce_inner_loop_body_reduction_dim_11.io.Out(19)


  const6.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(0)

  bitcast_16.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(1)


  st_17.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(2)


  Gep_18.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(3)


  ld_19.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(4)


  bitcast_fusion_120.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(5)


  br_21.io.enable <> bb_fusion_1_loop_body_dim_1_lr_ph2.io.Out(6)


  const7.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(0)

  const8.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(1)

  const9.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(2)

  const10.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(3)

  const11.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(4)

  const12.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(5)

  const13.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(6)

  const14.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(7)

  const15.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(8)

  phifusion_1_indvar_dim_1822.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(9)


  Gep_tmp223.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(10)


  Gep_tmp324.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(11)


  ld_25.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(12)


  ld_26.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(13)


  FP_27.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(14)


  Gep_tmp428.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(15)


  Gep_tmp529.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(16)


  st_30.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(17)


  binaryOp_invar_inc331.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(18)


  icmp_32.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(19)


  br_33.io.enable <> bb_fusion_1_loop_body_dim_13.io.Out(20)


  const16.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_1_lr_ph4.io.Out(0)

  Gep_34.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_1_lr_ph4.io.Out(1)


  br_35.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_1_lr_ph4.io.Out(2)


  constf2.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(0)

  const17.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(1)

  const18.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(2)

  const19.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(3)

  const20.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(4)

  const21.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(5)

  const22.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(6)

  phi36.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(7)


  phireduce_1_inner_indvar_reduction_dim_1437.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(8)


  Gep_tmp638.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(9)


  Gep_tmp739.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(10)


  ld_40.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(11)


  FP_41.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(12)


  binaryOp_invar_inc642.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(13)


  icmp_43.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(14)


  br_44.io.enable <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.Out(15)


  bitcast_45.io.enable <> bb_fusion_loop_body_dim_0_lr_ph6.io.Out(0)


  st_46.io.enable <> bb_fusion_loop_body_dim_0_lr_ph6.io.Out(1)


  bitcast_fusion47.io.enable <> bb_fusion_loop_body_dim_0_lr_ph6.io.Out(2)


  br_48.io.enable <> bb_fusion_loop_body_dim_0_lr_ph6.io.Out(3)


  const23.io.enable <> bb_fusion_loop_body_dim_07.io.Out(0)

  const24.io.enable <> bb_fusion_loop_body_dim_07.io.Out(1)

  const25.io.enable <> bb_fusion_loop_body_dim_07.io.Out(2)

  const26.io.enable <> bb_fusion_loop_body_dim_07.io.Out(3)

  const27.io.enable <> bb_fusion_loop_body_dim_07.io.Out(4)

  const28.io.enable <> bb_fusion_loop_body_dim_07.io.Out(5)

  const29.io.enable <> bb_fusion_loop_body_dim_07.io.Out(6)

  phifusion_indvar_dim_0249.io.enable <> bb_fusion_loop_body_dim_07.io.Out(7)


  Gep_tmp850.io.enable <> bb_fusion_loop_body_dim_07.io.Out(8)


  Gep_tmp951.io.enable <> bb_fusion_loop_body_dim_07.io.Out(9)


  ld_52.io.enable <> bb_fusion_loop_body_dim_07.io.Out(10)


  ld_53.io.enable <> bb_fusion_loop_body_dim_07.io.Out(11)


  FP_54.io.enable <> bb_fusion_loop_body_dim_07.io.Out(12)


  Gep_55.io.enable <> bb_fusion_loop_body_dim_07.io.Out(13)


  st_56.io.enable <> bb_fusion_loop_body_dim_07.io.Out(14)


  binaryOp_invar_inc1257.io.enable <> bb_fusion_loop_body_dim_07.io.Out(15)


  icmp_58.io.enable <> bb_fusion_loop_body_dim_07.io.Out(16)


  br_59.io.enable <> bb_fusion_loop_body_dim_07.io.Out(17)


  bitcast_60.io.enable <> bb_fusion_loop_exit_dim_08.io.Out(0)


  st_61.io.enable <> bb_fusion_loop_exit_dim_08.io.Out(1)


  ret_62.io.In.enable <> bb_fusion_loop_exit_dim_08.io.Out(2)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi4.io.Mask <> bb_reduce_inner_loop_body_reduction_dim_11.io.MaskBB(0)

  phireduce_inner_indvar_reduction_dim_1125.io.Mask <> bb_reduce_inner_loop_body_reduction_dim_11.io.MaskBB(1)

  phifusion_1_indvar_dim_1822.io.Mask <> bb_fusion_1_loop_body_dim_13.io.MaskBB(0)

  phi36.io.Mask <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.MaskBB(0)

  phireduce_1_inner_indvar_reduction_dim_1437.io.Mask <> bb_reduce_1_inner_loop_body_reduction_dim_15.io.MaskBB(1)

  phifusion_indvar_dim_0249.io.Mask <> bb_fusion_loop_body_dim_07.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_0.io.memReq

  ld_0.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_2.io.memReq

  ld_2.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_8.io.memReq

  ld_8.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(0) <> st_17.io.memReq

  st_17.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(3) <> ld_19.io.memReq

  ld_19.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_25.io.memReq

  ld_25.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_26.io.memReq

  ld_26.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.WriteIn(1) <> st_30.io.memReq

  st_30.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.ReadIn(6) <> ld_40.io.memReq

  ld_40.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.WriteIn(2) <> st_46.io.memReq

  st_46.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.ReadIn(7) <> ld_52.io.memReq

  ld_52.io.memResp <> MemCtrl.io.ReadOut(7)

  MemCtrl.io.ReadIn(8) <> ld_53.io.memReq

  ld_53.io.memResp <> MemCtrl.io.ReadOut(8)

  MemCtrl.io.WriteIn(3) <> st_56.io.memReq

  st_56.io.memResp <> MemCtrl.io.WriteOut(3)

  MemCtrl.io.WriteIn(4) <> st_61.io.memReq

  st_61.io.memResp <> MemCtrl.io.WriteOut(4)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */

  //SharedFPU.io.InData(0) <> FP_54.io.FUReq
  //FP_54.io.FUResp <> SharedFPU.io.OutData(0)



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phireduce_inner_indvar_reduction_dim_1125.io.InData(0) <> const0.io.Out

  Gep_tmp6.io.idx(0) <> const1.io.Out

  Gep_tmp6.io.idx(1) <> const2.io.Out

  Gep_tmp17.io.idx(0) <> const3.io.Out

  binaryOp_invar_inc113.io.RightIO <> const4.io.Out

  icmp_14.io.RightIO <> const5.io.Out

  Gep_18.io.idx(0) <> const6.io.Out

  phifusion_1_indvar_dim_1822.io.InData(0) <> const7.io.Out

  Gep_tmp223.io.idx(0) <> const8.io.Out

  Gep_tmp223.io.idx(1) <> const9.io.Out

  Gep_tmp324.io.idx(0) <> const10.io.Out

  Gep_tmp428.io.idx(0) <> const11.io.Out

  Gep_tmp428.io.idx(1) <> const12.io.Out

  Gep_tmp529.io.idx(0) <> const13.io.Out

  binaryOp_invar_inc331.io.RightIO <> const14.io.Out

  icmp_32.io.RightIO <> const15.io.Out

  Gep_34.io.idx(0) <> const16.io.Out

  phireduce_1_inner_indvar_reduction_dim_1437.io.InData(0) <> const17.io.Out

  Gep_tmp638.io.idx(0) <> const18.io.Out

  Gep_tmp638.io.idx(1) <> const19.io.Out

  Gep_tmp739.io.idx(0) <> const20.io.Out

  binaryOp_invar_inc642.io.RightIO <> const21.io.Out

  icmp_43.io.RightIO <> const22.io.Out

  phifusion_indvar_dim_0249.io.InData(0) <> const23.io.Out

  Gep_tmp850.io.idx(0) <> const24.io.Out

  Gep_tmp850.io.idx(1) <> const25.io.Out

  Gep_tmp951.io.idx(0) <> const26.io.Out

  Gep_55.io.idx(0) <> const27.io.Out

  binaryOp_invar_inc1257.io.RightIO <> const28.io.Out

  icmp_58.io.RightIO <> const29.io.Out

  phi4.io.InData(0) <> constf0.io.Out(0)

  FPCMP_10.io.RightIO <> constf1.io.Out(0)

  phi36.io.InData(0) <> constf2.io.Out(0)

  bitcast_bitcast1.io.Input <> ld_0.io.Out(0)

  bitcast_16.io.Input <> ld_2.io.Out(0)

  bitcast_fusion47.io.Input <> ld_2.io.Out(1)

  st_61.io.inData <> ld_2.io.Out(2)

  FPCMP_9.io.LeftIO <> phi4.io.Out(0)

  FPCMP_10.io.LeftIO <> phi4.io.Out(1)

  select_12.io.InData1 <> phi4.io.Out(2)

  Gep_tmp17.io.idx(1) <> phireduce_inner_indvar_reduction_dim_1125.io.Out(0)

  binaryOp_invar_inc113.io.LeftIO <> phireduce_inner_indvar_reduction_dim_1125.io.Out(1)

  Gep_tmp17.io.baseAddress <> Gep_tmp6.io.Out(0)

  ld_8.io.GepAddr <> Gep_tmp17.io.Out(0)

  FPCMP_9.io.RightIO <> ld_8.io.Out(0)

  select_12.io.InData2 <> ld_8.io.Out(1)

  binaryOp_11.io.LeftIO <> FPCMP_9.io.Out(0)

  binaryOp_11.io.RightIO <> FPCMP_10.io.Out(0)

  select_12.io.Select <> binaryOp_11.io.Out(0)

  icmp_14.io.LeftIO <> binaryOp_invar_inc113.io.Out(1)

  br_15.io.CmpIO <> icmp_14.io.Out(0)

  st_17.io.GepAddr <> bitcast_16.io.Out(1)

  ld_19.io.GepAddr <> Gep_18.io.Out(0)

  bitcast_fusion_120.io.Input <> ld_19.io.Out(0)

  Gep_34.io.baseAddress <> ld_19.io.Out(1)

  Gep_tmp324.io.idx(1) <> phifusion_1_indvar_dim_1822.io.Out(0)

  Gep_tmp529.io.idx(1) <> phifusion_1_indvar_dim_1822.io.Out(1)

  binaryOp_invar_inc331.io.LeftIO <> phifusion_1_indvar_dim_1822.io.Out(2)

  Gep_tmp324.io.baseAddress <> Gep_tmp223.io.Out(0)

  ld_25.io.GepAddr <> Gep_tmp324.io.Out(0)

  FP_27.io.LeftIO <> ld_25.io.Out(0)

  FP_27.io.RightIO <> ld_26.io.Out(0)

  Gep_tmp529.io.baseAddress <> Gep_tmp428.io.Out(0)

  st_30.io.GepAddr <> Gep_tmp529.io.Out(0)

  st_30.io.inData <> FP_27.io.Out(0)

  icmp_32.io.LeftIO <> binaryOp_invar_inc331.io.Out(1)

  br_33.io.CmpIO <> icmp_32.io.Out(0)

  bitcast_45.io.Input <> Gep_34.io.Out(0)

  FP_41.io.LeftIO <> phi36.io.Out(0)

  Gep_tmp739.io.idx(1) <> phireduce_1_inner_indvar_reduction_dim_1437.io.Out(0)

  binaryOp_invar_inc642.io.LeftIO <> phireduce_1_inner_indvar_reduction_dim_1437.io.Out(1)

  Gep_tmp739.io.baseAddress <> Gep_tmp638.io.Out(0)

  ld_40.io.GepAddr <> Gep_tmp739.io.Out(0)

  FP_41.io.RightIO <> ld_40.io.Out(0)

  icmp_43.io.LeftIO <> binaryOp_invar_inc642.io.Out(1)

  br_44.io.CmpIO <> icmp_43.io.Out(0)

  st_46.io.GepAddr <> bitcast_45.io.Out(1)

  Gep_tmp951.io.idx(1) <> phifusion_indvar_dim_0249.io.Out(0)

  Gep_55.io.idx(1) <> phifusion_indvar_dim_0249.io.Out(1)

  binaryOp_invar_inc1257.io.LeftIO <> phifusion_indvar_dim_0249.io.Out(2)

  Gep_tmp951.io.baseAddress <> Gep_tmp850.io.Out(0)

  ld_52.io.GepAddr <> Gep_tmp951.io.Out(0)

  FP_54.io.LeftIO <> ld_52.io.Out(0)

  FP_54.io.RightIO <> ld_53.io.Out(0)

  st_56.io.inData <> FP_54.io.Out(0)

  st_56.io.GepAddr <> Gep_55.io.Out(0)

  icmp_58.io.LeftIO <> binaryOp_invar_inc1257.io.Out(1)

  br_59.io.CmpIO <> icmp_58.io.Out(0)

  st_61.io.GepAddr <> bitcast_60.io.Out(0)

  bitcast_60.io.Input <> InputSplitter.io.Out.data.elements("field0")(0)

  ld_0.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(0)

  ld_2.io.GepAddr <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_18.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  st_17.io.Out(0).ready := true.B

  st_30.io.Out(0).ready := true.B

  st_46.io.Out(0).ready := true.B

  st_56.io.Out(0).ready := true.B

  st_61.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_62.io.Out

}

import java.io.{File, FileWriter}

abstract class softmax07bTopIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val out = Decoupled(new Call(List()))
  })
}


class softmax07bMain(implicit p: Parameters) extends softmax07bTopIO {

  // Wire up the cache and modules under test.
  val test = Module(new test_07_softmax_b_ir_4DF())
  val Stack = Module(new StackMem((1 << tlen) * 4))

  //Put an arbiter infront of cache

  // Connect input signals to cache
  Stack.io.req <> test.io.MemReq
  test.io.MemResp <> Stack.io.resp

  //Connect in/out ports
  test.io.in <> io.in
  io.out <> test.io.out

}


object test_07_softmax_b_ir_4Top extends App {
  val dir = new File("RTL/test_07_softmax_b_ir_4Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new softmax07bMain()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
