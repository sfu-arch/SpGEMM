package dnn

import chisel3._
import chisel3.util._
import chisel3.{Bundle, Flipped, Module, Output, RegInit, UInt, assert, printf, when}
import config.{Parameters, XLEN}
import config._
import dnn.types.{OperatorDot}
import interfaces.CustomDataBundle
//import javafx.scene.chart.PieChart.Data
import node.{AluGenerator, HandShakingIONPS, HandShakingNPS, Shapes}

class DotFU[gen <: Shapes : OperatorDot](left: => gen, lanes: Int, opCode: String)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(left))
    val b = Flipped(Valid(left))
    val o = Output(Valid(left))
  })


  val start = io.a.valid && io.b.valid
  val FU = OperatorDot.magic(io.a.bits, io.b.bits, start, lanes, opCode)
  io.o.bits := FU._1
  val latency = FU._2
  val latCnt = Module(new SatCounterModule(latency))
  latCnt.io.start := start
  io.o.valid := latCnt.io.wrap
}

class DotIO[gen <: Shapes](NumOuts: Int)(left: => gen)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt((left.getWidth).W))) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new CustomDataBundle(UInt((left.getWidth).W))))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new CustomDataBundle(UInt((left.getWidth).W))))

  override def cloneType = new DotIO(NumOuts)(left).asInstanceOf[this.type]
}

class DotNode[L <: Shapes : OperatorDot](NumOuts: Int, ID: Int, lanes: Int, opCode: String)(left: => L)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt(left.getWidth.W)))(p) {
  override lazy val io = IO(new DotIO(NumOuts)(left))

  /*===========================================*
 *            Registers                      *
 *===========================================*/
  // OP Inputs
  val left_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))
  val left_valid_R = RegInit(false.B)

  // Memory Response
  val right_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))
  val right_valid_R = RegInit(false.B)

  // Output register
  val data_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))

  val s_idle :: s_compute :: s_finish :: Nil = Enum(3)
  val state = RegInit(s_idle)

  /*===============================================*
   *            Latch inputs. Wire up left       *
   *===============================================*/

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire()) {
    left_R := io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire()) {
    right_R := io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
  }

  val FU = Module(new DotFU(left, lanes, opCode))
  FU.io.a.bits := (left_R.data).asTypeOf(left)
  FU.io.b.bits := (right_R.data).asTypeOf(left)

  FU.io.a.valid := false.B //true.B
  FU.io.b.valid := false.B //true.B

  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  switch(state){
    is(s_idle){
      when(left_valid_R && right_valid_R){
        state := s_compute
        FU.io.a.valid := true.B
        FU.io.b.valid := true.B
      }
    }
    is(s_compute){
      when(FU.io.o.valid){
        ValidOut()
        data_R.data := (FU.io.o.bits).asTypeOf(UInt(left.getWidth.W))
        state := s_finish
      }
    }
    is(s_finish){
      when(IsOutReady()){
        left_R := CustomDataBundle.default(0.U((left.getWidth).W))
        left_valid_R := false.B

        right_R := CustomDataBundle.default(0.U((left.getWidth).W))
        right_valid_R := false.B

        data_R := CustomDataBundle.default(0.U((left.getWidth).W))
        Reset()

        state := s_idle

      }
    }
  }

  /**
    * Cant print value with more than 64bits.
    * In this example value is > 64bits
    */
  //printf(p"\n State : ${state} Predicate ${predicate} Left ${left_R} Right ${right_R} Output: ${data_R}")

  var classname: String = (left.getClass).toString
  var signed = "S"
  override val printfSigil =
    opCode + "[" + classname.replaceAll("class node.", "") + "]_" + ID + ":"

  if (log == true && (comp contains "TYPOP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {
      }
      case "med" => {
      }
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"Left\": %x, \"Right\": %x},", x, (left_R.valid), (right_R.valid))
        printf("\"State\": {\"State\": \"%x\", \"(L,R)\": \"%x,%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },", state, left_R.data, right_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire())
        printf("}")
      }
      case everythingElse => {
      }
    }
  }
}


