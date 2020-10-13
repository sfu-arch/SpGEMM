package accel.coredf

import chisel3._
import chisel3.util._
import utility.UniformPrintfs
import interfaces._
import util._
import config._
import accel._
import dataflow._
import dataflow.tests._
import node.HandShaking

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


class NewCore(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreT(cNum,sNum)(p) {

  val (s_idle :: s_busy :: s_done :: Nil) = Enum(3)
  val state = RegInit(init = s_idle)
  //  val err_latch = Reg(Bool())

  val add_result_reg = Reg(Decoupled(new DataBundle))
  val start_reg = RegInit(false.B)

  //val addDF = Module(new Add01DF())

  val test03DF = Module(new test03DF())

  test03DF.io.MemResp <> DontCare
  test03DF.io.MemReq <> DontCare

  override val printfSigil = "CoreDF:  add_result_reg: " + add_result_reg.asUInt() + " state: " + state + " "

  //IO Connections
  io.ctrl(0).ready := true.B
  io.ctrl(1).ready := true.B
  io.ctrl(2).ready := true.B

  test03DF.io.in.bits.enable.control := start_reg
  test03DF.io.in.bits.enable.taskID := 0.U


  test03DF.io.in.bits.data("field0").data <> io.ctrl(0).bits.data(xlen-1,0)
  test03DF.io.in.bits.data("field0").predicate := true.B
  test03DF.io.in.bits.data("field0").taskID := 0.U

  test03DF.io.in.bits.data("field1").data <> io.ctrl(1).bits.data(xlen-1,0)
  test03DF.io.in.bits.data("field1").predicate := true.B
  test03DF.io.in.bits.data("field1").taskID := 0.U

// //   addDF.io.Data0.bits.valid     := true.B

//  addDF.io.Data1.bits.data      := io.ctrl(1).bits.data(xlen-1,0)
//  addDF.io.Data1.bits.predicate := true.B
// //   addDF.io.Data1.bits.valid     := true.B
  //result is Decoupled


  io.stat(0).bits.data := 0x55AA0002.U
// //   io.stat(0).bits.valid := true.B
  io.stat(0).valid := true.B
  io.stat(0).bits.predicate := true.B
  io.stat(0).bits.taskID := 0.U

  io.stat(1) <> add_result_reg
  io.stat(1).bits.predicate := true.B
  io.stat(1).bits.taskID := 0.U

  io.stat(2).bits.data := 0.U
  io.stat(2).valid := true.B
  io.stat(2).bits.predicate := true.B
  io.stat(2).bits.taskID := 0.U

  //Switch OFF CacheIO
  io.cache.req.valid := false.B
  io.cache.req.bits.tag := 0.U
  io.cache.req.bits.addr := 0.U
  io.cache.req.bits.iswrite := false.B
  io.cache.req.bits.mask := 0.U
  io.cache.req.bits.data := 0.U
  io.cache.req.bits.tile := 0.U
  io.cache.req.bits.taskID := 0.U
  io.cache.abort := false.B


  switch (state) {
    // Idle
    is(s_idle) {

      when(io.start) {
        start_reg := true.B
        state := s_busy
      }
    }

    is(s_busy) {

      when(test03DF.io.out.valid) {
        state := s_done
        add_result_reg.bits.data := test03DF.io.out.bits.data("field0").data
        add_result_reg.valid := test03DF.io.out.valid
        test03DF.io.out.ready := add_result_reg.ready
      }

    }


    // Done
    is (s_done) {

      start_reg := false.B
      when(io.init) {
        add_result_reg.bits.data := 0.U
        state := s_idle
      }
    }
  }

  // Reflect state machine status to processor
  io.done  := (state === s_done)
  io.ready := (state === s_idle)
//  io.stat  := Cat(err_latch,state.asUInt())

  // Intermediate
  //  addDF.io.result.ready  := (state === s_busy)
  test03DF.io.out.ready := true.B

  test03DF.io.in.valid := (state === s_busy || (state === s_idle && io.start))

  printf(p"-----------------------------------------------------\n")
  printf(p"add_result_reg: ${add_result_reg} ")
  printf(p"io.result.bits.data: ${test03DF.io.out.bits.data("field0").data} ")
  printf(p"io.result.valid: ${test03DF.io.out.valid} ")
  printf(p"io.result.ready: ${test03DF.io.out.ready} \n")

  printfInfo(" State: %x\n", state)

}
