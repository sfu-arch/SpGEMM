package accel

import chisel3._
import chisel3.util._

import junctions._
import config._

class NastiMasterReq(implicit p: Parameters) extends CoreBundle()(p) {
  val addr = UInt(xlen.W)
  val data = UInt(xlen.W)
  val read = Bool()
  val mask = UInt((xlen/8).W)
  val tag  = UInt(4.W)
}

object NastiMasterReq {
  def apply(addr : UInt = 0.U, data: UInt = 0.U, read: UInt = 0.U,
            mask: UInt = 0.U, tag: UInt = 0.U)(implicit p: Parameters): NastiMasterReq =
  {
    val w = Wire(new NastiMasterReq)
    w.addr := addr
    w.data := data
    w.read := read
    w.mask := mask
    w.tag  := tag
    w
  }
}

class NastiMasterResp(implicit p: Parameters) extends CoreBundle()(p) {
  val data = UInt(xlen.W)
  val tag  = UInt(4.W)
}

object NastiMasterResp {
  def apply(data: UInt = 0.U, tag: UInt = 0.U)(implicit p: Parameters): NastiMasterResp =
  {
    val w = Wire(new NastiMasterResp)
    w.data := data
    w.tag  := tag
    w
  }
}

abstract class NastiMasterIO()(implicit val p: Parameters) extends Module with CoreParams
{
  val io = IO(
    new Bundle {
      val req  = Flipped(Decoupled(new NastiMasterReq()))
      val resp = Valid(new NastiMasterResp())
      val nasti = new NastiIO()
    }
  )
}


class NastiMaster()(implicit p: Parameters) extends NastiMasterIO()(p) {

  // Address channel state machines
  val sAIdle :: sARead :: sAWrite :: sAWait :: Nil = Enum(4);
  val addrState = RegInit(sAIdle); // Address channel state

  // Other channel state machines
  val sIdle :: sWait :: Nil = Enum(2);
  val rState = RegInit(sIdle);    // Read data channel state
  val wState = RegInit(sIdle);    // Write data channel state
  val bState = RegInit(sIdle);    // Write response channel state

  // Generate Registers for Nasti bus outputs
  val ar_r = RegInit(NastiReadAddressChannel(0.U, 0.U, 0.U))
  val ar_valid_r = RegInit(false.B)
  val aw_r = RegInit(NastiWriteAddressChannel(0.U, 0.U, 0.U))
  val aw_valid_r = RegInit(false.B)
  val w_r  = RegInit(NastiWriteDataChannel(0.U))
  val w_valid_r = RegInit(false.B)
  val req_ready_r = RegInit(false.B)

  //-------------------------------------------------------------------
  // Address channel state machine
  //-------------------------------------------------------------------
  switch(addrState) {
    is(sAIdle) {
      io.req.ready := true.B
      when(io.req.valid) {
        when(io.req.bits.read) {
          ar_valid_r := true.B
          ar_r.addr := io.req.bits.addr
          ar_r.id := io.req.bits.tag
          ar_r.len := 0.U
          ar_r.size := 2.U
        } .otherwise {
          aw_valid_r := true.B
          aw_r.addr := io.req.bits.addr
          aw_r.id := io.req.bits.tag
          aw_r.len := 0.U
          aw_r.size := 2.U
        }
        when(!io.nasti.ar.ready && !io.nasti.aw.ready) {
          addrState   := sAWait
          req_ready_r := false.B
        }
      } .otherwise {
        ar_valid_r := false.B
        aw_valid_r := false.B
      }
    }
    is(sAWait) {
      when(io.nasti.ar.ready || io.nasti.aw.ready) {
        addrState := sAIdle
        req_ready_r := true.B
        ar_valid_r := false.B
        aw_valid_r := false.B
      }
    }
  }
  // Connect address channel I/O
  io.nasti.ar.bits := ar_r
  io.nasti.ar.valid := ar_valid_r
  io.nasti.aw.bits := aw_r
  io.nasti.aw.valid := aw_valid_r
  io.req.ready := req_ready_r

  //-------------------------------------------------------------------
  // Write data channel state machine
  //-------------------------------------------------------------------
  switch(wState) {
    is(sIdle) {
      when(io.req.valid) {
        when(!io.req.bits.read) {
          w_valid_r := true.B
          w_r.data := io.req.bits.data
          w_r.id := io.req.bits.tag
          w_r.strb := io.req.bits.mask
          w_r.last := true.B
          when(!io.nasti.w.ready) {
            wState := sWait
          }
        }
      }.otherwise {
        w_valid_r := false.B
      }
    }
    is(sWait) {
      when(io.nasti.w.ready) {
        wState := sIdle
        w_valid_r := false.B
      }
    }
  }
  io.nasti.w.valid := w_valid_r
  io.nasti.w.bits := w_r

  //-------------------------------------------------------------------
  // Read data channel
  //-------------------------------------------------------------------
  val resp_valid_r = RegInit(false.B)
  val resp_r = RegInit(NastiMasterResp(0.U,0.U))

  when(io.nasti.r.valid) {
      resp_valid_r := true.B
      resp_r.data := io.nasti.r.bits.data
      resp_r.tag := io.nasti.r.bits.id
      assert(io.nasti.r.bits.resp === NastiConstants.RESP_OKAY, "Read response bits don't contain OKAY code.")
  }.otherwise {
    resp_valid_r := false.B
  }
  io.resp.valid := resp_valid_r
  io.resp.bits := resp_r
  io.nasti.r.ready := true.B

  //-------------------------------------------------------------------
  // Write response channel
  //-------------------------------------------------------------------
  when(io.nasti.b.valid) {
    assert(io.nasti.b.bits.resp === NastiConstants.RESP_OKAY, "Write response bits don't contain OKAY code.")
  }
  io.nasti.b.ready := true.B
}

