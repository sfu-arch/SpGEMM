package control

import java.util.ResourceBundle.Control

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import utility.UniformPrintfs
import node._
import config._
import interfaces._
import muxes._
import util._


/**
  * @brief BasicBlockIO class definition
  * @details Implimentation of BasickBlockIO
  * @param NumInputs Number of predecessors
  * @param NumOuts   Number of successor instructions
  * @param NumPhi    Number existing phi nodes
  */

class BasicBlockIO(NumInputs: Int,
                   NumOuts: Int,
                   NumPhi: Int)
                  (implicit p: Parameters)
  extends HandShakingCtrlMaskIO(NumInputs, NumOuts, NumPhi) {

  val predicateIn = Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))

  override def cloneType = new BasicBlockIO(NumInputs, NumOuts, NumPhi).asInstanceOf[this.type]
}


/**
  * @brief BasicBlockIO class definition
  * @details Implimentation of BasickBlockIO
  * @param NumInputs Number of predecessors
  * @param NumOuts   Number of successor instructions
  * @param NumPhi    Number existing phi nodes
  * @param BID       BasicBlock ID
  * @note The logic for BasicBlock nodes differs from Compute nodes.
  *       In the BasicBlock nodes, as soon as one of the input signals get fires
  *       all the inputs should get not ready, since we don't need to wait for other
  *       inputs.
  */

class BasicBlockNode(NumInputs: Int,
                     NumOuts: Int,
                     NumPhi: Int,
                     BID: Int)
                    (implicit p: Parameters,
                     name: sourcecode.Name,
                     file: sourcecode.File)
  extends HandShakingCtrlMask(NumInputs, NumOuts, NumPhi, BID)(p) {

  override lazy val io = IO(new BasicBlockIO(NumInputs, NumOuts, NumPhi))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  override val printfSigil = node_name + BID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  //Assertion
  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val predicate_in_R = Seq.fill(NumInputs)(RegInit(ControlBundle.default))
  val predicate_control_R = RegInit(VecInit(Seq.fill(NumInputs)(false.B)))
  val predicate_valid_R = Seq.fill(NumInputs)(RegInit(false.B))

  val s_IDLE :: s_LATCH :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*===========================================*
   *            Valids                         *
   *===========================================*/

  val predicate = predicate_in_R.map(_.control).reduce(_ | _)
  val predicate_task = predicate_in_R.map(_.taskID).reduce(_ | _)

  val start = (io.predicateIn.map(_.fire()) zip predicate_valid_R) map { case (a, b) => a | b } reduce (_ & _)

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/


  for (i <- 0 until NumInputs) {
    io.predicateIn(i).ready := ~predicate_valid_R(i)
    when(io.predicateIn(i).fire()) {
      predicate_in_R(i) <> io.predicateIn(i).bits
      predicate_control_R(i) <> io.predicateIn(i).bits.control
      predicate_valid_R(i) := true.B
    }
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.control := predicate
    io.Out(i).bits.taskID := predicate_task
  }

  // Wire up mask output
  for (i <- 0 until NumPhi) {
    io.MaskBB(i).bits := predicate_control_R.asUInt()
  }


  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(start) {
        ValidOut()
        state := s_LATCH
        //        assert(PopCount(predicate_control_R) < 2.U)
      }
    }
    is(s_LATCH) {
      when(IsOutReady()) {
        predicate_valid_R.foreach(_ := false.B)
        Reset()
        state := s_IDLE

        when(predicate) {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [BB]   " +
              node_name + ": Output fired @ %d, Mask: %d\n", predicate_task
              , cycleCount, predicate_control_R.asUInt())
          }
        }.otherwise {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] " + node_name + ": Output fired @ %d -> 0 predicate\n", cycleCount)
          }
        }
      }
    }

  }


}


/**
  * @brief BasicBlockIO class definition
  * @details Implimentation of BasickBlockIO
  * @param NumInputs Number of predecessors
  * @param NumOuts   Number of successor instructions
  * @param NumPhi    Number existing phi nodes
  * @param BID       BasicBlock ID
  * @note The logic for BasicBlock nodes differs from Compute nodes.
  *       In the BasicBlock nodes, as soon as one of the input signals get fires
  *       all the inputs should get not ready, since we don't need to wait for other
  *       inputs.
  */

@deprecated("We don't want to have a speicific node for loop head, Loop head should be handle with LoopNode", "dataflow-lib 1.0")
class BasicBlockLoopHeadNode(NumInputs: Int,
                             NumOuts: Int,
                             NumPhi: Int,
                             BID: Int)
                            (implicit p: Parameters,
                             name: sourcecode.Name,
                             file: sourcecode.File)
  extends HandShakingCtrlMask(NumInputs, NumOuts, NumPhi, BID)(p) {

  override lazy val io = IO(new BasicBlockIO(NumInputs, NumOuts, NumPhi))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  override val printfSigil = node_name + BID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  //Assertion
  assert(NumPhi >= 1, "NumPhi Cannot be zero")
  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val predicate_in_R = RegInit(VecInit(Seq.fill(NumInputs)(false.B)))

  val predicate_valid_R = RegInit(false.B)
  val predicate_valid_W = WireInit(VecInit(Seq.fill(NumInputs)(false.B)))

  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val state = RegInit(s_idle)

  /*===========================================*
   *            Valids                         *
   *===========================================*/

  val predicate = predicate_in_R.asUInt().orR
  val start = predicate_valid_R.asUInt().orR

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  val pred_R = RegInit(ControlBundle.default)
  val fire_W = WireInit(false.B)


  //Make all the inputs invalid if one of the inputs
  //gets fire
  //
  when(state === s_idle) {
    predicate_valid_W := VecInit(Seq.fill(NumInputs)(false.B))
  }

  fire_W := predicate_valid_W.asUInt.orR

  when(fire_W & state === s_idle) {
    predicate_valid_R := true.B
  }

  for (i <- 0 until NumInputs) {
    io.predicateIn(i).ready := ~predicate_valid_R
    when(io.predicateIn(i).fire()) {
      state := s_LATCH
      predicate_in_R(i) <> io.predicateIn(i).bits.control
      predicate_valid_W(i) := true.B
    }
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.control := pred_R.control
    io.Out(i).bits.taskID := 0.U
  }

  // Wire up mask output
  for (i <- 0 until NumPhi) {
    io.MaskBB(i).bits := predicate_in_R.asUInt
  }


  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  when(start & state =/= s_COMPUTE) {
    when(predicate) {
      state := s_COMPUTE
      pred_R.control := predicate
      ValidOut()
    }.otherwise {
      state := s_idle
      predicate_valid_R := false.B
    }
  }

  /*==========================================*
   *      Output Handshaking and Reset        *
   *==========================================*/


  val out_ready_W = out_ready_R.asUInt.andR
  val out_valid_W = out_valid_R.asUInt.andR

  val mask_ready_W = mask_ready_R.reduceLeft(_ && _)
  val mask_valid_W = mask_valid_R.reduceLeft(_ && _)


  // Reseting all the latches
  when(out_ready_W & mask_ready_W & (state === s_COMPUTE)) {
    predicate_in_R := VecInit(Seq.fill(NumInputs)(false.B))
    predicate_valid_R := false.B

    // Reset output
    out_ready_R := VecInit(Seq.fill(NumOuts)(false.B))

    //Reset state
    state := s_idle
    when(predicate) {
      if (log) {
        printf("[LOG] " + "[" + module_name + "] " + node_name + ": Output fired @ %d, Mask: %d\n", cycleCount, predicate_in_R.asUInt())
      }
    }.otherwise {
      if (log) {
        printf("[LOG] " + "[" + module_name + "] " + node_name + ": Output fired @ %d -> 0 predicate\n", cycleCount)
      }
    }
    //Restart predicate bit
    pred_R.control := false.B
  }

}

/**
  * @brief BasicBlockIO class definition
  * @details Implimentation of BasickBlockIO
  * @param NumOuts Number of successor instructions
  */

class BasicBlockNoMaskDepIO(NumOuts: Int)
                           (implicit p: Parameters)
  extends HandShakingCtrlNoMaskIO(NumOuts) {
  //  val predicateIn = Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))
  val predicateIn = Flipped(Decoupled(new ControlBundle()))

  override def cloneType = new BasicBlockNoMaskDepIO(NumOuts).asInstanceOf[this.type]
}


/**
  * @brief BasicBlockIO class definition
  * @details Implimentation of BasickBlockIO
  * @param NumInputs Number of predecessors
  * @param NumOuts   Number of successor instructions
  * @param BID       BasicBlock ID
  */

@deprecated("Start using BasicBlockNoMaskNodeFast", "dataflow-lib 1.0")
class BasicBlockNoMaskNode(NumInputs: Int = 1,
                           NumOuts: Int,
                           BID: Int)
                          (implicit p: Parameters,
                           name: sourcecode.Name,
                           file: sourcecode.File)
  extends HandShakingCtrlNoMask(NumInputs, NumOuts, BID)(p) {

  override lazy val io = IO(new BasicBlockNoMaskDepIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = node_name + BID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  val predicate_in_R = RegInit(ControlBundle.default)
  val predicate_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.predicateIn.ready := ~predicate_valid_R
  when(io.predicateIn.fire()) {
    predicate_in_R <> io.predicateIn.bits
    predicate_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> predicate_in_R
  }


  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(io.predicateIn.fire()) {
        ValidOut()
        state := s_COMPUTE
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        predicate_in_R <> ControlBundle.default
        predicate_valid_R := false.B
        state := s_IDLE

        Reset()

        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output [T] fired @ %d\n",
            predicate_in_R.taskID, cycleCount)
        }
      }
    }

  }

}


/**
  * BasicBlockNoMaskFastNode
  *
  * @param BID
  * @param NumOuts
  * @param p
  * @param name
  * @param file
  */

class BasicBlockNoMaskFastIO(val NumOuts: Int)(implicit p: Parameters)
  extends CoreBundle()(p) {
  // Output IO
  val predicateIn = Flipped(Decoupled(new ControlBundle()))
  val Out = Vec(NumOuts, Decoupled(new ControlBundle))

  //  3.1 spec
  override def cloneType = new BasicBlockNoMaskFastIO(NumOuts).asInstanceOf[this.type]
}


@deprecated("BasicBlockNoMaskFastNode2 is deprecated. It doesn't handle some corner cases. Please use BasicBlockNoMaskFastNode", "dataflow-lib 1.0")
class BasicBlockNoMaskFastNode2(BID: Int, val NumOuts: Int)
                               (implicit val p: Parameters,
                                name: sourcecode.Name, file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new BasicBlockNoMaskFastIO(NumOuts)(p))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + BID + " "

  /*===========================================*
 *            Registers                      *
 *===========================================*/

  val out_ready_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_R = RegInit(VecInit(Seq.fill(NumOuts)(ControlBundle.default)))

  val predicate_in_R = RegInit(ControlBundle.default)
  val predicate_valid_R = RegInit(false.B)

  val allReady = out_ready_R.reduceLeft(_ && _)

  for (i <- 0 until NumOuts) {
    when(io.Out(i).fire()) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }


  io.predicateIn.ready := ~predicate_valid_R
  when(io.predicateIn.fire()) {
    predicate_in_R <> io.predicateIn.bits
    predicate_valid_R := true.B
  }

  /*============================================*
   *            ACTIONS                         *
   *============================================*/
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  for (i <- 0 until NumOuts) {
    when(io.predicateIn.fire) {
      io.Out(i).valid := true.B
    }.otherwise {
      io.Out(i).valid := out_valid_R(i)
    }
  }

  //Value initilization
  io.Out.map(_.bits).foreach(_ := predicate_in_R)

  val task_ID = io.predicateIn.bits.taskID


  switch(state) {
    is(s_idle) {
      io.predicateIn.ready := true.B
      io.Out.map(_.bits).foreach(_ := io.predicateIn.bits)

      // State change
      when(io.predicateIn.fire) {
        state := s_fire
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] "
            + node_name + ": Output [T] fired @ %d\n", task_ID, cycleCount)
        }
      }

    }
    is(s_fire) {
      io.predicateIn.ready := false.B

      io.Out.map(_.bits).foreach(_ := predicate_in_R)

      // Restarting the registers
      when(allReady) {
        predicate_in_R := ControlBundle.default
        predicate_valid_R := false.B

        out_ready_R := VecInit(Seq.fill(NumOuts)(false.B))
        out_valid_R := VecInit(Seq.fill(NumOuts)(false.B))
        state := s_idle
      }
    }
  }

}

/**
  * BasicBlockNoMaskFastNode3
  * It has only single input and it fires its output at the same cycle as it
  * receives its input.
  *
  * @param BID
  * @param NumOuts
  * @param p
  * @param name
  * @param file
  */

@deprecated("BasicBlockNoMaskFastNode3 is deprecated. It doesn't handle some corner cases. Please use BasicBlockNoMaskFastNode", "dataflow-lib 1.0")
class BasicBlockNoMaskFastNode3(BID: Int, val NumOuts: Int)
                               (implicit val p: Parameters,
                                name: sourcecode.Name, file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new BasicBlockNoMaskFastIO(NumOuts)(p))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + BID + " "

  /*===========================================*
 *            Registers                      *
 *===========================================*/

  val predicate_in_R = RegInit(ControlBundle.default)
  val predicate_valid_R = RegInit(false.B)

  val out_valid_R = Seq.fill(NumOuts)(RegInit(false.B))

  val fire_R = Seq.fill(NumOuts)(RegInit(false.B))

  val task_input = (io.predicateIn.bits.taskID | predicate_in_R.taskID)


  val predicate_input = (io.predicateIn.bits.control & io.predicateIn.valid) | (predicate_in_R.control & predicate_valid_R)

  for (i <- 0 until NumOuts) {
    when(io.Out(i).fire) {
      fire_R(i) := true.B
    }
  }

  io.predicateIn.ready := ~predicate_valid_R
  when(io.predicateIn.fire()) {
    predicate_in_R <> io.predicateIn.bits
    predicate_valid_R := true.B
  }

  val fire_mask = (fire_R zip io.Out.map(_.fire)).map { case (a, b) => a | b }

  /*============================================*
   *            ACTIONS                         *
   *============================================*/
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  //Value initilization
  io.Out.map(_.bits.control).foreach(_ := predicate_input)
  io.Out.map(_.bits.taskID).foreach(_ := task_input)
  io.Out.foreach(_.valid := false.B)


  switch(state) {
    is(s_idle) {
      // State change
      when(io.predicateIn.fire) {

        io.Out.foreach(_.valid := true.B)


        when(fire_mask.reduce(_ & _)) {
          predicate_in_R := ControlBundle.default
          predicate_valid_R := false.B
          out_valid_R.foreach(_ := false.B)
          fire_R.foreach(_ := false.B)

          state := s_idle
        }.otherwise {

          state := s_fire
        }

        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] "
            + node_name + ": Output [T] fired @ %d\n", task_input, cycleCount)
        }
      }

    }
    is(s_fire) {

      io.Out.foreach(_.valid := true.B)

      when(fire_mask.reduce(_ & _)) {
        predicate_in_R := ControlBundle.default
        predicate_valid_R := false.B
        out_valid_R.foreach(_ := false.B)
        fire_R.foreach(_ := false.B)

        state := s_idle

      }
    }
  }

}


class LoopHeadNodeIO(val NumOuts: Int, val NumPhi: Int)(implicit p: Parameters) extends CoreBundle {

  // Predicate enable
  val activate = Flipped(Decoupled(new ControlBundle))
  val loopBack = Flipped(Decoupled(new ControlBundle))

  // Output IO
  val Out = Vec(NumOuts, Decoupled(new ControlBundle))
  val MaskBB = Vec(NumPhi, Decoupled(UInt(2.W)))

  override def cloneType = new LoopHeadNodeIO(NumOuts, NumPhi).asInstanceOf[this.type]
}

@deprecated("Use LoopFastHead instead. For O1 the behaviour is not deterministic", "dandelion-1.0")
class LoopHead(val BID: Int, val NumOuts: Int, val NumPhi: Int)
              (implicit val p: Parameters,
               name: sourcecode.Name,
               file: sourcecode.File) extends Module with CoreParams with UniformPrintfs {
  // Defining IOs
  val io = IO(new LoopHeadNodeIO(NumOuts, NumPhi))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + BID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Enable Input
  val active_R = RegInit(ControlBundle.default)
  val active_valid_R = RegInit(false.B)

  val loop_back_R = RegInit(ControlBundle.default)
  val loop_back_valid_R = RegInit(false.B)

  val end_loop_R = RegInit(ControlBundle.default)
  val end_loop_valid_R = RegInit(false.B)

  // Output Handshaking
  val out_R = RegInit(ControlBundle.default)
  val out_val_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_fired_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))

  val mask_fired_R = RegInit(VecInit(Seq.fill(NumPhi)(false.B)))
  val mask_valid_R = RegInit(VecInit(Seq.fill(NumPhi)(false.B)))
  val mask_value_R = RegInit(0.U(2.W))

  val s_START :: s_FEED :: s_END :: Nil = Enum(3)
  val state = RegInit(s_START)

  io.activate.ready := ~active_valid_R
  when(io.activate.fire()) {
    active_R <> io.activate.bits
    active_valid_R := true.B
  }

  io.loopBack.ready := true.B //~loop_back_valid_R
  when(io.loopBack.fire()) {
    loop_back_R <> io.loopBack.bits
    loop_back_valid_R := true.B
  }

  for (i <- 0 until NumOuts) {
    io.Out(i).bits := out_R
  }

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    when(io.Out(i).fire()) {
      // Detecting when to reset
      out_fired_R(i) := true.B;
      // Propagating output
      out_valid_R(i) := false.B
    }
  }


  // Wire up MASK Readys and Valids
  for (i <- 0 until NumPhi) {
    io.MaskBB(i).bits := mask_value_R
    io.MaskBB(i).valid := mask_valid_R(i)
    when(io.MaskBB(i).fire()) {
      // Detecting when to reset
      mask_fired_R(i) := true.B
      // Propagating mask
      mask_valid_R(i) := false.B
    }

  }


  /*=================
   * States
   ==================*/

  switch(state) {
    is(s_START) { // First loop
      mask_value_R := 1.U
      when(active_valid_R) {
        when(active_R.control) {
          //Valid the output
          out_R := active_R
          out_valid_R := VecInit(Seq.fill(NumOuts)(true.B))
          mask_valid_R := VecInit(Seq.fill(NumPhi)(true.B))
          state := s_END
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Active fired @ %d, Mask: %d\n",
              active_R.taskID, cycleCount, 1.U)
          }
        }.otherwise {
          active_R := ControlBundle.default
          active_valid_R := false.B
          state := s_START
        }
      }
    }
    is(s_FEED) { // Wait for loop feedback signal.
      mask_value_R := 2.U
      when(loop_back_valid_R) {
        loop_back_valid_R := false.B
        when(loop_back_R.control) {
          out_R := loop_back_R
          out_valid_R := VecInit(Seq.fill(NumOuts)(true.B))
          mask_valid_R := VecInit(Seq.fill(NumPhi)(true.B))
          state := s_END
        }.otherwise {
          active_valid_R := false.B
          state := s_START
        }
      }
    }
    is(s_END) { // Wait until all outputs have fired
      when(out_fired_R.reduceLeft(_ && _) && mask_fired_R.reduceLeft(_ && _)) {
        mask_value_R := 2.U
        out_fired_R := VecInit(Seq.fill(NumOuts)(false.B))
        mask_fired_R := VecInit(Seq.fill(NumPhi)(false.B))
        state := s_FEED
      }
    }
  }


}


class LoopFastHead(val BID: Int, val NumOuts: Int, val NumPhi: Int)
                  (implicit val p: Parameters,
                   name: sourcecode.Name,
                   file: sourcecode.File) extends Module with CoreParams with UniformPrintfs {
  // Defining IOs
  val io = IO(new LoopHeadNodeIO(NumOuts, NumPhi))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + BID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Enable Input
  val active_R = RegInit(ControlBundle.default)
  val loop_back_R = RegInit(ControlBundle.default)

  val out_value = RegInit(ControlBundle.default)
  val mask_value = RegInit(0.U(2.W))

  // Output Handshaking
  val out_valid_R = Seq.fill(NumOuts)(RegInit(false.B))
  val out_fired_R = Seq.fill(NumOuts)(RegInit(false.B))

  val mask_valid_R = Seq.fill(NumPhi)(RegInit(false.B))
  val mask_fired_R = Seq.fill(NumPhi)(RegInit(false.B))

  val s_idle :: s_loop :: s_fire :: s_nofire :: Nil = Enum(4)

  val state = RegInit(s_idle)


  io.activate.ready := false.B
  io.loopBack.ready := false.B

  io.Out.foreach(_.bits := ControlBundle.default)
  io.Out.foreach(_.valid := false.B)

  io.MaskBB.foreach(_.bits := 0.U)
  io.MaskBB.foreach(_.valid := false.B)

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    when(io.Out(i).fire()) {
      // Detecting when to reset
      out_fired_R(i) := true.B
      // Propagating output
      out_valid_R(i) := false.B
    }
  }


  // Wire up MASK Readys and Valids
  for (i <- 0 until NumPhi) {
    when(io.MaskBB(i).fire()) {
      mask_fired_R(i) := true.B
      // Propagating output
      mask_valid_R(i) := false.B
    }
  }

  /*=================
   * States
   ==================*/

  switch(state) {
    is(s_idle) {

      // First loop
      // We should wait for first active signal
      io.activate.ready := true.B
      io.loopBack.ready := false.B

      when(io.activate.fire()) {

        out_value <> io.activate.bits
        mask_value := 1.U

        out_valid_R.foreach(_ := true.B)
        mask_valid_R.foreach(_ := true.B)


        //when loop is predicated
        //we need switch to loop back mode
        when(io.activate.bits.control) {
          state := s_fire
        }.otherwise {
          state := s_nofire
        }

        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Active fired @ %d, Mask: %d\n",
            active_R.taskID, cycleCount, 1.U)
        }
      }
    }

    is(s_nofire) {

      io.Out.foreach(_.bits <> out_value)
      (io.Out.map(_.valid) zip out_valid_R) foreach { case (a, b) => a := b }

      io.MaskBB.foreach(_.bits := mask_value)
      (io.MaskBB.map(_.valid) zip mask_valid_R) foreach { case (a, b) => a := b }

      val out_fire_mask = ((out_fired_R zip io.Out.map(_.fire)) map { case (a, b) => a | b }) reduce (_ & _)
      val mask_fire_mask = ((mask_fired_R zip io.MaskBB.map(_.fire)) map { case (a, b) => a | b }) reduce (_ & _)

      when(out_fire_mask & mask_fire_mask) {
        out_fired_R.foreach(_ := false.B)
        mask_fired_R.foreach(_ := false.B)
        state := s_idle
      }

    }

    is(s_fire) {
      io.Out.foreach(_.bits <> out_value)
      (io.Out.map(_.valid) zip out_valid_R) foreach { case (a, b) => a := b }

      io.MaskBB.foreach(_.bits := mask_value)
      (io.MaskBB.map(_.valid) zip mask_valid_R) foreach { case (a, b) => a := b }

      val out_fire_mask = ((out_fired_R zip io.Out.map(_.fire)) map { case (a, b) => a | b }) reduce (_ & _)
      val mask_fire_mask = ((mask_fired_R zip io.MaskBB.map(_.fire)) map { case (a, b) => a | b }) reduce (_ & _)

      when(out_fire_mask && mask_fire_mask) {
        out_fired_R.foreach(_ := false.B)
        mask_fired_R.foreach(_ := false.B)
        state := s_loop
      }
    }

    is(s_loop) {
      io.loopBack.ready := true.B

      when(io.loopBack.fire()) {

        out_value <> io.loopBack.bits
        mask_value := 2.U

        out_valid_R.foreach(_ := true.B)
        mask_valid_R.foreach(_ := true.B)

        //when loop is predicated
        //we need switch to loop back mode
        when(io.loopBack.bits.control) {
          state := s_fire
        }.otherwise {
          state := s_nofire
        }

        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] "
            + node_name + ": LoopBack fired @ %d, Mask: %d\n",
            active_R.taskID, cycleCount, 1.U)
        }
      }
    }
  }


}


/** =============================
  * Clean nodes
  * =============================
  */

class BasicBlockNoMaskFastVecIO(val NumInputs: Int, val NumOuts: Int)(implicit p: Parameters)
  extends CoreBundle()(p) {
  // Output IO
  val predicateIn = Vec(NumInputs, Flipped(Decoupled(new ControlBundle())))
  val Out = Vec(NumOuts, Decoupled(new ControlBundle))

  override def cloneType = new BasicBlockNoMaskFastVecIO(NumInputs, NumOuts).asInstanceOf[this.type]
}

/**
  * BasicBLockNoMaskFastNode details:
  * 1) Node can one one or multiple predicates as input and their type is controlBundle
  * 2) State machine inside the node waits for all the inputs to arrive and then fire.
  * 3) The ouput value is OR of all the input values
  * 4) Node can fire outputs at the same cycle if all the inputs. Since, basic block node
  * is only for circuit simplification therefore, in case that we know output is valid
  * we don't want to waste one cycle for latching purpose. Therefore, output can be zero
  * cycle.
  *
  * @param BID
  * @param NumInputs
  * @param NumOuts
  * @param p
  * @param name
  * @param file
  */

class BasicBlockNoMaskFastNode(BID: Int, val NumInputs: Int = 1, val NumOuts: Int)
                              (implicit val p: Parameters,
                               name: sourcecode.Name,
                               file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new BasicBlockNoMaskFastVecIO(NumInputs, NumOuts)(p))

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + BID + " "

  // Defining IO latches

  // Data Inputs
  val in_data_R = Seq.fill(NumInputs)(RegInit(ControlBundle.default))
  val in_data_valid_R = Seq.fill(NumInputs)(RegInit(false.B))

  val output_R = RegInit(ControlBundle.default)
  val output_valid_R = Seq.fill(NumOuts)(RegInit(false.B))
  val output_fire_R = Seq.fill(NumOuts)(RegInit(false.B))

  //Make sure whenever output is fired we latch it
  for (i <- 0 until NumInputs) {
    io.predicateIn(i).ready := ~in_data_valid_R(i)
    when(io.predicateIn(i).fire()) {
      in_data_R(i) <> io.predicateIn(i).bits
      in_data_valid_R(i) := true.B
    }
  }

  val in_task_ID = (io.predicateIn zip in_data_R) map {
    case (a, b) => a.bits.taskID | b.taskID
  } reduce (_ | _)

  //Output connections
  for (i <- 0 until NumOuts) {
    when(io.Out(i).fire()) {
      output_fire_R(i) := true.B
      output_valid_R(i) := false.B
    }
  }

  //Connecting output signals
  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> output_R
    io.Out(i).valid <> output_valid_R(i)
  }

  val select_valid = (in_data_valid_R zip io.predicateIn.map(_.fire)) map {
    case (a, b) => a | b
  } reduce (_ & _)


  val out_fire_mask = (output_fire_R zip io.Out.map(_.fire)) map { case (a, b) => a | b }


  //Masking output value
  val output_value = (io.predicateIn.map(_.bits.control) zip in_data_R.map(_.control)) map {
    case (a, b) => a | b
  } reduce (_ | _)

  val predicate_val = in_data_R.map(_.control).reduce(_ | _)

  output_R := ControlBundle.default(predicate_val, in_task_ID)

  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)


  switch(state) {
    is(s_idle) {
      when(in_data_valid_R.reduce(_ & _)) {
        output_valid_R.foreach(_ := true.B)
        state := s_fire

        when(predicate_val) {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [BB]   "
              + node_name + ": Output [T] fired @ %d\n", output_R.taskID, cycleCount)
          }
        }.otherwise {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [BB]   "
              + node_name + ": Output [F] fired @ %d\n", output_R.taskID, cycleCount)
          }

        }

      }
    }
    is(s_fire) {
      //Restart the states
      when(out_fire_mask.reduce(_ & _)) {

        in_data_R foreach (_ := ControlBundle.default)
        in_data_valid_R foreach (_ := false.B)

        output_fire_R foreach (_ := false.B)

        state := s_idle
      }
    }
  }


}




