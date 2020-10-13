// See LICENSE for license details.

package accel

import chisel3._
import chisel3.util._
import accel.coredf._
import config._
import junctions._

abstract class AcceleratorIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(
    new Bundle { 
      val h2f  = Flipped(new NastiIO)
      val f2h  = new NastiIO
    }
  )
}

class Accelerator(cNum : Int, sNum : Int, coreDF: => CoreT) (implicit p: Parameters)extends AcceleratorIO()(p) {

  val regs  = Module(new DataBundleReg(cNum, sNum))
  val core  = Module(coreDF)
  val cache = Module(new Cache)

  // Connect HPC AXI Master interface the control/status register block
  // AXI Slave interface
  regs.io.nasti <> io.h2f

  // Connect the first three control registers and one of the status
  // registers to the core logic block
  core.io.init  <> regs.io.init
  core.io.start <> regs.io.start
  core.io.ctrl  <> regs.io.ctrl
  regs.io.stat  <> core.io.stat
  core.io.ready <> regs.io.ready
  core.io.done  <> regs.io.done

  // Connect the cache CPU interface to the core logic block
  core.io.cache <> cache.io.cpu
  io.f2h <> cache.io.nasti

}
