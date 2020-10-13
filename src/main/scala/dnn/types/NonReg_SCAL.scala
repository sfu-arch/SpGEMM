package dnn.types

import FPU.{FPMAC, FType, FloatingPoint}
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.experimental.FixedPoint
import config._
import interfaces._
import node._
import dnn.modules.NCycle_CooSCAL

trait OperatorNRSCAL[T] {
  def magic(l: T, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (T, Bool)
}

object OperatorNRSCAL {

  implicit object FXmatNxN_FX extends OperatorNRSCAL[FXmatNxN] {
    def magic(l: FXmatNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXmatNxN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_CooSCAL(l.data(0)(0), flatvec.length, lanes = flatvec.length, opcode))
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }

      x.fromVecUInt(data)
      (x, start)
    }
  }

  implicit object FXvecN_UInt extends OperatorNRSCAL[FXvecN] {
    def magic(l: FXvecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FXvecN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_CooSCAL(l.data(0), flatvec.length, lanes = flatvec.length, opcode))
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      (x, start)
    }
  }

  implicit object matNxN_UInt extends OperatorNRSCAL[matNxN] {
    def magic(l: matNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (matNxN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      print(l.N + "," + l.issign)
      val FU = if (!l.issign) {
        Module(new NCycle_CooSCAL(l.data(0)(0), flatvec.length, lanes = flatvec.length, opcode))
      } else {
        Module(new NCycle_CooSCAL(SInt(l.data(0)(0).getWidth.W), flatvec.length, lanes = flatvec.length, opcode))
      }
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      (x, start)
    }
  }

  implicit object vecN_UInt extends OperatorNRSCAL[vecN] {
    def magic(l: vecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (vecN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = if (!l.issign) {
        Module(new NCycle_CooSCAL(l.data(0), flatvec.length, lanes = flatvec.length, opcode))
      } else {
        Module(new NCycle_CooSCAL(SInt(l.data(0).getWidth.W), flatvec.length, lanes = flatvec.length, opcode))
      }
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      (x, start)
    }
  }

  implicit object FPmatNxN_FX extends OperatorNRSCAL[FPmatNxN] {
    def magic(l: FPmatNxN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPmatNxN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_CooSCAL(new FloatingPoint(l.t), flatvec.length, lanes = flatvec.length, opcode))
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      (x, start)
    }
  }

  implicit object FPvecN_UInt extends OperatorNRSCAL[FPvecN] {
    def magic(l: FPvecN, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit p: Parameters): (FPvecN, Bool) = {
      val x       = Wire(l.cloneType)
      val flatvec = l.toVecUInt( )
      val FU      = Module(new NCycle_CooSCAL(new FloatingPoint(l.t), flatvec.length, lanes = flatvec.length, opcode))
      flatvec zip FU.io.input_vec foreach { case (a, b) => b := a }
      FU.io.scalar := r
      val data    = Wire(Vec(flatvec.length, UInt(p(XLEN).W)))
      for (i <- 0 until lanes) {
        data(i.U) := 0.U
        when(start){
          data(i.U) := FU.io.output(i)
        }
      }
      x.fromVecUInt(data)
      (x, start)
    }
  }

  def magic[T](l: T, r: UInt, start: Bool, lanes: Int, opcode: String)(implicit op: OperatorNRSCAL[T], p: Parameters): (T, Bool) = op.magic(l, r, start, lanes, opcode)


}




