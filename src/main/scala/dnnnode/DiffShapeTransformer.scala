
package dnnnode

import chisel3._
import chisel3.util._
import config._
import dnn.memory.{TensorMaster, TensorParams}
import shell._


/** Diff queue.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class DiffShapeTransformerIO(memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val len = Input(UInt(mp.addrBits.W))
    val in = Flipped(Decoupled(UInt(p(XLEN).W)))
    val out = Decoupled(UInt(p(XLEN).W))
  })
}

class DiffShapeTransformer(bufSize: Int, memTensorType: String = "none")(implicit p: Parameters)
  extends DiffShapeTransformerIO(memTensorType)(p) {

  require(bufSize > 1, "DiffQueue must have at least two elements")

  val elemNum = io.len

  val popCnt = Counter(tp.memDepth)

  val sIdle :: sRead :: Nil = Enum(2)
  val state = RegInit(sIdle)

  val queue = Module(new DiffQueue(UInt(p(XLEN).W), bufSize, NumIns = 1))

  queue.io.clear := false.B
  queue.io.enq.bits := io.in.bits.asTypeOf(queue.io.enq.bits)

  queue.io.enq.valid := io.in.valid && state === sRead
  io.in.ready := queue.io.enq.ready

  io.out <> queue.io.deq

  when(queue.io.deq.fire()) {popCnt.inc()}


  switch(state){
    is(sIdle){
      when(io.start){
        state := sRead
      }
    }
    is(sRead){
      when((popCnt.value === elemNum - 1.U) && queue.io.deq.fire()){
        popCnt.value := 0.U
        queue.io.clear := true.B
        state := sIdle
      }
    }
  }


}
