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

abstract class dedup_S3DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class dedup_S3DF(implicit p: Parameters) extends dedup_S3DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=1, NWrites=2)
		 (WControl=new WriteMemoryController(NumOps=2, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=1, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,1,1,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 7, BID = 0))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %arrayidx = getelementptr inbounds i32, i32* %chunk, i32 1
  val Gep_arrayidx0 = Module(new GepArrayOneNode(NumOuts=1, ID=0)(numByte=4)(size=1))

  //  store i32 2, i32* %arrayidx, align 4
  val st_1 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=1, RouteID=0))

  //  %arrayidx1 = getelementptr inbounds i32, i32* %q, i32 %wptr
  val Gep_arrayidx12 = Module(new GepArrayOneNode(NumOuts=1, ID=2)(numByte=4)(size=1))

  //  store volatile i32 %pos, i32* %arrayidx1, align 4
  val st_3 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=3, RouteID=1))

  //  ret void
  val ret_4 = Module(new RetNode(retTypes=List(32), ID = 4))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 1
  val const0 = Module(new ConstNode(value = 1, NumOuts = 1, ID = 0))

  //i32 2
  val const1 = Module(new ConstNode(value = 2, NumOuts = 1, ID = 1))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



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

  st_1.io.enable <> bb_entry0.io.Out(3)

  Gep_arrayidx12.io.enable <> bb_entry0.io.Out(4)

  st_3.io.enable <> bb_entry0.io.Out(5)

  ret_4.io.enable <> bb_entry0.io.Out(6)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.WriteIn(0) <> st_1.io.memReq

  st_1.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_3.io.memReq

  st_3.io.memResp <> MemCtrl.io.WriteOut(1)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  Gep_arrayidx0.io.idx1 <> const0.io.Out(0)

  st_1.io.inData <> const1.io.Out(0)

  st_1.io.GepAddr <> Gep_arrayidx0.io.Out(0)

  st_3.io.GepAddr <> Gep_arrayidx12.io.Out(0)

  ret_4.io.In("field0") <> st_3.io.Out(0)

  Gep_arrayidx0.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(0)

  st_3.io.inData <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_arrayidx12.io.idx1 <> InputSplitter.io.Out.data.elements("field2")(0)

  Gep_arrayidx12.io.baseAddress <> InputSplitter.io.Out.data.elements("field3")(0)

  st_1.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_4.io.Out

}

import java.io.{File, FileWriter}
object dedup_S3Main extends App {
  val dir = new File("RTL/dedup_S3") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new dedup_S3DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
