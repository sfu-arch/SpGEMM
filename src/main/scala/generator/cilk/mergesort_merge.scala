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

object Data_mergesort_merge_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_if_end_pred = Map(
    "br35" -> 0,
    "br41" -> 1
  )


  val bb_for_cond_pred = Map(
    "br14" -> 0,
    "br46" -> 1
  )


  val bb_for_inc_pred = Map(
    "br44" -> 0
  )


  val bb_for_body_pred = Map(
    "br19" -> 0
  )


  val bb_for_end_pred = Map(
    "br19" -> 0
  )


  val bb_land_lhs_true_pred = Map(
    "br21" -> 0
  )


  val bb_if_else_pred = Map(
    "br21" -> 0,
    "br29" -> 1
  )


  val bb_if_then_pred = Map(
    "br23" -> 0,
    "br29" -> 1
  )


  val bb_lor_lhs_false_pred = Map(
    "br23" -> 0
  )


  val br14_brn_bb = Map(
    "bb_for_cond" -> 0
  )


  val br19_brn_bb = Map(
    "bb_for_body" -> 0,
    "bb_for_end" -> 1
  )


  val br21_brn_bb = Map(
    "bb_land_lhs_true" -> 0,
    "bb_if_else" -> 1
  )


  val br23_brn_bb = Map(
    "bb_if_then" -> 0,
    "bb_lor_lhs_false" -> 1
  )


  val br29_brn_bb = Map(
    "bb_if_then" -> 0,
    "bb_if_else" -> 1
  )


  val br35_brn_bb = Map(
    "bb_if_end" -> 0
  )


  val br41_brn_bb = Map(
    "bb_if_end" -> 0
  )


  val br44_brn_bb = Map(
    "bb_for_inc" -> 0
  )


  val br46_brn_bb = Map(
    "bb_for_cond" -> 0
  )


  val bb_entry_activate = Map(
    "getelementptr0" -> 0,
    "load1" -> 1,
    "getelementptr2" -> 2,
    "load3" -> 3,
    "getelementptr4" -> 4,
    "load5" -> 5,
    "getelementptr6" -> 6,
    "load7" -> 7,
    "getelementptr8" -> 8,
    "load9" -> 9,
    "getelementptr10" -> 10,
    "load11" -> 11,
    "getelementptr12" -> 12,
    "load13" -> 13,
    "br14" -> 14
  )


  val bb_for_cond_activate = Map(
    "phi15" -> 0,
    "phi16" -> 1,
    "phi17" -> 2,
    "icmp18" -> 3,
    "br19" -> 4
  )


  val bb_for_body_activate = Map(
    "icmp20" -> 0,
    "br21" -> 1
  )


  val bb_land_lhs_true_activate = Map(
    "icmp22" -> 0,
    "br23" -> 1
  )


  val bb_lor_lhs_false_activate = Map(
    "getelementptr24" -> 0,
    "load25" -> 1,
    "getelementptr26" -> 2,
    "load27" -> 3,
    "icmp28" -> 4,
    "br29" -> 5
  )


  val bb_if_then_activate = Map(
    "getelementptr30" -> 0,
    "load31" -> 1,
    "getelementptr32" -> 2,
    "store33" -> 3,
    "add34" -> 4,
    "br35" -> 5
  )


  val bb_if_else_activate = Map(
    "getelementptr36" -> 0,
    "load37" -> 1,
    "getelementptr38" -> 2,
    "store39" -> 3,
    "add40" -> 4,
    "br41" -> 5
  )


  val bb_if_end_activate = Map(
    "phi42" -> 0,
    "phi43" -> 1,
    "br44" -> 2
  )


  val bb_for_inc_activate = Map(
    "add45" -> 0,
    "br46" -> 1
  )


  val bb_for_end_activate = Map(
    "ret47" -> 0
  )


  val phi15_phi_in = Map(
    "load11" -> 0,
    "phi42" -> 1
  )


  val phi16_phi_in = Map(
    "load13" -> 0,
    "phi43" -> 1
  )


  val phi17_phi_in = Map(
    "load5" -> 0,
    "add45" -> 1
  )


  val phi42_phi_in = Map(
    "add34" -> 0,
    "phi15" -> 1
  )


  val phi43_phi_in = Map(
    "phi16" -> 0,
    "add40" -> 1
  )


  //  %A1 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 0, !UID !2, !ScalaLabel !3
  val getelementptr0_in = Map(
    "field0" -> 0
  )


  //  %0 = load i32*, i32** %A1, align 4, !UID !4, !ScalaLabel !5
  val load1_in = Map(
    "getelementptr0" -> 0
  )


  //  %B2 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 1, !UID !6, !ScalaLabel !7
  val getelementptr2_in = Map(
    "field0" -> 1
  )


  //  %1 = load i32*, i32** %B2, align 4, !UID !8, !ScalaLabel !9
  val load3_in = Map(
    "getelementptr2" -> 0
  )


  //  %iBegin3 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !10, !ScalaLabel !11
  val getelementptr4_in = Map(
    "field0" -> 2
  )


  //  %2 = load i32, i32* %iBegin3, align 4, !UID !12, !ScalaLabel !13
  val load5_in = Map(
    "getelementptr4" -> 0
  )


  //  %iMiddle4 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !14, !ScalaLabel !15
  val getelementptr6_in = Map(
    "field0" -> 3
  )


  //  %3 = load i32, i32* %iMiddle4, align 4, !UID !16, !ScalaLabel !17
  val load7_in = Map(
    "getelementptr6" -> 0
  )


  //  %iEnd5 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 4, !UID !18, !ScalaLabel !19
  val getelementptr8_in = Map(
    "field0" -> 4
  )


  //  %4 = load i32, i32* %iEnd5, align 4, !UID !20, !ScalaLabel !21
  val load9_in = Map(
    "getelementptr8" -> 0
  )


  //  %iBegin6 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !22, !ScalaLabel !23
  val getelementptr10_in = Map(
    "field0" -> 5
  )


  //  %5 = load i32, i32* %iBegin6, align 4, !UID !24, !ScalaLabel !25
  val load11_in = Map(
    "getelementptr10" -> 0
  )


  //  %iMiddle7 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !26, !ScalaLabel !27
  val getelementptr12_in = Map(
    "field0" -> 6
  )


  //  %6 = load i32, i32* %iMiddle7, align 4, !UID !28, !ScalaLabel !29
  val load13_in = Map(
    "getelementptr12" -> 0
  )


  //  %i.0 = phi i32 [ %5, %entry ], [ %i.1, %for.inc ], !UID !33, !ScalaLabel !34
  val phi15_in = Map(
    "load11" -> 0,
    "phi42" -> 0
  )


  //  %j.0 = phi i32 [ %6, %entry ], [ %j.1, %for.inc ], !UID !35, !ScalaLabel !36
  val phi16_in = Map(
    "load13" -> 0,
    "phi43" -> 0
  )


  //  %k.0 = phi i32 [ %2, %entry ], [ %inc, %for.inc ], !UID !37, !ScalaLabel !38
  val phi17_in = Map(
    "load5" -> 0,
    "add45" -> 0
  )


  //  %cmp = icmp ult i32 %k.0, %4, !UID !39, !ScalaLabel !40
  val icmp18_in = Map(
    "phi17" -> 0,
    "load9" -> 0
  )


  //  br i1 %cmp, label %for.body, label %for.end, !UID !41, !BB_UID !42, !ScalaLabel !43
  val br19_in = Map(
    "icmp18" -> 0
  )


  //  %cmp8 = icmp ult i32 %i.0, %3, !UID !44, !ScalaLabel !45
  val icmp20_in = Map(
    "phi15" -> 0,
    "load7" -> 0
  )


  //  br i1 %cmp8, label %land.lhs.true, label %if.else, !UID !46, !BB_UID !47, !ScalaLabel !48
  val br21_in = Map(
    "icmp20" -> 0
  )


  //  %cmp9 = icmp uge i32 %j.0, %4, !UID !49, !ScalaLabel !50
  val icmp22_in = Map(
    "phi16" -> 0,
    "load9" -> 1
  )


  //  br i1 %cmp9, label %if.then, label %lor.lhs.false, !UID !51, !BB_UID !52, !ScalaLabel !53
  val br23_in = Map(
    "icmp22" -> 0
  )


  //  %arrayidx = getelementptr inbounds i32, i32* %0, i32 %i.0, !UID !54, !ScalaLabel !55
  val getelementptr24_in = Map(
    "load1" -> 0,
    "phi15" -> 1
  )


  //  %7 = load i32, i32* %arrayidx, align 4, !UID !56, !ScalaLabel !57
  val load25_in = Map(
    "getelementptr24" -> 0
  )


  //  %arrayidx10 = getelementptr inbounds i32, i32* %0, i32 %j.0, !UID !58, !ScalaLabel !59
  val getelementptr26_in = Map(
    "load1" -> 1,
    "phi16" -> 1
  )


  //  %8 = load i32, i32* %arrayidx10, align 4, !UID !60, !ScalaLabel !61
  val load27_in = Map(
    "getelementptr26" -> 0
  )


  //  %cmp11 = icmp ule i32 %7, %8, !UID !62, !ScalaLabel !63
  val icmp28_in = Map(
    "load25" -> 0,
    "load27" -> 0
  )


  //  br i1 %cmp11, label %if.then, label %if.else, !UID !64, !BB_UID !65, !ScalaLabel !66
  val br29_in = Map(
    "icmp28" -> 0
  )


  //  %arrayidx12 = getelementptr inbounds i32, i32* %0, i32 %i.0, !UID !67, !ScalaLabel !68
  val getelementptr30_in = Map(
    "load1" -> 2,
    "phi15" -> 2
  )


  //  %9 = load i32, i32* %arrayidx12, align 4, !UID !69, !ScalaLabel !70
  val load31_in = Map(
    "getelementptr30" -> 0
  )


  //  %arrayidx13 = getelementptr inbounds i32, i32* %1, i32 %k.0, !UID !71, !ScalaLabel !72
  val getelementptr32_in = Map(
    "load3" -> 0,
    "phi17" -> 1
  )


  //  store i32 %9, i32* %arrayidx13, align 4, !UID !73, !ScalaLabel !74
  val store33_in = Map(
    "load31" -> 0,
    "getelementptr32" -> 0
  )


  //  %add = add i32 %i.0, 1, !UID !75, !ScalaLabel !76
  val add34_in = Map(
    "phi15" -> 3
  )


  //  %arrayidx14 = getelementptr inbounds i32, i32* %0, i32 %j.0, !UID !80, !ScalaLabel !81
  val getelementptr36_in = Map(
    "load1" -> 3,
    "phi16" -> 2
  )


  //  %10 = load i32, i32* %arrayidx14, align 4, !UID !82, !ScalaLabel !83
  val load37_in = Map(
    "getelementptr36" -> 0
  )


  //  %arrayidx15 = getelementptr inbounds i32, i32* %1, i32 %k.0, !UID !84, !ScalaLabel !85
  val getelementptr38_in = Map(
    "load3" -> 1,
    "phi17" -> 2
  )


  //  store i32 %10, i32* %arrayidx15, align 4, !UID !86, !ScalaLabel !87
  val store39_in = Map(
    "load37" -> 0,
    "getelementptr38" -> 0
  )


  //  %add16 = add i32 %j.0, 1, !UID !88, !ScalaLabel !89
  val add40_in = Map(
    "phi16" -> 3
  )


  //  %i.1 = phi i32 [ %add, %if.then ], [ %i.0, %if.else ], !UID !93, !ScalaLabel !94
  val phi42_in = Map(
    "add34" -> 0,
    "phi15" -> 4
  )


  //  %j.1 = phi i32 [ %j.0, %if.then ], [ %add16, %if.else ], !UID !95, !ScalaLabel !96
  val phi43_in = Map(
    "phi16" -> 4,
    "add40" -> 0
  )


  //  %inc = add i32 %k.0, 1, !UID !100, !ScalaLabel !101
  val add45_in = Map(
    "phi17" -> 3
  )


  //  ret void, !UID !105, !BB_UID !106, !ScalaLabel !107
  val ret47_in = Map(

  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class mergesort_mergeDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32))))
    val StackResp = Flipped(Valid(new MemResp))
    val StackReq = Decoupled(new MemReq)
    val GlblResp = Flipped(Valid(new MemResp))
    val GlblReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class mergesort_mergeDF(implicit p: Parameters) extends mergesort_mergeDFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */

/*
  val StackPointer = Module(new Stack(NumOps = 1))

  val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=11,NWrites=2)
                (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
                (RControl=new ReadMemoryController(NumOps=11,BaseSize=2,NumEntries=2)))

  val StackCtrl = Module(new UnifiedController(ID=0,Size=64*1024,NReads=7,NWrites=1)
                (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
                (RControl=new ReadMemoryController(NumOps=7,BaseSize=2,NumEntries=2))
                (RWArbiter=new ReadWriteArbiter()))
*/
  val StackCtrl = Module(new ReadMemoryController(NumOps=7,BaseSize=2,NumEntries=2))

  io.StackReq <> StackCtrl.io.MemReq
  StackCtrl.io.MemResp <> io.StackResp

  val GlblCtrl = Module(new UnifiedController(ID=1,Size=64*1024,NReads=4,NWrites=2)
    (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
    (RControl=new ReadMemoryController(NumOps=4,BaseSize=2,NumEntries=2))
    (RWArbiter=new ReadWriteArbiter()))

  io.GlblReq <> GlblCtrl.io.MemReq
  GlblCtrl.io.MemResp <> io.GlblResp

  val InputSplitter = Module(new SplitCallNew(List(7)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  val lb_L_0 = Module(new LoopBlock(ID=999,NumIns=List(4,2,1,1,2,1,1),NumOuts=0,NumExits=1)) //@todo Fix NumExits


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 15, BID = 0))

  val bb_for_cond = Module(new LoopHead(NumOuts = 5, NumPhi = 3, BID = 1))

  val bb_for_body = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 2))

  val bb_land_lhs_true = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 3))

  val bb_lor_lhs_false = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 6, BID = 4))

  val bb_if_then = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 6, NumPhi = 0, BID = 5))

  val bb_if_else = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 6, NumPhi = 0, BID = 6))

  val bb_if_end = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi = 2, BID = 7))

  val bb_for_inc = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 8))

  val bb_for_end = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 1, BID = 9))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %A1 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 0, !UID !2, !ScalaLabel !3
  val getelementptr0 = Module (new GepTwoNode(NumOuts = 1, ID = 0)(numByte1 = 20, numByte2 = 4))


  //  %0 = load i32*, i32** %A1, align 4, !UID !4, !ScalaLabel !5
  val load1 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=1,RouteID=0))


  //  %B2 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 1, !UID !6, !ScalaLabel !7
  val getelementptr2 = Module (new GepTwoNode(NumOuts = 1, ID = 2)(numByte1 = 20, numByte2 = 4))


  //  %1 = load i32*, i32** %B2, align 4, !UID !8, !ScalaLabel !9
  val load3 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=3,RouteID=1))


  //  %iBegin3 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !10, !ScalaLabel !11
  val getelementptr4 = Module (new GepTwoNode(NumOuts = 1, ID = 4)(numByte1 = 20, numByte2 = 4))


  //  %2 = load i32, i32* %iBegin3, align 4, !UID !12, !ScalaLabel !13
  val load5 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=5,RouteID=2))


  //  %iMiddle4 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !14, !ScalaLabel !15
  val getelementptr6 = Module (new GepTwoNode(NumOuts = 1, ID = 6)(numByte1 = 20, numByte2 = 4))


  //  %3 = load i32, i32* %iMiddle4, align 4, !UID !16, !ScalaLabel !17
  val load7 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=7,RouteID=3))


  //  %iEnd5 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 4, !UID !18, !ScalaLabel !19
  val getelementptr8 = Module (new GepTwoNode(NumOuts = 1, ID = 8)(numByte1 = 20, numByte2 = 4))


  //  %4 = load i32, i32* %iEnd5, align 4, !UID !20, !ScalaLabel !21
  val load9 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=9,RouteID=4))


  //  %iBegin6 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 2, !UID !22, !ScalaLabel !23
  val getelementptr10 = Module (new GepTwoNode(NumOuts = 1, ID = 10)(numByte1 = 20, numByte2 = 4))


  //  %5 = load i32, i32* %iBegin6, align 4, !UID !24, !ScalaLabel !25
  val load11 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=11,RouteID=5))


  //  %iMiddle7 = getelementptr inbounds %struct.continue_struct, %struct.continue_struct* %p, i32 0, i32 3, !UID !26, !ScalaLabel !27
  val getelementptr12 = Module (new GepTwoNode(NumOuts = 1, ID = 12)(numByte1 = 20, numByte2 = 4))


  //  %6 = load i32, i32* %iMiddle7, align 4, !UID !28, !ScalaLabel !29
  val load13 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=13,RouteID=6))


  //  br label %for.cond, !UID !30, !BB_UID !31, !ScalaLabel !32
  val br14 = Module (new UBranchNode(ID = 14))

  // [BasicBlock]  for.cond:

  //  %i.0 = phi i32 [ %5, %entry ], [ %i.1, %for.inc ], !UID !33, !ScalaLabel !34
  val phi15 = Module (new PhiNode(NumInputs = 2, NumOuts = 5, ID = 15))


  //  %j.0 = phi i32 [ %6, %entry ], [ %j.1, %for.inc ], !UID !35, !ScalaLabel !36
  val phi16 = Module (new PhiNode(NumInputs = 2, NumOuts = 5, ID = 16))


  //  %k.0 = phi i32 [ %2, %entry ], [ %inc, %for.inc ], !UID !37, !ScalaLabel !38
  val phi17 = Module (new PhiNode(NumInputs = 2, NumOuts = 4, ID = 17))


  //  %cmp = icmp ult i32 %k.0, %4, !UID !39, !ScalaLabel !40
  val icmp18 = Module (new IcmpNode(NumOuts = 1, ID = 18, opCode = "ULT")(sign=false))


  //  br i1 %cmp, label %for.body, label %for.end, !UID !41, !BB_UID !42, !ScalaLabel !43
  val br19 = Module (new CBranchNode(ID = 19))

  // [BasicBlock]  for.body:

  //  %cmp8 = icmp ult i32 %i.0, %3, !UID !44, !ScalaLabel !45
  val icmp20 = Module (new IcmpNode(NumOuts = 1, ID = 20, opCode = "ULT")(sign=false))


  //  br i1 %cmp8, label %land.lhs.true, label %if.else, !UID !46, !BB_UID !47, !ScalaLabel !48
  val br21 = Module (new CBranchNode(ID = 21))

  // [BasicBlock]  land.lhs.true:

  //  %cmp9 = icmp uge i32 %j.0, %4, !UID !49, !ScalaLabel !50
  val icmp22 = Module (new IcmpNode(NumOuts = 1, ID = 22, opCode = "UGE")(sign=false))


  //  br i1 %cmp9, label %if.then, label %lor.lhs.false, !UID !51, !BB_UID !52, !ScalaLabel !53
  val br23 = Module (new CBranchNode(ID = 23))

  // [BasicBlock]  lor.lhs.false:

  //  %arrayidx = getelementptr inbounds i32, i32* %0, i32 %i.0, !UID !54, !ScalaLabel !55
  val getelementptr24 = Module (new GepOneNode(NumOuts = 1, ID = 24)(numByte1 = 4))


  //  %7 = load i32, i32* %arrayidx, align 4, !UID !56, !ScalaLabel !57
  val load25 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=25,RouteID=0))


  //  %arrayidx10 = getelementptr inbounds i32, i32* %0, i32 %j.0, !UID !58, !ScalaLabel !59
  val getelementptr26 = Module (new GepOneNode(NumOuts = 1, ID = 26)(numByte1 = 4))


  //  %8 = load i32, i32* %arrayidx10, align 4, !UID !60, !ScalaLabel !61
  val load27 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=27,RouteID=1))


  //  %cmp11 = icmp ule i32 %7, %8, !UID !62, !ScalaLabel !63
  val icmp28 = Module (new IcmpNode(NumOuts = 1, ID = 28, opCode = "ULE")(sign=false))


  //  br i1 %cmp11, label %if.then, label %if.else, !UID !64, !BB_UID !65, !ScalaLabel !66
  val br29 = Module (new CBranchNode(ID = 29))

  // [BasicBlock]  if.then:

  //  %arrayidx12 = getelementptr inbounds i32, i32* %0, i32 %i.0, !UID !67, !ScalaLabel !68
  val getelementptr30 = Module (new GepOneNode(NumOuts = 1, ID = 30)(numByte1 = 4))


  //  %9 = load i32, i32* %arrayidx12, align 4, !UID !69, !ScalaLabel !70
  val load31 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=31,RouteID=2))


  //  %arrayidx13 = getelementptr inbounds i32, i32* %1, i32 %k.0, !UID !71, !ScalaLabel !72
  val getelementptr32 = Module (new GepOneNode(NumOuts = 1, ID = 32)(numByte1 = 4))


  //  store i32 %9, i32* %arrayidx13, align 4, !UID !73, !ScalaLabel !74
  val store33 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=1, NumOuts=1,ID=33,RouteID=0))


  //  %add = add i32 %i.0, 1, !UID !75, !ScalaLabel !76
  val add34 = Module (new ComputeNode(NumOuts = 1, ID = 34, opCode = "add")(sign=false))


  //  br label %if.end, !UID !77, !BB_UID !78, !ScalaLabel !79
  val br35 = Module (new UBranchNode(ID = 35, NumPredOps=1))

  // [BasicBlock]  if.else:

  //  %arrayidx14 = getelementptr inbounds i32, i32* %0, i32 %j.0, !UID !80, !ScalaLabel !81
  val getelementptr36 = Module (new GepOneNode(NumOuts = 1, ID = 36)(numByte1 = 4))


  //  %10 = load i32, i32* %arrayidx14, align 4, !UID !82, !ScalaLabel !83
  val load37 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=37,RouteID=3))


  //  %arrayidx15 = getelementptr inbounds i32, i32* %1, i32 %k.0, !UID !84, !ScalaLabel !85
  val getelementptr38 = Module (new GepOneNode(NumOuts = 1, ID = 38)(numByte1 = 4))


  //  store i32 %10, i32* %arrayidx15, align 4, !UID !86, !ScalaLabel !87
  val store39 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=1, NumOuts=1,ID=39,RouteID=1))


  //  %add16 = add i32 %j.0, 1, !UID !88, !ScalaLabel !89
  val add40 = Module (new ComputeNode(NumOuts = 1, ID = 40, opCode = "add")(sign=false))


  //  br label %if.end, !UID !90, !BB_UID !91, !ScalaLabel !92
  val br41 = Module (new UBranchNode(ID = 41,NumPredOps=1))

  // [BasicBlock]  if.end:

  //  %i.1 = phi i32 [ %add, %if.then ], [ %i.0, %if.else ], !UID !93, !ScalaLabel !94
  val phi42 = Module (new PhiNode(NumInputs = 2, NumOuts = 2, ID = 42))


  //  %j.1 = phi i32 [ %j.0, %if.then ], [ %add16, %if.else ], !UID !95, !ScalaLabel !96
  val phi43 = Module (new PhiNode(NumInputs = 2, NumOuts = 2, ID = 43))


  //  br label %for.inc, !UID !97, !BB_UID !98, !ScalaLabel !99
  val br44 = Module (new UBranchNode(ID = 44, NumPredOps = 2, NumOuts = 2))

  // [BasicBlock]  for.inc:

  //  %inc = add i32 %k.0, 1, !UID !100, !ScalaLabel !101
  val add45 = Module (new ComputeNode(NumOuts = 1, ID = 45, opCode = "add")(sign=false))


  //  br label %for.cond, !UID !102, !BB_UID !103, !ScalaLabel !104
  val br46 = Module (new UBranchNode(ID = 46))

  // [BasicBlock]  for.end:

  //  ret void, !UID !105, !BB_UID !106, !ScalaLabel !107
  val ret47 = Module(new RetNode(retTypes=List(32), ID=47))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_mergesort_merge_FlowParam



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

  //Connecting br14 to bb_for_cond
  lb_L_0.io.enable <> br14.io.Out(param.br14_brn_bb("bb_for_cond"))
  bb_for_cond.io.activate <> lb_L_0.io.activate

  //Connecting br19 to bb_for_body
  bb_for_body.io.predicateIn <> br19.io.Out(param.br19_brn_bb("bb_for_body"))


  //Connecting br19 to bb_for_end
  lb_L_0.io.loopExit(0) <> br19.io.Out(param.br19_brn_bb("bb_for_end"))
  bb_for_end.io.predicateIn <> lb_L_0.io.endEnable

  //Connecting br21 to bb_land_lhs_true
  bb_land_lhs_true.io.predicateIn <> br21.io.Out(param.br21_brn_bb("bb_land_lhs_true"))


  //Connecting br21 to bb_if_else
  bb_if_else.io.predicateIn(1) <> br21.io.Out(param.br21_brn_bb("bb_if_else"))


  //Connecting br23 to bb_if_then
  bb_if_then.io.predicateIn(1) <> br23.io.Out(param.br23_brn_bb("bb_if_then"))


  //Connecting br23 to bb_lor_lhs_false
  bb_lor_lhs_false.io.predicateIn <> br23.io.Out(param.br23_brn_bb("bb_lor_lhs_false"))


  //Connecting br29 to bb_if_then
  bb_if_then.io.predicateIn(0) <> br29.io.Out(param.br29_brn_bb("bb_if_then"))


  //Connecting br29 to bb_if_else
  bb_if_else.io.predicateIn(0) <> br29.io.Out(param.br29_brn_bb("bb_if_else"))


  //Connecting br35 to bb_if_end
  bb_if_end.io.predicateIn(1) <> br35.io.Out(param.br35_brn_bb("bb_if_end"))


  //Connecting br41 to bb_if_end
  bb_if_end.io.predicateIn(0) <> br41.io.Out(param.br41_brn_bb("bb_if_end"))


  //Connecting br44 to bb_for_inc
  bb_for_inc.io.predicateIn <> br44.io.Out(param.br44_brn_bb("bb_for_inc"))


  //Connecting br46 to bb_for_cond
  bb_for_cond.io.loopBack <> br46.io.Out(0)

  // Make br44 dependent on last operations of for body (phi42, phi43)
  br44.io.PredOp(0).valid := phi42.io.Out(1).valid
  br44.io.PredOp(0).bits := phi42.io.Out(1).bits.asControlBundle()
  phi42.io.Out(1).ready := br44.io.PredOp(0).ready

  br44.io.PredOp(1).valid := phi43.io.Out(1).valid
  br44.io.PredOp(1).bits := phi43.io.Out(1).bits.asControlBundle()
  phi43.io.Out(1).ready := br44.io.PredOp(1).ready

  lb_L_0.io.latchEnable <>  br44.io.Out(1) // Manually connect to last instruction of loop body



  // There is no detach instruction



  // There is no detach instruction




  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  getelementptr0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr0"))

  load1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load1"))

  getelementptr2.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr2"))

  load3.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load3"))

  getelementptr4.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr4"))

  load5.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load5"))

  getelementptr6.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr6"))

  load7.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load7"))

  getelementptr8.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr8"))

  load9.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load9"))

  getelementptr10.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr10"))

  load11.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load11"))

  getelementptr12.io.enable <> bb_entry.io.Out(param.bb_entry_activate("getelementptr12"))

  load13.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load13"))

  br14.io.enable <> bb_entry.io.Out(param.bb_entry_activate("br14"))



  phi15.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("phi15"))

  phi16.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("phi16"))

  phi17.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("phi17"))

  icmp18.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("icmp18"))

  br19.io.enable <> bb_for_cond.io.Out(param.bb_for_cond_activate("br19"))



  icmp20.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("icmp20"))

  br21.io.enable <> bb_for_body.io.Out(param.bb_for_body_activate("br21"))



  icmp22.io.enable <> bb_land_lhs_true.io.Out(param.bb_land_lhs_true_activate("icmp22"))

  br23.io.enable <> bb_land_lhs_true.io.Out(param.bb_land_lhs_true_activate("br23"))



  getelementptr24.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("getelementptr24"))

  load25.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("load25"))

  getelementptr26.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("getelementptr26"))

  load27.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("load27"))

  icmp28.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("icmp28"))

  br29.io.enable <> bb_lor_lhs_false.io.Out(param.bb_lor_lhs_false_activate("br29"))



  getelementptr30.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("getelementptr30"))

  load31.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("load31"))

  getelementptr32.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("getelementptr32"))

  store33.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("store33"))

  add34.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("add34"))

  br35.io.PredOp(0) <> store33.io.SuccOp(0)
  br35.io.enable <> bb_if_then.io.Out(param.bb_if_then_activate("br35"))



  getelementptr36.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("getelementptr36"))

  load37.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("load37"))

  getelementptr38.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("getelementptr38"))

  store39.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("store39"))

  add40.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("add40"))

  br41.io.PredOp(0) <> store39.io.SuccOp(0)
  br41.io.enable <> bb_if_else.io.Out(param.bb_if_else_activate("br41"))



  phi42.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("phi42"))

  phi43.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("phi43"))

  br44.io.enable <> bb_if_end.io.Out(param.bb_if_end_activate("br44"))



  add45.io.enable <> bb_for_inc.io.Out(param.bb_for_inc_activate("add45"))

  br46.io.enable <> bb_for_inc.io.Out(param.bb_for_inc_activate("br46"))



  ret47.io.enable <> bb_for_end.io.Out(param.bb_for_end_activate("ret47"))





  /* ================================================================== *
   *                   CONNECTING LOOPHEADERS                           *
   * ================================================================== */


  // Connecting instruction to the loop header
  //  %0 = load i32*, i32** %A1, align 4, !UID !4, !ScalaLabel !5
  lb_L_0.io.In(0) <> load1.io.Out(param.getelementptr24_in("load1"))

  // Connecting instruction to the loop header
  //  %1 = load i32*, i32** %B2, align 4, !UID !8, !ScalaLabel !9
  lb_L_0.io.In(1) <> load3.io.Out(param.getelementptr32_in("load3"))

  // Connecting instruction to the loop header
  //  %2 = load i32, i32* %iBegin3, align 4, !UID !12, !ScalaLabel !13
  lb_L_0.io.In(2) <> load5.io.Out(param.phi17_in("load5"))

  // Connecting instruction to the loop header
  //  %3 = load i32, i32* %iMiddle4, align 4, !UID !16, !ScalaLabel !17
  lb_L_0.io.In(3) <> load7.io.Out(param.icmp20_in("load7"))

  // Connecting instruction to the loop header
  //  %4 = load i32, i32* %iEnd5, align 4, !UID !20, !ScalaLabel !21
  lb_L_0.io.In(4) <> load9.io.Out(param.icmp18_in("load9"))

  // Connecting instruction to the loop header
  //  %5 = load i32, i32* %iBegin6, align 4, !UID !24, !ScalaLabel !25
  lb_L_0.io.In(5) <> load11.io.Out(param.phi15_in("load11"))

  // Connecting instruction to the loop header
  //  %6 = load i32, i32* %iMiddle7, align 4, !UID !28, !ScalaLabel !29
  lb_L_0.io.In(6) <> load13.io.Out(param.phi16_in("load13"))



  /* ================================================================== *
   *                   DUMPING PHI NODES                                *
   * ================================================================== */


  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  // Wiring Live in to PHI node

  phi15.io.InData(param.phi15_phi_in("load11")) <> lb_L_0.io.liveIn("field5")(0)

  phi15.io.InData(param.phi15_phi_in("phi42")) <> phi42.io.Out(param.phi15_in("phi42"))

  // Wiring Live in to PHI node

  phi16.io.InData(param.phi16_phi_in("load13")) <> lb_L_0.io.liveIn("field6")(0)

  phi16.io.InData(param.phi16_phi_in("phi43")) <> phi43.io.Out(param.phi16_in("phi43"))

  // Wiring Live in to PHI node

  phi17.io.InData(param.phi17_phi_in("load5")) <> lb_L_0.io.liveIn("field2")(0)

  phi17.io.InData(param.phi17_phi_in("add45")) <> add45.io.Out(param.phi17_in("add45"))

  phi42.io.InData(param.phi42_phi_in("add34")) <> add34.io.Out(param.phi42_in("add34"))

  phi42.io.InData(param.phi42_phi_in("phi15")) <> phi15.io.Out(param.phi42_in("phi15"))

  phi43.io.InData(param.phi43_phi_in("phi16")) <> phi16.io.Out(param.phi43_in("phi16"))

  phi43.io.InData(param.phi43_phi_in("add40")) <> add40.io.Out(param.phi43_in("add40"))

  /**
    * Connecting PHI Masks
    */
  //Connect PHI node

  phi15.io.Mask <> bb_for_cond.io.MaskBB(0)

  phi16.io.Mask <> bb_for_cond.io.MaskBB(1)

  phi17.io.Mask <> bb_for_cond.io.MaskBB(2)

  phi42.io.Mask <> bb_if_end.io.MaskBB(0)

  phi43.io.Mask <> bb_if_end.io.MaskBB(1)



  /* ================================================================== *
   *                   DUMPING DATAFLOW                                 *
   * ================================================================== */


  /**
    * Connecting Dataflow signals
    */

  // Wiring GEP instruction to the function argument
  getelementptr0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  // Wiring GEP instruction to the Constant
  getelementptr0.io.idx1.valid :=  true.B
  getelementptr0.io.idx1.bits.predicate :=  true.B
  getelementptr0.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr0.io.idx2.valid :=  true.B
  getelementptr0.io.idx2.bits.predicate :=  true.B
  getelementptr0.io.idx2.bits.data :=  0.U


  // Wiring Load instruction to the parent instruction
  load1.io.GepAddr <> getelementptr0.io.Out(param.load1_in("getelementptr0"))
  load1.io.memResp <> StackCtrl.io.ReadOut(0)
  StackCtrl.io.ReadIn(0) <> load1.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr2.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  // Wiring GEP instruction to the Constant
  getelementptr2.io.idx1.valid :=  true.B
  getelementptr2.io.idx1.bits.predicate :=  true.B
  getelementptr2.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr2.io.idx2.valid :=  true.B
  getelementptr2.io.idx2.bits.predicate :=  true.B
  getelementptr2.io.idx2.bits.data :=  1.U


  // Wiring Load instruction to the parent instruction
  load3.io.GepAddr <> getelementptr2.io.Out(param.load3_in("getelementptr2"))
  load3.io.memResp <> StackCtrl.io.ReadOut(1)
  StackCtrl.io.ReadIn(1) <> load3.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr4.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  // Wiring GEP instruction to the Constant
  getelementptr4.io.idx1.valid :=  true.B
  getelementptr4.io.idx1.bits.predicate :=  true.B
  getelementptr4.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr4.io.idx2.valid :=  true.B
  getelementptr4.io.idx2.bits.predicate :=  true.B
  getelementptr4.io.idx2.bits.data :=  2.U


  // Wiring Load instruction to the parent instruction
  load5.io.GepAddr <> getelementptr4.io.Out(param.load5_in("getelementptr4"))
  load5.io.memResp <> StackCtrl.io.ReadOut(2)
  StackCtrl.io.ReadIn(2) <> load5.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr6.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(3)

  // Wiring GEP instruction to the Constant
  getelementptr6.io.idx1.valid :=  true.B
  getelementptr6.io.idx1.bits.predicate :=  true.B
  getelementptr6.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr6.io.idx2.valid :=  true.B
  getelementptr6.io.idx2.bits.predicate :=  true.B
  getelementptr6.io.idx2.bits.data :=  3.U


  // Wiring Load instruction to the parent instruction
  load7.io.GepAddr <> getelementptr6.io.Out(param.load7_in("getelementptr6"))
  load7.io.memResp <> StackCtrl.io.ReadOut(3)
  StackCtrl.io.ReadIn(3) <> load7.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr8.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(4)

  // Wiring GEP instruction to the Constant
  getelementptr8.io.idx1.valid :=  true.B
  getelementptr8.io.idx1.bits.predicate :=  true.B
  getelementptr8.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr8.io.idx2.valid :=  true.B
  getelementptr8.io.idx2.bits.predicate :=  true.B
  getelementptr8.io.idx2.bits.data :=  4.U


  // Wiring Load instruction to the parent instruction
  load9.io.GepAddr <> getelementptr8.io.Out(param.load9_in("getelementptr8"))
  load9.io.memResp <> StackCtrl.io.ReadOut(4)
  StackCtrl.io.ReadIn(4) <> load9.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr10.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(5)

  // Wiring GEP instruction to the Constant
  getelementptr10.io.idx1.valid :=  true.B
  getelementptr10.io.idx1.bits.predicate :=  true.B
  getelementptr10.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr10.io.idx2.valid :=  true.B
  getelementptr10.io.idx2.bits.predicate :=  true.B
  getelementptr10.io.idx2.bits.data :=  2.U

  // Wiring Load instruction to the parent instruction
  load11.io.GepAddr <> getelementptr10.io.Out(param.load11_in("getelementptr10"))
  load11.io.memResp <> StackCtrl.io.ReadOut(5)
  StackCtrl.io.ReadIn(5) <> load11.io.memReq




  // Wiring GEP instruction to the function argument
  getelementptr12.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(6)

  // Wiring GEP instruction to the Constant
  getelementptr12.io.idx1.valid :=  true.B
  getelementptr12.io.idx1.bits.predicate :=  true.B
  getelementptr12.io.idx1.bits.data :=  0.U


  // Wiring GEP instruction to the Constant
  getelementptr12.io.idx2.valid :=  true.B
  getelementptr12.io.idx2.bits.predicate :=  true.B
  getelementptr12.io.idx2.bits.data :=  3.U


  // Wiring Load instruction to the parent instruction
  load13.io.GepAddr <> getelementptr12.io.Out(param.load13_in("getelementptr12"))
  load13.io.memResp <> StackCtrl.io.ReadOut(6)
  StackCtrl.io.ReadIn(6) <> load13.io.memReq




  // Wiring instructions
  icmp18.io.LeftIO <> phi17.io.Out(param.icmp18_in("phi17"))

  // Wiring instructions
  icmp18.io.RightIO <> lb_L_0.io.liveIn("field4")(0)

  // Wiring Branch instruction
  br19.io.CmpIO <> icmp18.io.Out(param.br19_in("icmp18"))

  // Wiring instructions
  icmp20.io.LeftIO <> phi15.io.Out(param.icmp20_in("phi15"))

  // Wiring instructions
  icmp20.io.RightIO <> lb_L_0.io.liveIn("field3")(0)

  // Wiring Branch instruction
  br21.io.CmpIO <> icmp20.io.Out(param.br21_in("icmp20"))

  // Wiring instructions
  icmp22.io.LeftIO <> phi16.io.Out(param.icmp22_in("phi16"))

  // Wiring instructions
  icmp22.io.RightIO <> lb_L_0.io.liveIn("field4")(1)

  // Wiring Branch instruction
  br23.io.CmpIO <> icmp22.io.Out(param.br23_in("icmp22"))

  // Wiring GEP instruction to the loop header
  getelementptr24.io.baseAddress <> lb_L_0.io.liveIn("field0")(0)

  // Wiring GEP instruction to the parent instruction
  getelementptr24.io.idx1 <> phi15.io.Out(param.getelementptr24_in("phi15"))


  // Wiring Load instruction to the parent instruction
  load25.io.GepAddr <> getelementptr24.io.Out(param.load25_in("getelementptr24"))
  load25.io.memResp <> GlblCtrl.io.ReadOut(0)
  GlblCtrl.io.ReadIn(0) <> load25.io.memReq




  // Wiring GEP instruction to the loop header
  getelementptr26.io.baseAddress <> lb_L_0.io.liveIn("field0")(1)

  // Wiring GEP instruction to the parent instruction
  getelementptr26.io.idx1 <> phi16.io.Out(param.getelementptr26_in("phi16"))


  // Wiring Load instruction to the parent instruction
  load27.io.GepAddr <> getelementptr26.io.Out(param.load27_in("getelementptr26"))
  load27.io.memResp <> GlblCtrl.io.ReadOut(1)
  GlblCtrl.io.ReadIn(1) <> load27.io.memReq




  // Wiring instructions
  icmp28.io.LeftIO <> load25.io.Out(param.icmp28_in("load25"))

  // Wiring instructions
  icmp28.io.RightIO <> load27.io.Out(param.icmp28_in("load27"))

  // Wiring Branch instruction
  br29.io.CmpIO <> icmp28.io.Out(param.br29_in("icmp28"))

  // Wiring GEP instruction to the loop header
  getelementptr30.io.baseAddress <> lb_L_0.io.liveIn("field0")(2)

  // Wiring GEP instruction to the parent instruction
  getelementptr30.io.idx1 <> phi15.io.Out(param.getelementptr30_in("phi15"))

  // Wiring Load instruction to the parent instruction
  load31.io.GepAddr <> getelementptr30.io.Out(param.load31_in("getelementptr30"))
  load31.io.memResp <> GlblCtrl.io.ReadOut(2)
  GlblCtrl.io.ReadIn(2) <> load31.io.memReq




  // Wiring GEP instruction to the loop header
  getelementptr32.io.baseAddress <> lb_L_0.io.liveIn("field1")(0)

  // Wiring GEP instruction to the parent instruction
  getelementptr32.io.idx1 <> phi17.io.Out(param.getelementptr32_in("phi17"))


  store33.io.inData <> load31.io.Out(param.store33_in("load31"))



  // Wiring Store instruction to the parent instruction
  store33.io.GepAddr <> getelementptr32.io.Out(param.store33_in("getelementptr32"))
  store33.io.memResp  <> GlblCtrl.io.WriteOut(0)
  GlblCtrl.io.WriteIn(0) <> store33.io.memReq
  store33.io.Out(0).ready := true.B



  // Wiring instructions
  add34.io.LeftIO <> phi15.io.Out(param.add34_in("phi15"))

  // Wiring constant
  add34.io.RightIO.bits.data := 1.U
  add34.io.RightIO.bits.predicate := true.B
  add34.io.RightIO.valid := true.B

  // Wiring GEP instruction to the loop header
  getelementptr36.io.baseAddress <> lb_L_0.io.liveIn("field0")(3)

  // Wiring GEP instruction to the parent instruction
  getelementptr36.io.idx1 <> phi16.io.Out(param.getelementptr36_in("phi16"))


  // Wiring Load instruction to the parent instruction
  load37.io.GepAddr <> getelementptr36.io.Out(param.load37_in("getelementptr36"))
  load37.io.memResp <> GlblCtrl.io.ReadOut(3)
  GlblCtrl.io.ReadIn(3) <> load37.io.memReq




  // Wiring GEP instruction to the loop header
  getelementptr38.io.baseAddress <> lb_L_0.io.liveIn("field1")(1)

  // Wiring GEP instruction to the parent instruction
  getelementptr38.io.idx1 <> phi17.io.Out(param.getelementptr38_in("phi17"))


  store39.io.inData <> load37.io.Out(param.store39_in("load37"))



  // Wiring Store instruction to the parent instruction
  store39.io.GepAddr <> getelementptr38.io.Out(param.store39_in("getelementptr38"))
  store39.io.memResp  <> GlblCtrl.io.WriteOut(1)
  GlblCtrl.io.WriteIn(1) <> store39.io.memReq
  store39.io.Out(0).ready := true.B



  // Wiring instructions
  add40.io.LeftIO <> phi16.io.Out(param.add40_in("phi16"))

  // Wiring constant
  add40.io.RightIO.bits.data := 1.U
  add40.io.RightIO.bits.predicate := true.B
  add40.io.RightIO.valid := true.B

  // Wiring instructions
  add45.io.LeftIO <> phi17.io.Out(param.add45_in("phi17"))

  // Wiring constant
  add45.io.RightIO.bits.data := 1.U
  add45.io.RightIO.bits.predicate := true.B
  add45.io.RightIO.valid := true.B

  /**
    * Connecting Dataflow signals
    */
  ret47.io.In.elements("field0").bits.data := 1.U
  ret47.io.In.elements("field0").bits.predicate := true.B
  ret47.io.In.elements("field0").valid := true.B
  io.out <> ret47.io.Out

/*
  when (lb_L_0.io.endEnable.fire() && lb_L_0.io.endEnable.bits.control) {
    printf("DEBUG: Loop end ID=%d\n",lb_L_0.io.endEnable.bits.taskID)
  }
  when (load5.io.Out(0).fire()) {printf("DEBUG: iBeg =%d\n",load5.io.Out(0).bits.data)}
  when (load7.io.Out(0).fire()) {printf("DEBUG: iMid =%d\n",load7.io.Out(0).bits.data)}
  when (load9.io.Out(0).fire()) {printf("DEBUG: iEnd =%d\n",load9.io.Out(0).bits.data)}
  when (phi15.io.Out(0).fire() && phi15.io.Out(0).bits.predicate) {printf("DEBUG: i=%d\n",phi15.io.Out(0).bits.data)}
  when (phi16.io.Out(0).fire() && phi16.io.Out(0).bits.predicate) {printf("DEBUG: j=%d\n",phi16.io.Out(0).bits.data)}
  when (phi17.io.Out(0).fire() && phi17.io.Out(0).bits.predicate) {printf("DEBUG: k=%d\n",phi17.io.Out(0).bits.data)}

  when (load25.io.Out(0).fire()) {printf("DEBUG: lor_lhs A[i]= %d\n",load25.io.Out(0).bits.data)}
  when (load27.io.Out(0).fire()) {printf("DEBUG: lor_lhs A[j]= %d\n",load27.io.Out(0).bits.data)}
  when (load31.io.Out(0).fire()) {printf("DEBUG: if_then A[i]= %d\n",load31.io.Out(0).bits.data)}
  when (load37.io.Out(0).fire()) {printf("DEBUG: if_else A[j]= %d\n",load37.io.Out(0).bits.data)}

  when (br21.io.Out(param.br21_brn_bb("bb_if_else")).fire() && br21.io.Out(param.br21_brn_bb("bb_if_else")).bits.control)   {printf("DEBUG: branch to if_else\n")}
  when (br29.io.Out(param.br29_brn_bb("bb_if_else")).fire() && br21.io.Out(param.br29_brn_bb("bb_if_else")).bits.control)   {printf("DEBUG: branch to if_else\n")}
  when (br23.io.Out(param.br23_brn_bb("bb_if_then")).fire() && br23.io.Out(param.br23_brn_bb("bb_if_then")).bits.control)   {printf("DEBUG: branch to if_then\n")}
  when (br29.io.Out(param.br29_brn_bb("bb_if_then")).fire() && br29.io.Out(param.br29_brn_bb("bb_if_then")).bits.control)   {printf("DEBUG: branch to if_then\n")}

  when (br44.io.Out(0).fire())   {printf("DEBUG: for_inc fired\n")}
*/
}

import java.io.{File, FileWriter}
object mergesort_mergeMain extends App {
  val dir = new File("RTL/mergesort_merge") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new mergesort_mergeDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

