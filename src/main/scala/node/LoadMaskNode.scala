  package node

/**
  * Created by nvedula on 15/5/17.
  */

import chisel3._
import chisel3.util._
import org.scalacheck.Prop.False
import scala.util.control.Breaks._
import arbiters._
import config._
import interfaces._
import muxes._
import utility._
import memory._

import Constants._

abstract class LoadMaskIO(NumPredOps :Int = 1, NumSuccOps :Int = 1,
  val ID :Int = 0)(implicit val p: Parameters) extends Module with CoreParams{

  val io = IO(new Bundle {
    // gepAddr: The calculated address comming from GEP node
    val Gep = Flipped(Decoupled(UInt(xlen.W)))

    //Bool data from predecessor memory ops
    // using Handshaking protocols
    // predValid has to be size of atleast 1 and a true has to be wired to it
    // even if no predecessors exist.
    val PredOp = Vec(NumPredOps, Flipped(Decoupled(UInt(1.W))))

    //Memory interface
    val MemReq    = Decoupled(new ReadReq())
    val MemResp   = Flipped(Decoupled(new ReadResp()))

    // Data outs.

    // Successor Memory Op
    val SuccOp = Vec(NumPredOps, Decoupled(UInt(1.W))) 
    })
}


class LoadMaskNode(NumPredOps: Int = 1, NumSuccOps: Int = 1)(implicit p: Parameters) extends LoadMaskIO(NumPredOps)(p){



  // Extra information
  val token  = RegInit(0.U)
  val nodeID = RegInit(ID.U)

  val counter = RegInit(1.U)


  // Gep address passed into load
  val GepOperand   = RegInit(0.U(xlen.W))
  val GepValid     = RegInit(false.B)

  // predessor memory ops. whether they are valid.
  val predValid =  Seq.fill(NumPredOps)(RegInit(false.B))
  val succValid =  Seq.fill(NumSuccOps)(RegInit(false.B))

  // Mask for final ANDing and output of data
  val bitmask  = RegInit(0.U((2*xlen).W))
  // Send word mask for tracking how many words need to be read
  val sendbytemask = RegInit(0.U((2*xlen).W))
 

  // Is the request valid and request to memory
  val ReqValid     = RegInit(false.B)
  val ReqAddress   = RegNext((GepOperand >> log2Ceil(xlen/8)) << log2Ceil(xlen/8), 0.U(xlen.W))


  // Incoming data valid and daata operand.
  val DataValid    = RegInit(false.B)
  val ptr          = RegInit(0.U((2*xlen).W))
  val linebuffer   = RegInit(VecInit(Seq.fill(2)(0.U(xlen.W))))
 

  // Latch predecessor valid signals.
  for (i <- 0 until NumPredOps) {
    io.PredOp(i).ready := ~predValid(i)
    when(io.PredOp(i).fire()) {
      predValid(i) := io.PredOp(i).valid      
    }
  }


  //Latch GEP input if it's fire
  io.Gep.ready   := ~GepValid
  when(io.Gep.fire()) {
    GepOperand := io.Gep.bits
    GepValid   := io.Gep.valid
  }

  // Because of this statement. predValid vec has to be size of atleast 1 and a true has to be wired to it
  // even if no predecessors exist.
//  val predValidInt = predValid.asUInt
  val inputValid   = GepValid & predValid.reduce(_&_)
  io.MemReq.valid := ReqValid
 
  val s_init :: s_SENDING :: s_RECEIVING  :: s_Done :: Nil = Enum(4)
  val state = RegInit(s_init)
  
  val type_word = MT_W


  // Now I need a state machine to drive this. This would make it a lot easier when sending multiple requests.
  when(inputValid && state === s_init) 
  {
    printf("Inputs are Ready %d", GepOperand)

    bitmask        := ReadBitMask(type_word,GepOperand,xlen)
    // Two masks needed for keeping track of what is sent and received. 
    // Could also use count. Going from mask to word count is difficult
    // We simply use shifts in the mask to indicate if we have requested all words required
    sendbytemask   := ReadByteMask(type_word,GepOperand,xlen)
  
    state      := s_SENDING
    // Set the state to send
    // Generate send mask
    // Generate the based load addresses

  }.elsewhen((state === s_SENDING) && (sendbytemask =/= 0.asUInt(16.W))) {
     printf("Requesting data %x", sendbytemask)
     ReqValid := 1.U          
    // io.MemReq.ready means arbitration succeeded and memory op has been passed on
    when(io.MemReq.ready === true.B && ReqValid === 1.U) {
      // Next word to be fetched
      ReqAddress := ReqAddress + 1.U
      // Shift right by word length on machine. 
      sendbytemask := sendbytemask >> (xlen/8) 
      // Disable request
      ReqValid := 0.U
      // Move to receiving data
      state := s_RECEIVING
    }
  
  }.elsewhen ((state === s_RECEIVING) && (io.MemResp.valid === true.B)) {
   // Received data; concatenate into linebuffer 
   linebuffer(ptr) := io.MemResp.bits.data
   // Increment ptr to next entry in linebuffer (for next read)
   ptr := ptr + 1.U
   // Check if more data needs to be sent 
   val y = (sendbytemask === 0.asUInt(16.W))
   state := Mux(y,s_Done,s_SENDING)

  }.elsewhen (state === s_Done) {

   // AND with bitmask, shift and output data.
   // Set valid to true.
   // 
   state := s_init
   val z = linebuffer.asUInt
   printf("linebuffer %x", (z & bitmask) >> Cat(GepOperand(1,0),0.U(3.W)))

 }

 // val MuxEnable = RegInit(false.B)
 // MuxEnable := true.B
 val Tree = Module(new ArbiterTree(BaseSize = 2, NumOps = 8, UInt(32.W), Locks = 1))
 // val MuxTree = Module(new DeMuxTree(BaseSize = 2, NumOps = 16, new ReadResp()))
 // MuxTree.io.enable := MuxEnable
 // when (Tree.io.out.fire())
 // {
 //  MuxEnable := true.B  //MuxEnable
 //  }
 // MuxTree.io.input.data := 500.U+Tree.io.out.bits
 // MuxTree.io.input.RouteID := Tree.io.out.bits

 Tree.io.in(0).bits := 0.U
 Tree.io.in(0).valid := (counter === 1.U)
 Tree.io.in(1).bits := 1.U
 Tree.io.in(1).valid := true.B
 Tree.io.in(2).bits := 2.U
 Tree.io.in(2).valid := true.B
 Tree.io.in(3).bits := 3.U
 Tree.io.in(3).valid := true.B
 Tree.io.out.ready := (state =/= s_init)
 printf(p"Tree Out: ${Tree.io.out} \n")
 // printf(p"\n MuxTree Out: ${MuxTree.io.outputs} \n")

  // val y = PriorityEncoder(0x4.U.toBools)
  //  printf("Priority: %x",y)


  // printf(p"State: $state, ${io.MemResp.valid}")
}



  // }.elsewhen (state === s_RECEIVING && sendbytemask === 0.U) {
  //   // No more data left to receive. Waiting for final word.

  //   // when there is incoming valid. change state to done.
  //   // next state = done.
   // }
   // .elsewhen (state === s_Done) {

  // // when done and io.out.ready
  // // send data.
  // }

  // Set the validity of the output signal. 
  // Connect Mem Resp to Output

  //-----------------------------------
  // Once data_valid_reg is true -> set MEMIO->OUT->VALID and DATA
  // Once MEMIO->IN->sends ack (i.e READY == TRUE)
  // When the ack is received && all Inputs from other memory ops are true set memOpAck := true
  // For the time being just send output when data_valid is true
  //-----------------------------------

