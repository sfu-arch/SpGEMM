package node

import chisel3._
import chisel3.util._
import org.scalacheck.Prop.False

import config._
import interfaces._
import utility.Constants._
import utility.UniformPrintfs


// Design Doc
//////////
/// DRIVER ///
/// 1. Memory response only available atleast 1 cycle after request
//  2. Handshaking has to be done with registers.
// @todo : This node will only receive one word. To handle doubles. Change handshaking logic
//////////

class PrefetchIO(NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIOPS(0, 0, NumOuts)(new DataBundle) {
  // GepAddr: The calculated address comming from GEP node
  val GepAddr = Flipped(Decoupled(new DataBundle))
  // Memory request
  val memReq  = Decoupled(new MemReq( ))

  override def cloneType = new PrefetchIO(NumOuts).asInstanceOf[this.type]
}

/**
  * @brief Prefetch Node. Implements Prefetch operations
  * @details [Prefetch operations can either reference values in a scratchpad or cache]
  * @param NumOuts [always 1 output. To ensure circuit is not ripped out]
  */
class PrefetchLoad(NumOuts: Int,
                   ID: Int)(implicit p: Parameters,
                            name: sourcecode.Name,
                            file: sourcecode.File)
  extends HandShaking(0, 0, NumOuts, ID)(new DataBundle)(p) {

  override lazy val io = IO(new PrefetchIO(NumOuts))
  // Printf debugging
  val node_name       = name.value
  val module_name     = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "


  /*=============================================
  =            Registers                        =
  =============================================*/
  // OP Inputs
  val addr_R       = RegInit(DataBundle.default)
  val addr_valid_R = RegInit(false.B)

  // State machine
  val s_idle :: s_RECEIVING :: s_Done :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)


  /*================================================
  =            Latch inputs. Wire up output            =
  ================================================*/

  //Initialization READY-VALIDs for GepAddr and Predecessor memory ops
  io.GepAddr.ready := ~addr_valid_R
  when(io.GepAddr.fire( )) {
    addr_R := io.GepAddr.bits
    addr_valid_R := true.B
  }

  /*============================================
  =            Predicate Evaluation            =
  ============================================*/

  val complete     = IsOutReady( )
  val predicate    = addr_R.predicate && enable_R.control
  val mem_req_fire = addr_valid_R


  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := addr_R
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := addr_R.taskID | enable_R.taskID
  }

  io.memReq.valid := false.B
  io.memReq.bits.addr := (addr_R.data >> log2Ceil(xlen / 8)) << log2Ceil(xlen / 8)
  io.memReq.bits.iswrite := false.B
  io.memReq.bits.taskID := addr_R.taskID
  io.memReq.bits.tag := ID.U
  io.memReq.bits.mask := 0.U
  io.memReq.bits.tile := 0.U
  io.memReq.bits.data := 0.U


  /*=============================================
  =            ACTIONS (possibly dangerous)     =
  =============================================*/


  switch(state) {
    is(s_idle) {
      when(enable_valid_R && mem_req_fire) {
        when(enable_R.control && predicate) {
          io.memReq.valid := true.B
          when(io.memReq.ready) {
            state := s_RECEIVING
          }
        }.otherwise {
          state := s_RECEIVING
        }
      }
    }
    is(s_RECEIVING) {
      ValidOut( )
      // Completion state.
      state := s_Done
    }
    is(s_Done) {
      when(complete) {
        // Clear all the valid states.
        // Reset address
        //        addr_R := DataBundle.default
        addr_valid_R := false.B
        // Reset state.
        Reset( )
        // Reset state.
        state := s_idle
        printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, Address:%d\n", enable_R.taskID, cycleCount, addr_R.data)
        //printf("DEBUG " + node_name + ": $%d = %d\n", addr_R.data, data_R.data)
      }
    }
  }

  // Trace detail.
  if (log == true && (comp contains "LOAD")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U
    verb match {
      case "high" => {
      }
      case "med" => {
      }
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"GepAddr\": %x},", x, (addr_valid_R))
        printf("\"State\": {\"State\": \"%x\" },", state)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {
      }
    }
  }
}
