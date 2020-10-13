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

abstract class cilk_saxpyDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val call_11_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_11_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class cilk_saxpyDF(implicit p: Parameters) extends cilk_saxpyDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(2, 1, 1, 1)))
  InputSplitter.io.In <> io.in


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_pfor_detach_preheader1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_pfor_cond_cleanup_loopexit2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_pfor_cond_cleanup3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 1, BID = 3))

  val bb_pfor_detach4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 4))

  val bb_pfor_inc5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 5))

  val bb_sync_continue6 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 6))

  val bb_offload_pfor_body7 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 7))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %cmp17 = icmp sgt i32 %n, 0, !UID !21
  val icmp_cmp170 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "ugt")(sign = false))

  //  br i1 %cmp17, label %pfor.detach.preheader, label %pfor.cond.cleanup, !UID !22, !BB_UID !23
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  br label %pfor.detach, !UID !24, !BB_UID !25
  val br_2 = Module(new UBranchNode(ID = 2))

  //  br label %pfor.cond.cleanup, !UID !26, !BB_UID !27
  val br_3 = Module(new UBranchNode(ID = 3))

  //  sync within %syncreg, label %sync.continue, !UID !28, !BB_UID !29
  val sync_4 = Module(new SyncTC(ID = 4, NumInc=1, NumDec=1, NumOuts=1))

  //  %__begin.018 = phi i32 [ %inc, %pfor.inc ], [ 0, %pfor.detach.preheader ], !UID !30
  val phi__begin_0185 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 5, Res = false))

  //  detach within %syncreg, label %offload.pfor.body, label %pfor.inc, !UID !31, !BB_UID !32
  val detach_6 = Module(new Detach(ID = 6))

  //  %inc = add nuw nsw i32 %__begin.018, 1, !UID !33
  val binaryOp_inc7 = Module(new ComputeNode(NumOuts = 2, ID = 7, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, %n, !UID !34
  val icmp_exitcond8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %pfor.cond.cleanup.loopexit, label %pfor.detach, !llvm.loop !35, !UID !37, !BB_UID !38
  val br_9 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 9))

  //  ret i32 1, !UID !39, !BB_UID !40
  val ret_10 = Module(new RetNode2(retTypes = List(32), ID = 10))

  //  call void @saxpy_detach1(i32* %x, i32 %__begin.018, i32 %a, i32* %y), !UID !41
  val call_11_out = Module(new CallOutNode(ID = 11, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_11_in = Module(new CallInNode(ID = 11, argTypes = List()))

  //  reattach within %syncreg, label %pfor.inc, !UID !42, !BB_UID !43
  val reattach_12 = Module(new Reattach(NumPredOps= 1, ID = 12))


  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 1
  val const2 = Module(new ConstFastNode(value = 1, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_pfor_detach_preheader1.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_pfor_cond_cleanup3.io.predicateIn(1) <> br_1.io.FalseOutput(0)

  bb_pfor_cond_cleanup3.io.predicateIn(0) <> br_3.io.Out(0)

  bb_pfor_inc5.io.predicateIn(0) <> detach_6.io.Out(0)

  bb_sync_continue6.io.predicateIn(0) <> sync_4.io.Out(0)

  bb_offload_pfor_body7.io.predicateIn(0) <> detach_6.io.Out(1)


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_pfor_cond_cleanup_loopexit2.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_pfor_detach4.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_pfor_detach4.io.predicateIn(0) <> Loop_0.io.activate_loop_back


  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_4.io.incIn(0) <> detach_6.io.Out(2)

  sync_4.io.decIn(0) <> reattach_12.io.Out(0)


  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_2.io.Out(0)

  Loop_0.io.loopBack(0) <> br_9.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_9.io.TrueOutput(0)


  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_0.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field0")(0)


  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  call_11_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  call_11_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)

  call_11_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field2")(0)

  icmp_exitcond8.io.RightIO <> Loop_0.io.OutLiveIn.elements("field3")(0)


  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc7.io.Out(0)


  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_0185.io.InData(0) <> Loop_0.io.CarryDepenOut.elements("field0")(0)


  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  icmp_cmp170.io.enable <> bb_entry0.io.Out(1)


  br_1.io.enable <> bb_entry0.io.Out(2)


  br_2.io.enable <> bb_pfor_detach_preheader1.io.Out(0)


  br_3.io.enable <> bb_pfor_cond_cleanup_loopexit2.io.Out(0)


  sync_4.io.enable <> bb_pfor_cond_cleanup3.io.Out(0)


  const1.io.enable <> bb_pfor_detach4.io.Out(0)

  phi__begin_0185.io.enable <> bb_pfor_detach4.io.Out(1)


  detach_6.io.enable <> bb_pfor_detach4.io.Out(2)


  const2.io.enable <> bb_pfor_inc5.io.Out(0)

  binaryOp_inc7.io.enable <> bb_pfor_inc5.io.Out(1)


  icmp_exitcond8.io.enable <> bb_pfor_inc5.io.Out(2)


  br_9.io.enable <> bb_pfor_inc5.io.Out(3)


  const3.io.enable <> bb_sync_continue6.io.Out(0)

  ret_10.io.In.enable <> bb_sync_continue6.io.Out(1)


  call_11_in.io.enable <> bb_offload_pfor_body7.io.Out(1)

  call_11_out.io.enable <> bb_offload_pfor_body7.io.Out(0)


  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi__begin_0185.io.Mask <> bb_pfor_detach4.io.MaskBB(0)


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

  icmp_cmp170.io.RightIO <> const0.io.Out

  phi__begin_0185.io.InData(1) <> const1.io.Out

  binaryOp_inc7.io.RightIO <> const2.io.Out

  ret_10.io.In.data("field0") <> const3.io.Out

  br_1.io.CmpIO <> icmp_cmp170.io.Out(0)

  binaryOp_inc7.io.LeftIO <> phi__begin_0185.io.Out(0)

  call_11_out.io.In.elements("field1") <> phi__begin_0185.io.Out(1)

  icmp_exitcond8.io.LeftIO <> binaryOp_inc7.io.Out(1)

  br_9.io.CmpIO <> icmp_exitcond8.io.Out(0)

  icmp_cmp170.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  reattach_12.io.predicateIn(0).enq(DataBundle.active(1.U))


  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_11_in.io.In <> io.call_11_in

  io.call_11_out <> call_11_out.io.Out(0)

  reattach_12.io.enable <> call_11_in.io.Out.enable


  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_10.io.Out

}

import java.io.{File, FileWriter}

object cilk_saxpyTop extends App {
  val dir = new File("RTL/cilk_saxpyTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_saxpyDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
