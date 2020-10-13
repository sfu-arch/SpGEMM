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

abstract class stencilDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val call_8_out = Decoupled(new Call(List(32, 32, 32)))
    val call_8_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class stencilDF(implicit p: Parameters) extends stencilDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_pfor_cond_cleanup1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_pfor_detach2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 2))

  val bb_pfor_inc3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 3))

  val bb_sync_continue4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))

  val bb_offload_pfor_body5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 5))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %pfor.detach, !UID !21, !BB_UID !22
  val br_0 = Module(new UBranchNode(ID = 0))

  //  sync within %syncreg, label %sync.continue, !UID !23, !BB_UID !24
  val sync_1 = Module(new SyncTC(ID = 1, NumInc=1, NumDec=1, NumOuts=1))

  //  %__begin.031 = phi i32 [ 0, %entry ], [ %inc10, %pfor.inc ], !UID !25
  val phi__begin_0312 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 2, Res = true))

  //  detach within %syncreg, label %offload.pfor.body, label %pfor.inc, !UID !26, !BB_UID !27
  val detach_3 = Module(new Detach(ID = 3))

  //  %inc10 = add nuw nsw i32 %__begin.031, 1, !UID !28
  val binaryOp_inc104 = Module(new ComputeNode(NumOuts = 2, ID = 4, opCode = "add")(sign = false))

  //  %exitcond33 = icmp eq i32 %inc10, 16, !UID !29
  val icmp_exitcond335 = Module(new IcmpNode(NumOuts = 1, ID = 5, opCode = "eq")(sign = false))

  //  br i1 %exitcond33, label %pfor.cond.cleanup, label %pfor.detach, !llvm.loop !30, !UID !32, !BB_UID !33
  val br_6 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 6))

  //  ret void, !UID !34, !BB_UID !35
  val ret_7 = Module(new RetNode2(retTypes = List(), ID = 7))

  //  call void @stencil_detach1(i32 %__begin.031, i32* %in, i32* %out), !UID !36
  val call_8_out = Module(new CallOutNode(ID = 8, NumSuccOps = 0, argTypes = List(32,32,32)))

  val call_8_in = Module(new CallInNode(ID = 8, argTypes = List()))

  //  reattach within %syncreg, label %pfor.inc, !UID !37, !BB_UID !38
  val reattach_9 = Module(new Reattach(NumPredOps= 1, ID = 9))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 16
  val const2 = Module(new ConstFastNode(value = 16, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_pfor_inc3.io.predicateIn(0) <> detach_3.io.Out(0)

  bb_sync_continue4.io.predicateIn(0) <> sync_1.io.Out(0)

  bb_offload_pfor_body5.io.predicateIn(0) <> detach_3.io.Out(1)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_pfor_cond_cleanup1.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_pfor_detach2.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_pfor_detach2.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_1.io.incIn(0) <> detach_3.io.Out(2)

  sync_1.io.decIn(0) <> reattach_9.io.Out(0)



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_0.io.Out(0)

  Loop_0.io.loopBack(0) <> br_6.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_6.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  call_8_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  call_8_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc104.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_0312.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_entry0.io.Out(0)


  sync_1.io.enable <> bb_pfor_cond_cleanup1.io.Out(0)


  const0.io.enable <> bb_pfor_detach2.io.Out(0)

  phi__begin_0312.io.enable <> bb_pfor_detach2.io.Out(1)


  detach_3.io.enable <> bb_pfor_detach2.io.Out(2)


  const1.io.enable <> bb_pfor_inc3.io.Out(0)

  const2.io.enable <> bb_pfor_inc3.io.Out(1)

  binaryOp_inc104.io.enable <> bb_pfor_inc3.io.Out(2)


  icmp_exitcond335.io.enable <> bb_pfor_inc3.io.Out(3)


  br_6.io.enable <> bb_pfor_inc3.io.Out(4)


  ret_7.io.In.enable <> bb_sync_continue4.io.Out(0)


  call_8_in.io.enable <> bb_offload_pfor_body5.io.Out(1)

  call_8_out.io.enable <> bb_offload_pfor_body5.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi__begin_0312.io.Mask <> bb_pfor_detach2.io.MaskBB(0)



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

  phi__begin_0312.io.InData(0) <> const0.io.Out

  binaryOp_inc104.io.RightIO <> const1.io.Out

  icmp_exitcond335.io.RightIO <> const2.io.Out

  binaryOp_inc104.io.LeftIO <> phi__begin_0312.io.Out(0)

  call_8_out.io.In.elements("field0") <> phi__begin_0312.io.Out(1)

  icmp_exitcond335.io.LeftIO <> binaryOp_inc104.io.Out(1)

  br_6.io.CmpIO <> icmp_exitcond335.io.Out(0)

  reattach_9.io.predicateIn(0).enq(DataBundle.active(1.U))



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_8_in.io.In <> io.call_8_in

  io.call_8_out <> call_8_out.io.Out(0)

  reattach_9.io.enable <> call_8_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}

object stencilTop extends App {
  val dir = new File("RTL/stencilTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new stencilDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
