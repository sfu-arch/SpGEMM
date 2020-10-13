
package dnnnode

import chisel3._
import chisel3.util._
import config._
import dnn.memory.{TensorMaster, TensorParams}
import interfaces.CooDataBundle
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
class CooShifterIO(bufSize: Int, memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tp = new TensorParams(memTensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
//    val start = Input(Bool())
//    val done = Output(Bool())
//    val len = Input(UInt(mp.addrBits.W))
    val idx = Input(UInt(log2Ceil(bufSize + 1).W))
    val numDeq = Input(UInt(log2Ceil(bufSize + 1).W))

    val ind = Flipped(Decoupled(UInt(p(ROWLEN).W)))
    val value = Flipped(Decoupled(UInt(p(XLEN).W)))

//    val indTensor = new TensorMaster(memTensorType)
//    val valTensor = new TensorMaster(memTensorType)

    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
  })
}

class CooShifter[L <: Shapes](rowBased: Boolean, bufSize: Int, memTensorType: String = "none")
                                      (outShape: => L)(implicit p: Parameters)
  extends CooShifterIO(bufSize, memTensorType)(p) {

//  val elemNum = io.len / outShape.getLength().U
//  val memTensorRows = Mux(io.len % tp.tensorWidth.U === 0.U,
//    io.len / tp.tensorWidth.U,
//    (io.len / tp.tensorWidth.U) + 1.U)

  val pushCnt = Counter(tp.memDepth)
  val popCnt = Counter(tp.memDepth)

  val sIdle :: sRead :: sClear :: Nil = Enum(3)
  val state = RegInit(sIdle)


  val queue = Module(new MIMOShifter(new CooDataBundle(UInt(p(XLEN).W)), bufSize, NumIns = 1))

//  val validReg = RegInit(false.B)

//  val dataIn = Wire(Vec(tp.tensorWidth, new CooDataBundle(UInt(p(XLEN).W))))
  val dataIn = Wire(Vec(1, new CooDataBundle(UInt(p(XLEN).W))))

  if(rowBased){
//    for (i <- 0 until tp.tensorWidth) {
      dataIn(0).data := io.value.bits
      dataIn(0).row := io.ind.bits
      dataIn(0).col := 0.U
      dataIn(0).valid := true.B
//    }
  } else{
//    for (i <- 0 until tp.tensorWidth) {
      dataIn(0).data := io.value.bits
      dataIn(0).row := 0.U
      dataIn(0).col := io.ind.bits
      dataIn(0).valid := true.B
//    }
  }


//  io.done := false.B
  queue.io.clear := false.B
  queue.io.enq.bits := dataIn
  queue.io.enq.valid := io.ind.valid && io.value.valid     //queue.io.enq.ready && validReg === sRead//io.tensor(i).rd.data.valid
  io.ind.ready := queue.io.enq.ready && io.ind.valid && io.value.valid
  io.value.ready := queue.io.enq.ready && io.ind.valid && io.value.valid


  io.out <> queue.io.deq
  queue.io.idx := io.idx
  queue.io.numDeq := io.numDeq

  when(queue.io.enq.fire()) {pushCnt.inc()}
  when(queue.io.deq.fire()) {
    popCnt.value := popCnt.value + io.numDeq
  }

}
