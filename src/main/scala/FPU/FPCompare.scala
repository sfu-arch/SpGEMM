package FPU

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
import FType._
import hardfloat._

/**
  * [FPComputeNodeIO description]
  */
class FPCompareNodeIO(NumOuts: Int)
                     (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle( )))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle( )))
}

/**
  * [FPCompareNode description]
  */
class FPCompareNode(NumOuts: Int, ID: Int, opCode: String)
                   (t: FType)
                   (implicit p: Parameters,
                    name: sourcecode.Name,
                    file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new ComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  val FU = opCode match {
    case "<LT" => Module(new FPALU(FloatingPoint(t), "LT"))
    case ">GT" => Module(new FPALU(FloatingPoint(t), "GT"))
    case "=EQ" => Module(new FPALU(FloatingPoint(t), "EQ"))
    case "<=LTE" => Module(new FPALU(FloatingPoint(t), "LTE"))
    case ">=GTE" => Module(new FPALU(FloatingPoint(t), "GTE"))
    case "LT" => Module(new FPALU(FloatingPoint(t), "LT"))
    case "GT" => Module(new FPALU(FloatingPoint(t), "GT"))
    case "EQ" => Module(new FPALU(FloatingPoint(t), "EQ"))
    case "LTE" => Module(new FPALU(FloatingPoint(t), "LTE"))
    case "GTE" => Module(new FPALU(FloatingPoint(t), "GTE"))
    case _ => {
      assert(false, "Opcode not valid")
      Module(new FPALU(FloatingPoint(t), "EQ"))
    }
  }

  FU.io.in1 := FloatingPoint(left_R.data, t)
  FU.io.in2 := FloatingPoint(right_R.data, t)


  //  val FU = Module(new CompareRecFN(t.expWidth, t.sigWidth))
  //  FU.io.a   := t.recode(left_R.data)
  // FU.io.b   := t.recode(right_R.data)

  //@todo make sure you fixed DontCare signal
  //  FU.io.signaling <> DontCare

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          when(enable_R.control) {
            out_data_R.data := FU.io.out
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] " + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, io.Out(0).bits.data)
      }
    }
  }

}
