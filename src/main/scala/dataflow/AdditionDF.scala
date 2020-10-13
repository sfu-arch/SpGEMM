package dataflow

import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import org.scalatest.{FlatSpec, Matchers}
import muxes._
import config._
import control.{BasicBlockNoMaskNode, BasicBlockNode}
import util._
import interfaces._
import regfile._
import node._


/**
  * This Object should be initialize at the first step
  * It contains all the transformation from indecies to their module's name
  */
object functionParam {

  val b0_entry_pred = Map(
    "active" -> 0
  )

  val b1_then_pred = Map(
    "m1" -> 0
  )

  val b2_end_pred = Map(
    "m1" -> 0,
    "m3" -> 1
  )

  val m1_brn_bb = Map(
    "b1_then" -> 0,
    "b2_end" -> 1
  )

  val m3_ubrn_bb = Map(
    "b2_end" -> 0
  )

  val b0_entry_activate = Map(
    "m0" -> 0,
    "m1" -> 1
  )

  val b1_then_activate = Map(
    "m2" -> 0,
    "m3" -> 1
  )

  val b2_end_activate = Map(
    "m4" -> 0,
    "m5" -> 1
  )

  val m4_phi_in = Map(
    "const1" -> 0,
    "m2" -> 1
  )

}

//TODO uncomment if you remove StackCentral.scala file
//
abstract class AddDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val Data0 = Flipped(Decoupled(new DataBundle))
    val pred = Decoupled(new ControlBundle)
    val result = Decoupled(new DataBundle)
  })
}

class AddDF(implicit p: Parameters) extends AddDFIO() {


  /**
    * @note Module's variables they should set during initialization
    */
  //BasicBlock
  val b0_entry = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 0))
  val b1_then = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 2, BID = 1))
  val b2_end = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 2, NumPhi = 1, BID = 2))

  //Compute
  val m0 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "EQ")(sign = false))
  val m1 = Module(new CBranchNode(ID = 1))

  val m2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "Add")(sign = false))
  val m3 = Module(new UBranchNode(ID = 3))

  val m4 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 4))
  val m5 = Module(new ComputeNode(NumOuts = 1, ID = 5, opCode = "Add")(sign = false))

  /**
    * Instantiating parameters
    */
  val param = functionParam

  /**
    * Wireing control signals from BasicBlock nodes
    * to their child
    */

  //Grounding entry BasicBlock
  b0_entry.io.predicateIn.bits.control := true.B
  b0_entry.io.predicateIn.bits.taskID := 0.U
  b0_entry.io.predicateIn.valid := true.B

  /**
    * Connecting basic blocks to predicate instructions
    */
  //Connecting m1 to b1_then
  b1_then.io.predicateIn <> m1.io.Out(param.m1_brn_bb("b1_then"))

  //Connecting m1 to b2_end
  b2_end.io.predicateIn(param.b2_end_pred("m1")) <> m1.io.Out(param.m1_brn_bb("b2_end"))

  //Connecting m3 to b2_end
  b2_end.io.predicateIn(param.b2_end_pred("m3")) <> m3.io.Out(param.m3_ubrn_bb("b2_end"))


  /**
    * Wireing enable signals to the instructions
    */
  //Wiring enable signals
  m0.io.enable <> b0_entry.io.Out(param.b0_entry_activate("m0"))
  m1.io.enable <> b0_entry.io.Out(param.b0_entry_activate("m1"))

  m2.io.enable <> b1_then.io.Out(param.b1_then_activate("m2"))
  m3.io.enable <> b1_then.io.Out(param.b1_then_activate("m3"))

  m4.io.enable <> b2_end.io.Out(param.b2_end_activate("m4"))
  m5.io.enable <> b2_end.io.Out(param.b2_end_activate("m5"))

  /**
    * Connecting PHI nodes
    */
  //Connect PHI node
  m4.io.InData(param.m4_phi_in("m2")) <> m2.io.Out(0)
  //  m4.io.InData(param.m4_phi_in("const1")) <> io.Data0
  m4.io.InData(param.m4_phi_in("const1")).bits.data := 0.U
  m4.io.InData(param.m4_phi_in("const1")).bits.predicate := true.B
  // //   m4.io.InData(param.m4_phi_in("const1")).bits.valid := true.B
  m4.io.InData(param.m4_phi_in("const1")).valid := true.B
  m4.io.InData(param.m4_phi_in("const1")).bits.taskID := 0.U


  m4.io.Mask <> b2_end.io.MaskBB(0)
  //  m4.io.Mask <> b1_then.io.MaskBB(0)


  /**
    * Connecting Dataflow signals
    */
  //dataflow signal
  m0.io.LeftIO <> io.Data0
  m1.io.CmpIO <> m0.io.Out(0)
  m2.io.LeftIO <> io.Data0

  //Connecting add
  m5.io.LeftIO <> m4.io.Out(0)


  /**
    * Wireing constants
    */
  m0.io.RightIO.bits.data := 9.U
  m0.io.RightIO.bits.predicate := true.B
  m0.io.RightIO.bits.taskID := 0.U
  // //   m0.io.RightIO.bits.valid := true.B
  m0.io.RightIO.valid := true.B

  m2.io.RightIO.bits.data := 5.U
  m2.io.RightIO.bits.predicate := true.B
  m2.io.RightIO.bits.taskID := 0.U
  m2.io.RightIO.valid := true.B

  m0.io.RightIO.bits.data := 9.U
  m0.io.RightIO.bits.predicate := true.B

  // //   m0.io.RightIO.bits.valid := true.B
  m0.io.RightIO.valid := true.B

  m5.io.RightIO.bits.data := 4.U
  m5.io.RightIO.bits.predicate := true.B
  m5.io.RightIO.bits.taskID := 0.U
  // //   m5.io.RightIO.bits.valid := true.B
  m5.io.RightIO.valid := true.B

  //Output
  io.result <> m5.io.Out(0)

  //DEBUG
  io.pred <> b1_then.io.Out(0)

}
