package loop

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import interfaces._
import muxes._
import util._
import node._
import utility.UniformPrintfs


class LoopExampleIO[T <: Data](val ID: Int)(gen: T)(implicit p: Parameters) extends CoreBundle(){

  val Input1 = Flipped(Decoupled(gen))
  val Input2 = Flipped(Decoupled(gen))
  val Input3 = Flipped(Decoupled(gen))
  val Input4 = Flipped(Decoupled(gen))

  val Enable = Flipped(Decoupled(new ControlBundle))

  val Finish = Input(Bool())

  val Result = Decoupled(gen)

  override def cloneType = new LoopExampleIO(ID)(gen).asInstanceOf[this.type]
}


class LoopExample(val NumInputs: Int, val ID: Int)
                (implicit val p: Parameters) extends Module with CoreParams with UniformPrintfs{

  lazy val io = IO(new LoopExampleIO(ID)(new DataBundle()))

  val head = Module(new LoopHeader(NumInputs = NumInputs, NumOuts = 4, ID = 0))
  val comp1 = Module(new ComputeNode(NumOuts = 1, ID = 1, opCode = "Add")(sign = false))
  val comp2 = Module(new ComputeNode(NumOuts = 1, ID = 2, opCode = "Add")(sign = false))
  val comp3 = Module(new ComputeNode(NumOuts = 1, ID = 3, opCode = "Add")(sign = false))

  comp1.io.enable <> io.Enable
  comp2.io.enable <> io.Enable
  comp3.io.enable <> io.Enable

  head.io.inputArg(0) <> io.Input1
  head.io.inputArg(1) <> io.Input2
  head.io.inputArg(2) <> io.Input3
  head.io.inputArg(3) <> io.Input4

  head.io.Finish <> io.Finish

  comp1.io.LeftIO <> head.io.outputArg(0)
  comp1.io.RightIO <> head.io.outputArg(1)

  comp2.io.LeftIO <> head.io.outputArg(2)
  comp2.io.RightIO <> head.io.outputArg(3)

  comp3.io.LeftIO <> comp1.io.Out(0)
  comp3.io.RightIO <> comp2.io.Out(0)

  io.Result <> comp3.io.Out(0)

}
