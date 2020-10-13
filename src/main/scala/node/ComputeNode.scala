package node

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
import utility.UniformPrintfs


class ComputeNodeIO(NumOuts: Int)
                   (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle()))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle()))

  override def cloneType = new ComputeNodeIO(NumOuts).asInstanceOf[this.type]

}

class ComputeNode(NumOuts: Int, ID: Int, opCode: String)
                 (sign: Boolean)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {
  override lazy val io = IO(new ComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = right_R.taskID | left_R.taskID | enable_R.taskID

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)


  val predicate = enable_R.control

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code
  val FU = Module(new UALU(xlen, opCode))
  FU.io.in1 := left_R.data
  FU.io.in2 := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire()) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire()) {
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
    io.Out(i).bits := out_data_R
    io.Out(i).bits.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut()
          state := s_COMPUTE
          when(enable_R.control) {
            out_data_R.data := FU.io.out
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }.otherwise {
            out_data_R.data := 0.U
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B

        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] [COMPUTE] " +
            node_name + ": Output fired @ %d, Value: %d (%d + %d)\n", task_ID_R, cycleCount, FU.io.out, left_R.data, right_R.data)
        }
      }
    }
  }

}

/**
  * Fast version of compute node. It saves extra latching cycles
  * in scenarios which all the inputs are ready
  *
  * @param NumOuts
  * @param ID
  * @param opCode
  * @param sign
  * @param p
  * @param name
  * @param file
  */
class ComputeFastNode(NumOuts: Int, ID: Int, opCode: String)
                     (sign: Boolean)
                     (implicit val p: Parameters,
                      name: sourcecode.Name,
                      file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new Bundle {
    //Control Signal
    val enable = Flipped(Decoupled(new ControlBundle))

    //Input data
    val LeftIO = Flipped(Decoupled(new DataBundle()))
    val RightIO = Flipped(Decoupled(new DataBundle()))

    val Out = Vec(NumOuts, Decoupled(new DataBundle))
  })

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  //  val output_R = Seq.fill(NumOuts)(RegInit(DataBundle.default))
  val output_R = RegInit(DataBundle.default)
  val output_valid_R = Seq.fill(NumOuts)(RegInit(false.B))

  val fire_R = Seq.fill(NumOuts)(RegInit(false.B))

  val task_input = (io.enable.bits.taskID | enable_R.taskID)

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/


  val left_input = (io.LeftIO.bits.data & Fill(xlen, io.LeftIO.valid)) | (left_R.data & Fill(xlen, left_valid_R))
  val right_input = (io.RightIO.bits.data & Fill(xlen, io.RightIO.valid)) | (right_R.data & Fill(xlen, right_valid_R))

  val enable_input = (io.enable.bits.control & io.enable.valid) | (enable_R.control & enable_valid_R)

  val FU = Module(new UALU(xlen, opCode))
  FU.io.in1 := left_input
  FU.io.in2 := right_input

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire()) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire()) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  io.enable.ready := ~enable_valid_R
  when(io.enable.fire()) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  // Defalut values for output
  val predicate = enable_input

  output_R.data := FU.io.out
  output_R.predicate := predicate
  output_R.taskID := task_input

  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> output_R
    io.Out(i).valid <> output_valid_R(i)
  }

  for (i <- 0 until NumOuts) {
    when(io.Out(i).fire) {
      output_valid_R(i) := false.B
      fire_R(i) := true.B
    }
  }

  val fire_mask = (fire_R zip io.Out.map(_.fire)).map { case (a, b) => a | b }

  def IsEnableValid(): Bool = {
    return enable_valid_R || io.enable.fire
  }

  def IsLeftValid(): Bool = {
    return left_valid_R || io.LeftIO.fire
  }

  def IsRightValid(): Bool = {
    return right_valid_R || io.RightIO.fire
  }


  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state) {
    is(s_idle) {

      when(IsEnableValid() && IsLeftValid() && IsRightValid()) {

        output_valid_R.foreach(_ := true.B)

        state := s_fire

        if (log) {
          printf(f"[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %d (%d + %d)\n",
            task_input, cycleCount, FU.io.out.asSInt(), left_input.asSInt(), right_input.asSInt())
        }
      }
    }

    is(s_fire) {
      when(fire_mask.reduce(_ & _)) {

        left_R := DataBundle.default
        left_valid_R := false.B

        right_R := DataBundle.default
        right_valid_R := false.B

        enable_R := ControlBundle.default
        enable_valid_R := false.B

        output_R := DataBundle.default
        output_valid_R.foreach(_ := false.B)

        fire_R.foreach(_ := false.B)

        state := s_idle
      }
    }
  }

}

