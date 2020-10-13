package dnn

import FPU._
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.BinaryPoint
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import dnn.types.TwoOperand
import interfaces._
import muxes._
import util._
import utility.UniformPrintfs


class NCycle_Dot[T <: Data : TwoOperand.OperatorTwoOperand](val gen: T, val N: Int, val lanes: Int, val opcode: String)(implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val input_left_vec  = Input(Vec(N, UInt(xlen.W)))
    val input_right_vec = Input(Vec(N, UInt(xlen.W)))
    val activate        = Input(Bool( ))
    val stat            = Output(UInt(xlen.W))
    val valid           = Output(Bool( ))
    val output          = Output(Vec(lanes, UInt(xlen.W)))
  })

  require(gen.getWidth == xlen, "Size of element does not match xlen OR Size of vector does not match shape")
  require(N % lanes == 0, "Size of vector should be multiple of lanes")

  def latency(): Int = {
    N / lanes + 1
  }

  val PEs =
    for (i <- 0 until lanes) yield {
      Module(new TwoOperand_PE(gen, opcode))
    }

  /* PE Control */
  val s_idle :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)
  val input_steps                            = new Counter(N / lanes)
  io.stat := input_steps.value
  io.valid := (state === s_ACTIVE)
  when(state === s_idle) {
    when(io.activate) {
      state := s_ACTIVE
    }
  }.elsewhen(state === s_ACTIVE) {
    input_steps.inc( )
    when(input_steps.value === (N / lanes - 1).U) {
      state := s_idle
    }
  }

  val left_inputs = for (i <- 0 until lanes) yield {
    for (j <- 0 until N / lanes) yield {
      j.U -> io.input_left_vec(i + j * lanes)
    }
  }

  val right_inputs = for (i <- 0 until lanes) yield {
    for (j <- 0 until N / lanes) yield {
      j.U -> io.input_right_vec(i + j * lanes)
    }
  }

  val left_muxes = for (i <- 0 until lanes) yield {
    val mx = MuxLookup(input_steps.value, 0.U, left_inputs(i))
    mx
  }

  val right_muxes = for (i <- 0 until lanes) yield {
    val mx = MuxLookup(input_steps.value, 0.U, right_inputs(i))
    mx
  }


  for (i <- 0 until lanes) {
    PEs(i).io.left.bits := left_muxes(i)
    PEs(i).io.right.bits := right_muxes(i)
    PEs(i).io.left.valid := false.B
    PEs(i).io.right.valid := false.B
  }

  when(state === s_ACTIVE) {
    for (i <- 0 until lanes) {
      PEs(i).io.left.valid := true.B
      PEs(i).io.right.valid := true.B
    }
  }

  //printf(p"\n ${input_steps.value}")
  //  printf(p"1.U, 1.U ${
  //    Hexadecimal(PEs(0).io.out.bits)
  //  }")
  for (i <- 0 until lanes) {
    io.output(i) <> PEs(i).io.out.bits
    when(state === s_idle) {
      PEs(i).reset := true.B
    }
  }
}