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


////
class TwoOperand_PE_IO(implicit p: Parameters) extends CoreBundle( )(p) {
  // LeftIO: Left input data for computation
  val left = Input(Valid(UInt(xlen.W)))

  // RightIO: Right input data for computation
  val right = Input(Valid(UInt(xlen.W)))

  val out = Output(Valid(UInt(xlen.W)))

}


class TwoOperand_PE[T <: Data : TwoOperand.OperatorTwoOperand](gen: T, opcode: String)(implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new TwoOperand_PE_IO( ))

  io.out.valid := false.B
  io.out.bits := TwoOperand.binaryop(io.left.bits.asTypeOf(gen), io.right.bits.asTypeOf(gen), opcode).asUInt

  when(io.left.valid & io.right.valid) {
    io.out.valid := true.B
  }

}


class NCycle_SCAL[T <: Data : TwoOperand.OperatorTwoOperand](val gen: T, val N: Int, val lanes: Int, val opcode: String)(implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val input_vec = Input(Vec(N, UInt(xlen.W)))
    val scalar    = Input(UInt(xlen.W))
    val activate  = Input(Bool( ))
    val stat      = Output(UInt(xlen.W))
    val valid     = Output(Bool( ))
    val output    = Output(Vec(lanes, UInt(xlen.W)))
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

  val io_inputs = for (i <- 0 until lanes) yield {
    for (j <- 0 until N / lanes) yield {
      j.U -> io.input_vec(i + j * lanes)
    }
  }

  val input_muxes = for (i <- 0 until lanes) yield {
    val mx = MuxLookup(input_steps.value, 0.U, io_inputs(i))
    mx
  }

  for (i <- 0 until lanes) {
    PEs(i).io.left.bits := input_muxes(i)
    PEs(i).io.right.bits := io.scalar
    PEs(i).io.left.valid := false.B
    PEs(i).io.right.valid := false.B
  }

  when(state === s_ACTIVE) {
    for (i <- 0 until lanes) {
      PEs(i).io.left.valid := true.B
      PEs(i).io.right.valid := true.B
    }
  }

  for (i <- 0 until lanes) {
    io.output(i) <> PEs(i).io.out.bits
    when(state === s_idle) {
      PEs(i).reset := true.B
    }
  }
}