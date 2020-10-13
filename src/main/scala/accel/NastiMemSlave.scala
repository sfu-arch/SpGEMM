package accel

import chisel3._
import chisel3.core.Data
import chisel3.util._
import junctions._
import config.{CoreBundle, _}

class InitParams(implicit p: Parameters) extends CoreBundle( )(p) with CacheParams {
  val addr = UInt(nastiXAddrBits.W)
  val data = UInt(nastiXDataBits.W)
}

class NastiMemSlaveIO(implicit p: Parameters) extends CoreBundle( )(p) with CacheParams {
  val init  = Flipped(Valid(new InitParams( )(p)))
  val nasti = Flipped(new NastiIO)
}

class NastiMemSlave(val depth: Int = 1 << 24, latency: Int = 20)(implicit val p: Parameters) extends Module with CacheParams {

  val io = IO(new NastiMemSlaveIO( )(p))

  /* Memory model interface */
  val dutMem = Wire(new NastiIO)

  // Connect DUT Cache I/O to a queue for the memory model logic
  dutMem.ar <> Queue(io.nasti.ar, 32)
  dutMem.aw <> Queue(io.nasti.aw, 32)
  dutMem.w <> Queue(io.nasti.w, 32)
  io.nasti.b <> Queue(dutMem.b, 32)
  io.nasti.r <> Queue(dutMem.r, 32)

  val size = log2Ceil(nastiXDataBits / 8).U
  val len  = (dataBeats - 1).U

  /* Main Memory */
  val mem                                                               = Mem(depth, UInt(nastiXDataBits.W))
  val sMemIdle :: sMemWrite :: sMemWrAck :: sMemRead :: sMemWait :: Nil = Enum(5)
  val memState                                                          = RegInit(sMemIdle)
  val (wCnt, wDone)                                                     = Counter(memState === sMemWrite && dutMem.w.valid, dataBeats)
  val (rCnt, rDone)                                                     = Counter(memState === sMemRead && dutMem.r.ready, dataBeats)
  //val (waitCnt, waitDone) = Counter(memState === sMemWait, latency)
  val waitCnt                                                           = RegInit(0.U(16.W))


  when(io.init.valid) {
    mem.write((io.init.bits.addr >> 2).asUInt( ), io.init.bits.data)
  }

  dutMem.ar.ready := false.B
  dutMem.aw.ready := false.B
  dutMem.w.ready := false.B
  dutMem.b.valid := memState === sMemWrAck
  dutMem.b.bits := NastiWriteResponseChannel(dutMem.aw.bits.id)
  dutMem.r.valid := memState === sMemRead
  val rdAddr = (dutMem.ar.bits.addr >> size).asUInt( ) + rCnt.asUInt( )
  dutMem.r.bits := NastiReadDataChannel(dutMem.ar.bits.id, mem.read(rdAddr))

  switch(memState) {
    is(sMemIdle) {
      when(dutMem.aw.valid) {
        memState := sMemWrite
      }.elsewhen(dutMem.ar.valid) {
        memState := sMemWait
      }
    }
    is(sMemWrite) {
      assert(dutMem.aw.bits.size === size)
      assert(dutMem.aw.bits.len === len)
      when(dutMem.w.valid) {
        val wrAddr = (dutMem.aw.bits.addr >> size).asUInt( ) + wCnt.asUInt( )
        mem.write(wrAddr, dutMem.w.bits.data)
        if(log){printf("[write] mem[%x] <= %x\n", wrAddr, dutMem.w.bits.data)}
        dutMem.w.ready := true.B
      }
      when(wDone) {
        memState := sMemWrAck
      }
    }
    is(sMemWrAck) {
      when(dutMem.b.ready) {
        dutMem.aw.ready := true.B
        memState := sMemIdle
      }
    }
    is(sMemWait) {
      waitCnt := waitCnt + 1.U;
      when(waitCnt === (latency - 1).U) {
        memState := sMemRead
      }
    }
    is(sMemRead) {
      waitCnt := 0.U;
      when(dutMem.r.ready) {
        if(log){printf("[read] mem[%x] => %x\n", (dutMem.ar.bits.addr >> size) + rCnt, dutMem.r.bits.data)}
      }
      when(rDone) {
        dutMem.ar.ready := true.B
        memState := sMemIdle
      }
    }
  }
}

