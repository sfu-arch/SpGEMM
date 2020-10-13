package dnn.modules

import chisel3.{Module, _}
import chisel3.util._
import config._
import dnn.TwoOperand_PE
import dnn.types.TwoOperand
import utility.UniformPrintfs


class NCycle_CooSCAL[T <: Data : TwoOperand.OperatorTwoOperand](val gen: T, val N: Int, val lanes: Int, val opcode: String)(implicit val p: Parameters)
  extends Module with config.CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val input_vec = Input(Vec(N, UInt(xlen.W)))
    val scalar    = Input(UInt(xlen.W))
    val output    = Output(Vec(lanes, UInt(xlen.W)))
  })

  require(gen.getWidth == xlen, "Size of element does not match xlen OR Size of vector does not match shape")
  require(N % lanes == 0, "Size of vector should be multiple of lanes")

  def latency(): Int = {
    N / lanes + 1
  }

  val PEs =
    for (i <- 0 until lanes) yield {
      Module(new TwoOperand_PE(gen, opcode))
    }

  for (i <- 0 until lanes) {
    PEs(i).io.left.bits := io.input_vec(i)
    PEs(i).io.right.bits := io.scalar
    PEs(i).io.left.valid := true.B //io.input_vec(i).valid
    PEs(i).io.right.valid := true.B //io.scalar.valid
  }

  for (i <- 0 until lanes) {
    io.output(i) <> PEs(i).io.out.bits
    PEs(i).reset := false.B
  }
}