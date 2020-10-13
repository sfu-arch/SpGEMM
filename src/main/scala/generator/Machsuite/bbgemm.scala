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

abstract class bbgemmDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class bbgemmDF(implicit p: Parameters) extends bbgemmDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1, 2), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 1, 1, 1, 2, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))

  val Loop_2 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 3))

  val Loop_3 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 4))

  val Loop_4 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 5))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 2))

  val bb_3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 5, NumPhi = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 1, BID = 4))

  val bb_5 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 18, NumPhi = 1, BID = 5))

  val bb_6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 6))

  val bb_7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 7))

  val bb_8 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 8))

  val bb_9 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 9))

  val bb_10 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 10))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %4, !dbg !83, !UID !85, !BB_UID !86
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %5 = phi i64 [ 0, %3 ], [ %43, %42 ], !UID !87
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 1, Res = false))

  //  br label %6, !dbg !89, !UID !93, !BB_UID !94
  val br_2 = Module(new UBranchNode(ID = 2))

  //  %7 = phi i64 [ 0, %4 ], [ %40, %39 ], !UID !95
  val phi3 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 3, Res = false))

  //  br label %8, !dbg !97, !UID !101, !BB_UID !102
  val br_4 = Module(new UBranchNode(ID = 4))

  //  %9 = phi i64 [ 0, %6 ], [ %37, %36 ], !UID !103
  val phi5 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 5, Res = true))

  //  %10 = shl nsw i64 %9, 6, !UID !105
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "shl")(sign = false))

  //  br label %11, !dbg !106, !UID !110, !BB_UID !111
  val br_7 = Module(new UBranchNode(ID = 7))

  //  %12 = phi i64 [ 0, %8 ], [ %34, %33 ], !UID !112
  val phi8 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 8, Res = true))

  //  %13 = add nuw nsw i64 %12, %7, !dbg !113, !UID !116
  val binaryOp_9 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "add")(sign = false))

  //  %14 = shl i64 %13, 6, !dbg !117, !UID !118
  val binaryOp_10 = Module(new ComputeNode(NumOuts = 1, ID = 10, opCode = "shl")(sign = false))

  //  %15 = add nuw nsw i64 %12, %7, !dbg !119, !UID !120
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "add")(sign = false))

  //  %16 = add nuw nsw i64 %15, %10, !dbg !121, !UID !122
  val binaryOp_12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign = false))

  //  %17 = getelementptr inbounds double, double* %0, i64 %16, !dbg !123, !UID !124
  val Gep_13 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 13)(ElementSize = 8, ArraySize = List()))

  //  %18 = load double, double* %17, align 8, !dbg !123, !tbaa !125, !UID !129
  val ld_14 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 14, RouteID = 0))

  //  br label %19, !dbg !132, !UID !134, !BB_UID !135
  val br_15 = Module(new UBranchNode(ID = 15))

  //  %20 = phi i64 [ 0, %11 ], [ %31, %19 ], !UID !136
  val phi16 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 16, Res = true))

  //  %21 = add nuw nsw i64 %20, %5, !dbg !137, !UID !140
  val binaryOp_17 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "add")(sign = false))

  //  %22 = add nuw nsw i64 %21, %14, !dbg !141, !UID !142
  val binaryOp_18 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "add")(sign = false))

  //  %23 = getelementptr inbounds double, double* %1, i64 %22, !dbg !143, !UID !144
  val Gep_19 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 19)(ElementSize = 8, ArraySize = List()))

  //  %24 = load double, double* %23, align 8, !dbg !143, !tbaa !125, !UID !145
  val ld_20 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 20, RouteID = 1))

  //  %25 = fmul double %18, %24, !dbg !146, !UID !147
  val FP_21 = Module(new FPComputeNode(NumOuts = 1, ID = 21, opCode = "fmul")(t = p(FTYP)))

  //  %26 = add nuw nsw i64 %20, %5, !dbg !149, !UID !150
  val binaryOp_22 = Module(new ComputeNode(NumOuts = 1, ID = 22, opCode = "add")(sign = false))

  //  %27 = add nuw nsw i64 %26, %10, !dbg !151, !UID !152
  val binaryOp_23 = Module(new ComputeNode(NumOuts = 1, ID = 23, opCode = "add")(sign = false))

  //  %28 = getelementptr inbounds double, double* %2, i64 %27, !dbg !153, !UID !154
  val Gep_24 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 24)(ElementSize = 8, ArraySize = List()))

  //  %29 = load double, double* %28, align 8, !dbg !155, !tbaa !125, !UID !156
  val ld_25 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 25, RouteID = 2))

  //  %30 = fadd double %29, %25, !dbg !155, !UID !157
  val FP_26 = Module(new FPComputeNode(NumOuts = 1, ID = 26, opCode = "fadd")(t = p(FTYP)))

  //  store double %30, double* %28, align 8, !dbg !155, !tbaa !125, !UID !158
  val st_27 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 27, RouteID = 0))

  //  %31 = add nuw nsw i64 %20, 1, !dbg !159, !UID !160
  val binaryOp_28 = Module(new ComputeNode(NumOuts = 2, ID = 28, opCode = "add")(sign = false))

  //  %32 = icmp eq i64 %31, 8, !dbg !161, !UID !162
  val icmp_29 = Module(new IcmpNode(NumOuts = 1, ID = 29, opCode = "eq")(sign = false))

  //  br i1 %32, label %33, label %19, !dbg !132, !llvm.loop !163, !UID !165, !BB_UID !166
  val br_30 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 30))

  //  %34 = add nuw nsw i64 %12, 1, !dbg !167, !UID !168
  val binaryOp_31 = Module(new ComputeNode(NumOuts = 2, ID = 31, opCode = "add")(sign = false))

  //  %35 = icmp eq i64 %34, 8, !dbg !169, !UID !170
  val icmp_32 = Module(new IcmpNode(NumOuts = 1, ID = 32, opCode = "eq")(sign = false))

  //  br i1 %35, label %36, label %11, !dbg !106, !llvm.loop !171, !UID !173, !BB_UID !174
  val br_33 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 33))

  //  %37 = add nuw nsw i64 %9, 1, !dbg !175, !UID !176
  val binaryOp_34 = Module(new ComputeNode(NumOuts = 2, ID = 34, opCode = "add")(sign = false))

  //  %38 = icmp eq i64 %37, 64, !dbg !177, !UID !178
  val icmp_35 = Module(new IcmpNode(NumOuts = 1, ID = 35, opCode = "eq")(sign = false))

  //  br i1 %38, label %39, label %8, !dbg !97, !llvm.loop !179, !UID !181, !BB_UID !182
  val br_36 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 36))

  //  %40 = add nuw nsw i64 %7, 8, !dbg !183, !UID !184
  val binaryOp_37 = Module(new ComputeNode(NumOuts = 2, ID = 37, opCode = "add")(sign = false))

  //  %41 = icmp ult i64 %40, 64, !dbg !185, !UID !186
  val icmp_38 = Module(new IcmpNode(NumOuts = 1, ID = 38, opCode = "ult")(sign = false))

  //  br i1 %41, label %6, label %42, !dbg !89, !llvm.loop !187, !UID !189, !BB_UID !190
  val br_39 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 39))

  //  %43 = add nuw nsw i64 %5, 8, !dbg !191, !UID !192
  val binaryOp_40 = Module(new ComputeNode(NumOuts = 2, ID = 40, opCode = "add")(sign = false))

  //  %44 = icmp ult i64 %43, 64, !dbg !193, !UID !194
  val icmp_41 = Module(new IcmpNode(NumOuts = 1, ID = 41, opCode = "ult")(sign = false))

  //  br i1 %44, label %4, label %45, !dbg !83, !llvm.loop !195, !UID !197, !BB_UID !198
  val br_42 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 42))

  //  ret void, !dbg !199, !UID !200, !BB_UID !201
  val ret_43 = Module(new RetNode2(retTypes = List(), ID = 43))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i64 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i64 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i64 6
  val const3 = Module(new ConstFastNode(value = 6, ID = 3))

  //i64 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i64 6
  val const5 = Module(new ConstFastNode(value = 6, ID = 5))

  //i64 0
  val const6 = Module(new ConstFastNode(value = 0, ID = 6))

  //i64 1
  val const7 = Module(new ConstFastNode(value = 1, ID = 7))

  //i64 8
  val const8 = Module(new ConstFastNode(value = 8, ID = 8))

  //i64 1
  val const9 = Module(new ConstFastNode(value = 1, ID = 9))

  //i64 8
  val const10 = Module(new ConstFastNode(value = 8, ID = 10))

  //i64 1
  val const11 = Module(new ConstFastNode(value = 1, ID = 11))

  //i64 64
  val const12 = Module(new ConstFastNode(value = 64, ID = 12))

  //i64 8
  val const13 = Module(new ConstFastNode(value = 8, ID = 13))

  //i64 64
  val const14 = Module(new ConstFastNode(value = 64, ID = 14))

  //i64 8
  val const15 = Module(new ConstFastNode(value = 8, ID = 15))

  //i64 64
  val const16 = Module(new ConstFastNode(value = 64, ID = 16))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_1.io.predicateIn(0) <> Loop_4.io.activate_loop_start

  bb_1.io.predicateIn(1) <> Loop_4.io.activate_loop_back

  bb_2.io.predicateIn(0) <> Loop_3.io.activate_loop_start

  bb_2.io.predicateIn(1) <> Loop_3.io.activate_loop_back

  bb_3.io.predicateIn(1) <> Loop_2.io.activate_loop_start

  bb_3.io.predicateIn(0) <> Loop_2.io.activate_loop_back

  bb_4.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_4.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_5.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_5.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_6.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_7.io.predicateIn(0) <> Loop_1.io.loopExit(0)

  bb_8.io.predicateIn(0) <> Loop_2.io.loopExit(0)

  bb_9.io.predicateIn(0) <> Loop_3.io.loopExit(0)

  bb_10.io.predicateIn(0) <> Loop_4.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_15.io.Out(0)

  Loop_0.io.loopBack(0) <> br_30.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_30.io.TrueOutput(0)

  Loop_1.io.enable <> br_7.io.Out(0)

  Loop_1.io.loopBack(0) <> br_33.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_33.io.TrueOutput(0)

  Loop_2.io.enable <> br_4.io.Out(0)

  Loop_2.io.loopBack(0) <> br_36.io.FalseOutput(0)

  Loop_2.io.loopFinish(0) <> br_36.io.TrueOutput(0)

  Loop_3.io.enable <> br_2.io.Out(0)

  Loop_3.io.loopBack(0) <> br_39.io.TrueOutput(0)

  Loop_3.io.loopFinish(0) <> br_39.io.FalseOutput(0)

  Loop_4.io.enable <> br_0.io.Out(0)

  Loop_4.io.loopBack(0) <> br_42.io.TrueOutput(0)

  Loop_4.io.loopFinish(0) <> br_42.io.FalseOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> binaryOp_10.io.Out(0)

  Loop_0.io.InLiveIn(1) <> ld_14.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field0")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Loop_0.io.InLiveIn(5) <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Loop_1.io.InLiveIn(0) <> binaryOp_6.io.Out(0)

  Loop_1.io.InLiveIn(1) <> Loop_2.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(2) <> Loop_2.io.OutLiveIn.elements("field2")(0)

  Loop_1.io.InLiveIn(3) <> Loop_2.io.OutLiveIn.elements("field4")(0)

  Loop_1.io.InLiveIn(4) <> Loop_2.io.OutLiveIn.elements("field0")(0)

  Loop_1.io.InLiveIn(5) <> Loop_2.io.OutLiveIn.elements("field3")(0)

  Loop_2.io.InLiveIn(0) <> phi3.io.Out(0)

  Loop_2.io.InLiveIn(1) <> Loop_3.io.OutLiveIn.elements("field1")(0)

  Loop_2.io.InLiveIn(2) <> Loop_3.io.OutLiveIn.elements("field2")(0)

  Loop_2.io.InLiveIn(3) <> Loop_3.io.OutLiveIn.elements("field3")(0)

  Loop_2.io.InLiveIn(4) <> Loop_3.io.OutLiveIn.elements("field0")(0)

  Loop_3.io.InLiveIn(0) <> phi1.io.Out(0)

  Loop_3.io.InLiveIn(1) <> Loop_4.io.OutLiveIn.elements("field2")(0)

  Loop_3.io.InLiveIn(2) <> Loop_4.io.OutLiveIn.elements("field1")(0)

  Loop_3.io.InLiveIn(3) <> Loop_4.io.OutLiveIn.elements("field0")(0)

  Loop_4.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_4.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_4.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_18.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  FP_21.io.LeftIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  binaryOp_23.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_24.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  Gep_19.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field4")(0)

  binaryOp_17.io.RightIO <> Loop_0.io.OutLiveIn.elements("field5")(0)

  binaryOp_22.io.RightIO <> Loop_0.io.OutLiveIn.elements("field5")(1)

  binaryOp_12.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(1)

  binaryOp_9.io.RightIO <> Loop_1.io.OutLiveIn.elements("field4")(0)

  binaryOp_11.io.RightIO <> Loop_1.io.OutLiveIn.elements("field4")(1)

  Gep_13.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field5")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_28.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_31.io.Out(0)

  Loop_2.io.CarryDepenIn(0) <> binaryOp_34.io.Out(0)

  Loop_3.io.CarryDepenIn(0) <> binaryOp_37.io.Out(0)

  Loop_4.io.CarryDepenIn(0) <> binaryOp_40.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi16.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi8.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)

  phi5.io.InData(1) <> Loop_2.io.CarryDepenOut.elements("field0")(0)

  phi3.io.InData(1) <> Loop_3.io.CarryDepenOut.elements("field0")(0)

  phi1.io.InData(1) <> Loop_4.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  const0.io.enable <> bb_1.io.Out(0)

  phi1.io.enable <> bb_1.io.Out(1)


  br_2.io.enable <> bb_1.io.Out(2)


  const1.io.enable <> bb_2.io.Out(0)

  phi3.io.enable <> bb_2.io.Out(1)


  br_4.io.enable <> bb_2.io.Out(2)


  const2.io.enable <> bb_3.io.Out(0)

  const3.io.enable <> bb_3.io.Out(1)

  phi5.io.enable <> bb_3.io.Out(2)


  binaryOp_6.io.enable <> bb_3.io.Out(3)


  br_7.io.enable <> bb_3.io.Out(4)


  const4.io.enable <> bb_4.io.Out(0)

  const5.io.enable <> bb_4.io.Out(1)

  phi8.io.enable <> bb_4.io.Out(2)


  binaryOp_9.io.enable <> bb_4.io.Out(3)


  binaryOp_10.io.enable <> bb_4.io.Out(4)


  binaryOp_11.io.enable <> bb_4.io.Out(5)


  binaryOp_12.io.enable <> bb_4.io.Out(6)


  Gep_13.io.enable <> bb_4.io.Out(7)


  ld_14.io.enable <> bb_4.io.Out(8)


  br_15.io.enable <> bb_4.io.Out(9)


  const6.io.enable <> bb_5.io.Out(0)

  const7.io.enable <> bb_5.io.Out(1)

  const8.io.enable <> bb_5.io.Out(2)

  phi16.io.enable <> bb_5.io.Out(3)


  binaryOp_17.io.enable <> bb_5.io.Out(4)


  binaryOp_18.io.enable <> bb_5.io.Out(5)


  Gep_19.io.enable <> bb_5.io.Out(6)


  ld_20.io.enable <> bb_5.io.Out(7)


  FP_21.io.enable <> bb_5.io.Out(8)


  binaryOp_22.io.enable <> bb_5.io.Out(9)


  binaryOp_23.io.enable <> bb_5.io.Out(10)


  Gep_24.io.enable <> bb_5.io.Out(11)


  ld_25.io.enable <> bb_5.io.Out(12)


  FP_26.io.enable <> bb_5.io.Out(13)


  st_27.io.enable <> bb_5.io.Out(14)


  binaryOp_28.io.enable <> bb_5.io.Out(15)


  icmp_29.io.enable <> bb_5.io.Out(16)


  br_30.io.enable <> bb_5.io.Out(17)


  const9.io.enable <> bb_6.io.Out(0)

  const10.io.enable <> bb_6.io.Out(1)

  binaryOp_31.io.enable <> bb_6.io.Out(2)


  icmp_32.io.enable <> bb_6.io.Out(3)


  br_33.io.enable <> bb_6.io.Out(4)


  const11.io.enable <> bb_7.io.Out(0)

  const12.io.enable <> bb_7.io.Out(1)

  binaryOp_34.io.enable <> bb_7.io.Out(2)


  icmp_35.io.enable <> bb_7.io.Out(3)


  br_36.io.enable <> bb_7.io.Out(4)


  const13.io.enable <> bb_8.io.Out(0)

  const14.io.enable <> bb_8.io.Out(1)

  binaryOp_37.io.enable <> bb_8.io.Out(2)


  icmp_38.io.enable <> bb_8.io.Out(3)


  br_39.io.enable <> bb_8.io.Out(4)


  const15.io.enable <> bb_9.io.Out(0)

  const16.io.enable <> bb_9.io.Out(1)

  binaryOp_40.io.enable <> bb_9.io.Out(2)


  icmp_41.io.enable <> bb_9.io.Out(3)


  br_42.io.enable <> bb_9.io.Out(4)


  ret_43.io.In.enable <> bb_10.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi3.io.Mask <> bb_2.io.MaskBB(0)

  phi5.io.Mask <> bb_3.io.MaskBB(0)

  phi8.io.Mask <> bb_4.io.MaskBB(0)

  phi16.io.Mask <> bb_5.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_20.io.memReq

  ld_20.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_25.io.memReq

  ld_25.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(0) <> st_27.io.memReq

  st_27.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi1.io.InData(0) <> const0.io.Out

  phi3.io.InData(0) <> const1.io.Out

  phi5.io.InData(0) <> const2.io.Out

  binaryOp_6.io.RightIO <> const3.io.Out

  phi8.io.InData(0) <> const4.io.Out

  binaryOp_10.io.RightIO <> const5.io.Out

  phi16.io.InData(0) <> const6.io.Out

  binaryOp_28.io.RightIO <> const7.io.Out

  icmp_29.io.RightIO <> const8.io.Out

  binaryOp_31.io.RightIO <> const9.io.Out

  icmp_32.io.RightIO <> const10.io.Out

  binaryOp_34.io.RightIO <> const11.io.Out

  icmp_35.io.RightIO <> const12.io.Out

  binaryOp_37.io.RightIO <> const13.io.Out

  icmp_38.io.RightIO <> const14.io.Out

  binaryOp_40.io.RightIO <> const15.io.Out

  icmp_41.io.RightIO <> const16.io.Out

  binaryOp_40.io.LeftIO <> phi1.io.Out(1)

  binaryOp_37.io.LeftIO <> phi3.io.Out(1)

  binaryOp_6.io.LeftIO <> phi5.io.Out(0)

  binaryOp_34.io.LeftIO <> phi5.io.Out(1)

  binaryOp_9.io.LeftIO <> phi8.io.Out(0)

  binaryOp_11.io.LeftIO <> phi8.io.Out(1)

  binaryOp_31.io.LeftIO <> phi8.io.Out(2)

  binaryOp_10.io.LeftIO <> binaryOp_9.io.Out(0)

  binaryOp_12.io.LeftIO <> binaryOp_11.io.Out(0)

  Gep_13.io.idx(0) <> binaryOp_12.io.Out(0)

  ld_14.io.GepAddr <> Gep_13.io.Out(0)

  binaryOp_17.io.LeftIO <> phi16.io.Out(0)

  binaryOp_22.io.LeftIO <> phi16.io.Out(1)

  binaryOp_28.io.LeftIO <> phi16.io.Out(2)

  binaryOp_18.io.LeftIO <> binaryOp_17.io.Out(0)

  Gep_19.io.idx(0) <> binaryOp_18.io.Out(0)

  ld_20.io.GepAddr <> Gep_19.io.Out(0)

  FP_21.io.RightIO <> ld_20.io.Out(0)

  FP_26.io.RightIO <> FP_21.io.Out(0)

  binaryOp_23.io.LeftIO <> binaryOp_22.io.Out(0)

  Gep_24.io.idx(0) <> binaryOp_23.io.Out(0)

  ld_25.io.GepAddr <> Gep_24.io.Out(0)

  st_27.io.GepAddr <> Gep_24.io.Out(1)

  FP_26.io.LeftIO <> ld_25.io.Out(0)

  st_27.io.inData <> FP_26.io.Out(0)

  icmp_29.io.LeftIO <> binaryOp_28.io.Out(1)

  br_30.io.CmpIO <> icmp_29.io.Out(0)

  icmp_32.io.LeftIO <> binaryOp_31.io.Out(1)

  br_33.io.CmpIO <> icmp_32.io.Out(0)

  icmp_35.io.LeftIO <> binaryOp_34.io.Out(1)

  br_36.io.CmpIO <> icmp_35.io.Out(0)

  icmp_38.io.LeftIO <> binaryOp_37.io.Out(1)

  br_39.io.CmpIO <> icmp_38.io.Out(0)

  icmp_41.io.LeftIO <> binaryOp_40.io.Out(1)

  br_42.io.CmpIO <> icmp_41.io.Out(0)

  st_27.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_43.io.Out

}

import java.io.{File, FileWriter}

object bbgemmTop extends App {
  val dir = new File("RTL/bbgemmTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new bbgemmDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
