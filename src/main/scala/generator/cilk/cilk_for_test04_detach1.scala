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

abstract class cilk_for_test04_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class cilk_for_test04_detach1DF(implicit p: Parameters) extends cilk_for_test04_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 3, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val bb_my_pfor_preattach1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 7, BID = 1))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds i32, i32* %a.in, i32 %__begin.029.in, !UID !10
  val Gep_0 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 0)(ElementSize = 4, ArraySize = List()))

  //  %1 = load i32, i32* %0, align 4, !tbaa !11, !UID !15
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 1, RouteID = 0))

  //  %2 = getelementptr inbounds i32, i32* %b.in, i32 %__begin.029.in, !UID !16
  val Gep_2 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 2)(ElementSize = 4, ArraySize = List()))

  //  %3 = load i32, i32* %2, align 4, !tbaa !11, !UID !17
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 3, RouteID = 1))

  //  br label %my_pfor.preattach, !UID !18, !BB_UID !19
  val br_4 = Module(new UBranchNode(ID = 4))

  //  %4 = icmp ugt i32 %1, %3, !UID !20
  val icmp_5 = Module(new IcmpNode(NumOuts = 1, ID = 5, opCode = "ugt")(sign = false))

  //  %5 = sub i32 %1, %3, !UID !21
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "sub")(sign = false))

  //  %6 = sub i32 %3, %1, !UID !22
  val binaryOp_7 = Module(new ComputeNode(NumOuts = 1, ID = 7, opCode = "sub")(sign = false))

  //  %7 = select i1 %4, i32 %5, i32 %6, !UID !23
  val select_8 = Module(new SelectNode(NumOuts = 1, ID = 8))

  //  %8 = getelementptr inbounds i32, i32* %c.in, i32 %__begin.029.in, !UID !24
  val Gep_9 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 9)(ElementSize = 4, ArraySize = List()))

  //  store i32 %7, i32* %8, align 4, !tbaa !11, !UID !25
  val st_10 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 10, RouteID = 0))

  //  ret void, !UID !26, !BB_UID !27
  val ret_11 = Module(new RetNode2(retTypes = List(), ID = 11))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_my_pfor_preattach1.io.predicateIn(0) <> br_4.io.Out(0)



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

  Gep_0.io.enable <> bb_my_pfor_body0.io.Out(0)


  ld_1.io.enable <> bb_my_pfor_body0.io.Out(1)


  Gep_2.io.enable <> bb_my_pfor_body0.io.Out(2)


  ld_3.io.enable <> bb_my_pfor_body0.io.Out(3)


  br_4.io.enable <> bb_my_pfor_body0.io.Out(4)


  icmp_5.io.enable <> bb_my_pfor_preattach1.io.Out(0)


  binaryOp_6.io.enable <> bb_my_pfor_preattach1.io.Out(1)


  binaryOp_7.io.enable <> bb_my_pfor_preattach1.io.Out(2)


  select_8.io.enable <> bb_my_pfor_preattach1.io.Out(3)


  Gep_9.io.enable <> bb_my_pfor_preattach1.io.Out(4)


  st_10.io.enable <> bb_my_pfor_preattach1.io.Out(5)


  ret_11.io.In.enable <> bb_my_pfor_preattach1.io.Out(6)




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

  MemCtrl.io.WriteIn(0) <> st_10.io.memReq

  st_10.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  icmp_5.io.LeftIO <> ld_1.io.Out(0)

  binaryOp_6.io.LeftIO <> ld_1.io.Out(1)

  binaryOp_7.io.RightIO <> ld_1.io.Out(2)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  icmp_5.io.RightIO <> ld_3.io.Out(0)

  binaryOp_6.io.RightIO <> ld_3.io.Out(1)

  binaryOp_7.io.LeftIO <> ld_3.io.Out(2)

  select_8.io.Select <> icmp_5.io.Out(0)

  select_8.io.InData1 <> binaryOp_6.io.Out(0)

  select_8.io.InData2 <> binaryOp_7.io.Out(0)

  st_10.io.inData <> select_8.io.Out(0)

  st_10.io.GepAddr <> Gep_9.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_0.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_9.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_9.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(0)

  st_10.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_11.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test04_detach1Top extends App {
  val dir = new File("RTL/cilk_for_test04_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test04_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
