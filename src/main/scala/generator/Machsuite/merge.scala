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

abstract class mergeDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class mergeDF(implicit p: Parameters) extends mergeDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 3)
  (WControl = new WriteMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val StackPointer = Module(new Stack(NumOps = 1))

  val InputSplitter = Module(new SplitCallNew(List(3, 8, 6, 6)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 2, 1, 1), NumOuts = List(), NumCarry = List(1, 1, 1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 0))

  val bb_1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 15, BID = 1))

  val bb_2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 2, BID = 2))

  val bb_3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 6, BID = 3))

  val bb_4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 13, NumPhi = 1, BID = 4))

  val bb_loopexit1_loopexit5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 5))

  val bb_loopexit16 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 2, BID = 6))

  val bb_7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 7))

  val bb_8 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 14, NumPhi = 3, BID = 8))

  val bb_9 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 9))

  val bb_10 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 10))

  val bb_11 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 6, NumPhi = 2, BID = 11))

  val bb_loopexit_loopexit12 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 12))

  val bb_loopexit13 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 1, BID = 13))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %5 = alloca [2048 x i64], align 16, !UID !78
  val alloca_0 = Module(new AllocaNode(NumOuts=4, ID = 0, RouteID=0))

  //  %6 = bitcast [2048 x i64]* %5 to i8*, !dbg !83, !UID !84
  val bitcast_1 = Module(new BitCastNode(NumOuts = 1, ID = 1))

  //  %7 = icmp sgt i32 %1, %2, !dbg !87, !UID !90
  val icmp_2 = Module(new IcmpNode(NumOuts = 1, ID = 2, opCode = "ugt")(sign = false))

  //  br i1 %7, label %20, label %8, !dbg !91, !UID !92, !BB_UID !93
  val br_3 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 3))

  //  %9 = sext i32 %1 to i64, !dbg !91, !UID !94
  val sext4 = Module(new SextNode(NumOuts = 2))

  //  %10 = getelementptr [2048 x i64], [2048 x i64]* %5, i64 0, i64 %9, !dbg !91, !UID !95
  val Gep_5 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 5)(ElementSize = 8, ArraySize = List(16384)))

  //  %11 = bitcast i64* %10 to i8*, !UID !96
  val bitcast_6 = Module(new BitCastNode(NumOuts = 0, ID = 6))

  //  %12 = getelementptr i64, i64* %0, i64 %9, !dbg !91, !UID !97
  val Gep_7 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 7)(ElementSize = 8, ArraySize = List()))

  //  %13 = bitcast i64* %12 to i8*, !UID !98
  val bitcast_8 = Module(new BitCastNode(NumOuts = 0, ID = 8))

  //  %14 = icmp sgt i32 %2, %1, !dbg !91, !UID !99
  val icmp_9 = Module(new IcmpNode(NumOuts = 1, ID = 9, opCode = "ugt")(sign = false))

  //  %15 = select i1 %14, i32 %2, i32 %1, !dbg !91, !UID !100
  val select_10 = Module(new SelectNode(NumOuts = 1, ID = 10))

  //  %16 = sub i32 %15, %1, !dbg !91, !UID !101
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "sub")(sign = false))

  //  %17 = zext i32 %16 to i64, !dbg !91, !UID !102
  val sext12 = Module(new ZextNode(NumOuts = 1))

  //  %18 = shl nuw nsw i64 %17, 3, !dbg !91, !UID !103
  val binaryOp_13 = Module(new ComputeNode(NumOuts = 1, ID = 13, opCode = "shl")(sign = false))

  //  %19 = add nuw nsw i64 %18, 8, !dbg !91, !UID !104
  val binaryOp_14 = Module(new ComputeNode(NumOuts = 0, ID = 14, opCode = "add")(sign = false))

  //  br label %20, !dbg !107, !UID !109, !BB_UID !110
  val br_15 = Module(new UBranchNode(ID = 15))

  //  %21 = icmp slt i32 %2, %3, !dbg !112, !UID !114
  val icmp_16 = Module(new IcmpNode(NumOuts = 1, ID = 16, opCode = "ult")(sign = false))

  //  br i1 %21, label %22, label %.loopexit1, !dbg !115, !UID !116, !BB_UID !117
  val br_17 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 17))

  //  %23 = add nsw i32 %2, 1, !dbg !107, !UID !118
  val binaryOp_18 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "add")(sign = false))

  //  %24 = add nsw i32 %23, %3, !UID !119
  val binaryOp_19 = Module(new ComputeNode(NumOuts = 1, ID = 19, opCode = "add")(sign = false))

  //  %25 = sext i32 %2 to i64, !dbg !115, !UID !120
  val sext20 = Module(new SextNode(NumOuts = 1))

  //  %26 = sext i32 %3 to i64, !dbg !115, !UID !121
  val sext21 = Module(new SextNode(NumOuts = 1))

  //  br label %27, !dbg !115, !UID !122, !BB_UID !123
  val br_22 = Module(new UBranchNode(ID = 22))

  //  %28 = phi i64 [ %29, %27 ], [ %25, %22 ], !UID !124
  val phi23 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 23, Res = false))

  //  %29 = add i64 %28, 1, !UID !125
  val binaryOp_24 = Module(new ComputeNode(NumOuts = 4, ID = 24, opCode = "add")(sign = false))

  //  %30 = getelementptr inbounds i64, i64* %0, i64 %29, !dbg !126, !UID !128
  val Gep_25 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 25)(ElementSize = 8, ArraySize = List()))

  //  %31 = load i64, i64* %30, align 8, !dbg !126, !tbaa !129, !UID !133
  val ld_26 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 26, RouteID = 0))

  //  %32 = trunc i64 %29 to i32, !dbg !134, !UID !135
  val trunc27 = Module(new TruncNode(NumOuts = 1))

  //  %33 = sub i32 %24, %32, !dbg !134, !UID !136
  val binaryOp_28 = Module(new ComputeNode(NumOuts = 1, ID = 28, opCode = "sub")(sign = false))

  //  %34 = sext i32 %33 to i64, !dbg !137, !UID !138
  val sext29 = Module(new SextNode(NumOuts = 1))

  //  %35 = getelementptr inbounds [2048 x i64], [2048 x i64]* %5, i64 0, i64 %34, !dbg !137, !UID !139
  val Gep_30 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 30)(ElementSize = 8, ArraySize = List(16384)))

  //  store i64 %31, i64* %35, align 8, !dbg !140, !tbaa !129, !UID !141
  val st_31 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 31, RouteID = 0))

  //  %36 = icmp slt i64 %29, %26, !dbg !112, !UID !142
  val icmp_32 = Module(new IcmpNode(NumOuts = 1, ID = 32, opCode = "ult")(sign = false))

  //  br i1 %36, label %27, label %.loopexit1.loopexit, !dbg !115, !llvm.loop !143, !UID !145, !BB_UID !146
  val br_33 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 33))

  //  br label %.loopexit1, !dbg !147
  val br_34 = Module(new UBranchNode(ID = 34))

  //  %37 = icmp sgt i32 %1, %3, !dbg !147, !UID !149
  val icmp_35 = Module(new IcmpNode(NumOuts = 1, ID = 35, opCode = "ugt")(sign = false))

  //  br i1 %37, label %.loopexit, label %38, !dbg !150, !UID !151, !BB_UID !152
  val br_36 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 36))

  //  %39 = sext i32 %1 to i64, !dbg !150, !UID !153
  val sext37 = Module(new SextNode(NumOuts = 1))

  //  %40 = sext i32 %3 to i64, !dbg !150, !UID !154
  val sext38 = Module(new SextNode(NumOuts = 1))

  //  br label %41, !dbg !150, !UID !155, !BB_UID !156
  val br_39 = Module(new UBranchNode(ID = 39))

  //  %42 = phi i64 [ %60, %57 ], [ %39, %38 ], !UID !157
  val phi40 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 40, Res = false))

  //  %43 = phi i32 [ %59, %57 ], [ %1, %38 ], !UID !158
  val phi41 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 41, Res = false))

  //  %44 = phi i32 [ %58, %57 ], [ %3, %38 ], !UID !159
  val phi42 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 42, Res = false))

  //  %45 = sext i32 %44 to i64, !dbg !160, !UID !161
  val sext43 = Module(new SextNode(NumOuts = 1))

  //  %46 = getelementptr inbounds [2048 x i64], [2048 x i64]* %5, i64 0, i64 %45, !dbg !160, !UID !162
  val Gep_44 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 44)(ElementSize = 8, ArraySize = List(16384)))

  //  %47 = load i64, i64* %46, align 8, !dbg !160, !tbaa !129, !UID !163
  val ld_45 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 45, RouteID = 1))

  //  %48 = sext i32 %43 to i64, !dbg !165, !UID !166
  val sext46 = Module(new SextNode(NumOuts = 1))

  //  %49 = getelementptr inbounds [2048 x i64], [2048 x i64]* %5, i64 0, i64 %48, !dbg !165, !UID !167
  val Gep_47 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 47)(ElementSize = 8, ArraySize = List(16384)))

  //  %50 = load i64, i64* %49, align 8, !dbg !165, !tbaa !129, !UID !168
  val ld_48 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 48, RouteID = 2))

  //  %51 = icmp ult i64 %47, %50, !dbg !170, !UID !172
  val icmp_49 = Module(new IcmpNode(NumOuts = 1, ID = 49, opCode = "ult")(sign = false))

  //  %52 = getelementptr inbounds i64, i64* %0, i64 %42, !UID !173
  val Gep_50 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 50)(ElementSize = 8, ArraySize = List()))

  //  br i1 %51, label %53, label %55, !dbg !174, !UID !175, !BB_UID !176
  val br_51 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 51))

  //  store i64 %47, i64* %52, align 8, !dbg !177, !tbaa !129, !UID !179
  val st_52 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 52, RouteID = 1))

  //  %54 = add nsw i32 %44, -1, !dbg !180, !UID !181
  val binaryOp_53 = Module(new ComputeNode(NumOuts = 1, ID = 53, opCode = "add")(sign = false))

  //  br label %57, !dbg !182, !UID !183, !BB_UID !184
  val br_54 = Module(new UBranchNode(ID = 54))

  //  store i64 %50, i64* %52, align 8, !dbg !185, !tbaa !129, !UID !187
  val st_55 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 55, RouteID = 2))

  //  %56 = add nsw i32 %43, 1, !dbg !188, !UID !189
  val binaryOp_56 = Module(new ComputeNode(NumOuts = 1, ID = 56, opCode = "add")(sign = false))

  //  br label %57, !UID !190, !BB_UID !191
  val br_57 = Module(new UBranchNode(ID = 57))

  //  %58 = phi i32 [ %54, %53 ], [ %44, %55 ], !UID !192
  val phi58 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 58, Res = true))

  //  %59 = phi i32 [ %43, %53 ], [ %56, %55 ], !UID !193
  val phi59 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 59, Res = true))

  //  %60 = add nsw i64 %42, 1, !dbg !194, !UID !195
  val binaryOp_60 = Module(new ComputeNode(NumOuts = 1, ID = 60, opCode = "add")(sign = false))

  //  %61 = icmp slt i64 %42, %40, !dbg !147, !UID !196
  val icmp_61 = Module(new IcmpNode(NumOuts = 1, ID = 61, opCode = "ult")(sign = false))

  //  br i1 %61, label %41, label %.loopexit.loopexit, !dbg !150, !llvm.loop !197, !UID !199, !BB_UID !200
  val br_62 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 62))

  //  br label %.loopexit, !dbg !201
  val br_63 = Module(new UBranchNode(ID = 63))

  //  ret void, !dbg !201, !UID !202, !BB_UID !203
  val ret_64 = Module(new RetNode2(retTypes = List(), ID = 64))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i64 3
  val const1 = Module(new ConstFastNode(value = 3, ID = 1))

  //i64 8
  val const2 = Module(new ConstFastNode(value = 8, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))

  //i64 1
  val const4 = Module(new ConstFastNode(value = 1, ID = 4))

  //i64 0
  val const5 = Module(new ConstFastNode(value = 0, ID = 5))

  //i64 0
  val const6 = Module(new ConstFastNode(value = 0, ID = 6))

  //i64 0
  val const7 = Module(new ConstFastNode(value = 0, ID = 7))

  //i32 -1
  val const8 = Module(new ConstFastNode(value = -1, ID = 8))

  //i32 1
  val const9 = Module(new ConstFastNode(value = 1, ID = 9))

  //i64 1
  val const10 = Module(new ConstFastNode(value = 1, ID = 10))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_1.io.predicateIn(0) <> br_3.io.FalseOutput(0)

  bb_2.io.predicateIn(1) <> br_3.io.TrueOutput(0)

  bb_2.io.predicateIn(0) <> br_15.io.Out(0)

  bb_3.io.predicateIn(0) <> br_17.io.TrueOutput(0)

  bb_loopexit16.io.predicateIn(1) <> br_17.io.FalseOutput(0)

  bb_loopexit16.io.predicateIn(0) <> br_34.io.Out(0)

  bb_7.io.predicateIn(0) <> br_36.io.FalseOutput(0)

  bb_9.io.predicateIn(0) <> br_51.io.TrueOutput(0)

  bb_10.io.predicateIn(0) <> br_51.io.FalseOutput(0)

  bb_11.io.predicateIn(1) <> br_54.io.Out(0)

  bb_11.io.predicateIn(0) <> br_57.io.Out(0)

  bb_loopexit13.io.predicateIn(1) <> br_36.io.TrueOutput(0)

  bb_loopexit13.io.predicateIn(0) <> br_63.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_4.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_4.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_loopexit1_loopexit5.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_8.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_8.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_loopexit_loopexit12.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_39.io.Out(0)

  Loop_0.io.loopBack(0) <> br_62.io.TrueOutput(0)

  Loop_0.io.loopFinish(0) <> br_62.io.FalseOutput(0)

  Loop_1.io.enable <> br_22.io.Out(0)

  Loop_1.io.loopBack(0) <> br_33.io.TrueOutput(0)

  Loop_1.io.loopFinish(0) <> br_33.io.FalseOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> sext37.io.Out(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_0.io.InLiveIn(3) <> alloca_0.io.Out(0)

  Loop_0.io.InLiveIn(4) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(5) <> sext38.io.Out(0)

  Loop_1.io.InLiveIn(0) <> sext20.io.Out(0)

  Loop_1.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field0")(1)

  Loop_1.io.InLiveIn(2) <> binaryOp_19.io.Out(0)

  Loop_1.io.InLiveIn(3) <> alloca_0.io.Out(1)

  Loop_1.io.InLiveIn(4) <> sext21.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phi40.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  phi41.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field1")(0)

  phi42.io.InData(1) <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_44.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  Gep_47.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(1)

  Gep_50.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field4")(0)

  icmp_61.io.RightIO <> Loop_0.io.OutLiveIn.elements("field5")(0)

  phi23.io.InData(1) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Gep_25.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field1")(0)

  binaryOp_28.io.LeftIO <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Gep_30.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field3")(0)

  icmp_32.io.RightIO <> Loop_1.io.OutLiveIn.elements("field4")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> phi59.io.Out(0)

  Loop_0.io.CarryDepenIn(1) <> phi58.io.Out(0)

  Loop_0.io.CarryDepenIn(2) <> binaryOp_60.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_24.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi41.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi42.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field1")(0)

  phi40.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field2")(0)

  phi23.io.InData(0) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  alloca_0.io.enable <> bb_0.io.Out(0)


  bitcast_1.io.enable <> bb_0.io.Out(1)


  icmp_2.io.enable <> bb_0.io.Out(2)


  br_3.io.enable <> bb_0.io.Out(3)


  const0.io.enable <> bb_1.io.Out(0)

  const1.io.enable <> bb_1.io.Out(1)

  const2.io.enable <> bb_1.io.Out(2)

  sext4.io.enable <> bb_1.io.Out(3)


  Gep_5.io.enable <> bb_1.io.Out(4)


  bitcast_6.io.enable <> bb_1.io.Out(5)


  Gep_7.io.enable <> bb_1.io.Out(6)


  bitcast_8.io.enable <> bb_1.io.Out(7)


  icmp_9.io.enable <> bb_1.io.Out(8)


  select_10.io.enable <> bb_1.io.Out(9)


  binaryOp_11.io.enable <> bb_1.io.Out(10)


  sext12.io.enable <> bb_1.io.Out(11)


  binaryOp_13.io.enable <> bb_1.io.Out(12)


  binaryOp_14.io.enable <> bb_1.io.Out(13)


  br_15.io.enable <> bb_1.io.Out(14)


  icmp_16.io.enable <> bb_2.io.Out(0)


  br_17.io.enable <> bb_2.io.Out(1)


  const3.io.enable <> bb_3.io.Out(0)

  binaryOp_18.io.enable <> bb_3.io.Out(1)


  binaryOp_19.io.enable <> bb_3.io.Out(2)


  sext20.io.enable <> bb_3.io.Out(3)


  sext21.io.enable <> bb_3.io.Out(4)


  br_22.io.enable <> bb_3.io.Out(5)


  const4.io.enable <> bb_4.io.Out(0)

  const5.io.enable <> bb_4.io.Out(1)

  phi23.io.enable <> bb_4.io.Out(2)


  binaryOp_24.io.enable <> bb_4.io.Out(3)


  Gep_25.io.enable <> bb_4.io.Out(4)


  ld_26.io.enable <> bb_4.io.Out(5)


  trunc27.io.enable <> bb_4.io.Out(6)


  binaryOp_28.io.enable <> bb_4.io.Out(7)


  sext29.io.enable <> bb_4.io.Out(8)


  Gep_30.io.enable <> bb_4.io.Out(9)


  st_31.io.enable <> bb_4.io.Out(10)


  icmp_32.io.enable <> bb_4.io.Out(11)


  br_33.io.enable <> bb_4.io.Out(12)


  br_34.io.enable <> bb_loopexit1_loopexit5.io.Out(0)


  icmp_35.io.enable <> bb_loopexit16.io.Out(0)


  br_36.io.enable <> bb_loopexit16.io.Out(1)


  sext37.io.enable <> bb_7.io.Out(0)


  sext38.io.enable <> bb_7.io.Out(1)


  br_39.io.enable <> bb_7.io.Out(2)


  const6.io.enable <> bb_8.io.Out(0)

  const7.io.enable <> bb_8.io.Out(1)

  phi40.io.enable <> bb_8.io.Out(2)


  phi41.io.enable <> bb_8.io.Out(3)


  phi42.io.enable <> bb_8.io.Out(4)


  sext43.io.enable <> bb_8.io.Out(5)


  Gep_44.io.enable <> bb_8.io.Out(6)


  ld_45.io.enable <> bb_8.io.Out(7)


  sext46.io.enable <> bb_8.io.Out(8)


  Gep_47.io.enable <> bb_8.io.Out(9)


  ld_48.io.enable <> bb_8.io.Out(10)


  icmp_49.io.enable <> bb_8.io.Out(11)


  Gep_50.io.enable <> bb_8.io.Out(12)


  br_51.io.enable <> bb_8.io.Out(13)


  const8.io.enable <> bb_9.io.Out(0)

  st_52.io.enable <> bb_9.io.Out(1)


  binaryOp_53.io.enable <> bb_9.io.Out(2)


  br_54.io.enable <> bb_9.io.Out(3)


  const9.io.enable <> bb_10.io.Out(0)

  st_55.io.enable <> bb_10.io.Out(1)


  binaryOp_56.io.enable <> bb_10.io.Out(2)


  br_57.io.enable <> bb_10.io.Out(3)


  const10.io.enable <> bb_11.io.Out(0)

  phi58.io.enable <> bb_11.io.Out(1)


  phi59.io.enable <> bb_11.io.Out(2)


  binaryOp_60.io.enable <> bb_11.io.Out(3)


  icmp_61.io.enable <> bb_11.io.Out(4)


  br_62.io.enable <> bb_11.io.Out(5)


  br_63.io.enable <> bb_loopexit_loopexit12.io.Out(0)


  ret_64.io.In.enable <> bb_loopexit13.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi23.io.Mask <> bb_4.io.MaskBB(0)

  phi40.io.Mask <> bb_8.io.MaskBB(0)

  phi41.io.Mask <> bb_8.io.MaskBB(1)

  phi42.io.Mask <> bb_8.io.MaskBB(2)

  phi58.io.Mask <> bb_11.io.MaskBB(0)

  phi59.io.Mask <> bb_11.io.MaskBB(1)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */

  alloca_0.io.allocaInputIO.bits.size      := 1.U
  alloca_0.io.allocaInputIO.bits.numByte   := 16384.U
  alloca_0.io.allocaInputIO.bits.predicate := true.B
  alloca_0.io.allocaInputIO.bits.valid     := true.B
  alloca_0.io.allocaInputIO.valid          := true.B





  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  StackPointer.io.InData(0) <> alloca_0.io.allocaReqIO

  alloca_0.io.allocaRespIO <> StackPointer.io.OutData(0)

  MemCtrl.io.ReadIn(0) <> ld_26.io.memReq

  ld_26.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> st_31.io.memReq

  st_31.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(1) <> ld_45.io.memReq

  ld_45.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_48.io.memReq

  ld_48.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(1) <> st_52.io.memReq

  st_52.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_55.io.memReq

  st_55.io.memResp <> MemCtrl.io.WriteOut(2)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_5.io.idx(0) <> const0.io.Out

  binaryOp_13.io.RightIO <> const1.io.Out

  binaryOp_14.io.RightIO <> const2.io.Out

  binaryOp_18.io.RightIO <> const3.io.Out

  binaryOp_24.io.RightIO <> const4.io.Out

  Gep_30.io.idx(0) <> const5.io.Out

  Gep_44.io.idx(0) <> const6.io.Out

  Gep_47.io.idx(0) <> const7.io.Out

  binaryOp_53.io.RightIO <> const8.io.Out

  binaryOp_56.io.RightIO <> const9.io.Out

  binaryOp_60.io.RightIO <> const10.io.Out

  bitcast_1.io.Input <> alloca_0.io.Out(2)

  bitcast_1.io.Out(0).ready := true.B

  Gep_5.io.baseAddress <> alloca_0.io.Out(3)

  br_3.io.CmpIO <> icmp_2.io.Out(0)

  Gep_5.io.idx(1) <> sext4.io.Out(0)

  Gep_7.io.idx(0) <> sext4.io.Out(1)

  bitcast_6.io.Input <> Gep_5.io.Out(0)

  bitcast_8.io.Input <> Gep_7.io.Out(0)

  select_10.io.Select <> icmp_9.io.Out(0)

  binaryOp_11.io.LeftIO <> select_10.io.Out(0)

  sext12.io.Input <> binaryOp_11.io.Out(0)

  binaryOp_13.io.LeftIO <> sext12.io.Out(0)

  binaryOp_14.io.LeftIO <> binaryOp_13.io.Out(0)

  br_17.io.CmpIO <> icmp_16.io.Out(0)

  binaryOp_19.io.LeftIO <> binaryOp_18.io.Out(0)

  binaryOp_24.io.LeftIO <> phi23.io.Out(0)

  Gep_25.io.idx(0) <> binaryOp_24.io.Out(1)

  trunc27.io.Input <> binaryOp_24.io.Out(2)

  icmp_32.io.LeftIO <> binaryOp_24.io.Out(3)

  ld_26.io.GepAddr <> Gep_25.io.Out(0)

  st_31.io.inData <> ld_26.io.Out(0)

  binaryOp_28.io.RightIO <> trunc27.io.Out(0)

  sext29.io.Input <> binaryOp_28.io.Out(0)

  Gep_30.io.idx(1) <> sext29.io.Out(0)

  st_31.io.GepAddr <> Gep_30.io.Out(0)

  br_33.io.CmpIO <> icmp_32.io.Out(0)

  br_36.io.CmpIO <> icmp_35.io.Out(0)

  Gep_50.io.idx(0) <> phi40.io.Out(0)

  binaryOp_60.io.LeftIO <> phi40.io.Out(1)

  icmp_61.io.LeftIO <> phi40.io.Out(2)

  sext46.io.Input <> phi41.io.Out(0)

  binaryOp_56.io.LeftIO <> phi41.io.Out(1)

  phi59.io.InData(0) <> phi41.io.Out(2)

  sext43.io.Input <> phi42.io.Out(0)

  binaryOp_53.io.LeftIO <> phi42.io.Out(1)

  phi58.io.InData(1) <> phi42.io.Out(2)

  Gep_44.io.idx(1) <> sext43.io.Out(0)

  ld_45.io.GepAddr <> Gep_44.io.Out(0)

  icmp_49.io.LeftIO <> ld_45.io.Out(0)

  st_52.io.inData <> ld_45.io.Out(1)

  Gep_47.io.idx(1) <> sext46.io.Out(0)

  ld_48.io.GepAddr <> Gep_47.io.Out(0)

  icmp_49.io.RightIO <> ld_48.io.Out(0)

  st_55.io.inData <> ld_48.io.Out(1)

  br_51.io.CmpIO <> icmp_49.io.Out(0)

  st_52.io.GepAddr <> Gep_50.io.Out(0)

  st_55.io.GepAddr <> Gep_50.io.Out(1)

  phi58.io.InData(0) <> binaryOp_53.io.Out(0)

  phi59.io.InData(1) <> binaryOp_56.io.Out(0)

  br_62.io.CmpIO <> icmp_61.io.Out(0)

  Gep_7.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  icmp_2.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(1)

  sext4.io.Input <> InputSplitter.io.Out.data.elements("field1")(2)

  icmp_9.io.RightIO <> InputSplitter.io.Out.data.elements("field1")(3)

  select_10.io.InData2 <> InputSplitter.io.Out.data.elements("field1")(4)

  binaryOp_11.io.RightIO <> InputSplitter.io.Out.data.elements("field1")(5)

  icmp_35.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(6)

  sext37.io.Input <> InputSplitter.io.Out.data.elements("field1")(7)

  icmp_2.io.RightIO <> InputSplitter.io.Out.data.elements("field2")(0)

  icmp_9.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(1)

  select_10.io.InData1 <> InputSplitter.io.Out.data.elements("field2")(2)

  icmp_16.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(3)

  binaryOp_18.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(4)

  sext20.io.Input <> InputSplitter.io.Out.data.elements("field2")(5)

  icmp_16.io.RightIO <> InputSplitter.io.Out.data.elements("field3")(1)

  binaryOp_19.io.RightIO <> InputSplitter.io.Out.data.elements("field3")(2)

  sext21.io.Input <> InputSplitter.io.Out.data.elements("field3")(3)

  icmp_35.io.RightIO <> InputSplitter.io.Out.data.elements("field3")(4)

  sext38.io.Input <> InputSplitter.io.Out.data.elements("field3")(5)

  st_31.io.Out(0).ready := true.B

  st_52.io.Out(0).ready := true.B

  st_55.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_64.io.Out

}

import java.io.{File, FileWriter}

object mergeTop extends App {
  val dir = new File("RTL/mergeTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new mergeDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
