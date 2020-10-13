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

abstract class cilk_for_test05_detach2DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class cilk_for_test05_detach2DF(implicit p: Parameters) extends cilk_for_test05_detach2DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 3, 3, 1, 1)))
  InputSplitter.io.In <> io.in

  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body130 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 8, BID = 0))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds [5 x i32], [5 x i32]* %a.in, i32 %__begin.044.in, i32 %__begin3.043.in, !UID !21
  val Gep_0 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 0)(ElementSize = 4, ArraySize = List(20)))

  //  %1 = load i32, i32* %0, align 4, !tbaa !22, !UID !26
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 1, RouteID = 0))

  //  %2 = getelementptr inbounds [5 x i32], [5 x i32]* %b.in, i32 %__begin.044.in, i32 %__begin3.043.in, !UID !27
  val Gep_2 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 2)(ElementSize = 4, ArraySize = List(20)))

  //  %3 = load i32, i32* %2, align 4, !tbaa !22, !UID !28
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 1))

  //  %4 = add nsw i32 %3, %1, !UID !29
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "add")(sign = false))

  //  %5 = getelementptr inbounds [5 x i32], [5 x i32]* %c.in, i32 %__begin.044.in, i32 %__begin3.043.in, !UID !30
  val Gep_5 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 5)(ElementSize = 4, ArraySize = List(20)))

  //  store i32 %4, i32* %5, align 4, !tbaa !22, !UID !31
  val st_6 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 1, ID = 6, RouteID = 0))

  //  ret void, !UID !32, !BB_UID !33
  val ret_7 = Module(new RetNode2(retTypes = List(), ID = 7))


  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body130.io.predicateIn(0) <> InputSplitter.io.Out.enable


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */


  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */


  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */


  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  Gep_0.io.enable <> bb_my_pfor_body130.io.Out(0)


  ld_1.io.enable <> bb_my_pfor_body130.io.Out(1)


  Gep_2.io.enable <> bb_my_pfor_body130.io.Out(2)


  ld_3.io.enable <> bb_my_pfor_body130.io.Out(3)


  binaryOp_4.io.enable <> bb_my_pfor_body130.io.Out(4)


  Gep_5.io.enable <> bb_my_pfor_body130.io.Out(5)


  st_6.io.enable <> bb_my_pfor_body130.io.Out(6)


  //  ret_7.io.In.enable <> bb_my_pfor_body130.io.Out(7)
  bb_my_pfor_body130.io.Out(7).ready := true.B


  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_1.io.memReq

  ld_1.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_3.io.memReq

  ld_3.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_6.io.memReq

  st_6.io.memResp <> MemCtrl.io.WriteOut(0)


  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  binaryOp_4.io.RightIO <> ld_1.io.Out(0)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  binaryOp_4.io.LeftIO <> ld_3.io.Out(0)

  st_6.io.inData <> binaryOp_4.io.Out(0)

  st_6.io.GepAddr <> Gep_5.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_0.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_5.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_0.io.idx(1) <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_2.io.idx(1) <> InputSplitter.io.Out.data.elements("field2")(1)

  Gep_5.io.idx(1) <> InputSplitter.io.Out.data.elements("field2")(2)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(0)

  Gep_5.io.baseAddress <> InputSplitter.io.Out.data.elements("field4")(0)

  st_6.io.Out(0).ready := true.B


  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  ret_7.io.In.enable <> st_6.io.SuccOp(0)
  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test05_detach2Top extends App {
  val dir = new File("RTL/cilk_for_test05_detach2Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test05_detach2DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
