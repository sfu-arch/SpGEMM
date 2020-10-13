// See LICENSE for license details.

package accel

import chisel3._
import chisel3.util._
import junctions._
import config._
import interfaces._
import memory._
import utility._

class InterleavedStackIO(NumPorts:Int)(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
      val MemReq   = Vec(NumPorts, Flipped(Decoupled(new MemReq)))
      val MemResp  = Vec(NumPorts, Output(Valid(new MemResp)))
  })
}

class InterleavedStack(val size : Int, ramIdx : List[Int], NumPorts:Int)(implicit p: Parameters)
  extends InterleavedStackIO(NumPorts)(p) {

  // The number of RAMs to interleave
  val numBits = ramIdx(0)-ramIdx(1)+1
  val numRAMs = 1<<(numBits)

  // Instantiate the individual Stack RAMs
  val mems = for (i <- 0 until numRAMs) yield {
    val mem = Module(new StackMem(size/numRAMs))
    mem
  }

  // Instantiate an arbiter to sit in front of each RAM
  val arbs = for (i <- 0 until numRAMs) yield {
    val arb = Module(new MemArbiter(NumPorts))
    arb
  }

  // Create one-hot bit masks for each request input to indicate the target RAM for the request
  val mask = Seq.fill(NumPorts)(WireInit(0.U(numRAMs.W)))
  for (i <- 0 until NumPorts) {
    mask(i) := UIntToOH(io.MemReq(i).bits.addr(ramIdx(0),ramIdx(1)))
  }

  /*
  val sel = Seq.fill(NumPorts)(RegInit(0.U(numBits.W)))
  for (i <- 0 until NumPorts) {
    when(io.MemReq(i).fire()) {
      sel(i) := io.MemReq(i).bits.addr(ramIdx(0), ramIdx(1))
    }
  }
  */
  val sel = Seq.fill(numRAMs)(RegInit(0.U(log2Ceil(NumPorts).W)))
  for (mem <- 0 until numRAMs) {
    when(arbs(mem).io.cache.MemReq.fire()) {
      sel(mem) := arbs(mem).io.cache.chosen
    }
  }

  // Connect up the Requests to the Arbiters
  for (input <- 0 until NumPorts) {
    io.MemReq(input).ready := false.B
    for (mem <- 0 until numRAMs) {
      arbs(mem).io.cpu.MemReq(input).valid := io.MemReq(input).valid && mask(input)(mem)
      arbs(mem).io.cpu.MemReq(input).bits := io.MemReq(input).bits
      when (arbs(mem).io.cpu.MemReq(input).ready && mask(input)(mem)) {
        io.MemReq(input).ready := true.B
      }
    }
  }

  // Create output muxes from arbiters
  val outmux = for (output <- 0 until NumPorts) yield {
    val mux = Module(new Arbiter(new MemResp,numRAMs))
    mux
  }

  // Use the output muxes to combine all the RAM responses for each
  // port.
  for (output <- 0 until NumPorts) {
    for(mem <- 0 until numRAMs) {
      outmux(output).io.in(mem).valid := arbs(mem).io.cpu.MemResp(output).valid
      outmux(output).io.in(mem).bits := arbs(mem).io.cpu.MemResp(output).bits
    }
    io.MemResp(output).valid := outmux(output).io.out.valid
    io.MemResp(output).bits := outmux(output).io.out.bits
    outmux(output).io.out.ready := true.B
  }

  // Connect Arbiters to RAMs
  for (mem <- 0 until numRAMs) {
    mems(mem).io.req <> arbs(mem).io.cache.MemReq
    // over-ride the address signal removing the two bits used to select the RAMs
    mems(mem).io.req.bits.addr := Cat(arbs(mem).io.cache.MemReq.bits.addr(31,ramIdx(0)+1),
      arbs(mem).io.cache.MemReq.bits.addr(ramIdx(1)-1,0))
    arbs(mem).io.cache.MemResp <> mems(mem).io.resp
  }

}
