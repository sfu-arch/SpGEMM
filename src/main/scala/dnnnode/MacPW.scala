
package dnnnode

import chisel3._
import chisel3.util._
import config._
import dnn.MacNode
import dnn.memory.TensorParams
import dnn.types.{OperatorDot, OperatorReduction}
import interfaces.{ControlBundle, CustomDataBundle, TensorReadReq, TensorReadResp}
import node.{HandShakingIONPS, HandShakingNPS, Shapes, vecN}
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
class MacPWIO[gen <: vecN](NumMac: Int, wgtTensorType: String = "none")
                                                (wgtShape: => gen)(implicit p: Parameters)
  extends HandShakingIONPS(NumMac)(new CustomDataBundle(UInt(p(XLEN).W))) {
  val tpWgt = new TensorParams(wgtTensorType)
  val mp = p(ShellKey).memParams
    val wgtTensorReq = Decoupled(new TensorReadReq())
    val wgtTensorResp = Input(Flipped(new TensorReadResp(wgtShape.getWidth)))
    val wgtIndex = Input(UInt(tpWgt.memAddrBits.W))
    val outRowWidth = Input(UInt(mp.addrBits.W))
    val last = Output(Bool())
    val start = Input(Bool())
    val done = Output(Bool())
  val in = Vec(NumMac ,Flipped(Decoupled(new CustomDataBundle(UInt(wgtShape.getWidth.W)))))

  override def cloneType = new MacPWIO(NumMac, wgtTensorType)(wgtShape).asInstanceOf[this.type]
}

class MacPW[L <: vecN, K <: Shapes : OperatorDot : OperatorReduction](NumMac: Int, wgtTensorType: String = "none")
                                                                           (wgtShape: => L)(macShape: => K)
                                                                           (implicit p: Parameters)
  extends HandShakingNPS(NumMac, 0)(new CustomDataBundle(UInt(p(XLEN).W)))(p) {
  override lazy val io = IO(new MacPWIO(NumMac, wgtTensorType)(wgtShape))

  val outCnt = Counter(io.tpWgt.memDepth)
  io.done := false.B
  io.last := false.B

  val loadWeight = Module(new TLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 0, RouteID = 0)(wgtShape))
  val weight = RegInit(CustomDataBundle.default(0.U(wgtShape.getWidth.W)))
  val weight_valid = RegInit(false.B)

  loadWeight.io.enable.bits <> ControlBundle.active()
  loadWeight.io.enable.valid := true.B
  io.wgtTensorReq <> loadWeight.io.tensorReq
  loadWeight.io.tensorResp <> io.wgtTensorResp
  loadWeight.io.GepAddr.valid := false.B
  loadWeight.io.GepAddr.bits.taskID := 0.U
  loadWeight.io.GepAddr.bits.predicate := true.B
  loadWeight.io.GepAddr.bits.data := io.wgtIndex

  loadWeight.io.Out(0).ready := ~weight_valid
  when(loadWeight.io.Out(0).fire()) {
    weight := loadWeight.io.Out(0).bits
    weight_valid := true.B
  }

  val mac = for (i <- 0 until NumMac) yield {
    val macNode = Module(new MacNode(NumOuts = 1, ID = 0, lanes = macShape.getLength())(macShape))
    macNode
  }


  val sIdle :: sExec :: sFinish :: Nil = Enum(3)
  val state = RegInit(sIdle)


  for (i <- 0 until NumMac) {

    mac(i).io.enable.bits <> ControlBundle.active()
    mac(i).io.enable.valid := true.B

    mac(i).io.LeftIO <> io.in(i)

    mac(i).io.RightIO.bits := weight
    mac(i).io.RightIO.valid := weight_valid & state === sExec
    io.Out(i) <> mac(i).io.Out(0)
  }

  when (mac.map(_.io.Out(0).fire()).reduceLeft(_ && _) & state === sExec){
    outCnt.inc()
  }

  switch(state) {
    is (sIdle) {
      when(io.start) {
        loadWeight.io.GepAddr.valid := true.B
        state := sExec
      }
    }
    is (sExec) {
        when (outCnt.value === io.outRowWidth) {
        state := sFinish
        outCnt.value := 0.U
      }
    }
    is (sFinish){
        weight_valid := false.B
        io.done := true.B
        io.last := true.B
        state := sIdle
    }
  }

}
