package dnn.types

import FPU.{FPMAC, FType, FloatingPoint}
import chisel3.{Module, when, _}
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.experimental.FixedPoint
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import interfaces._
import muxes._
import util._
import node._
import dnn._
//import FPOperator_Dot._

trait OperatorDot[T] {
  def magic(l: T, r: T, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (T, Int)

}

object OperatorDot {

  implicit object FXmatNxN_FX extends OperatorDot[FXmatNxN] {
    def magic(l: FXmatNxN, r: FXmatNxN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXmatNxN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU       = Module(new NCycle_Dot(l.data(0)(0), leftvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      when (FU.io.activate) {
        current := 0.U
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FXvecN_UInt extends OperatorDot[FXvecN] {
    def magic(l: FXvecN, r: FXvecN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXvecN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU       = Module(new NCycle_Dot(l.data(0), leftvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      when (FU.io.activate) {
        current := 0.U
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object matNxN_UInt extends OperatorDot[matNxN] {
    def magic(l: matNxN, r: matNxN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (matNxN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU = if (!l.issign) {
        Module(new NCycle_Dot(l.data(0)(0), leftvec.length, lanes = lanes, opcode))
      } else {
        Module(new NCycle_Dot(SInt(l.data(0)(0).getWidth.W), leftvec.length, lanes = lanes, opcode))
      }
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      when (FU.io.activate) {
        current := 0.U
      }
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object vecN_UInt extends OperatorDot[vecN] {
    def magic(l: vecN, r: vecN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (vecN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU = if (!l.issign) {
        Module(new NCycle_Dot(l.data(0), leftvec.length, lanes = lanes, opcode))
      } else {
        Module(new NCycle_Dot(SInt(l.data(0).getWidth.W), leftvec.length, lanes = lanes, opcode))
      }
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      when (FU.io.activate) {
        current := 0.U
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FPmatNxN_FX extends OperatorDot[FPmatNxN] {
    def magic(l: FPmatNxN, r: FPmatNxN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPmatNxN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU       = Module(new NCycle_Dot(new FloatingPoint(l.t), leftvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      printf(p"Data = $data $current")
      when (FU.io.activate) {
        current := 0.U
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FPvecN_UInt extends OperatorDot[FPvecN] {
    def magic(l: FPvecN, r: FPvecN, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPvecN, Int) = {
      val x        = Wire(l.cloneType)
      val leftvec  = l.toVecUInt( )
      val rightvec = r.toVecUInt( )
      val FU       = Module(new NCycle_Dot(new FloatingPoint(l.t), leftvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      leftvec zip FU.io.input_left_vec foreach { case (a, b) => b := a }
      rightvec zip FU.io.input_right_vec foreach { case (a, b) => b := a }
      val data    = Reg(Vec(leftvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(leftvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      when (FU.io.activate) {
        current := 0.U
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  def magic[T](l: T, r: T, start: Bool, lanes: Int, opcode: String)(implicit op: OperatorDot[T], p: Parameters): (T, Int) = op.magic(l, r, start, lanes, opcode)


}




