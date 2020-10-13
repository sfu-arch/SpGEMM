
package dnn_layers

import chisel3._
import chisel3.util._
import config._
import dnn.memory._
import dnn.types.{OperatorDot, OperatorReduction}
import dnnnode.{Mac1D, ShapeTransformer}
import interfaces.ControlBundle
import node.{Shapes, vecN}
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
class PW_BlockIO(Hx: Int, Fx: Int, wgtType: String = "none", memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tpMem = new TensorParams(memTensorType)
  val tpWgt = new TensorParams(wgtType)

  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val inBaseAddr = Input(UInt(mp.addrBits.W))
    val outBaseAddr = Input(UInt(mp.addrBits.W))



    val rowWidth = Input(UInt(mp.addrBits.W))

    val vme_rd = Vec(Hx, new VMEReadMaster)
    val vme_wr = Vec(Fx * Hx, new VMEWriteMaster)

    val wgtIndex = Input(UInt(tpWgt.memAddrBits.W))
    val vme_wgt_rd = new VMEReadMaster

    val wgt_baddr = Input(UInt(mp.addrBits.W))

    val inDMA_act_time = Output(UInt(mp.addrBits.W))
    val inDMA_wgt_time = Output(UInt(mp.addrBits.W))
    val outDMA_act_time = Output(UInt(mp.addrBits.W))
    val mac_time = Output(UInt(mp.addrBits.W))
  })
}

class PW_Block[L <: Shapes, K <: Shapes : OperatorDot : OperatorReduction]
(Hx: Int, Fx: Int, ChBatch: Int, wgtType: String = "none", memTensorType: String = "none")
(memShape: => L)(CxShape: => K)(implicit p: Parameters)
  extends PW_BlockIO(Hx, Fx, wgtType, memTensorType)(p) {

  val inDMA_act_time = Counter(2000)
  val inDMA_wgt_time = Counter(2000)
  val outDMA_act_time = Counter(2000)
  val mac_time = Counter(2000)

  io.inDMA_act_time := inDMA_act_time.value
  io.inDMA_wgt_time := inDMA_wgt_time.value
  io.outDMA_act_time := outDMA_act_time.value
  io.mac_time := mac_time.value

  val M_Brick_in =  Module(new inDMA_act_HWC(Hx, NumOuts = 1, memTensorType))

  val I_Brick = Module(new ShapeTransformer(Hx, Fx, bufSize = 20, memTensorType)(CxShape))

  val L_Brick = for (i <- 0 until Fx) yield {
    val mac1d = Module(new Mac1D(Hx, ChBatch, wgtType)(CxShape))
    mac1d
  }

  val M_Brick_out = for (i <- 0 until Fx) yield {
    val outDMA = Module(new outDMA_act(Hx, bufSize = 20, memTensorType))
    outDMA
  }

  val doneR = for (i <- 0 until Fx) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  val readTensorCnt = Counter(tpMem.memDepth)

  val sIdle :: sWgtRead :: sActRead :: sMacLoadWgt :: sExec :: Nil = Enum(5)
  val state = RegInit(sIdle)

  /* ================================================================== *
   *                     Depth-wise - inDMA_weight                      *
   * ================================================================== */

  val M_Brick_wgt = Module(new inDMA_wgt(wgtTFDepth = 20, bufSize = 100, wgtType, memTensorType)(CxShape))
  val wgtCtrl = Module(new ReadTensorController(Fx, wgtType)(CxShape))
  M_Brick_wgt.io.tensor <> wgtCtrl.io.tensor
  io.vme_wgt_rd <> M_Brick_wgt.io.vme_rd

  M_Brick_wgt.io.numWeight := 15.U
  M_Brick_wgt.io.start := false.B
  M_Brick_wgt.io.baddr := io.wgt_baddr

  for (i <- 0 until Fx) {
    wgtCtrl.io.ReadIn(i) <> L_Brick(i).io.wgtTensorReq
    L_Brick(i).io.wgtTensorResp <> wgtCtrl.io.ReadOut(i)
  }

  /* ================================================================== *
    *                      inDMA_acts & loadNodes                       *
    * ================================================================== */

  M_Brick_in.io.start := io.start
  M_Brick_in.io.rowWidth := io.rowWidth
  M_Brick_in.io.depth := CxShape.getLength().U * ChBatch.U
  M_Brick_in.io.baddr := io.inBaseAddr

  I_Brick.io.start := M_Brick_in.io.done
  I_Brick.io.len := io.rowWidth
  I_Brick.io.depth := CxShape.getLength().U * ChBatch.U
  for (i <- 0 until Hx) {
    M_Brick_in.io.tensor(i) <> I_Brick.io.tensor(i)
    io.vme_rd(i) <> M_Brick_in.io.vme_rd(i)
  }
  /* ================================================================== *
    *                        loadNodes & mac1Ds                         *
    * ================================================================== */

  for (i <- 0 until Fx) {
    L_Brick(i).io.enable.bits <> ControlBundle.active()
    L_Brick(i).io.enable.valid := true.B
    L_Brick(i).io.wgtIndex := io.wgtIndex + (i * ChBatch).U
    L_Brick(i).io.rowWidth := io.rowWidth

    for (j <- 0 until Hx) {
      L_Brick(i).io.in(j) <> I_Brick.io.out(j)(i)

      M_Brick_out(i).io.in(j) <> L_Brick(i).io.Out(j)
      io.vme_wr(i*Hx + j) <> M_Brick_out(i).io.vme_wr(j)
    }
    M_Brick_out(i).io.rowWidth := io.rowWidth
    M_Brick_out(i).io.baddr := io.outBaseAddr + (i.U * (Hx.U * io.rowWidth  * (tpMem.tensorElemBits.U / 8.U) ))

    M_Brick_out(i).io.last.foreach(a => a := L_Brick(i).io.last)
    M_Brick_out(i).io.start := L_Brick(i).io.doneMac
    when(M_Brick_out(i).io.done) {
      doneR(i) := true.B
    }
  }

  L_Brick.foreach(_.io.startLoadWgt := false.B)
  L_Brick.foreach(_.io.startMac := false.B)

  /* ================================================================== *
      *                        Done Signal                              *
      * ================================================================== */

  io.done := false.B
  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
  }

  val memTensorRows = Mux(io.rowWidth * ChBatch.U * CxShape.getLength().U  % tpMem.tensorWidth.U === 0.U,
    io.rowWidth * ChBatch.U * CxShape.getLength().U / tpMem.tensorWidth.U,
    (io.rowWidth * ChBatch.U * CxShape.getLength().U /tpMem.tensorWidth.U) + 1.U)

  when(L_Brick.map(_.io.in.map(_.fire()).reduceLeft(_ && _)).reduceLeft(_ && _) & state === sExec){
    readTensorCnt.inc()
  }

  when(readTensorCnt.value === memTensorRows) {
    readTensorCnt.value := 0.U
  }

  when(state === sIdle){
    inDMA_act_time.value := 0.U
    inDMA_wgt_time.value := 0.U
    outDMA_act_time.value := 0.U
    mac_time.value := 0.U
  }

  when(state === sWgtRead) {inDMA_wgt_time.inc()}
  when(state === sActRead) {inDMA_act_time.inc()}
  when(state === sExec) {mac_time.inc()}

  switch(state) {
    is(sIdle) {
      when(io.start) {
        M_Brick_wgt.io.start := true.B
        state := sWgtRead
      }
    }
    is(sWgtRead) {
      when(M_Brick_wgt.io.done) {
        state := sActRead
        M_Brick_in.io.start := true.B
        L_Brick.foreach(_.io.startLoadWgt := true.B)
      }
    }
    is(sActRead) {
      when(M_Brick_in.io.done){
        state := sMacLoadWgt
      }
    }
    is(sMacLoadWgt){
      when(L_Brick.map(_.io.doneLoadWgt).reduceLeft(_ && _)){
        L_Brick.foreach(_.io.startMac := true.B)
        state := sExec
      }
    }
    is(sExec){
      when(doneR.reduceLeft(_ && _)) {
        io.done := true.B
        state := sIdle
      }

    }
  }

}
