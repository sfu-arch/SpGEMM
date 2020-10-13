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

abstract class bgemmDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val call_9_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_9_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class bgemmDF(implicit p: Parameters) extends bgemmDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

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

  val bb_pfor_cond_cleanup1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_pfor_detach2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 5, NumPhi = 1, BID = 2))

  val bb_pfor_inc593 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 3))

  val bb_sync_continue614 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))

  val bb_offload_loopkk5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 5))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %pfor.detach, !UID !10, !BB_UID !11
  val br_0 = Module(new UBranchNode(ID = 0))

  //  sync within %syncreg, label %sync.continue61, !UID !12, !BB_UID !13
  val sync_1 = Module(new SyncTC(ID = 1, NumInc=1, NumDec=1, NumOuts=1))

  //  %__begin.0101 = phi i32 [ 0, %entry ], [ %inc60, %pfor.inc59 ], !UID !14
  val phi__begin_01012 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 2, Res = true))

  //  %mul1 = shl nuw nsw i32 %__begin.0101, 1, !UID !15
  val binaryOp_mul13 = Module(new ComputeNode(NumOuts = 1, ID = 3, opCode = "shl")(sign = false))

  //  detach within %syncreg, label %offload.loopkk, label %pfor.inc59, !UID !16, !BB_UID !17
  val detach_4 = Module(new Detach(ID = 4))

  //  %inc60 = add nuw nsw i32 %__begin.0101, 1, !UID !18
  val binaryOp_inc605 = Module(new ComputeNode(NumOuts = 2, ID = 5, opCode = "add")(sign = false))

  //  %exitcond105 = icmp eq i32 %inc60, 2, !UID !19
  val icmp_exitcond1056 = Module(new IcmpNode(NumOuts = 1, ID = 6, opCode = "eq")(sign = false))

  //  br i1 %exitcond105, label %pfor.cond.cleanup, label %pfor.detach, !llvm.loop !20, !UID !22, !BB_UID !23
  val br_7 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 7))

  //  ret void, !UID !24, !BB_UID !25
  val ret_8 = Module(new RetNode2(retTypes = List(), ID = 8))

  //  call void @bgemm_detach1(i32* %m1, i32 %mul1, i32* %m2, i32* %prod), !UID !26
  val call_9_out = Module(new CallOutNode(ID = 9, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_9_in = Module(new CallInNode(ID = 9, argTypes = List()))

  //  reattach within %syncreg, label %pfor.inc59, !UID !27, !BB_UID !28
  val reattach_10 = Module(new Reattach(NumPredOps= 1, ID = 10))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 1
  val const2 = Module(new ConstFastNode(value = 1, ID = 2))

  //i32 2
  val const3 = Module(new ConstFastNode(value = 2, ID = 3))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_pfor_inc593.io.predicateIn(0) <> detach_4.io.Out(0)

  bb_sync_continue614.io.predicateIn(0) <> sync_1.io.Out(0)

  bb_offload_loopkk5.io.predicateIn(0) <> detach_4.io.Out(1)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_pfor_cond_cleanup1.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_pfor_detach2.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_pfor_detach2.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_1.io.incIn(0) <> detach_4.io.Out(2)

  sync_1.io.decIn(0) <> reattach_10.io.Out(0)



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_0.io.Out(0)

  Loop_0.io.loopBack(0) <> br_7.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_7.io.TrueOutput(0)



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

  call_9_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  call_9_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)

  call_9_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc605.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_01012.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_entry0.io.Out(0)


  sync_1.io.enable <> bb_pfor_cond_cleanup1.io.Out(0)


  const0.io.enable <> bb_pfor_detach2.io.Out(0)

  const1.io.enable <> bb_pfor_detach2.io.Out(1)

  phi__begin_01012.io.enable <> bb_pfor_detach2.io.Out(2)


  binaryOp_mul13.io.enable <> bb_pfor_detach2.io.Out(3)


  detach_4.io.enable <> bb_pfor_detach2.io.Out(4)


  const2.io.enable <> bb_pfor_inc593.io.Out(0)

  const3.io.enable <> bb_pfor_inc593.io.Out(1)

  binaryOp_inc605.io.enable <> bb_pfor_inc593.io.Out(2)


  icmp_exitcond1056.io.enable <> bb_pfor_inc593.io.Out(3)


  br_7.io.enable <> bb_pfor_inc593.io.Out(4)


  ret_8.io.In.enable <> bb_sync_continue614.io.Out(0)


  call_9_in.io.enable <> bb_offload_loopkk5.io.Out(1)

  call_9_out.io.enable <> bb_offload_loopkk5.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi__begin_01012.io.Mask <> bb_pfor_detach2.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_01012.io.InData(0) <> const0.io.Out

  binaryOp_mul13.io.RightIO <> const1.io.Out

  binaryOp_inc605.io.RightIO <> const2.io.Out

  icmp_exitcond1056.io.RightIO <> const3.io.Out

  binaryOp_mul13.io.LeftIO <> phi__begin_01012.io.Out(0)

  binaryOp_inc605.io.LeftIO <> phi__begin_01012.io.Out(1)

  call_9_out.io.In.elements("field1") <> binaryOp_mul13.io.Out(0)

  icmp_exitcond1056.io.LeftIO <> binaryOp_inc605.io.Out(1)

  br_7.io.CmpIO <> icmp_exitcond1056.io.Out(0)

  reattach_10.io.predicateIn(0).enq(DataBundle.active(1.U))



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_9_in.io.In <> io.call_9_in

  io.call_9_out <> call_9_out.io.Out(0)

  reattach_10.io.enable <> call_9_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_8.io.Out

}

import java.io.{File, FileWriter}

object bgemmTop extends App {
  val dir = new File("RTL/bgemmTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new bgemmDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
