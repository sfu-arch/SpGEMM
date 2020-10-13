package node

import FPU.{FPMAC, FType}
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.core.FixedPoint
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import interfaces._
import muxes._
import util._

import scala.reflect.runtime.universe._


//////////////////////////////////SHAPE DEFINITIONS//////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////

abstract class Shapes(implicit p: Parameters) extends CoreBundle( )(p) {

  // Gather and Scatter from flat vectors
  def fromVecUInt(input: Vec[UInt])

  def toVecUInt(): Vec[UInt]

  def getdouble(): UInt

  def gethalf(): UInt

  def getWidthData(): Int

  def getLength(): Int

}


class vecN(val N: Int, val isCol: Int = 0, val issign: Boolean = false)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, UInt(xlen.W))

  def fromVecUInt(input: Vec[UInt]) = {
    data zip input map { case
      (
        a, b
        ) => a := b
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N, UInt(xlen.W)))
    x := data.asTypeOf(x)
    x
  }

  // FIXME : Add sign support
  def getdouble(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, UInt((data(0).getWidth * 2).W))))
    if (!issign) {
      uintdata zip data map { case (a, b) => a := OperatorResize.doublewidth(b) }
    } else {
      uintdata zip data map { case (a, b) => a := OperatorResize.doublewidth(b.asSInt).asUInt }
    }
    uintdata.asUInt
  }

  def gethalf(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, UInt((data(0).getWidth / 2).W))))
    if (!issign) {
      uintdata zip data map { case (a, b) => a := OperatorResize.halfwidth(b) }
    } else {
      uintdata zip data map { case (a, b) => a := OperatorResize.halfwidth(b.asSInt).asUInt }
    }
    uintdata.asUInt
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new vecN(N, isCol, issign).asInstanceOf[this.type]

}

class matNxN(val N: Int = 0, val issign: Boolean = false)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, Vec(N, UInt(xlen.W)))

  def fromVecUInt(input: Vec[UInt]) = {
    data.flatten zip input map { case
      (
        a, b
        ) => a := b
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N * N, UInt(xlen.W)))
    x := data.asTypeOf(x)
    x
  }

  def getdouble(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, UInt((data(0)(0).getWidth * 2).W))))
    if (!issign) {
      uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.doublewidth(b) }
    } else {
      uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.doublewidth(b.asSInt).asUInt }
    }
    uintdata.asUInt
  }

  def gethalf(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, UInt((data(0)(0).getWidth / 2).W))))
    if (!issign) {
      uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.halfwidth(b) }
    } else {
      uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.halfwidth(b.asSInt).asUInt }
    }
    uintdata.asUInt
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new matNxN(N, issign).asInstanceOf[this.type]
}


class FXmatNxN(val N: Int, val fraction: Int)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, Vec(N, FixedPoint(xlen.W, fraction.BP)))

  def fromVecUInt(input: Vec[UInt]) = {
    data.flatten zip input map { case
      (
        a, b
        ) => a := b.asFixedPoint(fraction.BP)
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N * N, UInt(xlen.W)))
    x zip data.flatten map { case (a, b) => a := b.asUInt }
    x
  }

  def getdouble(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, FixedPoint((data(0)(0).getWidth * 2).W, (data(0)(0).binaryPoint.get * 2).BP))))
    uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.doublewidth(b) }
    uintdata.asUInt
  }

  def gethalf(): UInt = {
    val uintdata = Wire(Vec(N, Vec(N, FixedPoint((data(0)(0).getWidth / 2).W, (data(0)(0).binaryPoint.get / 2).BP))))
    uintdata.flatten zip data.flatten map { case (a, b) => a := OperatorResize.halfwidth(b) }
    uintdata.asUInt
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new FXmatNxN(N, fraction).asInstanceOf[this.type]
}

class FXvecN(val N: Int, val fraction: Int, val isCol: Int = 0)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, FixedPoint(xlen.W, fraction.BP))

  def fromVecUInt(input: Vec[UInt]) = {
    data zip input map { case
      (
        a, b
        ) => a := b.asFixedPoint(fraction.BP)
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N, UInt(xlen.W)))
    x := data.asTypeOf(x)
    x
  }

  def getdouble(): UInt = {
    val uintdata = Wire(Vec(N, FixedPoint((data(0).getWidth * 2).W, (data(0).binaryPoint.get * 2).BP)))
    uintdata zip data map { case (a, b) => a := OperatorResize.doublewidth(b) }
    uintdata.asUInt
  }

  def gethalf(): UInt = {
    val uintdata = Wire(Vec(N, FixedPoint((data(0).getWidth / 2).W, (data(0).binaryPoint.get / 2).BP)))
    uintdata zip data map { case (a, b) => a := OperatorResize.halfwidth(b) }
    uintdata.asUInt
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new FXvecN(N, fraction).asInstanceOf[this.type]
}


class FPmatNxN(val N: Int, val t: FType)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, Vec(N, UInt(t.ieeeWidth.W)))

  def fromVecUInt(input: Vec[UInt]) = {
    data.flatten zip input map { case
      (
        a, b
        ) => a := b
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N * N, UInt(xlen.W)))
    x := data.asTypeOf(x)
    x
  }

  def getdouble(): UInt = {
    assert(false.B, "FPs do not have implicit resizing. Use ResizeFN function unit")
    0.U
  }

  def gethalf(): UInt = {
    assert(false.B, "FPs do not have implicit resizing. Use ResizeFN function unit")
    0.U
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new FPmatNxN(N, t).asInstanceOf[this.type]
}

class FPvecN(val N: Int, val t: FType, val isCol: Int = 0)(implicit p: Parameters) extends Shapes {
  val data = Vec(N, UInt(t.ieeeWidth.W))

  def fromVecUInt(input: Vec[UInt]) = {
    data zip input map { case
      (
        a, b
        ) => a := b
    }
  }

  def toVecUInt(): Vec[UInt] = {
    val x = Wire(Vec(N, UInt(xlen.W)))
    x := data.asTypeOf(x)
    x
  }

  def getdouble(): UInt = {
    assert(false.B, "FPs do not have implicit resizing. Use ResizeFN function unit")
    0.U
  }

  def gethalf(): UInt = {
    assert(false.B, "FPs do not have implicit resizing. Use ResizeFN function unit")
    0.U
  }

  def getWidthData(): Int = {
    return data.getWidth
  }

  def getLength(): Int = {
    return data.length
  }

  override def cloneType = new FPvecN(N, t).asInstanceOf[this.type]
}


// Wrapper for converting bits -> Shape and then invoking half/double and
// then convert back to bits.
class changeWidth[T <: Shapes](gen: T, val halfordouble: String) extends Module {
  val io    = IO(new Bundle {
    val in  = Input(UInt(gen.getWidth.W))
    val out =
      if (halfordouble == "double") {
        Output(UInt((gen.getWidth * 2).W))
      } else {
        Output(UInt((gen.getWidth / 2).W))
      }
  })
  val shape = io.in.asTypeOf(gen)
  if (halfordouble == "double") {
    io.out := shape.getdouble( )
  } else {
    io.out := shape.gethalf( )
  }
}

/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////// Generic Typeclass Op for a Shape ///////////////////////////
/////////////////////// To use: Uncomment and fill empty functions /////////////////////
//trait OperatorX[T] {
//  def X(l: T)(implicit p: Parameters): T
//}
//
//object OperatorX {
//
//  implicit object FXmatNxN extends OperatorX[FXmatNxN] {
//    def magic(l: FXmatNxN)(implicit p: Parameters): FXmatNxN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//  implicit object FXvecN extends OperatorX[FXvecN] {
//    def magic(l: FXvecN)(implicit p: Parameters): FXvecN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//  implicit object matNxN extends OperatorX[matNxN] {
//    def magic(l: matNxN)(implicit p: Parameters): matNxN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//
//  implicit object vecN extends OperatorX[vecN] {
//    def magic(l: vecN)(implicit p: Parameters): vecN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//  implicit object FPmatNxN extends OperatorX[FPmatNxN] {
//    def magic(l: FPmatNxN)(implicit p: Parameters): FPmatNxN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//  implicit object FPvecN extends OperatorX[FPvecN] {
//    def magic(l: FPvecN)(implicit p: Parameters): FPvecN = {
//      val x       = Wire(l.cloneType)
//      x
//    }
//  }
//
//  def X[T](l: T)(implicit op: OperatorX[T], p: Parameters): T = op.X(l)
//
//}

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

object operation {

  def addition[T](l: T, r: T)(implicit op: OperatorLike[T], p: Parameters): T = op.addition(l, r)

  def subtraction[T](l: T, r: T)(implicit op: OperatorLike[T], p: Parameters): T = op.subtraction(l, r)

  def multiplication[T](l: T, r: T)(implicit op: OperatorLike[T], p: Parameters): T = op.multiplication(l, r)

  def OpMagic[T](l: T, r: T, opcode: String
                )(
                  implicit op: OperatorLike[T]
                  , p: Parameters
                ): T = op.OpMagic(l, r, opcode)

  trait OperatorLike[T] {
    def addition(l: T, r: T)(implicit p: Parameters): T

    def subtraction(l: T, r: T)(implicit p: Parameters): T

    def multiplication(l: T, r: T)(implicit p: Parameters): T

    def OpMagic(l: T, r: T, opcode: String)(implicit p: Parameters): T
  }

  object OperatorLike {

    implicit object FPmatNxNlikeNumber extends OperatorLike[FPmatNxN] {
      def addition(l: FPmatNxN, r: FPmatNxN)(implicit p: Parameters): FPmatNxN = {
        val x = Wire(new FPmatNxN(l.N, l.t))
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            val FPadd = Module(new FPMAC(p(XLEN), "Add", l.t))
            FPadd.io.in1 := l.data(i)(j)
            FPadd.io.in2 := r.data(i)(j)
            x.data(i)(j) := FPadd.io.out
          }
        }
        x
      }

      def subtraction(l: FPmatNxN, r: FPmatNxN)(implicit p: Parameters): FPmatNxN = {
        val x = Wire(new FPmatNxN(l.N, l.t))
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            val FPadd = Module(new FPMAC(p(XLEN), "Sub", l.t))
            FPadd.io.in1 := l.data(i)(j)
            FPadd.io.in2 := r.data(i)(j)
            x.data(i)(j) := FPadd.io.out
          }
        }
        x
      }


      def multiplication(l: FPmatNxN, r: FPmatNxN)(implicit p: Parameters): FPmatNxN = {
        val x = Wire(new FPmatNxN(l.N, l.t))
        printf(p"Left: ${l.data}")
        val products = for (i <- 0 until l.N) yield {
          for (j <- 0 until l.N) yield {
            for (k <- 0 until l.N) yield {
              val FPadd = Module(new FPMAC(p(XLEN), "Mul", l.t))
              FPadd.io.in1 := l.data(i)(k)
              FPadd.io.in2 := r.data(k)(j)
              FPadd.io.out
            }
          }
        }
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            val FP_add_reduce = for (k <- 0 until l.N - 1) yield {
              val FPadd = Module(new FPMAC(p(XLEN), "Add", l.t))
              FPadd
            }

            for (k <- 0 until l.N - 2) {
              FP_add_reduce(k + 1).io.in1 := FP_add_reduce(k).io.out
              FP_add_reduce(k + 1).io.in2 := products(i)(j)(k + 2)
            }
            FP_add_reduce(0).io.in1 := products(i)(j)(0)
            FP_add_reduce(0).io.in2 := products(i)(j)(1)

            x.data(i)(j) := FP_add_reduce(l.N - 2).io.out
          }
        }
        x
      }

      def OpMagic(l: FPmatNxN, r: FPmatNxN, opcode: String)(implicit p: Parameters): FPmatNxN = {
        assert(false, "OpMagic does not exist for FP matrix")
        l
      }
    }

    implicit object matNxNlikeNumber extends OperatorLike[matNxN] {
      def addition(l: matNxN, r: matNxN)(implicit p: Parameters): matNxN = {
        val x = Wire(new matNxN(l.N))
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            x.data(i)(j) := l.data(i)(j) + r.data(i)(j)
          }
        }
        x
      }

      def subtraction(l: matNxN, r: matNxN)(implicit p: Parameters): matNxN = {
        val x = Wire(new matNxN(l.N))
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            x.data(i)(j) := l.data(i)(j) - r.data(i)(j)
          }
        }
        x
      }

      def multiplication(l: matNxN, r: matNxN)(implicit p: Parameters): matNxN = {
        val x        = Wire(new matNxN(l.N))
        val products = for (i <- 0 until l.N) yield {
          for (j <- 0 until l.N) yield {
            for (k <- 0 until l.N) yield
              l.data(i)(k) * r.data(k)(j)
          }
        }
        for (i <- 0 until l.N) {
          for (j <- 0 until l.N) {
            x.data(i)(j) := products(i)(j).reduceLeft(_ + _)
          }
        }
        x
      }

      def OpMagic(l: matNxN, r: matNxN, opcode: String)(implicit p: Parameters): matNxN = {
        assert(false, "OpMagic does not exist for matrix")
        l
      }
    }

    implicit object vecNlikeNumber extends OperatorLike[vecN] {
      def addition(l: vecN, r: vecN)(implicit p: Parameters): vecN = {
        assert(l.N == r.N)
        val x = Wire(new vecN(l.N))
        for (i <- 0 until l.N) {
          x.data(i) := l.data(i) + r.data(i)
        }
        x
      }

      def subtraction(l: vecN, r: vecN)(implicit p: Parameters): vecN = {
        assert(l.N == r.N)
        val x = Wire(new vecN(l.N))
        for (i <- 0 until l.N) {
          x.data(i) := l.data(i) - r.data(i)
        }
        x
      }

      def multiplication(l: vecN, r: vecN)(implicit p: Parameters): vecN = {
        assert(l.N == r.N)
        val x = Wire(new vecN(l.N))
        for (i <- 0 until l.N) {
          x.data(i) := l.data(i) * r.data(i)
        }
        x
      }

      def OpMagic(l: vecN, r: vecN, opcode: String)(implicit p: Parameters): vecN = {
        val x      = Wire(new vecN(l.N))
        val Op_FUs = Seq.fill(l.N)(Module(new UALU(p(XLEN), opcode)))
        for (i <- 0 until l.N) {
          Op_FUs(i).io.in1 := l.data(i)
          Op_FUs(i).io.in2 := r.data(i)
          x.data(i) := Op_FUs(i).io.out
        }
        x
      }
    }

  }

}

import operation._

class OperatorModule[T <: Shapes : OperatorLike](gen: => T, val opCode: String)(implicit val p: Parameters) extends Module {
  val io       = IO(new Bundle {
    val a = Flipped(Valid(gen))
    val b = Flipped(Valid(gen))
    val o = Output(Valid(gen))
  })
  val MatOrVec = (gen.className).toString
  io.o.valid := io.a.valid && io.b.valid
  if (opCode.toLowerCase( ) == "add") {
    io.o.bits := addition(io.a.bits, io.b.bits)
  } else if (opCode.toLowerCase( ) == "sub") {
    io.o.bits := subtraction(io.a.bits, io.b.bits)
  } else if (opCode.toLowerCase( ) == "mul") {
    io.o.bits := multiplication(io.a.bits, io.b.bits)
  } else if (MatOrVec.contains("Vec")) {
    io.o.bits := OpMagic(io.a.bits, io.b.bits, opCode)
  } else {
    assert(false, "Unknown TypCompute OpCode. Check Operator and/or Type!")
  }
}

class TypComputeIO(NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new TypBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new TypBundle))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new TypBundle))

  override def cloneType = new TypComputeIO(NumOuts).asInstanceOf[this.type]
}

class TypCompute[T <: Shapes : OperatorLike](NumOuts: Int, ID: Int, opCode: String)(sign: Boolean)(gen: => T)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new TypBundle)(p) {
  override lazy val io          = IO(new TypComputeIO(NumOuts))
  override      val printfSigil = opCode + "[" + classname.replaceAll("class node.", "") + "]_" + ID + ":"
  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val left_R                                = RegInit(TypBundle.default)
  // Memory Response
  val right_R                               = RegInit(TypBundle.default)
  // Output register
  val data_R                                = RegInit(TypBundle.default)
  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/
  val state     = RegInit(s_idle)
  val predicate = left_R.predicate & right_R.predicate & IsEnable( )

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/
  val start = left_R.valid & right_R.valid & IsEnableValid( )

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire( )) {
    //printfInfo("Latch left data\n")
    state := s_LATCH
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  io.RightIO.ready := ~right_R.valid
  when(io.RightIO.fire( )) {
    //printfInfo("Latch right data\n")
    state := s_LATCH
    right_R.data := io.RightIO.bits.data
    right_R.valid := true.B
    right_R.predicate := io.RightIO.bits.predicate
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_R.data
    io.Out(i).bits.valid := true.B
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
  }

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/
  // Predicate register
  val pred_R = RegInit(init = false.B)

  FU.io.a.bits := (left_R.data).asTypeOf(gen)
  FU.io.b.bits := (right_R.data).asTypeOf(gen)
  data_R.data := (FU.io.o.bits).asTypeOf(UInt(Typ_SZ.W))
  pred_R := predicate
  FU.io.a.valid := left_R.valid
  FU.io.b.valid := right_R.valid
  data_R.valid := FU.io.o.valid
  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  when(start & predicate & state =/= s_COMPUTE) {
    state := s_COMPUTE
    // Next cycle it will become valid.
    ValidOut( )
  }.elsewhen(start && !predicate && state =/= s_COMPUTE) {
    state := s_COMPUTE
    ValidOut( )
  }

  when(IsOutReady( ) && state === s_COMPUTE) {
    left_R := TypBundle.default
    right_R := TypBundle.default
    data_R := TypBundle.default
    Reset( )
    state := s_idle
  }
  val FU                = Module(new OperatorModule(gen, opCode))
  var classname: String = (gen.getClass).toString
  var signed            = if (sign == true) "S" else "U"

  if (log == true && (comp contains "TYPOP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"Left\": %x, \"Right\": %x},", x, (left_R.valid), (right_R.valid))
        printf("\"State\": {\"State\": \"%x\", \"(L,R)\": \"%x,%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },", state, left_R.data, right_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {}
    }
  }
}


