package dnnnode

import chisel3._
import chisel3.util._
import config._
import dnn.memory.TensorParams
import interfaces._
import node.{HandShaking, HandShakingIOPS, Shapes}
import utility.Constants._


class TLoadIO[gen <: Shapes](NumPredOps: Int, NumSuccOps: Int, NumOuts: Int)(shape: => gen)(implicit p: Parameters)
  extends HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(new CustomDataBundle(UInt(shape.getWidth.W))) {
  val GepAddr = Flipped(Decoupled(new DataBundle))
  val tensorReq = Decoupled(new TensorReadReq())
  val tensorResp = Input(Flipped(new TensorReadResp(shape.getWidth)))

  override def cloneType = new TLoadIO(NumPredOps, NumSuccOps, NumOuts)(shape).asInstanceOf[this.type]
}

/**
  * @brief Type Load Node. Implements store operations
  * @details [long description]
  * @param NumPredOps [Number of predicate memory operations]
  */
class TLoad[L <: Shapes](NumPredOps: Int,
                         NumSuccOps: Int,
                         NumOuts: Int,
                         ID: Int = 0,
                         RouteID: Int = 0)(shape: => L)
                        (implicit p: Parameters, name: sourcecode.Name, file: sourcecode.File)
  extends HandShaking(NumPredOps, NumSuccOps, NumOuts, ID)(new CustomDataBundle(UInt(shape.getWidth.W)))(p) {
  override lazy val io = IO(new TLoadIO(NumPredOps, NumSuccOps, NumOuts)(shape))
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "


  /*=============================================
  =            Registers                        =
  =============================================*/
  // OP Inputs
  val addr_R = RegInit(DataBundle.default)
  val addr_valid_R = RegInit(false.B)

  // Memory Response
  val data_R = RegInit(CustomDataBundle.default(0.U(shape.getWidth.W)))
  val data_valid_R = RegInit(false.B)

  // State machine
  val s_idle :: s_RECEIVING :: s_Done :: Nil = Enum(3)
  val state = RegInit(s_idle)


  /*================================================
  =            Latch inputs. Wire up output            =
  ================================================*/

  //Initialization READY-VALIDs for GepAddr and Predecessor memory ops
  io.GepAddr.ready := ~addr_valid_R
  when(io.GepAddr.fire()) {
    addr_R := io.GepAddr.bits
    addr_valid_R := true.B
  }

  /*============================================
  =            Predicate Evaluation            =
  ============================================*/

  val complete = IsSuccReady() && IsOutReady()
  val predicate = addr_R.predicate && enable_R.control
  val mem_req_fire = addr_valid_R && IsPredValid()


  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := addr_R.taskID | enable_R.taskID
  }

  io.tensorReq.valid := false.B
  io.tensorReq.bits.index := addr_R.data
  //  io.tensorReq.bits.Typ := Typ
  io.tensorReq.bits.RouteID := RouteID.U
  io.tensorReq.bits.taskID := addr_R.taskID

  // Connect successors outputs to the enable status
  when(io.enable.fire()) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }
  /*=============================================
  =            ACTIONS (possibly dangerous)     =
  =============================================*/


  switch(state) {
    is(s_idle) {
      when(enable_valid_R && mem_req_fire) {
        when(enable_R.control && predicate) {
          io.tensorReq.valid := true.B
          when(io.tensorReq.ready) {
            state := s_RECEIVING
          }
        }.otherwise {
          data_R.predicate := false.B
          ValidSucc()
          ValidOut()
          // Completion state.
          state := s_Done
        }
      }
    }
    is(s_RECEIVING) {
      when(io.tensorResp.valid) {

        // Set data output registers
        data_R.data := io.tensorResp.data
        data_R.predicate := true.B

        ValidSucc()
        ValidOut()
        // Completion state.
        state := s_Done

      }
    }
    is(s_Done) {
      when(complete) {
        // Clear all the valid states.
        // Reset address
        // addr_R := DataBundle.default
        addr_valid_R := false.B
        // Reset data
        // data_R := DataBundle.default
        data_valid_R := false.B
        // Reset state.
        Reset()
        // Reset state.
        state := s_idle

        /**
          * Cant print value with more than 64bits.
          * In this example value is > 64bits
          */
        //        if (log) {
        //          printf("[LOG] " + "[" + module_name + "] [TID->%d] [LOAD] " + node_name + ": Output fired @ %d, Address:%d, Value: %d\n",
        //            enable_R.taskID, cycleCount, addr_R.data, data_R.data)
        //printf("DEBUG " + node_name + ": $%d = %d\n", addr_R.data, data_R.data)
        //        }
      }
    }
  }
  // Trace detail.
  if (log == true && (comp contains "LOAD")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U
    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"GepAddr\": %x},", x, (addr_valid_R))
        printf("\"State\": {\"State\": \"%x\", \"data_R(Valid,Data,Pred)\":\"%x,%x,%x\" },", state, data_valid_R, data_R.data, data_R.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire())
        printf("}")
      }
      case everythingElse => {}
    }
  }
}
