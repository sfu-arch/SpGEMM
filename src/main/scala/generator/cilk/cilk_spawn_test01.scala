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

object Data_cilk_spawn_test01_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_det_achd_pred = Map(
    "detach1" -> 0
  )


  val bb_det_cont_pred = Map(
    "detach1" -> 0
  )


  val detach1_brn_bb = Map(
    "bb_det_achd" -> 0,
    "bb_det_cont" -> 1
  )


  val bb_entry_activate = Map(
    "alloca0" -> 0,
    "detach1" -> 1
  )


  val bb_det_achd_activate = Map(
    "add2" -> 0,
    "store3" -> 1,
    "reattach4" -> 2
  )


  val bb_det_cont_activate = Map(
    "add5" -> 0,
    "sync6" -> 1
  )


  val bb_sync_continue_activate = Map(
    "load7" -> 0,
    "add8" -> 1,
    "ret9" -> 2
  )


  //  %x = alloca i32, align 4, !UID !7, !ScalaLabel !8
  val alloca0_in = Map(

  )


  //  detach label %det.achd, label %det.cont, !UID !9, !BB_UID !10, !ScalaLabel !11
  val detach1_in = Map(
    "" -> 0,
    "" -> 1
  )


  //  %add = add nsw i32 %a, 5, !UID !12, !ScalaLabel !13
  val add2_in = Map(
    "field0" -> 0
  )


  //  store i32 %add, i32* %x, align 4, !UID !14, !ScalaLabel !15
  val store3_in = Map(
    "add2" -> 0,
    "alloca0" -> 0
  )


  //  reattach label %det.cont, !UID !16, !BB_UID !17, !ScalaLabel !18
  val reattach4_in = Map(
    "" -> 2
  )


  //  %add1 = add nsw i32 %b, 5, !UID !19, !ScalaLabel !20
  val add5_in = Map(
    "field1" -> 0
  )


  //  sync label %sync.continue, !UID !21, !BB_UID !22, !ScalaLabel !23
  val sync6_in = Map(
    "" -> 3
  )


  //  %0 = load i32, i32* %x, align 4, !UID !24, !ScalaLabel !25
  val load7_in = Map(
    "alloca0" -> 1
  )


  //  %add2 = add nsw i32 %0, %add1, !UID !26, !ScalaLabel !27
  val add8_in = Map(
    "load7" -> 0,
    "add5" -> 0
  )


  //  ret i32 %add2, !UID !28, !BB_UID !29, !ScalaLabel !30
  val ret9_in = Map(
    "add8" -> 0
  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class cilk_spawn_test01DFIO(implicit val p: Parameters) extends Module with CoreParams {
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


class cilk_spawn_test01DF(implicit p: Parameters) extends cilk_spawn_test01DFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */


	val StackPointer = Module(new Stack(NumOps = 1))

	val RegisterFile = Module(new TypeStackFile(ID=0,Size=32,NReads=1,NWrites=1)
		            (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=1,BaseSize=2,NumEntries=2)))

	val CacheMem = Module(new UnifiedController(ID=0,Size=32,NReads=1,NWrites=1)
		            (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
		            (RControl=new ReadMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
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

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 0))

  val bb_det_achd = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 1))

  val bb_det_cont = Module(new BasicBlockNoMaskNode(NumInputs = 2, NumOuts = 2, BID = 2))

  val bb_sync_continue = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 3))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %x = alloca i32, align 4, !UID !7, !ScalaLabel !8
  val alloca0 = Module(new AllocaNode(NumOuts=2, RouteID=0, ID=0))


  //  detach label %det.achd, label %det.cont, !UID !9, !BB_UID !10, !ScalaLabel !11
  val detach1 = Module(new Detach(ID = 1))

  // [BasicBlock]  det.achd:

  //  %add = add nsw i32 %a, 5, !UID !12, !ScalaLabel !13
  val add2 = Module (new ComputeNode(NumOuts = 1, ID = 2, opCode = "add")(sign=false))


  //  store i32 %add, i32* %x, align 4, !UID !14, !ScalaLabel !15
  val store3 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=3,RouteID=0))


  //  reattach label %det.cont, !UID !16, !BB_UID !17, !ScalaLabel !18
  val reattach4 = Module(new Reattach(NumPredOps=1, ID=4))

  // [BasicBlock]  det.cont:

  //  %add1 = add nsw i32 %b, 5, !UID !19, !ScalaLabel !20
  val add5 = Module (new ComputeNode(NumOuts = 1, ID = 5, opCode = "add")(sign=false))


  //  sync label %sync.continue, !UID !21, !BB_UID !22, !ScalaLabel !23
  val sync6 = Module(new Sync(ID = 6, NumOuts = 1, NumInc = 1, NumDec = 1))

  // [BasicBlock]  sync.continue:

  //  %0 = load i32, i32* %x, align 4, !UID !24, !ScalaLabel !25
  val load7 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=7,RouteID=0))


  //  %add2 = add nsw i32 %0, %add1, !UID !26, !ScalaLabel !27
  val add8 = Module (new ComputeNode(NumOuts = 1, ID = 8, opCode = "add")(sign=false))


  //  ret i32 %add2, !UID !28, !BB_UID !29, !ScalaLabel !30
  val ret9 = Module(new RetNode(retTypes=List(32), ID=9))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_cilk_spawn_test01_FlowParam



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


  //Connecting detach1 to bb_det_achd
  bb_det_achd.io.predicateIn <> detach1.io.Out(param.detach1_brn_bb("bb_det_achd"))


  //Connecting detach1 to bb_det_cont
  bb_det_cont.io.predicateIn <> detach1.io.Out(param.detach1_brn_bb("bb_det_cont"))


  bb_sync_continue.io.predicateIn <> sync6.io.Out(0) // Manually added


  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  alloca0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("alloca0"))

  detach1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("detach1"))



  add2.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("add2"))

  store3.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("store3"))

  reattach4.io.enable <> bb_det_achd.io.Out(param.bb_det_achd_activate("reattach4"))



  add5.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("add5"))

  sync6.io.enable <> bb_det_cont.io.Out(param.bb_det_cont_activate("sync6"))



  load7.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("load7"))

  add8.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("add8"))

  ret9.io.enable <> bb_sync_continue.io.Out(param.bb_sync_continue_activate("ret9"))





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
  alloca0.io.allocaInputIO.bits.valid     := true.B // Redundant and unused
  alloca0.io.allocaInputIO.valid          := true.B  // Manually added

  // Connecting Alloca to Stack
  StackPointer.io.InData(0) <> alloca0.io.allocaReqIO
  alloca0.io.allocaRespIO <> StackPointer.io.OutData(0)


  // Wiring Binary instruction to the function argument
  add2.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")

  // Wiring constant
  add2.io.RightIO.bits.data := 5.U
  add2.io.RightIO.bits.predicate := true.B
  add2.io.RightIO.valid := true.B

  store3.io.inData <> add2.io.Out(param.store3_in("add2"))



  // Wiring Store instruction to the parent instruction
  store3.io.GepAddr <> alloca0.io.Out(param.store3_in("alloca0"))
  store3.io.memResp  <> CacheMem.io.WriteOut(0)
  CacheMem.io.WriteIn(0) <> store3.io.memReq
  //store3.io.Out(0).ready := true.B // Manually removed



  // Wiring Binary instruction to the function argument
  add5.io.LeftIO <> InputSplitter.io.Out.data.elements("field1")

  // Wiring constant
  add5.io.RightIO.bits.data := 5.U
  add5.io.RightIO.bits.predicate := true.B
  add5.io.RightIO.valid := true.B

  // Wiring Load instruction to another instruction
  load7.io.GepAddr <> alloca0.io.Out(param.load7_in("alloca0"))  // Manually corrected. Why was this hardwired?
  load7.io.memResp <> CacheMem.io.ReadOut(0)
  CacheMem.io.ReadIn(0) <> load7.io.memReq



  // Wiring instructions
  add8.io.LeftIO <> load7.io.Out(param.add8_in("load7"))

  // Wiring instructions
  add8.io.RightIO <> add5.io.Out(param.add8_in("add5"))


  // Reattach (Manual add)
  reattach4.io.predicateIn(0) <> store3.io.Out(0)

  // Sync (Manual add)
  sync6.io.incIn(0) <> detach1.io.Out(2)
  sync6.io.decIn(0) <> reattach4.io.Out(0)


  // Wiring return instruction
  
  
  
  ret9.io.In.elements("field0") <> add8.io.Out(param.ret9_in("add8"))
  io.out <> ret9.io.Out


}

import java.io.{File, FileWriter}
object cilk_spawn_test01Main extends App {
  val dir = new File("RTL/cilk_spawn_test01") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cilk_spawn_test01DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

