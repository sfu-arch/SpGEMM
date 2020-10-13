package FPU

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import hardfloat._
import interfaces._
import muxes._
import util._
import node._
import FType._

class FNtoFNNodeIO(Src: FType, Des: FType,NumOuts: Int)
                   (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new CustomDataBundle(UInt((Des.ieeeWidth).W))) {
  // LeftIO: Left input data for computation
  //Input for floating point width
    val Input = Flipped(Decoupled(new CustomDataBundle(UInt((Src.ieeeWidth).W))))
    // Output gets initialized as part of Handshaking.

  override def cloneType = new FNtoFNNodeIO(Src, Des, NumOuts).asInstanceOf[this.type]
}

class FNtoFNNode(Src: FType, Des: FType, NumOuts: Int, ID: Int)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new CustomDataBundle(UInt((Des.ieeeWidth).W)))(p) {
  override lazy val io = IO(new FNtoFNNodeIO(Src,Des, NumOuts))


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
  val Input_R = RegInit(CustomDataBundle.default((UInt((Src.ieeeWidth).W))))
  val Input_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(CustomDataBundle.default((UInt((Src.ieeeWidth).W))))

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)


  val predicate = Input_R.predicate //  IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate FN resize with selected code. Recoded FU.
  val recFNsrc = Src.recode(Input_R.data)
  val FU = Module(new RecFNToRecFN(Src.expWidth, Src.sigWidth, Des.expWidth, Des.sigWidth))
  FU.io.in             := recFNsrc
  FU.io.roundingMode   := "b110".U(3.W)
  FU.io.detectTininess := 0.U(1.W)


  io.Input.ready := ~Input_valid_R
  when(io.Input.fire()) {
    Input_R <> io.Input.bits
    Input_valid_R := true.B
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
        when(Input_valid_R) {
          ValidOut()
          when(enable_R.control) {
            out_data_R.data := Des.ieee(FU.io.out)
            out_data_R.predicate := predicate
            out_data_R.taskID := Input_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        Input_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset()
        printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] " + node_name + ": Output fired @ %d, Value: 0x%x\n", task_ID_R, cycleCount, Des.ieee(FU.io.out))
      }
    }
  }

}
