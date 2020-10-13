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

class TypStoreIO(NumPredOps: Int,
  NumSuccOps: Int,
  NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(new TypBundle) {
  // Node specific IO
  // GepAddr: The calculated address comming from GEP node
  val GepAddr = Flipped(Decoupled(new DataBundle))
  // Store data.
  val inData = Flipped(Decoupled(new TypBundle))
  // Memory request
  val memReq = Decoupled(new WriteReq())
  // Memory response.
  val memResp = Input(Flipped(new WriteResp()))

  override def cloneType = new TypStoreIO(NumPredOps, NumSuccOps, NumOuts).asInstanceOf[this.type]
}

/**
 * @brief TYPE Store Node. Implements store operations
 * @details [long description]
 *
 * @param NumPredOps [Number of predicate memory operations]
 */
class TypStore(NumPredOps: Int,
               NumSuccOps: Int,
               NumOuts: Int,
               ID: Int,
               RouteID: Int)
               (implicit p: Parameters,
               name: sourcecode.Name,
               file: sourcecode.File)
  extends HandShaking(NumPredOps, NumSuccOps, NumOuts, ID)(new TypBundle)(p) {

  // Set up StoreIO
  override lazy val io = IO(new TypStoreIO(NumPredOps, NumSuccOps, NumOuts))

  // Printf debugging
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

  val data_R = RegInit(TypBundle.default)
  val data_valid_R = RegInit(false.B)

  // State machine
  val s_idle :: s_RECEIVING :: s_Done :: Nil = Enum(3)
  val state = RegInit(s_idle)

  val sendptr = Counter(Beats)
  val buffer = data_R.data.asTypeOf(Vec(Beats, UInt(xlen.W)))

/*================================================
=            Latch inputs. Set output            =
================================================*/

  //Initialization READY-VALIDs for GepAddr and Predecessor memory ops
  io.GepAddr.ready := ~addr_valid_R
  io.inData.ready := ~data_valid_R

  // ACTION: GepAddr
  io.GepAddr.ready := ~addr_valid_R
  when(io.GepAddr.fire()) {
    addr_R := io.GepAddr.bits
    addr_valid_R := true.B
  }

  // ACTION: inData
  when(io.inData.fire()) {
    // Latch the data
    data_R := io.inData.bits
    data_valid_R := true.B
  }

  /*============================================
  =            Predicate Evaluation            =
  ============================================*/
  val start = addr_valid_R & data_valid_R & IsPredValid() & IsEnableValid()
  val complete = IsSuccReady() && IsOutReady()
  val predicate = addr_R.predicate && data_R.predicate && enable_R.control
  val mem_req_fire = addr_valid_R && data_valid_R  && IsPredValid()


  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
    io.Out(i).bits.predicate := predicate
    io.Out(i).bits.taskID := enable_R.taskID | addr_R.taskID | data_R.taskID

  }


  // Outgoing Address Req ->
  io.memReq.valid := false.B
  io.memReq.bits.address := addr_R.data
  io.memReq.bits.data    := buffer(sendptr.value)
  io.memReq.bits.Typ := MT_W
  io.memReq.bits.RouteID := RouteID.U
  io.memReq.bits.taskID  := enable_R.taskID | addr_R.taskID | data_R.taskID
  io.memReq.bits.mask    := 15.U

  // Connect successors outputs to the enable status
  when(io.enable.fire()) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }
  when(start & predicate) {
    /*=============================================
    =            ACTIONS (possibly dangerous)     =
    =============================================*/
    switch(state) {
      is(s_idle) {
        when(enable_valid_R && mem_req_fire) {
          io.memReq.valid := true.B
          // Arbitration ready. Move on to the next word
          when(io.memReq.fire()) {
            sendptr.inc()
            // If last word then move to next state.
            when(sendptr.value === (Beats - 1).U) {
              state := s_RECEIVING
            }
          }
        }
      }
      is(s_RECEIVING) {
        //  ACTION:  <- Incoming Data
        when(io.memResp.valid) {
          // Set output to valid
          ValidSucc()
          ValidOut()
          state := s_Done
        }
      }
    }
  }.elsewhen(start && !predicate) {
    ValidSucc()
    ValidOut()
    state := s_Done
  }
  /*===========================================
=            Output Handshaking and Reset   =
===========================================*/

  //  ACTION: <- Check Out READY and Successors READY
  when(state === s_Done) {
    when(complete) {
      // Clear all the valid states.
      // Reset address
      // addr_R := DataBundle.default
      addr_valid_R  := false.B
      // Reset data.
      // data_R := TypBundle.default
      data_valid_R  := false.B
      // Clear ptrs
      sendptr.value := 0.U
      // Clear all other state
      Reset()
      // Reset state.
      state := s_idle
      printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d\n",enable_R.taskID, cycleCount)

    }
  }
  // Trace detail.
  if (log == true && (comp contains "TYPSTORE")) {
    val x = RegInit(0.U(xlen.W))
    x     := x + 1.U


    verb match {
      case "high"  => { }
      case "med"   => { }
      case "low"   => {
        printfInfo("Cycle %d : { \"Inputs\": {\"GepAddr\": %x, \"inData\": %x },\n",x, (addr_valid_R),(data_valid_R))
        printf("\"State\": {\"State\": %x, \"data_R\": \"%x,%x\" },",state,data_R.data,io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}",io.Out(0).fire())
        printf("}")
       }
      case everythingElse => {}
    }
  }
}
