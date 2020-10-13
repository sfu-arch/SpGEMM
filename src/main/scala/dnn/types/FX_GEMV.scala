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
import dnn._
import FPOperator_GEMV._

object GEMV_OpCode {
  val Add = 1
  val Sub = 2
  val Mul = 3

  val opMap  = Map(
    "Add" -> Add,
    "add" -> Add,
    "Sub" -> Sub,
    "sub" -> Sub,
    "Mul" -> Mul,
    "mul" -> Mul
  )
  val length = 3
}


trait OperatorGEMV[T, T2] {
  def addition(l: T, r: T2)(implicit p: Parameters): T

  def subtraction(l: T, r: T2)(implicit p: Parameters): T

  def multiplication(l: T, r: T2)(implicit p: Parameters): T2

  def getfns(l: Shapes, r: Shapes)(implicit p: Parameters): Array[(Int, Shapes)]

}

object OperatorGEMV {

  implicit object FXmatNxN_FXvecN extends OperatorGEMV[FXmatNxN, FXvecN] {
    def addition(l: FXmatNxN, r: FXvecN)(implicit p: Parameters): FXmatNxN = {
      val x = Wire(new FXmatNxN(l.N, l.fraction))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          if (r.isCol == 0) {
            x.data(i)(j) := l.data(i)(j) + r.data(j)
          } else {
            x.data(i)(j) := l.data(i)(j) + r.data(i)
          }
        }
      }
      x
    }

    def subtraction(l: FXmatNxN, r: FXvecN)(implicit p: Parameters): FXmatNxN = {
      val x = Wire(new FXmatNxN(l.N, l.fraction))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          if (r.isCol == 0) {
            x.data(i)(j) := l.data(i)(j) - r.data(j)
          } else {
            x.data(i)(j) := l.data(i)(j) - r.data(i)
          }
        }
      }
      x
    }

    def multiplication(l: FXmatNxN, r: FXvecN)(implicit p: Parameters): FXvecN = {
      val x        = Wire(new FXvecN(r.N, r.fraction))
      val products = for (i <- 0 until l.N) yield {
        for (j <- 0 until l.N) yield {
          l.data(i)(j) * r.data(j)
        }
      }
      for (i <- 0 until l.N) {
        x.data(i) := products(i).reduceLeft(_ + _)
      }
      x
    }

    def getfns(l: Shapes, r: Shapes)(implicit p: Parameters): Array[(Int, Shapes)] = {
      Array(
        GEMV_OpCode.Add -> addition(l.asInstanceOf[FXmatNxN], r.asInstanceOf[FXvecN]),
        GEMV_OpCode.Sub -> subtraction(l.asInstanceOf[FXmatNxN], r.asInstanceOf[FXvecN]),
        GEMV_OpCode.Mul -> multiplication(l.asInstanceOf[FXmatNxN], r.asInstanceOf[FXvecN])
      )
    }
  }

  implicit object matNxN_vecN extends OperatorGEMV[matNxN, vecN] {
    def addition(l: matNxN, r: vecN)(implicit p: Parameters): matNxN = {
      val x = Wire(new matNxN(l.N))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          if (r.isCol == 0) {
            x.data(i)(j) := l.data(i)(j) + r.data(j)
          } else {
            x.data(i)(j) := l.data(i)(j) + r.data(i)
          }
        }
      }
      x
    }

    def subtraction(l: matNxN, r: vecN)(implicit p: Parameters): matNxN = {
      val x = Wire(new matNxN(l.N))
      for (i <- 0 until l.N) {
        for (j <- 0 until l.N) {
          if (r.isCol == 0) {
            x.data(i)(j) := l.data(i)(j) - r.data(j)
          } else {
            x.data(i)(j) := l.data(i)(j) - r.data(i)
          }
        }
      }
      x
    }

    def multiplication(l: matNxN, r: vecN)(implicit p: Parameters): vecN = {
      val x        = Wire(new vecN(l.N))
      val products = for (i <- 0 until l.N) yield {
        for (j <- 0 until l.N) yield {
          l.data(i)(j) * r.data(j)
        }
      }
      for (i <- 0 until l.N) {
        x.data(i) := products(i).reduceLeft(_ + _)
      }
      x
    }

    def getfns(l: Shapes, r: Shapes)(implicit p: Parameters): Array[(Int, Shapes)] = {
      Array(
        GEMV_OpCode.Add -> addition(l.asInstanceOf[matNxN], r.asInstanceOf[vecN]),
        GEMV_OpCode.Sub -> subtraction(l.asInstanceOf[matNxN], r.asInstanceOf[vecN]),
        GEMV_OpCode.Mul -> multiplication(l.asInstanceOf[matNxN], r.asInstanceOf[vecN])
      )
    }
  }

}

object GEMV_fns {

  def getfns(l: => Shapes, r: => Shapes)(implicit p: Parameters): Array[(Int, Shapes)] = {
    val lclass = l.getClass.getSimpleName
    val rclass = r.getClass.getSimpleName
    val parse  = "(.*)(mat|vec|Bit)([a-zA-Z]*)".r

    val parse(ltype, lshape, lsize) = lclass
    val parse(rtype, rshape, rsize) = rclass

    print(ltype + rtype + lshape + rshape + lsize + rsize)

    //    Check the type of left and right operand are the same
    require(ltype == rtype)
    //    Check that the left operand is matrix and the right operand is vector.
    require(lshape == "mat" && (rshape == "vec" || rshape == "Bits"))

    val aluOp =
      if (ltype == "FX") {
        implicitly[OperatorGEMV[FXmatNxN, FXvecN]].getfns(l, r)
      } else if (ltype == "") {
        implicitly[OperatorGEMV[matNxN, vecN]].getfns(l, r)
      } else if (ltype == "FP") {
        implicitly[OperatorGEMV[FPmatNxN, FPvecN]].getfns(l, r)
      } else { // You should never get here. Just a default.
        require(0 == 1, "Unsupported type of GEMV Operands")
        Array(
          GEMV_OpCode.Add -> (implicitly[OperatorGEMV[matNxN, vecN]].
            addition(l.asInstanceOf[matNxN], r.asInstanceOf[vecN])),
          GEMV_OpCode.Mul -> (implicitly[OperatorGEMV[matNxN, vecN]].
            multiplication(l.asInstanceOf[matNxN], r.asInstanceOf[vecN]))
        )
      }
    aluOp
  }
}




