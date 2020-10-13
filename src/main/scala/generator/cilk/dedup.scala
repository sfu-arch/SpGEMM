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

abstract class dedupDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val call_1_out = Decoupled(new Call(List(32, 32, 32)))
    val call_1_in = Flipped(Decoupled(new Call(List(32))))
    val call_15_out = Decoupled(new Call(List(32, 32, 32, 32)))
    val call_15_in = Flipped(Decoupled(new Call(List(32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class dedupDF(implicit p: Parameters) extends dedupDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=2, NWrites=2)
		 (WControl=new WriteMemoryController(NumOps=2, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=2, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(2,1,4)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */

  val Loop_0 = Module(new LoopBlock(NumIns=List(2,2), NumOuts = 2, NumExits=1, ID = 0))



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 0))

  val bb_det_achd1 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_det_cont2 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 2))

  val bb_while_cond3 = Module(new LoopHead(NumOuts = 9, NumPhi=2, BID = 3))

  val bb_while_body4 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 4))

  val bb_if_then5 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 5))

  val bb_det_achd36 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 6))

  val bb_det_cont47 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 7, BID = 7))

  val bb_if_end8 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi=2, BID = 8))

  val bb_while_end9 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 9))

  val bb_sync_continue10 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 10))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  detach label %det.achd, label %det.cont
  val detach_0 = Module(new Detach(ID = 0))

  //  call void @dedup_S4(i32* %x, i32* %y, i32* %q)
  val call_1_out = Module(new CallOutNode(ID = 1, NumSuccOps = 0, argTypes = List(32,32,32)))

  val call_1_in = Module(new CallInNode(ID = 1, argTypes = List(32)))

  //  reattach label %det.cont
  //val reattach_2 = Module(new Reattach(NumPredOps= 1, ID = 2))

  //  br label %while.cond
  val br_3 = Module(new UBranchNode(ID = 3))

  //  %pos.0 = phi i32 [ 0, %det.cont ], [ %pos.1, %if.end ]
  val phi_pos_04 = Module(new PhiNode(NumInputs = 2, NumOuts = 4, ID = 4))

  //  %wptr.0 = phi i32 [ 0, %det.cont ], [ %wptr.1, %if.end ]
  val phi_wptr_05 = Module(new PhiNode(NumInputs = 2, NumOuts = 6, ID = 5))

  //  %arrayidx = getelementptr inbounds i32, i32* %x, i32 %pos.0
  val Gep_arrayidx6 = Module(new GepArrayOneNode(NumOuts=1, ID=6)(numByte=4)(size=1))

  //  %0 = load i32, i32* %arrayidx, align 4
  val ld_7 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=7, RouteID=0))

  //  %cmp = icmp ne i32 %0, 0
  val icmp_cmp8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "ne")(sign=false))

  //  br i1 %cmp, label %while.body, label %while.end
  val br_9 = Module(new CBranchNode(ID = 9))

  //  %arrayidx1 = getelementptr inbounds i32, i32* %q, i32 %wptr.0
  val Gep_arrayidx110 = Module(new GepArrayOneNode(NumOuts=1, ID=10)(numByte=4)(size=1))

  //  %1 = load volatile i32, i32* %arrayidx1, align 4
  val ld_11 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=11, RouteID=1))

  //  %cmp2 = icmp eq i32 %1, 999
  val icmp_cmp212 = Module(new IcmpNode(NumOuts = 1, ID = 12, opCode = "eq")(sign=false))

  //  br i1 %cmp2, label %if.then, label %if.end
  val br_13 = Module(new CBranchNode(ID = 13))

  //  detach label %det.achd3, label %det.cont4
  val detach_14 = Module(new Detach(ID = 14))

  //  call void @dedup_S2(i32* %x, i32 %pos.0, i32 %wptr.0, i32* %q)
  val call_15_out = Module(new CallOutNode(ID = 15, NumSuccOps = 0, argTypes = List(32,32,32,32)))

  val call_15_in = Module(new CallInNode(ID = 15, argTypes = List(32)))

  //  reattach label %det.cont4
  //val reattach_16 = Module(new Reattach(NumPredOps= 1, ID = 16))

  //  %add = add nsw i32 %pos.0, 2
  val binaryOp_add17 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "add")(sign=false))

  //  %add5 = add i32 %wptr.0, 1
  val binaryOp_add518 = Module(new ComputeNode(NumOuts = 1, ID = 18, opCode = "add")(sign=false))

  //  %and = and i32 %add5, 127
  val binaryOp_and19 = Module(new ComputeNode(NumOuts = 1, ID = 19, opCode = "and")(sign=false))

  //  br label %if.end
  val br_20 = Module(new UBranchNode(ID = 20))

  //  %pos.1 = phi i32 [ %add, %det.cont4 ], [ %pos.0, %while.body ]
  val phi_pos_121 = Module(new PhiNode(NumInputs = 2, NumOuts = 1, ID = 21))

  //  %wptr.1 = phi i32 [ %and, %det.cont4 ], [ %wptr.0, %while.body ]
  val phi_wptr_122 = Module(new PhiNode(NumInputs = 2, NumOuts = 1, ID = 22))

  //  br label %while.cond
  val br_23 = Module(new UBranchNode(NumOuts=2, ID = 23))

  //  %arrayidx6 = getelementptr inbounds i32, i32* %q, i32 %wptr.0
  val Gep_arrayidx624 = Module(new GepArrayOneNode(NumOuts=1, ID=24)(numByte=4)(size=1))

  //  store volatile i32 9999, i32* %arrayidx6, align 4
  val st_25 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=25, RouteID=0))

  //  sync label %sync.continue
  val sync_26 = Module(new SyncTC(ID = 26, NumInc=2, NumDec=2, NumOuts=1))

  //  %arrayidx7 = getelementptr inbounds i32, i32* %q, i32 %wptr.0
  val Gep_arrayidx727 = Module(new GepArrayOneNode(NumOuts=1, ID=27)(numByte=4)(size=1))

  //  store volatile i32 999, i32* %arrayidx7, align 4
  val st_28 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=28, RouteID=1))

  //  ret void
  val ret_29 = Module(new RetNode(retTypes=List(32), ID = 29))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 0))

  //i32 0
  val const1 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 1))

  //i32 0
  val const2 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 2))

  //i32 999
  val const3 = Module(new ConstNode(value = 999, NumOuts = 1, ID = 3))

  //i32 2
  val const4 = Module(new ConstNode(value = 2, NumOuts = 1, ID = 4))

  //i32 1
  val const5 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 5))

  //i32 127
  val const6 = Module(new ConstNode(value = 127, NumOuts = 1, ID = 6))

  //i32 9999
  val const7 = Module(new ConstNode(value = 9999, NumOuts = 1, ID = 7))

  //i32 999
  val const8 = Module(new ConstNode(value = 999, NumOuts = 1, ID = 8))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_det_achd1.io.predicateIn <> detach_0.io.Out(1)

  bb_det_cont2.io.predicateIn <> detach_0.io.Out(0)

  bb_while_cond3.io.activate <> Loop_0.io.activate

  bb_while_cond3.io.loopBack <> br_23.io.Out(0)

  bb_while_body4.io.predicateIn <> br_9.io.Out(0)

  bb_if_then5.io.predicateIn <> br_13.io.Out(0)

  bb_det_achd36.io.predicateIn <> detach_14.io.Out(1)

  bb_det_cont47.io.predicateIn <> detach_14.io.Out(0)

  bb_if_end8.io.predicateIn(0) <> br_13.io.Out(1)

  bb_if_end8.io.predicateIn(1) <> br_20.io.Out(0)

  bb_while_end9.io.predicateIn <> Loop_0.io.endEnable

  bb_sync_continue10.io.predicateIn <> sync_26.io.Out(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */

  sync_26.io.incIn(0) <> detach_0.io.Out(2)
  sync_26.io.incIn(1) <> detach_14.io.Out(2)

  sync_26.io.decIn(0) <> call_1_in.io.Out.enable//reattach_2.io.Out(0)
  sync_26.io.decIn(1) <> call_15_in.io.Out.enable//reattach_16.io.Out(0)



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */

  Loop_0.io.enable <> br_3.io.Out(0)

  Loop_0.io.latchEnable <> br_23.io.Out(1)

  Loop_0.io.loopExit(0) <> br_9.io.Out(1)



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */

  Loop_0.io.In(0) <> InputSplitter.io.Out.data.elements("field0")(1)

  Loop_0.io.In(1) <> InputSplitter.io.Out.data.elements("field2")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */

  Gep_arrayidx6.io.baseAddress <> Loop_0.io.liveIn.elements("field0")(0)

  call_15_out.io.In("field0") <> Loop_0.io.liveIn.elements("field0")(1)

  Gep_arrayidx110.io.baseAddress <> Loop_0.io.liveIn.elements("field1")(0)

  call_15_out.io.In("field3") <> Loop_0.io.liveIn.elements("field1")(1)



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */

  Loop_0.io.liveOut(0) <> phi_wptr_05.io.Out(5)

  Loop_0.io.liveOut(1) <> phi_wptr_05.io.Out(4)  // Manually corrected index



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  detach_0.io.enable <> bb_entry0.io.Out(0)


  call_1_in.io.enable.enq(ControlBundle.active())

  call_1_out.io.enable <> bb_det_achd1.io.Out(0)


  br_3.io.enable <> bb_det_cont2.io.Out(0)


  const0.io.enable <> bb_while_cond3.io.Out(0)

  const1.io.enable <> bb_while_cond3.io.Out(1)

  const2.io.enable <> bb_while_cond3.io.Out(2)

  phi_pos_04.io.enable <> bb_while_cond3.io.Out(3)

  phi_wptr_05.io.enable <> bb_while_cond3.io.Out(4)

  Gep_arrayidx6.io.enable <> bb_while_cond3.io.Out(5)

  ld_7.io.enable <> bb_while_cond3.io.Out(6)

  icmp_cmp8.io.enable <> bb_while_cond3.io.Out(7)

  br_9.io.enable <> bb_while_cond3.io.Out(8)


  const3.io.enable <> bb_while_body4.io.Out(0)

  Gep_arrayidx110.io.enable <> bb_while_body4.io.Out(1)

  ld_11.io.enable <> bb_while_body4.io.Out(2)

  icmp_cmp212.io.enable <> bb_while_body4.io.Out(3)

  br_13.io.enable <> bb_while_body4.io.Out(4)


  detach_14.io.enable <> bb_if_then5.io.Out(0)


  call_15_in.io.enable.enq(ControlBundle.active())

  call_15_out.io.enable <> bb_det_achd36.io.Out(0)


  const4.io.enable <> bb_det_cont47.io.Out(0)

  const5.io.enable <> bb_det_cont47.io.Out(1)

  const6.io.enable <> bb_det_cont47.io.Out(2)

  binaryOp_add17.io.enable <> bb_det_cont47.io.Out(3)

  binaryOp_add518.io.enable <> bb_det_cont47.io.Out(4)

  binaryOp_and19.io.enable <> bb_det_cont47.io.Out(5)

  br_20.io.enable <> bb_det_cont47.io.Out(6)


  phi_pos_121.io.enable <> bb_if_end8.io.Out(0)

  phi_wptr_122.io.enable <> bb_if_end8.io.Out(1)

  br_23.io.enable <> bb_if_end8.io.Out(2)


  const7.io.enable <> bb_while_end9.io.Out(0)

  Gep_arrayidx624.io.enable <> bb_while_end9.io.Out(1)

  st_25.io.enable <> bb_while_end9.io.Out(2)

  sync_26.io.enable <> bb_while_end9.io.Out(3)


  const8.io.enable <> bb_sync_continue10.io.Out(0)

  Gep_arrayidx727.io.enable <> bb_sync_continue10.io.Out(1)

  st_28.io.enable <> bb_sync_continue10.io.Out(2)

  ret_29.io.enable <> bb_sync_continue10.io.Out(3)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi_pos_04.io.Mask <> bb_while_cond3.io.MaskBB(0)

  phi_wptr_05.io.Mask <> bb_while_cond3.io.MaskBB(1)

  phi_pos_121.io.Mask <> bb_if_end8.io.MaskBB(0)

  phi_wptr_122.io.Mask <> bb_if_end8.io.MaskBB(1)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_7.io.memReq

  ld_7.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_11.io.memReq

  ld_11.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.WriteIn(0) <> st_25.io.memReq

  st_25.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_28.io.memReq

  st_28.io.memResp <> MemCtrl.io.WriteOut(1)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi_pos_04.io.InData(0) <> const0.io.Out(0)

  phi_wptr_05.io.InData(0) <> const1.io.Out(0)

  icmp_cmp8.io.RightIO <> const2.io.Out(0)

  icmp_cmp212.io.RightIO <> const3.io.Out(0)

  binaryOp_add17.io.RightIO <> const4.io.Out(0)

  binaryOp_add518.io.RightIO <> const5.io.Out(0)

  binaryOp_and19.io.RightIO <> const6.io.Out(0)

  st_25.io.inData <> const7.io.Out(0)

  st_28.io.inData <> const8.io.Out(0)

  call_1_in.io.Out.data.elements("field0").ready := true.B
//  reattach_2.io.predicateIn(0) <> DataBundle.active()//call_1_in.io.Out.data.elements("field0")

  Gep_arrayidx6.io.idx1 <> phi_pos_04.io.Out(0)

  call_15_out.io.In("field1") <> phi_pos_04.io.Out(1)

  binaryOp_add17.io.LeftIO <> phi_pos_04.io.Out(2)

  phi_pos_121.io.InData(1) <> phi_pos_04.io.Out(3)

  Gep_arrayidx110.io.idx1 <> phi_wptr_05.io.Out(0)

  call_15_out.io.In("field2") <> phi_wptr_05.io.Out(1)

  binaryOp_add518.io.LeftIO <> phi_wptr_05.io.Out(2)

  phi_wptr_122.io.InData(1) <> phi_wptr_05.io.Out(3)

  ld_7.io.GepAddr <> Gep_arrayidx6.io.Out(0)

  icmp_cmp8.io.LeftIO <> ld_7.io.Out(0)

  br_9.io.CmpIO <> icmp_cmp8.io.Out(0)

  ld_11.io.GepAddr <> Gep_arrayidx110.io.Out(0)

  icmp_cmp212.io.LeftIO <> ld_11.io.Out(0)

  br_13.io.CmpIO <> icmp_cmp212.io.Out(0)

  call_15_in.io.Out.data.elements("field0").ready := true.B
//  reattach_16.io.predicateIn(0) <> DataBundle.active()//call_15_in.io.Out.data.elements("field0")

  phi_pos_121.io.InData(0) <> binaryOp_add17.io.Out(0)

  binaryOp_and19.io.LeftIO <> binaryOp_add518.io.Out(0)

  phi_wptr_122.io.InData(0) <> binaryOp_and19.io.Out(0)

  phi_pos_04.io.InData(1) <> phi_pos_121.io.Out(0)

  phi_wptr_05.io.InData(1) <> phi_wptr_122.io.Out(0)

  st_25.io.GepAddr <> Gep_arrayidx624.io.Out(0)

  st_28.io.GepAddr <> Gep_arrayidx727.io.Out(0)

  ret_29.io.In.elements("field0") <> st_28.io.Out(0)

  Gep_arrayidx727.io.idx1 <> Loop_0.io.Out(0)

  Gep_arrayidx624.io.idx1 <> Loop_0.io.Out(1)

  call_1_out.io.In("field0") <> InputSplitter.io.Out.data.elements("field0")(0)

  call_1_out.io.In("field1") <> InputSplitter.io.Out.data.elements("field1")(0)

  call_1_out.io.In("field2") <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_arrayidx624.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(2)

  Gep_arrayidx727.io.baseAddress <> InputSplitter.io.Out.data.elements("field2")(3)

  st_25.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_1_in.io.In <> io.call_1_in

  io.call_1_out <> call_1_out.io.Out(0)

//  reattach_2.io.enable <> call_1_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING CALLIN AND CALLOUT INTERFACE            *
   * ================================================================== */

  call_15_in.io.In <> io.call_15_in

  io.call_15_out <> call_15_out.io.Out(0)

//  reattach_16.io.enable <> call_15_in.io.Out.enable



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_29.io.Out

}

class dedupTopIO(implicit val p: Parameters)  extends Module with CoreParams with CacheParams {
  val io = IO( new CoreBundle {
    val in = Flipped(Decoupled(new Call(List(32,32,32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}


class dedupTop(tiles : Int)(implicit p: Parameters) extends dedupTopIO  {

  // Wire up the cache, TM, and modules under test.

  val S2Tiles = 1
  val S3Tiles = tiles // 1,2,4,8
  val S4Tiles = 1
  val dedup = Module(new dedupDF())
  val dedup_S2 = for (i <- 0 until S2Tiles) yield {
    val S2 = Module(new dedup_S2DF())
    S2
  }
  val dedup_S3 = for (i <- 0 until S3Tiles) yield {
    val S3 = Module(new dedup_S3DF())
    S3
  }
  val dedup_S4 = for (i <- 0 until S4Tiles) yield {
    val S4 = Module(new dedup_S4DF())
    S4
  }
  val S2TC = Module(new TaskController(List(32,32,32,32), List(32), 1, S2Tiles))
  val S3TC = Module(new TaskController(List(32,32,32,32), List(32), S2Tiles, S3Tiles))
  val S4TC = Module(new TaskController(List(32,32,32), List(32), 1, S4Tiles))

  // Connect cache interfaces to a cache arbiter
  val MemArbiter = Module(new MemArbiter(1+S2Tiles+S3Tiles+S4Tiles))
  MemArbiter.io.cpu.MemReq(0) <> dedup.io.MemReq
  dedup.io.MemResp <> MemArbiter.io.cpu.MemResp(0)
  for (i <- 0 until S2Tiles) {
    MemArbiter.io.cpu.MemReq(i+1) <> dedup_S2(i).io.MemReq
    dedup_S2(i).io.MemResp <> MemArbiter.io.cpu.MemResp(i+1)
  }
  for (i <- 0 until S3Tiles) {
    MemArbiter.io.cpu.MemReq(i+1+S2Tiles) <> dedup_S3(i).io.MemReq
    dedup_S3(i).io.MemResp <> MemArbiter.io.cpu.MemResp(i+1+S2Tiles)
  }
  for (i <- 0 until S4Tiles) {
    MemArbiter.io.cpu.MemReq(i+1+S2Tiles+S3Tiles) <> dedup_S4(i).io.MemReq
    dedup_S4(i).io.MemResp <> MemArbiter.io.cpu.MemResp(i+1+S2Tiles+S3Tiles)
  }
  io.MemReq <> MemArbiter.io.cache.MemReq
  MemArbiter.io.cache.MemResp <> io.MemResp

  // tester to dedup
  dedup.io.in <> io.in

  // Task Controllers
  S4TC.io.parentIn(0) <> dedup.io.call_1_out
  dedup.io.call_1_in <> S4TC.io.parentOut(0)
  for(i <- 0 until S4Tiles) {
    dedup_S4(i).io.in <> S4TC.io.childOut(i)
    S4TC.io.childIn(i) <> dedup_S4(i).io.out
  }

  S2TC.io.parentIn(0) <> dedup.io.call_15_out
  dedup.io.call_15_in <> S2TC.io.parentOut(0)
  for(i <- 0 until S2Tiles) {
    dedup_S2(i).io.in <> S2TC.io.childOut(i)
    S2TC.io.childIn(i) <> dedup_S2(i).io.out
    S3TC.io.parentIn(i) <> dedup_S2(i).io.call_8_out
    dedup_S2(i).io.call_8_in <> S3TC.io.parentOut(i)
  }
  for(i <- 0 until S3Tiles) {
    dedup_S3(i).io.in <> S3TC.io.childOut(i)
    S3TC.io.childIn(i) <> dedup_S3(i).io.out
  }


  // dedup to tester
  io.out <> dedup.io.out

}

import java.io.{File, FileWriter}
object dedupMain extends App {
  val dir = new File("RTL/dedupTop") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val testParams = p.alterPartial({
    case TLEN => 6
    case TRACE => false
  })
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new dedupTop(1)(testParams)))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
