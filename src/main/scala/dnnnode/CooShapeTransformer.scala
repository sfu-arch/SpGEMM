
package dnnnode

import chisel3._
import chisel3.util._
import config._
import dnn.memory.{TensorMaster, TensorParams}
import interfaces.{CooDataBundle}
import node.Shapes
import shell._


/** Coordinate Shape Transformer.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class CooShapeTransformerIO[gen <: Shapes](memTensorType: String = "none")(outShape: => gen)(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val ind = Flipped(Decoupled(UInt(p(ROWLEN).W)))
    val value = Flipped(Decoupled(UInt(p(XLEN).W)))
    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
  })
}

class CooShapeTransformer[L <: Shapes](rowBased: Boolean, memTensorType: String = "none")
                                      (outShape: => L)(implicit p: Parameters)
  extends CooShapeTransformerIO(memTensorType)(outShape)(p) {

  if (rowBased) {
    io.out.bits.data := io.value.bits
    io.out.bits.row := io.ind.bits
    io.out.bits.col := 0.U
    io.out.bits.valid := true.B
  } else {
    io.out.bits.data := io.value.bits
    io.out.bits.col := io.ind.bits
    io.out.bits.row := 0.U
    io.out.bits.valid := true.B
  }

  io.out.valid := io.ind.valid && io.value.valid

  io.ind.ready := io.out.ready && io.value.valid && io.ind.valid
  io.value.ready := io.out.ready && io.value.valid && io.ind.valid


}
