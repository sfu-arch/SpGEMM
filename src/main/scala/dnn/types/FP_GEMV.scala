package dnn.types

import FPU.{FPMAC, FType}
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
import node._
import utility.UniformPrintfs
import dnn._

object FPOperator_GEMV {

  implicit object FPmatNxN_FPvecN extends OperatorGEMV[FPmatNxN, FPvecN] {
    def addition(l: FPmatNxN, r: FPvecN)(implicit p: Parameters): FPmatNxN = {
      //    require((l.N & (l.N - 1)) == 0, "left operand not a power of 2")
      val x = Wire(new FPmatNxN(l.N, l.t))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          val FPadd = Module(new FPMAC(p(XLEN), "Add", l.t))
          FPadd.io.in1 := l.data(i)(j)
          if (r.isCol == 0) {
            FPadd.io.in2 := r.data(j)
          } else {
            FPadd.io.in2 := r.data(i)
          }
          x.data(i)(j) := FPadd.io.out
        }
      }
      x
    }

    def subtraction(l: FPmatNxN, r: FPvecN)(implicit p: Parameters): FPmatNxN = {
      val x = Wire(new FPmatNxN(l.N, l.t))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          val FPadd = Module(new FPMAC(p(XLEN), "Sub", l.t))
          FPadd.io.in1 := l.data(i)(j)
          if (r.isCol == 0) {
            FPadd.io.in2 := r.data(j)
          } else {
            FPadd.io.in2 := r.data(i)
          }
          x.data(i)(j) := FPadd.io.out
        }
      }
      x
    }

    def multiplication(l: FPmatNxN, r: FPvecN)(implicit p: Parameters): FPvecN = {
      require(r.isCol == 1, "Right vector should be a column vector")
      val x = Wire(new FPvecN(r.N, r.t))


      x
    }

    def getfns(l: Shapes, r: Shapes)(implicit p: Parameters): Array[(Int, Shapes)] = {
      Array(
        GEMV_OpCode.Add -> addition(l.asInstanceOf[FPmatNxN], r.asInstanceOf[FPvecN]),
        GEMV_OpCode.Sub -> subtraction(l.asInstanceOf[FPmatNxN], r.asInstanceOf[FPvecN]),
        GEMV_OpCode.Mul -> multiplication(l.asInstanceOf[FPmatNxN], r.asInstanceOf[FPvecN])
      )
    }

  }

}