package accel

import FPU._
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
import regfile._
import stack._
import util._


  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */

abstract class prefetchDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)

    val PreReq = Decoupled(new MemReq)

    val out = Decoupled(new Call(List(32)))
  })
}

class prefetchDF(implicit p: Parameters) extends prefetchDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=1, NWrites=1)
		 (WControl=new WriteMemoryController(NumOps=1, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=1, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))


  val InputSplitter = Module(new SplitCallNew(List(2)))
  InputSplitter.io.In <> io.in


  //Delaying logic
  val queues =
    for (i <- 0 until 5) yield {
      val qe = Module(new Queue(new DataBundle, 1))
      qe
    }

  for (i <- 0 until 4) {
    queues(i + 1).io.enq <> queues(i).io.deq
  }

  queues(0).io.enq <> InputSplitter.io.Out.data("field0")(0)

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp


  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 3, BID = 0))

  //  %0 = load i32, i32* %arrayidx, align 4, !tbaa !2
  val ld_2 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=2, RouteID=0))

  // New prefetchnode
  val pf_ld_2 = Module(new PrefetchLoad(NumOuts = 1, ID = 555))

  //  ret i32 %value.0
  val ret_7 = Module(new RetNode2(retTypes=List(32), ID = 7))

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  io.PreReq <> pf_ld_2.io.memReq
  pf_ld_2.io.Out(0).ready := true.B

  ld_2.io.enable <> bb_entry0.io.Out(0)
  ret_7.io.In.enable <> bb_entry0.io.Out(1)
  pf_ld_2.io.enable <> bb_entry0.io.Out(2)

  MemCtrl.io.ReadIn(0) <> ld_2.io.memReq
  ld_2.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> DontCare
  MemCtrl.io.WriteOut(0) <> DontCare

  //  ld_2.io.GepAddr <> InputSplitter.io.Out.data("field0")(0)

  ld_2.io.GepAddr <> queues(4).io.deq
  pf_ld_2.io.GepAddr <> InputSplitter.io.Out.data("field0")(1)

  ret_7.io.In.data("field0") <> ld_2.io.Out(0)

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}
object prefetchMain extends App {
  val dir = new File("RTL/prefetchTest") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new prefetchDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
