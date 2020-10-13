package dnn.types

import FPU._
import chisel3._
import chisel3.core.FixedPoint
import config._
import node._

object MAC {

  trait OperatorMAC[T] {
    def mac(l: T, r: T, c: T)(implicit p: Parameters): T
  }

  object OperatorMAC {

    implicit object UIntMAC extends OperatorMAC[UInt] {
      def mac(l: UInt, r: UInt, c: UInt)(implicit p: Parameters): UInt = {
        val x     = Wire(l.cloneType)
        val FXALU = Module(new UALU(p(XLEN), "Mac"))
        FXALU.io.in1 := l
        FXALU.io.in2 := r
        FXALU.io.in3.get := c
        x := FXALU.io.out.asTypeOf(l)
        x
      }
    }


    implicit object FixedPointMAC extends OperatorMAC[FixedPoint] {
      def mac(l: FixedPoint, r: FixedPoint, c: FixedPoint)(implicit p: Parameters): FixedPoint = {
        val x     = Wire(l.cloneType)
        val FXALU = Module(new DSPALU(FixedPoint(l.getWidth.W, l.binaryPoint), "Mac"))
        FXALU.io.in1 := l
        FXALU.io.in2 := r
        FXALU.io.in3.get := c
        x := FXALU.io.out.asTypeOf(l)
        // Uncomment if you do not have access to DSP tools and need to use chisel3.experimental FixedPoint. DSP tools provides implicit support for truncation.
        //  val mul = ((l.data * r.data) >> l.fraction.U).asFixedPoint(l.fraction.BP)
        // x.data := mul + c.data
        x
      }
    }


    implicit object FP_MAC extends OperatorMAC[FloatingPoint] {
      def mac(l: FloatingPoint, r: FloatingPoint, c: FloatingPoint)(implicit p: Parameters): FloatingPoint = {
        val x   = Wire(new FloatingPoint(l.t))
        val mac = Module(new FPMAC(p(XLEN), opCode = "Mac", t = l.t))
        mac.io.in1 := l.value
        mac.io.in2 := r.value
        mac.io.in3.get := c.value
        x.value := mac.io.out
        x
      }
    }

  }

  def mac[T](l: T, r: T, c: T)(implicit op: OperatorMAC[T], p: Parameters): T = op.mac(l, r, c)
}

object TwoOperand {

  trait OperatorTwoOperand[T] {
    def binaryop(l: T, r: T, opcode: String)(implicit p: Parameters): T
  }

  object OperatorTwoOperand {

    implicit object UIntTwoOperand extends OperatorTwoOperand[UInt] {
      def binaryop(l: UInt, r: UInt, opcode: String)(implicit p: Parameters): UInt = {
        val x     = Wire(l.cloneType)
        val FXALU = Module(new UALU(p(XLEN), opcode))
        FXALU.io.in1 := l
        FXALU.io.in2 := r
        x := FXALU.io.out.asTypeOf(l)
        x
      }
    }

    implicit object SIntTwoOperand extends OperatorTwoOperand[SInt] {
      def binaryop(l: SInt, r: SInt, opcode: String)(implicit p: Parameters): SInt = {
        val x     = Wire(l.cloneType)
        val FXALU = Module(new UALU(p(XLEN), opcode, true))
        FXALU.io.in1 := l.asUInt
        FXALU.io.in2 := r.asUInt
        x := FXALU.io.out.asTypeOf(l)
        //       printf("%x,%x,%x \n", l.asUInt, r.asUInt, FXALU.io.out)
        x
      }
    }


    implicit object FixedPointTwoOperand extends OperatorTwoOperand[FixedPoint] {
      def binaryop(l: FixedPoint, r: FixedPoint, opcode: String)(implicit p: Parameters): FixedPoint = {
        val x     = Wire(l.cloneType)
        //       val FXALU = Module(new DSPALU(FixedPoint(l.getWidth.W, l.binaryPoint), opcode))
        val FXALU = Module(new DSPorCORDIC(l.getWidth, l.binaryPoint.get, opcode))
        FXALU.io.in1 := l
        FXALU.io.in2 := r
        x := FXALU.io.out //asTypeOf(l)
        //        printf("%x,%x,%x",l.asUInt,r.asUInt,FXALU.io.out)
        x
      }
    }


    implicit object FP_TwoOperand extends OperatorTwoOperand[FloatingPoint] {
      def binaryop(l: FloatingPoint, r: FloatingPoint, opcode: String)(implicit p: Parameters): FloatingPoint = {
        val x   = Wire(new FloatingPoint(l.t))
        val mac = Module(new FPMAC(p(XLEN), opcode, t = l.t))
        mac.io.in1 := l.value
        mac.io.in2 := r.value
        x.value := mac.io.out
        //  printf(p"${mac.io.in1},${mac.io.in2},${mac.io.out}")
        x
      }
    }

  }

  def binaryop[T](l: T, r: T, opcode: String)(implicit op: OperatorTwoOperand[T], p: Parameters): T = op.binaryop(l, r, opcode)
}

