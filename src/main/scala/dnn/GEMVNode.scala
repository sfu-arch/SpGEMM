package dnn

import FPU.{FPMAC, FType}
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import dnn.types.{GEMV_OpCode, GEMV_fns}
import interfaces._
import muxes._
import util._
import node._

class OperatorMatVecModule[L <: Shapes, R <: Shapes, O <: Shapes](left: => L, right: => R, output: => O, val opCode: String)
                                                                 (implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(left))
    val b = Flipped(Valid(right))
    val o = Output(Valid(output))
  })
  // Check if right is vec
  val is_vec = right.getClass.getName
  if (!(is_vec.contains("vec"))) {
    assert(false, "Right operand. Only vector!")
  }

  val aluOp = GEMV_fns.getfns(io.a.bits, io.b.bits)
  require(!GEMV_OpCode.opMap.get(opCode).isEmpty, "Wrong matrix OP. Check operator!")

  io.o.valid := io.a.valid && io.b.valid

  io.o.bits := AluGenerator(GEMV_OpCode.opMap(opCode), aluOp)

}

class GEMVIO[L <: Shapes, R <: Shapes, O <: Shapes](NumOuts: Int)(left: => L, right: => R)(output: => O)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt((output.getWidth).W))) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new CustomDataBundle(UInt((left.getWidth).W))))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new CustomDataBundle(UInt((right.getWidth).W))))

  override def cloneType = new GEMVIO(NumOuts)(left, right)(output).asInstanceOf[this.type]
}

class GEMV_1Cycle[L <: Shapes, R <: Shapes, O <: Shapes](NumOuts: Int, ID: Int, opCode: String)
                                                        (sign: Boolean)(left: => L, right: R)(output: => O)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt(output.getWidth.W)))(p) {
  override lazy val io = IO(new GEMVIO(NumOuts)(left, right)(output))

  /*===========================================*
 *            Registers                      *
 *===========================================*/
  // OP Inputs
  val left_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))

  // Memory Response
  val right_R = RegInit(CustomDataBundle.default(0.U((right.getWidth).W)))

  // Output register
  val data_R = RegInit(CustomDataBundle.default(0.U((output.getWidth).W)))

  val s_idle :: s_LATCH :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(4)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = left_R.predicate & right_R.predicate & IsEnable()
  val start = left_R.valid & right_R.valid & IsEnableValid()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  // Predicate register
  val pred_R = RegInit(init = false.B)

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire()) {
    //printfInfo("Latch left data\n")
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  io.RightIO.ready := ~right_R.valid
  when(io.RightIO.fire()) {
    //printfInfo("Latch right data\n")
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

  val FU = Module(new OperatorMatVecModule(left, right, output, opCode))
  FU.io.a.bits := (left_R.data).asTypeOf(left)
  FU.io.b.bits := (right_R.data).asTypeOf(right)
  data_R.data := (FU.io.o.bits).asTypeOf(UInt(output.getWidth.W))
  data_R.predicate := predicate
  pred_R := predicate
  FU.io.a.valid := left_R.valid
  FU.io.b.valid := right_R.valid
  data_R.valid := FU.io.o.valid

  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  when(start & predicate & state =/= s_COMPUTE) {
    state := s_COMPUTE
    // Next cycle it will become valid.
    ValidOut()
  }.elsewhen(start && !predicate && state =/= s_COMPUTE) {
    state := s_COMPUTE
    ValidOut()
  }

  when(IsOutReady() && state === s_COMPUTE) {
    left_R := CustomDataBundle.default(0.U((left.getWidth).W))
    right_R := CustomDataBundle.default(0.U((right.getWidth).W))
    data_R := CustomDataBundle.default(0.U((output.getWidth).W))
    Reset()
    state := s_idle
  }

  printf(p"\n State : ${state} Predicate ${predicate} Left ${left_R} Right ${right_R} Output: ${data_R}")

  var classname: String = (left.getClass).toString
  var signed = if (sign == true) "S" else "U"
  override val printfSigil =
    opCode + "[" + classname.replaceAll("class node.", "") + "]_" + ID + ":"

  if ((log == true) && (comp contains "TYPOP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {
      }
      case "med" => {
      }
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"Left\": %x, \"Right\": %x},", x, (left_R.valid), (right_R.valid))
        printf("\"State\": {\"State\": \"%x\", \"(L,R)\": \"%x,%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },",
          state, left_R.data, right_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire())
        printf("}")
      }
      case everythingElse => {
      }
    }
  }
}


