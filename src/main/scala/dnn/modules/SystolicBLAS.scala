package dnn

import FPU.{FPMAC, FType}
import breeze.linalg.*
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.BinaryPoint
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import dnn.types.MAC
import interfaces._
import muxes._
import util._
import utility.UniformPrintfs


class SystolicBLAS[T <: Data : MAC.OperatorMAC](gen: T, val M: Int, val K: Int, val N: Int)(implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val left        = Input(Vec(M * K, UInt(xlen.W)))
    val right       = Input(Vec(K * N, UInt(xlen.W)))
    val activate    = Input(Bool( ))
    val async_reset = Input(Bool( ))
    val output      = Output(Vec(M * N, UInt(xlen.W)))
  })

  def latency(): Int = {
    val tallorwide = if (M < N) M else N
    val latency    = 2 * K + tallorwide - 1
    latency
  }

  val PEs =
    for (i <- 0 until M) yield
      for (j <- 0 until N) yield {
        if (i == 0 & j == 0)
          Module(new PE(gen, left_delay = 0, top_delay = 0, row = 0, col = 0))
        else if (j == 0)
          Module(new PE(gen, left_delay = i, top_delay = 1, row = i, col = j))
        else if (i == 0)
          Module(new PE(gen, left_delay = 1, top_delay = j, row = i, col = j))
        else
          Module(new PE(gen, left_delay = 1, top_delay = 1, row = i, col = j))
      }

  /* PE Control */
  val s_idle :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)
  val input_steps                            = new Counter(latency)
  when(state === s_idle) {
    when(io.activate) {
      state := s_ACTIVE
    }
  }.elsewhen(state === s_ACTIVE) {
    input_steps.inc( )
    when(input_steps.value === (K - 1).U) {
      state := s_COMPUTE
    }
  }.elsewhen(state === s_COMPUTE) {
    input_steps.inc( )
    when(input_steps.value === ((latency - 1).U)) {
      state := s_idle
    }
  }

  val io_lefts = for (i <- 0 until M) yield
    for (j <- 0 until K) yield {
      j.U -> io.left(i * K + j)
    }

  val io_rights = for (i <- 0 until N) yield
    for (j <- 0 until K) yield {
      j.U -> io.right(i + j * N)
    }


  val left_muxes = for (i <- 0 until M) yield {
    val mx = MuxLookup(input_steps.value, 0.U, io_lefts(i))
    mx
  }

  val top_muxes = for (i <- 0 until N) yield {
    val mx = MuxLookup(input_steps.value, 0.U, io_rights(i))
    mx
  }


  for (i <- 0 until M) {
    for (j <- 0 until N) {
      if (j != N - 1) {
        PEs(i)(j + 1).io.Left <> PEs(i)(j).io.Right
      }
      if (i != M - 1) {
        PEs(i + 1)(j).io.Top <> PEs(i)(j).io.Bottom
      }
      if (i == 0) {
        PEs(0)(j).io.Top.bits := top_muxes(j)
      }
      if (j == 0) {
        PEs(i)(0).io.Left.bits := left_muxes(i)
      }
    }
  }

  for (i <- 0 until N) {
    PEs(0)(i).io.Top.valid := false.B
  }
  for (i <- 0 until M) {
    PEs(i)(0).io.Left.valid := false.B
  }

  when(state === s_ACTIVE) {
    for (i <- 0 until N) {
      printf(p"$i,${PEs(0)(i).io.Top}")
      PEs(0)(i).io.Top.valid := true.B
    }
    for (i <- 0 until M) {
      PEs(i)(0).io.Left.valid := true.B
    }
  }

  printf("\nGrid  %d %d\n ", input_steps.value, state)
  printf(p"${M - 1}.U, ${N - 1}.U ${PEs(M - 1)(N - 1).io.Out.bits}")
  for (i <- 0 until M) {
    for (j <- 0 until N) {
      io.output(i * (N) + j) <> PEs(i)(j).io.Out.bits
      when(state === s_idle) {
        PEs(i)(j).reset := true.B
        //        done_input := false.B
      }
    }
    //    printf("\n")
  }
}
