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


/**
  * @note
  * For Conditional Branch output is always equal to two!
  * Since your branch output wire to two different basic block only
  */

class CBranchNodeIO(NumOuts: Int = 2)
                   (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new ControlBundle) {

  // RightIO: Right input data for computation
  val CmpIO = Flipped(Decoupled(new DataBundle))

  override def cloneType = new CBranchNodeIO(NumOuts).asInstanceOf[this.type]
}

class CBranchNode(ID: Int)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingCtrlNPS(2, ID)(p) {
  override lazy val io = IO(new CBranchNodeIO())
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // OP Inputs
  val cmp_R = RegInit(DataBundle.default)
  val cmp_valid_R = RegInit(false.B)


  // Output wire
  //  val data_out_w = WireInit(VecInit(Seq.fill(2)(false.B)))
  val data_out_R = RegInit(VecInit(Seq.fill(2)(false.B)))

  //  val s_IDLE :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val start = cmp_valid_R & IsEnableValid()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  // Predicate register
  //val pred_R = RegInit(init = false.B)


  io.CmpIO.ready := ~cmp_valid_R
  when(io.CmpIO.fire()) {
    cmp_R := io.CmpIO.bits
    cmp_valid_R := true.B
  }

  // Wire up Outputs
  io.Out(0).bits.control := data_out_R(0)
  io.Out(0).bits.taskID := enable_R.taskID
  io.Out(1).bits.control := data_out_R(1)
  io.Out(1).bits.taskID := enable_R.taskID

  /*============================================*
   *            STATE MACHINE                   *
   *============================================*/

  /**
    * Combination of bits and valid signal from CmpIn whill result the output value:
    * valid == 0  ->  output = 0
    * valid == 1  ->  cmp = true  then 1
    * valid == 1  ->  cmp = false then 2
    */

  switch(state) {
    is(s_IDLE) {
      when(IsEnableValid() && cmp_valid_R) {
        state := s_COMPUTE
        ValidOut()
        when(IsEnable()) {
          data_out_R(0) := cmp_R.data.asUInt.orR
          data_out_R(1) := ~cmp_R.data.asUInt.orR
        }.otherwise {
          data_out_R := VecInit(Seq.fill(2)(false.B))
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Restarting
        //cmp_R := DataBundle.default
        cmp_valid_R := false.B

        // Reset output
        data_out_R := VecInit(Seq.fill(2)(false.B))
        //Reset state
        state := s_IDLE

        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
            node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_out_R.asUInt())
        }
      }
    }
  }

}

class CBranchFastIO()(implicit p: Parameters) extends CoreBundle {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))
  // Comparator input
  val CmpIO = Flipped(Decoupled(new DataBundle))
  // Output IO
  val Out = Vec(2, Decoupled(new ControlBundle))
}

@deprecated("CBranchFastNode is deprecated. It doesn't handle all the corner cases. Please use CBranchFastNodeVariable", "dataflow-lib 1.0")
class CBranchFastNode(ID: Int)
                     (implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  // Printf debugging
  override val printfSigil = "Node (UBR) ID: " + ID + " "
  val io = IO(new CBranchFastIO()(p))
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  io.Out(0).bits.control := io.enable.bits.control && io.CmpIO.bits.data(0)
  io.Out(1).bits.control := io.enable.bits.control && !io.CmpIO.bits.data(0)
  io.Out(0).bits.taskID := io.enable.bits.taskID
  io.Out(1).bits.taskID := io.enable.bits.taskID

  when(io.Out(0).ready && io.Out(1).ready && io.CmpIO.valid && io.enable.valid) {
    io.Out(0).valid := true.B
    io.Out(1).valid := true.B
    io.CmpIO.ready := true.B
    io.enable.ready := true.B
  }.otherwise {
    io.Out(0).valid := false.B
    io.Out(1).valid := false.B
    io.CmpIO.ready := false.B
    io.enable.ready := false.B
  }


}


/**
  * This class is the fast version of CBranch which the IO supports
  * a vector of output for each side True/False
  *
  * @param NumPredOps Number of parents
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  */

@deprecated("Use CBranchFastNodeVariable2 instead. The behaviour is not deterministic","dandelion-1.0")
class CBranchFastNodeVariable(val NumTrue: Int = 1, val NumFalse: Int = 1, val ID: Int)
                             (implicit val p: Parameters,
                              name: sourcecode.Name,
                              file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new Bundle {
    //Control signal
    val enable = Flipped(Decoupled(new ControlBundle))

    //Comparision result
    val CmpIO = Flipped(Decoupled(new DataBundle))

    //Output
    val TrueOutput = Vec(NumTrue, Decoupled(new ControlBundle))
    val FalseOutput = Vec(NumFalse, Decoupled(new ControlBundle))
  })

  // Printf debugging
  override val printfSigil = "Node (CBR) ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  //Latching input comparision result
  val cmp_R = RegInit(false.B)
  val cmp_valid = RegInit(false.B)

  //Latching control signal
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  val output_true_R = RegInit(ControlBundle.default)
  val output_true_valid_R = Seq.fill(NumTrue)(RegInit(false.B))
  val fire_true_R = Seq.fill(NumTrue)(RegInit(false.B))

  val output_false_R = RegInit(ControlBundle.default)
  val output_false_valid_R = Seq.fill(NumFalse)(RegInit(false.B))
  val fire_false_R = Seq.fill(NumFalse)(RegInit(false.B))

  val predicate = (io.enable.bits.control & io.enable.valid) | (enable_R.control & enable_valid_R)
  val predicate_cmp = (io.CmpIO.bits.data.orR() & io.CmpIO.valid) | (cmp_R & cmp_valid)
  val task_id = (io.enable.bits.taskID & io.enable.valid) | (enable_R.taskID & enable_valid_R)


  // Latching CMP input
  io.CmpIO.ready := ~cmp_valid
  when(io.CmpIO.fire && io.CmpIO.bits.predicate) {
    cmp_R := io.CmpIO.bits.data.orR()
    cmp_valid := true.B
  }

  // Latching enable signal
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  // Output for true and false sides
  val true_output = predicate & predicate_cmp
  val false_output = predicate & (~predicate_cmp).toBool

  // Defalut values for Trueoutput
  //
  //
  output_true_R.control := true_output
  output_true_R.taskID := task_id

  for (i <- 0 until NumTrue) {
    io.TrueOutput(i).bits <> ControlBundle.default(true_output, task_id)
    io.TrueOutput(i).valid <> output_true_valid_R(i)
  }

  for (i <- 0 until NumTrue) {
    when(io.TrueOutput(i).fire) {
      fire_true_R(i) := true.B
    }
  }


  // Defalut values for False output
  //
  //
  output_false_R.control := false_output
  output_false_R.taskID := task_id

  for (i <- 0 until NumFalse) {
    io.FalseOutput(i).bits <> ControlBundle.default(false_output, task_id)
    io.FalseOutput(i).valid <> output_false_valid_R(i)
  }

  for (i <- 0 until NumFalse) {
    when(io.FalseOutput(i).fire) {
      fire_false_R(i) := true.B
    }
  }

  val fire_true_mask = (fire_true_R zip io.TrueOutput.map(_.fire)).map { case (a, b) => a | b }
  val fire_false_mask = (fire_false_R zip io.FalseOutput.map(_.fire)).map { case (a, b) => a | b }


  //Output register
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state) {
    is(s_idle) {

      io.TrueOutput.foreach(_.valid := false.B)
      io.FalseOutput.foreach(_.valid := false.B)

      when((enable_valid_R || io.enable.fire)
        && (cmp_valid || io.CmpIO.fire)) {

        io.TrueOutput.foreach(_.valid := true.B)
        io.FalseOutput.foreach(_.valid := true.B)

        //Check if we can restart the state at the same cycle
        when(io.TrueOutput.map(_.fire).reduce(_ & _)
          && io.FalseOutput.map(_.fire).reduce(_ & _)) {
          //Latching input comparision result
          cmp_R := false.B
          cmp_valid := false.B

          //Latching control signal
          enable_R := ControlBundle.default
          enable_valid_R := false.B

          output_true_R := ControlBundle.default
          output_true_valid_R.foreach(_ := false.B)
          fire_true_R.foreach(_ := false.B)

          output_false_R := ControlBundle.default
          output_false_valid_R.foreach(_ := false.B)
          fire_false_R.foreach(_ := false.B)

          state := s_idle
        }.otherwise {
          state := s_fire
        }


        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
            node_name + ": Output fired(%d) @ %d\n", enable_R.taskID, Cat(true_output, false_output).asUInt(), cycleCount)
        }
      }
    }
    is(s_fire) {
      io.TrueOutput.foreach(_.bits := output_true_R)
      (io.TrueOutput.map(_.valid) zip output_true_valid_R).map { case (a, b) => a := b }

      io.FalseOutput.foreach(_.bits := output_false_R)
      (io.FalseOutput.map(_.valid) zip output_false_valid_R).map { case (a, b) => a := b }

      //Now we can restart the states
      when(fire_true_mask.reduce(_ & _) && fire_false_mask.reduce(_ & _)) {
        //Latching input comparision result
        cmp_R := false.B
        cmp_valid := false.B

        //Latching control signal
        enable_R := ControlBundle.default
        enable_valid_R := false.B

        output_true_R := ControlBundle.default
        output_true_valid_R.foreach(_ := false.B)
        fire_true_R.foreach(_ := false.B)

        output_false_R := ControlBundle.default
        output_false_valid_R.foreach(_ := false.B)
        fire_false_R.foreach(_ := false.B)

        state := s_idle

      }

    }
  }
}


/**
  * This class is the fast version of CBranch which the IO supports
  * a vector of output for each side True/False
  *
  * @param NumPredOps Number of parents
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  */


class CBranchNodeVariableLoop(val NumTrue: Int = 1, val NumFalse: Int = 1, val NumPredecessor: Int = 0, val ID: Int)
                             (implicit val p: Parameters,
                              name: sourcecode.Name,
                              file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  override lazy val io = IO(new CBranchIO(NumTrue = NumTrue, NumFalse = NumFalse, NumPredecessor = NumPredecessor))

  // Printf debugging
  override val printfSigil = "Node (CBR) ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  //Latching input comparision result
  val cmp_R = RegInit(false.B)
  val cmp_valid = RegInit(false.B)

  //Latching control signal
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  val output_true_R = RegInit(ControlBundle.default)
  val output_true_valid_R = Seq.fill(NumTrue)(RegInit(false.B))
  val fire_true_R = Seq.fill(NumTrue)(RegInit(false.B))

  val output_false_R = RegInit(ControlBundle.default)
  val output_false_valid_R = Seq.fill(NumFalse)(RegInit(false.B))
  val fire_false_R = Seq.fill(NumFalse)(RegInit(false.B))

  val predecessor_R = Seq.fill(NumPredecessor)(RegInit(ControlBundle.default))
  val predecessor_valid_R = Seq.fill(NumPredecessor)(RegInit(false.B))

  val task_id = enable_R.taskID & enable_valid_R


  // Latching CMP input
  io.CmpIO.ready := ~cmp_valid
  when(io.CmpIO.fire) {
    cmp_R := io.CmpIO.bits.data.orR()
    cmp_valid := true.B
  }

  // Latching enable signal
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  for (i <- 0 until NumPredecessor) {
    io.PredOp(i).ready := ~predecessor_valid_R(i)
    when(io.PredOp(i).fire) {
      predecessor_R(i) <> io.PredOp(i).bits
      predecessor_valid_R(i) := true.B
    }
  }

  def IsPredecessorValid(): Bool = {
    if (NumPredecessor == 0) {
      true.B
    }
    else {
      predecessor_valid_R.reduce(_ & _)
    }
  }


  // Output for true and false sides
  val predicate = enable_R.control & enable_valid_R
  val true_output = predicate & cmp_R
  val false_output = predicate & (~cmp_R).toBool

  // Defalut values for Trueoutput
  //
  output_true_R.control := true_output
  output_true_R.taskID := task_id

  for (i <- 0 until NumTrue) {
    io.TrueOutput(i).bits <> output_true_R
    io.TrueOutput(i).valid <> output_true_valid_R(i)
  }

  for (i <- 0 until NumTrue) {
    when(io.TrueOutput(i).fire) {
      fire_true_R(i) := true.B
      output_true_valid_R(i) := false.B
    }
  }


  // Defalut values for False output
  //
  output_false_R.control := false_output
  output_false_R.taskID := task_id

  for (i <- 0 until NumFalse) {
    io.FalseOutput(i).bits <> output_false_R
    io.FalseOutput(i).valid <> output_false_valid_R(i)
  }

  for (i <- 0 until NumFalse) {
    when(io.FalseOutput(i).fire) {
      fire_false_R(i) := true.B
      output_false_valid_R(i) := false.B
    }
  }

  val fire_true_mask = fire_true_R.reduce(_ & _)
  val fire_false_mask = fire_false_R.reduce(_ & _)


  //Output register
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)


  switch(state) {
    is(s_idle) {

      when(enable_valid_R && cmp_valid && IsPredecessorValid()) {
        when(enable_R.control) {
          output_true_valid_R.foreach(_ := true.B)
          output_false_valid_R.foreach(_ := true.B)

          state := s_fire


          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
              node_name + ": Output fired @ %d\n", enable_R.taskID, cycleCount)
          }
        }.otherwise {
          //Latching input comparision result
          cmp_R := false.B
          cmp_valid := false.B

          predecessor_R foreach (_ := ControlBundle.default)
          predecessor_valid_R foreach (_ := false.B)

          //Latching control signal
          enable_R := ControlBundle.default
          enable_valid_R := false.B

          output_true_R := ControlBundle.default
          output_true_valid_R.foreach(_ := false.B)
          fire_true_R.foreach(_ := false.B)

          output_false_R := ControlBundle.default
          output_false_valid_R.foreach(_ := false.B)
          fire_false_R.foreach(_ := false.B)

          state := s_idle

        }
      }
    }
    is(s_fire) {

      //Now we can restart the states
      when(fire_true_mask && fire_false_mask) {
        //Latching input comparision result
        cmp_R := false.B
        cmp_valid := false.B

        //Latching control signal
        enable_R := ControlBundle.default
        enable_valid_R := false.B

        predecessor_R foreach (_ := ControlBundle.default)
        predecessor_valid_R foreach (_ := false.B)

        output_true_R := ControlBundle.default
        output_true_valid_R.foreach(_ := false.B)
        fire_true_R.foreach(_ := false.B)

        output_false_R := ControlBundle.default
        output_false_valid_R.foreach(_ := false.B)
        fire_false_R.foreach(_ := false.B)

        state := s_idle

      }

    }
  }
}

class UBranchNode(NumPredOps: Int = 0,
                  NumOuts: Int = 1,
                  ID: Int)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShaking(NumPredOps, 0, NumOuts, ID)(new ControlBundle)(p) {
  override lazy val io = IO(new HandShakingIOPS(NumPredOps, 0, NumOuts)(new ControlBundle)(p))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/

  val s_idle :: s_OUTPUT :: Nil = Enum(2)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  /**
    * Combination of bits and valid signal from CmpIn whill result the output value:
    * valid == 0  ->  output = 0
    * valid == 1  ->  cmp = true  then 1
    * valid == 1  ->  cmp = false then 2
    *
    * @note data_R value is equale to predicate bit
    */
  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> enable_R
  }

  switch(state) {
    is(s_idle) {
      when(IsEnableValid() && IsPredValid()) {
        state := s_OUTPUT
        ValidOut()
        when(enable_R.control) {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [UBR] "
              + node_name + ": Output fired [T] @ %d,\n",
              enable_R.taskID, cycleCount)
          }
        }.otherwise {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [UBR] "
              + node_name + ": Output fired [F] @ %d,\n",
              enable_R.taskID, cycleCount)
          }
        }

      }
    }
    is(s_OUTPUT) {
      when(IsOutReady()) {
        state := s_idle
        Reset()
        enable_R := ControlBundle.default

      }
    }
  }

}

@deprecated("Use UBranchFastNode instead. It wastes one extra cycle","dandelion-1.0")
class UBranchEndNode(NumPredOps: Int = 0,
                     NumOuts: Int = 1,
                     ID: Int)
                    (implicit p: Parameters,
                     name: sourcecode.Name,
                     file: sourcecode.File)
  extends HandShaking(NumPredOps, 0, NumOuts, ID)(new ControlBundle)(p) {
  override lazy val io = IO(new HandShakingIOPS(NumPredOps, 0, NumOuts)(new ControlBundle)(p))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/

  val s_idle :: s_OUTPUT :: Nil = Enum(2)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  /**
    * Combination of bits and valid signal from CmpIn whill result the output value:
    * valid == 0  ->  output = 0
    * valid == 1  ->  cmp = true  then 1
    * valid == 1  ->  cmp = false then 2
    *
    * @note data_R value is equale to predicate bit
    */
  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := enable_R
  }

  switch(state) {
    is(s_idle) {
      when(IsEnableValid() && IsPredValid()) {
        when(enable_R.control) {
          state := s_OUTPUT
          ValidOut()
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] "
              + node_name + ": Output fired [T] @ %d,\n",
              enable_R.taskID, cycleCount)
          }
        }.otherwise {
          state := s_idle
          Reset()
          enable_R := ControlBundle.default
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] "
              + node_name + ": Output fired [F] @ %d,\n",
              enable_R.taskID, cycleCount)
          }
        }

      }
    }
    is(s_OUTPUT) {
      when(IsOutReady()) {
        state := s_idle
        Reset()
        enable_R := ControlBundle.default

      }
    }
  }

}


class UBranchFastIO()(implicit p: Parameters) extends CoreBundle {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))
  // Output IO
  val Out = Vec(1, Decoupled(new ControlBundle))
}

class UBranchFastNode(ID: Int)
                     (implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  // Printf debugging
  override val printfSigil = "Node (UBR) ID: " + ID + " "
  val io = IO(new UBranchFastIO()(p))
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  io.Out(0) <> io.enable


}


/**
  * @note
  * For Conditional Branch output is always equal to two!
  * Since your branch output wire to two different basic block only
  */

class CompareBranchIO()(implicit p: Parameters) extends CoreBundle {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))

  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle))
  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle))

  // Output IO
  val Out = Vec(2, Decoupled(new ControlBundle))
}

class CompareBranchNode(ID: Int, opCode: String)
                       (implicit val p: Parameters,
                        name: sourcecode.Name,
                        file: sourcecode.File) extends Module with CoreParams with UniformPrintfs {
  // Defining IOs
  val io = IO(new CompareBranchIO())
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/

  // Enable Input
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  // Left Input
  val left_R = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  // Output Handshaking
  val out_ready_R = RegInit(VecInit(Seq.fill(2)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(2)(false.B)))


  val FU = Module(new UCMP(xlen, opCode))
  FU.io.in1 := left_R.data
  FU.io.in2 := right_R.data

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.enable.ready := ~enable_valid_R
  when(io.enable.fire()) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

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
  when(enable_valid_R) {
    io.Out(0).bits.control := FU.io.out
    io.Out(1).bits.control := ~FU.io.out
  }.otherwise {
    io.Out(0).bits.control := false.B
    io.Out(1).bits.control := false.B
  }

  io.Out(0).bits.taskID := enable_R.taskID
  out_ready_R(0) := io.Out(0).ready
  io.Out(0).valid := out_valid_R(0)
  io.Out(1).bits.taskID := enable_R.taskID
  out_ready_R(1) := io.Out(1).ready
  io.Out(1).valid := out_valid_R(1)

  /*============================================*
   *            STATE MACHINE                   *
   *============================================*/

  /**
    * Combination of bits and valid signal from CmpIn whill result the output value:
    * valid == 0  ->  output = 0
    * valid == 1  ->  cmp = true  then 1
    * valid == 1  ->  cmp = false then 2
    */

  when(state === s_COMPUTE) {
    assert((left_R.taskID === enable_R.taskID) && (right_R.taskID === enable_R.taskID), "Control channel should be in sync with data channel!")
  }

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when((~enable_R.control).toBool) {

          enable_R := ControlBundle.default
          enable_valid_R := false.B

          left_R := DataBundle.default
          left_valid_R := false.B

          right_R := DataBundle.default
          right_valid_R := false.B

          out_ready_R := VecInit(Seq.fill(2)(false.B))
          out_valid_R := VecInit(Seq.fill(2)(false.B))

          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID-> %d] "
              + node_name + ": Not predicated value -> reset\n", enable_R.taskID)
          }

        }.elsewhen((io.LeftIO.fire() || left_valid_R) && (io.RightIO.fire() || right_valid_R)) {
          out_valid_R := VecInit(Seq.fill(2)(true.B))
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(out_ready_R.asUInt.andR) {
        enable_R := ControlBundle.default
        enable_valid_R := false.B

        left_R := DataBundle.default
        left_valid_R := false.B

        right_R := DataBundle.default
        right_valid_R := false.B

        out_ready_R := VecInit(Seq.fill(2)(false.B))
        out_valid_R := VecInit(Seq.fill(2)(false.B))

        state := s_IDLE

        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name +
            ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, FU.io.out.asUInt())
        }
      }
    }
  }

}


/**
  * This class is the fast version of CBranch which the IO supports
  * a vector of output for each side True/False
  *
  * @param NumPredOps Number of parents
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  */

class UBranchFastNodeVariable(val NumOutputs: Int = 1, val ID: Int)
                             (implicit val p: Parameters,
                              name: sourcecode.Name,
                              file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new Bundle {
    //Control signal
    val enable = Flipped(Decoupled(new ControlBundle))

    //Output
    val Out = Vec(NumOutputs, Decoupled(new ControlBundle))
  })

  // Printf debugging
  override val printfSigil = "Node (CBR) ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  //Latching control signal
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  val output_R = RegInit(ControlBundle.default)
  val output_valid_R = Seq.fill(NumOutputs)(RegInit(false.B))
  val fire_R = Seq.fill(NumOutputs)(RegInit(false.B))

  val predicate = (io.enable.bits.control & io.enable.valid) | (enable_R.control & enable_valid_R)
  val task_id = (io.enable.bits.taskID & io.enable.valid) | (enable_R.taskID & enable_valid_R)


  // Latching enable signal
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  // Defalut values for Trueoutput
  //
  //
  output_R.control := predicate
  output_R.taskID := task_id

  for (i <- 0 until NumOutputs) {
    io.Out(i).bits <> ControlBundle.default(predicate, task_id)
    io.Out(i).valid <> output_valid_R(i)
  }

  for (i <- 0 until NumOutputs) {
    when(io.Out(i).fire) {
      fire_R(i) := true.B
    }
  }


  val fire_mask = (fire_R zip io.Out.map(_.fire)).map { case (a, b) => a | b }


  //Output register
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state) {
    is(s_idle) {

      io.Out.foreach(_.valid := false.B)

      when((enable_valid_R || io.enable.fire)) {

        io.Out.foreach(_.valid := true.B)

        //Check if we can restart the state at the same cycle
        when(io.Out.map(_.fire).reduce(_ & _)) {
          //Latching control signal
          enable_R := ControlBundle.default
          enable_valid_R := false.B

          output_R := ControlBundle.default
          output_valid_R.foreach(_ := false.B)
          fire_R.foreach(_ := false.B)

          state := s_idle
        }.otherwise {
          state := s_fire
        }


        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
            node_name + ": Output fired @ %d\n", enable_R.taskID, cycleCount)
        }
      }
    }
    is(s_fire) {
      io.Out.foreach(_.bits := output_R)
      (io.Out.map(_.valid) zip output_valid_R).map { case (a, b) => a := b }

      //Now we can restart the states
      when(fire_mask.reduce(_ & _)) {

        //Latching control signal
        enable_R := ControlBundle.default
        enable_valid_R := false.B

        output_R := ControlBundle.default
        output_valid_R.foreach(_ := false.B)
        fire_R.foreach(_ := false.B)

      }

    }
  }
}

/**
  * This class is the fast version of CBranch which the IO supports
  * a vector of output for each side True/False
  *
  * @param NumPredOps Number of parents
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  */

class CBranchIO(val NumTrue: Int, val NumFalse: Int, val NumPredecessor: Int = 0)(implicit p: Parameters)
  extends CoreBundle()(p) {
  //Control signal
  val enable = Flipped(Decoupled(new ControlBundle))

  //Comparision result
  val CmpIO = Flipped(Decoupled(new DataBundle))

  // Control dependencies
  val PredOp = Vec(NumPredecessor, Flipped(Decoupled(new ControlBundle)))

  //Output
  val TrueOutput = Vec(NumTrue, Decoupled(new ControlBundle))
  val FalseOutput = Vec(NumFalse, Decoupled(new ControlBundle))

  override def cloneType = new CBranchIO(NumTrue, NumFalse, NumPredecessor).asInstanceOf[this.type]
}


class CBranchFastNodeVariable2(val NumTrue: Int = 1, val NumFalse: Int = 1, val NumSuccessor: Int = 0, val ID: Int)
                              (implicit val p: Parameters,
                               name: sourcecode.Name,
                               file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  val io = IO(new CBranchIO(NumTrue, NumFalse)(p))

  // Printf debugging
  override val printfSigil = "Node (CBR) ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  //Latching input comparision result
  val cmp_R = RegInit(false.B)
  val cmp_valid = RegInit(false.B)

  val success_R = Seq.fill(NumSuccessor)(RegInit(ControlBundle.default))
  val success_valid_R = Seq.fill(NumSuccessor)(RegInit(false.B))

  //Latching control signal
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  val output_true_R = RegInit(ControlBundle.default)
  val output_true_valid_R = Seq.fill(NumTrue)(RegInit(false.B))
  val fire_true_R = Seq.fill(NumTrue)(RegInit(false.B))

  val output_false_R = RegInit(ControlBundle.default)
  val output_false_valid_R = Seq.fill(NumFalse)(RegInit(false.B))
  val fire_false_R = Seq.fill(NumFalse)(RegInit(false.B))

  val predicate = enable_R.control & enable_valid_R
  val task_id = enable_R.taskID & enable_valid_R


  // Latching CMP input
  io.CmpIO.ready := ~cmp_valid
  when(io.CmpIO.fire) {
    cmp_R := io.CmpIO.bits.data.orR()
    cmp_valid := true.B
  }

  for (i <- 0 until NumSuccessor) {
    io.PredOp(i).ready := ~success_valid_R(i)
    when(io.PredOp(i).fire) {
      success_R(i) <> io.PredOp(i).bits
      success_valid_R(i) := true.B
    }
  }

  def isSuccessorValid(): Bool = {
    if (NumSuccessor == 0) {
      true.B
    }
    else {
      success_valid_R.reduce(_ & _)
    }
  }

  // Latching enable signal
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  // Output for true and false sides

  val true_output = predicate & cmp_R
  val false_output = predicate & (~cmp_R).toBool
  //  val true_output = cmp_R
  //  val false_output = ~cmp_R

  // Defalut values for Trueoutput
  output_true_R.control := true_output
  output_true_R.taskID := task_id

  for (i <- 0 until NumTrue) {
    io.TrueOutput(i).bits <> ControlBundle.default
    io.TrueOutput(i).valid := false.B
  }

  for (i <- 0 until NumTrue) {
    when(io.TrueOutput(i).fire) {
      fire_true_R(i) := true.B
      output_true_valid_R(i) := false.B
    }
  }


  // Defalut values for False output
  output_false_R.control := false_output
  output_false_R.taskID := task_id

  for (i <- 0 until NumFalse) {
    io.FalseOutput(i).bits <> ControlBundle.default
    io.FalseOutput(i).valid := false.B
  }

  for (i <- 0 until NumFalse) {
    when(io.FalseOutput(i).fire) {
      fire_false_R(i) := true.B
      output_false_valid_R(i) := false.B
    }
  }

  val fire_true_mask = (fire_true_R zip io.TrueOutput.map(_.fire)).map { case (a, b) => a | b }
  val fire_false_mask = (fire_false_R zip io.FalseOutput.map(_.fire)).map { case (a, b) => a | b }

  val enable_value = (enable_R.control & enable_valid_R) | (io.enable.bits.control & io.enable.fire)
  val input_available = (enable_valid_R | io.enable.fire) & (cmp_valid | io.CmpIO.fire)


  //Output register
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)

  switch(state) {
    is(s_idle) {
      when(enable_valid_R && cmp_valid && isSuccessorValid()) {
        when(enable_value) {
          output_true_valid_R foreach {
            _ := true.B
          }
          output_false_valid_R foreach {
            _ := true.B
          }
          state := s_fire

          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
              node_name + ": Output fired(%d) @ %d\n", enable_R.taskID, Cat(true_output, false_output).asUInt(), cycleCount)
          }
        }.otherwise {
          cmp_R := false.B
          cmp_valid := false.B

          enable_R := ControlBundle.default
          enable_valid_R := false.B

          success_R foreach (_ := ControlBundle.default)
          success_valid_R foreach (_ := false.B)

          output_true_R := ControlBundle.default
          output_true_valid_R.foreach(_ := false.B)
          fire_true_R.foreach(_ := false.B)

          output_false_R := ControlBundle.default
          output_false_valid_R.foreach(_ := false.B)
          fire_false_R.foreach(_ := false.B)

          state := s_idle


          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
              node_name + ": Output flushed(%d) @ %d\n", enable_R.taskID, Cat(true_output, false_output).asUInt(), cycleCount)
          }
        }


      }
    }
    is(s_fire) {
      io.TrueOutput.foreach(_.bits := output_true_R)
      (io.TrueOutput.map(_.valid) zip output_true_valid_R).map { case (a, b) => a := b }

      io.FalseOutput.foreach(_.bits := output_false_R)
      (io.FalseOutput.map(_.valid) zip output_false_valid_R).map { case (a, b) => a := b }

      //Now we can restart the states
      when(fire_true_mask.reduce(_ & _) && fire_false_mask.reduce(_ & _)) {
        //Latching input comparision result
        cmp_R := false.B
        cmp_valid := false.B

        //Latching control signal
        enable_R := ControlBundle.default
        enable_valid_R := false.B

        success_R foreach (_ := ControlBundle.default)
        success_valid_R foreach (_ := false.B)

        output_true_R := ControlBundle.default
        output_true_valid_R.foreach(_ := false.B)
        fire_true_R.foreach(_ := false.B)

        output_false_R := ControlBundle.default
        output_false_valid_R.foreach(_ := false.B)
        fire_false_R.foreach(_ := false.B)

        state := s_idle
      }
    }
  }

}

/**
  * This class is the fast version of CBranch which the IO supports
  * a vector of output for each side True/False
  *
  * @param NumPredOps Number of parents
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  */

class CBranchNodeVariable(val NumTrue: Int = 1, val NumFalse: Int = 1, val NumPredecessor: Int = 0, val ID: Int)
                         (implicit val p: Parameters,
                          name: sourcecode.Name,
                          file: sourcecode.File)
  extends Module with CoreParams with UniformPrintfs {

  override lazy val io = IO(new CBranchIO(NumTrue = NumTrue, NumFalse = NumFalse, NumPredecessor = NumPredecessor))

  // Printf debugging
  override val printfSigil = "Node (CBR) ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  //Latching input comparision result
  val cmp_R = RegInit(ControlBundle.default)
  val cmp_valid = RegInit(false.B)

  //Latching control signal
  val enable_R = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)


  val predecessor_R = Seq.fill(NumPredecessor)(RegInit(ControlBundle.default))
  val predecessor_valid_R = Seq.fill(NumPredecessor)(RegInit(false.B))

  val output_true_R = RegInit(ControlBundle.default)
  val output_true_valid_R = Seq.fill(NumTrue)(RegInit(false.B))
  val fire_true_R = Seq.fill(NumTrue)(RegInit(false.B))

  val output_false_R = RegInit(ControlBundle.default)
  val output_false_valid_R = Seq.fill(NumFalse)(RegInit(false.B))
  val fire_false_R = Seq.fill(NumFalse)(RegInit(false.B))

  val task_id = enable_R.taskID | cmp_R.taskID


  // Latching CMP input
  io.CmpIO.ready := ~cmp_valid
  when(io.CmpIO.fire) {
    cmp_R.control := io.CmpIO.bits.data.orR()
    cmp_R.taskID := io.CmpIO.bits.taskID
    cmp_valid := true.B
  }

  for (i <- 0 until NumPredecessor) {
    io.PredOp(i).ready := ~predecessor_valid_R(i)
    when(io.PredOp(i).fire) {
      predecessor_R(i) <> io.PredOp(i).bits
      predecessor_valid_R(i) := true.B
    }
  }

  def IsPredecessorValid(): Bool = {
    if (NumPredecessor == 0) {
      true.B
    }
    else {
      predecessor_valid_R.reduce(_ & _)
    }
  }


  // Latching enable signal
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire) {
    enable_R <> io.enable.bits
    enable_valid_R := true.B
  }

  // Output for true and false sides
  val predicate = enable_R.control & enable_valid_R
  val true_output = predicate & cmp_R.control
  val false_output = predicate & (~cmp_R.control).toBool

  // Defalut values for Trueoutput
  //
  output_true_R.control := true_output
  output_true_R.taskID := task_id

  for (i <- 0 until NumTrue) {
    io.TrueOutput(i).bits <> output_true_R
    io.TrueOutput(i).valid <> output_true_valid_R(i)
  }

  for (i <- 0 until NumTrue) {
    when(io.TrueOutput(i).fire) {
      fire_true_R(i) := true.B
      output_true_valid_R(i) := false.B
    }
  }


  // Defalut values for False output
  //
  output_false_R.control := false_output
  output_false_R.taskID := task_id

  for (i <- 0 until NumFalse) {
    io.FalseOutput(i).bits <> output_false_R
    io.FalseOutput(i).valid <> output_false_valid_R(i)
  }

  for (i <- 0 until NumFalse) {
    when(io.FalseOutput(i).fire) {
      fire_false_R(i) := true.B
      output_false_valid_R(i) := false.B
    }
  }

  val fire_true_mask = fire_true_R.reduce(_ & _)
  val fire_false_mask = fire_false_R.reduce(_ & _)


  //Output register
  val s_idle :: s_fire :: Nil = Enum(2)
  val state = RegInit(s_idle)


  switch(state) {
    is(s_idle) {

      when(enable_valid_R && cmp_valid && IsPredecessorValid()) {

        output_true_valid_R.foreach(_ := true.B)
        output_false_valid_R.foreach(_ := true.B)

        state := s_fire

        when(enable_R.control) {
          when(IsPredecessorValid()) {
            if (log) {
              printf("[LOG] " + "[" + module_name + "] [TID->%d] [CBR] "
                + node_name + ": Output fired [T F] @ %d,\n",
                enable_R.taskID, cycleCount)
            }
          }.otherwise {
            if (log) {
              printf("[LOG] " + "[" + module_name + "] [TID->%d] [CBR] "
                + node_name + ": Output fired [F T] @ %d,\n",
                enable_R.taskID, cycleCount)
            }
          }

        }.otherwise {
          if (log) {
            printf("[LOG] " + "[" + module_name + "] [TID->%d] [CBR] "
              + node_name + ": Output fired [F F] @ %d,\n",
              enable_R.taskID, cycleCount)
          }
        }
      }
    }
    is(s_fire) {

      //Now we can restart the states
      when(fire_true_mask && fire_false_mask) {
        //Latching input comparision result
        cmp_R <> ControlBundle.default
        cmp_valid := false.B

        //Latching control signal
        enable_R := ControlBundle.default
        enable_valid_R := false.B
        predecessor_valid_R foreach (_ := false.B)

        output_true_R := ControlBundle.default
        output_true_valid_R.foreach(_ := false.B)
        fire_true_R.foreach(_ := false.B)

        output_false_R := ControlBundle.default
        output_false_valid_R.foreach(_ := false.B)
        fire_false_R.foreach(_ := false.B)

        state := s_idle

      }

    }
  }
}

