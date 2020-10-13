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

abstract class test11DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32))))
    val call_5_out = Decoupled(new Call(List(32)))
    val call_5_in = Flipped(Decoupled(new Call(List(32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test11DF(implicit p: Parameters) extends test11DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1), NumOuts = List(1), NumCarry = List(1, 1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_for_cond_cleanup1 = Module(new BasicBlockNode(NumInputs = 1, NumOuts = 2, NumPhi = 1, BID = 1))

  val bb_for_body2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 2, BID = 2))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %for.body, !dbg !22, !UID !23, !BB_UID !24
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %add.lcssa = phi i32 [ %add, %for.body ], !UID !25
  val phiadd_lcssa1 = Module(new PhiFastNode(NumInputs = 1, NumOutputs = 1, ID = 1, Res = false))

  //  ret i32 %add.lcssa, !dbg !26, !UID !27, !BB_UID !28
  val ret_2 = Module(new RetNode2(retTypes = List(32), ID = 2))

  //  %i.07 = phi i32 [ 0, %entry ], [ %inc, %for.body ], !UID !29
  val phii_073 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 3, Res = true))

  //  %foo.06 = phi i32 [ %j, %entry ], [ %add, %for.body ], !UID !30
  val phifoo_064 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 4, Res = true))

  //  %call = tail call i32 @test11_inner(i32 %foo.06), !dbg !31, !UID !34
  val call_5_out = Module(new CallOutNode(ID = 5, NumSuccOps = 0, argTypes = List(32)))

  val call_5_in = Module(new CallInNode(ID = 5, argTypes = List(32)))

  //  %add = add i32 %call, %foo.06, !dbg !35, !UID !36
  val binaryOp_add6 = Module(new ComputeNode(NumOuts = 2, ID = 6, opCode = "add")(sign = false))

  //  %inc = add nuw nsw i32 %i.07, 1, !dbg !37, !UID !38
  val binaryOp_inc7 = Module(new ComputeNode(NumOuts = 2, ID = 7, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, 5, !dbg !39, !UID !40
  val icmp_exitcond8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup, label %for.body, !dbg !22, !llvm.loop !41, !UID !43, !BB_UID !44
  val br_9 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 1, ID = 9))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 5
  val const2 = Module(new ConstFastNode(value = 5, ID = 2))



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

  Loop_0.io.loopBack(0) <> br_9.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_9.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  phifoo_064.io.InData(0) <> Loop_0.io.OutLiveIn.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_0.io.InLiveOut(0) <> binaryOp_add6.io.Out(0)



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */

  phiadd_lcssa1.io.InData(0) <> Loop_0.io.OutLiveOut.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc7.io.Out(0)

  Loop_0.io.CarryDepenIn(1) <> binaryOp_add6.io.Out(1)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phii_073.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phifoo_064.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field1")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_entry0.io.Out(0)


  phiadd_lcssa1.io.enable <> bb_for_cond_cleanup1.io.Out(0)

  ret_2.io.In.enable <> bb_for_cond_cleanup1.io.Out(1)


  const0.io.enable <> bb_for_body2.io.Out(0)

  const1.io.enable <> bb_for_body2.io.Out(1)

  const2.io.enable <> bb_for_body2.io.Out(2)

  phii_073.io.enable <> bb_for_body2.io.Out(3)

  phifoo_064.io.enable <> bb_for_body2.io.Out(4)

  call_5_in.io.enable.enq(ControlBundle.active())

  call_5_out.io.enable <> bb_for_body2.io.Out(5)

  binaryOp_add6.io.enable <> bb_for_body2.io.Out(6)

  binaryOp_inc7.io.enable <> bb_for_body2.io.Out(7)

  icmp_exitcond8.io.enable <> bb_for_body2.io.Out(8)

  br_9.io.enable <> bb_for_body2.io.Out(9)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phiadd_lcssa1.io.Mask <> bb_for_cond_cleanup1.io.MaskBB(0)

  phii_073.io.Mask <> bb_for_body2.io.MaskBB(0)

  phifoo_064.io.Mask <> bb_for_body2.io.MaskBB(1)



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

  phii_073.io.InData(0) <> const0.io.Out

  binaryOp_inc7.io.RightIO <> const1.io.Out

  icmp_exitcond8.io.RightIO <> const2.io.Out

  ret_2.io.In.data("field0") <> phiadd_lcssa1.io.Out(0)

  binaryOp_inc7.io.LeftIO <> phii_073.io.Out(0)

  call_5_out.io.In("field0") <> phifoo_064.io.Out(0)

  binaryOp_add6.io.RightIO <> phifoo_064.io.Out(1)

  binaryOp_add6.io.LeftIO <> call_5_in.io.Out.data("field0")

  icmp_exitcond8.io.LeftIO <> binaryOp_inc7.io.Out(1)

  br_9.io.CmpIO <> icmp_exitcond8.io.Out(0)



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_5_in.io.In <> io.call_5_in

  io.call_5_out <> call_5_out.io.Out(0)

  br_9.io.PredOp(0) <> call_5_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_2.io.Out

}

import java.io.{File, FileWriter}

object test11Top extends App {
  val dir = new File("RTL/test11Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test11DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
