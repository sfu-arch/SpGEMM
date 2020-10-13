package memory


import chisel3._
import chisel3.Module
import chisel3.util._
import chisel3.experimental._


// Config
import config._
import utility._
import interfaces._


class SinglePortDRAM(DATA: Int = 32, ADDR: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val wr = Input(Bool())
    val addr = Input(UInt(ADDR.W))
    val din = Input(UInt(DATA.W))
    val dout = Output(UInt(DATA.W))
  })

  setResource("/verilog/SinglePortDRAM.v")

}

class FastMem(DATA: Int = 32, ADDR: Int = 32) extends Module {
  val io = IO(new Bundle {
    val wr = Input(Bool())
    val addr = Input(UInt(ADDR.W))
    val din = Input(UInt(DATA.W))
    val dout = Output(UInt(DATA.W))
  })

  val memory = Module(new SinglePortDRAM())

  memory.io.clk := clock
  memory.io.wr <> io.wr
  memory.io.addr <> io.addr
  memory.io.din <> io.din
  io.dout <> memory.io.dout

}
