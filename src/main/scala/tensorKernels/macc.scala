package tensorKernels

import Chisel._
import chisel3.util.HasBlackBoxResource
import chisel3.{BlackBox, Reset}

class macc(SIZEIN: Int = 16, SIZEOUT: Int = 40) extends BlackBox(Map("SIZEIN" -> SIZEIN, "SIZEOUT" -> SIZEOUT)) with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val ce = Input(Bool ())
    val sload = Input(Bool ())
    val a = Input(UInt(SIZEIN.W))
    val b = Input(UInt(SIZEIN.W))
    val accum_out = Input(UInt(SIZEOUT.W))
  })

  //  setResource("/home/reza/git/tensorstrainers/src/main/resources/verilog/macc.v")
  setResource("/verilog/macc.v")
}


