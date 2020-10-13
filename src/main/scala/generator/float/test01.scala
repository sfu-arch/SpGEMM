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
import regfile._
import stack._
import util._
import FPU._
import FType._


  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */

abstract class testFP01DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })

}

class testFP01DF(implicit p: Parameters) extends testFP01DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  //Remember if there is no mem operation io memreq/memresp should be grounded
  io.MemReq  <> DontCare
  io.MemResp <> DontCare

  val InputSplitter = Module(new SplitCallNew(List(2,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 0))

  val bb_if_then1 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 1))

  val bb_if_end2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 3, NumPhi=1, BID = 2))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %div = fdiv float %a, 2.000000e+00
  val FP_div0 = Module(new FPDivSqrtNode(NumOuts = 1, ID = 0, opCode = "DIV")(t = S))

  //  %cmp = fcmp ogt float %div, 0x4000CCCCC0000000
  val FPCMP_cmp1 = Module(new FPCompareNode(NumOuts = 1, ID = 1, opCode = ">GT")(t = S))

  //  br i1 %cmp, label %if.then, label %if.end
  val br_2 = Module(new CBranchNode(ID = 2))

  //  %add = fadd float %a, %b
  val FP_add3 = Module(new FPComputeNode(NumOuts = 1, ID = 3, opCode = "add")(t = S))

  //  br label %if.end
  val br_4 = Module(new UBranchNode(ID = 4))

  //  %sum.0 = phi float [ %add, %if.then ], [ 0.000000e+00, %entry ]
  val phi_sum_05 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 5, Res = true))

  //  ret float %sum.0
  val ret_6 = Module(new RetNode(retTypes=List(32), ID = 6))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //float 2.000000e+00
  val const0 = Module(new ConstNode(value = 0x40000000, NumOuts = 1, ID = 0))

  //float 0x4000CCCCC0000000
  val const1 = Module(new ConstNode(value = 0x40066666, NumOuts = 1, ID = 1))

  //float 0.000000e+00
  val const2 = Module(new ConstNode(value = 0x0, NumOuts = 1, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_if_then1.io.predicateIn <> br_2.io.Out(0)

  bb_if_end2.io.predicateIn(0) <> br_2.io.Out(1)

  bb_if_end2.io.predicateIn(1) <> br_4.io.Out(0)


  /*********
    * Floating point unit
    */
  val SharedDiv = Module(new SharedFPU(NumOps = 1, PipeDepth = 32)(t = S))

  SharedDiv.io.InData(0) <> FP_div0.io.FUReq
  FP_div0.io.FUResp <> SharedDiv.io.OutData(0)

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

  FP_div0.io.enable <> bb_entry0.io.Out(2)

  FPCMP_cmp1.io.enable <> bb_entry0.io.Out(3)

  br_2.io.enable <> bb_entry0.io.Out(4)


  FP_add3.io.enable <> bb_if_then1.io.Out(0)

  br_4.io.enable <> bb_if_then1.io.Out(1)


  const2.io.enable <> bb_if_end2.io.Out(0)

  phi_sum_05.io.enable <> bb_if_end2.io.Out(1)

  ret_6.io.enable <> bb_if_end2.io.Out(2)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phi_sum_05.io.Mask <> bb_if_end2.io.MaskBB(0)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  FP_div0.io.b <> const0.io.Out(0)

  FPCMP_cmp1.io.RightIO <> const1.io.Out(0)

  phi_sum_05.io.InData(1) <> const2.io.Out(0)

  FPCMP_cmp1.io.LeftIO <> FP_div0.io.Out(0)

  br_2.io.CmpIO <> FPCMP_cmp1.io.Out(0)

  phi_sum_05.io.InData(0) <> FP_add3.io.Out(0)

  ret_6.io.In.elements("field0") <> phi_sum_05.io.Out(0)

  FP_div0.io.a <> InputSplitter.io.Out.data.elements("field0")(0)

  FP_add3.io.LeftIO <> InputSplitter.io.Out.data.elements("field0")(1)

  FP_add3.io.RightIO <> InputSplitter.io.Out.data.elements("field1")(0)



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_6.io.Out

}

import java.io.{File, FileWriter}
object testFP01Main extends App {
  val dir = new File("RTL/testFP01") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new testFP01DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
