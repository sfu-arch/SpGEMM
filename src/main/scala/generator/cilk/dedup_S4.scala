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
import org.scalatest.Matchers._
import regfile._
import stack._
import util._


  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */

abstract class dedup_S4DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class dedup_S4DF(implicit p: Parameters) extends dedup_S4DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=3, NWrites=3)
		 (WControl=new WriteMemoryController(NumOps=3, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=3, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,1,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlock(NumIns=List(2,2,2), NumOuts = 1, NumExits=1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_while_cond1 = Module(new LoopHead(NumOuts = 7, NumPhi=1, BID = 1))

  val bb_while_body2 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 2))

  val bb_if_then3 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 20, BID = 3))

  val bb_if_end4 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 2, NumPhi=1, BID = 4))

  val bb_while_end5 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 5))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  br label %while.cond
  val br_0 = Module(new UBranchNode(ID = 0))

  //  %rptr.0 = phi i32 [ 0, %entry ], [ %rptr.1, %if.end ]
  val phi_rptr_01 = Module(new PhiNode(NumInputs = 2, NumOuts = 4, ID = 1))

  //  %arrayidx = getelementptr inbounds i32, i32* %q, i32 %rptr.0
  val Gep_arrayidx2 = Module(new GepArrayOneNode(NumOuts=1, ID=2)(numByte=4)(size=1))

  //  %0 = load volatile i32, i32* %arrayidx, align 4
  val ld_3 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=6, ID=3, RouteID=0))

  //  %cmp = icmp ne i32 %0, 9999
  val icmp_cmp4 = Module(new IcmpNode(NumOuts = 1, ID = 4, opCode = "ne")(sign=false))

  //  br i1 %cmp, label %while.body, label %while.end
  val br_5 = Module(new CBranchNode(ID = 5))

  //  %cmp1 = icmp ne i32 %0, 999
  val icmp_cmp16 = Module(new IcmpNode(NumOuts = 1, ID = 6, opCode = "ne")(sign=false))

  //  br i1 %cmp1, label %if.then, label %if.end
  val br_7 = Module(new CBranchNode(ID = 7))

  //  %arrayidx2 = getelementptr inbounds i32, i32* %x, i32 %0
  val Gep_arrayidx28 = Module(new GepArrayOneNode(NumOuts=1, ID=8)(numByte=4)(size=1))

  //  %1 = load i32, i32* %arrayidx2, align 4
  val ld_9 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=9, RouteID=1))

  //  %arrayidx3 = getelementptr inbounds i32, i32* %y, i32 %0
  val Gep_arrayidx310 = Module(new GepArrayOneNode(NumOuts=1, ID=10)(numByte=4)(size=1))

  //  store i32 %1, i32* %arrayidx3, align 4
  val st_11 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=1, ID=11, RouteID=0))

  //  %add = add i32 %0, 1
  val binaryOp_add12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign=false))

  //  %arrayidx4 = getelementptr inbounds i32, i32* %x, i32 %add
  val Gep_arrayidx413 = Module(new GepArrayOneNode(NumOuts=1, ID=13)(numByte=4)(size=1))

  //  %2 = load i32, i32* %arrayidx4, align 4
  val ld_14 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=14, RouteID=2))

  //  %add5 = add i32 %0, 1
  val binaryOp_add515 = Module(new ComputeNode(NumOuts = 1, ID = 15, opCode = "add")(sign=false))

  //  %arrayidx6 = getelementptr inbounds i32, i32* %y, i32 %add5
  val Gep_arrayidx616 = Module(new GepArrayOneNode(NumOuts=1, ID=16)(numByte=4)(size=1))

  //  store i32 %2, i32* %arrayidx6, align 4
  val st_17 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=1, ID=17, RouteID=1))

  //  %arrayidx7 = getelementptr inbounds i32, i32* %q, i32 %rptr.0
  val Gep_arrayidx718 = Module(new GepArrayOneNode(NumOuts=1, ID=18)(numByte=4)(size=1))

  //  store volatile i32 999, i32* %arrayidx7, align 4
  val st_19 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=1, ID=19, RouteID=2))

  //  %add8 = add i32 %rptr.0, 1
  val binaryOp_add820 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "add")(sign=false))

  //  %and = and i32 %add8, 127
  val binaryOp_and21 = Module(new ComputeNode(NumOuts = 1, ID = 21, opCode = "and")(sign=false))

  //  br label %if.end
  val br_22 = Module(new UBranchNode(NumPredOps=3, ID = 22))

  //  %rptr.1 = phi i32 [ %and, %if.then ], [ %rptr.0, %while.body ]
  val phi_rptr_123 = Module(new PhiNode(NumInputs = 2, NumOuts = 1, ID = 23))

  //  br label %while.cond
  val br_24 = Module(new UBranchNode(NumOuts=2, ID = 24))

  //  ret void
  val ret_25 = Module(new RetNode(retTypes=List(32), ID = 25))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 0))

  //i32 9999
  val const1 = Module(new ConstNode(value = 9999, NumOuts = 1, ID = 1))

  //i32 999
  val const2 = Module(new ConstNode(value = 999, NumOuts = 1, ID = 2))

  //i32 1
  val const3 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 3))

  //i32 1
  val const4 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 4))

  //i32 999
  val const5 = Module(new ConstNode(value = 999, NumOuts = 1, ID = 5))

  //i32 1
  val const6 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 6))

  //i32 127
  val const7 = Module(new ConstNode(value = 127, NumOuts = 1, ID = 7))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_while_cond1.io.activate <> Loop_0.io.activate

  bb_while_cond1.io.loopBack <> br_24.io.Out(0)

  bb_while_body2.io.predicateIn <> br_5.io.Out(0)

  bb_if_then3.io.predicateIn <> br_7.io.Out(0)

  bb_if_end4.io.predicateIn(0) <> br_7.io.Out(1)

  bb_if_end4.io.predicateIn(1) <> br_22.io.Out(0)

  bb_while_end5.io.predicateIn <> Loop_0.io.endEnable



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_0.io.Out(0)

  Loop_0.io.latchEnable <> br_24.io.Out(1)

  Loop_0.io.loopExit(0) <> br_5.io.Out(1)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */

  br_22.io.PredOp(0) <> st_11.io.SuccOp(0)

  br_22.io.PredOp(1) <> st_17.io.SuccOp(0)

  br_22.io.PredOp(2) <> st_19.io.SuccOp(0)



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.In(0) <> InputSplitter.io.Out.data.elements("field2")(0)

  Loop_0.io.In(1) <> InputSplitter.io.Out.data.elements("field0")(0)

  Loop_0.io.In(2) <> InputSplitter.io.Out.data.elements("field1")(0)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_arrayidx2.io.baseAddress <> Loop_0.io.liveIn.elements("field0")(0)

  Gep_arrayidx718.io.baseAddress <> Loop_0.io.liveIn.elements("field0")(1)

  Gep_arrayidx28.io.baseAddress <> Loop_0.io.liveIn.elements("field1")(0)

  Gep_arrayidx413.io.baseAddress <> Loop_0.io.liveIn.elements("field1")(1)

  Gep_arrayidx310.io.baseAddress <> Loop_0.io.liveIn.elements("field2")(0)

  Gep_arrayidx616.io.baseAddress <> Loop_0.io.liveIn.elements("field2")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */
  Loop_0.io.liveOut(0) <> st_19.io.Out(0)  // Manual


  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  br_0.io.enable <> bb_entry0.io.Out(0)


  const0.io.enable <> bb_while_cond1.io.Out(0)

  const1.io.enable <> bb_while_cond1.io.Out(1)

  phi_rptr_01.io.enable <> bb_while_cond1.io.Out(2)

  Gep_arrayidx2.io.enable <> bb_while_cond1.io.Out(3)

  ld_3.io.enable <> bb_while_cond1.io.Out(4)

  icmp_cmp4.io.enable <> bb_while_cond1.io.Out(5)

  br_5.io.enable <> bb_while_cond1.io.Out(6)


  const2.io.enable <> bb_while_body2.io.Out(0)

  icmp_cmp16.io.enable <> bb_while_body2.io.Out(1)

  br_7.io.enable <> bb_while_body2.io.Out(2)


  const3.io.enable <> bb_if_then3.io.Out(0)

  const4.io.enable <> bb_if_then3.io.Out(1)

  const5.io.enable <> bb_if_then3.io.Out(2)

  const6.io.enable <> bb_if_then3.io.Out(3)

  const7.io.enable <> bb_if_then3.io.Out(4)

  Gep_arrayidx28.io.enable <> bb_if_then3.io.Out(5)

  ld_9.io.enable <> bb_if_then3.io.Out(6)

  Gep_arrayidx310.io.enable <> bb_if_then3.io.Out(7)

  st_11.io.enable <> bb_if_then3.io.Out(8)

  binaryOp_add12.io.enable <> bb_if_then3.io.Out(9)

  Gep_arrayidx413.io.enable <> bb_if_then3.io.Out(10)

  ld_14.io.enable <> bb_if_then3.io.Out(11)

  binaryOp_add515.io.enable <> bb_if_then3.io.Out(12)

  Gep_arrayidx616.io.enable <> bb_if_then3.io.Out(13)

  st_17.io.enable <> bb_if_then3.io.Out(14)

  Gep_arrayidx718.io.enable <> bb_if_then3.io.Out(15)

  st_19.io.enable <> bb_if_then3.io.Out(16)

  binaryOp_add820.io.enable <> bb_if_then3.io.Out(17)

  binaryOp_and21.io.enable <> bb_if_then3.io.Out(18)

  br_22.io.enable <> bb_if_then3.io.Out(19)


  phi_rptr_123.io.enable <> bb_if_end4.io.Out(0)

  br_24.io.enable <> bb_if_end4.io.Out(1)


  ret_25.io.enable <> bb_while_end5.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi_rptr_01.io.Mask <> bb_while_cond1.io.MaskBB(0)

  phi_rptr_123.io.Mask <> bb_if_end4.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_3.io.memReq

  ld_3.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_9.io.memReq

  ld_9.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_11.io.memReq

  st_11.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.ReadIn(2) <> ld_14.io.memReq

  ld_14.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(1) <> st_17.io.memReq

  st_17.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_19.io.memReq

  st_19.io.memResp <> MemCtrl.io.WriteOut(2)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi_rptr_01.io.InData(0) <> const0.io.Out(0)

  icmp_cmp4.io.RightIO <> const1.io.Out(0)

  icmp_cmp16.io.RightIO <> const2.io.Out(0)

  binaryOp_add12.io.RightIO <> const3.io.Out(0)

  binaryOp_add515.io.RightIO <> const4.io.Out(0)

  st_19.io.inData <> const5.io.Out(0)

  binaryOp_add820.io.RightIO <> const6.io.Out(0)

  binaryOp_and21.io.RightIO <> const7.io.Out(0)

  Gep_arrayidx2.io.idx1 <> phi_rptr_01.io.Out(0)

  Gep_arrayidx718.io.idx1 <> phi_rptr_01.io.Out(1)

  binaryOp_add820.io.LeftIO <> phi_rptr_01.io.Out(2)

  phi_rptr_123.io.InData(1) <> phi_rptr_01.io.Out(3)

  ld_3.io.GepAddr <> Gep_arrayidx2.io.Out(0)

  icmp_cmp4.io.LeftIO <> ld_3.io.Out(0)

  icmp_cmp16.io.LeftIO <> ld_3.io.Out(1)

  Gep_arrayidx28.io.idx1 <> ld_3.io.Out(2)

  Gep_arrayidx310.io.idx1 <> ld_3.io.Out(3)

  binaryOp_add12.io.LeftIO <> ld_3.io.Out(4)

  binaryOp_add515.io.LeftIO <> ld_3.io.Out(5)

  br_5.io.CmpIO <> icmp_cmp4.io.Out(0)

  br_7.io.CmpIO <> icmp_cmp16.io.Out(0)

  ld_9.io.GepAddr <> Gep_arrayidx28.io.Out(0)

  st_11.io.inData <> ld_9.io.Out(0)

  st_11.io.GepAddr <> Gep_arrayidx310.io.Out(0)

  Gep_arrayidx413.io.idx1 <> binaryOp_add12.io.Out(0)

  ld_14.io.GepAddr <> Gep_arrayidx413.io.Out(0)

  st_17.io.inData <> ld_14.io.Out(0)

  Gep_arrayidx616.io.idx1 <> binaryOp_add515.io.Out(0)

  st_17.io.GepAddr <> Gep_arrayidx616.io.Out(0)

  st_19.io.GepAddr <> Gep_arrayidx718.io.Out(0)

  ret_25.io.In.elements("field0") <> Loop_0.io.Out(0)// st_19.io.Out(0) Manual

  binaryOp_and21.io.LeftIO <> binaryOp_add820.io.Out(0)

  phi_rptr_123.io.InData(0) <> binaryOp_and21.io.Out(0)

  phi_rptr_01.io.InData(1) <> phi_rptr_123.io.Out(0)

  st_11.io.Out(0).ready := true.B

  st_17.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_25.io.Out

}

import java.io.{File, FileWriter}
object dedup_S4Main extends App {
  val dir = new File("RTL/dedup_S4") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new dedup_S4DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
