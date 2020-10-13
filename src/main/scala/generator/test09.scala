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

abstract class test09DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test09DF(implicit p: Parameters) extends test09DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_for_cond_cleanup1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 1))

  val bb_for_body2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 14, NumPhi = 1, BID = 2))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %for.body, !UID !10, !BB_UID !11
  val br_0 = Module(new UBranchNode(ID = 0))

  //  ret i32 1, !UID !12, !BB_UID !13
  val ret_1 = Module(new RetNode2(retTypes = List(32), ID = 1))

  //  %i.08 = phi i32 [ 0, %entry ], [ %inc, %for.body ], !UID !14
  val phii_082 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 4, ID = 2, Res = true))

  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %i.08, !UID !15
  val Gep_arrayidx3 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 3)(ElementSize = 4, ArraySize = List()))

  //  %0 = load i32, i32* %arrayidx, align 4, !tbaa !16, !UID !20
  val ld_4 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 4, RouteID = 0))

  //  %arrayidx1 = getelementptr inbounds i32, i32* %b, i32 %i.08, !UID !21
  val Gep_arrayidx15 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 5)(ElementSize = 4, ArraySize = List()))

  //  %1 = load i32, i32* %arrayidx1, align 4, !tbaa !16, !UID !22
  val ld_6 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 6, RouteID = 1))

  //  %add = add i32 %1, %0, !UID !23
  val binaryOp_add7 = Module(new ComputeNode(NumOuts = 1, ID = 7, opCode = "add")(sign = false))

  //  %arrayidx2 = getelementptr inbounds i32, i32* %c, i32 %i.08, !UID !24
  val Gep_arrayidx28 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 8)(ElementSize = 4, ArraySize = List()))

  //  store i32 %add, i32* %arrayidx2, align 4, !tbaa !16, !UID !25
  val st_9 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 9, RouteID = 0))

  //  %inc = add nuw nsw i32 %i.08, 1, !UID !26
  val binaryOp_inc10 = Module(new ComputeNode(NumOuts = 2, ID = 10, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, 5, !UID !27
  val icmp_exitcond11 = Module(new IcmpNode(NumOuts = 1, ID = 11, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup, label %for.body, !UID !28, !BB_UID !29
  val br_12 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 12))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 1
  val const0 = Module(new ConstFastNode(value = 1, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 1
  val const2 = Module(new ConstFastNode(value = 1, ID = 2))

  //i32 5
  val const3 = Module(new ConstFastNode(value = 5, ID = 3))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_for_cond_cleanup1.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_for_body2.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_for_body2.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_0.io.Out(0)

  Loop_0.io.loopBack(0) <> br_12.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_12.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_arrayidx3.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field0")(0)

  Gep_arrayidx15.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field1")(0)

  Gep_arrayidx28.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc10.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phii_082.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_entry0.io.Out(0)


  const0.io.enable <> bb_for_cond_cleanup1.io.Out(0)

  ret_1.io.In.enable <> bb_for_cond_cleanup1.io.Out(1)


  const1.io.enable <> bb_for_body2.io.Out(0)

  const2.io.enable <> bb_for_body2.io.Out(1)

  const3.io.enable <> bb_for_body2.io.Out(2)

  phii_082.io.enable <> bb_for_body2.io.Out(3)

  Gep_arrayidx3.io.enable <> bb_for_body2.io.Out(4)

  ld_4.io.enable <> bb_for_body2.io.Out(5)

  Gep_arrayidx15.io.enable <> bb_for_body2.io.Out(6)

  ld_6.io.enable <> bb_for_body2.io.Out(7)

  binaryOp_add7.io.enable <> bb_for_body2.io.Out(8)

  Gep_arrayidx28.io.enable <> bb_for_body2.io.Out(9)

  st_9.io.enable <> bb_for_body2.io.Out(10)

  binaryOp_inc10.io.enable <> bb_for_body2.io.Out(11)

  icmp_exitcond11.io.enable <> bb_for_body2.io.Out(12)

  br_12.io.enable <> bb_for_body2.io.Out(13)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phii_082.io.Mask <> bb_for_body2.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_4.io.memReq

  ld_4.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_6.io.memReq

  ld_6.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_9.io.memReq

  st_9.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  ret_1.io.In.data("field0") <> const0.io.Out

  phii_082.io.InData(0) <> const1.io.Out

  binaryOp_inc10.io.RightIO <> const2.io.Out

  icmp_exitcond11.io.RightIO <> const3.io.Out

  Gep_arrayidx3.io.idx(0) <> phii_082.io.Out(0)

  Gep_arrayidx15.io.idx(0) <> phii_082.io.Out(1)

  Gep_arrayidx28.io.idx(0) <> phii_082.io.Out(2)

  binaryOp_inc10.io.LeftIO <> phii_082.io.Out(3)

  ld_4.io.GepAddr <> Gep_arrayidx3.io.Out(0)

  binaryOp_add7.io.RightIO <> ld_4.io.Out(0)

  ld_6.io.GepAddr <> Gep_arrayidx15.io.Out(0)

  binaryOp_add7.io.LeftIO <> ld_6.io.Out(0)

  st_9.io.inData <> binaryOp_add7.io.Out(0)

  st_9.io.GepAddr <> Gep_arrayidx28.io.Out(0)

  icmp_exitcond11.io.LeftIO <> binaryOp_inc10.io.Out(1)

  br_12.io.CmpIO <> icmp_exitcond11.io.Out(0)

  st_9.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_1.io.Out

}

import java.io.{File, FileWriter}

object test09Top extends App {
  val dir = new File("RTL/test09Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test09DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
