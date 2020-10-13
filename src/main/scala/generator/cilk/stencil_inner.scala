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

abstract class stencil_innerDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List()))
  })
}

class stencil_innerDF(implicit p: Parameters) extends stencil_innerDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 2, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1, 1, 2, 2, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1, 1, 1, 1, 2), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 12, BID = 0))

  val bb_for_cond_cleanup1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_for_body2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 7, NumPhi = 1, BID = 2))

  val bb_if_then53 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 7, BID = 3))

  val bb_if_end114 = Module(new BasicBlockNoMaskFastNode(NumInputs = 2, NumOuts = 5, BID = 4))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %add = add i32 %i, -1, !UID !44
  val binaryOp_add0 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "add")(sign = false))

  //  %sub = add i32 %add, %nr, !UID !45
  val binaryOp_sub1 = Module(new ComputeNode(NumOuts = 2, ID = 1, opCode = "add")(sign = false))

  //  %add1 = add i32 %j, -1, !UID !46
  val binaryOp_add12 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "add")(sign = false))

  //  %mul = shl i32 %sub, 2, !UID !47
  val binaryOp_mul3 = Module(new ComputeNode(NumOuts = 1, ID = 3, opCode = "shl")(sign = false))

  //  %mul7 = shl i32 %i, 2, !UID !48
  val binaryOp_mul74 = Module(new ComputeNode(NumOuts = 1, ID = 4, opCode = "shl")(sign = false))

  //  %add8 = add i32 %mul7, %j, !UID !49
  val binaryOp_add85 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "add")(sign = false))

  //  %arrayidx9 = getelementptr inbounds i32, i32* %out, i32 %add8, !UID !50
  val Gep_arrayidx96 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 6)(ElementSize = 4, ArraySize = List()))

  //  br label %for.body, !dbg !51, !UID !52, !BB_UID !53
  val br_7 = Module(new UBranchNode(ID = 7))

  //  ret void, !dbg !54, !UID !55, !BB_UID !56
  val ret_8 = Module(new RetNode2(retTypes = List(), ID = 8))

  //  %nc.021 = phi i32 [ 0, %entry ], [ %inc, %if.end11 ], !UID !57
  val phinc_0219 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 2, ID = 9, Res = true))

  //  %sub2 = add i32 %add1, %nc.021, !dbg !59, !UID !60
  val binaryOp_sub210 = Module(new ComputeNode(NumOuts = 2, ID = 10, opCode = "add")(sign = false))

  //  %0 = or i32 %sub2, %sub, !dbg !62, !UID !63
  val binaryOp_11 = Module(new ComputeNode(NumOuts = 1, ID = 11, opCode = "or")(sign = false))

  //  %1 = icmp ult i32 %0, 4, !dbg !62, !UID !64
  val icmp_12 = Module(new IcmpNode(NumOuts = 1, ID = 12, opCode = "ult")(sign = false))

  //  br i1 %1, label %if.then5, label %if.end11, !dbg !62, !UID !65, !BB_UID !66
  val br_13 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 13))

  //  %add6 = add i32 %sub2, %mul, !dbg !67, !UID !72
  val binaryOp_add614 = Module(new ComputeNode(NumOuts = 1, ID = 14, opCode = "add")(sign = false))

  //  %arrayidx = getelementptr inbounds i32, i32* %in, i32 %add6, !dbg !73, !UID !74
  val Gep_arrayidx15 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 15)(ElementSize = 4, ArraySize = List()))

  //  %2 = load i32, i32* %arrayidx, align 4, !dbg !73, !tbaa !75, !UID !79
  val ld_16 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 16, RouteID = 0))

  //  %3 = load i32, i32* %arrayidx9, align 4, !dbg !80, !tbaa !75, !UID !81
  val ld_17 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 17, RouteID = 1))

  //  %add10 = add i32 %3, %2, !dbg !80, !UID !82
  val binaryOp_add1018 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "add")(sign = false))

  //  store i32 %add10, i32* %arrayidx9, align 4, !dbg !80, !tbaa !75, !UID !83
  val st_19 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 1, ID = 19, RouteID = 0))

  //  br label %if.end11, !dbg !84, !UID !85, !BB_UID !86
  val br_20 = Module(new UBranchNode(ID = 20, NumPredOps = 1))

  //  %inc = add nuw nsw i32 %nc.021, 1, !dbg !87, !UID !88
  val binaryOp_inc21 = Module(new ComputeNode(NumOuts = 2, ID = 21, opCode = "add")(sign = false))

  //  %exitcond = icmp eq i32 %inc, 3, !dbg !89, !UID !90
  val icmp_exitcond22 = Module(new IcmpNode(NumOuts = 1, ID = 22, opCode = "eq")(sign = false))

  //  br i1 %exitcond, label %for.cond.cleanup, label %for.body, !dbg !51, !llvm.loop !91, !UID !93, !BB_UID !94
  val br_23 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 23))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 -1
  val const0 = Module(new ConstFastNode(value = -1, ID = 0))

  //i32 -1
  val const1 = Module(new ConstFastNode(value = -1, ID = 1))

  //i32 2
  val const2 = Module(new ConstFastNode(value = 2, ID = 2))

  //i32 2
  val const3 = Module(new ConstFastNode(value = 2, ID = 3))

  //i32 0
  val const4 = Module(new ConstFastNode(value = 0, ID = 4))

  //i32 4
  val const5 = Module(new ConstFastNode(value = 4, ID = 5))

  //i32 1
  val const6 = Module(new ConstFastNode(value = 1, ID = 6))

  //i32 3
  val const7 = Module(new ConstFastNode(value = 3, ID = 7))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_if_then53.io.predicateIn(0) <> br_13.io.TrueOutput(0)

  bb_if_end114.io.predicateIn(1) <> br_13.io.FalseOutput(0)

  bb_if_end114.io.predicateIn(0) <> br_20.io.Out(0)



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

  Loop_0.io.enable <> br_7.io.Out(0)

  Loop_0.io.loopBack(0) <> br_23.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_23.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> binaryOp_add12.io.Out(0)

  Loop_0.io.InLiveIn(1) <> binaryOp_sub1.io.Out(0)

  Loop_0.io.InLiveIn(2) <> binaryOp_mul3.io.Out(0)

  Loop_0.io.InLiveIn(3) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.InLiveIn(4) <> Gep_arrayidx96.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  binaryOp_sub210.io.LeftIO <> Loop_0.io.OutLiveIn.elements("field0")(0)

  binaryOp_11.io.RightIO <> Loop_0.io.OutLiveIn.elements("field1")(0)

  binaryOp_add614.io.RightIO <> Loop_0.io.OutLiveIn.elements("field2")(0)

  Gep_arrayidx15.io.baseAddress <> Loop_0.io.OutLiveIn.elements("field3")(0)

  ld_17.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field4")(0)

  st_19.io.GepAddr <> Loop_0.io.OutLiveIn.elements("field4")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc21.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phinc_0219.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  const1.io.enable <> bb_entry0.io.Out(1)

  const2.io.enable <> bb_entry0.io.Out(2)

  const3.io.enable <> bb_entry0.io.Out(3)

  binaryOp_add0.io.enable <> bb_entry0.io.Out(4)


  binaryOp_sub1.io.enable <> bb_entry0.io.Out(5)


  binaryOp_add12.io.enable <> bb_entry0.io.Out(6)


  binaryOp_mul3.io.enable <> bb_entry0.io.Out(7)


  binaryOp_mul74.io.enable <> bb_entry0.io.Out(8)


  binaryOp_add85.io.enable <> bb_entry0.io.Out(9)


  Gep_arrayidx96.io.enable <> bb_entry0.io.Out(10)


  br_7.io.enable <> bb_entry0.io.Out(11)


  ret_8.io.In.enable <> bb_for_cond_cleanup1.io.Out(0)


  const4.io.enable <> bb_for_body2.io.Out(0)

  const5.io.enable <> bb_for_body2.io.Out(1)

  phinc_0219.io.enable <> bb_for_body2.io.Out(2)


  binaryOp_sub210.io.enable <> bb_for_body2.io.Out(3)


  binaryOp_11.io.enable <> bb_for_body2.io.Out(4)


  icmp_12.io.enable <> bb_for_body2.io.Out(5)


  br_13.io.enable <> bb_for_body2.io.Out(6)


  binaryOp_add614.io.enable <> bb_if_then53.io.Out(0)


  Gep_arrayidx15.io.enable <> bb_if_then53.io.Out(1)


  ld_16.io.enable <> bb_if_then53.io.Out(2)


  ld_17.io.enable <> bb_if_then53.io.Out(3)


  binaryOp_add1018.io.enable <> bb_if_then53.io.Out(4)


  st_19.io.enable <> bb_if_then53.io.Out(5)


  br_20.io.enable <> bb_if_then53.io.Out(6)


  const6.io.enable <> bb_if_end114.io.Out(0)

  const7.io.enable <> bb_if_end114.io.Out(1)

  binaryOp_inc21.io.enable <> bb_if_end114.io.Out(2)


  icmp_exitcond22.io.enable <> bb_if_end114.io.Out(3)


  br_23.io.enable <> bb_if_end114.io.Out(4)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phinc_0219.io.Mask <> bb_for_body2.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_16.io.memReq

  ld_16.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_17.io.memReq

  ld_17.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_19.io.memReq

  st_19.io.memResp <> MemCtrl.io.WriteOut(0)

  br_20.io.PredOp(0) <> st_19.io.SuccOp(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  binaryOp_add0.io.RightIO <> const0.io.Out

  binaryOp_add12.io.RightIO <> const1.io.Out

  binaryOp_mul3.io.RightIO <> const2.io.Out

  binaryOp_mul74.io.RightIO <> const3.io.Out

  phinc_0219.io.InData(0) <> const4.io.Out

  icmp_12.io.RightIO <> const5.io.Out

  binaryOp_inc21.io.RightIO <> const6.io.Out

  icmp_exitcond22.io.RightIO <> const7.io.Out

  binaryOp_sub1.io.LeftIO <> binaryOp_add0.io.Out(0)

  binaryOp_mul3.io.LeftIO <> binaryOp_sub1.io.Out(1)

  binaryOp_add85.io.LeftIO <> binaryOp_mul74.io.Out(0)

  Gep_arrayidx96.io.idx(0) <> binaryOp_add85.io.Out(0)

  binaryOp_sub210.io.RightIO <> phinc_0219.io.Out(0)

  binaryOp_inc21.io.LeftIO <> phinc_0219.io.Out(1)

  binaryOp_11.io.LeftIO <> binaryOp_sub210.io.Out(0)

  binaryOp_add614.io.LeftIO <> binaryOp_sub210.io.Out(1)

  icmp_12.io.LeftIO <> binaryOp_11.io.Out(0)

  br_13.io.CmpIO <> icmp_12.io.Out(0)

  Gep_arrayidx15.io.idx(0) <> binaryOp_add614.io.Out(0)

  ld_16.io.GepAddr <> Gep_arrayidx15.io.Out(0)

  binaryOp_add1018.io.RightIO <> ld_16.io.Out(0)

  binaryOp_add1018.io.LeftIO <> ld_17.io.Out(0)

  st_19.io.inData <> binaryOp_add1018.io.Out(0)

  icmp_exitcond22.io.LeftIO <> binaryOp_inc21.io.Out(1)

  br_23.io.CmpIO <> icmp_exitcond22.io.Out(0)

  Gep_arrayidx96.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(0)

  binaryOp_add0.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(0)

  binaryOp_mul74.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(1)

  binaryOp_add12.io.LeftIO <> InputSplitter.io.Out.data.elements("field3")(0)

  binaryOp_add85.io.RightIO <> InputSplitter.io.Out.data.elements("field3")(1)

  binaryOp_sub1.io.RightIO <> InputSplitter.io.Out.data.elements("field4")(0)

  st_19.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_8.io.Out

}

import java.io.{File, FileWriter}

object stencil_innerTop extends App {
  val dir = new File("RTL/stencil_innerTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new stencil_innerDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
