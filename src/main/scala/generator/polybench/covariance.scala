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

abstract class covarianceDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class covarianceDF(implicit p: Parameters) extends covarianceDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 9, NWrites = 8)
  (WControl = new WriteMemoryController(NumOps = 8, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 9, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val SharedFPU = Module(new SharedFPU(NumOps = 2, PipeDepth = 32)(t = p(FTYP)))

  val InputSplitter = Module(new SplitCallNew(List(2, 3, 1, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 2, 2, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(4, 1, 2, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 4))

  val Loop_4 = Module(new LoopBlockNode(NumIns = List(1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 5))

  val Loop_5 = Module(new LoopBlockNode(NumIns = List(1, 2, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 6))

  val Loop_6 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 7))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 6, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 14, NumPhi = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 8, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))

  val bb_5 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 5))

  val bb_6 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 15, NumPhi = 1, BID = 6))

  val bb_7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 7))

  val bb_8 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 8))

  val bb_9 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 9))

  val bb_10 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 1, BID = 10))

  val bb_11 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 19, NumPhi = 1, BID = 11))

  val bb_12 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 12, BID = 12))

  val bb_13 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 13))

  val bb_14 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 14))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %5, !dbg !57, !UID !59, !BB_UID !60
  val br_0 = Module(new UBranchFastNode(ID = 0))

  //  %6 = phi i64 [ 0, %4 ], [ %18, %15 ], !UID !61
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 1, Res = true))

  //  %7 = getelementptr inbounds double, double* %3, i64 %6, !dbg !62, !UID !65
  val Gep_2 = Module(new GepNode(NumIns = 1, NumOuts = 4, ID = 2)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %7, align 8, !dbg !66, !tbaa !67, !UID !71
  val st_3 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 3, RouteID = 0))

  //  br label %8, !dbg !73, !UID !75, !BB_UID !76
  val br_4 = Module(new UBranchFastNode(ID = 4))

  //  %9 = phi i64 [ 0, %5 ], [ %13, %8 ], !UID !77
  val phi5 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 5, Res = true))

  //  %tmp = getelementptr [28 x double], [28 x double]* %1, i64 %9, !UID !78
  val Gep_tmp6 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 6)(ElementSize = 8, ArraySize = List()))

  //  %tmp1 = getelementptr [28 x double], [28 x double]* %tmp, i64 0, i64 %6, !UID !79
  val Gep_tmp17 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 7)(ElementSize = 8, ArraySize = List()))

  //  %10 = load double, double* %tmp1, align 8, !dbg !80, !tbaa !67, !UID !82
  val ld_8 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 8, RouteID = 0))

  //  %11 = load double, double* %7, align 8, !dbg !83, !tbaa !67, !UID !84
  val ld_9 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 9, RouteID = 1))

  //  %12 = fadd double %10, %11, !dbg !83, !UID !85
  val FP_10 = Module(new FPComputeNode(NumOuts = 1, ID = 10, opCode = "fadd")(t = p(FTYP)))

  //  store double %12, double* %7, align 8, !dbg !83, !tbaa !67, !UID !86
  val st_11 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 11, RouteID = 1))

  //  %13 = add nuw nsw i64 %9, 1, !dbg !87, !UID !88
  val binaryOp_12 = Module(new ComputeFastNode(NumOuts = 2, ID = 12, opCode = "add")(sign = false))

  //  %14 = icmp eq i64 %13, 32, !dbg !89, !UID !90
  val icmp_13 = Module(new IcmpNode(NumOuts = 1, ID = 13, opCode = "eq")(sign = false))

  //  br i1 %14, label %15, label %8, !dbg !73, !llvm.loop !91, !UID !93, !BB_UID !94
  val br_14 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 14))

  //  %16 = load double, double* %7, align 8, !dbg !95, !tbaa !67, !UID !96
  val ld_15 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 15, RouteID = 2))

  //  %17 = fdiv double %16, %0, !dbg !95, !UID !97
  val FP_16 = Module(new FPDivSqrtNode(NumOuts = 1, ID = 16, RouteID = 0, opCode = "fdiv")(t = p(FTYP)))

  //  store double %17, double* %7, align 8, !dbg !95, !tbaa !67, !UID !98
  val st_17 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 17, RouteID = 2))

  //  %18 = add nuw nsw i64 %6, 1, !dbg !99, !UID !100
  val binaryOp_18 = Module(new ComputeFastNode(NumOuts = 2, ID = 18, opCode = "add")(sign = false))

  //  %19 = icmp eq i64 %18, 28, !dbg !101, !UID !102
  val icmp_19 = Module(new IcmpFastNode(NumOuts = 1, ID = 19, opCode = "eq")(sign = false))

  //  br i1 %19, label %20, label %5, !dbg !57, !llvm.loop !103, !UID !105, !BB_UID !106
  val br_20 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 20))

  //  br label %21, !dbg !107, !UID !111, !BB_UID !112
  val br_21 = Module(new UBranchNode(ID = 21))

  //  %22 = phi i64 [ %32, %31 ], [ 0, %20 ], !UID !113
  val phi22 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 22, Res = true))

  //  br label %23, !dbg !107, !UID !114, !BB_UID !115
  val br_23 = Module(new UBranchFastNode(ID = 23))

  //  %24 = phi i64 [ 0, %21 ], [ %29, %23 ], !UID !116
  val phi24 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 24, Res = true))

  //  %25 = getelementptr inbounds double, double* %3, i64 %24, !dbg !117, !UID !119
  val Gep_25 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 25)(ElementSize = 8, ArraySize = List()))

  //  %26 = load double, double* %25, align 8, !dbg !117, !tbaa !67, !UID !120
  val ld_26 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 26, RouteID = 3))

  //  %tmp2 = getelementptr [28 x double], [28 x double]* %1, i64 %22, !UID !121
  val Gep_tmp227 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 27)(ElementSize = 8, ArraySize = List()))

  //  %tmp3 = getelementptr [28 x double], [28 x double]* %tmp2, i64 0, i64 %24, !UID !122
  val Gep_tmp328 = Module(new GepNode(NumIns = 2, NumOuts = 2, ID = 28)(ElementSize = 8, ArraySize = List()))

  //  %27 = load double, double* %tmp3, align 8, !dbg !123, !tbaa !67, !UID !124
  val ld_29 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 29, RouteID = 4))

  //  %28 = fsub double %27, %26, !dbg !123, !UID !125
  val FP_30 = Module(new FPComputeNode(NumOuts = 1, ID = 30, opCode = "fsub")(t = p(FTYP)))

  //  store double %28, double* %tmp3, align 8, !dbg !123, !tbaa !67, !UID !126
  val st_31 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 31, RouteID = 3))

  //  %29 = add nuw nsw i64 %24, 1, !dbg !127, !UID !128
  val binaryOp_32 = Module(new ComputeFastNode(NumOuts = 2, ID = 32, opCode = "add")(sign = false))

  //  %30 = icmp eq i64 %29, 28, !dbg !129, !UID !130
  val icmp_33 = Module(new IcmpFastNode(NumOuts = 1, ID = 33, opCode = "eq")(sign = false))

  //  br i1 %30, label %31, label %23, !dbg !107, !llvm.loop !131, !UID !133, !BB_UID !134
  val br_34 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 34))

  //  %32 = add nuw nsw i64 %22, 1, !dbg !135, !UID !136
  val binaryOp_35 = Module(new ComputeFastNode(NumOuts = 2, ID = 35, opCode = "add")(sign = false))

  //  %33 = icmp eq i64 %32, 32, !dbg !137, !UID !138
  val icmp_36 = Module(new IcmpFastNode(NumOuts = 1, ID = 36, opCode = "eq")(sign = false))

  //  br i1 %33, label %34, label %21, !dbg !139, !llvm.loop !140, !UID !142, !BB_UID !143
  val br_37 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 37))

  //  %35 = fadd double %0, -1.000000e+00, !UID !144
  val FP_38 = Module(new FPComputeNode(NumOuts = 1, ID = 38, opCode = "fadd")(t = p(FTYP)))

  //  br label %36, !dbg !145, !UID !147, !BB_UID !148
  val br_39 = Module(new UBranchFastNode(ID = 39))

  //  %37 = phi i64 [ 0, %34 ], [ %55, %54 ], !UID !149
  val phi40 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 40, Res = false))

  //  br label %38, !dbg !150, !UID !153, !BB_UID !154
  val br_41 = Module(new UBranchFastNode(ID = 41))

  //  %39 = phi i64 [ %37, %36 ], [ %52, %49 ], !UID !155
  val phi42 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 42, Res = true))

  //  %tmp4 = getelementptr [28 x double], [28 x double]* %2, i64 %37, !UID !156
  val Gep_tmp443 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 43)(ElementSize = 8, ArraySize = List()))

  //  %tmp5 = getelementptr [28 x double], [28 x double]* %tmp4, i64 0, i64 %39, !UID !157
  val Gep_tmp544 = Module(new GepNode(NumIns = 2, NumOuts = 4, ID = 44)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %tmp5, align 8, !dbg !158, !tbaa !67, !UID !161
  val st_45 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 45, RouteID = 4))

  //  br label %40, !dbg !163, !UID !165, !BB_UID !166
  val br_46 = Module(new UBranchFastNode(ID = 46))

  //  %41 = phi i64 [ 0, %38 ], [ %47, %40 ], !UID !167
  val phi47 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 47, Res = true))

  //  %tmp6 = getelementptr [28 x double], [28 x double]* %1, i64 %41, !UID !168
  val Gep_tmp648 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 48)(ElementSize = 8, ArraySize = List()))

  //  %tmp7 = getelementptr [28 x double], [28 x double]* %tmp6, i64 0, i64 %37, !UID !169
  val Gep_tmp749 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 49)(ElementSize = 8, ArraySize = List()))

  //  %42 = load double, double* %tmp7, align 8, !dbg !170, !tbaa !67, !UID !172
  val ld_50 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 50, RouteID = 5))

  //  %tmp8 = getelementptr [28 x double], [28 x double]* %1, i64 %41, !UID !173
  val Gep_tmp851 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 51)(ElementSize = 8, ArraySize = List()))

  //  %tmp9 = getelementptr [28 x double], [28 x double]* %tmp8, i64 0, i64 %39, !UID !174
  val Gep_tmp952 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 52)(ElementSize = 8, ArraySize = List()))

  //  %43 = load double, double* %tmp9, align 8, !dbg !175, !tbaa !67, !UID !176
  val ld_53 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 53, RouteID = 6))

  //  %44 = fmul double %42, %43, !dbg !177, !UID !178
  val FP_54 = Module(new FPComputeNode(NumOuts = 1, ID = 54, opCode = "fmul")(t = p(FTYP)))

  //  %45 = load double, double* %tmp5, align 8, !dbg !179, !tbaa !67, !UID !180
  val ld_55 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 55, RouteID = 7))

  //  %46 = fadd double %45, %44, !dbg !179, !UID !181
  val FP_56 = Module(new FPComputeNode(NumOuts = 1, ID = 56, opCode = "fadd")(t = p(FTYP)))

  //  store double %46, double* %tmp5, align 8, !dbg !179, !tbaa !67, !UID !182
  val st_57 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 57, RouteID = 5))

  //  %47 = add nuw nsw i64 %41, 1, !dbg !183, !UID !184
  val binaryOp_58 = Module(new ComputeFastNode(NumOuts = 2, ID = 58, opCode = "add")(sign = false))

  //  %48 = icmp eq i64 %47, 32, !dbg !185, !UID !186
  val icmp_59 = Module(new IcmpNode(NumOuts = 1, ID = 59, opCode = "eq")(sign = false))

  //  br i1 %48, label %49, label %40, !dbg !163, !llvm.loop !187, !UID !189, !BB_UID !190
  val br_60 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 60))

  //  %50 = load double, double* %tmp5, align 8, !dbg !191, !tbaa !67, !UID !192
  val ld_61 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 61, RouteID = 8))

  //  %51 = fdiv double %50, %35, !dbg !191, !UID !193
  val FP_62 = Module(new FPDivSqrtNode(NumOuts = 2, ID = 62, RouteID = 1, opCode = "fdiv")(t = p(FTYP)))

  //  store double %51, double* %tmp5, align 8, !dbg !191, !tbaa !67, !UID !194
  val st_63 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 63, RouteID = 6))

  //  %tmp10 = getelementptr [28 x double], [28 x double]* %2, i64 %39, !UID !195
  val Gep_tmp1064 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 64)(ElementSize = 8, ArraySize = List()))

  //  %tmp11 = getelementptr [28 x double], [28 x double]* %tmp10, i64 0, i64 %37, !UID !196
  val Gep_tmp1165 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 65)(ElementSize = 8, ArraySize = List()))

  //  store double %51, double* %tmp11, align 8, !dbg !197, !tbaa !67, !UID !198
  val st_66 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 66, RouteID = 7))

  //  %52 = add nuw nsw i64 %39, 1, !dbg !199, !UID !200
  val binaryOp_67 = Module(new ComputeFastNode(NumOuts = 2, ID = 67, opCode = "add")(sign = false))

  //  %53 = icmp eq i64 %52, 28, !dbg !201, !UID !202
  val icmp_68 = Module(new IcmpFastNode(NumOuts = 1, ID = 68, opCode = "eq")(sign = false))

  //  br i1 %53, label %54, label %38, !dbg !150, !llvm.loop !203, !UID !205, !BB_UID !206
  val br_69 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 69))

  //  %55 = add nuw nsw i64 %37, 1, !dbg !207, !UID !208
  val binaryOp_70 = Module(new ComputeFastNode(NumOuts = 2, ID = 70, opCode = "add")(sign = false))

  //  %56 = icmp eq i64 %55, 28, !dbg !209, !UID !210
  val icmp_71 = Module(new IcmpFastNode(NumOuts = 1, ID = 71, opCode = "eq")(sign = false))

  //  br i1 %56, label %57, label %36, !dbg !145, !llvm.loop !211, !UID !213, !BB_UID !214
  val br_72 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 72))

  //  ret void, !dbg !215, !UID !216, !BB_UID !217
  val ret_73 = Module(new RetNode2(retTypes = List(), ID = 73))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i64 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i64 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i64 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))

  //i64 32
  val const4 = Module(new ConstFastNode(value = 32, ID = 4))

  //i64 1
  val const5 = Module(new ConstFastNode(value = 1, ID = 5))

  //i64 28
  val const6 = Module(new ConstFastNode(value = 28, ID = 6))

  //i64 0
  val const7 = Module(new ConstFastNode(value = 0, ID = 7))

  //i64 0
  val const8 = Module(new ConstFastNode(value = 0, ID = 8))

  //i64 0
  val const9 = Module(new ConstFastNode(value = 0, ID = 9))

  //i64 1
  val const10 = Module(new ConstFastNode(value = 1, ID = 10))

  //i64 28
  val const11 = Module(new ConstFastNode(value = 28, ID = 11))

  //i64 1
  val const12 = Module(new ConstFastNode(value = 1, ID = 12))

  //i64 32
  val const13 = Module(new ConstFastNode(value = 32, ID = 13))

  //i64 0
  val const14 = Module(new ConstFastNode(value = 0, ID = 14))

  //i64 0
  val const15 = Module(new ConstFastNode(value = 0, ID = 15))

  //i64 0
  val const16 = Module(new ConstFastNode(value = 0, ID = 16))

  //i64 0
  val const17 = Module(new ConstFastNode(value = 0, ID = 17))

  //i64 0
  val const18 = Module(new ConstFastNode(value = 0, ID = 18))

  //i64 1
  val const19 = Module(new ConstFastNode(value = 1, ID = 19))

  //i64 32
  val const20 = Module(new ConstFastNode(value = 32, ID = 20))

  //i64 0
  val const21 = Module(new ConstFastNode(value = 0, ID = 21))

  //i64 1
  val const22 = Module(new ConstFastNode(value = 1, ID = 22))

  //i64 28
  val const23 = Module(new ConstFastNode(value = 28, ID = 23))

  //i64 1
  val const24 = Module(new ConstFastNode(value = 1, ID = 24))

  //i64 28
  val const25 = Module(new ConstFastNode(value = 28, ID = 25))

  //double 0.000000e+00
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))

  //double -1.000000e+00
  val constf1 = Module(new ConstNode(value = 0x0, ID = 1))

  //double 0.000000e+00
  val constf2 = Module(new ConstNode(value = 0x0, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_1.io.predicateIn(1) <> Loop_6.io.activate_loop_start

  bb_1.io.predicateIn(0) <> Loop_6.io.activate_loop_back

  bb_2.io.predicateIn(1) <> Loop_5.io.activate_loop_start

  bb_2.io.predicateIn(0) <> Loop_5.io.activate_loop_back

  bb_3.io.predicateIn(0) <> Loop_5.io.loopExit(0)

  bb_4.io.predicateIn(0) <> Loop_6.io.loopExit(0)

  bb_5.io.predicateIn(0) <> Loop_4.io.activate_loop_start

  bb_5.io.predicateIn(1) <> Loop_4.io.activate_loop_back

  bb_6.io.predicateIn(1) <> Loop_3.io.activate_loop_start

  bb_6.io.predicateIn(0) <> Loop_3.io.activate_loop_back

  bb_7.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_8.io.predicateIn(0) <> Loop_4.io.loopExit(0)

  bb_9.io.predicateIn(0) <> Loop_2.io.activate_loop_start

  bb_9.io.predicateIn(1) <> Loop_2.io.activate_loop_back

  bb_10.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_10.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_11.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_11.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_12.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_13.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_14.io.predicateIn(0) <> Loop_2.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_46.io.Out(0)

  Loop_0.io.loopBack(0) <> br_60.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_60.io.TrueOutput(0)

  Loop_1.io.enable <> br_41.io.Out(0)

  Loop_1.io.loopBack(0) <> br_69.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_69.io.TrueOutput(0)

  Loop_2.io.enable <> br_39.io.Out(0)

  Loop_2.io.loopBack(0) <> br_72.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_72.io.TrueOutput(0)

  Loop_3.io.enable <> br_23.io.Out(0)

  Loop_3.io.loopBack(0) <> br_34.io.FalseOutput(0)

  Loop_3.io.loopFinish(0) <> br_34.io.TrueOutput(0)

  Loop_4.io.enable <> br_21.io.Out(0)

  Loop_4.io.loopBack(0) <> br_37.io.FalseOutput(0)

  Loop_4.io.loopFinish(0) <> br_37.io.TrueOutput(0)

  Loop_5.io.enable <> br_4.io.Out(0)

  Loop_5.io.loopBack(0) <> br_14.io.FalseOutput(0)

  Loop_5.io.loopFinish(0) <> br_14.io.TrueOutput(0)

  Loop_6.io.enable <> br_0.io.Out(0)

  Loop_6.io.loopBack(0) <> br_20.io.FalseOutput(0)

  Loop_6.io.loopFinish(0) <> br_20.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> phi42.io.Out(0)

  Loop_0.io.InLiveIn(1) <> Gep_tmp544.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(0) <> phi40.io.Out(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_2.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_2.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_2.io.InLiveIn(2) <> FP_38.io.Out(0)

  Loop_3.io.InLiveIn(0) <> phi22.io.Out(0)

  Loop_3.io.InLiveIn(1) <> Loop_4.io.OutLiveIn.elements("field0")(0)

  Loop_3.io.InLiveIn(2) <> Loop_4.io.OutLiveIn.elements("field1")(0)

  Loop_4.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_4.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(1)

  Loop_5.io.InLiveIn(0) <> phi1.io.Out(0)

  Loop_5.io.InLiveIn(1) <> Gep_2.io.Out(0)

  Loop_5.io.InLiveIn(2) <> Loop_6.io.OutLiveIn.elements("field1")(0)

  Loop_6.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field3")(1)

  Loop_6.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(2)

  Loop_6.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_tmp952.io.idx(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  ld_55.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(0)

  st_57.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(1)

  Gep_tmp648.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_tmp851.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(1)

  Gep_tmp749.io.idx(1) <> Loop_0.io.OutLiveIn.elements("field3")(0)

  phi42.io.InData(0) <> Loop_1.io.OutLiveIn.elements("field0")(1)

  Gep_tmp443.io.idx(0) <> Loop_1.io.OutLiveIn.elements("field0")(2)

  Gep_tmp1165.io.idx(1) <> Loop_1.io.OutLiveIn.elements("field0")(3)

  Gep_tmp443.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Gep_tmp1064.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field2")(1)

  FP_62.io.b <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Gep_tmp227.io.idx(0) <> Loop_3.io.OutLiveIn.elements("field0")(0)

  Gep_25.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field1")(0)

  Gep_tmp227.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field2")(0)

  Gep_tmp17.io.idx(1) <> Loop_5.io.OutLiveIn.elements("field0")(0)

  ld_9.io.GepAddr <> Loop_5.io.OutLiveIn.elements("field1")(0)

  st_11.io.GepAddr <> Loop_5.io.OutLiveIn.elements("field1")(1)

  Gep_tmp6.io.baseAddress <> Loop_5.io.OutLiveIn.elements("field2")(0)

  Gep_2.io.baseAddress <> Loop_6.io.OutLiveIn.elements("field0")(0)

  FP_16.io.b <> Loop_6.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_58.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_67.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_70.io.Out(0)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_32.io.Out(0)

  Loop_4.io.CarryDepenIn(0) <> binaryOp_35.io.Out(0)

  Loop_5.io.CarryDepenIn(0) <> binaryOp_12.io.Out(0)

  Loop_6.io.CarryDepenIn(0) <> binaryOp_18.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi47.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi42.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phi40.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phi24.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phi22.io.InData(0) <> Loop_4.io.CarryDepenOut.elements("field0")(0)

  phi5.io.InData(1) <> Loop_5.io.CarryDepenOut.elements("field0")(0)

  phi1.io.InData(1) <> Loop_6.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  constf0.io.enable <> bb_1.io.Out(1)

  const0.io.enable <> bb_1.io.Out(0)

  phi1.io.enable <> bb_1.io.Out(2)


  Gep_2.io.enable <> bb_1.io.Out(3)


  st_3.io.enable <> bb_1.io.Out(4)


  br_4.io.enable <> bb_1.io.Out(5)


  const1.io.enable <> bb_2.io.Out(0)

  const2.io.enable <> bb_2.io.Out(1)

  const3.io.enable <> bb_2.io.Out(2)

  const4.io.enable <> bb_2.io.Out(3)

  phi5.io.enable <> bb_2.io.Out(4)


  Gep_tmp6.io.enable <> bb_2.io.Out(5)


  Gep_tmp17.io.enable <> bb_2.io.Out(6)


  ld_8.io.enable <> bb_2.io.Out(7)


  ld_9.io.enable <> bb_2.io.Out(8)


  FP_10.io.enable <> bb_2.io.Out(9)


  st_11.io.enable <> bb_2.io.Out(10)


  binaryOp_12.io.enable <> bb_2.io.Out(11)


  icmp_13.io.enable <> bb_2.io.Out(12)


  br_14.io.enable <> bb_2.io.Out(13)


  const5.io.enable <> bb_3.io.Out(0)

  const6.io.enable <> bb_3.io.Out(1)

  ld_15.io.enable <> bb_3.io.Out(2)


  FP_16.io.enable <> bb_3.io.Out(3)


  st_17.io.enable <> bb_3.io.Out(4)


  binaryOp_18.io.enable <> bb_3.io.Out(5)


  icmp_19.io.enable <> bb_3.io.Out(6)


  br_20.io.enable <> bb_3.io.Out(7)


  br_21.io.enable <> bb_4.io.Out(0)


  const7.io.enable <> bb_5.io.Out(0)

  phi22.io.enable <> bb_5.io.Out(1)


  br_23.io.enable <> bb_5.io.Out(2)


  const8.io.enable <> bb_6.io.Out(0)

  const9.io.enable <> bb_6.io.Out(1)

  const10.io.enable <> bb_6.io.Out(2)

  const11.io.enable <> bb_6.io.Out(3)

  phi24.io.enable <> bb_6.io.Out(4)


  Gep_25.io.enable <> bb_6.io.Out(5)


  ld_26.io.enable <> bb_6.io.Out(6)


  Gep_tmp227.io.enable <> bb_6.io.Out(7)


  Gep_tmp328.io.enable <> bb_6.io.Out(8)


  ld_29.io.enable <> bb_6.io.Out(9)


  FP_30.io.enable <> bb_6.io.Out(10)


  st_31.io.enable <> bb_6.io.Out(11)


  binaryOp_32.io.enable <> bb_6.io.Out(12)


  icmp_33.io.enable <> bb_6.io.Out(13)


  br_34.io.enable <> bb_6.io.Out(14)


  const12.io.enable <> bb_7.io.Out(0)

  const13.io.enable <> bb_7.io.Out(1)

  binaryOp_35.io.enable <> bb_7.io.Out(2)


  icmp_36.io.enable <> bb_7.io.Out(3)


  br_37.io.enable <> bb_7.io.Out(4)


  constf1.io.enable <> bb_8.io.Out(0)

  FP_38.io.enable <> bb_8.io.Out(1)


  br_39.io.enable <> bb_8.io.Out(2)


  const14.io.enable <> bb_9.io.Out(0)

  phi40.io.enable <> bb_9.io.Out(1)


  br_41.io.enable <> bb_9.io.Out(2)


  constf2.io.enable <> bb_10.io.Out(1)

  const15.io.enable <> bb_10.io.Out(0)

  phi42.io.enable <> bb_10.io.Out(2)


  Gep_tmp443.io.enable <> bb_10.io.Out(3)


  Gep_tmp544.io.enable <> bb_10.io.Out(4)


  st_45.io.enable <> bb_10.io.Out(5)


  br_46.io.enable <> bb_10.io.Out(6)


  const16.io.enable <> bb_11.io.Out(0)

  const17.io.enable <> bb_11.io.Out(1)

  const18.io.enable <> bb_11.io.Out(2)

  const19.io.enable <> bb_11.io.Out(3)

  const20.io.enable <> bb_11.io.Out(4)

  phi47.io.enable <> bb_11.io.Out(5)


  Gep_tmp648.io.enable <> bb_11.io.Out(6)


  Gep_tmp749.io.enable <> bb_11.io.Out(7)


  ld_50.io.enable <> bb_11.io.Out(8)


  Gep_tmp851.io.enable <> bb_11.io.Out(9)


  Gep_tmp952.io.enable <> bb_11.io.Out(10)


  ld_53.io.enable <> bb_11.io.Out(11)


  FP_54.io.enable <> bb_11.io.Out(12)


  ld_55.io.enable <> bb_11.io.Out(13)


  FP_56.io.enable <> bb_11.io.Out(14)


  st_57.io.enable <> bb_11.io.Out(15)


  binaryOp_58.io.enable <> bb_11.io.Out(16)


  icmp_59.io.enable <> bb_11.io.Out(17)


  br_60.io.enable <> bb_11.io.Out(18)


  const21.io.enable <> bb_12.io.Out(0)

  const22.io.enable <> bb_12.io.Out(1)

  const23.io.enable <> bb_12.io.Out(2)

  ld_61.io.enable <> bb_12.io.Out(3)


  FP_62.io.enable <> bb_12.io.Out(4)


  st_63.io.enable <> bb_12.io.Out(5)


  Gep_tmp1064.io.enable <> bb_12.io.Out(6)


  Gep_tmp1165.io.enable <> bb_12.io.Out(7)


  st_66.io.enable <> bb_12.io.Out(8)


  binaryOp_67.io.enable <> bb_12.io.Out(9)


  icmp_68.io.enable <> bb_12.io.Out(10)


  br_69.io.enable <> bb_12.io.Out(11)


  const24.io.enable <> bb_13.io.Out(0)

  const25.io.enable <> bb_13.io.Out(1)

  binaryOp_70.io.enable <> bb_13.io.Out(2)


  icmp_71.io.enable <> bb_13.io.Out(3)


  br_72.io.enable <> bb_13.io.Out(4)


  ret_73.io.In.enable <> bb_14.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi5.io.Mask <> bb_2.io.MaskBB(0)

  phi22.io.Mask <> bb_5.io.MaskBB(0)

  phi24.io.Mask <> bb_6.io.MaskBB(0)

  phi40.io.Mask <> bb_9.io.MaskBB(0)

  phi42.io.Mask <> bb_10.io.MaskBB(0)

  phi47.io.Mask <> bb_11.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.WriteIn(0) <> st_3.io.memReq

  st_3.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(0) <> ld_8.io.memReq

  ld_8.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_9.io.memReq

  ld_9.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(1) <> st_11.io.memReq

  st_11.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.ReadIn(2) <> ld_15.io.memReq

  ld_15.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(2) <> st_17.io.memReq

  st_17.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.ReadIn(3) <> ld_26.io.memReq

  ld_26.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_29.io.memReq

  ld_29.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.WriteIn(3) <> st_31.io.memReq

  st_31.io.memResp <> MemCtrl.io.WriteOut(3)

  MemCtrl.io.WriteIn(4) <> st_45.io.memReq

  st_45.io.memResp <> MemCtrl.io.WriteOut(4)

  MemCtrl.io.ReadIn(5) <> ld_50.io.memReq

  ld_50.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.ReadIn(6) <> ld_53.io.memReq

  ld_53.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.ReadIn(7) <> ld_55.io.memReq

  ld_55.io.memResp <> MemCtrl.io.ReadOut(7)

  MemCtrl.io.WriteIn(5) <> st_57.io.memReq

  st_57.io.memResp <> MemCtrl.io.WriteOut(5)

  MemCtrl.io.ReadIn(8) <> ld_61.io.memReq

  ld_61.io.memResp <> MemCtrl.io.ReadOut(8)

  MemCtrl.io.WriteIn(6) <> st_63.io.memReq

  st_63.io.memResp <> MemCtrl.io.WriteOut(6)

  MemCtrl.io.WriteIn(7) <> st_66.io.memReq

  st_66.io.memResp <> MemCtrl.io.WriteOut(7)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */

  SharedFPU.io.InData(0) <> FP_16.io.FUReq
  FP_16.io.FUResp <> SharedFPU.io.OutData(0)

  SharedFPU.io.InData(1) <> FP_62.io.FUReq
  FP_62.io.FUResp <> SharedFPU.io.OutData(1)



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi1.io.InData(0) <> const0.io.Out

  phi5.io.InData(0) <> const1.io.Out

  Gep_tmp17.io.idx(0) <> const2.io.Out

  binaryOp_12.io.RightIO <> const3.io.Out

  icmp_13.io.RightIO <> const4.io.Out

  binaryOp_18.io.RightIO <> const5.io.Out

  icmp_19.io.RightIO <> const6.io.Out

  phi22.io.InData(1) <> const7.io.Out

  phi24.io.InData(0) <> const8.io.Out

  Gep_tmp328.io.idx(0) <> const9.io.Out

  binaryOp_32.io.RightIO <> const10.io.Out

  icmp_33.io.RightIO <> const11.io.Out

  binaryOp_35.io.RightIO <> const12.io.Out

  icmp_36.io.RightIO <> const13.io.Out

  phi40.io.InData(0) <> const14.io.Out

  Gep_tmp544.io.idx(0) <> const15.io.Out

  phi47.io.InData(0) <> const16.io.Out

  Gep_tmp749.io.idx(0) <> const17.io.Out

  Gep_tmp952.io.idx(0) <> const18.io.Out

  binaryOp_58.io.RightIO <> const19.io.Out

  icmp_59.io.RightIO <> const20.io.Out

  Gep_tmp1165.io.idx(0) <> const21.io.Out

  binaryOp_67.io.RightIO <> const22.io.Out

  icmp_68.io.RightIO <> const23.io.Out

  binaryOp_70.io.RightIO <> const24.io.Out

  icmp_71.io.RightIO <> const25.io.Out

  st_3.io.inData <> constf0.io.Out(0)

  FP_38.io.RightIO <> constf1.io.Out(0)

  st_45.io.inData <> constf2.io.Out(0)

  Gep_2.io.idx(0) <> phi1.io.Out(1)

  binaryOp_18.io.LeftIO <> phi1.io.Out(2)

  st_3.io.GepAddr <> Gep_2.io.Out(1)

  ld_15.io.GepAddr <> Gep_2.io.Out(2)

  st_17.io.GepAddr <> Gep_2.io.Out(3)

  Gep_tmp6.io.idx(0) <> phi5.io.Out(0)

  binaryOp_12.io.LeftIO <> phi5.io.Out(1)

  Gep_tmp17.io.baseAddress <> Gep_tmp6.io.Out(0)

  ld_8.io.GepAddr <> Gep_tmp17.io.Out(0)

  FP_10.io.LeftIO <> ld_8.io.Out(0)

  FP_10.io.RightIO <> ld_9.io.Out(0)

  st_11.io.inData <> FP_10.io.Out(0)

  icmp_13.io.LeftIO <> binaryOp_12.io.Out(1)

  br_14.io.CmpIO <> icmp_13.io.Out(0)

  FP_16.io.a <> ld_15.io.Out(0)

  st_17.io.inData <> FP_16.io.Out(0)

  icmp_19.io.LeftIO <> binaryOp_18.io.Out(1)

  br_20.io.CmpIO <> icmp_19.io.Out(0)

  binaryOp_35.io.LeftIO <> phi22.io.Out(1)

  Gep_25.io.idx(0) <> phi24.io.Out(0)

  Gep_tmp328.io.idx(1) <> phi24.io.Out(1)

  binaryOp_32.io.LeftIO <> phi24.io.Out(2)

  ld_26.io.GepAddr <> Gep_25.io.Out(0)

  FP_30.io.RightIO <> ld_26.io.Out(0)

  Gep_tmp328.io.baseAddress <> Gep_tmp227.io.Out(0)

  ld_29.io.GepAddr <> Gep_tmp328.io.Out(0)

  st_31.io.GepAddr <> Gep_tmp328.io.Out(1)

  FP_30.io.LeftIO <> ld_29.io.Out(0)

  st_31.io.inData <> FP_30.io.Out(0)

  icmp_33.io.LeftIO <> binaryOp_32.io.Out(1)

  br_34.io.CmpIO <> icmp_33.io.Out(0)

  icmp_36.io.LeftIO <> binaryOp_35.io.Out(1)

  br_37.io.CmpIO <> icmp_36.io.Out(0)

  binaryOp_70.io.LeftIO <> phi40.io.Out(1)

  Gep_tmp544.io.idx(1) <> phi42.io.Out(1)

  Gep_tmp1064.io.idx(0) <> phi42.io.Out(2)

  binaryOp_67.io.LeftIO <> phi42.io.Out(3)

  Gep_tmp544.io.baseAddress <> Gep_tmp443.io.Out(0)

  st_45.io.GepAddr <> Gep_tmp544.io.Out(1)

  ld_61.io.GepAddr <> Gep_tmp544.io.Out(2)

  st_63.io.GepAddr <> Gep_tmp544.io.Out(3)

  Gep_tmp648.io.idx(0) <> phi47.io.Out(0)

  Gep_tmp851.io.idx(0) <> phi47.io.Out(1)

  binaryOp_58.io.LeftIO <> phi47.io.Out(2)

  Gep_tmp749.io.baseAddress <> Gep_tmp648.io.Out(0)

  ld_50.io.GepAddr <> Gep_tmp749.io.Out(0)

  FP_54.io.LeftIO <> ld_50.io.Out(0)

  Gep_tmp952.io.baseAddress <> Gep_tmp851.io.Out(0)

  ld_53.io.GepAddr <> Gep_tmp952.io.Out(0)

  FP_54.io.RightIO <> ld_53.io.Out(0)

  FP_56.io.RightIO <> FP_54.io.Out(0)

  FP_56.io.LeftIO <> ld_55.io.Out(0)

  st_57.io.inData <> FP_56.io.Out(0)

  icmp_59.io.LeftIO <> binaryOp_58.io.Out(1)

  br_60.io.CmpIO <> icmp_59.io.Out(0)

  FP_62.io.a <> ld_61.io.Out(0)

  st_63.io.inData <> FP_62.io.Out(0)

  st_66.io.inData <> FP_62.io.Out(1)

  Gep_tmp1165.io.baseAddress <> Gep_tmp1064.io.Out(0)

  st_66.io.GepAddr <> Gep_tmp1165.io.Out(0)

  icmp_68.io.LeftIO <> binaryOp_67.io.Out(1)

  br_69.io.CmpIO <> icmp_68.io.Out(0)

  icmp_71.io.LeftIO <> binaryOp_70.io.Out(1)

  br_72.io.CmpIO <> icmp_71.io.Out(0)

  FP_38.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  st_3.io.Out(0).ready := true.B

  st_11.io.Out(0).ready := true.B

  st_17.io.Out(0).ready := true.B

  st_31.io.Out(0).ready := true.B

  st_45.io.Out(0).ready := true.B

  st_57.io.Out(0).ready := true.B

  st_63.io.Out(0).ready := true.B

  st_66.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_73.io.Out

}

import java.io.{File, FileWriter}

object covarianceTop extends App {
  val dir = new File("RTL/covarianceTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new covarianceDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
