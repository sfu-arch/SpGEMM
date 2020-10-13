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

abstract class dedup_S2DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val call_8_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_8_in = Flipped(Decoupled(new Call(List(32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class dedup_S2DF(implicit p: Parameters) extends dedup_S2DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=2, NWrites=2)
		 (WControl=new WriteMemoryController(NumOps=2, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=2, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,3,2,2)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 9, BID = 0))

  val bb_if_then1 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_det_achd2 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_det_cont3 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_if_else4 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 4))

  val bb_if_end5 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 1, NumPhi = 0, BID = 5))

  val bb_sync_continue6 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 6))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %arrayidx = getelementptr inbounds i32, i32* %x, i32 %pos
  val Gep_arrayidx0 = Module(new GepArrayOneNode(NumOuts=3, ID=0)(numByte=4)(size=1))

  //  %arrayidx1 = getelementptr inbounds i32, i32* %arrayidx, i32 0
  val Gep_arrayidx11 = Module(new GepArrayOneNode(NumOuts=1, ID=1)(numByte=4)(size=1))

  //  %0 = load i32, i32* %arrayidx1, align 4
  val ld_2 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=2, RouteID=0))

  //  %arrayidx2 = getelementptr inbounds i32, i32* %arrayidx, i32 1
  val Gep_arrayidx23 = Module(new GepArrayOneNode(NumOuts=1, ID=3)(numByte=4)(size=1))

  //  %1 = load i32, i32* %arrayidx2, align 4
  val ld_4 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=4, RouteID=1))

  //  %cmp = icmp eq i32 %0, %1
  val icmp_cmp5 = Module(new IcmpNode(NumOuts = 1, ID = 5, opCode = "eq")(sign=false))

  //  br i1 %cmp, label %if.then, label %if.else
  val br_6 = Module(new CBranchNode(ID = 6))

  //  detach label %det.achd, label %det.cont
  val detach_7 = Module(new Detach(ID = 7))

  //  call void @dedup_S3(i32* %arrayidx, i32 %pos, i32 %wptr, i32* %q)
  val call_8_out = Module(new CallOutNode(ID = 8, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_8_in = Module(new CallInNode(ID = 8, argTypes = List(32)))

  //  reattach label %det.cont
  val reattach_9 = Module(new Reattach(NumPredOps= 1, ID = 9))

  //  br label %if.end
  val br_10 = Module(new UBranchNode(ID = 10))

  //  %arrayidx3 = getelementptr inbounds i32, i32* %q, i32 %wptr
  val Gep_arrayidx311 = Module(new GepArrayOneNode(NumOuts=1, ID=11)(numByte=4)(size=1))

  //  store volatile i32 %pos, i32* %arrayidx3, align 4
  val st_12 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=12, RouteID=0))

  //  br label %if.end
  val br_13 = Module(new UBranchNode(ID = 13))

  //  sync label %sync.continue
  val sync_14 = Module(new SyncTC(ID = 14, NumInc=1, NumDec=1, NumOuts=1))

  //  ret void
  val ret_15 = Module(new RetNode(retTypes=List(32), ID = 15))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 0))

  //i32 1
  val const1 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 1))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_if_then1.io.predicateIn <> br_6.io.Out(0)

  bb_det_achd2.io.predicateIn <> detach_7.io.Out(1)

  bb_det_cont3.io.predicateIn <> detach_7.io.Out(0)

  bb_if_else4.io.predicateIn <> br_6.io.Out(1)

  bb_if_end5.io.predicateIn(0) <> br_10.io.Out(0)

  bb_if_end5.io.predicateIn(1) <> br_13.io.Out(0)

  bb_sync_continue6.io.predicateIn <> sync_14.io.Out(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_14.io.incIn(0) <> detach_7.io.Out(2)

  sync_14.io.decIn(0) <> reattach_9.io.Out(0)



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

  const0.io.enable <> bb_entry0.io.Out(0)

  const1.io.enable <> bb_entry0.io.Out(1)

  Gep_arrayidx0.io.enable <> bb_entry0.io.Out(2)

  Gep_arrayidx11.io.enable <> bb_entry0.io.Out(3)

  ld_2.io.enable <> bb_entry0.io.Out(4)

  Gep_arrayidx23.io.enable <> bb_entry0.io.Out(5)

  ld_4.io.enable <> bb_entry0.io.Out(6)

  icmp_cmp5.io.enable <> bb_entry0.io.Out(7)

  br_6.io.enable <> bb_entry0.io.Out(8)


  detach_7.io.enable <> bb_if_then1.io.Out(0)


  call_8_in.io.enable.enq(ControlBundle.active())

  call_8_out.io.enable <> bb_det_achd2.io.Out(0)


  br_10.io.enable <> bb_det_cont3.io.Out(0)


  Gep_arrayidx311.io.enable <> bb_if_else4.io.Out(0)

  st_12.io.enable <> bb_if_else4.io.Out(1)

  br_13.io.enable <> bb_if_else4.io.Out(2)


  sync_14.io.enable <> bb_if_end5.io.Out(0)


  ret_15.io.enable <> bb_sync_continue6.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_2.io.memReq

  ld_2.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_4.io.memReq

  ld_4.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_12.io.memReq

  st_12.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_arrayidx11.io.idx1 <> const0.io.Out(0)

  Gep_arrayidx23.io.idx1 <> const1.io.Out(0)

  Gep_arrayidx11.io.baseAddress <> Gep_arrayidx0.io.Out(0)

  Gep_arrayidx23.io.baseAddress <> Gep_arrayidx0.io.Out(1)

  call_8_out.io.In("field0") <> Gep_arrayidx0.io.Out(2)

  ld_2.io.GepAddr <> Gep_arrayidx11.io.Out(0)

  icmp_cmp5.io.LeftIO <> ld_2.io.Out(0)

  ld_4.io.GepAddr <> Gep_arrayidx23.io.Out(0)

  icmp_cmp5.io.RightIO <> ld_4.io.Out(0)

  br_6.io.CmpIO <> icmp_cmp5.io.Out(0)

  reattach_9.io.predicateIn(0) <> call_8_in.io.Out.data("field0")

  st_12.io.GepAddr <> Gep_arrayidx311.io.Out(0)

  ret_15.io.In.elements("field0") <> st_12.io.Out(0)

  Gep_arrayidx0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_arrayidx0.io.idx1 <> InputSplitter.io.Out.data.elements("field1")(0)

  call_8_out.io.In("field1") <> InputSplitter.io.Out.data.elements("field1")(1)

  st_12.io.inData <> InputSplitter.io.Out.data.elements("field1")(2)

  call_8_out.io.In("field2") <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_arrayidx311.io.idx1 <> InputSplitter.io.Out.data.elements("field2")(1)

  call_8_out.io.In("field3") <> InputSplitter.io.Out.data.elements("field3")(0)

  Gep_arrayidx311.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(1)



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_8_in.io.In <> io.call_8_in

  io.call_8_out <> call_8_out.io.Out(0)

  reattach_9.io.enable <> call_8_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_15.io.Out

}

import java.io.{File, FileWriter}
object dedup_S2Main extends App {
  val dir = new File("RTL/dedup_S2") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new dedup_S2DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
