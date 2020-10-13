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
//  2. Need registers for pipeline handshaking e.g., _valid,
// _ready need to latch ready and valid signals.
//////////

class TypLoadIO(NumPredOps: Int, NumSuccOps: Int, NumOuts: Int)(implicit p: Parameters) extends HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(new TypBundle) {
  // Node specific IO
  // GepAddr: The calculated address comming from GEP node
  val GepAddr = Flipped(Decoupled(new DataBundle))
  // Memory request
  val memReq  = Decoupled(new ReadReq( ))
  // Memory response.
  val memResp = Input(Flipped(new ReadResp( )))

  override def cloneType = new TypLoadIO(NumPredOps, NumSuccOps, NumOuts).asInstanceOf[this.type]
}

/**
  * @brief Type Load Node. Implements store operations
  * @details [long description]
  * @param NumPredOps [Number of predicate memory operations]
  */
class TypLoad(NumPredOps: Int,
              NumSuccOps: Int,
              NumOuts: Int,
              ID: Int,
              RouteID: Int)
             (implicit p: Parameters,
              name: sourcecode.Name,
              file: sourcecode.File)
  extends HandShaking(NumPredOps, NumSuccOps, NumOuts, ID)(new TypBundle)(p) {

  // Set up StoreIO
  override lazy val io = IO(new TypLoadIO(NumPredOps, NumSuccOps, NumOuts))
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

  // Memory Response
  val data_R       = RegInit(TypBundle.default)
  val data_valid_R = RegInit(false.B)
  val recvptr      = RegInit(0.U(log2Ceil(Beats + 1).W))
  val linebuffer   = RegInit(VecInit(Seq.fill(Beats)(0.U(xlen.W))))

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
    //addr_R.valid := true.B
    addr_valid_R := true.B
  }

  /*============================================
  =            Predicate Evaluation            =
  ============================================*/

  val complete     = IsSuccReady( ) && IsOutReady( )
  val predicate    = addr_R.predicate && enable_R.control
  val mem_req_fire = addr_valid_R && IsPredValid( )


  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := addr_R.taskID | enable_R.taskID
  }

  io.memReq.valid := false.B
  io.memReq.bits.address := addr_R.data
  io.memReq.bits.Typ := MT_W
  io.memReq.bits.RouteID := RouteID.U
  io.memReq.bits.taskID := addr_R.taskID


  // Connect successors outputs to the enable status
  when(io.enable.fire( )) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }
  /*=============================================
  =            ACTIONS (possibly dangerous)     =
  =============================================*/


  switch(state) {
    is(s_idle) {
      when(IsEnableValid() && mem_req_fire) {
        when(predicate) {
          io.memReq.valid := true.B
          when(io.memReq.ready) {
            state := s_RECEIVING
          }
        }.otherwise {
          data_R.predicate := false.B
          ValidSucc( )
          ValidOut( )
          // Completion state.
          state := s_Done
        }
      }
    }
    is(s_RECEIVING) {
      when(io.memResp.valid && (recvptr =/= (Beats).U)) {
        linebuffer(recvptr) := io.memResp.data
        recvptr := recvptr + 1.U
      }
      when(recvptr === (Beats).U) {
        // Set data output registers
        data_R.data := linebuffer.asUInt
        data_R.predicate := true.B
        ValidSucc( )
        ValidOut( )
        // Completion state
        state := s_Done
      }
    }
    is(s_Done) {
      when(complete) {
        // Clear all the valid states.
        // Reset address
        //        addr_R := DataBundle.default
        addr_valid_R := false.B
        // Reset data
        //        data_R := DataBundle.default
        data_valid_R := false.B
        // Clear ptrs
        recvptr := 0.U
        // Clear all other state
        Reset( )
        // Reset state.
        state := s_idle
        printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d\n", enable_R.taskID, cycleCount)
        //printf("DEBUG " + node_name + ": $%d = %d\n", addr_R.data, data_R.data)
      }
    }
  }
  // Trace detail.
  if (log == true && (comp contains "TYPLOAD")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"GepAddr\": %x},", x, (addr_valid_R))
        printf("\"State\": {\"State\": \"%x\", \"data_R(Valid,Data,Pred)\": \"%x,%x,%x\" },", state, data_valid_R, data_R.data, data_R.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {}
    }
  }
}
