
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import dnnnode.WeightShapeTransformer
import node.{Shapes, vecN}
import shell._
//import vta.util.config._
import dnn.memory.ISA._


/** TensorLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class inDMA_wgtIO[gen <: Shapes](wgtTensorType: String = "none")(wgtShape: => gen)(implicit val p: Parameters)
  extends Module {
  val tpWgt = new TensorParams(wgtTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val numWeight = Input(UInt(mp.addrBits.W))
    val baddr = Input(UInt(mp.addrBits.W))
    val vme_rd = new VMEReadMaster
    val tensor = new TensorClient(wgtTensorType)
  })
}


class inDMA_wgt[L <: Shapes](wgtTFDepth: Int, bufSize: Int, intWgtTensorType: String = "none", extWgtTensorType: String = "none")(wgtShape: => L)
                            (implicit p: Parameters)
  extends inDMA_wgtIO(intWgtTensorType)(wgtShape)(p) {

  val tpMem = new TensorParams(extWgtTensorType)
  val wgtTransformer = Module(new WeightShapeTransformer(wgtTFDepth, bufSize, intWgtTensorType, extWgtTensorType)(wgtShape))
  val tensorLoad = Module(new TensorLoad(extWgtTensorType))

  val tl_Inst = Wire(new MemDecode)
  val memTensorRows = Mux(io.numWeight * wgtShape.getLength().U % tpMem.tensorWidth.U === 0.U,
    io.numWeight * wgtShape.getLength().U / tpMem.tensorWidth.U, (io.numWeight * wgtShape.getLength().U / tpMem.tensorWidth.U) + 1.U)



  tensorLoad.io.start := io.start
  tensorLoad.io.inst := tl_Inst.asTypeOf(UInt(INST_BITS.W))
  tensorLoad.io.baddr := io.baddr
  io.vme_rd <> tensorLoad.io.vme_rd

  wgtTransformer.io.start := tensorLoad.io.done
  wgtTransformer.io.numWeight := io.numWeight
  io.done := wgtTransformer.io.done

  tensorLoad.io.tensor <> wgtTransformer.io.tensorMaster
  wgtTransformer.io.tensor <> io.tensor

  tl_Inst.xpad_0 := 0.U
  tl_Inst.xpad_1 := 0.U
  tl_Inst.ypad_0 := 0.U
  tl_Inst.ypad_1 := 0.U
  tl_Inst.xstride := memTensorRows
  tl_Inst.xsize := memTensorRows
  tl_Inst.ysize := 1.U
  tl_Inst.empty_0 := 0.U
  tl_Inst.dram_offset := 0.U
  tl_Inst.sram_offset := 0.U
  tl_Inst.id := 3.U
  tl_Inst.push_next := 0.U
  tl_Inst.push_prev := 0.U
  tl_Inst.pop_next := 0.U
  tl_Inst.pop_prev := 0.U
  tl_Inst.op := 0.U

}
