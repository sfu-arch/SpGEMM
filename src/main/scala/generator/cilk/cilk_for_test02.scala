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

abstract class cilk_for_test02DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32))))
    val call_12_out = Decoupled(new Call(List(32)))
    val call_12_in = Flipped(Decoupled(new Call(List())))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class cilk_for_test02DF(implicit p: Parameters) extends cilk_for_test02DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 1, NWrites = 1)
  (WControl = new WriteMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 1))
  (RControl = new ReadMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 1))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val StackPointer = Module(new Stack(NumOps = 1))

  val InputSplitter = Module(new SplitCallNew(List(1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlockNode(NumIns = List(1), NumOuts = List(), NumCarry = List(1), NumExits = 1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 4, BID = 0))

  val bb_pfor_cond_cleanup1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_pfor_detach2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 1, BID = 2))

  val bb_pfor_inc173 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 5, BID = 3))

  val bb_sync_continue194 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 4))

  val bb_offload_pfor_body5 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 5))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %foo = alloca i32, align 4, !UID !10
  val alloca_foo0 = Module(new AllocaNode(NumOuts=4, ID = 0, RouteID=0))

  //  %foo.0.foo.0..sroa_cast = bitcast i32* %foo to i8*, !UID !11
  val bitcast_foo_0_foo_0__sroa_cast1 = Module(new BitCastNode(NumOuts = 1, ID = 1))

  //  store i32 %j, i32* %foo, align 4, !UID !12
  val st_2 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 1, ID = 2, RouteID = 0))

  //  br label %pfor.detach, !UID !13, !BB_UID !14
  val br_3 = Module(new UBranchNode(ID = 3))

  //  sync within %syncreg, label %sync.continue19, !UID !15, !BB_UID !16
  val sync_4 = Module(new SyncTC(ID = 4, NumInc=1, NumDec=1, NumOuts=1))

  //  %__begin.040 = phi i32 [ 0, %entry ], [ %inc18, %pfor.inc17 ], !UID !17
  val phi__begin_0405 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 5, Res = true))

  //  detach within %syncreg, label %offload.pfor.body, label %pfor.inc17, !UID !18, !BB_UID !19
  val detach_6 = Module(new Detach(ID = 6))

  //  %inc18 = add nuw nsw i32 %__begin.040, 1, !UID !20
  val binaryOp_inc187 = Module(new ComputeNode(NumOuts = 2, ID = 7, opCode = "add")(sign = false))

  //  %exitcond41 = icmp eq i32 %inc18, 5, !UID !21
  val icmp_exitcond418 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "eq")(sign = false))

  //  br i1 %exitcond41, label %pfor.cond.cleanup, label %pfor.detach, !llvm.loop !22, !UID !24, !BB_UID !25
  val br_9 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 9))

  //  %foo.0.load38 = load i32, i32* %foo, align 4, !UID !26
  val ld_10 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 10, RouteID = 0))

  //  ret i32 %foo.0.load38, !UID !27, !BB_UID !28
  val ret_11 = Module(new RetNode2(retTypes = List(32), ID = 11))

  //  call void @cilk_for_test02_detach1(i32* %foo), !UID !29
  val call_12_out = Module(new CallOutNode(ID = 12, NumSuccOps = 0, argTypes = List(32)))

  val call_12_in = Module(new CallInNode(ID = 12, argTypes = List()))

  //  reattach within %syncreg, label %pfor.inc17, !UID !30, !BB_UID !31
  val reattach_13 = Module(new Reattach(NumPredOps= 1, ID = 13))



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

  bb_pfor_inc173.io.predicateIn(0) <> detach_6.io.Out(0)

  bb_sync_continue194.io.predicateIn(0) <> sync_4.io.Out(0)

  bb_offload_pfor_body5.io.predicateIn(0) <> detach_6.io.Out(1)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */

  bb_pfor_cond_cleanup1.io.predicateIn(0) <> Loop_0.io.loopExit(0)

  bb_pfor_detach2.io.predicateIn(1) <> Loop_0.io.activate_loop_start

  bb_pfor_detach2.io.predicateIn(0) <> Loop_0.io.activate_loop_back



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_4.io.incIn(0) <> detach_6.io.Out(2)

  sync_4.io.decIn(0) <> reattach_13.io.Out(0)



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_3.io.Out(0)

  Loop_0.io.loopBack(0) <> br_9.io.FalseOutput(0)

  Loop_0.io.loopFinish(0) <> br_9.io.TrueOutput(0)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.InLiveIn(0) <> alloca_foo0.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  call_12_out.io.In.elements("field0") <> Loop_0.io.OutLiveIn.elements("field0")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */

  Loop_0.io.CarryDepenIn(0) <> binaryOp_inc187.io.Out(0)



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_0405.io.InData(1) <> Loop_0.io.CarryDepenOut.elements("field0")(0)



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  alloca_foo0.io.enable <> bb_entry0.io.Out(0)


  bitcast_foo_0_foo_0__sroa_cast1.io.enable <> bb_entry0.io.Out(1)


  st_2.io.enable <> bb_entry0.io.Out(2)


  bb_entry0.io.Out(3).ready := true.B

  br_3.io.enable <> st_2.io.SuccOp(0)


  sync_4.io.enable <> bb_pfor_cond_cleanup1.io.Out(0)


  const0.io.enable <> bb_pfor_detach2.io.Out(0)

  phi__begin_0405.io.enable <> bb_pfor_detach2.io.Out(1)


  detach_6.io.enable <> bb_pfor_detach2.io.Out(2)


  const1.io.enable <> bb_pfor_inc173.io.Out(0)

  const2.io.enable <> bb_pfor_inc173.io.Out(1)

  binaryOp_inc187.io.enable <> bb_pfor_inc173.io.Out(2)


  icmp_exitcond418.io.enable <> bb_pfor_inc173.io.Out(3)


  br_9.io.enable <> bb_pfor_inc173.io.Out(4)


  ld_10.io.enable <> bb_sync_continue194.io.Out(0)


  ret_11.io.In.enable <> bb_sync_continue194.io.Out(1)


  call_12_in.io.enable <> bb_offload_pfor_body5.io.Out(1)

  call_12_out.io.enable <> bb_offload_pfor_body5.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi__begin_0405.io.Mask <> bb_pfor_detach2.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */

  alloca_foo0.io.allocaInputIO.bits.size      := 1.U
  alloca_foo0.io.allocaInputIO.bits.numByte   := 4.U
  alloca_foo0.io.allocaInputIO.bits.predicate := true.B
  alloca_foo0.io.allocaInputIO.bits.valid     := true.B
  alloca_foo0.io.allocaInputIO.valid          := true.B





  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  StackPointer.io.InData(0) <> alloca_foo0.io.allocaReqIO

  alloca_foo0.io.allocaRespIO <> StackPointer.io.OutData(0)

  MemCtrl.io.WriteIn(0) <> st_2.io.memReq

  st_2.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(0) <> ld_10.io.memReq

  ld_10.io.memResp <> MemCtrl.io.ReadOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi__begin_0405.io.InData(0) <> const0.io.Out

  binaryOp_inc187.io.RightIO <> const1.io.Out

  icmp_exitcond418.io.RightIO <> const2.io.Out

  bitcast_foo_0_foo_0__sroa_cast1.io.Input <> alloca_foo0.io.Out(1)

  bitcast_foo_0_foo_0__sroa_cast1.io.Out(0).ready := true.B

  st_2.io.GepAddr <> alloca_foo0.io.Out(2)

  ld_10.io.GepAddr <> alloca_foo0.io.Out(3)

  binaryOp_inc187.io.LeftIO <> phi__begin_0405.io.Out(0)

  icmp_exitcond418.io.LeftIO <> binaryOp_inc187.io.Out(1)

  br_9.io.CmpIO <> icmp_exitcond418.io.Out(0)

  ret_11.io.In.data("field0") <> ld_10.io.Out(0)

  st_2.io.inData <> InputSplitter.io.Out.data.elements("field0")(0)

  st_2.io.Out(0).ready := true.B

  reattach_13.io.predicateIn(0).enq(DataBundle.active(1.U))



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_12_in.io.In <> io.call_12_in

  io.call_12_out <> call_12_out.io.Out(0)

  reattach_13.io.enable <> call_12_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_11.io.Out

}

import java.io.{File, FileWriter}

object cilk_for_test02Top extends App {
  val dir = new File("RTL/cilk_for_test02Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_for_test02DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
