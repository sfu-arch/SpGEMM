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

abstract class bgemm_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val call_9_out = Decoupled(new Call(List(32, 32, 32, 32, 32)))
    val call_9_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class bgemm_detach1DF(implicit p: Parameters) extends bgemm_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_loopkk0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_my_pfor_detach101 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 5, NumPhi = 1, BID = 1))

  val bb_my_pfor_inc532 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 2))

  val bb_my_pfor_cond_cleanup83 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_my_sync_continue554 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))

  val bb_my_offload_loopi5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 5))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %my_pfor.detach10, !UID !10, !BB_UID !11
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %1 = phi i32 [ 0, %my_loopkk ], [ %3, %my_pfor.inc53 ], !UID !12
  val phi1 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 1, Res = true))

  //  %2 = shl nuw nsw i32 %1, 1, !UID !13
  val binaryOp_2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "shl")(sign = false))

  //  detach within %0, label %my_offload.loopi, label %my_pfor.inc53, !UID !14, !BB_UID !15
  val detach_3 = Module(new Detach(ID = 3))

  //  %3 = add nuw nsw i32 %1, 1, !UID !16
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 2, ID = 4, opCode = "add")(sign = false))

  //  %4 = icmp eq i32 %3, 2, !UID !17
  val icmp_5 = Module(new IcmpNode(NumOuts = 1, ID = 5, opCode = "eq")(sign = false))

  //  br i1 %4, label %my_pfor.cond.cleanup8, label %my_pfor.detach10, !llvm.loop !18, !UID !20, !BB_UID !21
  val br_6 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 6))

  //  sync within %0, label %my_sync.continue55, !UID !22, !BB_UID !23
  val sync_7 = Module(new SyncTC(ID = 7, NumInc=1, NumDec=1, NumOuts=1))

  //  ret void, !UID !24, !BB_UID !25
  val ret_8 = Module(new RetNode2(retTypes = List(), ID = 8))

  //  call void @bgemm_detach2(i32 %2, i32* %m1.in, i32 %mul1.in, i32* %m2.in, i32* %prod.in), !UID !26
  val call_9_out = Module(new CallOutNode(ID = 9, NumSuccOps = 0, argTypes = List(32,32,32,32,32)))

  val call_9_in = Module(new CallInNode(ID = 9, argTypes = List()))

  //  reattach within %0, label %my_pfor.inc53, !UID !27, !BB_UID !28
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

  bb_my_loopkk0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_my_pfor_inc532.io.predicateIn(0) <> detach_3.io.Out(0)

  bb_my_sync_continue554.io.predicateIn(0) <> sync_7.io.Out(0)

  bb_my_offload_loopi5.io.predicateIn(0) <> detach_3.io.Out(1)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_my_pfor_detach101.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_my_pfor_detach101.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_my_pfor_cond_cleanup83.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_7.io.incIn(0) <> detach_3.io.Out(2)

  sync_7.io.decIn(0) <> reattach_10.io.Out(0)



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

  Loop_0.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_0.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field3")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  call_9_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  call_9_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field1")(0)

  call_9_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field2")(0)

  call_9_out.io.In.elements("field4") <> Loop_0.io.OutLiveIn.elements("field3")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_4.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi1.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_my_loopkk0.io.Out(0)


  const0.io.enable <> bb_my_pfor_detach101.io.Out(0)

  const1.io.enable <> bb_my_pfor_detach101.io.Out(1)

  phi1.io.enable <> bb_my_pfor_detach101.io.Out(2)


  binaryOp_2.io.enable <> bb_my_pfor_detach101.io.Out(3)


  detach_3.io.enable <> bb_my_pfor_detach101.io.Out(4)


  const2.io.enable <> bb_my_pfor_inc532.io.Out(0)

  const3.io.enable <> bb_my_pfor_inc532.io.Out(1)

  binaryOp_4.io.enable <> bb_my_pfor_inc532.io.Out(2)


  icmp_5.io.enable <> bb_my_pfor_inc532.io.Out(3)


  br_6.io.enable <> bb_my_pfor_inc532.io.Out(4)


  sync_7.io.enable <> bb_my_pfor_cond_cleanup83.io.Out(0)


  ret_8.io.In.enable <> bb_my_sync_continue554.io.Out(0)


  call_9_in.io.enable <> bb_my_offload_loopi5.io.Out(1)

  call_9_out.io.enable <> bb_my_offload_loopi5.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi1.io.Mask <> bb_my_pfor_detach101.io.MaskBB(0)



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

  phi1.io.InData(0) <> const0.io.Out

  binaryOp_2.io.RightIO <> const1.io.Out

  binaryOp_4.io.RightIO <> const2.io.Out

  icmp_5.io.RightIO <> const3.io.Out

  binaryOp_2.io.LeftIO <> phi1.io.Out(0)

  binaryOp_4.io.LeftIO <> phi1.io.Out(1)

  call_9_out.io.In.elements("field0") <> binaryOp_2.io.Out(0)

  icmp_5.io.LeftIO <> binaryOp_4.io.Out(1)

  br_6.io.CmpIO <> icmp_5.io.Out(0)

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

object bgemm_detach1Top extends App {
  val dir = new File("RTL/bgemm_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new bgemm_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
