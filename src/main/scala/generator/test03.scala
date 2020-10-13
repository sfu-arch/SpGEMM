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

abstract class test03DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test03DF(implicit p: Parameters) extends test03DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(3, 3)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 9, BID = 0))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %3 = icmp slt i32 %1, %0, !UID !3
  val icmp_0 = Module(new IcmpNode(NumOuts = 2, ID = 0, opCode = "ult")(sign = false))

  //  %4 = select i1 %3, i32 %1, i32 0, !UID !4
  val select_1 = Module(new SelectNode(NumOuts = 1, ID = 1))

  //  %5 = sub nsw i32 %0, %4, !UID !5
  val binaryOp_2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "sub")(sign = false))

  //  %6 = select i1 %3, i32 0, i32 %0, !UID !6
  val select_3 = Module(new SelectNode(NumOuts = 1, ID = 3))

  //  %7 = sub nsw i32 %1, %6, !UID !7
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "sub")(sign = false))

  //  %8 = mul nsw i32 %5, %7, !UID !8
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "mul")(sign = false))

  //  ret i32 %8, !UID !9, !BB_UID !10
  val ret_6 = Module(new RetNode2(retTypes = List(32), ID = 6))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_0.io.predicateIn(0) <> InputSplitter.io.Out.enable



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

  const0.io.enable <> bb_0.io.Out(0)

  const1.io.enable <> bb_0.io.Out(1)

  icmp_0.io.enable <> bb_0.io.Out(2)

  select_1.io.enable <> bb_0.io.Out(3)

  binaryOp_2.io.enable <> bb_0.io.Out(4)

  select_3.io.enable <> bb_0.io.Out(5)

  binaryOp_4.io.enable <> bb_0.io.Out(6)

  binaryOp_5.io.enable <> bb_0.io.Out(7)

  ret_6.io.In.enable <> bb_0.io.Out(8)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */



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

  select_1.io.InData2 <> const0.io.Out

  select_3.io.InData1 <> const1.io.Out

  select_1.io.Select <> icmp_0.io.Out(0)

  select_3.io.Select <> icmp_0.io.Out(1)

  binaryOp_2.io.RightIO <> select_1.io.Out(0)

  binaryOp_5.io.LeftIO <> binaryOp_2.io.Out(0)

  binaryOp_4.io.RightIO <> select_3.io.Out(0)

  binaryOp_5.io.RightIO <> binaryOp_4.io.Out(0)

  ret_6.io.In.data("field0") <> binaryOp_5.io.Out(0)

  icmp_0.io.RightIO <> InputSplitter.io.Out.data.elements("field0")(0)

  binaryOp_2.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  select_3.io.InData2 <> InputSplitter.io.Out.data.elements("field0")(2)

  icmp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(0)

  select_1.io.InData1 <> InputSplitter.io.Out.data.elements("field1")(1)

  binaryOp_4.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")(2)



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_6.io.Out

}

import java.io.{File, FileWriter}

object test03Top extends App {
  val dir = new File("RTL/test03Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test03DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
