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
  * Contain each loop input argument works like register file
  */
class LoopElementIO()(implicit p: Parameters) extends CoreBundle() {

  /**
    * Module input
    */
  val inData = Flipped(Decoupled(CustomDataBundle(UInt(16.W))))
  val Finish = Input(Bool())

  /**
    * Module output
    */
  val outData = new Bundle{
    val data  = Output(CustomDataBundle(UInt(32.W))) // Defaults to UInt(32.W)
    val valid = Output(Bool())
  }
}


class LoopElement(val ID: Int)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  override lazy val io = IO(new LoopElementIO())

  // Printf debugging
  override val printfSigil = "Node ID: " + ID + " "

  /**
    * Always latch the input data
    */
  val data_R = RegNext(io.inData.bits)

  io.outData.data <> data_R

  /**
    * Defining state machines
    */
  val s_INIT :: s_LATCH :: Nil = Enum(2)
  val state = RegInit(s_INIT)

  /**
    * State transision
    */

  when(state === s_INIT){
    io.inData.ready := true.B
    io.outData.valid := false.B

  }.otherwise{
//  }.elsewhen( state === s_LATCH){
    io.inData.ready := false.B
    io.outData.valid := true.B
  }

  when(io.inData.fire()){
    state := s_LATCH
  }

  when(io.Finish){
    state := s_INIT
  }

  /**
    * Debuging info
    */
//  printfInfo(" State: %x\n", state)

}
