package dnn.types

import FPU.{FPMAC, FType, FloatingPoint}
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
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
//import FPOperator_SCAL._

trait OperatorSCAL[T] {
  def magic(l: T, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (T, Int)

}

object OperatorSCAL {

  implicit object FXmatNxN_FX extends OperatorSCAL[FXmatNxN] {
    def magic(l: FXmatNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXmatNxN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_SCAL(l.data(0)(0), flatvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FXvecN_UInt extends OperatorSCAL[FXvecN] {
    def magic(l: FXvecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXvecN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_SCAL(l.data(0), flatvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object matNxN_UInt extends OperatorSCAL[matNxN] {
    def magic(l: matNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (matNxN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      print(l.N + "," + l.issign)
      val FU = if (!l.issign) {
        Module(new NCycle_SCAL(l.data(0)(0), flatvec.length, lanes = lanes, opcode))
      } else {
        Module(new NCycle_SCAL(SInt(l.data(0)(0).getWidth.W), flatvec.length, lanes = lanes, opcode))
      }
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      printf(p"Data = $data $current")
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object vecN_UInt extends OperatorSCAL[vecN] {
    def magic(l: vecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (vecN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = if (!l.issign) {
        Module(new NCycle_SCAL(l.data(0), flatvec.length, lanes = lanes, opcode))
      } else {
        Module(new NCycle_SCAL(SInt(l.data(0).getWidth.W), flatvec.length, lanes = lanes, opcode))
      }
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FPmatNxN_FX extends OperatorSCAL[FPmatNxN] {
    def magic(l: FPmatNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPmatNxN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_SCAL(new FloatingPoint(l.t), flatvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      printf(p"Data = $data $current")
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  implicit object FPvecN_UInt extends OperatorSCAL[FPvecN] {
    def magic(l: FPvecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPvecN, Int) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_SCAL(new FloatingPoint(l.t), flatvec.length, lanes = lanes, opcode))
      FU.io.activate := start
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Reg(Vec(flatvec.length, UInt(p(XLEN).W)))
      val current = RegInit(0.U(log2Ceil(flatvec.length).W))
      when(FU.io.valid) {
        current := current + lanes.U
        for (i <- 0 until lanes) {
          data(i.U + current) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      // Lane wise operations require 1 extra cycle.
      // 1 cycle required for registering the output.
      (x, FU.latency( ) + 1)
    }
  }

  def magic[T](l: T, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit op: OperatorSCAL[T], p: Parameters): (T, Int) = op.magic(l, r, start, lanes, opcode)


}




