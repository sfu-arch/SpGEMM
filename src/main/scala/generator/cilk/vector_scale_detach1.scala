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

abstract class vector_scale_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class vector_scale_detach1DF(implicit p: Parameters) extends vector_scale_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 1, NWrites = 2)
  (WControl = new WriteMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 3, 2, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val bb_my_if_then1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 1))

  val bb_my_pfor_preattach2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 1, BID = 2))

  val bb_my_if_else3 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 10, BID = 3))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds i32, i32* %a.in, i32 %__begin.030.in, !UID !21
  val Gep_0 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 0)(ElementSize = 4, ArraySize = List()))

  //  %1 = load i32, i32* %0, align 4, !tbaa !22, !UID !26
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 2, ID = 1, RouteID = 0))

  //  %2 = icmp slt i32 %1, 0, !UID !27
  val icmp_2 = Module(new IcmpNode(NumOuts = 1, ID = 2, opCode = "ult")(sign = false))

  //  br i1 %2, label %my_if.then, label %my_if.else, !UID !28, !BB_UID !29
  val br_3 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 3))

  //  %3 = getelementptr inbounds i32, i32* %c.in, i32 %__begin.030.in, !UID !30
  val Gep_4 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 4)(ElementSize = 4, ArraySize = List()))

  //  store i32 0, i32* %3, align 4, !tbaa !22, !UID !31
  val st_5 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 5, RouteID = 0))

  //  br label %my_pfor.preattach, !UID !32, !BB_UID !33
  val br_6 = Module(new UBranchNode(ID = 6))

  //  ret void, !UID !34, !BB_UID !35
  val ret_7 = Module(new RetNode2(retTypes = List(), ID = 7))

  //  %4 = mul nsw i32 %1, %scale.in, !UID !36
  val binaryOp_8 = Module(new ComputeNode(NumOuts = 2, ID = 8, opCode = "mul")(sign = false))

  //  %5 = ashr i32 %4, 8, !UID !37
  val binaryOp_9 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "ashr")(sign = false))

  //  %6 = getelementptr inbounds i32, i32* %c.in, i32 %__begin.030.in, !UID !38
  val Gep_10 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 10)(ElementSize = 4, ArraySize = List()))

  //  %7 = icmp sgt i32 %4, 65535, !UID !39
  val icmp_11 = Module(new IcmpNode(NumOuts = 1, ID = 11, opCode = "ugt")(sign = false))

  //  %8 = select i1 %7, i32 255, i32 %5, !UID !40
  val select_12 = Module(new SelectNode(NumOuts = 1, ID = 12))

  //  store i32 %8, i32* %6, align 4, !UID !41
  val st_13 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 13, RouteID = 1))

  //  br label %my_pfor.preattach, !UID !42, !BB_UID !43
  val br_14 = Module(new UBranchNode(ID = 14))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 8
  val const2 = Module(new ConstFastNode(value = 8, ID = 2))

  //i32 65535
  val const3 = Module(new ConstFastNode(value = 65535, ID = 3))

  //i32 255
  val const4 = Module(new ConstFastNode(value = 255, ID = 4))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_pfor_body0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_my_if_then1.io.predicateIn(0) <> br_3.io.TrueOutput(0)

  bb_my_pfor_preattach2.io.predicateIn(1) <> br_6.io.Out(0)

  bb_my_pfor_preattach2.io.predicateIn(0) <> br_14.io.Out(0)

  bb_my_if_else3.io.predicateIn(0) <> br_3.io.FalseOutput(0)



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

  Gep_0.io.enable <> bb_my_pfor_body0.io.Out(1)


  ld_1.io.enable <> bb_my_pfor_body0.io.Out(2)


  icmp_2.io.enable <> bb_my_pfor_body0.io.Out(3)


  br_3.io.enable <> bb_my_pfor_body0.io.Out(4)


  const1.io.enable <> bb_my_if_then1.io.Out(0)

  Gep_4.io.enable <> bb_my_if_then1.io.Out(1)


  st_5.io.enable <> bb_my_if_then1.io.Out(2)


  br_6.io.enable <> bb_my_if_then1.io.Out(3)


  ret_7.io.In.enable <> bb_my_pfor_preattach2.io.Out(0)


  const2.io.enable <> bb_my_if_else3.io.Out(0)

  const3.io.enable <> bb_my_if_else3.io.Out(1)

  const4.io.enable <> bb_my_if_else3.io.Out(2)

  binaryOp_8.io.enable <> bb_my_if_else3.io.Out(3)


  binaryOp_9.io.enable <> bb_my_if_else3.io.Out(4)


  Gep_10.io.enable <> bb_my_if_else3.io.Out(5)


  icmp_11.io.enable <> bb_my_if_else3.io.Out(6)


  select_12.io.enable <> bb_my_if_else3.io.Out(7)


  st_13.io.enable <> bb_my_if_else3.io.Out(8)


  br_14.io.enable <> bb_my_if_else3.io.Out(9)




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

  MemCtrl.io.WriteIn(0) <> st_5.io.memReq

  st_5.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_13.io.memReq

  st_13.io.memResp <> MemCtrl.io.WriteOut(1)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  icmp_2.io.RightIO <> const0.io.Out

  st_5.io.inData <> const1.io.Out

  binaryOp_9.io.RightIO <> const2.io.Out

  icmp_11.io.RightIO <> const3.io.Out

  select_12.io.InData1 <> const4.io.Out

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  icmp_2.io.LeftIO <> ld_1.io.Out(0)

  binaryOp_8.io.LeftIO <> ld_1.io.Out(1)

  br_3.io.CmpIO <> icmp_2.io.Out(0)

  st_5.io.GepAddr <> Gep_4.io.Out(0)

  binaryOp_9.io.LeftIO <> binaryOp_8.io.Out(0)

  icmp_11.io.LeftIO <> binaryOp_8.io.Out(1)

  select_12.io.InData2 <> binaryOp_9.io.Out(0)

  st_13.io.GepAddr <> Gep_10.io.Out(0)

  select_12.io.Select <> icmp_11.io.Out(0)

  st_13.io.inData <> select_12.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_0.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_4.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_10.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_4.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_10.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  binaryOp_8.io.RightIO <> InputSplitter.io.Out.data.elements("field3")(0)

  st_5.io.Out(0).ready := true.B

  st_13.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}

object vector_scale_detach1Top extends App {
  val dir = new File("RTL/vector_scale_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new vector_scale_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
