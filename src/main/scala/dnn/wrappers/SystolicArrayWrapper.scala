package dnn.wrappers

import FPU.{FPMAC, FType, FloatingPoint}
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.BinaryPoint
import chisel3.testers._
import chisel3.util._
import dnn.SystolicSquareBuffered
//import com.sun.java.util.jar.pack.Instruction.Switch
import org.scalatest.{FlatSpec, Matchers}
import config._
import dnn.types.MAC
import interfaces._
import muxes._
import util._
import node._
import utility.UniformPrintfs


class SystolicSquareWrapper[T <: Data : MAC.OperatorMAC](gen: T, val N: Int)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val input_data = Flipped(Decoupled(UInt(xlen.W)))
    val input_sop  = Input(Bool( ))
    val input_eop  = Input(Bool( ))

    val output     = Decoupled(UInt(xlen.W))
    val output_sop = Output(Bool( ))
    val output_eop = Output(Bool( ))
  })

  val s_idle :: s_read :: s_execute :: s_write :: Nil = Enum(4)
  val state                                           = RegInit(s_idle)

  val ScratchPad_input  = RegInit(VecInit(Seq.fill(2 * N * N)(0.U(xlen.W))))
  val ScratchPad_output = RegInit(VecInit(Seq.fill(N * N)((0.U(xlen.W)))))

  val input_counter  = Counter(2 * N * N)
  val output_counter = Counter(N * N)

  val PE = Module(new SystolicSquareBuffered(UInt(p(XLEN).W), 3))


  for (i <- 0 until 2 * N * N) {
    if (i < N * N) {
      PE.io.left(i) := ScratchPad_input(i)
    } else {
      PE.io.right(i - (N * N)) := ScratchPad_input(i)
    }
  }

  (ScratchPad_output zip PE.io.output.bits).foreach { case (mem, pe_out) => mem := Mux(PE.io.output.valid, pe_out, mem) }

  io.input_data.ready := ((state === s_idle) || (state === s_read))
  PE.io.activate := Mux(input_counter.value === ((2 * N * N) - 1).U, true.B, false.B)
  PE.io.async_reset := false.B
  io.output.bits := 0.U
  io.output.valid := false.B

  io.output_sop := false.B
  io.output_eop := false.B

  switch(state) {
    is(s_idle) {
      when(io.input_data.fire) {
        state := s_read
      }
    }
    is(s_read) {
      when(input_counter.value === ((2 * N * N) - 1).U) {
        state := s_execute
      }.otherwise {
        ScratchPad_input(input_counter.value) := io.input_data.bits
        input_counter.inc( )
      }
    }
    is(s_execute) {
      when(PE.io.output.valid) {
        state := s_write
      }
    }
    is(s_write) {
      io.output.valid := true.B
      io.output.bits := ScratchPad_output(output_counter.value)
      //io.output.bits := output_counter.value
      //end-of-packet signal
      when(output_counter.value === ((N * N) - 1).U) {
        io.output_eop := true.B
      }.otherwise {
        io.output_eop := false.B
      }

      //start-of-packet signal
      when(output_counter.value === 0.U) {
        io.output_sop := true.B
      }.otherwise {
        io.output_sop := false.B
      }
      when(output_counter.value === ((N * N) - 1).U) {
        state := s_idle
      }.otherwise {
        when(io.output.fire) {
          output_counter.inc( )
        }
      }
    }
  }

  printf(p"[DEBUG] State: ${state}\n")
}

import java.io.{File, FileWriter}

object SystolicSquareWrapperMain extends App {
  val dir = new File("RTL/SystolicSquareWrapper");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new SystolicSquareWrapper(UInt(p(XLEN).W), 3)))

  //  () => new SystolicSquareWrapper(UInt(p(XLEN).W), 3)

  val verilogFile   = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close( )
}
