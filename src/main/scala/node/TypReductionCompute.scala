package node

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

import scala.reflect.runtime.universe._


object operationreduction {

  trait OperatorReductionLike[T] {
    def OpMagic(l: T, opcode: String)(implicit p: Parameters): UInt
  }

  object OperatorReductionLike {

    implicit object FPmatNxNlikeNumber extends OperatorReductionLike[FPmatNxN] {
      def OpMagic(l: FPmatNxN, opcode: String)(implicit p: Parameters): UInt = {
        assert(false, "OpMagic does not exist for FP matrix")
        l.data(0)(0)
      }
    }

    implicit object matNxNlikeNumber extends OperatorReductionLike[matNxN] {
      def OpMagic(l: matNxN, opcode: String)(implicit p: Parameters): UInt = {
        val res = Wire(new vecN(l.N))
        val rowvec =
          for (i <- 0 until l.N) yield {
            val x = Wire(new vecN(l.N))
            x.data := l.data(i)
            val y = vecNlikeNumber.OpMagic(x, opcode)
            y
          }
        res.data := VecInit(rowvec)
        vecNlikeNumber.OpMagic(res, opcode)
      }
    }

    implicit object vecNlikeNumber extends OperatorReductionLike[vecN] {
      def OpMagic(l: vecN, opcode: String)(implicit p: Parameters): UInt = {
        if (l.N == 1) return l.data(0)

        val ops = for (k <- 0 until l.N) yield {
          val reduction_node = Module(new UALU(p(XLEN), opcode))
          reduction_node.io.in1 <> DontCare
          reduction_node.io.in2 <> DontCare
          reduction_node
        }

        for (k <- 0 until l.N - 2) {
          ops(k + 1).io.in1 := ops(k).io.out
          ops(k + 1).io.in2 := l.data(k + 2)
        }
        ops(0).io.in1 := l.data(0)
        ops(0).io.in2 := l.data(1)

        ops(l.N - 2).io.out
      }
    }

  }

  def OpMagic[T](l: T, opcode: String
                )(
                  implicit op: OperatorReductionLike[T]
                  , p: Parameters
                ): UInt = op.OpMagic(l, opcode)

}

import operationreduction._

class OperatorReductionModule[T <: Shapes : OperatorReductionLike](gen: => T, val opCode: String)(implicit val p: Parameters) extends Module {
  val io       = IO(new Bundle {
    val a = Flipped(Valid(gen))
    val o = Output(Valid(UInt(p(XLEN).W)))
  })
  val MatOrVec = (gen.className).toString
  io.o.valid := io.a.valid
  io.o.bits := OpMagic(io.a.bits, opCode)
}


class TypReduceComputeIO(NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new TypBundle))

  override def cloneType = new TypReduceComputeIO(NumOuts).asInstanceOf[this.type]

}

class TypReduceCompute[T <: Shapes : OperatorReductionLike](NumOuts: Int, ID: Int, opCode: String)(sign: Boolean)(gen: => T)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new TypReduceComputeIO(NumOuts))

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val left_R = RegInit(TypBundle.default)

  // Output register
  val data_R = RegInit(DataBundle.default)

  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val state                                 = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = left_R.predicate & IsEnable( )
  val start     = left_R.valid & IsEnableValid( )

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  // Predicate register
  val pred_R = RegInit(init = false.B)

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire( )) {
    //printfInfo("Latch left data\n")
    state := s_LATCH
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_R.data
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := left_R.taskID | enable_R.taskID
  }

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  val FU = Module(new OperatorReductionModule(gen, opCode))

  FU.io.a.bits := (left_R.data).asTypeOf(gen)
  data_R.data := (FU.io.o.bits)
  pred_R := predicate
  FU.io.a.valid := left_R.valid
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
    data_R := TypBundle.default
    Reset( )
    state := s_idle
  }
  var classname: String = (gen.getClass).toString
  var signed            = if (sign == true) "S" else "U"
  override val printfSigil = opCode + "[" + classname.replaceAll("class node.", "") + "]_" + ID + ":"

  if (log == true && (comp contains "TYPOP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"Left\": %x},", x, (left_R.valid))
        printf("\"State\": {\"State\": \"%x\", \"(L)\": \"%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },", state, left_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {}
    }
  }
}

