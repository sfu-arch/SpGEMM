package accel

import chisel3._
import chisel3.util._
import utility.UniformPrintfs
import config._
import dataflow._
import interfaces._

/**
  * The Core class contains the dataflow logic for the accelerator.
  * This particular core file implements a simple memory test routine to
  * validate the register interface and the Nasti bus operation on an SoC FPGA.
  *
  * @param p Project parameters. Only xlen is used to specify register and
  *          data bus width.
  * @note io.ctrl  A control register (from SimpleReg block) to start test
  * @note io.addr  A control register containing the physical address for
  *       the test
  * @note io.len   A control register containing the length of the memory
  *       test (number of words)
  * @note io.stat  A status register containing the current state of the test
  * @note io.cache A Read/Write request interface to a memory cache block
  */

abstract class CoreDFIO(cNum : Int, sNum: Int)(implicit val p: Parameters) extends Module with CoreParams with UniformPrintfs
{
  val io = IO(
    new Bundle {
      val start  = Input(Bool())
      val init   = Input(Bool())
      val ready  = Output(Bool())
      val done   = Output(Bool())
      val ctrl   = Vec(cNum,Flipped(Decoupled(new DataBundle())))
      val stat   = Vec(sNum,Decoupled(new DataBundle()))
//      val err   = Output(UInt(xlen.W)) TODO : Need to have a err signal with error codes
      val cache  = Flipped(new CacheIO)
    }
  )
}


abstract class CoreT(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreDFIO(cNum,sNum)(p) {
}

class Core(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreT(cNum,sNum)(p) {

  val dataBytes = xlen / 8
  val reqAddr = Reg(UInt(32.W))
  val reqTag = Reg(io.cache.req.bits.tag.cloneType)
  val wordCount = Reg(UInt(32.W))

  val writeData = wordCount
  val expectedData = wordCount

  val (sIdle :: sWriteReq :: sWriteResp :: sReadReq :: sReadResp :: sDone :: Nil) = Enum(6)
  val state = RegInit(init = sIdle)
  val stall = !io.cache.resp.valid
  val errorLatch = Reg(Bool())

  io.ctrl(0).ready := true.B
  io.ctrl(1).ready := true.B
  io.ctrl(2).ready := true.B

  switch(state) {
   // Idle
    is(sIdle) {
      reqAddr := io.ctrl(1).bits.data(31, 0)
      wordCount := 0.U
      when(io.start) {
        when(io.ctrl(0).bits.data(0) === true.B) {
          state := sReadReq
          reqTag := 1.U
        }.otherwise {
          state := sWriteReq
        }
      }
    }
    // Write
    is(sWriteReq) {
      when(io.cache.req.ready) {
        state := sWriteResp
      }
    }
    is(sWriteResp) {
      when(!stall) {
        wordCount := wordCount + 1.U
        reqAddr := reqAddr + dataBytes.U
        when(wordCount < io.ctrl(2).bits.data) {
          state := sWriteReq
        }.otherwise {
          state := sDone
        }
      }
    }
    // Read
    is(sReadReq) {
      when(io.cache.req.ready) {
        state := sReadResp
      }
    }
    is(sReadResp) {
      when(!stall) {
        wordCount := wordCount + 1.U
        reqAddr := reqAddr + dataBytes.U
        when(wordCount < io.ctrl(2).bits.data) {
          state := sReadReq
        }.otherwise {
          state := sDone
        }
      }
    }
    // Done
    is(sDone) {
      when(io.init) {
        state := sIdle
      }
    }
  }

  io.cache.req.valid := (state === sReadReq || state === sWriteReq ||
    state === sReadResp || state === sWriteResp)
  io.cache.req.bits.addr := reqAddr
  io.cache.req.bits.data := writeData
  io.cache.req.bits.tag := reqTag
  io.cache.req.bits.iswrite := (state === sWriteReq || state === sWriteResp)

  when(state === sWriteReq || state === sWriteResp) {
    io.cache.req.bits.mask := ~0.U(dataBytes.W)
  }.otherwise {
    io.cache.req.bits.mask := 0.U(dataBytes.W)
  }

  when(state===sReadResp && io.cache.resp.valid && io.cache.resp.bits.data =/= expectedData) {
    errorLatch := true.B
  }.elsewhen(io.init){
    errorLatch := false.B
  }

  // Reflect state machine status to processor
  io.done := (state === sDone)
  io.ready := (state === sIdle)

  // Connect a revision number to the first status register
  io.stat(0).bits.data := 0x55AA0001.U
// //  io.stat(0).bits.valid := true.B
  io.stat(0).valid := true.B
  io.stat(0).bits.predicate := true.B

  io.stat(1).bits.data := Cat(errorLatch, state.asUInt())
// //  io.stat(1).bits.valid := true.B
  io.stat(1).valid := true.B
  io.stat(1).bits.predicate := true.B

  io.stat(2).bits.data := Cat(errorLatch, state.asUInt())
// //  io.stat(2).bits.valid := true.B
  io.stat(2).valid := true.B
  io.stat(2).bits.predicate := true.B

  io.cache.abort := false.B

}
