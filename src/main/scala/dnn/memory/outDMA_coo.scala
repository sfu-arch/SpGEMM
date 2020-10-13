
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import dnn.memory.ISA._
import dnnnode.StoreQueue
import interfaces.{CooDataBundle, CustomDataBundle}
import shell._


/** outDMA_act
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class outDMA_cooIO(memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val done = Output(Bool())
    val baddr_row = Input(UInt(mp.addrBits.W))
    val baddr_col = Input(UInt(mp.addrBits.W))
    val baddr_val = Input(UInt(mp.addrBits.W))
    val vme_wr_row = new VMEWriteMaster
    val vme_wr_col = new VMEWriteMaster
    val vme_wr_val = new VMEWriteMaster
    val in = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val last = Input(Bool())
    val outLen = Output(UInt(mp.addrBits.W))
  })
}

class outDMA_coo(bufSize: Int, memTensorType: String = "none")(implicit p: Parameters)
  extends outDMA_cooIO(memTensorType)(p) {
  require(bufSize > tp.tensorWidth, "buffer size should be greater than the tensorFile width")

  val tensorStore_row = Module(new TensorStore(memTensorType))
  val tensorStore_col = Module(new TensorStore(memTensorType))
  val tensorStore_val = Module(new TensorStore(memTensorType))

  val storeQueue = Module(new StoreQueue(new CooDataBundle(UInt(p(XLEN).W)), bufSize, tp.tensorWidth))

  val popCnt = Counter(math.pow(2, p(XLEN)).toInt)
  val pushCnt = Counter(math.pow(2, p(XLEN)).toInt)
  val length = RegInit(init = 0.U)
  val sendingState = RegInit(false.B)
  val start = RegNext(io.last)

  when(storeQueue.io.enq.fire()){
    pushCnt.inc()
  }
  when (storeQueue.io.deq.fire()) {
    popCnt.inc()
  }


  when(io.last){
    length := pushCnt.value
    pushCnt.value := 0.U
    sendingState := true.B
  }

  val ts_Inst = Wire(new MemDecode)
  val memTensorRows = Mux(length % tp.tensorWidth.U === 0.U, length / tp.tensorWidth.U, (length /tp.tensorWidth.U) + 1.U)


  when (popCnt.value === memTensorRows && length > 0.U){
    popCnt.value := 0.U

  }

  storeQueue.io.last := io.last
  storeQueue.io.enq <> io.in
  io.in.ready := storeQueue.io.enq.ready && !sendingState

  storeQueue.io.deq.ready := true.B

  tensorStore_row.io.tensor.wr.valid := storeQueue.io.deq.valid
  tensorStore_row.io.tensor.wr.bits.data := VecInit(storeQueue.io.deq.bits.map(_.row.asUInt())).asTypeOf(tensorStore_row.io.tensor.wr.bits.data)
  tensorStore_row.io.tensor.wr.bits.idx := popCnt.value
  tensorStore_row.io.tensor.rd <> DontCare

  tensorStore_col.io.tensor.wr.valid := storeQueue.io.deq.valid
  tensorStore_col.io.tensor.wr.bits.data := VecInit(storeQueue.io.deq.bits.map(_.col.asUInt())).asTypeOf(tensorStore_col.io.tensor.wr.bits.data)
  tensorStore_col.io.tensor.wr.bits.idx := popCnt.value
  tensorStore_col.io.tensor.rd <> DontCare

  tensorStore_val.io.tensor.wr.valid := storeQueue.io.deq.valid
  tensorStore_val.io.tensor.wr.bits.data := VecInit(storeQueue.io.deq.bits.map(_.data.asUInt())).asTypeOf(tensorStore_val.io.tensor.wr.bits.data)
  tensorStore_val.io.tensor.wr.bits.idx := popCnt.value
  tensorStore_val.io.tensor.rd <> DontCare


  tensorStore_row.io.start := start
  tensorStore_row.io.baddr := io.baddr_row
  tensorStore_row.io.inst := ts_Inst.asTypeOf(UInt(INST_BITS.W))

  tensorStore_col.io.start := start
  tensorStore_col.io.baddr := io.baddr_col
  tensorStore_col.io.inst := ts_Inst.asTypeOf(UInt(INST_BITS.W))

  tensorStore_val.io.start := start
  tensorStore_val.io.baddr := io.baddr_val
  tensorStore_val.io.inst := ts_Inst.asTypeOf(UInt(INST_BITS.W))

  io.vme_wr_row <> tensorStore_row.io.vme_wr
  io.vme_wr_col <> tensorStore_col.io.vme_wr
  io.vme_wr_val <> tensorStore_val.io.vme_wr

  val doneR = for (i <- 0 until 3) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  io.done := doneR.reduceLeft(_ && _)
  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
    sendingState := false.B
  }


  io.outLen := length

  when (tensorStore_row.io.done) {doneR(0) := true.B}
  when (tensorStore_col.io.done) {doneR(1) := true.B}
  when (tensorStore_val.io.done) {doneR(2) := true.B}

  ts_Inst.xpad_0 := 0.U
  ts_Inst.xpad_1 := 0.U
  ts_Inst.ypad_0 := 0.U
  ts_Inst.ypad_1 := 0.U
  ts_Inst.xstride := memTensorRows
  ts_Inst.xsize := memTensorRows
  ts_Inst.ysize := 1.U
  ts_Inst.empty_0 := 0.U
  ts_Inst.dram_offset := 0.U
  ts_Inst.sram_offset := 0.U
  ts_Inst.id := 3.U
  ts_Inst.push_next := 0.U
  ts_Inst.push_prev := 0.U
  ts_Inst.pop_next := 0.U
  ts_Inst.pop_prev := 0.U
  ts_Inst.op := 0.U
}
