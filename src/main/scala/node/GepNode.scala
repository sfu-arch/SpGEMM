package node

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{Matchers, FlatSpec}

import config._
import interfaces._
import muxes._
import utility._

class GepNodeOneIO(NumOuts: Int)
                  (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  // Inputs should be fed only when Ready is HIGH
  // Inputs are always latched.
  // If Ready is LOW; Do not change the inputs as this will cause a bug
  val baseAddress = Flipped(Decoupled(new DataBundle()))
  val idx1 = Flipped(Decoupled(new DataBundle()))

  //  3.1
  override def cloneType = new GepNodeOneIO(NumOuts).asInstanceOf[this.type]

}

class GepNodeTwoIO(NumOuts: Int)
                  (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  // Inputs should be fed only when Ready is HIGH
  // Inputs are always latched.
  // If Ready is LOW; Do not change the inputs as this will cause a bug
  val baseAddress = Flipped(Decoupled(new DataBundle()))
  val idx1 = Flipped(Decoupled(new DataBundle()))
  val idx2 = Flipped(Decoupled(new DataBundle()))

  override def cloneType = new GepNodeTwoIO(NumOuts).asInstanceOf[this.type]

}

class GepNodeStackIO(NumOuts: Int)
                    (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  // Inputs should be fed only when Ready is HIGH
  // Inputs are always latched.
  // If Ready is LOW; Do not change the inputs as this will cause a bug
  val baseAddress = Flipped(Decoupled(new DataBundle()))

  override def cloneType = new GepNodeStackIO(NumOuts).asInstanceOf[this.type]

}

class GepNodeIO(NumIns: Int, NumOuts: Int)
               (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  // Inputs should be fed only when Ready is HIGH
  // Inputs are always latched.
  // If Ready is LOW; Do not change the inputs as this will cause a bug
  val baseAddress = Flipped(Decoupled(new DataBundle()))
  val idx = Vec(NumIns, Flipped(Decoupled(new DataBundle())))

  override def cloneType = new GepNodeIO(NumIns, NumOuts).asInstanceOf[this.type]
}


class GepOneNode(NumOuts: Int, ID: Int)
                (numByte1: Int)
                (implicit p: Parameters,
                 name: sourcecode.Name,
                 file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeOneIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = base_addr_R.predicate & idx1_R.predicate & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  // Output
  val data_W = base_addr_R.data +
    (idx1_R.data * numByte1.U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        /*
                when((~enable_R.control).toBool) {
                  idx1_R := DataBundle.default
                  base_addr_R := DataBundle.default

                  idx1_valid_R := false.B
                  base_addr_valid_R := false.B

                  Reset()
                  printf("[LOG] " + "[" + module_name + "] [TID-> %d]" + node_name + ": Not predicated value -> reset\n", enable_R.taskID)
                }.elsewhen((idx1_valid_R) && (base_addr_valid_R)) {
        */
        when((idx1_valid_R) && (base_addr_valid_R)) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " +
            node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }


}


class GepTwoNode(NumOuts: Int, ID: Int)
                (numByte1: Int,
                 numByte2: Int)
                (implicit p: Parameters,
                 name: sourcecode.Name,
                 file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeTwoIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)

  // Index 2 input
  val idx2_R = RegInit(DataBundle.default)
  val idx2_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  io.idx2.ready := ~idx2_valid_R
  when(io.idx2.fire()) {
    idx2_R <> io.idx2.bits
    idx2_valid_R := true.B
  }

  val data_W = base_addr_R.data +
    (idx1_R.data * numByte1.U) + (idx2_R.data * numByte2.U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
     *            STATES                          *
     *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(idx1_valid_R && idx2_valid_R && base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        idx2_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        idx2_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }
}

class GepNodeStack(NumOuts: Int, ID: Int)
                  (numByte1: Int)
                  (implicit p: Parameters,
                   name: sourcecode.Name,
                   file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeStackIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = base_addr_R.predicate & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  // Output
  val data_W = base_addr_R.data +
    (enable_R.taskID * numByte1.U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID | enable_R.taskID
  }

  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }


}


/**
  * GepStructNode
  * Contains list of size of the element types of the structure and the input index
  * will pick the correct offset.
  *
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @param numByte
  * @param p
  * @param name
  * @param file
  */
class GepStructOneNode(NumOuts: Int, ID: Int)
                      (numByte: List[Int])
                      (implicit p: Parameters,
                       name: sourcecode.Name,
                       file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeOneIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  // Lookup table
  val look_up_table = VecInit(numByte.map(_.U))

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  val data_W = base_addr_R.data +
    (idx1_R.data * numByte.last.U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(idx1_valid_R && base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }
}

/**
  * GepStructNode
  * Contains list of size of the element types of the structure and the input index
  * will pick the correct offset.
  *
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @param numByte
  * @param p
  * @param name
  * @param file
  */
class GepStructTwoNode(NumOuts: Int, ID: Int)
                      (numByte: List[Int])
                      (implicit p: Parameters,
                       name: sourcecode.Name,
                       file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeTwoIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)

  // Index 2 input
  val idx2_R = RegInit(DataBundle.default)
  val idx2_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  // Lookup table
  val look_up_table = VecInit(numByte.map(_.U))

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  io.idx2.ready := ~idx2_valid_R
  when(io.idx2.fire()) {
    idx2_R <> io.idx2.bits
    idx2_valid_R := true.B
  }

  val data_W = base_addr_R.data +
    (idx1_R.data * numByte.last.U) + look_up_table(idx2_R.data)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(idx1_valid_R && idx2_valid_R && base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        idx2_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        idx2_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }
}

/**
  * GepArrayNode
  * Contains list of size of the element types of the structure and the input index
  * will pick the correct offset.
  *
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @param numByte
  * @param p
  * @param name
  * @param file
  */
class GepArrayOneNode(NumOuts: Int, ID: Int)
                     (numByte: Int)
                     (size: Int)
                     (implicit p: Parameters,
                      name: sourcecode.Name,
                      file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeOneIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)


  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  val data_W = base_addr_R.data +
    (idx1_R.data * (numByte * size).U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(idx1_valid_R && base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }
}

/**
  * GepArrayTwoNode
  * Contains list of size of the element types of the structure and the input index
  * will pick the correct offset.
  *
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @param numByte
  * @param p
  * @param name
  * @param file
  */
class GepArrayTwoNode(NumOuts: Int, ID: Int)
                     (numByte: Int)
                     (size: Int)
                     (implicit p: Parameters,
                      name: sourcecode.Name,
                      file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeTwoIO(NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx1_R = RegInit(DataBundle.default)
  val idx1_valid_R = RegInit(false.B)

  // Index 2 input
  val idx2_R = RegInit(DataBundle.default)
  val idx2_valid_R = RegInit(false.B)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  // Lookup table
  //  val look_up_table = VecInit(numByte.map(_.U))

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  io.idx1.ready := ~idx1_valid_R
  when(io.idx1.fire()) {
    idx1_R <> io.idx1.bits
    idx1_valid_R := true.B
  }

  io.idx2.ready := ~idx2_valid_R
  when(io.idx2.fire()) {
    idx2_R <> io.idx2.bits
    idx2_valid_R := true.B
  }

  val data_W = base_addr_R.data +
    (idx1_R.data * (numByte * size).U) + (idx2_R.data * numByte.U)

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_W
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(idx1_valid_R && idx2_valid_R && base_addr_valid_R) {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx1_R := DataBundle.default
        idx2_R := DataBundle.default
        base_addr_R := DataBundle.default

        idx1_valid_R := false.B
        idx2_valid_R := false.B
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_W)
        }
      }
    }
  }
}


/**
  * GetElementPtrNodeIO
  * GEP node calculates memory addresses for Load and Store instructions
  * Features:
  * It has a list of sizes -> compile
  * It has a list of indexes -> runtime
  */

class GepIO(NumIns: Int, NumOuts: Int)
           (implicit p: Parameters)
  extends CoreBundle {

  // Input indexes
  // Indexes can be either constant our coming from other nodes
  val BaseAddress = Flipped(Decoupled(new DataBundle))

  val InIndex = Vec(NumIns, Flipped((Decoupled(new DataBundle))))

  val enable = Flipped(Decoupled(new ControlBundle))

  val Out = Vec(NumOuts, Decoupled(new DataBundle))

  //  3.1
  override def cloneType = new GepIO(NumIns, NumOuts).asInstanceOf[this.type]
}


class GepNode(NumIns: Int, NumOuts: Int, ID: Int)
             (ElementSize: Int, ArraySize: List[Int])
             (implicit p: Parameters,
              name: sourcecode.Name,
              file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeIO(NumIns, NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx_R = Seq.fill(NumIns)(RegInit(DataBundle.default))
  val idx_valid_R = Seq.fill(NumIns)(RegInit(false.B))


  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  //We support only geps with 1 or 2 inputs
  assert(NumIns <= 2)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  for (i <- 0 until NumIns) {
    io.idx(i).ready := ~idx_valid_R(i)
    when(io.idx(i).fire()) {
      idx_R(i) <> io.idx(i).bits
      idx_valid_R(i) := true.B
    }
  }

  val seek_value =
    if (ArraySize.isEmpty) {
      idx_R(0).data * ElementSize.U
    } else if (ArraySize.length == 1) {
      (idx_R(0).data * ArraySize(0).U) + (idx_R(1).data * ElementSize.U)
    }
    else {
      0.U
    }

  val data_out = base_addr_R.data + seek_value

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_out
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R && base_addr_valid_R && idx_valid_R.reduce(_ & _)) {
        ValidOut()
        state := s_COMPUTE
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx_R.foreach(_ := DataBundle.default)
        base_addr_R := DataBundle.default

        idx_valid_R.foreach(_ := false.B)
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] [GEP] " +
            node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_out)
        }
      }
    }
  }
}


/**
  * GetElementPtrNodeIO
  * GEP node calculates memory addresses for Load and Store instructions
  * Features:
  * It has a list of sizes -> compile
  * It has a list of indexes -> runtime
  */

class GepFastNode(NumIns: Int, NumOuts: Int, ID: Int)
                 (ElementSize: Int, ArraySize: List[Int])
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new GepNodeIO(NumIns, NumOuts))
  // Printf debugging
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Addr Inputs
  val base_addr_R = RegInit(DataBundle.default)
  val base_addr_valid_R = RegInit(false.B)

  // Index 1 input
  val idx_R = Seq.fill(NumIns)(RegInit(DataBundle.default))
  val idx_valid_R = Seq.fill(NumIns)(RegInit(false.B))


  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state = RegInit(s_IDLE)

  //We support only geps with 1 or 2 inputs
  assert(NumIns <= 2)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  io.baseAddress.ready := ~base_addr_valid_R
  when(io.baseAddress.fire()) {
    base_addr_R <> io.baseAddress.bits
    base_addr_valid_R := true.B
  }

  for (i <- 0 until NumIns) {
    io.idx(i).ready := ~idx_valid_R(i)
    when(io.idx(i).fire()) {
      idx_R(i) <> io.idx(i).bits
      idx_valid_R(i) := true.B
    }
  }

  val seek_value =
    if (ArraySize.isEmpty) {
      idx_R(0).data * ElementSize.U
    } else if (ArraySize.length == 1) {
      (idx_R(0).data * ArraySize(0).U) + (idx_R(1).data * ElementSize.U)
    }
    else {
      0.U
    }

  val data_out = base_addr_R.data + seek_value

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits.data := data_out
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := base_addr_R.taskID
  }


  /*============================================*
   *            STATES                          *
   *============================================*/

  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(enable_R.control) {
          when(idx_valid_R.reduce(_ & _) && base_addr_valid_R) {
            ValidOut()
            io.Out.map(_.valid) foreach (_ := true.B)
            state := s_COMPUTE
          }
        }.otherwise {
          ValidOut()
          state := s_COMPUTE
        }
      }

    }
    is(s_COMPUTE) {
      when(IsOutReady()) {
        // Reset output
        idx_R.foreach(_ := DataBundle.default)
        base_addr_R := DataBundle.default

        idx_valid_R.foreach(_ := false.B)
        base_addr_valid_R := false.B

        // Reset state
        state := s_IDLE

        // Reset output
        Reset()
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] [GEP] " +
            node_name + ": Output fired @ %d, Value: %d\n", enable_R.taskID, cycleCount, data_out)
        }
      }
    }
  }
}


