package tensorKernels

import Chisel._
import chisel3.{BlackBox, Reset, Clock}
import chisel3.util.HasBlackBoxResource

class UltraRAM(DWIDTH: Int = 72, AWIDTH: Int = 12, NBPIPE: Int = 1) extends BlackBox(Map("DWIDTH" -> DWIDTH, "AWIDTH" -> AWIDTH, "NBPIPE" -> NBPIPE)) with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rst = Input(Reset())
    val we = Input(Bool ())
    val regce = Input(Bool ())
    val mem_en = Input(Bool ())
    val din = Input(UInt(DWIDTH.W))
    val raddr = Input(UInt(AWIDTH.W))
    val waddr = Input(UInt(AWIDTH.W))
    val dout = Output(UInt(DWIDTH.W))
  })
  setResource("/verilog/UltraRAM.v")
  require(DWIDTH <= 72, "In URAM, data width should be equal or less than 72bits")
  require(AWIDTH <= 12, "In URAM, address width should be equal or less than 12bits")

}


