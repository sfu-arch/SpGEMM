package node

import chisel3._
import chisel3.Module
import junctions._

import config._
import interfaces._
import util._
import utility.UniformPrintfs

class CallNodeIO(val argTypes: Seq[Int], val retTypes: Seq[Int])(implicit p: Parameters)
  extends Bundle
{
  val In      = Flipped(new CallDecoupled(argTypes))   // Requests from calling block(s)
  val callOut = Decoupled(new Call(argTypes))          // To task
  val retIn   = Flipped(Decoupled(new Call(retTypes))) // From task
  val Out     = new CallDecoupled(retTypes)            // Returns to calling block(s)
  override def cloneType = new CallNodeIO(argTypes,retTypes).asInstanceOf[this.type]
}

class CallNode(ID: Int, argTypes: Seq[Int], retTypes: Seq[Int])
              (implicit p: Parameters,
               name: sourcecode.Name,
               file: sourcecode.File) extends Module
  with UniformPrintfs {
  override lazy val io = IO(new CallNodeIO(argTypes, retTypes)(p))

  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount,_) = Counter(true.B,32*1024)
  override val printfSigil = module_name + ": " + node_name + ID + " "

  // Combine individually decoupled enable and data into single decoupled call
  val CombineIn = Module(new CombineCall(argTypes))
  CombineIn.io.In <> io.In
  io.callOut <> CombineIn.io.Out

  // Split return enable and arguments into individually decoupled enable and data
  val SplitOut = Module(new SplitCall(retTypes))
  SplitOut.io.In <> io.retIn
  io.Out <> SplitOut.io.Out

  when(CombineIn.io.Out.fire) {
    when (CombineIn.io.Out.bits.enable.control)
    {
      printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name  + ": Output fired @ %d\n", CombineIn.io.Out.bits.enable.taskID, cycleCount)
    }
  }
}
