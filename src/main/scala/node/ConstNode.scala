package node

import chisel3._
import chisel3.Module
import config._
import interfaces.{ControlBundle, DataBundle}
import util._
import utility.UniformPrintfs

class ConstNode(value: Int, NumOuts: Int = 1, ID: Int)
               (implicit p: Parameters,
                name: sourcecode.Name,
                file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {

  override lazy val io = IO(new HandShakingIONPS(NumOuts)(new DataBundle()))
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  //val task_ID_R = RegNext(next = enable_R.taskID)
  val task_ID_W = io.enable.bits.taskID

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)


  //val predicate = left_R.predicate & right_R.predicate// & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

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
      when(io.enable.fire()) {
        ValidOut()
        when(io.enable.bits.control) {

          // NOTE: Remember when you are casting signed values you have put the width as well
          out_data_R.data := value.asSInt(xlen.W).asUInt()
          out_data_R.predicate := io.enable.bits.control
          out_data_R.taskID := task_ID_W
        }
        state := s_COMPUTE
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        //Reset state
        state := s_IDLE
        out_data_R.predicate := false.B
        Reset()
        if (p(TRACE)) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] " +
            node_name + ": Output fired @ %d, Value: %d\n",
            task_ID_W, cycleCount, value.asSInt(xlen.W))
        }
      }
    }
  }
}

class ConstFastNode(value: Int, ID: Int)
                   (implicit val p: Parameters,
                    name: sourcecode.Name,
                    file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(new ControlBundle))
    val Out = Decoupled(new DataBundle)
  })

  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)


  /*===========================================*
   *            Registers                      *
   *===========================================*/
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  val task_input = (io.enable.bits.taskID | enable_R.taskID)

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/


  //  io.Out.bits.data := value.asSInt(xlen.W).asUInt()
  val output_value = value.asSInt(xlen.W).asUInt()

  io.enable.ready := false.B

  io.Out.bits.data := output_value
  io.Out.bits.predicate := enable_R.control
  io.Out.bits.taskID := task_input
  io.Out.valid := false.B

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state) {
    is(s_idle) {
      io.enable.ready := true.B
      io.Out.valid := false.B

      when(io.enable.fire()) {

        io.Out.valid := true.B
        io.Out.bits.predicate := io.enable.bits.control
        io.Out.bits.taskID := io.enable.bits.taskID

        enable_R <> io.enable.bits
        state := s_fire

        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] [CONST] "
            + node_name + ": Output fired @ %d, Value: %d\n",
            task_input, cycleCount, output_value.asSInt())
        }
      }
    }

    is(s_fire) {
      io.Out.valid := true.B

      when(io.Out.fire) {

        enable_R := ControlBundle.default
        enable_valid_R := false.B

        state := s_idle
      }
    }
  }

}


