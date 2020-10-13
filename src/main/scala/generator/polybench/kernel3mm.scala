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

abstract class k3mmDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class k3mmDF(implicit p: Parameters) extends k3mmDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 9, NWrites = 6)
  (WControl = new WriteMemoryController(NumOps = 6, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 9, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(2, 1, 1, 2, 1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1, 2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 4))

  val Loop_4 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 5))

  val Loop_5 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 6))

  val Loop_6 = Module(new LoopBlockNode(NumIns = List(1, 2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 7))

  val Loop_7 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 8))

  val Loop_8 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 9))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 8, NumPhi = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 19, NumPhi = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 4))

  val bb_5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 5))

  val bb_6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 6))

  val bb_7 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 7))

  val bb_8 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 8, NumPhi = 1, BID = 8))

  val bb_9 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 19, NumPhi = 1, BID = 9))

  val bb_10 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 10))

  val bb_11 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 11))

  val bb_12 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 12))

  val bb_13 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 13))

  val bb_14 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 8, NumPhi = 1, BID = 14))

  val bb_15 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 19, NumPhi = 1, BID = 15))

  val bb_16 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 16))

  val bb_17 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 17))

  val bb_18 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 18))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %8, !dbg !93, !UID !95, !BB_UID !96
  val br_0 = Module(new UBranchFastNode(ID = 0))

  //  %9 = phi i64 [ 0, %7 ], [ %25, %24 ], !UID !97
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 1, Res = true))

  //  br label %10, !dbg !99, !UID !102, !BB_UID !103
  val br_2 = Module(new UBranchFastNode(ID = 2))

  //  %11 = phi i64 [ 0, %8 ], [ %22, %21 ], !UID !104
  val phi3 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 3, Res = true))

  //  %tmp = getelementptr [18 x double], [18 x double]* %0, i64 %9, !UID !105
  val Gep_tmp4 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 4)(ElementSize = 8, ArraySize = List()))

  //  %tmp1 = getelementptr [18 x double], [18 x double]* %tmp, i64 0, i64 %11, !UID !106
  val Gep_tmp15 = Module(new GepNode(NumIns = 2, NumOuts = 2, ID = 5)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %tmp1, align 8, !dbg !107, !tbaa !110, !UID !114
  val st_6 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 6, RouteID = 0))

  //  br label %12, !dbg !116, !UID !118, !BB_UID !119
  val br_7 = Module(new UBranchFastNode(ID = 7))

  //  %13 = phi i64 [ 0, %10 ], [ %19, %12 ], !UID !120
  val phi8 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 8, Res = true))

  //  %tmp2 = getelementptr [20 x double], [20 x double]* %1, i64 %9, !UID !121
  val Gep_tmp29 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 9)(ElementSize = 8, ArraySize = List()))

  //  %tmp3 = getelementptr [20 x double], [20 x double]* %tmp2, i64 0, i64 %13, !UID !122
  val Gep_tmp310 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 10)(ElementSize = 8, ArraySize = List()))

  //  %14 = load double, double* %tmp3, align 8, !dbg !123, !tbaa !110, !UID !125
  val ld_11 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 11, RouteID = 0))

  //  %tmp4 = getelementptr [18 x double], [18 x double]* %2, i64 %13, !UID !126
  val Gep_tmp412 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 12)(ElementSize = 8, ArraySize = List()))

  //  %tmp5 = getelementptr [18 x double], [18 x double]* %tmp4, i64 0, i64 %11, !UID !127
  val Gep_tmp513 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 13)(ElementSize = 8, ArraySize = List()))

  //  %15 = load double, double* %tmp5, align 8, !dbg !128, !tbaa !110, !UID !129
  val ld_14 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 14, RouteID = 1))

  //  %16 = fmul double %14, %15, !dbg !130, !UID !131
  val FP_15 = Module(new FPComputeNode(NumOuts = 1, ID = 15, opCode = "fmul")(t = p(FTYP)))

  //  %17 = load double, double* %tmp1, align 8, !dbg !132, !tbaa !110, !UID !133
  val ld_16 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 16, RouteID = 2))

  //  %18 = fadd double %17, %16, !dbg !132, !UID !134
  val FP_17 = Module(new FPComputeNode(NumOuts = 1, ID = 17, opCode = "fadd")(t = p(FTYP)))

  //  store double %18, double* %tmp1, align 8, !dbg !132, !tbaa !110, !UID !135
  val st_18 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 18, RouteID = 1))

  //  %19 = add nuw nsw i64 %13, 1, !dbg !136, !UID !137
  val binaryOp_19 = Module(new ComputeFastNode(NumOuts = 2, ID = 19, opCode = "add")(sign = false))

  //  %20 = icmp eq i64 %19, 20, !dbg !138, !UID !139
  val icmp_20 = Module(new IcmpFastNode(NumOuts = 1, ID = 20, opCode = "eq")(sign = false))

  //  br i1 %20, label %21, label %12, !dbg !116, !llvm.loop !140, !UID !142, !BB_UID !143
  val br_21 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 21))

  //  %22 = add nuw nsw i64 %11, 1, !dbg !144, !UID !145
  val binaryOp_22 = Module(new ComputeFastNode(NumOuts = 2, ID = 22, opCode = "add")(sign = false))

  //  %23 = icmp eq i64 %22, 18, !dbg !146, !UID !147
  val icmp_23 = Module(new IcmpFastNode(NumOuts = 1, ID = 23, opCode = "eq")(sign = false))

  //  br i1 %23, label %24, label %10, !dbg !99, !llvm.loop !148, !UID !150, !BB_UID !151
  val br_24 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 24))

  //  %25 = add nuw nsw i64 %9, 1, !dbg !152, !UID !153
  val binaryOp_25 = Module(new ComputeNode(NumOuts = 2, ID = 25, opCode = "add")(sign = false))

  //  %26 = icmp eq i64 %25, 16, !dbg !154, !UID !155
  val icmp_26 = Module(new IcmpFastNode(NumOuts = 1, ID = 26, opCode = "eq")(sign = false))

  //  br i1 %26, label %27, label %8, !dbg !93, !llvm.loop !156, !UID !158, !BB_UID !159
  val br_27 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 27))

  //  br label %28, !dbg !160, !UID !164, !BB_UID !165
  val br_28 = Module(new UBranchNode(ID = 28))

  //  %29 = phi i64 [ %45, %44 ], [ 0, %27 ], !UID !166
  val phi29 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 29, Res = true))

  //  br label %30, !dbg !160, !UID !167, !BB_UID !168
  val br_30 = Module(new UBranchNode(ID = 30))

  //  %31 = phi i64 [ 0, %28 ], [ %42, %41 ], !UID !169
  val phi31 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 31, Res = true))

  //  %tmp6 = getelementptr [22 x double], [22 x double]* %3, i64 %29, !UID !170
  val Gep_tmp632 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 32)(ElementSize = 8, ArraySize = List()))

  //  %tmp7 = getelementptr [22 x double], [22 x double]* %tmp6, i64 0, i64 %31, !UID !171
  val Gep_tmp733 = Module(new GepNode(NumIns = 2, NumOuts = 2, ID = 33)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %tmp7, align 8, !dbg !172, !tbaa !110, !UID !175
  val st_34 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 34, RouteID = 2))

  //  br label %32, !dbg !176, !UID !178, !BB_UID !179
  val br_35 = Module(new UBranchNode(ID = 35))

  //  %33 = phi i64 [ 0, %30 ], [ %39, %32 ], !UID !180
  val phi36 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 36, Res = true))

  //  %tmp8 = getelementptr [24 x double], [24 x double]* %4, i64 %29, !UID !181
  val Gep_tmp837 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 37)(ElementSize = 8, ArraySize = List()))

  //  %tmp9 = getelementptr [24 x double], [24 x double]* %tmp8, i64 0, i64 %33, !UID !182
  val Gep_tmp938 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 38)(ElementSize = 8, ArraySize = List()))

  //  %34 = load double, double* %tmp9, align 8, !dbg !183, !tbaa !110, !UID !185
  val ld_39 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 39, RouteID = 3))

  //  %tmp10 = getelementptr [22 x double], [22 x double]* %5, i64 %33, !UID !186
  val Gep_tmp1040 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 40)(ElementSize = 8, ArraySize = List()))

  //  %tmp11 = getelementptr [22 x double], [22 x double]* %tmp10, i64 0, i64 %31, !UID !187
  val Gep_tmp1141 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 41)(ElementSize = 8, ArraySize = List()))

  //  %35 = load double, double* %tmp11, align 8, !dbg !188, !tbaa !110, !UID !189
  val ld_42 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 42, RouteID = 4))

  //  %36 = fmul double %34, %35, !dbg !190, !UID !191
  val FP_43 = Module(new FPComputeNode(NumOuts = 1, ID = 43, opCode = "fmul")(t = p(FTYP)))

  //  %37 = load double, double* %tmp7, align 8, !dbg !192, !tbaa !110, !UID !193
  val ld_44 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 44, RouteID = 5))

  //  %38 = fadd double %37, %36, !dbg !192, !UID !194
  val FP_45 = Module(new FPComputeNode(NumOuts = 1, ID = 45, opCode = "fadd")(t = p(FTYP)))

  //  store double %38, double* %tmp7, align 8, !dbg !192, !tbaa !110, !UID !195
  val st_46 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 46, RouteID = 3))

  //  %39 = add nuw nsw i64 %33, 1, !dbg !196, !UID !197
  val binaryOp_47 = Module(new ComputeFastNode(NumOuts = 2, ID = 47, opCode = "add")(sign = false))

  //  %40 = icmp eq i64 %39, 24, !dbg !198, !UID !199
  val icmp_48 = Module(new IcmpNode(NumOuts = 1, ID = 48, opCode = "eq")(sign = false))

  //  br i1 %40, label %41, label %32, !dbg !176, !llvm.loop !200, !UID !202, !BB_UID !203
  val br_49 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 49))

  //  %42 = add nuw nsw i64 %31, 1, !dbg !204, !UID !205
  val binaryOp_50 = Module(new ComputeFastNode(NumOuts = 2, ID = 50, opCode = "add")(sign = false))

  //  %43 = icmp eq i64 %42, 22, !dbg !206, !UID !207
  val icmp_51 = Module(new IcmpFastNode(NumOuts = 1, ID = 51, opCode = "eq")(sign = false))

  //  br i1 %43, label %44, label %30, !dbg !160, !llvm.loop !208, !UID !210, !BB_UID !211
  val br_52 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 52))

  //  %45 = add nuw nsw i64 %29, 1, !dbg !212, !UID !213
  val binaryOp_53 = Module(new ComputeFastNode(NumOuts = 2, ID = 53, opCode = "add")(sign = false))

  //  %46 = icmp eq i64 %45, 18, !dbg !214, !UID !215
  val icmp_54 = Module(new IcmpFastNode(NumOuts = 1, ID = 54, opCode = "eq")(sign = false))

  //  br i1 %46, label %47, label %28, !dbg !216, !llvm.loop !217, !UID !219, !BB_UID !220
  val br_55 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 55))

  //  br label %48, !dbg !221, !UID !225, !BB_UID !226
  val br_56 = Module(new UBranchNode(ID = 56))

  //  %49 = phi i64 [ %65, %64 ], [ 0, %47 ], !UID !227
  val phi57 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 57, Res = true))

  //  br label %50, !dbg !221, !UID !228, !BB_UID !229
  val br_58 = Module(new UBranchFastNode(ID = 58))

  //  %51 = phi i64 [ 0, %48 ], [ %62, %61 ], !UID !230
  val phi59 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 59, Res = true))

  //  %tmp12 = getelementptr [22 x double], [22 x double]* %6, i64 %49, !UID !231
  val Gep_tmp1260 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 60)(ElementSize = 8, ArraySize = List()))

  //  %tmp13 = getelementptr [22 x double], [22 x double]* %tmp12, i64 0, i64 %51, !UID !232
  val Gep_tmp1361 = Module(new GepNode(NumIns = 2, NumOuts = 2, ID = 61)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %tmp13, align 8, !dbg !233, !tbaa !110, !UID !236
  val st_62 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 62, RouteID = 4))

  //  br label %52, !dbg !237, !UID !239, !BB_UID !240
  val br_63 = Module(new UBranchNode(ID = 63))

  //  %53 = phi i64 [ 0, %50 ], [ %59, %52 ], !UID !241
  val phi64 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 64, Res = true))

  //  %tmp14 = getelementptr [18 x double], [18 x double]* %0, i64 %49, !UID !242
  val Gep_tmp1465 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 65)(ElementSize = 8, ArraySize = List()))

  //  %tmp15 = getelementptr [18 x double], [18 x double]* %tmp14, i64 0, i64 %53, !UID !243
  val Gep_tmp1566 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 66)(ElementSize = 8, ArraySize = List()))

  //  %54 = load double, double* %tmp15, align 8, !dbg !244, !tbaa !110, !UID !246
  val ld_67 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 67, RouteID = 6))

  //  %tmp16 = getelementptr [22 x double], [22 x double]* %3, i64 %53, !UID !247
  val Gep_tmp1668 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 68)(ElementSize = 8, ArraySize = List()))

  //  %tmp17 = getelementptr [22 x double], [22 x double]* %tmp16, i64 0, i64 %51, !UID !248
  val Gep_tmp1769 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 69)(ElementSize = 8, ArraySize = List()))

  //  %55 = load double, double* %tmp17, align 8, !dbg !249, !tbaa !110, !UID !250
  val ld_70 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 70, RouteID = 7))

  //  %56 = fmul double %54, %55, !dbg !251, !UID !252
  val FP_71 = Module(new FPComputeNode(NumOuts = 1, ID = 71, opCode = "fmul")(t = p(FTYP)))

  //  %57 = load double, double* %tmp13, align 8, !dbg !253, !tbaa !110, !UID !254
  val ld_72 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 72, RouteID = 8))

  //  %58 = fadd double %57, %56, !dbg !253, !UID !255
  val FP_73 = Module(new FPComputeNode(NumOuts = 1, ID = 73, opCode = "fadd")(t = p(FTYP)))

  //  store double %58, double* %tmp13, align 8, !dbg !253, !tbaa !110, !UID !256
  val st_74 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 74, RouteID = 5))

  //  %59 = add nuw nsw i64 %53, 1, !dbg !257, !UID !258
  val binaryOp_75 = Module(new ComputeFastNode(NumOuts = 2, ID = 75, opCode = "add")(sign = false))

  //  %60 = icmp eq i64 %59, 18, !dbg !259, !UID !260
  val icmp_76 = Module(new IcmpFastNode(NumOuts = 1, ID = 76, opCode = "eq")(sign = false))

  //  br i1 %60, label %61, label %52, !dbg !237, !llvm.loop !261, !UID !263, !BB_UID !264
  val br_77 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 77))

  //  %62 = add nuw nsw i64 %51, 1, !dbg !265, !UID !266
  val binaryOp_78 = Module(new ComputeFastNode(NumOuts = 2, ID = 78, opCode = "add")(sign = false))

  //  %63 = icmp eq i64 %62, 22, !dbg !267, !UID !268
  val icmp_79 = Module(new IcmpFastNode(NumOuts = 1, ID = 79, opCode = "eq")(sign = false))

  //  br i1 %63, label %64, label %50, !dbg !221, !llvm.loop !269, !UID !271, !BB_UID !272
  val br_80 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 80))

  //  %65 = add nuw nsw i64 %49, 1, !dbg !273, !UID !274
  val binaryOp_81 = Module(new ComputeFastNode(NumOuts = 2, ID = 81, opCode = "add")(sign = false))

  //  %66 = icmp eq i64 %65, 16, !dbg !275, !UID !276
  val icmp_82 = Module(new IcmpFastNode(NumOuts = 1, ID = 82, opCode = "eq")(sign = false))

  //  br i1 %66, label %67, label %48, !dbg !277, !llvm.loop !278, !UID !280, !BB_UID !281
  val br_83 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 83))

  //  ret void, !dbg !282, !UID !283, !BB_UID !284
  val ret_84 = Module(new RetNode2(retTypes = List(), ID = 84))



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

  //i64 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i64 0
  val const5 = Module(new ConstFastNode(value = 0, ID = 5))

  //i64 1
  val const6 = Module(new ConstFastNode(value = 1, ID = 6))

  //i64 20
  val const7 = Module(new ConstFastNode(value = 20, ID = 7))

  //i64 1
  val const8 = Module(new ConstFastNode(value = 1, ID = 8))

  //i64 18
  val const9 = Module(new ConstFastNode(value = 18, ID = 9))

  //i64 1
  val const10 = Module(new ConstFastNode(value = 1, ID = 10))

  //i64 16
  val const11 = Module(new ConstFastNode(value = 16, ID = 11))

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

  //i64 24
  val const19 = Module(new ConstFastNode(value = 24, ID = 19))

  //i64 1
  val const20 = Module(new ConstFastNode(value = 1, ID = 20))

  //i64 22
  val const21 = Module(new ConstFastNode(value = 22, ID = 21))

  //i64 1
  val const22 = Module(new ConstFastNode(value = 1, ID = 22))

  //i64 18
  val const23 = Module(new ConstFastNode(value = 18, ID = 23))

  //i64 0
  val const24 = Module(new ConstFastNode(value = 0, ID = 24))

  //i64 0
  val const25 = Module(new ConstFastNode(value = 0, ID = 25))

  //i64 0
  val const26 = Module(new ConstFastNode(value = 0, ID = 26))

  //i64 0
  val const27 = Module(new ConstFastNode(value = 0, ID = 27))

  //i64 0
  val const28 = Module(new ConstFastNode(value = 0, ID = 28))

  //i64 0
  val const29 = Module(new ConstFastNode(value = 0, ID = 29))

  //i64 1
  val const30 = Module(new ConstFastNode(value = 1, ID = 30))

  //i64 18
  val const31 = Module(new ConstFastNode(value = 18, ID = 31))

  //i64 1
  val const32 = Module(new ConstFastNode(value = 1, ID = 32))

  //i64 22
  val const33 = Module(new ConstFastNode(value = 22, ID = 33))

  //i64 1
  val const34 = Module(new ConstFastNode(value = 1, ID = 34))

  //i64 16
  val const35 = Module(new ConstFastNode(value = 16, ID = 35))

  //double 0.000000e+00
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))

  //double 0.000000e+00
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

  bb_1.io.predicateIn(1) <> Loop_8.io.activate_loop_start

  bb_1.io.predicateIn(0) <> Loop_8.io.activate_loop_back

  bb_2.io.predicateIn(1) <> Loop_7.io.activate_loop_start

  bb_2.io.predicateIn(0) <> Loop_7.io.activate_loop_back

  bb_3.io.predicateIn(1) <> Loop_6.io.activate_loop_start

  bb_3.io.predicateIn(0) <> Loop_6.io.activate_loop_back

  bb_4.io.predicateIn(0) <> Loop_6.io.loopExit(0)

  bb_5.io.predicateIn(0) <> Loop_7.io.loopExit(0)

  bb_6.io.predicateIn(0) <> Loop_8.io.loopExit(0)

  bb_7.io.predicateIn(0) <> Loop_5.io.activate_loop_start

  bb_7.io.predicateIn(1) <> Loop_5.io.activate_loop_back

  bb_8.io.predicateIn(1) <> Loop_4.io.activate_loop_start

  bb_8.io.predicateIn(0) <> Loop_4.io.activate_loop_back

  bb_9.io.predicateIn(1) <> Loop_3.io.activate_loop_start

  bb_9.io.predicateIn(0) <> Loop_3.io.activate_loop_back

  bb_10.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_11.io.predicateIn(0) <> Loop_4.io.loopExit(0)

  bb_12.io.predicateIn(0) <> Loop_5.io.loopExit(0)

  bb_13.io.predicateIn(0) <> Loop_2.io.activate_loop_start

  bb_13.io.predicateIn(1) <> Loop_2.io.activate_loop_back

  bb_14.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_14.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_15.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_15.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_16.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_17.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_18.io.predicateIn(0) <> Loop_2.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_63.io.Out(0)

  Loop_0.io.loopBack(0) <> br_77.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_77.io.TrueOutput(0)

  Loop_1.io.enable <> br_58.io.Out(0)

  Loop_1.io.loopBack(0) <> br_80.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_80.io.TrueOutput(0)

  Loop_2.io.enable <> br_56.io.Out(0)

  Loop_2.io.loopBack(0) <> br_83.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_83.io.TrueOutput(0)

  Loop_3.io.enable <> br_35.io.Out(0)

  Loop_3.io.loopBack(0) <> br_49.io.FalseOutput(0)

  Loop_3.io.loopFinish(0) <> br_49.io.TrueOutput(0)

  Loop_4.io.enable <> br_30.io.Out(0)

  Loop_4.io.loopBack(0) <> br_52.io.FalseOutput(0)

  Loop_4.io.loopFinish(0) <> br_52.io.TrueOutput(0)

  Loop_5.io.enable <> br_28.io.Out(0)

  Loop_5.io.loopBack(0) <> br_55.io.FalseOutput(0)

  Loop_5.io.loopFinish(0) <> br_55.io.TrueOutput(0)

  Loop_6.io.enable <> br_7.io.Out(0)

  Loop_6.io.loopBack(0) <> br_21.io.FalseOutput(0)

  Loop_6.io.loopFinish(0) <> br_21.io.TrueOutput(0)

  Loop_7.io.enable <> br_2.io.Out(0)

  Loop_7.io.loopBack(0) <> br_24.io.FalseOutput(0)

  Loop_7.io.loopFinish(0) <> br_24.io.TrueOutput(0)

  Loop_8.io.enable <> br_0.io.Out(0)

  Loop_8.io.loopBack(0) <> br_27.io.FalseOutput(0)

  Loop_8.io.loopFinish(0) <> br_27.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> phi59.io.Out(0)

  Loop_0.io.InLiveIn(1) <> Gep_tmp1361.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(0) <> phi57.io.Out(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_2.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field6")(0)

  Loop_2.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_2.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_3.io.InLiveIn(0) <> phi31.io.Out(0)

  Loop_3.io.InLiveIn(1) <> Gep_tmp733.io.Out(0)

  Loop_3.io.InLiveIn(2) <> Loop_4.io.OutLiveIn.elements("field1")(0)

  Loop_3.io.InLiveIn(3) <> Loop_4.io.OutLiveIn.elements("field2")(0)

  Loop_3.io.InLiveIn(4) <> Loop_4.io.OutLiveIn.elements("field0")(0)

  Loop_4.io.InLiveIn(0) <> phi29.io.Out(0)

  Loop_4.io.InLiveIn(1) <> Loop_5.io.OutLiveIn.elements("field1")(0)

  Loop_4.io.InLiveIn(2) <> Loop_5.io.OutLiveIn.elements("field2")(0)

  Loop_4.io.InLiveIn(3) <> Loop_5.io.OutLiveIn.elements("field0")(0)

  Loop_5.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field3")(1)

  Loop_5.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field4")(0)

  Loop_5.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field5")(0)

  Loop_6.io.InLiveIn(0) <> phi3.io.Out(0)

  Loop_6.io.InLiveIn(1) <> Gep_tmp15.io.Out(0)

  Loop_6.io.InLiveIn(2) <> Loop_7.io.OutLiveIn.elements("field1")(0)

  Loop_6.io.InLiveIn(3) <> Loop_7.io.OutLiveIn.elements("field2")(0)

  Loop_6.io.InLiveIn(4) <> Loop_7.io.OutLiveIn.elements("field0")(0)

  Loop_7.io.InLiveIn(0) <> phi1.io.Out(0)

  Loop_7.io.InLiveIn(1) <> Loop_8.io.OutLiveIn.elements("field2")(0)

  Loop_7.io.InLiveIn(2) <> Loop_8.io.OutLiveIn.elements("field1")(0)

  Loop_7.io.InLiveIn(3) <> Loop_8.io.OutLiveIn.elements("field0")(0)

  Loop_8.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(1)

  Loop_8.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_8.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_tmp1769.io.idx(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  ld_72.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(0)

  st_74.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(1)

  Gep_tmp1668.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_tmp1465.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  Gep_tmp1465.io.idx(0) <> Loop_0.io.OutLiveIn.elements("field4")(0)

  Gep_tmp1260.io.idx(0) <> Loop_1.io.OutLiveIn.elements("field0")(1)

  Gep_tmp1260.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Gep_tmp1141.io.idx(1) <> Loop_3.io.OutLiveIn.elements("field0")(0)

  ld_44.io.GepAddr <> Loop_3.io.OutLiveIn.elements("field1")(0)

  st_46.io.GepAddr <> Loop_3.io.OutLiveIn.elements("field1")(1)

  Gep_tmp837.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field2")(0)

  Gep_tmp1040.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field3")(0)

  Gep_tmp837.io.idx(0) <> Loop_3.io.OutLiveIn.elements("field4")(0)

  Gep_tmp632.io.idx(0) <> Loop_4.io.OutLiveIn.elements("field0")(1)

  Gep_tmp632.io.baseAddress <> Loop_4.io.OutLiveIn.elements("field3")(0)

  Gep_tmp513.io.idx(1) <> Loop_6.io.OutLiveIn.elements("field0")(0)

  ld_16.io.GepAddr <> Loop_6.io.OutLiveIn.elements("field1")(0)

  st_18.io.GepAddr <> Loop_6.io.OutLiveIn.elements("field1")(1)

  Gep_tmp412.io.baseAddress <> Loop_6.io.OutLiveIn.elements("field2")(0)

  Gep_tmp29.io.baseAddress <> Loop_6.io.OutLiveIn.elements("field3")(0)

  Gep_tmp29.io.idx(0) <> Loop_6.io.OutLiveIn.elements("field4")(0)

  Gep_tmp4.io.idx(0) <> Loop_7.io.OutLiveIn.elements("field0")(1)

  Gep_tmp4.io.baseAddress <> Loop_7.io.OutLiveIn.elements("field3")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_75.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_78.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_81.io.Out(0)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_47.io.Out(0)

  Loop_4.io.CarryDepenIn(0) <> binaryOp_50.io.Out(0)

  Loop_5.io.CarryDepenIn(0) <> binaryOp_53.io.Out(0)

  Loop_6.io.CarryDepenIn(0) <> binaryOp_19.io.Out(0)

  Loop_7.io.CarryDepenIn(0) <> binaryOp_22.io.Out(0)

  Loop_8.io.CarryDepenIn(0) <> binaryOp_25.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi64.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi59.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phi57.io.InData(0) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phi36.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phi31.io.InData(1) <> Loop_4.io.CarryDepenOut.elements("field0")(0)

  phi29.io.InData(0) <> Loop_5.io.CarryDepenOut.elements("field0")(0)

  phi8.io.InData(1) <> Loop_6.io.CarryDepenOut.elements("field0")(0)

  phi3.io.InData(1) <> Loop_7.io.CarryDepenOut.elements("field0")(0)

  phi1.io.InData(1) <> Loop_8.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  const0.io.enable <> bb_1.io.Out(0)

  phi1.io.enable <> bb_1.io.Out(1)


  br_2.io.enable <> bb_1.io.Out(2)


  constf0.io.enable <> bb_2.io.Out(2)

  const1.io.enable <> bb_2.io.Out(0)

  const2.io.enable <> bb_2.io.Out(1)

  phi3.io.enable <> bb_2.io.Out(3)


  Gep_tmp4.io.enable <> bb_2.io.Out(4)


  Gep_tmp15.io.enable <> bb_2.io.Out(5)


  st_6.io.enable <> bb_2.io.Out(6)


  br_7.io.enable <> bb_2.io.Out(7)


  const3.io.enable <> bb_3.io.Out(0)

  const4.io.enable <> bb_3.io.Out(1)

  const5.io.enable <> bb_3.io.Out(2)

  const6.io.enable <> bb_3.io.Out(3)

  const7.io.enable <> bb_3.io.Out(4)

  phi8.io.enable <> bb_3.io.Out(5)


  Gep_tmp29.io.enable <> bb_3.io.Out(6)


  Gep_tmp310.io.enable <> bb_3.io.Out(7)


  ld_11.io.enable <> bb_3.io.Out(8)


  Gep_tmp412.io.enable <> bb_3.io.Out(9)


  Gep_tmp513.io.enable <> bb_3.io.Out(10)


  ld_14.io.enable <> bb_3.io.Out(11)


  FP_15.io.enable <> bb_3.io.Out(12)


  ld_16.io.enable <> bb_3.io.Out(13)


  FP_17.io.enable <> bb_3.io.Out(14)


  st_18.io.enable <> bb_3.io.Out(15)


  binaryOp_19.io.enable <> bb_3.io.Out(16)


  icmp_20.io.enable <> bb_3.io.Out(17)


  br_21.io.enable <> bb_3.io.Out(18)


  const8.io.enable <> bb_4.io.Out(0)

  const9.io.enable <> bb_4.io.Out(1)

  binaryOp_22.io.enable <> bb_4.io.Out(2)


  icmp_23.io.enable <> bb_4.io.Out(3)


  br_24.io.enable <> bb_4.io.Out(4)


  const10.io.enable <> bb_5.io.Out(0)

  const11.io.enable <> bb_5.io.Out(1)

  binaryOp_25.io.enable <> bb_5.io.Out(2)


  icmp_26.io.enable <> bb_5.io.Out(3)


  br_27.io.enable <> bb_5.io.Out(4)


  br_28.io.enable <> bb_6.io.Out(0)


  const12.io.enable <> bb_7.io.Out(0)

  phi29.io.enable <> bb_7.io.Out(1)


  br_30.io.enable <> bb_7.io.Out(2)


  constf1.io.enable <> bb_8.io.Out(2)

  const13.io.enable <> bb_8.io.Out(0)

  const14.io.enable <> bb_8.io.Out(1)

  phi31.io.enable <> bb_8.io.Out(3)


  Gep_tmp632.io.enable <> bb_8.io.Out(4)


  Gep_tmp733.io.enable <> bb_8.io.Out(5)


  st_34.io.enable <> bb_8.io.Out(6)


  br_35.io.enable <> bb_8.io.Out(7)


  const15.io.enable <> bb_9.io.Out(0)

  const16.io.enable <> bb_9.io.Out(1)

  const17.io.enable <> bb_9.io.Out(2)

  const18.io.enable <> bb_9.io.Out(3)

  const19.io.enable <> bb_9.io.Out(4)

  phi36.io.enable <> bb_9.io.Out(5)


  Gep_tmp837.io.enable <> bb_9.io.Out(6)


  Gep_tmp938.io.enable <> bb_9.io.Out(7)


  ld_39.io.enable <> bb_9.io.Out(8)


  Gep_tmp1040.io.enable <> bb_9.io.Out(9)


  Gep_tmp1141.io.enable <> bb_9.io.Out(10)


  ld_42.io.enable <> bb_9.io.Out(11)


  FP_43.io.enable <> bb_9.io.Out(12)


  ld_44.io.enable <> bb_9.io.Out(13)


  FP_45.io.enable <> bb_9.io.Out(14)


  st_46.io.enable <> bb_9.io.Out(15)


  binaryOp_47.io.enable <> bb_9.io.Out(16)


  icmp_48.io.enable <> bb_9.io.Out(17)


  br_49.io.enable <> bb_9.io.Out(18)


  const20.io.enable <> bb_10.io.Out(0)

  const21.io.enable <> bb_10.io.Out(1)

  binaryOp_50.io.enable <> bb_10.io.Out(2)


  icmp_51.io.enable <> bb_10.io.Out(3)


  br_52.io.enable <> bb_10.io.Out(4)


  const22.io.enable <> bb_11.io.Out(0)

  const23.io.enable <> bb_11.io.Out(1)

  binaryOp_53.io.enable <> bb_11.io.Out(2)


  icmp_54.io.enable <> bb_11.io.Out(3)


  br_55.io.enable <> bb_11.io.Out(4)


  br_56.io.enable <> bb_12.io.Out(0)


  const24.io.enable <> bb_13.io.Out(0)

  phi57.io.enable <> bb_13.io.Out(1)


  br_58.io.enable <> bb_13.io.Out(2)


  constf2.io.enable <> bb_14.io.Out(2)

  const25.io.enable <> bb_14.io.Out(0)

  const26.io.enable <> bb_14.io.Out(1)

  phi59.io.enable <> bb_14.io.Out(3)


  Gep_tmp1260.io.enable <> bb_14.io.Out(4)


  Gep_tmp1361.io.enable <> bb_14.io.Out(5)


  st_62.io.enable <> bb_14.io.Out(6)


  br_63.io.enable <> bb_14.io.Out(7)


  const27.io.enable <> bb_15.io.Out(0)

  const28.io.enable <> bb_15.io.Out(1)

  const29.io.enable <> bb_15.io.Out(2)

  const30.io.enable <> bb_15.io.Out(3)

  const31.io.enable <> bb_15.io.Out(4)

  phi64.io.enable <> bb_15.io.Out(5)


  Gep_tmp1465.io.enable <> bb_15.io.Out(6)


  Gep_tmp1566.io.enable <> bb_15.io.Out(7)


  ld_67.io.enable <> bb_15.io.Out(8)


  Gep_tmp1668.io.enable <> bb_15.io.Out(9)


  Gep_tmp1769.io.enable <> bb_15.io.Out(10)


  ld_70.io.enable <> bb_15.io.Out(11)


  FP_71.io.enable <> bb_15.io.Out(12)


  ld_72.io.enable <> bb_15.io.Out(13)


  FP_73.io.enable <> bb_15.io.Out(14)


  st_74.io.enable <> bb_15.io.Out(15)


  binaryOp_75.io.enable <> bb_15.io.Out(16)


  icmp_76.io.enable <> bb_15.io.Out(17)


  br_77.io.enable <> bb_15.io.Out(18)


  const32.io.enable <> bb_16.io.Out(0)

  const33.io.enable <> bb_16.io.Out(1)

  binaryOp_78.io.enable <> bb_16.io.Out(2)


  icmp_79.io.enable <> bb_16.io.Out(3)


  br_80.io.enable <> bb_16.io.Out(4)


  const34.io.enable <> bb_17.io.Out(0)

  const35.io.enable <> bb_17.io.Out(1)

  binaryOp_81.io.enable <> bb_17.io.Out(2)


  icmp_82.io.enable <> bb_17.io.Out(3)


  br_83.io.enable <> bb_17.io.Out(4)


  ret_84.io.In.enable <> bb_18.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi3.io.Mask <> bb_2.io.MaskBB(0)

  phi8.io.Mask <> bb_3.io.MaskBB(0)

  phi29.io.Mask <> bb_7.io.MaskBB(0)

  phi31.io.Mask <> bb_8.io.MaskBB(0)

  phi36.io.Mask <> bb_9.io.MaskBB(0)

  phi57.io.Mask <> bb_13.io.MaskBB(0)

  phi59.io.Mask <> bb_14.io.MaskBB(0)

  phi64.io.Mask <> bb_15.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.WriteIn(0) <> st_6.io.memReq

  st_6.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(0) <> ld_11.io.memReq

  ld_11.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_16.io.memReq

  ld_16.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(1) <> st_18.io.memReq

  st_18.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_34.io.memReq

  st_34.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.ReadIn(3) <> ld_39.io.memReq

  ld_39.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_42.io.memReq

  ld_42.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_44.io.memReq

  ld_44.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.WriteIn(3) <> st_46.io.memReq

  st_46.io.memResp <> MemCtrl.io.WriteOut(3)

  MemCtrl.io.WriteIn(4) <> st_62.io.memReq

  st_62.io.memResp <> MemCtrl.io.WriteOut(4)

  MemCtrl.io.ReadIn(6) <> ld_67.io.memReq

  ld_67.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.ReadIn(7) <> ld_70.io.memReq

  ld_70.io.memResp <> MemCtrl.io.ReadOut(7)

  MemCtrl.io.ReadIn(8) <> ld_72.io.memReq

  ld_72.io.memResp <> MemCtrl.io.ReadOut(8)

  MemCtrl.io.WriteIn(5) <> st_74.io.memReq

  st_74.io.memResp <> MemCtrl.io.WriteOut(5)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi1.io.InData(0) <> const0.io.Out

  phi3.io.InData(0) <> const1.io.Out

  Gep_tmp15.io.idx(0) <> const2.io.Out

  phi8.io.InData(0) <> const3.io.Out

  Gep_tmp310.io.idx(0) <> const4.io.Out

  Gep_tmp513.io.idx(0) <> const5.io.Out

  binaryOp_19.io.RightIO <> const6.io.Out

  icmp_20.io.RightIO <> const7.io.Out

  binaryOp_22.io.RightIO <> const8.io.Out

  icmp_23.io.RightIO <> const9.io.Out

  binaryOp_25.io.RightIO <> const10.io.Out

  icmp_26.io.RightIO <> const11.io.Out

  phi29.io.InData(1) <> const12.io.Out

  phi31.io.InData(0) <> const13.io.Out

  Gep_tmp733.io.idx(0) <> const14.io.Out

  phi36.io.InData(0) <> const15.io.Out

  Gep_tmp938.io.idx(0) <> const16.io.Out

  Gep_tmp1141.io.idx(0) <> const17.io.Out

  binaryOp_47.io.RightIO <> const18.io.Out

  icmp_48.io.RightIO <> const19.io.Out

  binaryOp_50.io.RightIO <> const20.io.Out

  icmp_51.io.RightIO <> const21.io.Out

  binaryOp_53.io.RightIO <> const22.io.Out

  icmp_54.io.RightIO <> const23.io.Out

  phi57.io.InData(1) <> const24.io.Out

  phi59.io.InData(0) <> const25.io.Out

  Gep_tmp1361.io.idx(0) <> const26.io.Out

  phi64.io.InData(0) <> const27.io.Out

  Gep_tmp1566.io.idx(0) <> const28.io.Out

  Gep_tmp1769.io.idx(0) <> const29.io.Out

  binaryOp_75.io.RightIO <> const30.io.Out

  icmp_76.io.RightIO <> const31.io.Out

  binaryOp_78.io.RightIO <> const32.io.Out

  icmp_79.io.RightIO <> const33.io.Out

  binaryOp_81.io.RightIO <> const34.io.Out

  icmp_82.io.RightIO <> const35.io.Out

  st_6.io.inData <> constf0.io.Out(0)

  st_34.io.inData <> constf1.io.Out(0)

  st_62.io.inData <> constf2.io.Out(0)

  binaryOp_25.io.LeftIO <> phi1.io.Out(1)

  Gep_tmp15.io.idx(1) <> phi3.io.Out(1)

  binaryOp_22.io.LeftIO <> phi3.io.Out(2)

  Gep_tmp15.io.baseAddress <> Gep_tmp4.io.Out(0)

  st_6.io.GepAddr <> Gep_tmp15.io.Out(1)

  Gep_tmp310.io.idx(1) <> phi8.io.Out(0)

  Gep_tmp412.io.idx(0) <> phi8.io.Out(1)

  binaryOp_19.io.LeftIO <> phi8.io.Out(2)

  Gep_tmp310.io.baseAddress <> Gep_tmp29.io.Out(0)

  ld_11.io.GepAddr <> Gep_tmp310.io.Out(0)

  FP_15.io.LeftIO <> ld_11.io.Out(0)

  Gep_tmp513.io.baseAddress <> Gep_tmp412.io.Out(0)

  ld_14.io.GepAddr <> Gep_tmp513.io.Out(0)

  FP_15.io.RightIO <> ld_14.io.Out(0)

  FP_17.io.RightIO <> FP_15.io.Out(0)

  FP_17.io.LeftIO <> ld_16.io.Out(0)

  st_18.io.inData <> FP_17.io.Out(0)

  icmp_20.io.LeftIO <> binaryOp_19.io.Out(1)

  br_21.io.CmpIO <> icmp_20.io.Out(0)

  icmp_23.io.LeftIO <> binaryOp_22.io.Out(1)

  br_24.io.CmpIO <> icmp_23.io.Out(0)

  icmp_26.io.LeftIO <> binaryOp_25.io.Out(1)

  br_27.io.CmpIO <> icmp_26.io.Out(0)

  binaryOp_53.io.LeftIO <> phi29.io.Out(1)

  Gep_tmp733.io.idx(1) <> phi31.io.Out(1)

  binaryOp_50.io.LeftIO <> phi31.io.Out(2)

  Gep_tmp733.io.baseAddress <> Gep_tmp632.io.Out(0)

  st_34.io.GepAddr <> Gep_tmp733.io.Out(1)

  Gep_tmp938.io.idx(1) <> phi36.io.Out(0)

  Gep_tmp1040.io.idx(0) <> phi36.io.Out(1)

  binaryOp_47.io.LeftIO <> phi36.io.Out(2)

  Gep_tmp938.io.baseAddress <> Gep_tmp837.io.Out(0)

  ld_39.io.GepAddr <> Gep_tmp938.io.Out(0)

  FP_43.io.LeftIO <> ld_39.io.Out(0)

  Gep_tmp1141.io.baseAddress <> Gep_tmp1040.io.Out(0)

  ld_42.io.GepAddr <> Gep_tmp1141.io.Out(0)

  FP_43.io.RightIO <> ld_42.io.Out(0)

  FP_45.io.RightIO <> FP_43.io.Out(0)

  FP_45.io.LeftIO <> ld_44.io.Out(0)

  st_46.io.inData <> FP_45.io.Out(0)

  icmp_48.io.LeftIO <> binaryOp_47.io.Out(1)

  br_49.io.CmpIO <> icmp_48.io.Out(0)

  icmp_51.io.LeftIO <> binaryOp_50.io.Out(1)

  br_52.io.CmpIO <> icmp_51.io.Out(0)

  icmp_54.io.LeftIO <> binaryOp_53.io.Out(1)

  br_55.io.CmpIO <> icmp_54.io.Out(0)

  binaryOp_81.io.LeftIO <> phi57.io.Out(1)

  Gep_tmp1361.io.idx(1) <> phi59.io.Out(1)

  binaryOp_78.io.LeftIO <> phi59.io.Out(2)

  Gep_tmp1361.io.baseAddress <> Gep_tmp1260.io.Out(0)

  st_62.io.GepAddr <> Gep_tmp1361.io.Out(1)

  Gep_tmp1566.io.idx(1) <> phi64.io.Out(0)

  Gep_tmp1668.io.idx(0) <> phi64.io.Out(1)

  binaryOp_75.io.LeftIO <> phi64.io.Out(2)

  Gep_tmp1566.io.baseAddress <> Gep_tmp1465.io.Out(0)

  ld_67.io.GepAddr <> Gep_tmp1566.io.Out(0)

  FP_71.io.LeftIO <> ld_67.io.Out(0)

  Gep_tmp1769.io.baseAddress <> Gep_tmp1668.io.Out(0)

  ld_70.io.GepAddr <> Gep_tmp1769.io.Out(0)

  FP_71.io.RightIO <> ld_70.io.Out(0)

  FP_73.io.RightIO <> FP_71.io.Out(0)

  FP_73.io.LeftIO <> ld_72.io.Out(0)

  st_74.io.inData <> FP_73.io.Out(0)

  icmp_76.io.LeftIO <> binaryOp_75.io.Out(1)

  br_77.io.CmpIO <> icmp_76.io.Out(0)

  icmp_79.io.LeftIO <> binaryOp_78.io.Out(1)

  br_80.io.CmpIO <> icmp_79.io.Out(0)

  icmp_82.io.LeftIO <> binaryOp_81.io.Out(1)

  br_83.io.CmpIO <> icmp_82.io.Out(0)

  st_6.io.Out(0).ready := true.B

  st_18.io.Out(0).ready := true.B

  st_34.io.Out(0).ready := true.B

  st_46.io.Out(0).ready := true.B

  st_62.io.Out(0).ready := true.B

  st_74.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_84.io.Out

}

import java.io.{File, FileWriter}

object k3mmTop extends App {
  val dir = new File("RTL/k3mmTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new k3mmDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
