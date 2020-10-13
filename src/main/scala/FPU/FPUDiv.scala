package FPU

import chisel3.{RegInit, _}
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import interfaces._
import util._
import node._
import FType._
import org.scalacheck.Prop.False
import utility.Constants._
import utility.UniformPrintfs

// Design Doc
//////////
/// DRIVER ///
/// 1. FU response only available atleast 1 cycle after request
//  2. Need registers for pipeline handshaking e.g., _valid,
// _ready need to latch ready and valid signals.
//////////

class FPDivSqrtIO(NumOuts: Int,
                  argTypes: Seq[Int])
                 (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // Divisor or Sqrt
  val a      = Flipped(Decoupled(new DataBundle))
  // Dividend
  val b      = Flipped(Decoupled(new DataBundle))
  // FU request
  val FUReq  = Decoupled(new FUReq(argTypes))
  // FU response.
  val FUResp = Input(Flipped(new FUResp()))

  override def cloneType = new FPDivSqrtIO(NumOuts, argTypes).asInstanceOf[this.type]
}

/**
  * @brief FP Node. Implements store operations
  * @details [long description]
  * @param NumOuts [Number of FU operations]
  */
class FPDivSqrtNode(NumOuts: Int,
                    ID: Int,
                    RouteID: Int = 0,
                    opCode: String)
                   (t: FType)
                   (implicit p: Parameters,
                    name: sourcecode.Name,
                    file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPDivSqrtIO(NumOuts, List(xlen, xlen, 1)))

  // Printf debugging
  val node_name       = name.value
  val module_name     = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "


  /*=============================================
  =            Registers                        =
  =============================================*/

  // Dividend or Sqrt
  val a_R       = RegInit(DataBundle.default)
  val a_valid_R = RegInit(false.B)

  // Divisor
  val b_R       = RegInit(DataBundle.default)
  val b_valid_R = RegInit(false.B)

  // FU Response
  val data_R       = RegInit(DataBundle.default)
  val data_valid_R = RegInit(false.B)

  // State machine
  val s_idle :: s_RECEIVING :: s_Done :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)

  /*   ================================================
    =            Latch inputs. Set output            =
    ================================================  */

  //Initialization READY-VALIDs for Dividor and Dividend  FU ops
  io.a.ready := ~a_valid_R
  io.b.ready := ~b_valid_R

  // ACTION: A
  when(io.a.fire( )) {
    a_R := io.a.bits
    a_valid_R := true.B
  }

  // ACTION: B
  when(io.b.fire( )) {
    // Latch the data
    b_R := io.b.bits
    b_valid_R := true.B
  }

  /*============================================
  =            Predicate Evaluation            =
  ============================================*/

  val complete    = IsOutReady( )
  val predicate   = a_R.predicate && b_R.predicate && enable_R.control
  val FU_req_fire = a_valid_R && b_valid_R


  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := a_R.taskID | b_R.taskID | enable_R.taskID
  }

  // Outgoing FU Req ->


  io.FUReq.valid := false.B
  io.FUReq.bits.data("field0").data := a_R.data
  io.FUReq.bits.data("field1").data := b_R.data
  io.FUReq.bits.data("field0").predicate := true.B
  io.FUReq.bits.data("field1").predicate := true.B
  io.FUReq.bits.data("field0").taskID := a_R.taskID | b_R.taskID | enable_R.taskID
  io.FUReq.bits.data("field1").taskID := a_R.taskID | b_R.taskID | enable_R.taskID


  require((opCode == "DIV" || opCode == "fdiv" || opCode == "SQRT"), "DIV or SQRT required")
  val DivOrSqrt = opCode match {
    case "DIV" => false.B
    case "fdiv" => false.B
    case "SQRT" => true.B
  }
  io.FUReq.bits.data("field2").data := DivOrSqrt
  io.FUReq.bits.data("field2").predicate := true.B
  io.FUReq.bits.data("field2").taskID := 1.U

  io.FUReq.bits.RouteID := RouteID.U

  /*=============================================
  =            ACTIONS (possibly dangerous)     =
  =============================================*/
  switch(state) {
    is(s_idle) {
      when(enable_valid_R && FU_req_fire) {
        when(enable_R.control && predicate) {
          io.FUReq.valid := true.B
          when(io.FUReq.ready) {
            state := s_RECEIVING
          }
        }.otherwise {
          data_R.predicate := false.B
          ValidOut( )

          state := s_Done
        }
      }
    }
    is(s_RECEIVING) {
      when(io.FUResp.valid) {

        // Set data output registers
        data_R.data := io.FUResp.data
        data_R.predicate := true.B
        //data_R.valid := true.B
        ValidOut( )
        // Completion state.
        state := s_Done
      }
    }
    is(s_Done) {
      when(complete) {
        // Clear all the valid states.
        // Reset a
        a_valid_R := false.B
        // Reset b.
        b_valid_R := false.B
        // Reset data
        data_valid_R := false.B
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
  if (log == true && (comp contains "FPDIV")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U
    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"A\": %x},", x, (a_valid_R))
        printf("\"State\": {\"State\": \"%x\", \"data_R(Valid,Data,Pred)\":\"%x,%x,%x\" },", state, data_valid_R, data_R.data, data_R.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {}
    }
  }
}
