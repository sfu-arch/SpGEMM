package FPU

import hardfloat._
import chisel3.Module
import chisel3._
import chisel3.util._
import FType._
import chisel3.core.CompileOptions
import config._

case class FType(exp: Int, sig: Int) {
  def ieeeWidth = exp + sig

  def expWidth = exp

  def sigWidth = sig

  def recodedWidth = ieeeWidth + 1

  def qNaN = ((BigInt(7) << (exp + sig - 3)) + (BigInt(1) << (sig - 2))).U((exp + sig + 1).W)

  def isNaN(x: UInt) = x(sig + exp - 1, sig + exp - 3).andR

  def isSNaN(x: UInt) = isNaN(x) && !x(sig - 2)

  def classify(x: UInt) = {
    val sign = x(sig + exp)
    val code = x(exp + sig - 1, exp + sig - 3)
    val codeHi = code(2, 1)
    val isSpecial = codeHi === 3.U

    val isHighSubnormalIn = x(exp + sig - 3, sig - 1) < 2.U
    val isSubnormal = code === 1.U || codeHi === 1.U && isHighSubnormalIn
    val isNormal = codeHi === 1.U && !isHighSubnormalIn || codeHi === 2.U
    val isZero = code === 0.U
    val isInf = isSpecial && !code(0)
    val isNaN = code.andR
    val isSNaN = isNaN && !x(sig - 2)
    val isQNaN = isNaN && x(sig - 2)

    Cat(isQNaN, isSNaN, isInf && !sign, isNormal && !sign,
      isSubnormal && !sign, isZero && !sign, isZero && sign,
      isSubnormal && sign, isNormal && sign, isInf && sign)
  }

  // convert between formats, ignoring rounding, range, NaN
  def unsafeConvert(x: UInt, to: FType) = if (this == to) x else {
    val sign = x(sig + exp)
    val fractIn = x(sig - 2, 0)
    val expIn = x(sig + exp - 1, sig - 1)
    val fractOut = fractIn << to.sig >> sig
    val expOut = {
      val expCode = expIn(exp, exp - 2)
      val commonCase = (expIn + (1 << to.exp).U) - (1 << exp).U
      Mux(expCode === 0.U || expCode >= 6.U, Cat(expCode, commonCase(to.exp - 3, 0)), commonCase(to.exp, 0))
    }
    Cat(sign, expOut, fractOut)
  }

  def recode(x: UInt) = hardfloat.recFNFromFN(exp, sig, x)

  def ieee(x: UInt) = hardfloat.fNFromRecFN(exp, sig, x)
}

object FType {
  val S = new FType(8, 24)
  val D = new FType(11, 53)
  val H = new FType(5, 11)
  val M = new FType(exp = 3, sig = 5)
}

class FloatingPoint(val t: FType) extends Bundle {
  val value = UInt(t.ieeeWidth.W)

  def asRecFn(): UInt = {
    t.recode(value)
  }

  override def toPrintable: Printable = {
    p"0x${Hexadecimal(value)} }"
  }
}

object FloatingPoint {
  def apply(t: FType): FloatingPoint = {
    val wire = Wire(new FloatingPoint(t))
    wire.value := 0.U
    wire
  }

  def apply(value: UInt, t: FType): FloatingPoint = {
    val wire = Wire(new FloatingPoint(t))
    wire.value := value
    wire
  }
}


/** @param key     a key to search for
  * @param default a default value if nothing is found
  * @param mapping a sequence to search of keys and values
  * @return the value found or the default if not
  */
object FPAluGenerator {
  def apply[S <: Int, T <: Data](key: S, mapping: Seq[(S, T)]): T = {

    //Assign default to first element
    var res = mapping(0)._2
    for ((k, v) <- mapping) {
      if (k == key)
        res = v
    }

    res
  }
}


/**
  * ALU class supports all the computation operations exist in LLVM
  * to use the class you only need to specify the length of inputs
  * and opCode of your alu.
  *
  * @param opCode opcode which indicates ALU operation
  * @param xlen   bit width of the inputs
  */
class FPMAC(val xlen: Int, val opCode: String, t: FType) extends Module {
  val io = IO(new Bundle {
    val in1 = Input(UInt(xlen.W))
    val in2 = Input(UInt(xlen.W))
    val in3 = if (node.AluOpCode.opMap(opCode) == node.AluOpCode.Mac)
      Some(Input(UInt(xlen.W))) else None

    val out = Output(UInt(xlen.W))
  })

  /* We are hardcoding the signals at compile time to enable
     the backend synthesis tools to optimize. This is important 
     from a dataflow perspective to ensure we get the most optimal node
     for each element in the dataflow.
     If we decode, then a MUX would be needed within the hardware.
  */
  def FPUControl(): Unit = {
    node.AluOpCode.opMap(opCode) match {
      case node.AluOpCode.Add => { // b + c
        mulAddRecFN.io.op := 0.U
        mulAddRecFN.io.a := dummy1.io.out
        mulAddRecFN.io.b := in1RecFN
        mulAddRecFN.io.c := in2RecFN
      }
      case node.AluOpCode.Sub => { // b - c
        mulAddRecFN.io.op := 1.U
        mulAddRecFN.io.a := dummy1.io.out
        mulAddRecFN.io.b := in1RecFN
        mulAddRecFN.io.c := in2RecFN
      }
      case node.AluOpCode.Mul => { // a*b
        mulAddRecFN.io.op := 0.U
        mulAddRecFN.io.a := in1RecFN
        mulAddRecFN.io.b := in2RecFN
        mulAddRecFN.io.c := dummy0.io.out
      }
      case node.AluOpCode.Mac => { // a*b + c
        mulAddRecFN.io.op := 0.U
        mulAddRecFN.io.a := in1RecFN
        mulAddRecFN.io.b := in2RecFN
        mulAddRecFN.io.c := in3RecFN
      }
    }
  }

  /* 1 and 0 encoded in berkley hardfloat format. 
    This is useful for hardwiring some of the inputs e.g., 
    use implement b + c as 1 * b + c on a MAC.
  */
  val dummy1 = Module(new INToRecFN(t.ieeeWidth, t.expWidth, t.sigWidth))
  dummy1.io.signedIn := false.B
  dummy1.io.in := 1.U((t.ieeeWidth).W)
  dummy1.io.roundingMode := "b110".U(3.W)
  dummy1.io.detectTininess := 0.U(1.W)

  val dummy0 = Module(new INToRecFN(t.ieeeWidth, t.expWidth, t.sigWidth))
  dummy0.io.signedIn := false.B
  dummy0.io.in := 0.U((t.ieeeWidth).W)
  dummy0.io.roundingMode := "b110".U(3.W)
  dummy0.io.detectTininess := 0.U(1.W)

  /* Recode inputs into ieee format */
  val in1RecFN    = t.recode(io.in1)
  val in2RecFN    = t.recode(io.in2)
  val in3RecFN    = if (node.AluOpCode.opMap(opCode) == node.AluOpCode.Mac) {
    t.recode(io.in3.get)
  } else {
    dummy0.io.out
  }
  val mulAddRecFN = Module(new MulAddRecFN(t.expWidth, t.sigWidth))
  mulAddRecFN.io.roundingMode := "b110".U(3.W)
  mulAddRecFN.io.detectTininess := 0.U(1.W)

  assert(!node.AluOpCode.opMap.get(opCode).isEmpty, "Wrong ALU OP!")

  FPUControl( )
  io.out := t.ieee(mulAddRecFN.io.out)

}

object addsubmul {
  def apply(in1: FloatingPoint, in2: FloatingPoint, opcode: String): UInt = {
    val FU = Module(new FPMAC(in1.t.ieeeWidth, opcode, in1.t))
    FU.io.in1 := in1.value
    FU.io.in2 := in2.value
    FU.io.out
  }
}

object fmac {
  def apply(in1: FloatingPoint, in2: FloatingPoint, in3: FloatingPoint): UInt = {
    val FU = Module(new FPMAC(in1.t.ieeeWidth, "mac", in1.t))
    FU.io.in1 := in1.value
    FU.io.in2 := in2.value
    FU.io.in3.get := in3.value
    FU.io.out
  }
}

object compare {
  def apply(in1: FloatingPoint, in2: FloatingPoint, opcode: String): UInt = {
    val FU = Module(new CompareRecFN(in1.t.expWidth, in1.t.sigWidth))
    FU.io.a := in1.asRecFn
    FU.io.b := in2.asRecFn
    FU.io.signaling <> DontCare
    opcode match {
      case "LT" => FU.io.lt.asUInt
      case "GT" => FU.io.gt.asUInt
      case "EQ" => FU.io.eq.asUInt
      case "LTE" => (FU.io.eq | FU.io.lt).asUInt
      case "GTE" => (FU.io.eq | FU.io.gt).asUInt
      case _ => FU.io.lt.asUInt
    }
  }
}

object max {
  def apply(in1: FloatingPoint, in2: FloatingPoint): UInt = {
    val FU = Module(new CompareRecFN(in1.t.expWidth, in1.t.sigWidth))
    FU.io.a := in1.asRecFn
    FU.io.b := in2.asRecFn
    FU.io.signaling <> DontCare
    Mux(FU.io.gt, in1.value, in2.value)
  }
}

object min {
  def apply(in1: FloatingPoint, in2: FloatingPoint): UInt = {
    val FU = Module(new CompareRecFN(in1.t.expWidth, in1.t.sigWidth))
    FU.io.a := in1.asRecFn
    FU.io.b := in2.asRecFn
    FU.io.signaling <> DontCare
    Mux(FU.io.lt, in1.value, in2.value)
  }
}

class FPALU(val gen: FloatingPoint, val opCode: String)(implicit val p: Parameters) extends Module {
  val io    = IO(new Bundle {
    val in1 = Input(gen.cloneType)
    val in2 = Input(gen.cloneType)
    val in3 = if (node.AluOpCode.DSPopMap(opCode) == node.AluOpCode.Mac)
      Some(Input(gen.cloneType)) else None

    val out = Output(UInt(gen.getWidth.W))
  })
  var aluOp = Array(
    node.AluOpCode.Add -> (addsubmul(io.in1, io.in2, "add")),
    node.AluOpCode.Sub -> (addsubmul(io.in1, io.in2, "sub")),
    node.AluOpCode.LT -> (compare(io.in1, io.in2, "LT")),
    node.AluOpCode.GT -> (compare(io.in1, io.in2, "GT")),
    node.AluOpCode.EQ -> (compare(io.in1, io.in2, "EQ")),
    node.AluOpCode.LTE -> (compare(io.in1, io.in2, "LTE")),
    node.AluOpCode.GTE -> (compare(io.in1, io.in2, "GTE")),
    node.AluOpCode.PassA -> io.in1.value,
    node.AluOpCode.PassB -> io.in2.value,
    node.AluOpCode.Mul -> (addsubmul(io.in1, io.in2, "mul")),
    node.AluOpCode.Max -> (max(io.in1, io.in2)),
    node.AluOpCode.Min -> (min(io.in1, io.in2))
  )

  if (node.AluOpCode.DSPopMap(opCode) == node.AluOpCode.Mac) {
    aluOp = aluOp :+ node.AluOpCode.Mac -> (fmac(io.in1, io.in2, io.in3.get))
  }

  assert(!node.AluOpCode.DSPopMap.get(opCode).isEmpty, "Wrong ALU OP!")
  io.out := node.AluGenerator(node.AluOpCode.DSPopMap(opCode), aluOp).asUInt
}