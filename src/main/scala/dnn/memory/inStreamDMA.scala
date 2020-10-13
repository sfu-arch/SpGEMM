
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import interfaces.CooDataBundle
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
class inStreamDMAIO(memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val baddr = Input(UInt(mp.addrBits.W))
    val len = Input(UInt(mp.addrBits.W))
    val vme_rd = new VMEReadMaster
    val out = Decoupled(UInt(p(XLEN).W))
  })
}

class inStreamDMA(bufSize: Int, memTensorType: String = "none")(implicit p: Parameters)
  extends inStreamDMAIO(memTensorType)(p) {

  val strLoad = Module(new StreamLoad(bufSize, memTensorType))


  io.done := strLoad.io.done

//  val popCnt = Counter(math.pow(2, p(XLEN)).toInt)
//  val width =  p(ShellKey).memParams.dataBits / p(XLEN)

  val tl_Inst = Wire(new MemDecode)
  val memTensorRows = Mux(io.len % tp.tensorWidth.U === 0.U,
                          io.len / tp.tensorWidth.U,
                          (io.len / tp.tensorWidth.U) + 1.U)

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

  strLoad.io.start := io.start
  strLoad.io.inst := tl_Inst.asTypeOf(UInt(INST_BITS.W))
  strLoad.io.baddr := io.baddr
  io.out <> strLoad.io.out
  io.vme_rd <> strLoad.io.vme_rd

//  when(io.out.fire()) {popCnt.inc()}

//  io.out.valid := strLoad.io.out.valid && (popCnt.value < io.len)


}
