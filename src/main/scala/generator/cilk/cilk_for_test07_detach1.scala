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

abstract class cilk_for_test07_detach1DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class cilk_for_test07_detach1DF(implicit p: Parameters) extends cilk_for_test07_detach1DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 3)
  (WControl = new WriteMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(3, 6, 3)))
  InputSplitter.io.In <> io.in


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_pfor_body0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 49, BID = 0))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = getelementptr inbounds [3 x i32], [3 x i32]* %rgb.in, i32 %__begin.054.in, i32 0, !UID !18
  val Gep_0 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 0)(ElementSize = 4, ArraySize = List(12)))

  //  %1 = load i32, i32* %0, align 4, !tbaa !19, !UID !23
  val ld_1 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 1, RouteID = 0))

  //  %2 = getelementptr inbounds [3 x i32], [3 x i32]* %rgb.in, i32 %__begin.054.in, i32 1, !UID !24
  val Gep_2 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 2)(ElementSize = 4, ArraySize = List(12)))

  //  %3 = load i32, i32* %2, align 4, !tbaa !19, !UID !25
  val ld_3 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 3, RouteID = 1))

  //  %4 = getelementptr inbounds [3 x i32], [3 x i32]* %rgb.in, i32 %__begin.054.in, i32 2, !UID !26
  val Gep_4 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 4)(ElementSize = 4, ArraySize = List(12)))

  //  %5 = load i32, i32* %4, align 4, !tbaa !19, !UID !27
  val ld_5 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 3, ID = 5, RouteID = 2))

  //  %6 = mul nsw i32 %1, 27030, !UID !28
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "mul")(sign = false))

  //  %7 = mul nsw i32 %3, 23434, !UID !29
  val binaryOp_7 = Module(new ComputeNode(NumOuts = 1, ID = 7, opCode = "mul")(sign = false))

  //  %8 = add nsw i32 %7, %6, !UID !30
  val binaryOp_8 = Module(new ComputeNode(NumOuts = 1, ID = 8, opCode = "add")(sign = false))

  //  %9 = mul nsw i32 %5, 11825, !UID !31
  val binaryOp_9 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "mul")(sign = false))

  //  %10 = add nsw i32 %8, %9, !UID !32
  val binaryOp_10 = Module(new ComputeNode(NumOuts = 1, ID = 10, opCode = "add")(sign = false))

  //  %11 = ashr i32 %10, 16, !UID !33
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "ashr")(sign = false))

  //  %12 = getelementptr inbounds [3 x i32], [3 x i32]* %xyz.in, i32 %__begin.054.in, i32 0, !UID !34
  val Gep_12 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 12)(ElementSize = 4, ArraySize = List(12)))

  //  store i32 %11, i32* %12, align 4, !tbaa !19, !UID !35
  val st_13 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 13, RouteID = 0))

  //  %13 = mul nsw i32 %1, 13937, !UID !36
  val binaryOp_14 = Module(new ComputeNode(NumOuts = 1, ID = 14, opCode = "mul")(sign = false))

  //  %14 = mul nsw i32 %3, 46868, !UID !37
  val binaryOp_15 = Module(new ComputeNode(NumOuts = 1, ID = 15, opCode = "mul")(sign = false))

  //  %15 = add nsw i32 %14, %13, !UID !38
  val binaryOp_16 = Module(new ComputeNode(NumOuts = 1, ID = 16, opCode = "add")(sign = false))

  //  %16 = mul nsw i32 %5, 4730, !UID !39
  val binaryOp_17 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "mul")(sign = false))

  //  %17 = add nsw i32 %15, %16, !UID !40
  val binaryOp_18 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "add")(sign = false))

  //  %18 = ashr i32 %17, 16, !UID !41
  val binaryOp_19 = Module(new ComputeNode(NumOuts = 1, ID = 19, opCode = "ashr")(sign = false))

  //  %19 = getelementptr inbounds [3 x i32], [3 x i32]* %xyz.in, i32 %__begin.054.in, i32 1, !UID !42
  val Gep_20 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 20)(ElementSize = 4, ArraySize = List(12)))

  //  store i32 %18, i32* %19, align 4, !tbaa !19, !UID !43
  val st_21 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 21, RouteID = 1))

  //  %20 = mul nsw i32 %1, 1267, !UID !44
  val binaryOp_22 = Module(new ComputeNode(NumOuts = 1, ID = 22, opCode = "mul")(sign = false))

  //  %21 = mul nsw i32 %3, 7811, !UID !45
  val binaryOp_23 = Module(new ComputeNode(NumOuts = 1, ID = 23, opCode = "mul")(sign = false))

  //  %22 = add nsw i32 %21, %20, !UID !46
  val binaryOp_24 = Module(new ComputeNode(NumOuts = 1, ID = 24, opCode = "add")(sign = false))

  //  %23 = mul nsw i32 %5, 62279, !UID !47
  val binaryOp_25 = Module(new ComputeNode(NumOuts = 1, ID = 25, opCode = "mul")(sign = false))

  //  %24 = add nsw i32 %22, %23, !UID !48
  val binaryOp_26 = Module(new ComputeNode(NumOuts = 1, ID = 26, opCode = "add")(sign = false))

  //  %25 = ashr i32 %24, 16, !UID !49
  val binaryOp_27 = Module(new ComputeNode(NumOuts = 1, ID = 27, opCode = "ashr")(sign = false))

  //  %26 = getelementptr inbounds [3 x i32], [3 x i32]* %xyz.in, i32 %__begin.054.in, i32 2, !UID !50
  val Gep_28 = Module(new GepNode(NumIns = 2, NumOuts = 1, ID = 28)(ElementSize = 4, ArraySize = List(12)))

  //  store i32 %25, i32* %26, align 4, !tbaa !19, !UID !51
  val st_29 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 29, RouteID = 2))

  //  ret void, !UID !52, !BB_UID !53
  val ret_30 = Module(new RetNode2(retTypes = List(), ID = 30))


  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 2
  val const2 = Module(new ConstFastNode(value = 2, ID = 2))

  //i32 27030
  val const3 = Module(new ConstFastNode(value = 27030, ID = 3))

  //i32 23434
  val const4 = Module(new ConstFastNode(value = 23434, ID = 4))

  //i32 11825
  val const5 = Module(new ConstFastNode(value = 11825, ID = 5))

  //i32 16
  val const6 = Module(new ConstFastNode(value = 16, ID = 6))

  //i32 0
  val const7 = Module(new ConstFastNode(value = 0, ID = 7))

  //i32 13937
  val const8 = Module(new ConstFastNode(value = 13937, ID = 8))

  //i32 46868
  val const9 = Module(new ConstFastNode(value = 46868, ID = 9))

  //i32 4730
  val const10 = Module(new ConstFastNode(value = 4730, ID = 10))

  //i32 16
  val const11 = Module(new ConstFastNode(value = 16, ID = 11))

  //i32 1
  val const12 = Module(new ConstFastNode(value = 1, ID = 12))

  //i32 1267
  val const13 = Module(new ConstFastNode(value = 1267, ID = 13))

  //i32 7811
  val const14 = Module(new ConstFastNode(value = 7811, ID = 14))

  //i32 62279
  val const15 = Module(new ConstFastNode(value = 62279, ID = 15))

  //i32 16
  val const16 = Module(new ConstFastNode(value = 16, ID = 16))

  //i32 2
  val const17 = Module(new ConstFastNode(value = 2, ID = 17))


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

  const4.io.enable <> bb_my_pfor_body0.io.Out(4)

  const5.io.enable <> bb_my_pfor_body0.io.Out(5)

  const6.io.enable <> bb_my_pfor_body0.io.Out(6)

  const7.io.enable <> bb_my_pfor_body0.io.Out(7)

  const8.io.enable <> bb_my_pfor_body0.io.Out(8)

  const9.io.enable <> bb_my_pfor_body0.io.Out(9)

  const10.io.enable <> bb_my_pfor_body0.io.Out(10)

  const11.io.enable <> bb_my_pfor_body0.io.Out(11)

  const12.io.enable <> bb_my_pfor_body0.io.Out(12)

  const13.io.enable <> bb_my_pfor_body0.io.Out(13)

  const14.io.enable <> bb_my_pfor_body0.io.Out(14)

  const15.io.enable <> bb_my_pfor_body0.io.Out(15)

  const16.io.enable <> bb_my_pfor_body0.io.Out(16)

  const17.io.enable <> bb_my_pfor_body0.io.Out(17)

  Gep_0.io.enable <> bb_my_pfor_body0.io.Out(18)


  ld_1.io.enable <> bb_my_pfor_body0.io.Out(19)


  Gep_2.io.enable <> bb_my_pfor_body0.io.Out(20)


  ld_3.io.enable <> bb_my_pfor_body0.io.Out(21)


  Gep_4.io.enable <> bb_my_pfor_body0.io.Out(22)


  ld_5.io.enable <> bb_my_pfor_body0.io.Out(23)


  binaryOp_6.io.enable <> bb_my_pfor_body0.io.Out(24)


  binaryOp_7.io.enable <> bb_my_pfor_body0.io.Out(25)


  binaryOp_8.io.enable <> bb_my_pfor_body0.io.Out(26)


  binaryOp_9.io.enable <> bb_my_pfor_body0.io.Out(27)


  binaryOp_10.io.enable <> bb_my_pfor_body0.io.Out(28)


  binaryOp_11.io.enable <> bb_my_pfor_body0.io.Out(29)


  Gep_12.io.enable <> bb_my_pfor_body0.io.Out(30)


  st_13.io.enable <> bb_my_pfor_body0.io.Out(31)


  binaryOp_14.io.enable <> bb_my_pfor_body0.io.Out(32)


  binaryOp_15.io.enable <> bb_my_pfor_body0.io.Out(33)


  binaryOp_16.io.enable <> bb_my_pfor_body0.io.Out(34)


  binaryOp_17.io.enable <> bb_my_pfor_body0.io.Out(35)


  binaryOp_18.io.enable <> bb_my_pfor_body0.io.Out(36)


  binaryOp_19.io.enable <> bb_my_pfor_body0.io.Out(37)


  Gep_20.io.enable <> bb_my_pfor_body0.io.Out(38)


  st_21.io.enable <> bb_my_pfor_body0.io.Out(39)


  binaryOp_22.io.enable <> bb_my_pfor_body0.io.Out(40)


  binaryOp_23.io.enable <> bb_my_pfor_body0.io.Out(41)


  binaryOp_24.io.enable <> bb_my_pfor_body0.io.Out(42)


  binaryOp_25.io.enable <> bb_my_pfor_body0.io.Out(43)


  binaryOp_26.io.enable <> bb_my_pfor_body0.io.Out(44)


  binaryOp_27.io.enable <> bb_my_pfor_body0.io.Out(45)


  Gep_28.io.enable <> bb_my_pfor_body0.io.Out(46)


  st_29.io.enable <> bb_my_pfor_body0.io.Out(47)


  ret_30.io.In.enable <> bb_my_pfor_body0.io.Out(48)


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

  MemCtrl.io.WriteIn(0) <> st_13.io.memReq

  st_13.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_21.io.memReq

  st_21.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_29.io.memReq

  st_29.io.memResp <> MemCtrl.io.WriteOut(2)


  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */


  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_0.io.idx(1) <> const0.io.Out

  Gep_2.io.idx(1) <> const1.io.Out

  Gep_4.io.idx(1) <> const2.io.Out

  binaryOp_6.io.RightIO <> const3.io.Out

  binaryOp_7.io.RightIO <> const4.io.Out

  binaryOp_9.io.RightIO <> const5.io.Out

  binaryOp_11.io.RightIO <> const6.io.Out

  Gep_12.io.idx(1) <> const7.io.Out

  binaryOp_14.io.RightIO <> const8.io.Out

  binaryOp_15.io.RightIO <> const9.io.Out

  binaryOp_17.io.RightIO <> const10.io.Out

  binaryOp_19.io.RightIO <> const11.io.Out

  Gep_20.io.idx(1) <> const12.io.Out

  binaryOp_22.io.RightIO <> const13.io.Out

  binaryOp_23.io.RightIO <> const14.io.Out

  binaryOp_25.io.RightIO <> const15.io.Out

  binaryOp_27.io.RightIO <> const16.io.Out

  Gep_28.io.idx(1) <> const17.io.Out

  ld_1.io.GepAddr <> Gep_0.io.Out(0)

  binaryOp_6.io.LeftIO <> ld_1.io.Out(0)

  binaryOp_14.io.LeftIO <> ld_1.io.Out(1)

  binaryOp_22.io.LeftIO <> ld_1.io.Out(2)

  ld_3.io.GepAddr <> Gep_2.io.Out(0)

  binaryOp_7.io.LeftIO <> ld_3.io.Out(0)

  binaryOp_15.io.LeftIO <> ld_3.io.Out(1)

  binaryOp_23.io.LeftIO <> ld_3.io.Out(2)

  ld_5.io.GepAddr <> Gep_4.io.Out(0)

  binaryOp_9.io.LeftIO <> ld_5.io.Out(0)

  binaryOp_17.io.LeftIO <> ld_5.io.Out(1)

  binaryOp_25.io.LeftIO <> ld_5.io.Out(2)

  binaryOp_8.io.RightIO <> binaryOp_6.io.Out(0)

  binaryOp_8.io.LeftIO <> binaryOp_7.io.Out(0)

  binaryOp_10.io.LeftIO <> binaryOp_8.io.Out(0)

  binaryOp_10.io.RightIO <> binaryOp_9.io.Out(0)

  binaryOp_11.io.LeftIO <> binaryOp_10.io.Out(0)

  st_13.io.inData <> binaryOp_11.io.Out(0)

  st_13.io.GepAddr <> Gep_12.io.Out(0)

  binaryOp_16.io.RightIO <> binaryOp_14.io.Out(0)

  binaryOp_16.io.LeftIO <> binaryOp_15.io.Out(0)

  binaryOp_18.io.LeftIO <> binaryOp_16.io.Out(0)

  binaryOp_18.io.RightIO <> binaryOp_17.io.Out(0)

  binaryOp_19.io.LeftIO <> binaryOp_18.io.Out(0)

  st_21.io.inData <> binaryOp_19.io.Out(0)

  st_21.io.GepAddr <> Gep_20.io.Out(0)

  binaryOp_24.io.RightIO <> binaryOp_22.io.Out(0)

  binaryOp_24.io.LeftIO <> binaryOp_23.io.Out(0)

  binaryOp_26.io.LeftIO <> binaryOp_24.io.Out(0)

  binaryOp_26.io.RightIO <> binaryOp_25.io.Out(0)

  binaryOp_27.io.LeftIO <> binaryOp_26.io.Out(0)

  st_29.io.inData <> binaryOp_27.io.Out(0)

  st_29.io.GepAddr <> Gep_28.io.Out(0)

  Gep_0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_2.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_4.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  Gep_0.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_2.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_4.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_12.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(3)

  Gep_20.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(4)

  Gep_28.io.idx(0) <> InputSplitter.io.Out.data.elements("field1")(5)

  Gep_12.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_20.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(1)

  Gep_28.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(2)

  st_13.io.Out(0).ready := true.B

  st_21.io.Out(0).ready := true.B

  st_29.io.Out(0).ready := true.B


  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_30.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test07_detach1Top extends App {
  val dir = new File("RTL/cilk_for_test07_detach1Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test07_detach1DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
