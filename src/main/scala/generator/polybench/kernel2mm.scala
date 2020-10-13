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

abstract class k2mmDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class k2mmDF(implicit p: Parameters) extends k2mmDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 7, NWrites = 4)
  (WControl = new WriteMemoryController(NumOps = 4, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 7, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 2, 1, 1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 2, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1, 2, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 4))

  val Loop_4 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 5))

  val Loop_5 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 6))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 8, NumPhi = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 20, NumPhi = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 4))

  val bb_5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 5))

  val bb_6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 6))

  val bb_7 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 7))

  val bb_8 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 9, NumPhi = 1, BID = 8))

  val bb_9 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 19, NumPhi = 1, BID = 9))

  val bb_10 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 10))

  val bb_11 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 11))

  val bb_12 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 12))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %8, !dbg !81, !UID !83, !BB_UID !84
  val br_0 = Module(new UBranchFastNode(ID = 0))

  //  %9 = phi i64 [ 0, %7 ], [ %26, %25 ], !UID !85
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 1, Res = true))

  //  br label %10, !dbg !87, !UID !90, !BB_UID !91
  val br_2 = Module(new UBranchFastNode(ID = 2))

  //  %11 = phi i64 [ 0, %8 ], [ %23, %22 ], !UID !92
  val phi3 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 3, Res = true))

  //  %tmp = getelementptr [18 x double], [18 x double]* %2, i64 %9, !UID !93
  val Gep_tmp4 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 4)(ElementSize = 8, ArraySize = List()))

  //  %tmp1 = getelementptr [18 x double], [18 x double]* %tmp, i64 0, i64 %11, !UID !94
  val Gep_tmp15 = Module(new GepNode(NumIns = 2, NumOuts = 2, ID = 5)(ElementSize = 8, ArraySize = List()))

  //  store double 0.000000e+00, double* %tmp1, align 8, !dbg !95, !tbaa !98, !UID !102
  val st_6 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 6, RouteID = 0))

  //  br label %12, !dbg !104, !UID !106, !BB_UID !107
  val br_7 = Module(new UBranchFastNode(ID = 7))

  //  %13 = phi i64 [ 0, %10 ], [ %20, %12 ], !UID !108
  val phi8 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 8, Res = true))

  //  %tmp2 = getelementptr [22 x double], [22 x double]* %3, i64 %9, !UID !109
  val Gep_tmp29 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 9)(ElementSize = 8, ArraySize = List()))

  //  %tmp3 = getelementptr [22 x double], [22 x double]* %tmp2, i64 0, i64 %13, !UID !110
  val Gep_tmp310 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 10)(ElementSize = 8, ArraySize = List()))

  //  %14 = load double, double* %tmp3, align 8, !dbg !111, !tbaa !98, !UID !113
  val ld_11 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 11, RouteID = 0))

  //  %15 = fmul double %14, %0, !dbg !114, !UID !115
  val FP_12 = Module(new FPComputeNode(NumOuts = 1, ID = 12, opCode = "fmul")(t = p(FTYP)))

  //  %tmp4 = getelementptr [18 x double], [18 x double]* %4, i64 %13, !UID !116
  val Gep_tmp413 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 13)(ElementSize = 8, ArraySize = List()))

  //  %tmp5 = getelementptr [18 x double], [18 x double]* %tmp4, i64 0, i64 %11, !UID !117
  val Gep_tmp514 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 14)(ElementSize = 8, ArraySize = List()))

  //  %16 = load double, double* %tmp5, align 8, !dbg !118, !tbaa !98, !UID !119
  val ld_15 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 15, RouteID = 1))

  //  %17 = fmul double %15, %16, !dbg !120, !UID !121
  val FP_16 = Module(new FPComputeNode(NumOuts = 1, ID = 16, opCode = "fmul")(t = p(FTYP)))

  //  %18 = load double, double* %tmp1, align 8, !dbg !122, !tbaa !98, !UID !123
  val ld_17 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 17, RouteID = 2))

  //  %19 = fadd double %18, %17, !dbg !122, !UID !124
  val FP_18 = Module(new FPComputeNode(NumOuts = 1, ID = 18, opCode = "fadd")(t = p(FTYP)))

  //  store double %19, double* %tmp1, align 8, !dbg !122, !tbaa !98, !UID !125
  val st_19 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 19, RouteID = 1))

  //  %20 = add nuw nsw i64 %13, 1, !dbg !126, !UID !127
  val binaryOp_20 = Module(new ComputeFastNode(NumOuts = 2, ID = 20, opCode = "add")(sign = false))

  //  %21 = icmp eq i64 %20, 22, !dbg !128, !UID !129
  val icmp_21 = Module(new IcmpFastNode(NumOuts = 1, ID = 21, opCode = "eq")(sign = false))

  //  br i1 %21, label %22, label %12, !dbg !104, !llvm.loop !130, !UID !132, !BB_UID !133
  val br_22 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 22))

  //  %23 = add nuw nsw i64 %11, 1, !dbg !134, !UID !135
  val binaryOp_23 = Module(new ComputeFastNode(NumOuts = 2, ID = 23, opCode = "add")(sign = false))

  //  %24 = icmp eq i64 %23, 18, !dbg !136, !UID !137
  val icmp_24 = Module(new IcmpFastNode(NumOuts = 1, ID = 24, opCode = "eq")(sign = false))

  //  br i1 %24, label %25, label %10, !dbg !87, !llvm.loop !138, !UID !140, !BB_UID !141
  val br_25 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 25))

  //  %26 = add nuw nsw i64 %9, 1, !dbg !142, !UID !143
  val binaryOp_26 = Module(new ComputeFastNode(NumOuts = 2, ID = 26, opCode = "add")(sign = false))

  //  %27 = icmp eq i64 %26, 16, !dbg !144, !UID !145
  val icmp_27 = Module(new IcmpFastNode(NumOuts = 1, ID = 27, opCode = "eq")(sign = false))

  //  br i1 %27, label %28, label %8, !dbg !81, !llvm.loop !146, !UID !148, !BB_UID !149
  val br_28 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 28))

  //  br label %29, !dbg !150, !UID !154, !BB_UID !155
  val br_29 = Module(new UBranchFastNode(ID = 29))

  //  %30 = phi i64 [ %48, %47 ], [ 0, %28 ], !UID !156
  val phi30 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 30, Res = true))

  //  br label %31, !dbg !150, !UID !157, !BB_UID !158
  val br_31 = Module(new UBranchFastNode(ID = 31))

  //  %32 = phi i64 [ 0, %29 ], [ %45, %44 ], !UID !159
  val phi32 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 32, Res = true))

  //  %tmp6 = getelementptr [24 x double], [24 x double]* %6, i64 %30, !UID !160
  val Gep_tmp633 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 33)(ElementSize = 8, ArraySize = List()))

  //  %tmp7 = getelementptr [24 x double], [24 x double]* %tmp6, i64 0, i64 %32, !UID !161
  val Gep_tmp734 = Module(new GepNode(NumIns = 2, NumOuts = 3, ID = 34)(ElementSize = 8, ArraySize = List()))

  //  %33 = load double, double* %tmp7, align 8, !dbg !162, !tbaa !98, !UID !165
  val ld_35 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 35, RouteID = 3))

  //  %34 = fmul double %33, %1, !dbg !162, !UID !166
  val FP_36 = Module(new FPComputeNode(NumOuts = 1, ID = 36, opCode = "fmul")(t = p(FTYP)))

  //  store double %34, double* %tmp7, align 8, !dbg !162, !tbaa !98, !UID !167
  val st_37 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 37, RouteID = 2))

  //  br label %35, !dbg !168, !UID !170, !BB_UID !171
  val br_38 = Module(new UBranchFastNode(ID = 38))

  //  %36 = phi i64 [ 0, %31 ], [ %42, %35 ], !UID !172
  val phi39 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 39, Res = true))

  //  %tmp8 = getelementptr [18 x double], [18 x double]* %2, i64 %30, !UID !173
  val Gep_tmp840 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 40)(ElementSize = 8, ArraySize = List()))

  //  %tmp9 = getelementptr [18 x double], [18 x double]* %tmp8, i64 0, i64 %36, !UID !174
  val Gep_tmp941 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 41)(ElementSize = 8, ArraySize = List()))

  //  %37 = load double, double* %tmp9, align 8, !dbg !175, !tbaa !98, !UID !177
  val ld_42 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 42, RouteID = 4))

  //  %tmp10 = getelementptr [24 x double], [24 x double]* %5, i64 %36, !UID !178
  val Gep_tmp1043 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 43)(ElementSize = 8, ArraySize = List()))

  //  %tmp11 = getelementptr [24 x double], [24 x double]* %tmp10, i64 0, i64 %32, !UID !179
  val Gep_tmp1144 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 44)(ElementSize = 8, ArraySize = List()))

  //  %38 = load double, double* %tmp11, align 8, !dbg !180, !tbaa !98, !UID !181
  val ld_45 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 45, RouteID = 5))

  //  %39 = fmul double %37, %38, !dbg !182, !UID !183
  val FP_46 = Module(new FPComputeNode(NumOuts = 1, ID = 46, opCode = "fmul")(t = p(FTYP)))

  //  %40 = load double, double* %tmp7, align 8, !dbg !184, !tbaa !98, !UID !185
  val ld_47 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 47, RouteID = 6))

  //  %41 = fadd double %40, %39, !dbg !184, !UID !186
  val FP_48 = Module(new FPComputeNode(NumOuts = 1, ID = 48, opCode = "fadd")(t = p(FTYP)))

  //  store double %41, double* %tmp7, align 8, !dbg !184, !tbaa !98, !UID !187
  val st_49 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 49, RouteID = 3))

  //  %42 = add nuw nsw i64 %36, 1, !dbg !188, !UID !189
  val binaryOp_50 = Module(new ComputeNode(NumOuts = 2, ID = 50, opCode = "add")(sign = false))

  //  %43 = icmp eq i64 %42, 18, !dbg !190, !UID !191
  val icmp_51 = Module(new IcmpFastNode(NumOuts = 1, ID = 51, opCode = "eq")(sign = false))

  //  br i1 %43, label %44, label %35, !dbg !168, !llvm.loop !192, !UID !194, !BB_UID !195
  val br_52 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 52))

  //  %45 = add nuw nsw i64 %32, 1, !dbg !196, !UID !197
  val binaryOp_53 = Module(new ComputeNode(NumOuts = 2, ID = 53, opCode = "add")(sign = false))

  //  %46 = icmp eq i64 %45, 24, !dbg !198, !UID !199
  val icmp_54 = Module(new IcmpFastNode(NumOuts = 1, ID = 54, opCode = "eq")(sign = false))

  //  br i1 %46, label %47, label %31, !dbg !150, !llvm.loop !200, !UID !202, !BB_UID !203
  val br_55 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 55))

  //  %48 = add nuw nsw i64 %30, 1, !dbg !204, !UID !205
  val binaryOp_56 = Module(new ComputeFastNode(NumOuts = 2, ID = 56, opCode = "add")(sign = false))

  //  %49 = icmp eq i64 %48, 16, !dbg !206, !UID !207
  val icmp_57 = Module(new IcmpFastNode(NumOuts = 1, ID = 57, opCode = "eq")(sign = false))

  //  br i1 %49, label %50, label %29, !dbg !208, !llvm.loop !209, !UID !211, !BB_UID !212
  val br_58 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 58))

  //  ret void, !dbg !213, !UID !214, !BB_UID !215
  val ret_59 = Module(new RetNode2(retTypes = List(), ID = 59))



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

  //i64 22
  val const7 = Module(new ConstFastNode(value = 22, ID = 7))

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

  //i64 18
  val const19 = Module(new ConstFastNode(value = 18, ID = 19))

  //i64 1
  val const20 = Module(new ConstFastNode(value = 1, ID = 20))

  //i64 24
  val const21 = Module(new ConstFastNode(value = 24, ID = 21))

  //i64 1
  val const22 = Module(new ConstFastNode(value = 1, ID = 22))

  //i64 16
  val const23 = Module(new ConstFastNode(value = 16, ID = 23))

  //double 0.000000e+00
  val constf0 = Module(new ConstNode(value = 0x0, ID = 0))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_1.io.predicateIn(1) <> Loop_5.io.activate_loop_start

  bb_1.io.predicateIn(0) <> Loop_5.io.activate_loop_back

  bb_2.io.predicateIn(1) <> Loop_4.io.activate_loop_start

  bb_2.io.predicateIn(0) <> Loop_4.io.activate_loop_back

  bb_3.io.predicateIn(1) <> Loop_3.io.activate_loop_start

  bb_3.io.predicateIn(0) <> Loop_3.io.activate_loop_back

  bb_4.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_5.io.predicateIn(0) <> Loop_4.io.loopExit(0)

  bb_6.io.predicateIn(0) <> Loop_5.io.loopExit(0)

  bb_7.io.predicateIn(0) <> Loop_2.io.activate_loop_start

  bb_7.io.predicateIn(1) <> Loop_2.io.activate_loop_back

  bb_8.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_8.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_9.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_9.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_10.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_11.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_12.io.predicateIn(0) <> Loop_2.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_38.io.Out(0)

  Loop_0.io.loopBack(0) <> br_52.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_52.io.TrueOutput(0)

  Loop_1.io.enable <> br_31.io.Out(0)

  Loop_1.io.loopBack(0) <> br_55.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_55.io.TrueOutput(0)

  Loop_2.io.enable <> br_29.io.Out(0)

  Loop_2.io.loopBack(0) <> br_58.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_58.io.TrueOutput(0)

  Loop_3.io.enable <> br_7.io.Out(0)

  Loop_3.io.loopBack(0) <> br_22.io.FalseOutput(0)

  Loop_3.io.loopFinish(0) <> br_22.io.TrueOutput(0)

  Loop_4.io.enable <> br_2.io.Out(0)

  Loop_4.io.loopBack(0) <> br_25.io.FalseOutput(0)

  Loop_4.io.loopFinish(0) <> br_25.io.TrueOutput(0)

  Loop_5.io.enable <> br_0.io.Out(0)

  Loop_5.io.loopBack(0) <> br_28.io.FalseOutput(0)

  Loop_5.io.loopFinish(0) <> br_28.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> phi32.io.Out(0)

  Loop_0.io.InLiveIn(1) <> Gep_tmp734.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Loop_1.io.InLiveIn(0) <> phi30.io.Out(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field3")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(4) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_2.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field6")(0)

  Loop_2.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_2.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_2.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field5")(0)

  Loop_3.io.InLiveIn(0) <> phi3.io.Out(0)

  Loop_3.io.InLiveIn(1) <> Gep_tmp15.io.Out(0)

  Loop_3.io.InLiveIn(2) <> Loop_4.io.OutLiveIn.elements("field1")(0)

  Loop_3.io.InLiveIn(3) <> Loop_4.io.OutLiveIn.elements("field2")(0)

  Loop_3.io.InLiveIn(4) <> Loop_4.io.OutLiveIn.elements("field3")(0)

  Loop_3.io.InLiveIn(5) <> Loop_4.io.OutLiveIn.elements("field0")(0)

  Loop_4.io.InLiveIn(0) <> phi1.io.Out(0)

  Loop_4.io.InLiveIn(1) <> Loop_5.io.OutLiveIn.elements("field2")(0)

  Loop_4.io.InLiveIn(2) <> Loop_5.io.OutLiveIn.elements("field1")(0)

  Loop_4.io.InLiveIn(3) <> Loop_5.io.OutLiveIn.elements("field3")(0)

  Loop_4.io.InLiveIn(4) <> Loop_5.io.OutLiveIn.elements("field0")(0)

  Loop_5.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field2")(1)

  Loop_5.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_5.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_5.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field4")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_tmp1144.io.idx(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  ld_47.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(0)

  st_49.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field1")(1)

  Gep_tmp840.io.idx(0) <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_tmp840.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  Gep_tmp1043.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field4")(0)

  Gep_tmp633.io.idx(0) <> Loop_1.io.OutLiveIn.elements("field0")(1)

  FP_36.io.RightIO <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Gep_tmp633.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field4")(0)

  Gep_tmp514.io.idx(1) <> Loop_3.io.OutLiveIn.elements("field0")(0)

  ld_17.io.GepAddr <> Loop_3.io.OutLiveIn.elements("field1")(0)

  st_19.io.GepAddr <> Loop_3.io.OutLiveIn.elements("field1")(1)

  FP_12.io.RightIO <> Loop_3.io.OutLiveIn.elements("field2")(0)

  Gep_tmp29.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field3")(0)

  Gep_tmp413.io.baseAddress <> Loop_3.io.OutLiveIn.elements("field4")(0)

  Gep_tmp29.io.idx(0) <> Loop_3.io.OutLiveIn.elements("field5")(0)

  Gep_tmp4.io.idx(0) <> Loop_4.io.OutLiveIn.elements("field0")(1)

  Gep_tmp4.io.baseAddress <> Loop_4.io.OutLiveIn.elements("field4")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_50.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_53.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_56.io.Out(0)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_20.io.Out(0)

  Loop_4.io.CarryDepenIn(0) <> binaryOp_23.io.Out(0)

  Loop_5.io.CarryDepenIn(0) <> binaryOp_26.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi39.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi32.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phi30.io.InData(0) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phi8.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phi3.io.InData(1) <> Loop_4.io.CarryDepenOut.elements("field0")(0)

  phi1.io.InData(1) <> Loop_5.io.CarryDepenOut.elements("field0")(0)



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


  FP_12.io.enable <> bb_3.io.Out(9)


  Gep_tmp413.io.enable <> bb_3.io.Out(10)


  Gep_tmp514.io.enable <> bb_3.io.Out(11)


  ld_15.io.enable <> bb_3.io.Out(12)


  FP_16.io.enable <> bb_3.io.Out(13)


  ld_17.io.enable <> bb_3.io.Out(14)


  FP_18.io.enable <> bb_3.io.Out(15)


  st_19.io.enable <> bb_3.io.Out(16)


  binaryOp_20.io.enable <> bb_3.io.Out(17)


  icmp_21.io.enable <> bb_3.io.Out(18)


  br_22.io.enable <> bb_3.io.Out(19)


  const8.io.enable <> bb_4.io.Out(0)

  const9.io.enable <> bb_4.io.Out(1)

  binaryOp_23.io.enable <> bb_4.io.Out(2)


  icmp_24.io.enable <> bb_4.io.Out(3)


  br_25.io.enable <> bb_4.io.Out(4)


  const10.io.enable <> bb_5.io.Out(0)

  const11.io.enable <> bb_5.io.Out(1)

  binaryOp_26.io.enable <> bb_5.io.Out(2)


  icmp_27.io.enable <> bb_5.io.Out(3)


  br_28.io.enable <> bb_5.io.Out(4)


  br_29.io.enable <> bb_6.io.Out(0)


  const12.io.enable <> bb_7.io.Out(0)

  phi30.io.enable <> bb_7.io.Out(1)


  br_31.io.enable <> bb_7.io.Out(2)


  const13.io.enable <> bb_8.io.Out(0)

  const14.io.enable <> bb_8.io.Out(1)

  phi32.io.enable <> bb_8.io.Out(2)


  Gep_tmp633.io.enable <> bb_8.io.Out(3)


  Gep_tmp734.io.enable <> bb_8.io.Out(4)


  ld_35.io.enable <> bb_8.io.Out(5)


  FP_36.io.enable <> bb_8.io.Out(6)


  st_37.io.enable <> bb_8.io.Out(7)


  br_38.io.enable <> bb_8.io.Out(8)


  const15.io.enable <> bb_9.io.Out(0)

  const16.io.enable <> bb_9.io.Out(1)

  const17.io.enable <> bb_9.io.Out(2)

  const18.io.enable <> bb_9.io.Out(3)

  const19.io.enable <> bb_9.io.Out(4)

  phi39.io.enable <> bb_9.io.Out(5)


  Gep_tmp840.io.enable <> bb_9.io.Out(6)


  Gep_tmp941.io.enable <> bb_9.io.Out(7)


  ld_42.io.enable <> bb_9.io.Out(8)


  Gep_tmp1043.io.enable <> bb_9.io.Out(9)


  Gep_tmp1144.io.enable <> bb_9.io.Out(10)


  ld_45.io.enable <> bb_9.io.Out(11)


  FP_46.io.enable <> bb_9.io.Out(12)


  ld_47.io.enable <> bb_9.io.Out(13)


  FP_48.io.enable <> bb_9.io.Out(14)


  st_49.io.enable <> bb_9.io.Out(15)


  binaryOp_50.io.enable <> bb_9.io.Out(16)


  icmp_51.io.enable <> bb_9.io.Out(17)


  br_52.io.enable <> bb_9.io.Out(18)


  const20.io.enable <> bb_10.io.Out(0)

  const21.io.enable <> bb_10.io.Out(1)

  binaryOp_53.io.enable <> bb_10.io.Out(2)


  icmp_54.io.enable <> bb_10.io.Out(3)


  br_55.io.enable <> bb_10.io.Out(4)


  const22.io.enable <> bb_11.io.Out(0)

  const23.io.enable <> bb_11.io.Out(1)

  binaryOp_56.io.enable <> bb_11.io.Out(2)


  icmp_57.io.enable <> bb_11.io.Out(3)


  br_58.io.enable <> bb_11.io.Out(4)


  ret_59.io.In.enable <> bb_12.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi3.io.Mask <> bb_2.io.MaskBB(0)

  phi8.io.Mask <> bb_3.io.MaskBB(0)

  phi30.io.Mask <> bb_7.io.MaskBB(0)

  phi32.io.Mask <> bb_8.io.MaskBB(0)

  phi39.io.Mask <> bb_9.io.MaskBB(0)



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

  MemCtrl.io.ReadIn(1) <> ld_15.io.memReq

  ld_15.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_17.io.memReq

  ld_17.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(1) <> st_19.io.memReq

  st_19.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.ReadIn(3) <> ld_35.io.memReq

  ld_35.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.WriteIn(2) <> st_37.io.memReq

  st_37.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.ReadIn(4) <> ld_42.io.memReq

  ld_42.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_45.io.memReq

  ld_45.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.ReadIn(6) <> ld_47.io.memReq

  ld_47.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.WriteIn(3) <> st_49.io.memReq

  st_49.io.memResp <> MemCtrl.io.WriteOut(3)



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

  Gep_tmp514.io.idx(0) <> const5.io.Out

  binaryOp_20.io.RightIO <> const6.io.Out

  icmp_21.io.RightIO <> const7.io.Out

  binaryOp_23.io.RightIO <> const8.io.Out

  icmp_24.io.RightIO <> const9.io.Out

  binaryOp_26.io.RightIO <> const10.io.Out

  icmp_27.io.RightIO <> const11.io.Out

  phi30.io.InData(1) <> const12.io.Out

  phi32.io.InData(0) <> const13.io.Out

  Gep_tmp734.io.idx(0) <> const14.io.Out

  phi39.io.InData(0) <> const15.io.Out

  Gep_tmp941.io.idx(0) <> const16.io.Out

  Gep_tmp1144.io.idx(0) <> const17.io.Out

  binaryOp_50.io.RightIO <> const18.io.Out

  icmp_51.io.RightIO <> const19.io.Out

  binaryOp_53.io.RightIO <> const20.io.Out

  icmp_54.io.RightIO <> const21.io.Out

  binaryOp_56.io.RightIO <> const22.io.Out

  icmp_57.io.RightIO <> const23.io.Out

  st_6.io.inData <> constf0.io.Out(0)

  binaryOp_26.io.LeftIO <> phi1.io.Out(1)

  Gep_tmp15.io.idx(1) <> phi3.io.Out(1)

  binaryOp_23.io.LeftIO <> phi3.io.Out(2)

  Gep_tmp15.io.baseAddress <> Gep_tmp4.io.Out(0)

  st_6.io.GepAddr <> Gep_tmp15.io.Out(1)

  Gep_tmp310.io.idx(1) <> phi8.io.Out(0)

  Gep_tmp413.io.idx(0) <> phi8.io.Out(1)

  binaryOp_20.io.LeftIO <> phi8.io.Out(2)

  Gep_tmp310.io.baseAddress <> Gep_tmp29.io.Out(0)

  ld_11.io.GepAddr <> Gep_tmp310.io.Out(0)

  FP_12.io.LeftIO <> ld_11.io.Out(0)

  FP_16.io.LeftIO <> FP_12.io.Out(0)

  Gep_tmp514.io.baseAddress <> Gep_tmp413.io.Out(0)

  ld_15.io.GepAddr <> Gep_tmp514.io.Out(0)

  FP_16.io.RightIO <> ld_15.io.Out(0)

  FP_18.io.RightIO <> FP_16.io.Out(0)

  FP_18.io.LeftIO <> ld_17.io.Out(0)

  st_19.io.inData <> FP_18.io.Out(0)

  icmp_21.io.LeftIO <> binaryOp_20.io.Out(1)

  br_22.io.CmpIO <> icmp_21.io.Out(0)

  icmp_24.io.LeftIO <> binaryOp_23.io.Out(1)

  br_25.io.CmpIO <> icmp_24.io.Out(0)

  icmp_27.io.LeftIO <> binaryOp_26.io.Out(1)

  br_28.io.CmpIO <> icmp_27.io.Out(0)

  binaryOp_56.io.LeftIO <> phi30.io.Out(1)

  Gep_tmp734.io.idx(1) <> phi32.io.Out(1)

  binaryOp_53.io.LeftIO <> phi32.io.Out(2)

  Gep_tmp734.io.baseAddress <> Gep_tmp633.io.Out(0)

  ld_35.io.GepAddr <> Gep_tmp734.io.Out(1)

  st_37.io.GepAddr <> Gep_tmp734.io.Out(2)

  FP_36.io.LeftIO <> ld_35.io.Out(0)

  st_37.io.inData <> FP_36.io.Out(0)

  Gep_tmp941.io.idx(1) <> phi39.io.Out(0)

  Gep_tmp1043.io.idx(0) <> phi39.io.Out(1)

  binaryOp_50.io.LeftIO <> phi39.io.Out(2)

  Gep_tmp941.io.baseAddress <> Gep_tmp840.io.Out(0)

  ld_42.io.GepAddr <> Gep_tmp941.io.Out(0)

  FP_46.io.LeftIO <> ld_42.io.Out(0)

  Gep_tmp1144.io.baseAddress <> Gep_tmp1043.io.Out(0)

  ld_45.io.GepAddr <> Gep_tmp1144.io.Out(0)

  FP_46.io.RightIO <> ld_45.io.Out(0)

  FP_48.io.RightIO <> FP_46.io.Out(0)

  FP_48.io.LeftIO <> ld_47.io.Out(0)

  st_49.io.inData <> FP_48.io.Out(0)

  icmp_51.io.LeftIO <> binaryOp_50.io.Out(1)

  br_52.io.CmpIO <> icmp_51.io.Out(0)

  icmp_54.io.LeftIO <> binaryOp_53.io.Out(1)

  br_55.io.CmpIO <> icmp_54.io.Out(0)

  icmp_57.io.LeftIO <> binaryOp_56.io.Out(1)

  br_58.io.CmpIO <> icmp_57.io.Out(0)

  st_6.io.Out(0).ready := true.B

  st_19.io.Out(0).ready := true.B

  st_37.io.Out(0).ready := true.B

  st_49.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_59.io.Out

}

import java.io.{File, FileWriter}

object k2mmTop extends App {
  val dir = new File("RTL/k2mmTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new k2mmDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
