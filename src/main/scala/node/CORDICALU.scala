package node

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.BinaryPoint
import chisel3.util._
import dsptools._
import dsptools.numbers.{DspReal, RealBits}
import dsptools.numbers.implicits._
import dsptools.DspContext
import dsptools.numbers.RealTrig


/**
  * List of compute operations which we can support
  */
object CORDICOpCode {
  val ln    = 1
  val log10 = 2
  val exp   = 3
  val sqrt  = 4
  val pow   = 5
  val sin   = 6
  val cos   = 7
  val tan   = 8
  val atan  = 9
  val asin  = 10
  val acos  = 11
  val atan2 = 12
  val hypot = 13
  val sqr   = 14
  val sinh  = 15
  val cosh  = 16
  val tanh  = 17
  val asinh = 18
  val acosh = 19
  val atanh = 20
  val div   = 21

  val opMap = Map(
    "ln" -> ln,
    "log10" -> log10,
    "exp" -> exp,
    "sqrt" -> sqrt,
    "pow" -> pow,
    "sin" -> sin,
    "cos" -> cos,
    "tan" -> tan,
    "atan" -> atan,
    "asin" -> asin,
    "acos" -> acos,
    "atan2" -> atan2,
    "hypot" -> hypot,
    "sqr" -> sqr,
    "sinh" -> sinh,
    "cosh" -> cosh,
    "tanh" -> tanh,
    "asinh" -> asinh,
    "acosh" -> acosh,
    "atanh" -> atanh,
    "div" -> div
  )


  val length = 20
}


class CORDICIO(val xlen: Int, val fraction: Int, val opCode: String) extends Bundle {
  val in1 = Input(FixedPoint(xlen.W, fraction.BP))
  val in2 = Input(FixedPoint(xlen.W, fraction.BP))

  val out = Output(FixedPoint(xlen.W, fraction.BP))

  val outUInt = Output(UInt(xlen.W))

  override def cloneType: this.type

  = new CORDICIO(xlen, fraction, opCode).asInstanceOf[this.type]
}

// Parameterized Chisel Module; takes in type parameters as explained above
class CORDICALU(val xlen: Int, val fraction: Int, val opCode: String) extends Module {
  // This is how you declare an IO with parameters
  val io = IO(new CORDICIO(xlen, fraction, opCode))

  val in1real = io.in1.asReal( )
  val in2real = io.in2.asReal( )

  DspContext.alter(DspContext.current.copy(trimType = RoundDown, binaryPointGrowth = 0, numMulPipes = 0)) {
  }

  var aluOp = Array(
    CORDICOpCode.ln -> (in1real.ln( )),
    CORDICOpCode.log10 -> (in1real.log10( )),
    CORDICOpCode.exp -> (in1real.exp( )),
    CORDICOpCode.sqrt -> (in1real.sqrt( )),
    CORDICOpCode.pow -> (in1real.pow(in2real)),
    CORDICOpCode.sin -> (in1real.sin( )),
    CORDICOpCode.cos -> (in1real.cos( )),
    CORDICOpCode.tan -> (in1real.tan( )),
    CORDICOpCode.atan -> (in1real.atan( )),
    CORDICOpCode.asin -> (in1real.asin( )),
    CORDICOpCode.acos -> (in1real.acos( )),
    CORDICOpCode.atan2 -> (in1real.atan2(in2real)),
    CORDICOpCode.hypot -> (in1real.hypot(in2real)),
    CORDICOpCode.sqr -> (in1real context_* in1real),
    CORDICOpCode.sinh -> (in1real.sinh( )),
    CORDICOpCode.cosh -> (in1real.cosh( )),
    CORDICOpCode.tanh -> (in1real.tanh( )),
    CORDICOpCode.asinh -> (in1real.asinh( )),
    CORDICOpCode.acosh -> (in1real.acosh( )),
    CORDICOpCode.atanh -> (in1real.atanh( )),
    CORDICOpCode.div -> (in1real./(in2real))
  )

  assert(!CORDICOpCode.opMap.get(opCode).isEmpty, "Wrong ALU OP!")
  val op = AluGenerator(CORDICOpCode.opMap(opCode), aluOp).asFixed(io.in1)
  io.out := op
  io.outUInt := op.asUInt
}


class DSPorCORDIC(val xlen: Int, val fraction: Int, val opCode: String) extends Module {
  val io = IO(new CORDICIO(xlen, fraction, opCode))

  if (!CORDICOpCode.opMap.contains(opCode)) {
    val FU = Module(new DSPALU(FixedPoint(xlen.W, fraction.BP), opCode))
    FU.io.in1 <> io.in1
    FU.io.in2 <> io.in2
    require(AluOpCode.DSPopMap(opCode) != AluOpCode.Mac, "You cannot use MAC in SCAL operator. MACs require 3 inputs. SCAL nodes only supply 2")
    io.outUInt <> FU.io.out
    io.out <> FU.io.out.asFixedPoint(fraction.BP)
  } else {
    val FU = Module(new CORDICALU(xlen = xlen, fraction = fraction, opCode))
    FU.io.in1 <> io.in1
    FU.io.in2 <> io.in2
    io.out <> FU.io.out
    io.outUInt <> FU.io.outUInt
  }
}