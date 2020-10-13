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

abstract class ellpackDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class ellpackDF(implicit p: Parameters) extends ellpackDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 4, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 4, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 1), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 1))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 2))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 1, BID = 1))

  val bb_2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 18, NumPhi = 2, BID = 2))

  val bb_3 = Module(new BasicBlockNode(NumInputs = 1, NumOuts = 7, NumPhi = 1, BID = 3))

  val bb_4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %5, !dbg !89, !UID !90, !BB_UID !91
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %6 = phi i64 [ 0, %4 ], [ %26, %25 ], !UID !92
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 1, Res = true))

  //  %7 = getelementptr inbounds double, double* %3, i64 %6, !dbg !93, !UID !94
  val Gep_2 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 2)(ElementSize = 8, ArraySize = List()))

  //  %8 = load double, double* %7, align 8, !dbg !93, !tbaa !95, !UID !99
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 0))

  //  %9 = mul nuw nsw i64 %6, 10, !UID !102
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "mul")(sign = false))

  //  br label %10, !dbg !103, !UID !105, !BB_UID !106
  val br_5 = Module(new UBranchNode(ID = 5))

  //  %11 = phi i64 [ 0, %5 ], [ %23, %10 ], !UID !107
  val phi6 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 6, Res = true))

  //  %12 = phi double [ %8, %5 ], [ %22, %10 ], !UID !108
  val phi7 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 7, Res = true))

  //  %13 = add nuw nsw i64 %11, %9, !dbg !109, !UID !112
  val binaryOp_8 = Module(new ComputeNode(NumOuts = 2, ID = 8, opCode = "add")(sign = false))

  //  %14 = getelementptr inbounds double, double* %0, i64 %13, !dbg !113, !UID !114
  val Gep_9 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 9)(ElementSize = 8, ArraySize = List()))

  //  %15 = load double, double* %14, align 8, !dbg !113, !tbaa !95, !UID !115
  val ld_10 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 10, RouteID = 1))

  //  %16 = getelementptr inbounds i32, i32* %1, i64 %13, !dbg !116, !UID !117
  val Gep_11 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 11)(ElementSize = 4, ArraySize = List()))

  //  %17 = load i32, i32* %16, align 4, !dbg !116, !tbaa !118, !UID !120
  val ld_12 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 12, RouteID = 2))

  //  %18 = sext i32 %17 to i64, !dbg !121, !UID !122
  val sext13 = Module(new SextNode())

  //  %19 = getelementptr inbounds double, double* %2, i64 %18, !dbg !121, !UID !123
  val Gep_14 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 14)(ElementSize = 8, ArraySize = List()))

  //  %20 = load double, double* %19, align 8, !dbg !121, !tbaa !95, !UID !124
  val ld_15 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 15, RouteID = 3))

  //  %21 = fmul double %15, %20, !dbg !125, !UID !126
  val FP_16 = Module(new FPComputeNode(NumOuts = 1, ID = 16, opCode = "fmul")(t = p(FTYP)))

  //  %22 = fadd double %12, %21, !dbg !128, !UID !129
  val FP_17 = Module(new FPComputeNode(NumOuts = 2, ID = 17, opCode = "fadd")(t = p(FTYP)))

  //  %23 = add nuw nsw i64 %11, 1, !dbg !130, !UID !131
  val binaryOp_18 = Module(new ComputeNode(NumOuts = 2, ID = 18, opCode = "add")(sign = false))

  //  %24 = icmp eq i64 %23, 10, !dbg !132, !UID !133
  val icmp_19 = Module(new IcmpNode(NumOuts = 1, ID = 19, opCode = "eq")(sign = false))

  //  br i1 %24, label %25, label %10, !dbg !103, !llvm.loop !134, !UID !136, !BB_UID !137
  val br_20 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 20))

  //  %.lcssa = phi double [ %22, %10 ], !UID !138
  val phi_lcssa21 = Module(new PhiFastNode(NumInputs = 1, NumOutputs = 1, ID = 21, Res = false))

  //  store double %.lcssa, double* %7, align 8, !dbg !139, !tbaa !95, !UID !140
  val st_22 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 22, RouteID = 0))

  //  %26 = add nuw nsw i64 %6, 1, !dbg !141, !UID !142
  val binaryOp_23 = Module(new ComputeNode(NumOuts = 2, ID = 23, opCode = "add")(sign = false))

  //  %27 = icmp eq i64 %26, 494, !dbg !143, !UID !144
  val icmp_24 = Module(new IcmpNode(NumOuts = 1, ID = 24, opCode = "eq")(sign = false))

  //  br i1 %27, label %28, label %5, !dbg !89, !llvm.loop !145, !UID !147, !BB_UID !148
  val br_25 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 25))

  //  ret void, !dbg !149, !UID !150, !BB_UID !151
  val ret_26 = Module(new RetNode2(retTypes = List(), ID = 26))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i64 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i64 10
  val const1 = Module(new ConstFastNode(value = 10, ID = 1))

  //i64 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i64 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))

  //i64 10
  val const4 = Module(new ConstFastNode(value = 10, ID = 4))

  //i64 1
  val const5 = Module(new ConstFastNode(value = 1, ID = 5))

  //i64 494
  val const6 = Module(new ConstFastNode(value = 494, ID = 6))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_1.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_1.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_2.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_2.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_3.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_4.io.predicateIn(0) <> Loop_1.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_5.io.Out(0)

  Loop_0.io.loopBack(0) <> br_20.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_20.io.TrueOutput(0)

  Loop_1.io.enable <> br_0.io.Out(0)

  Loop_1.io.loopBack(0) <> br_25.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_25.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> ld_3.io.Out(0)

  Loop_0.io.InLiveIn(1) <> binaryOp_4.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field2")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_1.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_1.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_1.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_1.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phi7.io.InData(0) <> Loop_0.io.OutLiveIn.elements("field0")(0)

  binaryOp_8.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  Gep_14.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_11.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  Gep_9.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field4")(0)

  Gep_2.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_0.io.InLiveOut(0) <> FP_17.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phi_lcssa21.io.InData(0) <> Loop_0.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> FP_17.io.Out(1)

  Loop_0.io.CarryDepenIn(1) <> binaryOp_18.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_23.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi7.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi6.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field1")(0)

  phi1.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_0.io.Out(0)


  const0.io.enable <> bb_1.io.Out(0)

  const1.io.enable <> bb_1.io.Out(1)

  phi1.io.enable <> bb_1.io.Out(2)


  Gep_2.io.enable <> bb_1.io.Out(3)


  ld_3.io.enable <> bb_1.io.Out(4)


  binaryOp_4.io.enable <> bb_1.io.Out(5)


  br_5.io.enable <> bb_1.io.Out(6)


  const2.io.enable <> bb_2.io.Out(0)

  const3.io.enable <> bb_2.io.Out(1)

  const4.io.enable <> bb_2.io.Out(2)

  phi6.io.enable <> bb_2.io.Out(3)


  phi7.io.enable <> bb_2.io.Out(4)


  binaryOp_8.io.enable <> bb_2.io.Out(5)


  Gep_9.io.enable <> bb_2.io.Out(6)


  ld_10.io.enable <> bb_2.io.Out(7)


  Gep_11.io.enable <> bb_2.io.Out(8)


  ld_12.io.enable <> bb_2.io.Out(9)


  sext13.io.enable <> bb_2.io.Out(10)


  Gep_14.io.enable <> bb_2.io.Out(11)


  ld_15.io.enable <> bb_2.io.Out(12)


  FP_16.io.enable <> bb_2.io.Out(13)


  FP_17.io.enable <> bb_2.io.Out(14)


  binaryOp_18.io.enable <> bb_2.io.Out(15)


  icmp_19.io.enable <> bb_2.io.Out(16)


  br_20.io.enable <> bb_2.io.Out(17)


  const5.io.enable <> bb_3.io.Out(0)

  const6.io.enable <> bb_3.io.Out(1)

  phi_lcssa21.io.enable <> bb_3.io.Out(2)


  st_22.io.enable <> bb_3.io.Out(3)


  binaryOp_23.io.enable <> bb_3.io.Out(4)


  icmp_24.io.enable <> bb_3.io.Out(5)


  br_25.io.enable <> bb_3.io.Out(6)


  ret_26.io.In.enable <> bb_4.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_1.io.MaskBB(0)

  phi6.io.Mask <> bb_2.io.MaskBB(0)

  phi7.io.Mask <> bb_2.io.MaskBB(1)

  phi_lcssa21.io.Mask <> bb_3.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_3.io.memReq

  ld_3.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_10.io.memReq

  ld_10.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_12.io.memReq

  ld_12.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_15.io.memReq

  ld_15.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.WriteIn(0) <> st_22.io.memReq

  st_22.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi1.io.InData(0) <> const0.io.Out

  binaryOp_4.io.RightIO <> const1.io.Out

  phi6.io.InData(0) <> const2.io.Out

  binaryOp_18.io.RightIO <> const3.io.Out

  icmp_19.io.RightIO <> const4.io.Out

  binaryOp_23.io.RightIO <> const5.io.Out

  icmp_24.io.RightIO <> const6.io.Out

  Gep_2.io.idx(0) <> phi1.io.Out(0)

  binaryOp_4.io.LeftIO <> phi1.io.Out(1)

  binaryOp_23.io.LeftIO <> phi1.io.Out(2)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  st_22.io.GepAddr <> Gep_2.io.Out(1)

  binaryOp_8.io.LeftIO <> phi6.io.Out(0)

  binaryOp_18.io.LeftIO <> phi6.io.Out(1)

  FP_17.io.LeftIO <> phi7.io.Out(0)

  Gep_9.io.idx(0) <> binaryOp_8.io.Out(0)

  Gep_11.io.idx(0) <> binaryOp_8.io.Out(1)

  ld_10.io.GepAddr <> Gep_9.io.Out(0)

  FP_16.io.LeftIO <> ld_10.io.Out(0)

  ld_12.io.GepAddr <> Gep_11.io.Out(0)

  sext13.io.Input <> ld_12.io.Out(0)

  Gep_14.io.idx(0) <> sext13.io.Out(0)

  ld_15.io.GepAddr <> Gep_14.io.Out(0)

  FP_16.io.RightIO <> ld_15.io.Out(0)

  FP_17.io.RightIO <> FP_16.io.Out(0)

  icmp_19.io.LeftIO <> binaryOp_18.io.Out(1)

  br_20.io.CmpIO <> icmp_19.io.Out(0)

  st_22.io.inData <> phi_lcssa21.io.Out(0)

  icmp_24.io.LeftIO <> binaryOp_23.io.Out(1)

  br_25.io.CmpIO <> icmp_24.io.Out(0)

  st_22.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_26.io.Out

}

import java.io.{File, FileWriter}

object ellpackTop extends App {
  val dir = new File("RTL/ellpackTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new ellpackDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
