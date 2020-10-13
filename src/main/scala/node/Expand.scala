package node

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
import utility.UniformPrintfs

class ExpandNodeIO[T <: Data](NumOuts: Int)(gen: T)
                             (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(gen) {
  val InData = Flipped(Decoupled(gen))

  override def cloneType = new ExpandNodeIO(NumOuts)(gen).asInstanceOf[this.type]
}

class ExpandNode[T <: Data](NumOuts: Int, ID: Int)(gen: T)
                           (implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(gen)(p) {
  override lazy val io = IO(new ExpandNodeIO(NumOuts)(gen))
  // Printf debugging

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val indata_R = RegInit(0.U.asTypeOf(gen))
  val indata_valid_R = RegInit(false.B)

  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()
  val start = indata_valid_R & IsEnableValid()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/


  io.InData.ready := ~indata_valid_R
  when(io.InData.fire()) {
    state := s_LATCH
    indata_R <> io.InData.bits
    indata_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> indata_R
  }

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  when(start & state =/= s_COMPUTE) {
    state := s_COMPUTE
    ValidOut()
  }

  /*==========================================*
   *            Output Handshaking and Reset  *
   *==========================================*/

  when(IsOutReady() & (state === s_COMPUTE)) {

    // Reset data
    //    indata_R := gen.default

    indata_valid_R := false.B

    //Reset state
    state := s_idle
    //Reset output
    Reset()
  }

}

class ExpandFastNode[T <: Data](NumOuts: Int, ID: Int)(gen: T)
                               (implicit val p: Parameters,
                                name: sourcecode.Name,
                                file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new Bundle {
    //Input data
    val InData = Flipped(Decoupled(gen))
    val Out    = Vec(NumOuts, Decoupled(gen))
  })


  /*===========================================*
   *            Registers                      *
   *===========================================*/
  val indata_R = RegInit(0.U.asTypeOf(gen))
  val indata_valid_R = RegInit(false.B)

  val out_valid_R = Seq.fill(NumOuts)(RegInit(false.B))
  val out_fire_R = Seq.fill(NumOuts)(RegInit(false.B))

  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/


  io.InData.ready := ~indata_valid_R
  when(io.InData.fire()) {
    indata_R <> io.InData.bits
    indata_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> indata_R
    io.Out(i).valid <> out_valid_R(i)
  }

  for(i <- 0 until NumOuts){
    when(io.Out(i).fire){
      out_valid_R(i) := false.B
      out_fire_R(i) := true.B
    }
  }

  switch(state){
    is(s_idle){
      when(io.InData.fire) {
        state := s_fire
        out_valid_R.foreach(_ := true.B)
      }
    }
    is(s_fire){
      when(out_fire_R.reduce(_ & _)){
        indata_valid_R := false.B

        out_fire_R foreach(_ := false.B)
      }
    }
  }
}
