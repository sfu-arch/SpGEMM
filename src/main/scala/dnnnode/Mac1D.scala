
package dnnnode

import FPU.FType
import chisel3._
import chisel3.util._
import config._
import dnn.{MacNode, ReduceNode}
import dnn.memory.TensorParams
import dnn.types.{OperatorDot, OperatorReduction}
import interfaces.{ControlBundle, CustomDataBundle, TensorReadReq, TensorReadResp}
import node.{FPvecN, HandShakingIONPS, HandShakingNPS, Shapes, vecN}
import shell._
//import vta.util.config._


/** TensorLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class Mac1DIO[gen <: Shapes, gen2 <: Shapes](NumMac: Int, wgtTensorType: String = "none")
                                                (macShape: => gen2)(implicit p: Parameters)
  extends HandShakingIONPS(NumMac)(new CustomDataBundle(UInt(p(XLEN).W))) {
  val tpWgt = new TensorParams(wgtTensorType)
  val mp = p(ShellKey).memParams

    val in = Vec(NumMac ,Flipped(Decoupled(new CustomDataBundle(UInt(macShape.getWidth.W)))))
    val wgtTensorReq = Decoupled(new TensorReadReq())
    val wgtTensorResp = Input(Flipped(new TensorReadResp(macShape.getWidth)))
    val wgtIndex = Input(UInt(tpWgt.memAddrBits.W))
    val rowWidth = Input(UInt(mp.addrBits.W))
    val last = Output(Bool())
    val startLoadWgt = Input(Bool())
    val doneLoadWgt = Output(Bool())
    val startMac = Input(Bool())
    val doneMac = Output(Bool())

  override def cloneType = new Mac1DIO(NumMac, wgtTensorType)(macShape).asInstanceOf[this.type]
}

class Mac1D[L <: Shapes, K <: Shapes : OperatorDot : OperatorReduction]
              (NumMac: Int, ChBatch: Int, wgtTensorType: String = "none")
              (macShape: => K)
              (implicit p: Parameters)
  extends HandShakingNPS(NumMac, 0)(new CustomDataBundle(UInt(p(XLEN).W)))(p) {
  override lazy val io = IO(new Mac1DIO(NumMac, wgtTensorType)(macShape))

  val sIdle :: sReadWeight :: sMacStart :: sExec :: sFinish :: Nil = Enum(5)
  val state = RegInit(sIdle)


  val S = new FType(8, 24)
  val accShape = new FPvecN(2, S, 0)

  val readWgtCnt = Counter(ChBatch + 1)
  val outCnt = Counter(io.tpWgt.memDepth)
  io.doneMac := false.B
  io.last := false.B

  val loadWeight = Module(new TLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0)(macShape))
  val weight = RegInit(CustomDataBundle.default(0.U(macShape.getWidth.W)))
  val weight_valid = RegInit(false.B)

  loadWeight.io.enable.bits <> ControlBundle.active()
  loadWeight.io.enable.valid := true.B
  io.wgtTensorReq <> loadWeight.io.tensorReq
  loadWeight.io.tensorResp <> io.wgtTensorResp
  loadWeight.io.GepAddr.valid := false.B
  loadWeight.io.GepAddr.bits.taskID := 0.U
  loadWeight.io.GepAddr.bits.predicate := true.B
  loadWeight.io.GepAddr.bits.data := io.wgtIndex + readWgtCnt.value

  loadWeight.io.Out(0).ready := ~weight_valid
  when(loadWeight.io.Out(0).fire()) {
    weight := loadWeight.io.Out(0).bits
    weight_valid := true.B
  }


  val weightQ = Module( new Queue(CustomDataBundle(UInt(macShape.getWidth.W)), ChBatch + 1))
  weightQ.io.enq.bits := Mux(state === sReadWeight, weight, weightQ.io.deq.bits)
  weightQ.io.enq.valid := Mux(state === sReadWeight, weight_valid, weightQ.io.deq.valid)
  when(weightQ.io.enq.fire()) {
    weight_valid := false.B
  }

  val mac = for (i <- 0 until NumMac) yield {
    val macNode = Module(new MacNode(NumOuts = 1, ID = 0, lanes = macShape.getLength())(macShape))
    macNode
  }

  val acc = for (i <- 0 until NumMac) yield {
    val accNode = Module(new ReduceNode(NumOuts = 1, ID = 0, false, "Add")(accShape))
    accNode
  }

//  val accInput_wire = for (i <- 0 until NumMac) yield {
//    val acc_wire = Wire(Vec (2, UInt(xlen.W)))
//    acc_wire
//  }

  val outData = for (i <- 0 until NumMac) yield {
    val data = RegInit(CustomDataBundle.default(0.U(xlen.W)))
    data
  }

  val accValid = for (i <- 0 until NumMac) yield {
    val accV = RegInit(false.B)
    accV
  }

  val batchCnt = for (i <- 0 until NumMac) yield {
    val batchCounter = Counter(ChBatch + 1)
    batchCounter
  }

  for (i <- 0 until NumMac) {

    mac(i).io.enable.bits <> ControlBundle.active()
    mac(i).io.enable.valid := true.B

    acc(i).io.enable.bits <> ControlBundle.active()
    acc(i).io.enable.valid := true.B


    mac(i).io.LeftIO.bits <> io.in(i).bits
    mac(i).io.LeftIO.valid := io.in(i).valid & state === sExec
    io.in(i).ready := mac(i).io.LeftIO.ready & state === sExec

    mac(i).io.RightIO.bits := weightQ.io.deq.bits
    mac(i).io.RightIO.valid := weightQ.io.deq.valid & state === sExec
    weightQ.io.deq.ready := mac.map(_.io.RightIO.ready).reduceLeft(_ && _) & state === sExec


//    accInput_wire(i) := Cat(mac(i).io.Out(0).bits.data.asUInt(), outData(i).data.asUInt())
//    mac(i).io.Out(0).ready := ~accValid(i) & (batchCnt(i).value < ChBatch.U)
    mac(i).io.Out(0).ready := acc(i).io.LeftIO.ready & ~io.Out(i).valid
    acc(i).io.LeftIO.bits.data := Cat(mac(i).io.Out(0).bits.data.asUInt(), outData(i).data.asUInt()).asTypeOf(CustomDataBundle(UInt((2 * xlen).W))).data
    acc(i).io.LeftIO.bits.valid := mac(i).io.Out(0).bits.valid
    acc(i).io.LeftIO.bits.taskID := mac(i).io.Out(0).bits.taskID
    acc(i).io.LeftIO.bits.predicate := mac(i).io.Out(0).bits.predicate

    acc(i).io.LeftIO.valid := mac(i).io.Out(0).valid & ~io.Out(i).valid

    acc(i).io.Out(0).ready := ~accValid(i) & (batchCnt(i).value < ChBatch.U)
    when(acc(i).io.Out(0).fire()) {

      outData(i).data := acc(i).io.Out(0).bits.data.asUInt()
      outData(i).valid := acc(i).io.Out(0).bits.valid
      outData(i).taskID := acc(i).io.Out(0).bits.taskID
      outData(i).predicate := acc(i).io.Out(0).bits.predicate
      accValid(i) := true.B
    }

    when(accValid(i)) {
      accValid(i) := false.B
    }

    io.Out(i).bits := outData(i)

    when(acc(i).io.Out(0).fire()) {
      batchCnt(i).inc()
    }
    io.Out(i).valid := false.B
    when(batchCnt(i).value === ChBatch.U) {
      io.Out(i).valid := true.B
    }
    when(io.Out(i).fire()) {
      batchCnt(i).value := 0.U
      outData(i).data := 0.U
    }
  }


  when (io.Out.map(_.fire()).reduceLeft(_ && _)){
    outCnt.inc()
  }

  when (loadWeight.io.Out(0).fire()){
    readWgtCnt.inc()
  }

  io.doneLoadWgt := false.B

  switch(state) {
    is (sIdle) {
      when(io.startLoadWgt) {
        state := sReadWeight
      }
    }
    is (sReadWeight) {
      loadWeight.io.GepAddr.valid := true.B
      when(readWgtCnt.value === ChBatch.U){
        readWgtCnt.value := 0.U
        state := sMacStart
      }
    }
    is(sMacStart){
      io.doneLoadWgt := true.B
      when(io.startMac){
        state := sExec
      }
    }
    is (sExec) {
        when (outCnt.value === io.rowWidth) {
        state := sFinish
        outCnt.value := 0.U
      }
    }
    is (sFinish){
        io.doneMac := true.B
        io.last := true.B
        state := sIdle

    }
  }

}
