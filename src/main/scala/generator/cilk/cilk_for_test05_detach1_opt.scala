package dataflow

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

abstract class cilk_for_test05_detach1_optDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class cilk_for_test05_detach1_optDF(implicit p: Parameters) extends cilk_for_test05_detach1_optDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 6, NWrites = 2)
  (WControl = new WriteMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 6, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(3, 8, 3, 2)))
  InputSplitter.io.In <> io.in


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode3(NumOuts = 6, BID = 0))

  val bb_my_if_then1 = Module(new BasicBlockNoMaskFastNode3(NumOuts = 8, BID = 1))

  val bb_my_if_end2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 1, NumPhi = 0, BID = 2))

  val bb_my_pfor_preattach3 = Module(new BasicBlockNoMaskFastNode3(NumOuts = 1, BID = 3))

  val bb_my_if_else4 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 8, BID = 4))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds i32, i32* %a.in, i32 %i.0.in, !UID !1
  val Gep_0 = Module(new GepArrayOneNode(NumOuts = 1, ID = 0)(numByte = 4)(size = 1))

  //  %1 = load i32, i32* %0, align 4, !UID !2
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 1, RouteID = 0))

  //  %2 = getelementptr inbounds i32, i32* %b.in, i32 %i.0.in, !UID !3
  val Gep_2 = Module(new GepArrayOneNode(NumOuts = 1, ID = 2)(numByte = 4)(size = 1))

  //  %3 = load i32, i32* %2, align 4, !UID !4
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 1))

  //  %4 = icmp ugt i32 %1, %3, !UID !5
  val icmp_4 = Module(new IcmpFastNode(NumOuts = 1, ID = 4, opCode = "ugt")(sign = false))

  //  br i1 %4, label %my_if.then, label %my_if.else, !UID !6, !BB_UID !7
  //  val br_5 = Module(new CBranchNode(ID = 5))
  val br_5 = Module(new CBranchFastNode(ID = 5))

  //  %5 = getelementptr inbounds i32, i32* %a.in, i32 %i.0.in, !UID !8
  val Gep_6 = Module(new GepArrayOneNode(NumOuts = 1, ID = 6)(numByte = 4)(size = 1))

  //  %6 = load i32, i32* %5, align 4, !UID !9
  val ld_7 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 7, RouteID = 2))

  //  %7 = getelementptr inbounds i32, i32* %b.in, i32 %i.0.in, !UID !10
  val Gep_8 = Module(new GepArrayOneNode(NumOuts = 1, ID = 8)(numByte = 4)(size = 1))

  //  %8 = load i32, i32* %7, align 4, !UID !11
  val ld_9 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 9, RouteID = 3))

  //  %9 = sub i32 %6, %8, !UID !12
  val binaryOp_10 = Module(new ComputeFastNode(NumOuts = 1, ID = 10, opCode = "sub")(sign = false))

  //  %10 = getelementptr inbounds i32, i32* %c.in, i32 %i.0.in, !UID !13
  val Gep_11 = Module(new GepArrayOneNode(NumOuts = 1, ID = 11)(numByte = 4)(size = 1))

  //  store i32 %9, i32* %10, align 4, !UID !14
  val st_12 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 12, RouteID = 0))

  //  br label %my_if.end, !UID !15, !BB_UID !16
  val br_13 = Module(new UBranchFastNode(ID = 13))

  //  br label %my_pfor.preattach, !UID !17, !BB_UID !18
  val br_14 = Module(new UBranchFastNode(ID = 14))

  //  ret void
  val ret_15 = Module(new RetNode2(retTypes = List(), ID = 15))

  //  %11 = getelementptr inbounds i32, i32* %b.in, i32 %i.0.in, !UID !19
  val Gep_16 = Module(new GepArrayOneNode(NumOuts = 1, ID = 16)(numByte = 4)(size = 1))

  //  %12 = load i32, i32* %11, align 4, !UID !20
  val ld_17 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 17, RouteID = 4))

  //  %13 = getelementptr inbounds i32, i32* %a.in, i32 %i.0.in, !UID !21
  val Gep_18 = Module(new GepArrayOneNode(NumOuts = 1, ID = 18)(numByte = 4)(size = 1))

  //  %14 = load i32, i32* %13, align 4, !UID !22
  val ld_19 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 19, RouteID = 5))

  //  %15 = sub i32 %12, %14, !UID !23
  val binaryOp_20 = Module(new ComputeFastNode(NumOuts = 1, ID = 20, opCode = "sub")(sign = false))

  //  %16 = getelementptr inbounds i32, i32* %c.in, i32 %i.0.in, !UID !24
  val Gep_21 = Module(new GepArrayOneNode(NumOuts = 1, ID = 21)(numByte = 4)(size = 1))

  //  store i32 %15, i32* %16, align 4, !UID !25
  val st_22 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 22, RouteID = 1))

  //  br label %my_if.end, !UID !26, !BB_UID !27
  val br_23 = Module(new UBranchFastNode(ID = 23))


  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */


  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_my_if_then1.io.predicateIn <> br_5.io.Out(0)

  bb_my_if_end2.io.predicateIn(0) <> br_13.io.Out(0)

  bb_my_if_end2.io.predicateIn(1) <> br_23.io.Out(0)

  bb_my_pfor_preattach3.io.predicateIn <> br_14.io.Out(0)

  bb_my_if_else4.io.predicateIn <> br_5.io.Out(1)


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
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  Gep_0.io.enable <> bb_my_pfor_body0.io.Out(0)

  ld_1.io.enable <> bb_my_pfor_body0.io.Out(1)

  Gep_2.io.enable <> bb_my_pfor_body0.io.Out(2)

  ld_3.io.enable <> bb_my_pfor_body0.io.Out(3)

  icmp_4.io.enable <> bb_my_pfor_body0.io.Out(4)

  br_5.io.enable <> bb_my_pfor_body0.io.Out(5)


  Gep_6.io.enable <> bb_my_if_then1.io.Out(0)

  ld_7.io.enable <> bb_my_if_then1.io.Out(1)

  Gep_8.io.enable <> bb_my_if_then1.io.Out(2)

  ld_9.io.enable <> bb_my_if_then1.io.Out(3)

  binaryOp_10.io.enable <> bb_my_if_then1.io.Out(4)

  Gep_11.io.enable <> bb_my_if_then1.io.Out(5)

  st_12.io.enable <> bb_my_if_then1.io.Out(6)

  br_13.io.enable <> bb_my_if_then1.io.Out(7)


  br_14.io.enable <> bb_my_if_end2.io.Out(0)


  ret_15.io.In.enable <> bb_my_pfor_preattach3.io.Out(0)


  Gep_16.io.enable <> bb_my_if_else4.io.Out(0)

  ld_17.io.enable <> bb_my_if_else4.io.Out(1)

  Gep_18.io.enable <> bb_my_if_else4.io.Out(2)

  ld_19.io.enable <> bb_my_if_else4.io.Out(3)

  binaryOp_20.io.enable <> bb_my_if_else4.io.Out(4)

  Gep_21.io.enable <> bb_my_if_else4.io.Out(5)

  st_22.io.enable <> bb_my_if_else4.io.Out(6)

  br_23.io.enable <> bb_my_if_else4.io.Out(7)


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

  MemCtrl.io.ReadIn(2) <> ld_7.io.memReq

  ld_7.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_9.io.memReq

  ld_9.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.WriteIn(0) <> st_12.io.memReq

  st_12.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(4) <> ld_17.io.memReq

  ld_17.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_19.io.memReq

  ld_19.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.WriteIn(1) <> st_22.io.memReq

  st_22.io.memResp <> MemCtrl.io.WriteOut(1)


  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  icmp_4.io.LeftIO <> ld_1.io.Out(0)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  icmp_4.io.RightIO <> ld_3.io.Out(0)

  br_5.io.CmpIO <> icmp_4.io.Out(0)

  ld_7.io.GepAddr <> Gep_6.io.Out(0)

  binaryOp_10.io.LeftIO <> ld_7.io.Out(0)

  ld_9.io.GepAddr <> Gep_8.io.Out(0)

  binaryOp_10.io.RightIO <> ld_9.io.Out(0)

  st_12.io.inData <> binaryOp_10.io.Out(0)

  st_12.io.GepAddr <> Gep_11.io.Out(0)

  ld_17.io.GepAddr <> Gep_16.io.Out(0)

  binaryOp_20.io.LeftIO <> ld_17.io.Out(0)

  ld_19.io.GepAddr <> Gep_18.io.Out(0)

  binaryOp_20.io.RightIO <> ld_19.io.Out(0)

  st_22.io.inData <> binaryOp_20.io.Out(0)

  st_22.io.GepAddr <> Gep_21.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_6.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_18.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  Gep_0.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_6.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_8.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(3)

  Gep_11.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(4)

  Gep_16.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(5)

  Gep_18.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(6)

  Gep_21.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(7)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_8.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  Gep_16.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(2)

  Gep_11.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(0)

  Gep_21.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(1)

  st_12.io.Out(0).ready := true.B

  st_22.io.Out(0).ready := true.B


  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_15.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test05_detach1_optMain extends App {
  val dir = new File("RTL/cilk_for_test05_detach1_opt");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test05_detach1_optDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
