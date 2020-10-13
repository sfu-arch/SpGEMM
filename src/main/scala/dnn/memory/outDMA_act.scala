
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import dnnnode.{StoreQueue, TStore}
import interfaces.{ControlBundle, CustomDataBundle, TensorReadReq, TensorReadResp}
import node.{Shapes, vecN}
import shell._
import dnn.memory.ISA._


/** outDMA_act
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class outDMA_actIO(NumRows: Int, memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val baddr = Input(UInt(mp.addrBits.W))
    val rowWidth = Input(UInt(mp.addrBits.W))
    val vme_wr = Vec(NumRows, new VMEWriteMaster)
    val in = Vec(NumRows, Flipped(Decoupled(new CustomDataBundle(UInt(p(XLEN).W)))))
    val last = Vec(NumRows, Input(Bool()))
  })
}

class outDMA_act(NumRows: Int, bufSize: Int, memTensorType: String = "none")(implicit p: Parameters)
  extends outDMA_actIO(NumRows, memTensorType)(p) {

  val tensorStore = for (i <- 0 until NumRows) yield {
    val tensorS = Module(new TensorStore(memTensorType))
    tensorS
  }

  val storeBuffer = for (i <- 0 until NumRows) yield {
    val buf = Module(new StoreQueue(new CustomDataBundle(UInt(p(XLEN).W)), bufSize, tp.tensorWidth))
    buf
  }

  val indexCnt = for (i <- 0 until NumRows) yield {
    val cnt = Counter(tp.memDepth)
    cnt
  }

  val ts_Inst = Wire(new MemDecode)
  val memTensorRows = Mux(io.rowWidth % tp.tensorWidth.U === 0.U, io.rowWidth / tp.tensorWidth.U, (io.rowWidth /tp.tensorWidth.U) + 1.U)

  for (i <- 0 until NumRows) {
    when (storeBuffer(i).io.deq.fire()) {
      indexCnt(i).inc()
    }
    when (indexCnt(i).value === memTensorRows){
      indexCnt(i).value := 0.U
    }
  }

  for (i <-0 until NumRows) {

    storeBuffer(i).io.last := io.last(i)
    storeBuffer(i).io.enq <> io.in(i)

    storeBuffer(i).io.deq.ready := true.B
    tensorStore(i).io.tensor.wr.valid := storeBuffer(i).io.deq.valid
    tensorStore(i).io.tensor.wr.bits.data := VecInit(storeBuffer(i).io.deq.bits.map(_.data.asUInt())).asTypeOf(tensorStore(i).io.tensor.wr.bits.data)
    tensorStore(i).io.tensor.wr.bits.idx := indexCnt(i).value
    tensorStore(i).io.tensor.rd <> DontCare


    tensorStore(i).io.start := io.start
    tensorStore(i).io.baddr := io.baddr + (i.U * io.rowWidth * (tp.tensorElemBits.U / 8.U))
    tensorStore(i).io.inst := ts_Inst.asTypeOf(UInt(INST_BITS.W))
    io.vme_wr(i) <> tensorStore(i).io.vme_wr
  }

  val doneR = for (i <- 0 until NumRows) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  io.done := doneR.reduceLeft(_ && _)

  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
  }

  for (i <- 0 until NumRows) yield{
    when (tensorStore(i).io.done) {
      doneR(i) := true.B
    }
  }


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
