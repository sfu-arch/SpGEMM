
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import dnn.memory.ISA._
import dnnnode.StoreQueue
import interfaces.CooDataBundle
import shell._


/** outDMA_act
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class outStream_cooIO(memTensorType: String = "none")(implicit val p: Parameters)
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

class outStream_coo(bufSize: Int, memTensorType: String = "none")(implicit p: Parameters)
  extends outStream_cooIO(memTensorType)(p) {
  require(bufSize > tp.tensorWidth, "buffer size should be greater than the tensorFile width")

  val tensorStore_row = Module(new TensorStore(memTensorType))
  val tensorStore_col = Module(new TensorStore(memTensorType))
  val tensorStore_val = Module(new TensorStore(memTensorType))

  val storeQueue_row = Module(new StoreQueue(UInt(p(XLEN).W), bufSize, tp.tensorWidth))
  val storeQueue_col = Module(new StoreQueue(UInt(p(XLEN).W), bufSize, tp.tensorWidth))
  val storeQueue_val = Module(new StoreQueue(UInt(p(XLEN).W), bufSize, tp.tensorWidth))

  val popCnt_row = Counter(math.pow(2, p(XLEN)).toInt)
  val popCnt_col = Counter(math.pow(2, p(XLEN)).toInt)
  val popCnt_val = Counter(math.pow(2, p(XLEN)).toInt)

  val pushCnt = Counter(math.pow(2, p(XLEN)).toInt)
  val length = RegInit(init = 0.U)
  val sendingState = RegInit(false.B)
//  val start = RegNext(io.last)

  val start_row = RegInit(false.B)
  val start_col = RegInit(false.B)
  val start_val = RegInit(false.B)

  when(io.in.fire()){
    pushCnt.inc()
  }
  when (storeQueue_row.io.deq.fire()) {
    popCnt_row.inc()
  }
  when (storeQueue_col.io.deq.fire()) {
    popCnt_col.inc()
  }
  when (storeQueue_val.io.deq.fire()) {
    popCnt_val.inc()
  }


  when(io.last){
    length := pushCnt.value
    pushCnt.value := 0.U
    sendingState := true.B
  }

  val ts_Inst_row = Wire(new MemDecode)
  val ts_Inst_col = Wire(new MemDecode)
  val ts_Inst_val = Wire(new MemDecode)

  val memTensorRows = Mux(length % tp.tensorWidth.U === 0.U, length / tp.tensorWidth.U, (length /tp.tensorWidth.U) + 1.U)

  when (popCnt_row.value === memTensorRows && length > 0.U){
    popCnt_row.value := 0.U

  }
  val doneR = for (i <- 0 until 3) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  val outBulkSize = 4.U

  when(popCnt_row.value === outBulkSize) {
    start_row := true.B
    doneR(0) := false.B
    ts_Inst_row.xsize := outBulkSize
    ts_Inst_row.xstride := outBulkSize
  }.elsewhen ((popCnt_row.value % outBulkSize) === 0.U & doneR(0)){
    start_row := true.B
    doneR(0) := false.B
    ts_Inst_row.xsize := outBulkSize
    ts_Inst_row.xstride := outBulkSize
  }



  when(doneR(0)) {
    ts_Inst_row.sram_offset := ts_Inst_row.sram_offset + outBulkSize

  }

  storeQueue_row.io.last := io.last
  storeQueue_col.io.last := io.last
  storeQueue_val.io.last := io.last

  storeQueue_row.io.enq.bits := io.in.bits.row
  storeQueue_row.io.enq.valid := io.in.valid

  storeQueue_col.io.enq.bits := io.in.bits.col
  storeQueue_col.io.enq.valid := io.in.valid

  storeQueue_val.io.enq.bits := io.in.bits.data
  storeQueue_val.io.enq.valid := io.in.valid

  io.in.ready := storeQueue_row.io.enq.ready && storeQueue_col.io.enq.ready && storeQueue_val.io.enq.ready && !sendingState


//  io.in.ready := storeQueue_row.io.enq.ready && !sendingState

  storeQueue_row.io.deq.ready := true.B
  storeQueue_col.io.deq.ready := true.B
  storeQueue_val.io.deq.ready := true.B

  tensorStore_row.io.tensor.wr.valid := storeQueue_row.io.deq.valid
  tensorStore_row.io.tensor.wr.bits.data := VecInit(storeQueue_row.io.deq.bits.asUInt()).asTypeOf(tensorStore_row.io.tensor.wr.bits.data)
  tensorStore_row.io.tensor.wr.bits.idx := popCnt_row.value
  tensorStore_row.io.tensor.rd <> DontCare

  tensorStore_col.io.tensor.wr.valid := storeQueue_row.io.deq.valid
  tensorStore_col.io.tensor.wr.bits.data := VecInit(storeQueue_col.io.deq.bits.asUInt()).asTypeOf(tensorStore_col.io.tensor.wr.bits.data)
  tensorStore_col.io.tensor.wr.bits.idx := popCnt_col.value
  tensorStore_col.io.tensor.rd <> DontCare

  tensorStore_val.io.tensor.wr.valid := storeQueue_row.io.deq.valid
  tensorStore_val.io.tensor.wr.bits.data := VecInit(storeQueue_val.io.deq.bits.asUInt()).asTypeOf(tensorStore_val.io.tensor.wr.bits.data)
  tensorStore_val.io.tensor.wr.bits.idx := popCnt_val.value
  tensorStore_val.io.tensor.rd <> DontCare


  tensorStore_row.io.start := start_row
  tensorStore_row.io.baddr := io.baddr_row
  tensorStore_row.io.inst := ts_Inst_row.asTypeOf(UInt(INST_BITS.W))

  tensorStore_col.io.start := start_col
  tensorStore_col.io.baddr := io.baddr_col
  tensorStore_col.io.inst := ts_Inst_row.asTypeOf(UInt(INST_BITS.W))

  tensorStore_val.io.start := start_val
  tensorStore_val.io.baddr := io.baddr_val
  tensorStore_val.io.inst := ts_Inst_row.asTypeOf(UInt(INST_BITS.W))

  io.vme_wr_row <> tensorStore_row.io.vme_wr
  io.vme_wr_col <> tensorStore_col.io.vme_wr
  io.vme_wr_val <> tensorStore_val.io.vme_wr



//  io.done := doneR.reduceLeft(_ && _)

  /*when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
    sendingState := false.B
  }*/


  io.outLen := length

  when (tensorStore_row.io.done) {doneR(0) := true.B}
  when (tensorStore_col.io.done) {doneR(1) := true.B}
  when (tensorStore_val.io.done) {doneR(2) := true.B}

  ts_Inst_row.xpad_0 := 0.U
  ts_Inst_row.xpad_1 := 0.U
  ts_Inst_row.ypad_0 := 0.U
  ts_Inst_row.ypad_1 := 0.U
  ts_Inst_row.xstride := memTensorRows
  ts_Inst_row.xsize := memTensorRows
  ts_Inst_row.ysize := 1.U
  ts_Inst_row.empty_0 := 0.U
  ts_Inst_row.dram_offset := 0.U
  ts_Inst_row.sram_offset := 0.U
  ts_Inst_row.id := 3.U
  ts_Inst_row.push_next := 0.U
  ts_Inst_row.push_prev := 0.U
  ts_Inst_row.pop_next := 0.U
  ts_Inst_row.pop_prev := 0.U
  ts_Inst_row.op := 0.U
}
