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

abstract class cilk_for_test06_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class cilk_for_test06_detach1DF(implicit p: Parameters) extends cilk_for_test06_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 4, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 4, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(2, 5, 2, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 20, BID = 0))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds [2 x i32], [2 x i32]* %p1.in, i32 %__begin.037.in, i32 0, !UID !10
  val Gep_0 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 0)(ElementSize = 4, ArraySize = List(8)))

  //  %1 = load i32, i32* %0, align 4, !tbaa !11, !UID !15
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 1, RouteID = 0))

  //  %2 = getelementptr inbounds [2 x i32], [2 x i32]* %p1.in, i32 %__begin.037.in, i32 1, !UID !16
  val Gep_2 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 2)(ElementSize = 4, ArraySize = List(8)))

  //  %3 = load i32, i32* %2, align 4, !tbaa !11, !UID !17
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 1))

  //  %4 = getelementptr inbounds [2 x i32], [2 x i32]* %p2.in, i32 %__begin.037.in, i32 0, !UID !18
  val Gep_4 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 4)(ElementSize = 4, ArraySize = List(8)))

  //  %5 = load i32, i32* %4, align 4, !tbaa !11, !UID !19
  val ld_5 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 5, RouteID = 2))

  //  %6 = getelementptr inbounds [2 x i32], [2 x i32]* %p2.in, i32 %__begin.037.in, i32 1, !UID !20
  val Gep_6 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 6)(ElementSize = 4, ArraySize = List(8)))

  //  %7 = load i32, i32* %6, align 4, !tbaa !11, !UID !21
  val ld_7 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 7, RouteID = 3))

  //  %8 = sub nsw i32 %5, %1, !UID !22
  val binaryOp_8 = Module(new ComputeNode(NumOuts = 2, ID = 8, opCode = "sub")(sign = false))

  //  %9 = mul nsw i32 %8, %8, !UID !23
  val binaryOp_9 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "mul")(sign = false))

  //  %10 = sub nsw i32 %7, %3, !UID !24
  val binaryOp_10 = Module(new ComputeNode(NumOuts = 2, ID = 10, opCode = "sub")(sign = false))

  //  %11 = mul nsw i32 %10, %10, !UID !25
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "mul")(sign = false))

  //  %12 = add nuw nsw i32 %11, %9, !UID !26
  val binaryOp_12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign = false))

  //  %13 = getelementptr inbounds i32, i32* %d.in, i32 %__begin.037.in, !UID !27
  val Gep_13 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 13)(ElementSize = 4, ArraySize = List()))

  //  store i32 %12, i32* %13, align 4, !tbaa !11, !UID !28
  val st_14 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 14, RouteID = 0))

  //  ret void, !UID !29, !BB_UID !30
  val ret_15 = Module(new RetNode2(retTypes = List(), ID = 15))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 0
  val const2 = Module(new ConstFastNode(value = 0, ID = 2))

  //i32 1
  val const3 = Module(new ConstFastNode(value = 1, ID = 3))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body0.io.predicateIn(0) <> InputSplitter.io.Out.enable



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

  const0.io.enable <> bb_my_pfor_body0.io.Out(0)

  const1.io.enable <> bb_my_pfor_body0.io.Out(1)

  const2.io.enable <> bb_my_pfor_body0.io.Out(2)

  const3.io.enable <> bb_my_pfor_body0.io.Out(3)

  Gep_0.io.enable <> bb_my_pfor_body0.io.Out(4)


  ld_1.io.enable <> bb_my_pfor_body0.io.Out(5)


  Gep_2.io.enable <> bb_my_pfor_body0.io.Out(6)


  ld_3.io.enable <> bb_my_pfor_body0.io.Out(7)


  Gep_4.io.enable <> bb_my_pfor_body0.io.Out(8)


  ld_5.io.enable <> bb_my_pfor_body0.io.Out(9)


  Gep_6.io.enable <> bb_my_pfor_body0.io.Out(10)


  ld_7.io.enable <> bb_my_pfor_body0.io.Out(11)


  binaryOp_8.io.enable <> bb_my_pfor_body0.io.Out(12)


  binaryOp_9.io.enable <> bb_my_pfor_body0.io.Out(13)


  binaryOp_10.io.enable <> bb_my_pfor_body0.io.Out(14)


  binaryOp_11.io.enable <> bb_my_pfor_body0.io.Out(15)


  binaryOp_12.io.enable <> bb_my_pfor_body0.io.Out(16)


  Gep_13.io.enable <> bb_my_pfor_body0.io.Out(17)


  st_14.io.enable <> bb_my_pfor_body0.io.Out(18)


  ret_15.io.In.enable <> bb_my_pfor_body0.io.Out(19)




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

  MemCtrl.io.ReadIn(2) <> ld_5.io.memReq

  ld_5.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_7.io.memReq

  ld_7.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.WriteIn(0) <> st_14.io.memReq

  st_14.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_0.io.idx(1) <> const0.io.Out

  Gep_2.io.idx(1) <> const1.io.Out

  Gep_4.io.idx(1) <> const2.io.Out

  Gep_6.io.idx(1) <> const3.io.Out

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  binaryOp_8.io.RightIO <> ld_1.io.Out(0)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  binaryOp_10.io.RightIO <> ld_3.io.Out(0)

  ld_5.io.GepAddr <> Gep_4.io.Out(0)

  binaryOp_8.io.LeftIO <> ld_5.io.Out(0)

  ld_7.io.GepAddr <> Gep_6.io.Out(0)

  binaryOp_10.io.LeftIO <> ld_7.io.Out(0)

  binaryOp_9.io.LeftIO <> binaryOp_8.io.Out(0)

  binaryOp_9.io.RightIO <> binaryOp_8.io.Out(1)

  binaryOp_12.io.RightIO <> binaryOp_9.io.Out(0)

  binaryOp_11.io.LeftIO <> binaryOp_10.io.Out(0)

  binaryOp_11.io.RightIO <> binaryOp_10.io.Out(1)

  binaryOp_12.io.LeftIO <> binaryOp_11.io.Out(0)

  st_14.io.inData <> binaryOp_12.io.Out(0)

  st_14.io.GepAddr <> Gep_13.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_0.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_4.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_6.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(3)

  Gep_13.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(4)

  Gep_4.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_6.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  Gep_13.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(0)

  st_14.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_15.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test06_detach1Top extends App {
  val dir = new File("RTL/cilk_for_test06_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test06_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
