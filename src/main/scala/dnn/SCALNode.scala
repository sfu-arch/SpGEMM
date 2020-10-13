package dnn

import chisel3._
import chisel3.util.{Decoupled, Enum, Valid}
import chisel3.{Bundle, Flipped, Module, Output, RegInit, UInt, assert, printf, when}
import config.{Parameters, XLEN}
import config._
import dnn.types.{OperatorSCAL}
import interfaces.CustomDataBundle
//import javafx.scene.chart.PieChart.Data
import node.{AluGenerator, HandShakingIONPS, HandShakingNPS, Shapes}

class SCALFU[L <: Shapes : OperatorSCAL](left: => L, lanes: Int, opCode: String)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(left))
    val b = Flipped(Valid(UInt(p(XLEN).W)))
    val o = Output(Valid(left))
  })


  val start = io.a.valid && io.b.valid
  val FU    = OperatorSCAL.magic(io.a.bits, io.b.bits, start, lanes, opCode)
  io.o.bits := FU._1
  val latency = FU._2
  val latCnt  = Module(new SatCounterModule(latency))
  latCnt.io.start := start
  io.o.valid := latCnt.io.wrap
}

class SCALIO[L <: Shapes](NumOuts: Int)(left: => L)(implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt((left.getWidth).W))) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new CustomDataBundle(UInt((left.getWidth).W))))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new CustomDataBundle(UInt(xlen.W))))

  override def cloneType = new SCALIO(NumOuts)(left).asInstanceOf[this.type]
}

class SCALNode[L <: Shapes : OperatorSCAL](NumOuts: Int, ID: Int, lanes: Int, opCode: String)(left: => L)(implicit p: Parameters)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt(left.getWidth.W)))(p) {
  override lazy val io = IO(new SCALIO(NumOuts)(left))

  /*===========================================*
 *            Registers                      *
 *===========================================*/
  // OP Inputs
  val left_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))

  // Memory Response
  val right_R = RegInit(CustomDataBundle.default(0.U((xlen).W)))

  // Output register
  val data_R = RegInit(CustomDataBundle.default(0.U((left.getWidth).W)))

  val s_idle :: s_LATCH :: s_ACTIVE :: s_COMPUTE :: Nil = Enum(4)
  val state                                             = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = left_R.predicate & right_R.predicate & IsEnable( )
  val start     = left_R.valid & right_R.valid & IsEnableValid( )

  /*===============================================*
   *            Latch inputs. Wire up left       *
   *===============================================*/

  // Predicate register
  val pred_R = RegInit(init = false.B)

  //printfInfo("start: %x\n", start)

  io.LeftIO.ready := ~left_R.valid
  when(io.LeftIO.fire( )) {
    //printfInfo("Latch left data\n")
    left_R.data := io.LeftIO.bits.data
    left_R.valid := true.B
    left_R.predicate := io.LeftIO.bits.predicate
  }

  io.RightIO.ready := ~right_R.valid
  when(io.RightIO.fire( )) {
    //printfInfo("Latch right data\n")
    right_R.data := io.RightIO.bits.data
    right_R.valid := true.B
    right_R.predicate := io.RightIO.bits.predicate
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_R.data
    io.Out(i).bits.valid := data_R.valid
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
  }

  /*============================================*
 *            ACTIONS (possibly dangerous)    *
 *============================================*/

  val FU = Module(new SCALFU(left, lanes, opCode))
  FU.io.a.bits := (left_R.data).asTypeOf(left)
  FU.io.b.bits := (right_R.data)

  data_R.predicate := predicate
  pred_R := predicate
  FU.io.a.valid := false.B
  FU.io.b.valid := false.B
  //  This is written like this to enable FUs that are dangerous in the future.
  // If you don't start up then no value passed into function
  when(start & state === s_idle) {
    when(predicate) {
      FU.io.a.valid := true.B
      FU.io.b.valid := true.B
      state := s_ACTIVE
    }.otherwise {
      state := s_COMPUTE
      ValidOut( )
    }
  }

  when(state === s_ACTIVE) {
    when(FU.io.o.valid) {
      ValidOut( )
      data_R.data := (FU.io.o.bits).asTypeOf(UInt(left.getWidth.W))
      data_R.valid := true.B
      state := s_COMPUTE
    }.otherwise {
      state := s_ACTIVE
    }
  }
  when((IsOutReady( )) && (state === s_COMPUTE)) {
    left_R := CustomDataBundle.default(0.U((left.getWidth).W))
    right_R := CustomDataBundle.default(0.U((left.getWidth).W))
    data_R := CustomDataBundle.default(0.U((left.getWidth).W))
    Reset( )
    state := s_idle
  }


  var classname: String = (left.getClass).toString
  var signed            = "S"
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
        printf("\"State\": {\"State\": \"%x\", \"(L,R)\": \"%x,%x\",  \"O(V,D,P)\": \"%x,%x,%x\" },",
          state, left_R.data, right_R.data, io.Out(0).valid, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {
      }
    }
  }
}


