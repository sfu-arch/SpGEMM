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

class NCycle_Reduction[T <: Data : TwoOperand.OperatorTwoOperand](val gen: T, val N: Int, val pipelined: Boolean = false, val opcode: String)
                                                                 (implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val input_vec = Input(Vec(N, UInt(xlen.W)))
    val activate  = Input(Bool( ))
    val stat      = Output(UInt(xlen.W))
    val valid     = Output(Bool( ))
    val output    = Output(UInt(xlen.W))
  })

  require(gen.getWidth == xlen, "Size of element does not match xlen OR Size of vector does not match shape")
  if (N == 2) {
    require(pipelined == false, "Set pipelined to false")
  }

  def latency(): Int = {
    if (!pipelined)
      2
    else
      N
  }

  val PEs =
    for (i <- 0 until N - 1) yield {
      Module(new TwoOperand_PE(gen, opcode))
    }

  val PE_reg_or_wire =
    if (pipelined) {
      for (i <- 1 until N - 1) yield {
        val PE_reg   = RegNext(PEs(i - 1).io.out.bits)
        val PE_valid = RegNext(PEs(i - 1).io.out.valid)
        (PE_reg, PE_valid)
      }
    } else {
      for (i <- 1 until N - 1) yield {
        val PE_wire  = PEs(i - 1).io.out.bits
        val PE_valid = PEs(i - 1).io.out.valid
        (PE_wire, PE_valid)

      }
    }

  /* PE Control */
  val s_idle :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)
  val input_steps                            = new Counter(latency - 1)
  io.stat := input_steps.value


  for (i <- 0 until N - 1) {
    PEs(i).io.left.bits := io.input_vec(i + 1)
    PEs(i).io.left.valid := true.B
  }

  PEs.drop(1) zip PE_reg_or_wire map {
    case (current, prev) => {
      current.io.right.bits := prev._1
      current.io.right.valid := prev._2
    }
  }


  PEs(0).io.left.bits := io.input_vec(1)
  PEs(0).io.left.valid := true.B
  PEs(0).io.right.bits := io.input_vec(0)
  PEs(0).io.right.valid := false.B


  when(state === s_idle) {
    when(io.activate) {
      state := s_ACTIVE
      PEs(0).io.right.valid := true.B
    }
  }.elsewhen(state === s_ACTIVE) {
    input_steps.inc( )
    when(input_steps.value === 0.U) {
    }
    when(input_steps.value === (latency - 2).U) {
      state := s_idle
    }
  }

  //printf(p"\n ${input_steps.value}")
  //  printf(p"1.U, 1.U ${
  //    Hexadecimal(PEs(0).io.out.bits)
  //  }")
  val output_reg   = RegInit(0.U(xlen.W))
  val output_valid = RegNext(PEs(N - 2).io.out.valid)
  when(PEs(N - 2).io.out.valid) {
    output_reg := PEs(N - 2).io.out.bits
  }
  io.output := output_reg
  io.valid := output_valid
}
