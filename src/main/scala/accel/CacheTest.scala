package cache

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

abstract class cacheDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class cacheDF(implicit p: Parameters) extends cacheDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=1, NWrites=1)
		 (WControl=new WriteMemoryController(NumOps=1, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=1, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(1,2,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 0))

  val bb_if_then1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 1))

  val bb_if_else2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 2, BID = 2))

  val bb_if_end3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi=1, BID = 3))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %arrayidx = getelementptr inbounds i32, i32* %a, i32 %i
  val Gep_arrayidx0 = Module(new GepNode(NumIns = 1, NumOuts=2, ID=0)(ElementSize = 4, ArraySize = List()))

  //  br i1 %flag, label %if.then, label %if.else
  val br_1 = Module(new CBranchNodeVariable(ID = 1))

  //  %0 = load i32, i32* %arrayidx, align 4, !tbaa !2
  val ld_2 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=2, RouteID=0))

  //  br label %if.end
  val br_3 = Module(new UBranchNode(ID = 3))

  //  store i32 %i, i32* %arrayidx, align 4, !tbaa !2
  val st_4 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=4, RouteID=0))

  //  br label %if.end
  val br_5 = Module(new UBranchNode(ID = 5))

  //  %value.0 = phi i32 [ %0, %if.then ], [ 0, %if.else ]
  val phi_value_06 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 6, Res = true))

  //  ret i32 %value.0
  val ret_7 = Module(new RetNode2(retTypes=List(32), ID = 7))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_if_then1.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_if_else2.io.predicateIn(0) <> br_1.io.FalseOutput(0)

  bb_if_end3.io.predicateIn(0) <> br_5.io.Out(0)

  bb_if_end3.io.predicateIn(1) <> br_3.io.Out(0)



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

  Gep_arrayidx0.io.enable <> bb_entry0.io.Out(0)

  br_1.io.enable <> bb_entry0.io.Out(1)


  ld_2.io.enable <> bb_if_then1.io.Out(0)

  br_3.io.enable <> bb_if_then1.io.Out(1)


  st_4.io.enable <> bb_if_else2.io.Out(0)

  br_5.io.enable <> bb_if_else2.io.Out(1)


  const0.io.enable <> bb_if_end3.io.Out(0)

  phi_value_06.io.enable <> bb_if_end3.io.Out(1)

  ret_7.io.In.enable <> bb_if_end3.io.Out(2)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi_value_06.io.Mask <> bb_if_end3.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_2.io.memReq

  ld_2.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.WriteIn(0) <> st_4.io.memReq

  st_4.io.memResp <> MemCtrl.io.WriteOut(0)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  phi_value_06.io.InData(1) <> const0.io.Out

  ld_2.io.GepAddr <> Gep_arrayidx0.io.Out(0)

  st_4.io.GepAddr <> Gep_arrayidx0.io.Out(1)

  phi_value_06.io.InData(0) <> ld_2.io.Out(0)

  ret_7.io.In.data("field0") <> phi_value_06.io.Out(0)

  Gep_arrayidx0.io.baseAddress <> InputSplitter.io.Out.data("field0")(0)

  Gep_arrayidx0.io.idx(0) <> InputSplitter.io.Out.data("field1")(0)

  st_4.io.inData <> InputSplitter.io.Out.data("field1")(1)

  br_1.io.CmpIO <> InputSplitter.io.Out.data("field2")(0)

  st_4.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_7.io.Out

}

import java.io.{File, FileWriter}
object cacheMain extends App {
  val dir = new File("RTL/cacheTest") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new cacheDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
