package tensorKernels

import chisel3._
import chisel3.util._
import config._
import dnn.CooSCALNode
import dnn.memory._
import dnn.types.{OperatorCooSCAL, OperatorDot, OperatorReduction}
import dnnnode.{CooShapeTransformer, CooShifter, DiffShapeTransformer, ShapeTransformer}
import interfaces.CooDataBundle
import node.{Shapes, vecN}
import shell._
//import vta.util.config._


/** OuterProduct Block
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class OuterDotIO(memTensorType: String = "none")(implicit val p: Parameters)
  extends Module {
  val tpMem = new TensorParams(memTensorType)

  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())

    val ind_A_BaseAddr = Input(UInt(mp.addrBits.W))
    val val_A_BaseAddr = Input(UInt(mp.addrBits.W))
    val ptr_A_BaseAddr = Input(UInt(mp.addrBits.W))

    val ind_B_BaseAddr = Input(UInt(mp.addrBits.W))
    val val_B_BaseAddr = Input(UInt(mp.addrBits.W))
    val ptr_B_BaseAddr = Input(UInt(mp.addrBits.W))


    val nnz_A = Input(UInt(mp.addrBits.W))
    val nnz_B = Input(UInt(mp.addrBits.W))
    val segSize = Input(UInt(mp.addrBits.W))

    val vme_rd_ptr = Vec(2, new VMEReadMaster)
    val vme_rd_ind = Vec(2, new VMEReadMaster)
    val vme_rd_val = Vec(2, new VMEReadMaster)

    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val eop = Output(Bool( ))

  })
}

class OuterDot[L <: Shapes : OperatorDot : OperatorReduction : OperatorCooSCAL]
(memTensorType: String = "none", maxRowLen: Int)
(segShape: => L)(implicit p: Parameters)
  extends OuterDotIO(memTensorType)(p) {

  val shape = new vecN(1, 0, false)

  val indDMA_A =  Module(new inStreamDMA(bufSize = 5000, memTensorType))
  val valDMA_A =  Module(new inStreamDMA(bufSize = 5000, memTensorType))
  val ptrDMA_A =  Module(new inStreamDMA(bufSize = 5000, memTensorType))

  val indDMA_B =  Module(new inStreamDMA(bufSize = 5000, memTensorType))
  val valDMA_B =  Module(new inStreamDMA(bufSize = 5000, memTensorType))
  val ptrDMA_B =  Module(new inStreamDMA(bufSize = 5000, memTensorType))

  val shapeTransformer_A = Module(new CooShapeTransformer(rowBased = true, memTensorType)(segShape))
  val shapeTransformer_B = Module(new CooShifter(rowBased = false, 1000, memTensorType)(segShape))

  val ptrST_A = Module(new DiffShapeTransformer( 3, memTensorType))
  val ptrST_B = Module(new DiffShapeTransformer( 3, memTensorType))

  val mul = Module(new CooSCALNode(N = 1, ID = 0, opCode = "Mul")(segShape))

  val sIdle :: sExec :: Nil = Enum(2)
  val state = RegInit(sIdle)



  /* ================================================================== *
    *                      inDMA_acts & loadNodes                       *
    * ================================================================== */

  ptrDMA_A.io.start := io.start
  ptrDMA_A.io.len := io.segSize + 1.U
  ptrDMA_A.io.baddr := io.ptr_A_BaseAddr

  indDMA_A.io.start := io.start
  indDMA_A.io.len := io.nnz_A
  indDMA_A.io.baddr := io.ind_A_BaseAddr

  valDMA_A.io.start := io.start
  valDMA_A.io.len := io.nnz_A
  valDMA_A.io.baddr := io.val_A_BaseAddr

  ptrDMA_B.io.start := io.start
  ptrDMA_B.io.len := io.segSize + 1.U
  ptrDMA_B.io.baddr := io.ptr_B_BaseAddr

  indDMA_B.io.start := io.start
  indDMA_B.io.len := io.nnz_B
  indDMA_B.io.baddr := io.ind_B_BaseAddr

  valDMA_B.io.start := io.start
  valDMA_B.io.len := io.nnz_B
  valDMA_B.io.baddr := io.val_B_BaseAddr

  io.vme_rd_ind(0) <> indDMA_A.io.vme_rd
  io.vme_rd_ind(1) <> indDMA_B.io.vme_rd

  io.vme_rd_val(0) <> valDMA_A.io.vme_rd
  io.vme_rd_val(1) <> valDMA_B.io.vme_rd

  io.vme_rd_ptr(0) <> ptrDMA_A.io.vme_rd
  io.vme_rd_ptr(1) <> ptrDMA_B.io.vme_rd

  shapeTransformer_A.io.ind <> indDMA_A.io.out
  shapeTransformer_A.io.value <> valDMA_A.io.out
  ptrST_A.io.in <> ptrDMA_A.io.out

  shapeTransformer_B.io.ind <> indDMA_B.io.out
  shapeTransformer_B.io.value <> valDMA_B.io.out
  ptrST_B.io.in <> ptrDMA_B.io.out

  /* ================================================================== *
   *                      inDMA_acts & loadNodes                       *
   * ================================================================== */

  ptrST_A.io.len := io.segSize + 1.U
  ptrST_B.io.len := io.segSize + 1.U

  ptrST_A.io.out.ready := false.B
  ptrST_B.io.out.ready := false.B

  /* ================================================================== *
    *                        DMA done registers                         *
    * ================================================================== */

  val DMA_doneR_A = for (i <- 0 until 2) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }
  val DMA_doneR_B = for (i <- 0 until 2) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }
  ptrST_A.io.start := io.start

  ptrST_B.io.start := io.start

  when (DMA_doneR_A.reduceLeft(_ && _)) {
    DMA_doneR_A.foreach(a => a := false.B)
  }
  when (DMA_doneR_B.reduceLeft(_ && _)) {
    DMA_doneR_B.foreach(a => a := false.B)
  }

  when(indDMA_A.io.done) {DMA_doneR_A(0) := true.B}
  when(valDMA_A.io.done) {DMA_doneR_A(1) := true.B}
  when(indDMA_B.io.done) {DMA_doneR_B(0) := true.B}
  when(valDMA_B.io.done) {DMA_doneR_B(1) := true.B}

  /* ================================================================== *
     *                       multiplier and st                          *
     * ================================================================== */
  val colAisZero = Wire(Bool())
  colAisZero := false.B
  when(ptrST_A.io.out.bits === 0.U) {colAisZero := true.B}

  val rowBisZero = Wire(Bool())
  rowBisZero := false.B
  when(ptrST_B.io.out.bits === 0.U) {rowBisZero := true.B}

  mul.io.scal.bits := shapeTransformer_B.io.out.bits
  mul.io.scal.valid := shapeTransformer_B.io.out.valid && ptrST_B.io.out.valid && !rowBisZero
  shapeTransformer_B.io.out.ready := false.B

  mul.io.vec(0).bits := shapeTransformer_A.io.out.bits
  mul.io.vec(0).valid := shapeTransformer_A.io.out.valid && ptrST_A.io.out.valid && !colAisZero
  shapeTransformer_A.io.out.ready := false.B

  io.out <> mul.io.out(0)

  /* ================================================================== *
    *                          State Machine                            *
    * ================================================================== */

  val bCnt = Counter(math.pow(2,p(XLEN)).toInt)
  val aCnt = Counter(math.pow(2,p(XLEN)).toInt)

  shapeTransformer_B.io.idx := bCnt.value
  shapeTransformer_B.io.numDeq := ptrST_B.io.out.bits

  val outCnt_a = Counter(math.pow(2,p(XLEN)).toInt)
  val outCnt_b = Counter(math.pow(2,p(XLEN)).toInt)
  when(ptrST_A.io.out.fire()){outCnt_a.inc()}
  when(ptrST_B.io.out.fire()){outCnt_b.inc()}

  io.eop := false.B
//  io.lastOut := false.B


  switch(state) {
    is(sIdle) {
      when(io.start) {
        indDMA_A.io.start := true.B
        valDMA_A.io.start := true.B
        state := sExec
      }
    }

    is(sExec) {
      when(ptrST_A.io.out.valid && ptrST_B.io.out.valid) {
        when(mul.io.scal.ready && mul.io.vec(0).ready && !colAisZero && !rowBisZero) {
          bCnt.inc()
          when(bCnt.value === ptrST_B.io.out.bits - 1.U) {
            shapeTransformer_A.io.out.ready := true.B
            bCnt.value := 0.U
            aCnt.inc()
            when(aCnt.value === ptrST_A.io.out.bits - 1.U) {
              aCnt.value := 0.U
              shapeTransformer_B.io.out.ready := true.B
              ptrST_A.io.out.ready := true.B
              ptrST_B.io.out.ready := true.B
            }
          }
        }.elsewhen(colAisZero && !rowBisZero && shapeTransformer_B.io.out.valid) {
          shapeTransformer_B.io.out.ready := true.B
          ptrST_A.io.out.ready := true.B
          ptrST_B.io.out.ready := true.B

        }.elsewhen(rowBisZero && !colAisZero && shapeTransformer_A.io.out.valid) {
          shapeTransformer_A.io.out.ready := true.B
          aCnt.inc()
          when(aCnt.value === ptrST_A.io.out.bits - 1.U) {
            aCnt.value := 0.U
            shapeTransformer_B.io.out.ready := true.B
            ptrST_A.io.out.ready := true.B
            ptrST_B.io.out.ready := true.B
          }
        }.elsewhen(colAisZero && rowBisZero) {
          ptrST_A.io.out.ready := true.B
          ptrST_B.io.out.ready := true.B
        }
      }

      when(outCnt_a.value === io.segSize && outCnt_b.value === io.segSize) {
        io.eop := true.B
//        io.lastOut := true.B
        state := sIdle
      }
    }
  }
}
