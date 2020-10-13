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

abstract class test13DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test13DF(implicit p: Parameters) extends test13DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val SharedFPU = Module(new SharedFPU(NumOps = 1, PipeDepth = 32)(t = p(FTYP)))

  val InputSplitter = Module(new SplitCallNew(List(2, 3, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_for_body_preheader1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_for_cond_cleanup_loopexit2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_for_cond_cleanup3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 5, BID = 3))

  val bb_for_body4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 1, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %cmp11 = icmp sgt i32 %n, 0, !dbg !31, !UID !33
  val icmp_cmp110 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "ugt")(sign = false))

  //  br i1 %cmp11, label %for.body.preheader, label %for.cond.cleanup, !dbg !34, !UID !35, !BB_UID !36
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  br label %for.body, !dbg !37, !UID !39, !BB_UID !40
  val br_2 = Module(new UBranchNode(ID = 2))

  //  br label %for.cond.cleanup, !dbg !41
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %sub = add nsw i32 %n, -1, !dbg !41, !UID !42
  val binaryOp_sub4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "add")(sign = false))

  //  %arrayidx2 = getelementptr inbounds float, float* %a, i32 %sub, !dbg !43, !UID !44
  val Gep_arrayidx25 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 5)(ElementSize = 4, ArraySize = List()))

  //  %0 = load float, float* %arrayidx2, align 4, !dbg !43, !tbaa !45, !UID !49
  val ld_6 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 6, RouteID = 0))

  //  ret float %0, !dbg !50, !UID !51, !BB_UID !52
  val ret_7 = Module(new RetNode2(retTypes = List(32), ID = 7))

  //  %k.012 = phi i32 [ %inc, %for.body ], [ 0, %for.body.preheader ], !UID !53
  val phik_0128 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 8, Res = false))

  //  %arrayidx = getelementptr inbounds float, float* %a, i32 %k.012, !dbg !37, !UID !54
  val Gep_arrayidx9 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 9)(ElementSize = 4, ArraySize = List()))

  //  %1 = load float, float* %arrayidx, align 4, !dbg !37, !tbaa !45, !UID !55
  val ld_10 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 10, RouteID = 1))

  //  %div = fdiv float %1, %mean, !dbg !56, !UID !57
  val FP_div11 = Module(new FPDivSqrtNode(NumOuts = 1, ID = 11, RouteID = 0, opCode = "fdiv")(t = p(FTYP)))

  //  store float %div, float* %arrayidx, align 4, !dbg !58, !tbaa !45, !UID !59
  val st_12 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 12, RouteID = 0))

  //  %inc = add nuw nsw i32 %k.012, 1, !dbg !60, !UID !61
  val binaryOp_inc13 = Module(new ComputeNode(NumOuts = 2, ID = 13, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, %n, !dbg !31, !UID !62
  val icmp_exitcond14 = Module(new IcmpNode(NumOuts = 1, ID = 14, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup.loopexit, label %for.body, !dbg !34, !llvm.loop !63, !UID !65, !BB_UID !66
  val br_15 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 15))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 -1
  val const1 = Module(new ConstFastNode(value = -1, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_for_body_preheader1.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_for_cond_cleanup3.io.predicateIn(1) <> br_1.io.FalseOutput(0)

  bb_for_cond_cleanup3.io.predicateIn(0) <> br_3.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_for_cond_cleanup_loopexit2.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_for_body4.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_for_body4.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_2.io.Out(0)

  Loop_0.io.loopBack(0) <> br_15.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_15.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_arrayidx9.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field0")(0)

  FP_div11.io.b <> Loop_0.io.OutLiveIn.elements("field1")(0)

  icmp_exitcond14.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc13.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phik_0128.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  icmp_cmp110.io.enable <> bb_entry0.io.Out(1)


  br_1.io.enable <> bb_entry0.io.Out(2)


  br_2.io.enable <> bb_for_body_preheader1.io.Out(0)


  br_3.io.enable <> bb_for_cond_cleanup_loopexit2.io.Out(0)


  const1.io.enable <> bb_for_cond_cleanup3.io.Out(0)

  binaryOp_sub4.io.enable <> bb_for_cond_cleanup3.io.Out(1)


  Gep_arrayidx25.io.enable <> bb_for_cond_cleanup3.io.Out(2)


  ld_6.io.enable <> bb_for_cond_cleanup3.io.Out(3)


  ret_7.io.In.enable <> bb_for_cond_cleanup3.io.Out(4)


  const2.io.enable <> bb_for_body4.io.Out(0)

  const3.io.enable <> bb_for_body4.io.Out(1)

  phik_0128.io.enable <> bb_for_body4.io.Out(2)


  Gep_arrayidx9.io.enable <> bb_for_body4.io.Out(3)


  ld_10.io.enable <> bb_for_body4.io.Out(4)


  FP_div11.io.enable <> bb_for_body4.io.Out(5)


  st_12.io.enable <> bb_for_body4.io.Out(6)


  binaryOp_inc13.io.enable <> bb_for_body4.io.Out(7)


  icmp_exitcond14.io.enable <> bb_for_body4.io.Out(8)


  br_15.io.enable <> bb_for_body4.io.Out(9)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phik_0128.io.Mask <> bb_for_body4.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_6.io.memReq

  ld_6.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_10.io.memReq

  ld_10.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_12.io.memReq

  st_12.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */

  SharedFPU.io.InData(0) <> FP_div11.io.FUReq
  FP_div11.io.FUResp <> SharedFPU.io.OutData(0)



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  icmp_cmp110.io.RightIO <> const0.io.Out

  binaryOp_sub4.io.RightIO <> const1.io.Out

  phik_0128.io.InData(1) <> const2.io.Out

  binaryOp_inc13.io.RightIO <> const3.io.Out

  br_1.io.CmpIO <> icmp_cmp110.io.Out(0)

  Gep_arrayidx25.io.idx(0) <> binaryOp_sub4.io.Out(0)

  ld_6.io.GepAddr <> Gep_arrayidx25.io.Out(0)

  ret_7.io.In.data("field0") <> ld_6.io.Out(0)

  Gep_arrayidx9.io.idx(0) <> phik_0128.io.Out(0)

  binaryOp_inc13.io.LeftIO <> phik_0128.io.Out(1)

  ld_10.io.GepAddr <> Gep_arrayidx9.io.Out(0)

  st_12.io.GepAddr <> Gep_arrayidx9.io.Out(1)

  FP_div11.io.a <> ld_10.io.Out(0)

  st_12.io.inData <> FP_div11.io.Out(0)

  icmp_exitcond14.io.LeftIO <> binaryOp_inc13.io.Out(1)

  br_15.io.CmpIO <> icmp_exitcond14.io.Out(0)

  Gep_arrayidx25.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  icmp_cmp110.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(1)

  binaryOp_sub4.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(2)

  st_12.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}

object test13Top extends App {
  val dir = new File("RTL/test13Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test13DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
