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


/**
  * Defining LoopOutputBundle
  * @param gen Datatype
  * @tparam T
  */
class LoopOutputBundleIO[+T <: Data](gen: T) extends Bundle(){
  val bits  = Output(gen.cloneType)
  val valid = Output(Bool())
  override def cloneType: this.type = new LoopOutputBundleIO(gen).asInstanceOf[this.type]
}

object LoopOutputBundle{
  def apply[T <: Data](gen: T): LoopOutputBundleIO[T] = new LoopOutputBundleIO(gen)
}

/**
  * @note Loop header IO
  * @param NumInputs Number of inputs
  */
class LoopHeaderIO[T <: Data](val NumInputs: Int, val NumOuts: Int)
                             (gen: T)(implicit p: Parameters) extends CoreBundle()(p) {

  val inputArg  = Vec(NumInputs, Flipped(Decoupled(gen)))
  val outputArg = Vec(NumOuts, Decoupled(gen))

  /**
    * @note This is an example of how to build custom IO
    */
  //  val outputArg = Vec(NumOuts,LoopOutputBundle(gen))

  /**
    * Finish signal comes from Ret instruction
    */
  val Finish = Input(Bool())

  /**
    * @todo connect the START to entry basic block
    */
  val Start = Output(Bool())

  override def cloneType =
    new LoopHeaderIO(NumInputs, NumOuts)(gen).asInstanceOf[this.type]


}



class LoopHeader(val NumInputs: Int, val NumOuts: Int, val ID: Int)
                (implicit val p: Parameters) extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new LoopHeaderIO(NumInputs, NumOuts)(new DataBundle()))

  val valids = WireInit(VecInit(Seq.fill(NumInputs){false.B}))

  val Args = for (i <- 0 until NumInputs) yield {
    val arg = Module(new LoopElement(ID = i))
    arg
  }

  //Iterating over each loopelement and connect them to the IO
  for (i <- 0 until NumInputs) {
    Args(i).io.inData <> io.inputArg(i)
    Args(i).io.Finish <> io.Finish
  }

  for (i <- 0 until NumOuts) {
    io.outputArg(i).bits  <> Args(i).io.outData.data
    io.outputArg(i).valid <> Args(i).io.outData.valid
  }

  for (i <- 0 until NumInputs) {
    valids(i) <> io.inputArg(i).valid
  }

  io.Start := valids.asUInt().andR()


}
