package node

import chisel3.{RegInit, _}
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

class StoreAliasIO(NumPredOps: Int,
                   NumSuccOps: Int,
                   NumAliasPredOps: Int,
                   NumAliasSuccOps: Int,
                   NumOuts: Int)(implicit p: Parameters)
  extends HandShakingAliasIO(NumPredOps = NumPredOps, NumSuccOps = NumSuccOps, NumAliasPredOps = NumAliasPredOps, NumAliasSuccOps = NumAliasSuccOps, NumOuts = NumOuts)(new DataBundle) { // Node specific IO
// GepAddr: The calculated address comming from GEP node
val GepAddr = Flipped(Decoupled(new DataBundle))
  // Store data.
val inData  = Flipped(Decoupled(new DataBundle))
  // Memory request
val memReq  = Decoupled(new WriteReq( ))
  // Memory response.
val memResp = Input(Flipped(new WriteResp( )))

  override def cloneType = new StoreAliasIO(NumPredOps, NumSuccOps, NumAliasPredOps, NumAliasSuccOps, NumOuts).asInstanceOf[this.type]

}

/**
  * @brief Store Node. Implements store operations
  * @details [long description]
  * @param NumPredOps [Number of predicate memory operations]
  */
class UnTypStoreAlias(NumPredOps: Int,
                      NumSuccOps: Int,
                      NumAliasPredOps: Int = 0,
                      NumAliasSuccOps: Int = 0,
                      NumOuts: Int = 1,
                      Typ: UInt = MT_W,
                      ID: Int,
                      RouteID: Int)(implicit p: Parameters,
                                    name: sourcecode.Name,
                                    file: sourcecode.File)
  extends HandShakingAlias(NumPredOps = NumPredOps, NumSuccOps = NumSuccOps, NumAliasPredOps = NumAliasPredOps, NumAliasSuccOps = NumAliasSuccOps, NumOuts = NumOuts, ID = ID)(new DataBundle)(p) {

  // Set up StoreIO
  override lazy val io = IO(new StoreAliasIO(NumPredOps, NumSuccOps, NumAliasPredOps, NumAliasSuccOps, NumOuts))
  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*=============================================
  =            Register declarations            =
  =============================================*/

  // OP Inputs
  val addr_R       = RegInit(DataBundle.default)
  val data_R       = RegInit(DataBundle.default)
  val addr_valid_R = RegInit(false.B)
  val data_valid_R = RegInit(false.B)

  // State machine
  val s_idle :: s_RECEIVING :: s_Done :: Nil = Enum(3)
  val state                                  = RegInit(s_idle)

  /*================================================
  =            Latch inputs. Set output            =
  ================================================*/

  //Initialization READY-VALIDs for GepAddr and Predecessor memory ops
  io.GepAddr.ready := ~addr_valid_R
  io.inData.ready := ~data_valid_R

  // ACTION: GepAddr
  io.GepAddr.ready := ~addr_valid_R
  when(io.GepAddr.fire( )) {
    addr_R := io.GepAddr.bits
    addr_valid_R := true.B
  }

  // ACTION: inData
  when(io.inData.fire( )) {
    // Latch the data
    data_R := io.inData.bits
    data_valid_R := true.B
  }

  /*============================================
 =            Predicate Evaluation            =
 ============================================*/
  // Ensure all predecessors have arrived. This is needed to ensure current node doesnt runahead in the pipeline
  // Ensure all sucessesors. Alias or Not
  val complete = IsSuccReady( ) && IsOutReady( ) && IsAliasPredValid( ) && IsAliasSuccReady( ) && IsAliasOutReady( )

  // And AliasPredValid
  val predicate    = addr_R.predicate && enable_R.control
  val mem_req_fire = addr_valid_R && data_valid_R && IsPredValid( ) && AliasInfoAvail( ) && AliasReady(addr_R.data)


  ConnectAliasInfo(addr_R.data, addr_R.taskID)
  //  As soon as the address is available provide the data.
  //  But only once as they are registers.
  //  The pipeline logic demands that you only ValidSucc and ValidOut exactly once.
  when(io.GepAddr.fire( )) {
    ValidAliasOut( )
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    io.Out(i).bits := data_R
    io.Out(i).bits.taskID := data_R.taskID | addr_R.taskID | enable_R.taskID
  }
  // Outgoing Address Req ->
  io.memReq.bits.address := addr_R.data
  io.memReq.bits.data := data_R.data
  io.memReq.bits.Typ := Typ
  io.memReq.bits.RouteID := RouteID.U
  io.memReq.bits.taskID := data_R.taskID | addr_R.taskID | enable_R.taskID
  io.memReq.bits.mask := 15.U
  io.memReq.valid := false.B

  when(io.enable.fire( )) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }
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
          data_R.predicate := false.B
          ValidSucc( )
          ValidOut( )
          ValidAliasSucc( )
          // Completion state
          state := s_Done
        }
      }
    }
    is(s_RECEIVING) {
      when(io.memResp.valid) {


        ValidSucc( )
        // Indicate to successors that alias has completed.
        ValidAliasSucc( )
        ValidOut( )
        state := s_Done
      }
    }
    is(s_Done) {
      when(complete) {
        // Clear all the valid states.
        // Reset address
        addr_R := DataBundle.default
        addr_valid_R := false.B
        // Reset data.
        data_R := DataBundle.default
        data_valid_R := false.B
        // Clear all other state
        Reset( )
        // Reset state.
        state := s_idle
        printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d, data: %d\n", enable_R.taskID, cycleCount, data_R.data)
        //printf("DEBUG " + node_name + ": $%d = %d\n", addr_R.data, data_R.data)
      }
    }
  }
  // Trace detail.
  if (log == true && (comp contains "STORE")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U
    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : { \"Inputs\": {\"GepAddr\": %x},", x, (addr_valid_R))
        printf("\"State\": {\"State\": \"%x\", \"data_R(Valid,Data,Pred)\": \"%x,%x,%x\" },", state, data_valid_R, data_R.data, io.Out(0).bits.predicate)
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire( ))
        printf("}")
      }
      case everythingElse => {}
    }
  }
}
