package accel.coredf

/**
  * Created by nvedula on 28/6/17.
  */

import accel._
import chisel3._
import config._
import dataflow.filter._

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


class VecFilterDFCore(cNum : Int, sNum: Int)(implicit p: Parameters) extends CoreT(cNum,sNum)(p) {

  val FilterSize = 3

  val Loader = Module(new CacheVecLoader(6)(p))
  val Filt = Module(new VecFilter()(p))
  val done = RegInit(init=false.B)
  
  Loader.io.enable.bits.control := true.B
  Loader.io.enable.bits.taskID := 0.U
  Loader.io.enable.valid := true.B
  Filt.io.enable.bits.control := true.B
  Filt.io.enable.bits.taskID := 0.U
  Filt.io.enable.valid := true.B

  Loader.io.ptr <> io.ctrl

  for (i <- 0 until 3) {
    Filt.io.data(i) <> Loader.io.data(i)
    Filt.io.kern(i) <> Loader.io.data(i+3)
  }

  Loader.io.sum <> Filt.io.sum

  io.stat(0).bits.data := 0x55AA0003.U
  io.stat(0).valid := true.B

//  io.stat(1).bits.data := Filt.io.sum.bits.data(31,0)
//  io.stat(1).valid := Filt.io.sum.valid
//  io.stat(2).bits.data <> Filt.io.sum.bits.data(63,32)
//  io.stat(2).valid := Filt.io.sum.valid
//  io.stat(3).bits.data <> Filt.io.sum.bits.data(95,64)
//  io.stat(3).valid := Filt.io.sum.valid

  Loader.io.cache <> io.cache
  when (io.init) {
    done := false.B
  } .otherwise {
    when (Filt.io.sum.valid) {
      done := Filt.io.sum.valid
    }
  }
  io.done := done
  io.ready := true.B
}
