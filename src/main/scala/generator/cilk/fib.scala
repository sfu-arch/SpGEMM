package dataflow

import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters._
import org.scalatest.{FlatSpec, Matchers}
import muxes._
import config._
import control._
import util._
import interfaces._
import regfile._
import memory._
import stack._
import arbiters._
import loop._
import accel._
import node._
import junctions._


/**
  * This Object should be initialized at the first step
  * It contains all the transformation from indices to their module's name
  */

object Data_fib_FlowParam {

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_if_then_pred = Map(
    "br5" -> 0
  )


  val bb_if_end_pred = Map(
    "br5" -> 0
  )


  val bb_return_pred = Map(
    "br7" -> 0,
    "br18" -> 1
  )


  val br5_brn_bb = Map(
    "bb_if_then" -> 0,
    "bb_if_end" -> 1
  )


  val br7_brn_bb = Map(
    "bb_return" -> 0
  )


  val br18_brn_bb = Map(
    "bb_return" -> 0
  )


  val bb_det_achd_pred = Map(
    "detach9" -> 0
  )


  val bb_det_cont_pred = Map(
    "detach9" -> 0
  )


  val bb_det_achd2_pred = Map(
    "detach13" -> 0
  )


  val bb_det_cont3_pred = Map(
    "detach13" -> 0
  )


  val detach9_brn_bb = Map(
    "bb_det_achd" -> 0,
    "bb_det_cont" -> 1
  )


  val detach13_brn_bb = Map(
    "bb_det_achd2" -> 0,
    "bb_det_cont3" -> 1
  )


  val bb_entry_activate = Map(
    "alloca0" -> 0,
    "alloca1" -> 1,
    "alloca2" -> 2,
    "store3" -> 3,
    "icmp4" -> 4,
    "br5" -> 5
  )


  val bb_if_then_activate = Map(
    "store6" -> 0,
    "br7" -> 1
  )


  val bb_if_end_activate = Map(
    "sub8" -> 0,
    "detach9" -> 1
  )


  val bb_det_achd_activate = Map(
    "call10" -> 0,
    "reattach11" -> 1
  )


  val bb_det_cont_activate = Map(
    "sub12" -> 0,
    "detach13" -> 1
  )


  val bb_det_achd2_activate = Map(
    "call14" -> 0,
    "reattach15" -> 1
  )


  val bb_det_cont3_activate = Map(
    "sync16" -> 0
  )


  val bb_sync_continue_activate = Map(
    "call17" -> 0,
    "br18" -> 1
  )


  val bb_return_activate = Map(
    "ret19" -> 0
  )


  //  %x = alloca i32, align 4, !UID !2, !ScalaLabel !3
  val alloca0_in = Map(

  )


  //  %y = alloca i32, align 4, !UID !4, !ScalaLabel !5
  val alloca1_in = Map(

  )


  //  %rv = alloca i32*, align 4, !UID !6, !ScalaLabel !7
  val alloca2_in = Map(

  )


  //  store i32* %r, i32** %rv, align 4, !UID !8, !ScalaLabel !9
  val store3_in = Map(
    "field1" -> 0,
    "alloca2" -> 0
  )


  //  %cmp = icmp slt i32 %n, 2, !UID !10, !ScalaLabel !11
  val icmp4_in = Map(
    "field0" -> 0
  )


  //  br i1 %cmp, label %if.then, label %if.end, !UID !12, !BB_UID !13, !ScalaLabel !14
  val br5_in = Map(
    "icmp4" -> 0
  )


  //  store i32 %n, i32* %r, align 4, !UID !15, !ScalaLabel !16
  val store6_in = Map(
    "field0" -> 1,
    "field1" -> 1
  )


  //  %sub = sub nsw i32 %n, 1, !UID !20, !ScalaLabel !21
  val sub8_in = Map(
    "field0" -> 2
  )


  //  detach label %det.achd, label %det.cont, !UID !22, !BB_UID !23, !ScalaLabel !24
  val detach9_in = Map(
    "" -> 0,
    "" -> 1
  )


  //  call void @fib(i32 %sub, i32* %x), !UID !25, !ScalaLabel !26
  val call10_in = Map(
    "sub8" -> 0,
    "alloca0" -> 0,
    "" -> 2
  )


  //  reattach label %det.cont, !UID !27, !BB_UID !28, !ScalaLabel !29
  val reattach11_in = Map(
    "" -> 3
  )


  //  %sub1 = sub nsw i32 %n, 2, !UID !30, !ScalaLabel !31
  val sub12_in = Map(
    "field0" -> 3
  )


  //  detach label %det.achd2, label %det.cont3, !UID !32, !BB_UID !33, !ScalaLabel !34
  val detach13_in = Map(
    "" -> 4,
    "" -> 5
  )


  //  call void @fib(i32 %sub1, i32* %y), !UID !35, !ScalaLabel !36
  val call14_in = Map(
    "sub12" -> 0,
    "alloca1" -> 0,
    "" -> 6
  )


  //  reattach label %det.cont3, !UID !37, !BB_UID !38, !ScalaLabel !39
  val reattach15_in = Map(
    "" -> 7
  )


  //  sync label %sync.continue, !UID !40, !BB_UID !41, !ScalaLabel !42
  val sync16_in = Map(
    "" -> 8
  )


  //  call void @fib_continue(i32* %x, i32* %y, i32** %rv), !UID !43, !ScalaLabel !44
  val call17_in = Map(
    "alloca0" -> 1,
    "alloca1" -> 1,
    "alloca2" -> 1,
    "" -> 9
  )


  //  ret void, !UID !48, !BB_UID !49, !ScalaLabel !50
  val ret19_in = Map(

  )


}


/* ================================================================== *
 *                   PRINTING PORTS DEFINITION                        *
 * ================================================================== */


abstract class fibDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val call10_out = Decoupled(new Call(List(32, 32)))
    val call10_in = Flipped(Decoupled(new Call(List(32))))
    val call14_out = Decoupled(new Call(List(32, 32)))
    val call14_in = Flipped(Decoupled(new Call(List(32))))
    val call17_out = Decoupled(new Call(List(32, 32, 32)))
    val call17_in = Flipped(Decoupled(new Call(List(32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}


/* ================================================================== *
 *                   PRINTING MODULE DEFINITION                       *
 * ================================================================== */


class fibDF(implicit p: Parameters) extends fibDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */

  /*
    val StackPointer = Module(new Stack(NumOps = 3))

    val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=2,NWrites=2)
                  (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
                  (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2)))
  */
  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 64 * 1024, NReads = 1, NWrites = 2)
  (WControl = new WriteMemoryController(NumOps = 2, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 1, BaseSize = 2, NumEntries = 1))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  MemCtrl.io.ReadIn(0) <> DontCare
  MemCtrl.io.ReadOut(0) <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(4, 2)))
  InputSplitter.io.In <> io.in


  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  //Function doesn't have any loop


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val bb_if_then = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 1))

  val bb_if_end = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 2))

  val bb_det_achd = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 3))

  val bb_det_cont = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 3, BID = 4))

  val bb_det_achd2 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 5))

  val bb_det_cont3 = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 1, BID = 6))

  val bb_sync_continue = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 7))

  val bb_return = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 8))


  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %x = alloca i32, align 4, !UID !2, !ScalaLabel !3
  //  val alloca0 = Module(new AllocaNode(NumOuts=2, RouteID=0, ID=0))


  //  %y = alloca i32, align 4, !UID !4, !ScalaLabel !5
  //  val alloca1 = Module(new AllocaNode(NumOuts=2, RouteID=1, ID=1))


  //  %rv = alloca i32*, align 4, !UID !6, !ScalaLabel !7
  //  val alloca2 = Module(new AllocaNode(NumOuts=2, RouteID=2, ID=2))


  //  store i32* %r, i32** %rv, align 4, !UID !8, !ScalaLabel !9
  val stackSize = 16 // bytes
  val gep3 = Module(new GepNodeStack(NumOuts = 1, ID = 3)(numByte1 = stackSize))
  val store3 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 3, RouteID = 0))


  //  %cmp = icmp slt i32 %n, 2, !UID !10, !ScalaLabel !11
  val icmp4 = Module(new IcmpNode(NumOuts = 1, ID = 4, opCode = "ULT")(sign = false))


  //  br i1 %cmp, label %if.then, label %if.end, !UID !12, !BB_UID !13, !ScalaLabel !14
  val br5 = Module(new CBranchNode(ID = 5))

  // [BasicBlock]  if.then:

  //  store i32 %n, i32* %r, align 4, !UID !15, !ScalaLabel !16
  val store6 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 6, RouteID = 1))


  //  br label %return, !UID !17, !BB_UID !18, !ScalaLabel !19
  val br7 = Module(new UBranchNode(ID = 7)) // manual

  // [BasicBlock]  if.end:

  //  %sub = sub nsw i32 %n, 1, !UID !20, !ScalaLabel !21
  val sub8 = Module(new ComputeNode(NumOuts = 1, ID = 8, opCode = "sub")(sign = false))


  //  detach label %det.achd, label %det.cont, !UID !22, !BB_UID !23, !ScalaLabel !24
  val detach9 = Module(new Detach(ID = 9))

  // [BasicBlock]  det.achd:

  //  call void @fib(i32 %sub, i32* %x), !UID !25, !ScalaLabel !26
  val gep10 = Module(new GepNodeStack(NumOuts = 1, ID = 10)(numByte1 = stackSize))

  val call10_out = Module(new CallOutNode2(ID = 10, NumSuccOps = 0, argTypes = List(32, 32)))
  val call10_in = Module(new CallInNode(ID = 499, argTypes = List(32)))


  //  reattach label %det.cont, !UID !27, !BB_UID !28, !ScalaLabel !29
  val reattach11 = Module(new Reattach(NumPredOps = 1, ID = 11))

  // [BasicBlock]  det.cont:

  //  %sub1 = sub nsw i32 %n, 2, !UID !30, !ScalaLabel !31
  val sub12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "sub")(sign = false))


  //  detach label %det.achd2, label %det.cont3, !UID !32, !BB_UID !33, !ScalaLabel !34
  val detach13 = Module(new Detach(ID = 13))

  // [BasicBlock]  det.achd2:

  //  call void @fib(i32 %sub1, i32* %y), !UID !35, !ScalaLabel !36
  val gep14 = Module(new GepNodeStack(NumOuts = 1, ID = 14)(numByte1 = stackSize))
  val call14_out = Module(new CallOutNode2(ID = 14, NumSuccOps = 0, argTypes = List(32, 32)))
  val call14_in = Module(new CallInNode(ID = 499, argTypes = List(32)))


  //  reattach label %det.cont3, !UID !37, !BB_UID !38, !ScalaLabel !39
  val reattach15 = Module(new Reattach(NumPredOps = 1, ID = 15))

  // [BasicBlock]  det.cont3:

  //  sync label %sync.continue, !UID !40, !BB_UID !41, !ScalaLabel !42
  val sync16 = Module(new SyncTC2(ID = 16, NumOuts = 1, NumInc = 2, NumDec = 2))

  // [BasicBlock]  sync.continue:

  //  call void @fib_continue(i32* %x, i32* %y, i32** %rv), !UID !43, !ScalaLabel !44
  val gep17a = Module(new GepNodeStack(NumOuts = 1, ID = 17)(numByte1 = stackSize))
  val gep17b = Module(new GepNodeStack(NumOuts = 1, ID = 17)(numByte1 = stackSize))
  val gep17c = Module(new GepNodeStack(NumOuts = 1, ID = 17)(numByte1 = stackSize))

  val call17_out = Module(new CallOutNode(ID = 17, NumSuccOps = 0, argTypes = List(32, 32, 32)))
  val call17_in = Module(new CallInNode(ID = 499, argTypes = List(32)))


  //  br label %return, !UID !45, !BB_UID !46, !ScalaLabel !47
  //  val br18 = Module (new UBranchNode(ID = 18, NumPredOps=1)) // manual

  // [BasicBlock]  return:

  //  ret void, !UID !48, !BB_UID !49, !ScalaLabel !50
  val ret19 = Module(new RetNode(retTypes = List(32), ID = 19))

  val ret20 = Module(new RetNode(retTypes = List(32), ID = 20)) // manual


  val const1 = Module(new ConstFastNode(value = 1, ID = 40))

  val const2 = Module(new ConstFastNode(value = 2, ID = 40))

  val const3 = Module(new ConstFastNode(value = 2, ID = 40))


  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_fib_FlowParam


  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO PREDICATE INSTRUCTIONS*
   * ================================================================== */


  /**
    * Connecting basic blocks to predicate instructions
    */


  bb_entry.io.predicateIn <> InputSplitter.io.Out.enable

  /**
    * Connecting basic blocks to predicate instructions
    */

  //Connecting br5 to bb_if_then
  bb_if_then.io.predicateIn <> br5.io.Out(param.br5_brn_bb("bb_if_then"))


  //Connecting br5 to bb_if_end
  bb_if_end.io.predicateIn <> br5.io.Out(param.br5_brn_bb("bb_if_end"))


  //Connecting br7 to bb_return
  //  bb_return.io.predicateIn(0) <> br7.io.Out(param.br7_brn_bb("bb_return"))
  bb_return.io.predicateIn <> br7.io.Out(param.br7_brn_bb("bb_return")) //manual


  //Connecting br18 to bb_return
  //  bb_return.io.predicateIn(1) <> br18.io.Out(param.br18_brn_bb("bb_return"))
  //bb_return.io.MaskBB(0).ready := true.B  // Manual

  //Connecting detach9 to bb_det_achd
  bb_det_achd.io.predicateIn <> detach9.io.Out(param.detach9_brn_bb("bb_det_achd"))


  //Connecting detach9 to bb_det_cont
  bb_det_cont.io.predicateIn <> detach9.io.Out(param.detach9_brn_bb("bb_det_cont"))


  //Connecting detach13 to bb_det_achd2
  bb_det_achd2.io.predicateIn <> detach13.io.Out(param.detach13_brn_bb("bb_det_achd2"))


  //Connecting detach13 to bb_det_cont3
  bb_det_cont3.io.predicateIn <> detach13.io.Out(param.detach13_brn_bb("bb_det_cont3"))


  //Connecting sync16 to bb_sync_continue
  bb_sync_continue.io.predicateIn <> sync16.io.Out(0)


  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */
  /*
    alloca0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca0"))

    alloca1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca1"))

    alloca2.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca2"))
  */
  store3.io.enable <> bb_entry.io.Out(0)
  gep3.io.enable <> bb_entry.io.Out(3)

  icmp4.io.enable <> bb_entry.io.Out(1)

  const3.io.enable <> bb_entry.io.Out(4)

  br5.io.enable <> bb_entry.io.Out(2)


  store6.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("store6"))

  br7.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("br7"))

  sub8.io.enable <> bb_if_end.io.Out(0)

  const1.io.enable <> bb_if_end.io.Out(1)

  detach9.io.enable <> bb_if_end.io.Out(2)


  call10_out.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("call10"))
  gep10.io.enable <> bb_det_achd.io.Out(1)
  call10_in.io.enable.enq(ControlBundle.active())
  reattach11.io.enable <> call10_in.io.Out.enable //.enq(ControlBundle.active())


  sub12.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("sub12"))
  const2.io.enable <> bb_det_cont.io.Out(2)

  detach13.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("detach13"))


  call14_out.io.enable <> bb_det_achd2.io.Out(param.bb_det_achd2_activate("call14"))
  gep14.io.enable <> bb_det_achd2.io.Out(1)
  call14_in.io.enable.enq(ControlBundle.active())

  reattach15.io.enable.enq(ControlBundle.active())
  reattach15.io.enable <> call14_in.io.Out.enable


  sync16.io.enable <> bb_det_cont3.io.Out(param.bb_det_cont3_activate("sync16"))

  sync16.io.incIn(0) <> detach9.io.Out(2)
  sync16.io.incIn(1) <> detach13.io.Out(2)
  sync16.io.decIn(0) <> reattach11.io.Out(0)
  sync16.io.decIn(1) <> reattach15.io.Out(0)


  call17_out.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("call17"))
  call17_in.io.enable.enq(ControlBundle.active())

  gep17a.io.enable <> bb_sync_continue.io.Out(1)
  gep17b.io.enable <> bb_sync_continue.io.Out(2)
  gep17c.io.enable <> bb_sync_continue.io.Out(3)

  //  br18.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("br18"))
  //  br18.io.PredOp(0) <> call17_in.io.Out.enable
  //  call17_in.io.Out.enable.ready := true.B  // Manual

  ret19.io.enable <> bb_return.io.Out(param.bb_return_activate("ret19"))

  ret20.io.enable <> call17_in.io.Out.enable //bb_sync_continue.io.Out(param.bb_sync_continue_activate("br18"))

  /* ================================================================== *
   *                   CONNECTING LOOPHEADERS                           *
   * ================================================================== */


  //Function doesn't have any for loop


  /* ================================================================== *
   *                   DUMPING PHI NODES                                *
   * ================================================================== */


  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node
  // There is no PHI node


  /* ================================================================== *
   *                   DUMPING DATAFLOW                                 *
   * ================================================================== */


  /**
    * Connecting Dataflow signals
    */
  /*
    // Wiring Alloca instructions with Static inputs
    alloca0.io.allocaInputIO.bits.size      := 1.U
    alloca0.io.allocaInputIO.bits.numByte   := 4.U
    alloca0.io.allocaInputIO.bits.predicate := true.B
    alloca0.io.allocaInputIO.bits.valid     := true.B
    alloca0.io.allocaInputIO.valid          := true.B

    // Connecting Alloca to Stack
    StackPointer.io.InData(0) <> alloca0.io.allocaReqIO
    alloca0.io.allocaRespIO <> StackPointer.io.OutData(0)


    // Wiring Alloca instructions with Static inputs
    alloca1.io.allocaInputIO.bits.size      := 1.U
    alloca1.io.allocaInputIO.bits.numByte   := 4.U
    alloca1.io.allocaInputIO.bits.predicate := true.B
    alloca1.io.allocaInputIO.bits.valid     := true.B
    alloca1.io.allocaInputIO.valid          := true.B

    // Connecting Alloca to Stack
    StackPointer.io.InData(1) <> alloca1.io.allocaReqIO
    alloca1.io.allocaRespIO <> StackPointer.io.OutData(1)


    // Wiring Alloca instructions with Static inputs
    alloca2.io.allocaInputIO.bits.size      := 1.U
    alloca2.io.allocaInputIO.bits.numByte   := 4.U
    alloca2.io.allocaInputIO.bits.predicate := true.B
    alloca2.io.allocaInputIO.bits.valid     := true.B
    alloca2.io.allocaInputIO.valid          := true.B

    // Connecting Alloca to Stack
    StackPointer.io.InData(2) <> alloca2.io.allocaReqIO
    alloca2.io.allocaRespIO <> StackPointer.io.OutData(2)
  */

  // Wiring Store instruction to the function argument
  store3.io.inData <> InputSplitter.io.Out.data.elements("field1")(0)


  // Wiring Store instruction to the parent instruction
  gep3.io.baseAddress.enq(DataBundle.active(8.U))
  store3.io.GepAddr <> gep3.io.Out(0) // <> alloca2.io.Out(param.store3_in("alloca2"))
  store3.io.memResp <> MemCtrl.io.WriteOut(0)
  MemCtrl.io.WriteIn(0) <> store3.io.memReq
  store3.io.Out(0).ready := true.B


  // Wiring Binary instruction to the function argument
  icmp4.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(0)

  // Wiring constant
  icmp4.io.RightIO <> const3.io.Out
  //  icmp4.io.RightIO.bits.data := 2.U
  //  icmp4.io.RightIO.bits.predicate := true.B
  //  icmp4.io.RightIO.valid := true.B

  // Wiring Branch instruction
  br5.io.CmpIO <> icmp4.io.Out(param.br5_in("icmp4"))

  // Wiring Store instruction to the function argument
  store6.io.inData <> InputSplitter.io.Out.data.elements("field0")(1)


  // Wiring Store instruction to the function argument
  store6.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(1)
  store6.io.memResp <> MemCtrl.io.WriteOut(1)
  MemCtrl.io.WriteIn(1) <> store6.io.memReq
  //  store6.io.Out(0).ready := true.B  // Manual


  // Wiring Binary instruction to the function argument
  sub8.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(2)

  // Wiring constant
  sub8.io.RightIO <> const1.io.Out
  //  sub8.io.RightIO.bits.data := 1.U
  //  sub8.io.RightIO.bits.taskID := InputSplitter.io.Out.data.elements("field0")(2).bits.taskID
  //  sub8.io.RightIO.bits.predicate := true.B
  //  sub8.io.RightIO.valid := true.B

  // Wiring Call to I/O
  io.call10_out <> call10_out.io.Out(0)
  call10_in.io.In <> io.call10_in
  //call10_in.io.Out.enable.ready := true.B // Manual fix
  reattach11.io.predicateIn(0) <> call10_in.io.Out.data("field0") // manual


  // Wiring instructions
  call10_out.io.In.elements("field0") <> sub8.io.Out(param.call10_in("sub8"))

  // Wiring instructions
  gep10.io.baseAddress.enq(DataBundle.active(0.U))
  call10_out.io.In.elements("field1") <> gep10.io.Out(0) // <> alloca0.io.Out(param.call10_in("alloca0"))


  // Wiring Binary instruction to the function argument
  sub12.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(3)

  // Wiring constant
  sub12.io.RightIO <> const2.io.Out
  //  sub12.io.RightIO.bits.data := 2.U
  //  sub12.io.RightIO.bits.predicate := true.B
  //  sub12.io.RightIO.valid := true.B

  // Wiring Call to I/O
  io.call14_out <> call14_out.io.Out(0)
  call14_in.io.In <> io.call14_in
  //  call14_in.io.Out.enable.ready := true.B // Manual fix
  reattach15.io.predicateIn(0) <> call14_in.io.Out.data("field0") // manual

  // Wiring instructions
  call14_out.io.In.elements("field0") <> sub12.io.Out(param.call14_in("sub12"))

  // Wiring instructions
  gep14.io.baseAddress.enq(DataBundle.active(4.U))
  call14_out.io.In.elements("field1") <> gep14.io.Out(0) // <> alloca1.io.Out(param.call14_in("alloca1"))


  // Wiring Call to I/O
  io.call17_out <> call17_out.io.Out(0)
  call17_in.io.In <> io.call17_in

  // Wiring instructions
  gep17a.io.baseAddress.enq(DataBundle.active(0.U))
  call17_out.io.In.elements("field0") <> gep17a.io.Out(0) // <> alloca0.io.Out(param.call17_in("alloca0"))

  // Wiring instructions
  gep17b.io.baseAddress.enq(DataBundle.active(4.U))
  call17_out.io.In.elements("field1") <> gep17b.io.Out(0) // <> alloca1.io.Out(param.call17_in("alloca1"))

  // Wiring instructions
  gep17c.io.baseAddress.enq(DataBundle.active(8.U))
  call17_out.io.In.elements("field2") <> gep17c.io.Out(0) // <> alloca2.io.Out(param.call17_in("alloca2"))


  /**
    * Connecting Dataflow signals
    */


  ret19.io.In.elements("field0") <> store6.io.Out(0) // manual

  ret20.io.In.elements("field0") <> call17_in.io.Out.data("field0") // Manual fix

  val retArb = Module(new Arbiter(new Call(List(32)), 2))
  // Drop returns from the non-predicated branch
  retArb.io.in(0).bits := ret19.io.Out.bits
  retArb.io.in(0).valid := ret19.io.Out.valid && ret19.io.Out.bits.enable.control
  ret19.io.Out.ready := retArb.io.in(0).ready
  retArb.io.in(1).bits := ret20.io.Out.bits
  retArb.io.in(1).valid := ret20.io.Out.valid && ret20.io.Out.bits.enable.control
  ret20.io.Out.ready := retArb.io.in(1).ready

  io.out <> retArb.io.out

}

abstract class fibTopIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val out = Decoupled(new Call(List(32)))
  })
}

class fibTop(tiles: Int)(implicit p: Parameters) extends fibTopIO()(p) {
  val NumFibs = tiles
  val fib = for (i <- 0 until NumFibs) yield {
    val fibby = Module(new fibDF())
    fibby
  }
  val fib_continue = for (i <- 0 until NumFibs) yield {
    val fibby_continue = Module(new fib_continueDF())
    fibby_continue
  }
  val TC = Module(new TaskController(List(32, 32), List(32), 1 + (2 * NumFibs), NumFibs))
  val StackArb = Module(new MemArbiter((2 * NumFibs)))
  val Stack = Module(new StackMem((1 << tlen) * 4))


  // Merge the memory interfaces and connect to the stack memory
  for (i <- 0 until NumFibs) {
    // Connect to memory interface
    StackArb.io.cpu.MemReq(2 * i) <> fib(i).io.MemReq
    fib(i).io.MemResp <> StackArb.io.cpu.MemResp(2 * i)
    StackArb.io.cpu.MemReq(2 * i + 1) <> fib_continue(i).io.MemReq
    fib_continue(i).io.MemResp <> StackArb.io.cpu.MemResp(2 * i + 1)

    // Connect fib to continuation
    fib_continue(i).io.in <> fib(i).io.call17_out
    fib(i).io.call17_in <> fib_continue(i).io.out

    // Connect to task controller
    TC.io.parentIn(2 * i) <> fib(i).io.call10_out
    fib(i).io.call10_in <> TC.io.parentOut(2 * i)
    TC.io.parentIn(2 * i + 1) <> fib(i).io.call14_out
    fib(i).io.call14_in <> TC.io.parentOut(2 * i + 1)
    fib(i).io.in <> TC.io.childOut(i)
    TC.io.childIn(i) <> fib(i).io.out
  }

  Stack.io.req <> StackArb.io.cache.MemReq
  StackArb.io.cache.MemResp <> Stack.io.resp
  TC.io.parentIn(2 * NumFibs) <> io.in
  io.out <> TC.io.parentOut(2 * NumFibs)
}

import java.io.{File, FileWriter}

object fibMain extends App {
  val dir = new File("RTL/fibTop");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val testParams = p.alterPartial({
    case TLEN => 11
    case TRACE => false
  })
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new fibTop(4)(testParams)))

  val verilogFile = new File(dir, s"/${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

