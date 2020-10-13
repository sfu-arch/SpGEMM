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

abstract class bgemm_detach3DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class bgemm_detach3DF(implicit p: Parameters) extends bgemm_detach3DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 3, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 3, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 1, 1, 1, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 2, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))

  val Loop_1 = Module(new LoopBlockNode(NumIns = List(2, 2, 1, 1, 1, 1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 1))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_my_loopk0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_my_for_body1 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 10, NumPhi = 1, BID = 1))

  val bb_my_for_body382 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 18, NumPhi = 1, BID = 2))

  val bb_my_for_cond_cleanup373 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 3))

  val bb_my_for_cond_cleanup4 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %0 = shl nsw i32 %__begin17.099.in, 2, !UID !10
  val binaryOp_0 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "shl")(sign = false))

  //  br label %my_for.body, !UID !11, !BB_UID !12
  val br_1 = Module(new UBranchNode(ID = 1))

  //  %1 = phi i32 [ 0, %my_loopk ], [ %21, %my_for.cond.cleanup37 ], !UID !13
  val phi2 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 2, Res = true))

  //  %2 = add nuw nsw i32 %1, %mul11.in, !UID !14
  val binaryOp_3 = Module(new ComputeNode(NumOuts = 1, ID = 3, opCode = "add")(sign = false))

  //  %3 = shl i32 %2, 2, !UID !15
  val binaryOp_4 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "shl")(sign = false))

  //  %4 = add nuw nsw i32 %1, %mul11.in, !UID !16
  val binaryOp_5 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "add")(sign = false))

  //  %5 = add nuw nsw i32 %4, %0, !UID !17
  val binaryOp_6 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "add")(sign = false))

  //  %6 = getelementptr inbounds i32, i32* %m1.in, i32 %5, !UID !18
  val Gep_7 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 7)(ElementSize = 4, ArraySize = List()))

  //  %7 = load i32, i32* %6, align 4, !tbaa !19, !UID !23
  val ld_8 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 8, RouteID = 0))

  //  br label %my_for.body38, !UID !24, !BB_UID !25
  val br_9 = Module(new UBranchNode(ID = 9))

  //  %8 = phi i32 [ 0, %my_for.body ], [ %19, %my_for.body38 ], !UID !26
  val phi10 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 3, ID = 10, Res = true))

  //  %9 = add nuw nsw i32 %8, %mul1.in, !UID !27
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "add")(sign = false))

  //  %10 = add nuw nsw i32 %9, %3, !UID !28
  val binaryOp_12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign = false))

  //  %11 = getelementptr inbounds i32, i32* %m2.in, i32 %10, !UID !29
  val Gep_13 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 13)(ElementSize = 4, ArraySize = List()))

  //  %12 = load i32, i32* %11, align 4, !tbaa !19, !UID !30
  val ld_14 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 14, RouteID = 1))

  //  %13 = mul nsw i32 %12, %7, !UID !31
  val binaryOp_15 = Module(new ComputeNode(NumOuts = 1, ID = 15, opCode = "mul")(sign = false))

  //  %14 = add nuw nsw i32 %8, %mul1.in, !UID !32
  val binaryOp_16 = Module(new ComputeNode(NumOuts = 1, ID = 16, opCode = "add")(sign = false))

  //  %15 = add nuw nsw i32 %14, %0, !UID !33
  val binaryOp_17 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "add")(sign = false))

  //  %16 = getelementptr inbounds i32, i32* %prod.in, i32 %15, !UID !34
  val Gep_18 = Module(new GepNode(NumIns = 1, NumOuts = 2, ID = 18)(ElementSize = 4, ArraySize = List()))

  //  %17 = load i32, i32* %16, align 4, !tbaa !19, !UID !35
  val ld_19 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 19, RouteID = 2))

  //  %18 = add nsw i32 %17, %13, !UID !36
  val binaryOp_20 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "add")(sign = false))

  //  store i32 %18, i32* %16, align 4, !tbaa !19, !UID !37
  val st_21 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 21, RouteID = 0))

  //  %19 = add nuw nsw i32 %8, 1, !UID !38
  val binaryOp_22 = Module(new ComputeNode(NumOuts = 2, ID = 22, opCode = "add")(sign = false))

  //  %20 = icmp eq i32 %19, 2, !UID !39
  val icmp_23 = Module(new IcmpNode(NumOuts = 1, ID = 23, opCode = "eq")(sign = false))

  //  br i1 %20, label %my_for.cond.cleanup37, label %my_for.body38, !UID !40, !BB_UID !41
  val br_24 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 24))

  //  %21 = add nuw nsw i32 %1, 1, !UID !42
  val binaryOp_25 = Module(new ComputeNode(NumOuts = 2, ID = 25, opCode = "add")(sign = false))

  //  %22 = icmp eq i32 %21, 2, !UID !43
  val icmp_26 = Module(new IcmpNode(NumOuts = 1, ID = 26, opCode = "eq")(sign = false))

  //  br i1 %22, label %my_for.cond.cleanup, label %my_for.body, !UID !44, !BB_UID !45
  val br_27 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 27))

  //  ret void, !UID !46, !BB_UID !47
  val ret_28 = Module(new RetNode2(retTypes = List(), ID = 28))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 2
  val const0 = Module(new ConstFastNode(value = 2, ID = 0))

  //i32 0
  val const1 = Module(new ConstFastNode(value = 0, ID = 1))

  //i32 2
  val const2 = Module(new ConstFastNode(value = 2, ID = 2))

  //i32 0
  val const3 = Module(new ConstFastNode(value = 0, ID = 3))

  //i32 1
  val const4 = Module(new ConstFastNode(value = 1, ID = 4))

  //i32 2
  val const5 = Module(new ConstFastNode(value = 2, ID = 5))

  //i32 1
  val const6 = Module(new ConstFastNode(value = 1, ID = 6))

  //i32 2
  val const7 = Module(new ConstFastNode(value = 2, ID = 7))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_my_loopk0.io.predicateIn(0) <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_my_for_body1.io.predicateIn(1) <> Loop_1.io.activate_loop_start

  bb_my_for_body1.io.predicateIn(0) <> Loop_1.io.activate_loop_back

  bb_my_for_body382.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_my_for_body382.io.predicateIn(0) <> Loop_0.io.activate_loop_back

  bb_my_for_cond_cleanup373.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_my_for_cond_cleanup4.io.predicateIn(0) <> Loop_1.io.loopExit(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_9.io.Out(0)

  Loop_0.io.loopBack(0) <> br_24.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_24.io.TrueOutput(0)

  Loop_1.io.enable <> br_1.io.Out(0)

  Loop_1.io.loopBack(0) <> br_27.io.FalseOutput(0)

  Loop_1.io.loopFinish(0) <> br_27.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> binaryOp_4.io.Out(0)

  Loop_0.io.InLiveIn(1) <> ld_8.io.Out(0)

  Loop_0.io.InLiveIn(2) <> Loop_1.io.OutLiveIn.elements("field1")(0)

  Loop_0.io.InLiveIn(3) <> Loop_1.io.OutLiveIn.elements("field5")(0)

  Loop_0.io.InLiveIn(4) <> Loop_1.io.OutLiveIn.elements("field3")(0)

  Loop_0.io.InLiveIn(5) <> Loop_1.io.OutLiveIn.elements("field4")(0)

  Loop_1.io.InLiveIn(0) <> InputSplitter.io.Out.data.elements("field1")(0)

  Loop_1.io.InLiveIn(1) <> binaryOp_0.io.Out(0)

  Loop_1.io.InLiveIn(2) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_1.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field3")(0)

  Loop_1.io.InLiveIn(4) <> InputSplitter.io.Out.data.elements("field4")(0)

  Loop_1.io.InLiveIn(5) <> InputSplitter.io.Out.data.elements("field5")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_12.io.RightIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  binaryOp_15.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  binaryOp_17.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_18.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  binaryOp_11.io.RightIO <> Loop_0.io.OutLiveIn.elements("field4")(0)

  binaryOp_16.io.RightIO <> Loop_0.io.OutLiveIn.elements("field4")(1)

  Gep_13.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field5")(0)

  binaryOp_3.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(0)

  binaryOp_5.io.RightIO <> Loop_1.io.OutLiveIn.elements("field0")(1)

  binaryOp_6.io.RightIO <> Loop_1.io.OutLiveIn.elements("field1")(1)

  Gep_7.io.baseAddress <> Loop_1.io.OutLiveIn.elements("field2")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_22.io.Out(0)

  Loop_1.io.CarryDepenIn(0) <> binaryOp_25.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi10.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)

  phi2.io.InData(1) <> Loop_1.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_my_loopk0.io.Out(0)

  binaryOp_0.io.enable <> bb_my_loopk0.io.Out(1)


  br_1.io.enable <> bb_my_loopk0.io.Out(2)


  const1.io.enable <> bb_my_for_body1.io.Out(0)

  const2.io.enable <> bb_my_for_body1.io.Out(1)

  phi2.io.enable <> bb_my_for_body1.io.Out(2)


  binaryOp_3.io.enable <> bb_my_for_body1.io.Out(3)


  binaryOp_4.io.enable <> bb_my_for_body1.io.Out(4)


  binaryOp_5.io.enable <> bb_my_for_body1.io.Out(5)


  binaryOp_6.io.enable <> bb_my_for_body1.io.Out(6)


  Gep_7.io.enable <> bb_my_for_body1.io.Out(7)


  ld_8.io.enable <> bb_my_for_body1.io.Out(8)


  br_9.io.enable <> bb_my_for_body1.io.Out(9)


  const3.io.enable <> bb_my_for_body382.io.Out(0)

  const4.io.enable <> bb_my_for_body382.io.Out(1)

  const5.io.enable <> bb_my_for_body382.io.Out(2)

  phi10.io.enable <> bb_my_for_body382.io.Out(3)


  binaryOp_11.io.enable <> bb_my_for_body382.io.Out(4)


  binaryOp_12.io.enable <> bb_my_for_body382.io.Out(5)


  Gep_13.io.enable <> bb_my_for_body382.io.Out(6)


  ld_14.io.enable <> bb_my_for_body382.io.Out(7)


  binaryOp_15.io.enable <> bb_my_for_body382.io.Out(8)


  binaryOp_16.io.enable <> bb_my_for_body382.io.Out(9)


  binaryOp_17.io.enable <> bb_my_for_body382.io.Out(10)


  Gep_18.io.enable <> bb_my_for_body382.io.Out(11)


  ld_19.io.enable <> bb_my_for_body382.io.Out(12)


  binaryOp_20.io.enable <> bb_my_for_body382.io.Out(13)


  st_21.io.enable <> bb_my_for_body382.io.Out(14)


  binaryOp_22.io.enable <> bb_my_for_body382.io.Out(15)


  icmp_23.io.enable <> bb_my_for_body382.io.Out(16)


  br_24.io.enable <> bb_my_for_body382.io.Out(17)


  const6.io.enable <> bb_my_for_cond_cleanup373.io.Out(0)

  const7.io.enable <> bb_my_for_cond_cleanup373.io.Out(1)

  binaryOp_25.io.enable <> bb_my_for_cond_cleanup373.io.Out(2)


  icmp_26.io.enable <> bb_my_for_cond_cleanup373.io.Out(3)


  br_27.io.enable <> bb_my_for_cond_cleanup373.io.Out(4)


  ret_28.io.In.enable <> bb_my_for_cond_cleanup4.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi2.io.Mask <> bb_my_for_body1.io.MaskBB(0)

  phi10.io.Mask <> bb_my_for_body382.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_8.io.memReq

  ld_8.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_19.io.memReq

  ld_19.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(0) <> st_21.io.memReq

  st_21.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  binaryOp_0.io.RightIO <> const0.io.Out

  phi2.io.InData(0) <> const1.io.Out

  binaryOp_4.io.RightIO <> const2.io.Out

  phi10.io.InData(0) <> const3.io.Out

  binaryOp_22.io.RightIO <> const4.io.Out

  icmp_23.io.RightIO <> const5.io.Out

  binaryOp_25.io.RightIO <> const6.io.Out

  icmp_26.io.RightIO <> const7.io.Out

  binaryOp_3.io.LeftIO <> phi2.io.Out(0)

  binaryOp_5.io.LeftIO <> phi2.io.Out(1)

  binaryOp_25.io.LeftIO <> phi2.io.Out(2)

  binaryOp_4.io.LeftIO <> binaryOp_3.io.Out(0)

  binaryOp_6.io.LeftIO <> binaryOp_5.io.Out(0)

  Gep_7.io.idx(0) <> binaryOp_6.io.Out(0)

  ld_8.io.GepAddr <> Gep_7.io.Out(0)

  binaryOp_11.io.LeftIO <> phi10.io.Out(0)

  binaryOp_16.io.LeftIO <> phi10.io.Out(1)

  binaryOp_22.io.LeftIO <> phi10.io.Out(2)

  binaryOp_12.io.LeftIO <> binaryOp_11.io.Out(0)

  Gep_13.io.idx(0) <> binaryOp_12.io.Out(0)

  ld_14.io.GepAddr <> Gep_13.io.Out(0)

  binaryOp_15.io.LeftIO <> ld_14.io.Out(0)

  binaryOp_20.io.RightIO <> binaryOp_15.io.Out(0)

  binaryOp_17.io.LeftIO <> binaryOp_16.io.Out(0)

  Gep_18.io.idx(0) <> binaryOp_17.io.Out(0)

  ld_19.io.GepAddr <> Gep_18.io.Out(0)

  st_21.io.GepAddr <> Gep_18.io.Out(1)

  binaryOp_20.io.LeftIO <> ld_19.io.Out(0)

  st_21.io.inData <> binaryOp_20.io.Out(0)

  icmp_23.io.LeftIO <> binaryOp_22.io.Out(1)

  br_24.io.CmpIO <> icmp_23.io.Out(0)

  icmp_26.io.LeftIO <> binaryOp_25.io.Out(1)

  br_27.io.CmpIO <> icmp_26.io.Out(0)

  binaryOp_0.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(0)

  st_21.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_28.io.Out

}

import java.io.{File, FileWriter}

object bgemm_detach3Top extends App {
  val dir = new File("RTL/bgemm_detach3Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new bgemm_detach3DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
