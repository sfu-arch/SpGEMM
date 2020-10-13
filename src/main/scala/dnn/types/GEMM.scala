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


object GEMM {

  // Declare trait to encapsulate implicit functions
  trait OperatorGEMM[T] {
    def multiplication(l: T, r: T, start: Bool)(implicit p: Parameters): (T, Int)
  }

  // Implementation of actual functions
  object OperatorGEMM {

    //    FX Operations
    implicit object FXmatNxN extends OperatorGEMM[FXmatNxN] {
      def multiplication(l: FXmatNxN, r: FXmatNxN, start: Bool)(implicit p: Parameters): (FXmatNxN, Int) = {
        val x    = Wire(new FXmatNxN(l.N, l.fraction))
        val GEMM = Module(new SystolicSquare(l.data(0)(0).cloneType, l.N))
        GEMM.io.activate := start
        l.toVecUInt( ) zip GEMM.io.left foreach { case (a, b) => b := a }
        r.toVecUInt( ) zip GEMM.io.right foreach { case (a, b) => b := a }
        x.fromVecUInt(GEMM.io.output)
        (x, GEMM.latency( ))
      }
    }

    implicit object matNxN extends OperatorGEMM[matNxN] {
      def multiplication(l: matNxN, r: matNxN, start: Bool)(implicit p: Parameters): (matNxN, Int) = {
        val x    = Wire(new matNxN(l.N))
        val GEMM = Module(new SystolicSquare(l.data(0)(0).cloneType, l.N))
        GEMM.io.activate := start
        GEMM.io.async_reset := false.B
        l.toVecUInt( ) zip GEMM.io.left foreach { case (a, b) => b := a }
        r.toVecUInt( ) zip GEMM.io.right foreach { case (a, b) => b := a }
        x.fromVecUInt(GEMM.io.output)
        (x, GEMM.latency)
      }
    }

    implicit object FPmatNxN extends OperatorGEMM[FPmatNxN] {
      def multiplication(l: FPmatNxN, r: FPmatNxN, start: Bool)(implicit p: Parameters): (FPmatNxN, Int) = {
        val x    = Wire(new FPmatNxN(l.N, l.Ftyp))
        val GEMM = Module(new SystolicSquare(new FloatingPoint(l.Ftyp), l.N))
        GEMM.io.activate := start
        GEMM.io.async_reset := false.B
        l.toVecUInt( ) zip GEMM.io.left foreach { case (a, b) => b := a }
        r.toVecUInt( ) zip GEMM.io.right foreach { case (a, b) => b := a }
        x.fromVecUInt(GEMM.io.output)
        (x, GEMM.latency)
      }
    }


  }

  // Implicit functions to invoke.
  def GEMM[T](l: T, r: T, start: Bool)(implicit op: OperatorGEMM[T], p: Parameters): (T, Int) = op.multiplication(l, r, start)
}
