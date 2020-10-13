package node

import chisel3._
import chisel3.util._

import config._
import interfaces._
import util._

class LiveInNodeIO(NumOuts: Int)
                  (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  //Input data for live in
  val InData = Flipped(Decoupled(new DataBundle()))

  override def cloneType = new LiveInNodeIO(NumOuts).asInstanceOf[this.type]
}

class LiveInNode(NumOuts: Int, ID: Int)
                (implicit p: Parameters,
                 name: sourcecode.Name,
                 file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {
  override lazy val io = IO(new LiveInNodeIO(NumOuts))

  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  // Printf debugging
  override val printfSigil = module_name + ": " + node_name + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // In data Input
  val indata_R = RegInit(DataBundle.default)
  val indata_valid_R = RegInit(false.B)

  val s_IDLE :: s_LATCH :: s_VALIDOUT :: Nil = Enum(3)

  val state = RegInit(s_IDLE)

  /*===============================================*
   *            LATCHING INPUTS                    *
   *===============================================*/

  io.InData.ready := ~indata_valid_R
  when(io.InData.fire()) {
    indata_R <> io.InData.bits
    indata_valid_R := true.B
  }

  /*===============================================*
   *            DEFINING STATES                    *
   *===============================================*/

  switch(state) {
    is(s_IDLE) {
      when(io.InData.fire()) {
        state := s_VALIDOUT
        ValidOut()
        printf("[LOG] " + "[" + module_name + "] " + node_name + ": Latch fired @ %d, Value:%d\n", cycleCount, io.InData.bits.data.asUInt())
      }
    }
    is(s_VALIDOUT) {
      when(IsOutReady()) {
        state := s_LATCH
      }
    }
    is(s_LATCH) {
      when(enable_valid_R) {
        when(enable_R.control) {
          printf("[LOG] " + "[" + module_name + "] " + node_name + ": Latch invalidate @ %d\n", cycleCount)
          state := s_IDLE
          indata_R <> DataBundle.default
          indata_valid_R := false.B
          Reset()
        }.otherwise {
          state := s_VALIDOUT
          ValidOut()
          Reset()
        }
      }
    }
  }

  /*switch(state) {
    is(s_IDLE) {
      when(io.enable.fire() || enable_valid_R) {
        when(io.enable.bits.control || enable_R) {
          when(io.InData.fire() || indata_valid_R) {

            printf("[LOG] " + "[" + module_name + "] " + node_name + ": Latch fired @ %d, Value:%d\n"
              , cycleCount, io.InData.bits.data.asUInt())

            ValidOut()
            state := s_LATCH

            enable_R := false.B
            enable_valid_R := false.B

          }.otherwise{
            state := s_IDLE
          }
        }.elsewhen((~io.enable.bits.control).toBool || (~enable_R).toBool) {
          when(io.InData.fire() || indata_valid_R){
            indata_R := DataBundle.default
            indata_valid_R := false.B

            enable_R := false.B
            Reset()

            state := s_LATCH
          }.otherwise{
            state := s_IDLE
          }
        }
      }
    }

    is(s_LATCH){
      when(IsOutReady()){
        when(io.enable.fire() || enable_valid_R){
          when(io.enable.bits.control || enable_R){
            enable_R := false.B
            Reset()
            ValidOut()
            state := s_LATCH
          }
        }.elsewhen((~io.enable.bits.control).toBool || (~enable_R).toBool){
          enable_R := false.B
          Reset()
          state := s_IDLE
        }
      }.otherwise{
        state := s_LATCH
      }
    }
  }*/

  /*===============================================*
   *            CONNECTING OUTPUTS                 *
   *===============================================*/

  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> indata_R
  }
}


class LiveInNewNodeIO(NumOuts: Int)
                     (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {

  //Input data for live in
  val InData = Flipped(Decoupled(new DataBundle()))

  val Invalid = Flipped(Decoupled(new ControlBundle()))

}


class LiveInNewNode(NumOuts: Int, ID: Int)
                   (implicit p: Parameters,
                    name: sourcecode.Name,
                    file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle())(p) {
  override lazy val io = IO(new LiveInNewNodeIO(NumOuts))

  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  // Printf debugging
  override val printfSigil = module_name + ": " + node_name + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // In data Input
  val indata_R = RegInit(DataBundle.default)
  val indata_valid_R = RegInit(false.B)

  val invalid_R = RegInit(ControlBundle.default)
  val invalid_valid_R = RegInit(false.B)

  val s_IDLE :: s_LATCH :: Nil = Enum(2)

  val state = RegInit(s_IDLE)

  /*===============================================*
   *            LATCHING INPUTS                    *
   *===============================================*/

  io.InData.ready := ~indata_valid_R
  when(io.InData.fire()) {
    indata_R <> io.InData.bits
    indata_valid_R := true.B
  }

  io.Invalid.ready := ~invalid_valid_R
  when(io.Invalid.fire()) {
    invalid_R <> io.Invalid.bits
    invalid_valid_R := true.B
  }


  /*===============================================*
   *            DEFINING STATES                    *
   *===============================================*/

  switch(state) {
    is(s_IDLE) {
      when(io.enable.fire() || enable_valid_R) {
        when(io.enable.bits.control || enable_R.control) {
          when(indata_valid_R) {
            state := s_LATCH
            ValidOut()

            printf("[LOG] " + "[" + module_name + "] " + node_name + ": Latch fired @ %d, Value:%d\n", cycleCount, io.InData.bits.data.asUInt())
          }
        }.otherwise {
          assert((~enable_R.control).toBool, "ERROR!!")
        }
      }
    }
    is(s_LATCH) {
      when(IsOutReady()) {
        when(io.Invalid.fire() || invalid_valid_R) {
          when(io.Invalid.bits.control || invalid_R.control) {
            printf("[LOG] " + "[" + module_name + "] " + node_name + ": Latch invalidate @ %d\n", cycleCount)

            state := s_IDLE

            indata_R <> DataBundle.default
            indata_valid_R := false.B

            invalid_R <> ControlBundle.default
            invalid_valid_R := false.B

            out_ready_R.foreach(_ := false.B)
            enable_valid_R := false.B
          }.elsewhen((~io.Invalid.bits.control).toBool || (~invalid_R.control).toBool) {
            state := s_LATCH
            ValidOut()
            invalid_R <> ControlBundle.default
            invalid_valid_R := false.B

            out_ready_R.foreach(_ := false.B)
          }
        }
      }
    }
  }

  /*===============================================*
   *            CONNECTING OUTPUTS                 *
   *===============================================*/

  for (i <- 0 until NumOuts) {
    io.Out(i).bits <> indata_R
  }
}

