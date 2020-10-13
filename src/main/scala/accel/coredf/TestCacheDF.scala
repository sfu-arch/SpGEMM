package accel.coredf

/**
  * Created by nvedula on 28/6/17.
  */

import accel._
import chisel3._
import chisel3.util._
import config._
import dataflow._

/**
  * The Core class creates contains the dataflow logic for the accelerator.
  * This particular core file implements a simple memory test routine to
  * validate the register interface and the Nasti bus operation on an SoC FPGA.
  *
  * @param p Project parameters. Only xlen is used to specify register and
  *          data bus width.
  *
  * @note io.ctrl  A control register (from SimpleReg block) to start test
  * @note io.addr  A control register containing the physical address for
  *                the test
  * @note io.len   A control register containing the length of the memory
  *                test (number of words)
  * @note io.stat  A status register containing the current state of the test
  * @note io.cache A Read/Write request interface to a memory cache block
  */


class TestCacheDF(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreT(cNum,sNum)(p) {


  val (s_idle :: s_busy :: s_done :: Nil) = Enum(3)
  val state = RegInit(init = s_idle)
  //  val err_latch = Reg(Bool())
  val add_result_reg = Reg(UInt(xlen.W))
  val start_reg = RegInit(false.B)

  val MemDF = Module(new TestCacheDataFlow())
  MemDF.io.start := io.start
  override val printfSigil = "TestMemDF:  add_result_reg: " + add_result_reg.asUInt() + " state: " + state + " "

  //IO Connections
  //result is Decoupled
  io.stat(0) <> add_result_reg
  io.stat(0).valid := true.B

  io.cache.req <> MemDF.io.MemReq
  MemDF.io.MemResp <> io.cache.resp

  switch (state) {
    // Idle
    is(s_idle) {

      when(io.start) {
        start_reg := true.B
        state := s_busy
      }
    }

    is(s_busy) {

    when(MemDF.io.result.valid) {
        state := s_done
        add_result_reg := MemDF.io.result.bits.data(xlen-1,0)
      }

    }

    // Done
    is (s_done) {

      start_reg := false.B
      when(io.init) {
        add_result_reg := 0.U
        state := s_idle
      }
    }
  }

  // Reflect state machine status to processor
  io.done  := (state === s_done)
  io.ready := (state === s_idle)
  // Intermediate
  MemDF.io.result.ready  := (state === s_busy)


}
