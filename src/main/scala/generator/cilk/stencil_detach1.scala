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

abstract class stencil_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val call_4_out = Decoupled(new Call(List(32, 32, 32, 32, 32)))
    val call_4_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class stencil_detach1DF(implicit p: Parameters) extends stencil_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 1, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(3, 1, 2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val bb_my_for_body1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 9, NumPhi = 1, BID = 1))

  val bb_my_for_cond_cleanup2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 8, BID = 2))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = lshr i32 %__begin.031.in, 2, !UID !21
  val binaryOp_0 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "lshr")(sign = false))

  //  %1 = and i32 %__begin.031.in, 3, !UID !22
  val binaryOp_1 = Module(new ComputeNode(NumOuts = 1, ID = 1, opCode = "and")(sign = false))

  //  br label %my_for.body, !UID !23, !BB_UID !24
  val br_2 = Module(new UBranchNode(ID = 2))

  //  %2 = phi i32 [ 0, %my_pfor.body ], [ %3, %my_for.body ], !UID !25
  val phi3 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 3, Res = true))

  //  tail call void @stencil_inner(i32* %in.in, i32* %out.in, i32 %0, i32 %1, i32 %2), !UID !26
  val call_4_out = Module(new CallOutNode(ID = 4, NumSuccOps = 0, argTypes = List(32,32,32,32,32)))

  val call_4_in = Module(new CallInNode(ID = 4, argTypes = List()))

  //  %3 = add nuw nsw i32 %2, 1, !UID !27
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 2, ID = 5, opCode = "add")(sign = false))

  //  %4 = icmp eq i32 %3, 3, !UID !28
  val icmp_6 = Module(new IcmpNode(NumOuts = 1, ID = 6, opCode = "eq")(sign = false))

  //  br i1 %4, label %my_for.cond.cleanup, label %my_for.body, !UID !29, !BB_UID !30
  val br_7 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 1, ID = 7))

  //  %5 = getelementptr inbounds i32, i32* %out.in, i32 %__begin.031.in, !UID !31
  val Gep_8 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 8)(ElementSize = 4, ArraySize = List()))

  //  %6 = load i32, i32* %5, align 4, !tbaa !32, !UID !36
  val ld_9 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 9, RouteID = 0))

  //  %7 = add i32 %6, 9, !UID !37
  val binaryOp_10 = Module(new ComputeNode(NumOuts = 1, ID = 10, opCode = "add")(sign = false))

  //  %8 = udiv i32 %7, 9, !UID !38
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "udiv")(sign = false))

  //  store i32 %8, i32* %5, align 4, !tbaa !32, !UID !39
  val st_12 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 12, RouteID = 0))

  //  ret void, !UID !40, !BB_UID !41
  val ret_13 = Module(new RetNode2(retTypes = List(), ID = 13))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 2
  val const0 = Module(new ConstFastNode(value = 2, ID = 0))

  //i32 3
  val const1 = Module(new ConstFastNode(value = 3, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))

  //i32 3
  val const4 = Module(new ConstFastNode(value = 3, ID = 4))

  //i32 9
  val const5 = Module(new ConstFastNode(value = 9, ID = 5))

  //i32 9
  val const6 = Module(new ConstFastNode(value = 9, ID = 6))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_my_for_body1.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_my_for_body1.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_my_for_cond_cleanup2.io.predicateIn(0) <> Loop_0.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_2.io.Out(0)

  Loop_0.io.loopBack(0) <> br_7.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_7.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_0.io.InLiveIn(1) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_0.io.InLiveIn(2) <> binaryOp_0.io.Out(0)

  Loop_0.io.InLiveIn(3) <> binaryOp_1.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  call_4_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field0")(0)

  call_4_out.io.In.elements("field1") <> Loop_0.io.OutLiveIn.elements("field1")(0)

  call_4_out.io.In.elements("field2") <> Loop_0.io.OutLiveIn.elements("field2")(0)

  call_4_out.io.In.elements("field3") <> Loop_0.io.OutLiveIn.elements("field3")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_5.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi3.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_my_pfor_body0.io.Out(0)

  const1.io.enable <> bb_my_pfor_body0.io.Out(1)

  binaryOp_0.io.enable <> bb_my_pfor_body0.io.Out(2)


  binaryOp_1.io.enable <> bb_my_pfor_body0.io.Out(3)


  br_2.io.enable <> bb_my_pfor_body0.io.Out(4)


  const2.io.enable <> bb_my_for_body1.io.Out(0)

  const3.io.enable <> bb_my_for_body1.io.Out(1)

  const4.io.enable <> bb_my_for_body1.io.Out(2)

  phi3.io.enable <> bb_my_for_body1.io.Out(3)


  call_4_in.io.enable <> bb_my_for_body1.io.Out(5)

  call_4_out.io.enable <> bb_my_for_body1.io.Out(4)


  binaryOp_5.io.enable <> bb_my_for_body1.io.Out(6)


  icmp_6.io.enable <> bb_my_for_body1.io.Out(7)


  br_7.io.enable <> bb_my_for_body1.io.Out(8)


  const5.io.enable <> bb_my_for_cond_cleanup2.io.Out(0)

  const6.io.enable <> bb_my_for_cond_cleanup2.io.Out(1)

  Gep_8.io.enable <> bb_my_for_cond_cleanup2.io.Out(2)


  ld_9.io.enable <> bb_my_for_cond_cleanup2.io.Out(3)


  binaryOp_10.io.enable <> bb_my_for_cond_cleanup2.io.Out(4)


  binaryOp_11.io.enable <> bb_my_for_cond_cleanup2.io.Out(5)


  st_12.io.enable <> bb_my_for_cond_cleanup2.io.Out(6)


  ret_13.io.In.enable <> bb_my_for_cond_cleanup2.io.Out(7)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi3.io.Mask <> bb_my_for_body1.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_9.io.memReq

  ld_9.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> st_12.io.memReq

  st_12.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  binaryOp_0.io.RightIO <> const0.io.Out

  binaryOp_1.io.RightIO <> const1.io.Out

  phi3.io.InData(0) <> const2.io.Out

  binaryOp_5.io.RightIO <> const3.io.Out

  icmp_6.io.RightIO <> const4.io.Out

  binaryOp_10.io.RightIO <> const5.io.Out

  binaryOp_11.io.RightIO <> const6.io.Out

  call_4_out.io.In.elements("field4") <> phi3.io.Out(0)

  binaryOp_5.io.LeftIO <> phi3.io.Out(1)

  icmp_6.io.LeftIO <> binaryOp_5.io.Out(1)

  br_7.io.CmpIO <> icmp_6.io.Out(0)

  ld_9.io.GepAddr <> Gep_8.io.Out(0)

  st_12.io.GepAddr <> Gep_8.io.Out(1)

  binaryOp_10.io.LeftIO <> ld_9.io.Out(0)

  binaryOp_11.io.LeftIO <> binaryOp_10.io.Out(0)

  st_12.io.inData <> binaryOp_11.io.Out(0)

  binaryOp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(0)

  binaryOp_1.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_8.io.idx(0) <> InputSplitter.io.Out.data.elements("field0")(2)

  Gep_8.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  st_12.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_4_in.io.In <> io.call_4_in

  io.call_4_out <> call_4_out.io.Out(0)

  br_7.io.PredOp(0) <> call_4_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_13.io.Out

}

import java.io.{File, FileWriter}

object stencil_detach1Top extends App {
  val dir = new File("RTL/stencil_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new stencil_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
