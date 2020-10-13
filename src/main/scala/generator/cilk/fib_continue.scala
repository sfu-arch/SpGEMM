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

object Data_fib_continue_FlowParam{

  val bb_entry_pred = Map(
    "active" -> 0
  )


  val bb_entry_activate = Map(
    "load0" -> 0,
    "load1" -> 1,
    "add2" -> 2,
    "load3" -> 3,
    "store4" -> 4,
    "ret5" -> 5
  )


  //  %0 = load i32, i32* %x, align 4, !UID !2, !ScalaLabel !3
  val load0_in = Map(
    "field0" -> 0
  )


  //  %1 = load i32, i32* %y, align 4, !UID !4, !ScalaLabel !5
  val load1_in = Map(
    "field1" -> 0
  )


  //  %add = add nsw i32 %0, %1, !UID !6, !ScalaLabel !7
  val add2_in = Map(
    "load0" -> 0,
    "load1" -> 0
  )


  //  %2 = load i32*, i32** %r, align 4, !UID !8, !ScalaLabel !9
  val load3_in = Map(
    "field2" -> 0
  )


  //  store i32 %add, i32* %2, align 4, !UID !10, !ScalaLabel !11
  val store4_in = Map(
    "add2" -> 0,
    "load3" -> 0
  )


  //  ret void, !UID !12, !BB_UID !13, !ScalaLabel !14
  val ret5_in = Map(

  )


}




  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */


abstract class fib_continueDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32,32,32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}




  /* ================================================================== *
   *                   PRINTING MODULE DEFINITION                       *
   * ================================================================== */


class fib_continueDF(implicit p: Parameters) extends fib_continueDFIO()(p) {



  /* ================================================================== *
   *                   PRINTING MEMORY SYSTEM                           *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0,Size=64*1024,NReads=3,NWrites=1)
                (WControl=new WriteMemoryController(NumOps=1,BaseSize=2,NumEntries=2))
                (RControl=new ReadMemoryController(NumOps=3,BaseSize=4,NumEntries=3))
                (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,1,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */


  //Function doesn't have any loop


  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */


  //Initializing BasicBlocks: 

  val bb_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 6, BID = 0))






  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */


  //Initializing Instructions: 

  // [BasicBlock]  entry:

  //  %0 = load i32, i32* %x, align 4, !UID !2, !ScalaLabel !3
  val load0 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=0,RouteID=0))


  //  %1 = load i32, i32* %y, align 4, !UID !4, !ScalaLabel !5
  val load1 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=1,RouteID=1))


  //  %add = add nsw i32 %0, %1, !UID !6, !ScalaLabel !7
  val add2 = Module (new ComputeNode(NumOuts = 1, ID = 2, opCode = "add")(sign=false))


  //  %2 = load i32*, i32** %r, align 4, !UID !8, !ScalaLabel !9
  val load3 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=3,RouteID=2))


  //  store i32 %add, i32* %2, align 4, !UID !10, !ScalaLabel !11
  val store4 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=4,RouteID=0))


  //  ret void, !UID !12, !BB_UID !13, !ScalaLabel !14
  val ret5 = Module(new RetNode(retTypes=List(32), ID=5))





  /* ================================================================== *
   *                   INITIALIZING PARAM                               *
   * ================================================================== */


  /**
    * Instantiating parameters
    */
  val param = Data_fib_continue_FlowParam



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



  // There is no detach instruction



  // There is no detach instruction




  /* ================================================================== *
   *                   CONNECTING BASIC BLOCKS TO INSTRUCTIONS          *
   * ================================================================== */


  /**
    * Wiring enable signals to the instructions
    */

  load0.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load0"))

  load1.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load1"))

  add2.io.enable <> bb_entry.io.Out(param.bb_entry_activate("add2"))

  load3.io.enable <> bb_entry.io.Out(param.bb_entry_activate("load3"))

  store4.io.enable <> bb_entry.io.Out(param.bb_entry_activate("store4"))

  ret5.io.enable <> bb_entry.io.Out(param.bb_entry_activate("ret5"))





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

  // Wiring Load instruction to the function argument
  load0.io.GepAddr <>  InputSplitter.io.Out.data.elements("field0")(0)
  load0.io.memResp <> MemCtrl.io.ReadOut(0)
  MemCtrl.io.ReadIn(0) <> load0.io.memReq



  // Wiring Load instruction to the function argument
  load1.io.GepAddr <>  InputSplitter.io.Out.data.elements("field1")(0)
  load1.io.memResp <> MemCtrl.io.ReadOut(1)
  MemCtrl.io.ReadIn(1) <> load1.io.memReq



  // Wiring instructions
  add2.io.LeftIO <> load0.io.Out(param.add2_in("load0"))

  // Wiring instructions
  add2.io.RightIO <> load1.io.Out(param.add2_in("load1"))

  // Wiring Load instruction to the function argument
  load3.io.GepAddr <>  InputSplitter.io.Out.data.elements("field2")(0)
  load3.io.memResp <> MemCtrl.io.ReadOut(2)
  MemCtrl.io.ReadIn(2) <> load3.io.memReq



  store4.io.inData <> add2.io.Out(param.store4_in("add2"))



  // Wiring Store instruction to the parent instruction
  store4.io.GepAddr <> load3.io.Out(param.store4_in("load3"))
  store4.io.memResp  <> MemCtrl.io.WriteOut(0)
  MemCtrl.io.WriteIn(0) <> store4.io.memReq
  store4.io.Out(0).ready := true.B



  /**
    * Connecting Dataflow signals
    */
  ret5.io.In.elements("field0") <> store4.io.Out(0)
  io.out <> ret5.io.Out


}

import java.io.{File, FileWriter}
object fib_continueMain extends App {
  val dir = new File("RTL/fib_continue") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new fib_continueDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}

