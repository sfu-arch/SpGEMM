package dnnnode

import Chisel.Enum
import chisel3._
import chisel3.util._
import chisel3.{Module, UInt}
import config.{Parameters, XLEN}
import dnn.memory.{TensorClient, TensorMaster, TensorParams}
import interfaces.CustomDataBundle
import node.{Shapes, vecN}
import shell.ShellKey
import dnn.memory.ISA._

class WeightShapeTransformerIO[gen <: Shapes](wgtTensorType: String = "none", memTensorType: String = "none")(wgtShape: => gen)(implicit val p: Parameters)
  extends Module {
  val tpMem = new TensorParams(memTensorType)
  val tpWgt = new TensorParams(wgtTensorType)
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val numWeight = Input(UInt(tpWgt.memAddrBits.W))
    val tensorMaster = new TensorMaster(memTensorType)
    val tensor = new TensorClient(wgtTensorType)
  })
}

class WeightShapeTransformer[L <: Shapes](wgtTFDepth: Int, bufSize: Int, wgtTensorType: String = "none", memTensorType: String = "none")(wgtShape: => L)
                                         (implicit p: Parameters)
  extends WeightShapeTransformerIO(wgtTensorType, memTensorType)(wgtShape)(p) {

  val buffer = Module(new MIMOQueue(UInt(p(XLEN).W), bufSize, tpMem.tensorWidth, wgtShape.getLength()))
  require(bufSize >= tpMem.tensorWidth, "bufSize should be greater than memTensorWidth")

  val wgtTensorDepth = Mux(io.numWeight * wgtShape.getLength().U % tpMem.tensorWidth.U === 0.U,
        io.numWeight * wgtShape.getLength().U / tpMem.tensorWidth.U, (io.numWeight * wgtShape.getLength().U / tpMem.tensorWidth.U) + 1.U)

  val writeBufCntOn = RegInit(init = false.B)
  val (writeBufCnt, writeWrap) = Counter(writeBufCntOn, wgtTFDepth)

  val readWgtCnt = Counter(wgtTFDepth + 1)


  val s_idle :: s_BufferWrite :: s_Transfer :: s_Finish :: Nil = Enum(4)
  val state = RegInit(s_idle)

  val tensorFile = SyncReadMem(wgtTFDepth, wgtShape)

  buffer.io.enq.valid := io.tensorMaster.rd.data.valid
  buffer.io.enq.bits := io.tensorMaster.rd.data.bits(0)
  io.tensorMaster.rd.idx.bits := writeBufCnt
  io.tensorMaster.rd.idx.valid := buffer.io.enq.ready & writeBufCntOn
  io.tensorMaster.wr <> DontCare

  when (writeBufCnt === wgtTensorDepth - 1.U) {
    writeBufCntOn := false.B
    writeBufCnt := 0.U
  }
  when (io.start) {writeBufCntOn := true.B}

  when (buffer.io.deq.valid & readWgtCnt.value < io.numWeight) {
    tensorFile.write(readWgtCnt.value, buffer.io.deq.bits.asTypeOf(wgtShape))
    buffer.io.deq.ready := true.B
    readWgtCnt.inc()
  }.otherwise {
    buffer.io.deq.ready := false.B
  }


  when (readWgtCnt.value === io.numWeight) {
    io.done := true.B
    buffer.io.clear := true.B
//    readWgtCnt.inc()
    readWgtCnt.value := 0.U
  }.otherwise{
    buffer.io.clear := false.B
    io.done := false.B
  }

  val rvalid = RegNext(io.tensor.rd.idx.valid)
  io.tensor.rd.data.valid := rvalid

  val rdata = tensorFile.read(io.tensor.rd.idx.bits, io.tensor.rd.idx.valid)
  io.tensor.rd.data.bits := rdata.asUInt.asTypeOf(io.tensor.rd.data.bits)

}

