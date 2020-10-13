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

object Data_cilk_spawn_test02_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_det_achd_pred = Map(
    "detach2" -> 0
  )


  val bb_det_cont_pred = Map(
    "detach2" -> 0
  )


  val bb_det_achd1_pred = Map(
    "detach6" -> 0
  )


  val bb_det_cont3_pred = Map(
    "detach6" -> 0
  )


  val detach2_brn_bb = Map(
    "bb_det_achd" -> 0,
    "bb_det_cont" -> 1
  )


  val detach6_brn_bb = Map(
    "bb_det_achd1" -> 0,
    "bb_det_cont3" -> 1
  )


  val bb_entry_activate = Map(
    "alloca0" -> 0,
    "alloca1" -> 1,
    "detach2" -> 2
  )


  val bb_det_achd_activate = Map(
    "add3" -> 0,
    "store4" -> 1,
    "reattach5" -> 2
  )


  val bb_det_cont_activate = Map(
    "detach6" -> 0
  )


  val bb_det_achd1_activate = Map(
    "add7" -> 0,
    "store8" -> 1,
    "reattach9" -> 2
  )


  val bb_det_cont3_activate = Map(
    "sync10" -> 0
  )


  val bb_sync_continue_activate = Map(
    "load11" -> 0,
    "load12" -> 1,
    "add13" -> 2,
    "ret14" -> 3
  )


  //  %x = alloca i32, align 4, !UID !7, !ScalaLabel !8
  val alloca0_in = Map(

  )


  //  %y = alloca i32, align 4, !UID !9, !ScalaLabel !10
  val alloca1_in = Map(

  )


  //  detach label %det.achd, label %det.cont, !UID !11, !BB_UID !12, !ScalaLabel !13
  val detach2_in = Map(
    "" -> 0,
    "" -> 1
  )


  //  %add = add nsw i32 %a, 5, !UID !14, !ScalaLabel !15
  val add3_in = Map(
    "field0" -> 0
  )


  //  store i32 %add, i32* %x, align 4, !UID !16, !ScalaLabel !17
  val store4_in = Map(
    "add3" -> 0,
    "alloca0" -> 0
  )


  //  reattach label %det.cont, !UID !18, !BB_UID !19, !ScalaLabel !20
  val reattach5_in = Map(
    "" -> 2
  )


  //  detach label %det.achd1, label %det.cont3, !UID !21, !BB_UID !22, !ScalaLabel !23
  val detach6_in = Map(
    "" -> 3,
    "" -> 4
  )


  //  %add2 = add nsw i32 %b, 5, !UID !24, !ScalaLabel !25
  val add7_in = Map(
    "field1" -> 0
  )


  //  store i32 %add2, i32* %y, align 4, !UID !26, !ScalaLabel !27
  val store8_in = Map(
    "add7" -> 0,
    "alloca1" -> 0
  )


  //  reattach label %det.cont3, !UID !28, !BB_UID !29, !ScalaLabel !30
  val reattach9_in = Map(
    "" -> 5
  )


  //  sync label %sync.continue, !UID !31, !BB_UID !32, !ScalaLabel !33
  val sync10_in = Map(
    "" -> 6
  )


  //  %0 = load i32, i32* %x, align 4, !UID !34, !ScalaLabel !35
  val load11_in = Map(
    "alloca0" -> 1
  )


  //  %1 = load i32, i32* %y, align 4, !UID !36, !ScalaLabel !37
  val load12_in = Map(
    "alloca1" -> 1
  )


  //  %add4 = add nsw i32 %0, %1, !UID !38, !ScalaLabel !39
  val add13_in = Map(
    "load11" -> 0,
    "load12" -> 0
  )


  //  ret i32 %add4, !UID !40, !BB_UID !41, !ScalaLabel !42
  val ret14_in = Map(
    "add13" -> 0
  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class cilk_spawn_test02DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32,32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class cilk_spawn_test02DF(implicit p: Parameters) extends cilk_spawn_test02DFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */


	val StackPointer = Module(new Stack(NumOps = 2))

	val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=2,NWrites=2)
		            (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2)))

	val CacheMem = Module(new UnifiedController(ID=0,Size=32,NReads=2,NWrites=2)
		            (WControl=new WriteMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
		            (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> CacheMem.io.MemReq
  CacheMem.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCall(List(32,32)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  //Function doesn't have any loop


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_det_achd = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 1))

  val bb_det_cont = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 1, BID = 2))

  val bb_det_achd1 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 3))

  val bb_det_cont3 = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 1, BID = 4))

  val bb_sync_continue = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 4, BID = 5))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %x = alloca i32, align 4, !UID !7, !ScalaLabel !8
  val alloca0 = Module(new AllocaNode(NumOuts=2, RouteID=0, ID=0))


  //  %y = alloca i32, align 4, !UID !9, !ScalaLabel !10
  val alloca1 = Module(new AllocaNode(NumOuts=2, RouteID=1, ID=1))


  //  detach label %det.achd, label %det.cont, !UID !11, !BB_UID !12, !ScalaLabel !13
  val detach2 = Module(new Detach(ID = 2))

  // [BasicBlock]  det.achd:

  //  %add = add nsw i32 %a, 5, !UID !14, !ScalaLabel !15
  val add3 = Module (new ComputeNode(NumOuts = 1, ID = 3, opCode = "add")(sign=false))


  //  store i32 %add, i32* %x, align 4, !UID !16, !ScalaLabel !17
  val store4 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=4,RouteID=0))


  //  reattach label %det.cont, !UID !18, !BB_UID !19, !ScalaLabel !20
  val reattach5 = Module(new Reattach(NumPredOps=1, ID=5))

  // [BasicBlock]  det.cont:

  //  detach label %det.achd1, label %det.cont3, !UID !21, !BB_UID !22, !ScalaLabel !23
  val detach6 = Module(new Detach(ID = 6))

  // [BasicBlock]  det.achd1:

  //  %add2 = add nsw i32 %b, 5, !UID !24, !ScalaLabel !25
  val add7 = Module (new ComputeNode(NumOuts = 1, ID = 7, opCode = "add")(sign=false))


  //  store i32 %add2, i32* %y, align 4, !UID !26, !ScalaLabel !27
  val store8 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=8,RouteID=1))


  //  reattach label %det.cont3, !UID !28, !BB_UID !29, !ScalaLabel !30
  val reattach9 = Module(new Reattach(NumPredOps=1, ID=9))

  // [BasicBlock]  det.cont3:

  //  sync label %sync.continue, !UID !31, !BB_UID !32, !ScalaLabel !33
  val sync10 = Module(new Sync(ID = 10, NumOuts = 1, NumInc = 2, NumDec = 2))  // Manually updated

  // [BasicBlock]  sync.continue:

  //  %0 = load i32, i32* %x, align 4, !UID !34, !ScalaLabel !35
  val load11 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=11,RouteID=0))


  //  %1 = load i32, i32* %y, align 4, !UID !36, !ScalaLabel !37
  val load12 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=12,RouteID=1))


  //  %add4 = add nsw i32 %0, %1, !UID !38, !ScalaLabel !39
  val add13 = Module (new ComputeNode(NumOuts = 1, ID = 13, opCode = "add")(sign=false))


  //  ret i32 %add4, !UID !40, !BB_UID !41, !ScalaLabel !42
  val ret14 = Module(new RetNode(retTypes=List(32), ID=14))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_cilk_spawn_test02_FlowParam



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


  // There is no branch instruction


  //Connecting detach2 to bb_det_achd
  bb_det_achd.io.predicateIn <> detach2.io.Out(param.detach2_brn_bb("bb_det_achd"))


  //Connecting detach2 to bb_det_cont
  bb_det_cont.io.predicateIn <> detach2.io.Out(param.detach2_brn_bb("bb_det_cont"))


  //Connecting detach6 to bb_det_achd1
  bb_det_achd1.io.predicateIn <> detach6.io.Out(param.detach6_brn_bb("bb_det_achd1"))


  //Connecting detach6 to bb_det_cont3
  bb_det_cont3.io.predicateIn <> detach6.io.Out(param.detach6_brn_bb("bb_det_cont3"))

  bb_sync_continue.io.predicateIn <> sync10.io.Out(0) // Manually added



  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  alloca0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca0"))

  alloca1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca1"))

  detach2.io.enable <> bb_entry.io.Out(param.bb_entry_activate("detach2"))



  add3.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("add3"))

  store4.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("store4"))

  reattach5.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("reattach5"))



  detach6.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("detach6"))



  add7.io.enable <> bb_det_achd1.io.Out(param.bb_det_achd1_activate("add7"))

  store8.io.enable <> bb_det_achd1.io.Out(param.bb_det_achd1_activate("store8"))

  reattach9.io.enable <> bb_det_achd1.io.Out(param.bb_det_achd1_activate("reattach9"))



  sync10.io.enable <> bb_det_cont3.io.Out(param.bb_det_cont3_activate("sync10"))



  load11.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("load11"))

  load12.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("load12"))

  add13.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("add13"))

  ret14.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("ret14"))





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

  // Wiring Alloca instructions with Static inputs
  alloca0.io.allocaInputIO.bits.size      := 1.U
  alloca0.io.allocaInputIO.bits.numByte   := 4.U
  alloca0.io.allocaInputIO.bits.predicate := true.B
  alloca0.io.allocaInputIO.bits.valid     := true.B
  alloca0.io.allocaInputIO.valid          := true.B  // Manually added

  // Connecting Alloca to Stack
  StackPointer.io.InData(0) <> alloca0.io.allocaReqIO
  alloca0.io.allocaRespIO <> StackPointer.io.OutData(0)


  // Wiring Alloca instructions with Static inputs
  alloca1.io.allocaInputIO.bits.size      := 1.U
  alloca1.io.allocaInputIO.bits.numByte   := 4.U
  alloca1.io.allocaInputIO.bits.predicate := true.B
  alloca1.io.allocaInputIO.bits.valid     := true.B
  alloca1.io.allocaInputIO.valid          := true.B  // Manually added

  // Connecting Alloca to Stack
  StackPointer.io.InData(1) <> alloca1.io.allocaReqIO
  alloca1.io.allocaRespIO <> StackPointer.io.OutData(1)


  // Wiring Binary instruction to the function argument
  add3.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")

  // Wiring constant
  add3.io.RightIO.bits.data := 5.U
  add3.io.RightIO.bits.predicate := true.B
  add3.io.RightIO.valid := true.B

  store4.io.inData <> add3.io.Out(param.store4_in("add3"))



  // Wiring Store instruction to the parent instruction
  store4.io.GepAddr <> alloca0.io.Out(param.store4_in("alloca0"))
  store4.io.memResp  <> CacheMem.io.WriteOut(0)
  CacheMem.io.WriteIn(0) <> store4.io.memReq
  //store4.io.Out(0).ready := true.B // Manually removed

  // Reattach (Manual add)
  reattach5.io.predicateIn(0) <> store4.io.Out(0)

  // Wiring Binary instruction to the function argument
  add7.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")

  // Wiring constant
  add7.io.RightIO.bits.data := 5.U
  add7.io.RightIO.bits.predicate := true.B
  add7.io.RightIO.valid := true.B

  store8.io.inData <> add7.io.Out(param.store8_in("add7"))



  // Wiring Store instruction to the parent instruction
  store8.io.GepAddr <> alloca1.io.Out(param.store8_in("alloca1"))
  store8.io.memResp  <> CacheMem.io.WriteOut(1)
  CacheMem.io.WriteIn(1) <> store8.io.memReq
  //store8.io.Out(0).ready := true.B // Manually removed

  // Reattach (Manual add)
  reattach9.io.predicateIn(0) <> store8.io.Out(0)


  // Wiring Load instruction to another instruction
  load11.io.GepAddr <> alloca0.io.Out(param.load11_in("alloca0")) // Manually corrected
  load11.io.memResp <> CacheMem.io.ReadOut(0)
  CacheMem.io.ReadIn(0) <> load11.io.memReq



  // Wiring Load instruction to another instruction
  load12.io.GepAddr <> alloca1.io.Out(param.load12_in("alloca1")) // Manually corrected
  load12.io.memResp <> CacheMem.io.ReadOut(1)
  CacheMem.io.ReadIn(1) <> load12.io.memReq



  // Wiring instructions
  add13.io.LeftIO <> load11.io.Out(param.add13_in("load11"))

  // Wiring instructions
  add13.io.RightIO <> load12.io.Out(param.add13_in("load12"))

  // Sync (Manual add)
  sync10.io.incIn(0) <> detach2.io.Out(2)
  sync10.io.decIn(0) <> reattach5.io.Out(0)
  sync10.io.incIn(1) <> detach6.io.Out(2)
  sync10.io.decIn(1) <> reattach9.io.Out(0)

  // Wiring return instruction
  
  
  
  ret14.io.In.elements("field0") <> add13.io.Out(param.ret14_in("add13"))
  io.out <> ret14.io.Out


}

import java.io.{File, FileWriter}
object cilk_spawn_test02Main extends App {
  val dir = new File("RTL/cilk_spawn_test02") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_spawn_test02DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

