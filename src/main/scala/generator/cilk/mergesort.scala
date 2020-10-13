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

object Data_mergesort_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_if_then_pred = Map(
    "br11" -> 0
  )


  val bb_if_end_pred = Map(
    "br11" -> 0
  )


  val bb_return_pred = Map(
    "br12" -> 0,
    "br25" -> 1
  )


  val br11_brn_bb = Map(
    "bb_if_then" -> 0,
    "bb_if_end" -> 1
  )


  val br12_brn_bb = Map(
    "bb_return" -> 0
  )


  val br25_brn_bb = Map(
    "bb_return" -> 0
  )


  val bb_det_achd_pred = Map(
    "detach17" -> 0
  )


  val bb_det_cont_pred = Map(
    "detach17" -> 0
  )


  val bb_det_achd6_pred = Map(
    "detach20" -> 0
  )


  val bb_det_cont7_pred = Map(
    "detach20" -> 0
  )


  val detach17_brn_bb = Map(
    "bb_det_achd" -> 0,
    "bb_det_cont" -> 1
  )


  val detach20_brn_bb = Map(
    "bb_det_achd6" -> 0,
    "bb_det_cont7" -> 1
  )


  val bb_entry_activate = Map(
    "alloca0" -> 0,
    "getelementptr1" -> 1,
    "store2" -> 2,
    "getelementptr3" -> 3,
    "store4" -> 4,
    "getelementptr5" -> 5,
    "store6" -> 6,
    "getelementptr7" -> 7,
    "store8" -> 8,
    "sub9" -> 9,
    "icmp10" -> 10,
    "br11" -> 11
  )


  val bb_if_then_activate = Map(
    "br12" -> 0
  )


  val bb_if_end_activate = Map(
    "add13" -> 0,
    "udiv14" -> 1,
    "getelementptr15" -> 2,
    "store16" -> 3,
    "detach17" -> 4
  )


  val bb_det_achd_activate = Map(
    "call18" -> 0,
    "reattach19" -> 1
  )


  val bb_det_cont_activate = Map(
    "detach20" -> 0
  )


  val bb_det_achd6_activate = Map(
    "call21" -> 0,
    "reattach22" -> 1
  )


  val bb_det_cont7_activate = Map(
    "sync23" -> 0
  )


  val bb_sync_continue_activate = Map(
    "call24" -> 0,
    "br25" -> 1
  )


  val bb_return_activate = Map(
    "ret26" -> 0
  )


  //  %p = alloca %struct.continue_struct, align 4, !UID !2, !ScalaLabel !3
  val alloca0_in = Map(

  )


  //  %A1 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 0, !UID !4, !ScalaLabel !5
  val getelementptr1_in = Map(
    "alloca0" -> 0
  )


  //  store i32* %B, i32** %A1, align 4, !UID !6, !ScalaLabel !7
  val store2_in = Map(
    "field0" -> 0,
    "getelementptr1" -> 0
  )


  //  %B2 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 1, !UID !8, !ScalaLabel !9
  val getelementptr3_in = Map(
    "alloca0" -> 1
  )


  //  store i32* %A, i32** %B2, align 4, !UID !10, !ScalaLabel !11
  val store4_in = Map(
    "field3" -> 0,
    "getelementptr3" -> 0
  )


  //  %iBegin3 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !12, !ScalaLabel !13
  val getelementptr5_in = Map(
    "alloca0" -> 2
  )


  //  store i32 %iBegin, i32* %iBegin3, align 4, !UID !14, !ScalaLabel !15
  val store6_in = Map(
    "field1" -> 0,
    "getelementptr5" -> 0
  )


  //  %iEnd4 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 4, !UID !16, !ScalaLabel !17
  val getelementptr7_in = Map(
    "alloca0" -> 3
  )


  //  store i32 %iEnd, i32* %iEnd4, align 4, !UID !18, !ScalaLabel !19
  val store8_in = Map(
    "field2" -> 0,
    "getelementptr7" -> 0
  )


  //  %sub = sub i32 %iEnd, %iBegin, !UID !20, !ScalaLabel !21
  val sub9_in = Map(
    "field2" -> 1,
    "field1" -> 1
  )


  //  %cmp = icmp ult i32 %sub, 2, !UID !22, !ScalaLabel !23
  val icmp10_in = Map(
    "sub9" -> 0
  )


  //  br i1 %cmp, label %if.then, label %if.end, !UID !24, !BB_UID !25, !ScalaLabel !26
  val br11_in = Map(
    "icmp10" -> 0
  )


  //  %add = add i32 %iEnd, %iBegin, !UID !30, !ScalaLabel !31
  val add13_in = Map(
    "field2" -> 2,
    "field1" -> 2
  )


  //  %div = udiv i32 %add, 2, !UID !32, !ScalaLabel !33
  val udiv14_in = Map(
    "add13" -> 0
  )


  //  %iMiddle5 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !34, !ScalaLabel !35
  val getelementptr15_in = Map(
    "alloca0" -> 4
  )


  //  store i32 %div, i32* %iMiddle5, align 4, !UID !36, !ScalaLabel !37
  val store16_in = Map(
    "udiv14" -> 0,
    "getelementptr15" -> 0
  )


  //  detach label %det.achd, label %det.cont, !UID !38, !BB_UID !39, !ScalaLabel !40
  val detach17_in = Map(
    "" -> 0,
    "" -> 1
  )


  //  call void @mergesort(i32* %A, i32 %iBegin, i32 %div, i32* %B), !UID !41, !ScalaLabel !42
  val call18_in = Map(
    "field3" -> 1,
    "field1" -> 3,
    "udiv14" -> 1,
    "field0" -> 1,
    "" -> 2
  )


  //  reattach label %det.cont, !UID !43, !BB_UID !44, !ScalaLabel !45
  val reattach19_in = Map(
    "" -> 3
  )


  //  detach label %det.achd6, label %det.cont7, !UID !46, !BB_UID !47, !ScalaLabel !48
  val detach20_in = Map(
    "" -> 4,
    "" -> 5
  )


  //  call void @mergesort(i32* %A, i32 %div, i32 %iEnd, i32* %B), !UID !49, !ScalaLabel !50
  val call21_in = Map(
    "field3" -> 2,
    "udiv14" -> 2,
    "field2" -> 3,
    "field0" -> 2,
    "" -> 6
  )


  //  reattach label %det.cont7, !UID !51, !BB_UID !52, !ScalaLabel !53
  val reattach22_in = Map(
    "" -> 7
  )


  //  sync label %sync.continue, !UID !54, !BB_UID !55, !ScalaLabel !56
  val sync23_in = Map(
    "" -> 8
  )


  //  call void @mergesort_merge(%struct.continue_struct* %p), !UID !57, !ScalaLabel !58
  val call24_in = Map(
    "alloca0" -> 5,
    "" -> 9
  )


  //  ret void, !UID !62, !BB_UID !63, !ScalaLabel !64
  val ret26_in = Map(

  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class mergesortDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32,32,32,32))))
    val call18_out = Decoupled(new Call(List(32,32,32,32)))
    val call18_in = Flipped(Decoupled(new Call(List(32))))
    val call21_out = Decoupled(new Call(List(32,32,32,32)))
    val call21_in = Flipped(Decoupled(new Call(List(32))))
    val call24_out = Decoupled(new Call(List(32)))
    val call24_in = Flipped(Decoupled(new Call(List(32))))
    val StackResp = Flipped(Valid(new MemResp))
    val StackReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class mergesortDF(implicit p: Parameters) extends mergesortDFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */

/*
  val StackPointer = Module(new Stack(NumOps = 1))

  val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=2,NWrites=5)
                (WControl=new WriteMemoryController(NumOps=5,BaseSize=2,NumEntries=2))
                (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2)))
*/
  val StackCtrl = Module(new UnifiedController(ID=0,Size=64*1024,NReads=2,NWrites=5)
                (WControl=new WriteMemoryController(NumOps=5,BaseSize=2,NumEntries=2))
                (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
                (RWArbiter=new ReadWriteArbiter()))

  io.StackReq <> StackCtrl.io.MemReq
  StackCtrl.io.MemResp <> io.StackResp

  val InputSplitter = Module(new SplitCallNew(List(3,4,4,3)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  //Function doesn't have any loop


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 11, BID = 0))

  val bb_if_then = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 1))

  val bb_if_end = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 2))

  val bb_det_achd = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 3))

  val bb_det_cont = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 1, BID = 4))

  val bb_det_achd6 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 5))

  val bb_det_cont7 = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 1, BID = 6))

  val bb_sync_continue = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 7))

  val bb_return = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 8))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %p = alloca %struct.continue_struct, align 4, !UID !2, !ScalaLabel !3
//  val alloca0 = Module(new AllocaNode(NumOuts=6, RouteID=0, ID=0))


  //  %A1 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 0, !UID !4, !ScalaLabel !5
  val getelementptr1 = Module (new GepNodeStack(NumOuts = 1, ID = 1)(numByte1 = 20))


  //  store i32* %B, i32** %A1, align 4, !UID !6, !ScalaLabel !7
  val store2 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=2,RouteID=0))


  //  %B2 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 1, !UID !8, !ScalaLabel !9
  val getelementptr3 = Module (new GepNodeStack(NumOuts = 1, ID = 3)(numByte1 = 20))


  //  store i32* %A, i32** %B2, align 4, !UID !10, !ScalaLabel !11
  val store4 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=4,RouteID=1))


  //  %iBegin3 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !12, !ScalaLabel !13
  val getelementptr5 = Module (new GepNodeStack(NumOuts = 1, ID = 5)(numByte1 = 20))


  //  store i32 %iBegin, i32* %iBegin3, align 4, !UID !14, !ScalaLabel !15
  val store6 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=6,RouteID=2))


  //  %iEnd4 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 4, !UID !16, !ScalaLabel !17
  val getelementptr7 = Module (new GepNodeStack(NumOuts = 1, ID = 7)(numByte1 = 20))


  //  store i32 %iEnd, i32* %iEnd4, align 4, !UID !18, !ScalaLabel !19
  val store8 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=8,RouteID=3))


  //  %sub = sub i32 %iEnd, %iBegin, !UID !20, !ScalaLabel !21
  val sub9 = Module (new ComputeNode(NumOuts = 1, ID = 9, opCode = "sub")(sign=false))


  //  %cmp = icmp ult i32 %sub, 2, !UID !22, !ScalaLabel !23
  val icmp10 = Module (new IcmpNode(NumOuts = 1, ID = 10, opCode = "ULT")(sign=false))


  //  br i1 %cmp, label %if.then, label %if.end, !UID !24, !BB_UID !25, !ScalaLabel !26
  val br11 = Module (new CBranchNode(ID = 11))

  // [BasicBlock]  if.then:

  //  br label %return, !UID !27, !BB_UID !28, !ScalaLabel !29
  val br12 = Module (new UBranchNode(ID = 12))

  // [BasicBlock]  if.end:

  //  %add = add i32 %iEnd, %iBegin, !UID !30, !ScalaLabel !31
  val add13 = Module (new ComputeNode(NumOuts = 1, ID = 13, opCode = "add")(sign=false))


  //  %div = udiv i32 %add, 2, !UID !32, !ScalaLabel !33
  val udiv14 = Module (new ComputeNode(NumOuts = 3, ID = 14, opCode = "lshr")(sign=false))


  //  %iMiddle5 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !34, !ScalaLabel !35
  val getelementptr15 = Module (new GepNodeStack(NumOuts = 1, ID = 15)(numByte1 = 20))


  //  store i32 %div, i32* %iMiddle5, align 4, !UID !36, !ScalaLabel !37
  val store16 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=16,RouteID=4))


  //  detach label %det.achd, label %det.cont, !UID !38, !BB_UID !39, !ScalaLabel !40
  val detach17 = Module(new Detach(ID = 17))

  // [BasicBlock]  det.achd:

  //  call void @mergesort(i32* %A, i32 %iBegin, i32 %div, i32* %B), !UID !41, !ScalaLabel !42
  val call18_out = Module(new CallOutNode(ID=18,NumSuccOps=0,argTypes=List(32,32,32,32)))
  val call18_in = Module(new CallInNode(ID=499, argTypes=List(32)))



  //  reattach label %det.cont, !UID !43, !BB_UID !44, !ScalaLabel !45
  val reattach19 = Module(new Reattach(NumPredOps=1, ID=19))

  // [BasicBlock]  det.cont:

  //  detach label %det.achd6, label %det.cont7, !UID !46, !BB_UID !47, !ScalaLabel !48
  val detach20 = Module(new Detach(ID = 20))

  // [BasicBlock]  det.achd6:

  //  call void @mergesort(i32* %A, i32 %div, i32 %iEnd, i32* %B), !UID !49, !ScalaLabel !50
  val call21_out = Module(new CallOutNode(ID=21,NumSuccOps=0,argTypes=List(32,32,32,32)))
  val call21_in = Module(new CallInNode(ID=499, argTypes=List(32)))



  //  reattach label %det.cont7, !UID !51, !BB_UID !52, !ScalaLabel !53
  val reattach22 = Module(new Reattach(NumPredOps=1, ID=22))

  // [BasicBlock]  det.cont7:

  //  sync label %sync.continue, !UID !54, !BB_UID !55, !ScalaLabel !56
  val sync23 = Module(new SyncTC2(ID = 23, NumOuts = 1, NumInc = 2, NumDec = 2))

  // [BasicBlock]  sync.continue:

  //  call void @mergesort_merge(%struct.continue_struct* %p), !UID !57, !ScalaLabel !58
  val gep24   = Module (new GepNodeStack(NumOuts = 1, ID = 3)(numByte1 = 20))
  val call24_out = Module(new CallOutNode(ID=24,NumSuccOps=0,argTypes=List(32)))
  val call24_in = Module(new CallInNode(ID=499, argTypes=List(32)))



  //  br label %return, !UID !59, !BB_UID !60, !ScalaLabel !61
//  val br25 = Module (new UBranchNode(ID = 25))

  // [BasicBlock]  return:

  //  ret void, !UID !62, !BB_UID !63, !ScalaLabel !64
  val ret26 = Module(new RetNode(retTypes=List(32), ID=26))
  val ret27 = Module(new RetNode(retTypes=List(32), ID=27))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_mergesort_FlowParam



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

  //Connecting br11 to bb_if_then
  bb_if_then.io.predicateIn <> br11.io.Out(param.br11_brn_bb("bb_if_then"))


  //Connecting br11 to bb_if_end
  bb_if_end.io.predicateIn <> br11.io.Out(param.br11_brn_bb("bb_if_end"))


  //Connecting br12 to bb_return
  bb_return.io.predicateIn <> br12.io.Out(param.br12_brn_bb("bb_return"))


  //Connecting br25 to bb_return
//  bb_return.io.predicateIn <> br25.io.Out(param.br25_brn_bb("bb_return"))


  //Connecting detach17 to bb_det_achd
  bb_det_achd.io.predicateIn <> detach17.io.Out(param.detach17_brn_bb("bb_det_achd"))


  //Connecting detach17 to bb_det_cont
  bb_det_cont.io.predicateIn <> detach17.io.Out(param.detach17_brn_bb("bb_det_cont"))


  //Connecting detach20 to bb_det_achd6
  bb_det_achd6.io.predicateIn <> detach20.io.Out(param.detach20_brn_bb("bb_det_achd6"))


  //Connecting detach20 to bb_det_cont7
  bb_det_cont7.io.predicateIn <> detach20.io.Out(param.detach20_brn_bb("bb_det_cont7"))


  //Connecting sync23 to bb_sync_continue
  bb_sync_continue.io.predicateIn <> sync23.io.Out(0)




  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

//  alloca0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca0"))

  getelementptr1.io.enable <> bb_entry.io.Out(0)

  store2.io.enable <> bb_entry.io.Out(1)

  getelementptr3.io.enable <> bb_entry.io.Out(2)

  store4.io.enable <> bb_entry.io.Out(3)

  getelementptr5.io.enable <> bb_entry.io.Out(4)

  store6.io.enable <> bb_entry.io.Out(5)

  getelementptr7.io.enable <> bb_entry.io.Out(6)

  store8.io.enable <> bb_entry.io.Out(7)

  sub9.io.enable <> bb_entry.io.Out(8)

  icmp10.io.enable <> bb_entry.io.Out(9)

  br11.io.enable <> bb_entry.io.Out(10)



  br12.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("br12"))



  add13.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("add13"))

  udiv14.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("udiv14"))

  getelementptr15.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("getelementptr15"))

  store16.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("store16"))

  detach17.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("detach17"))



  call18_out.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("call18"))
  call18_in.io.enable.enq(ControlBundle.active())


  detach20.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("detach20"))



  call21_out.io.enable <> bb_det_achd6.io.Out(param.bb_det_achd6_activate("call21"))
  call21_in.io.enable.enq(ControlBundle.active())



  sync23.io.enable <> bb_det_cont7.io.Out(param.bb_det_cont7_activate("sync23"))

  sync23.io.incIn(0) <> detach17.io.Out(2)
  sync23.io.incIn(1) <> detach20.io.Out(2)
  sync23.io.decIn(0) <> reattach19.io.Out(0)
  sync23.io.decIn(1) <> reattach22.io.Out(0)


  gep24.io.enable <> bb_sync_continue.io.Out(0)
  call24_out.io.enable <> bb_sync_continue.io.Out(1)
  call24_in.io.enable.enq(ControlBundle.active())




  ret26.io.enable <> bb_return.io.Out(param.bb_return_activate("ret26"))


  ret27.io.enable <> call24_in.io.Out.enable



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
  alloca0.io.allocaInputIO.bits.numByte   := 20.U
  alloca0.io.allocaInputIO.bits.predicate := true.B
  alloca0.io.allocaInputIO.bits.valid     := true.B
  alloca0.io.allocaInputIO.valid          := true.B

  // Connecting Alloca to Stack
  StackPointer.io.InData(0) <> alloca0.io.allocaReqIO
  alloca0.io.allocaRespIO <> StackPointer.io.OutData(0)
*/

  // Wiring GEP instruction to the parent instruction
  getelementptr1.io.baseAddress.enq(DataBundle.active(0.U))// <> alloca0.io.Out(param.getelementptr1_in("alloca0"))

/*
  // Wiring GEP instruction to the Constant
  getelementptr1.io.idx1.valid :=  true.B
  getelementptr1.io.idx1.bits.predicate :=  true.B
  getelementptr1.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr1.io.idx2.valid :=  true.B
  getelementptr1.io.idx2.bits.predicate :=  true.B
  getelementptr1.io.idx2.bits.data :=  0.U
*/

  // Wiring Store instruction to the function argument
  store2.io.inData <>  InputSplitter.io.Out.data.elements("field0")(0)



  // Wiring Store instruction to the parent instruction
  store2.io.GepAddr <> getelementptr1.io.Out(param.store2_in("getelementptr1"))
  store2.io.memResp  <> StackCtrl.io.WriteOut(0)
  StackCtrl.io.WriteIn(0) <> store2.io.memReq
  store2.io.Out(0).ready := true.B



  // Wiring GEP instruction to the parent instruction
  getelementptr3.io.baseAddress .enq(DataBundle.active(4.U))//<> alloca0.io.Out(param.getelementptr3_in("alloca0"))

/*
  // Wiring GEP instruction to the Constant
  getelementptr3.io.idx1.valid :=  true.B
  getelementptr3.io.idx1.bits.predicate :=  true.B
  getelementptr3.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr3.io.idx2.valid :=  true.B
  getelementptr3.io.idx2.bits.predicate :=  true.B
  getelementptr3.io.idx2.bits.data :=  1.U
*/

  // Wiring Store instruction to the function argument
  store4.io.inData <>  InputSplitter.io.Out.data.elements("field3")(0)



  // Wiring Store instruction to the parent instruction
  store4.io.GepAddr <> getelementptr3.io.Out(param.store4_in("getelementptr3"))
  store4.io.memResp  <> StackCtrl.io.WriteOut(1)
  StackCtrl.io.WriteIn(1) <> store4.io.memReq
  store4.io.Out(0).ready := true.B



  // Wiring GEP instruction to the parent instruction
  getelementptr5.io.baseAddress .enq(DataBundle.active(8.U))//<> alloca0.io.Out(param.getelementptr5_in("alloca0"))

/*
  // Wiring GEP instruction to the Constant
  getelementptr5.io.idx1.valid :=  true.B
  getelementptr5.io.idx1.bits.predicate :=  true.B
  getelementptr5.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr5.io.idx2.valid :=  true.B
  getelementptr5.io.idx2.bits.predicate :=  true.B
  getelementptr5.io.idx2.bits.data :=  2.U
*/

  // Wiring Store instruction to the function argument
  store6.io.inData <>  InputSplitter.io.Out.data.elements("field1")(0)



  // Wiring Store instruction to the parent instruction
  store6.io.GepAddr <> getelementptr5.io.Out(param.store6_in("getelementptr5"))
  store6.io.memResp  <> StackCtrl.io.WriteOut(2)
  StackCtrl.io.WriteIn(2) <> store6.io.memReq
  store6.io.Out(0).ready := true.B



  // Wiring GEP instruction to the parent instruction
  getelementptr7.io.baseAddress .enq(DataBundle.active(16.U))//<> alloca0.io.Out(param.getelementptr7_in("alloca0"))

/*
  // Wiring GEP instruction to the Constant
  getelementptr7.io.idx1.valid :=  true.B
  getelementptr7.io.idx1.bits.predicate :=  true.B
  getelementptr7.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr7.io.idx2.valid :=  true.B
  getelementptr7.io.idx2.bits.predicate :=  true.B
  getelementptr7.io.idx2.bits.data :=  4.U
*/

  // Wiring Store instruction to the function argument
  store8.io.inData <>  InputSplitter.io.Out.data.elements("field2")(0)



  // Wiring Store instruction to the parent instruction
  store8.io.GepAddr <> getelementptr7.io.Out(param.store8_in("getelementptr7"))
  store8.io.memResp  <> StackCtrl.io.WriteOut(3)
  StackCtrl.io.WriteIn(3) <> store8.io.memReq
  store8.io.Out(0).ready := true.B



  // Wiring Binary instruction to the function argument
  sub9.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(1)

  // Wiring Binary instruction to the function argument
  sub9.io.RightIO <> InputSplitter.io.Out.data.elements("field1")(1)

  // Wiring instructions
  icmp10.io.LeftIO <> sub9.io.Out(param.icmp10_in("sub9"))

  // Wiring constant
  icmp10.io.RightIO.bits.data := 2.U
  icmp10.io.RightIO.bits.predicate := true.B
  icmp10.io.RightIO.valid := true.B

  // Wiring Branch instruction
  br11.io.CmpIO <> icmp10.io.Out(param.br11_in("icmp10"))

  // Wiring Binary instruction to the function argument
  add13.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(2)

  // Wiring Binary instruction to the function argument
  add13.io.RightIO <> InputSplitter.io.Out.data.elements("field1")(2)

  // Wiring instructions
  udiv14.io.LeftIO <> add13.io.Out(param.udiv14_in("add13"))

  // Wiring constant
  udiv14.io.RightIO.bits.data := 1.U
  udiv14.io.RightIO.bits.predicate := true.B
  udiv14.io.RightIO.valid := true.B

  // Wiring GEP instruction to the parent instruction
  getelementptr15.io.baseAddress .enq(DataBundle.active(12.U))//<> alloca0.io.Out(param.getelementptr15_in("alloca0"))

/*
  // Wiring GEP instruction to the Constant
  getelementptr15.io.idx1.valid :=  true.B
  getelementptr15.io.idx1.bits.predicate :=  true.B
  getelementptr15.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr15.io.idx2.valid :=  true.B
  getelementptr15.io.idx2.bits.predicate :=  true.B
  getelementptr15.io.idx2.bits.data :=  3.U
*/

  store16.io.inData <> udiv14.io.Out(param.store16_in("udiv14"))



  // Wiring Store instruction to the parent instruction
  store16.io.GepAddr <> getelementptr15.io.Out(param.store16_in("getelementptr15"))
  store16.io.memResp  <> StackCtrl.io.WriteOut(4)
  StackCtrl.io.WriteIn(4) <> store16.io.memReq
  store16.io.Out(0).ready := true.B



  // Wiring Call to I/O
  io.call18_out <> call18_out.io.Out(0)
  call18_in.io.In <> io.call18_in
  reattach19.io.enable <> call18_in.io.Out.enable// call18_in.io.enable.enq(ControlBundle.active())
  reattach19.io.predicateIn(0) <> call18_in.io.Out.data.elements("field0") // manual

  // Wiring Call to the function argument
  call18_out.io.In.elements("field0") <> InputSplitter.io.Out.data.elements("field3")(1)   // B[]

  // Wiring Call to the function argument
  call18_out.io.In.elements("field1") <> InputSplitter.io.Out.data.elements("field1")(3)   // iBegin

  // Wiring instructions
  call18_out.io.In.elements("field2") <> udiv14.io.Out(param.call18_in("udiv14")) // iEnd

  // Wiring Call to the function argument
  call18_out.io.In.elements("field3") <> InputSplitter.io.Out.data.elements("field0")(1)   // A[]



  // Wiring Call to I/O
  io.call21_out <> call21_out.io.Out(0)
  call21_in.io.In <> io.call21_in
  //call21_in.io.Out.enable.ready := true.B // Manual fix
  reattach22.io.enable <> call21_in.io.Out.enable// call21_in.io.enable.enq(ControlBundle.active())
  reattach22.io.predicateIn(0) <> call21_in.io.Out.data.elements("field0") // manual

  // Wiring Call to the function argument
  call21_out.io.In.elements("field0") <> InputSplitter.io.Out.data.elements("field3")(2)     // B[]

  // Wiring instructions
  call21_out.io.In.elements("field1") <> udiv14.io.Out(param.call21_in("udiv14"))   // iBegin

  // Wiring Call to the function argument
  call21_out.io.In.elements("field2") <> InputSplitter.io.Out.data.elements("field2")(3)     // iEnd

  // Wiring Call to the function argument
  call21_out.io.In.elements("field3") <> InputSplitter.io.Out.data.elements("field0")(2)     // A[]



  // Wiring Call to I/O
  io.call24_out <> call24_out.io.Out(0)
  call24_in.io.In <> io.call24_in
//  call24_in.io.Out.enable.ready := true.B // Manual fix

  // Wiring instructions
  gep24.io.baseAddress.enq(DataBundle.active(0.U))
  call24_out.io.In.elements("field0") <> gep24.io.Out(0)//alloca0.io.Out(param.call24_in("alloca0"))



  /**
    * Connecting Dataflow signals
    */
  ret26.io.In.elements("field0").bits.data := 1.U
  ret26.io.In.elements("field0").bits.predicate := true.B
  ret26.io.In.elements("field0").valid := true.B

  ret27.io.In.elements("field0") <> call24_in.io.Out.data.elements("field0")

  val retArb = Module(new Arbiter(new Call(List(32)),2))
  // Drop returns from the non-predicated branch
  retArb.io.in(0).bits := ret26.io.Out.bits
  retArb.io.in(0).valid := ret26.io.Out.valid && ret26.io.Out.bits.enable.control
  ret26.io.Out.ready := retArb.io.in(0).ready
  retArb.io.in(1).bits := ret27.io.Out.bits
  retArb.io.in(1).valid := ret27.io.Out.valid && ret27.io.Out.bits.enable.control
  ret27.io.Out.ready := retArb.io.in(1).ready

  io.out <> retArb.io.out

}
class mergesortTopIO(implicit val p: Parameters)  extends Module with CoreParams with CacheParams {
  val io = IO( new CoreBundle {
    val in = Flipped(Decoupled(new Call(List(32,32,32,32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}


class mergesortTop(tiles : Int)(implicit p: Parameters) extends mergesortTopIO {

  val NumMergesorts = tiles
  val mergesort = for (i <- 0 until NumMergesorts) yield {
    val mergesortby = Module(new mergesortDF())
    mergesortby
  }
  val mergesort_merge = for (i <- 0 until NumMergesorts) yield {
    val mergesortby_continue = Module(new mergesort_mergeDF())
    mergesortby_continue
  }
  val TC = Module(new TaskController(List(32,32,32,32), List(32), 1+(2*NumMergesorts), NumMergesorts))
  val CacheArb = Module(new MemArbiter(NumMergesorts+1))
  val StackArb = Module(new MemArbiter(2*NumMergesorts))
  val Stack = Module(new StackMem((1 << tlen)*4))


  // Merge the memory interfaces and connect to the stack memory
  for (i <- 0 until NumMergesorts) {
    // Connect to stack memory interface
    StackArb.io.cpu.MemReq(2*i) <> mergesort(i).io.StackReq
    mergesort(i).io.StackResp <> StackArb.io.cpu.MemResp(2*i)
    StackArb.io.cpu.MemReq(2*i+1) <> mergesort_merge(i).io.StackReq
    mergesort_merge(i).io.StackResp <> StackArb.io.cpu.MemResp(2*i+1)

    // Connect to cache memory
    CacheArb.io.cpu.MemReq(i) <> mergesort_merge(i).io.GlblReq
    mergesort_merge(i).io.GlblResp <> CacheArb.io.cpu.MemResp(i)

    // Connect mergesort to continuation
    mergesort_merge(i).io.in <> mergesort(i).io.call24_out
    mergesort(i).io.call24_in <> mergesort_merge(i).io.out

    // Connect to task controller
    TC.io.parentIn(2*i) <> mergesort(i).io.call18_out
    mergesort(i).io.call18_in <> TC.io.parentOut(2*i)
    TC.io.parentIn(2*i+1) <> mergesort(i).io.call21_out
    mergesort(i).io.call21_in <> TC.io.parentOut(2*i+1)
    mergesort(i).io.in <> TC.io.childOut(i)
    TC.io.childIn(i) <> mergesort(i).io.out
  }

  io.MemReq <> CacheArb.io.cache.MemReq
  CacheArb.io.cache.MemResp <> io.MemResp

  Stack.io.req <> StackArb.io.cache.MemReq
  StackArb.io.cache.MemResp <> Stack.io.resp
  TC.io.parentIn(2*NumMergesorts) <> io.in
  io.out <> TC.io.parentOut(2*NumMergesorts)

}

import java.io.{File, FileWriter}
object mergesortMain extends App {
  val dir = new File("RTL/mergesortTop") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val testParams = p.alterPartial({
    case TLEN => 11
    case TRACE => false
  })
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new mergesortTop(4)(testParams)))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

