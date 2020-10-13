package tensorKernels

import chisel3._
import chisel3.util._
import config._
import dnn.memory._
import dnn.types.{OperatorCooSCAL, OperatorDot, OperatorNRSCAL, OperatorReduction}
import node.{Shapes, vecN}
import shell._


/** TensorLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class SpMM_BlockIO(numSegments: Int, numColMerger: Int)(implicit val p: Parameters)
  extends Module {

  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())

    val ind_A_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val val_A_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val ptr_A_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))

    val ind_B_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val val_B_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val ptr_B_BaseAddr = Vec(numSegments, Input(UInt(mp.addrBits.W)))

    val outBaseAddr_row = Vec(numColMerger, Input(UInt(mp.addrBits.W)))
    val outBaseAddr_col = Vec(numColMerger, Input(UInt(mp.addrBits.W)))
    val outBaseAddr_val = Vec(numColMerger, Input(UInt(mp.addrBits.W)))

    val nnz_A = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val nnz_B = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val segSize = Vec(numSegments, Input(UInt(mp.addrBits.W)))

    val vme_rd_ptr = Vec(2 * numSegments, new VMEReadMaster)
    val vme_rd_ind = Vec(2 * numSegments, new VMEReadMaster)
    val vme_rd_val = Vec(2 * numSegments, new VMEReadMaster)

    val vme_wr_row = Vec(numColMerger, new VMEWriteMaster)
    val vme_wr_col = Vec(numColMerger, new VMEWriteMaster)
    val vme_wr_val = Vec(numColMerger, new VMEWriteMaster)

    val multiplicationDone = Output(Bool ( ))
    val inStreamingDone = Output(Bool ( ))


    val outDMA_len = Vec(numColMerger, Output(UInt(mp.addrBits.W)))
  })
}

class SpMM_Block[L <: Shapes : OperatorDot : OperatorReduction : OperatorNRSCAL : OperatorCooSCAL]
(numSegments: Int, numReducer: Int, numVC: Int, VCDepth: Int, maxRowLen: Int, maxColLen: Int)
(segShape: => L)(implicit p: Parameters)
  extends SpMM_BlockIO(numSegments, numReducer)(p) {

  val seg = for (i <- 0 until numSegments) yield {
    val outDot = Module(new OuterDot(memTensorType = "inp", maxRowLen = maxRowLen)(segShape))
    outDot
  }

  val sorter = for (i <- 0 until numSegments) yield {
    val sortNode = Module(new MergeSort(maxStreamLen = maxRowLen, ID = 1, rowBased = true))
    sortNode
  }

  val VC = for (i <- 0 until numSegments) yield {
    val channel = Module(new VirtualChannel(N = numVC, VCDepth = VCDepth))
    channel
  }

  val arbiter = Module(new ModArbiter(numIns = numSegments * numVC, numOuts = numReducer))

  val reducer = for (i <- 0 until numReducer) yield {
    val colMerger = Module(new MergeAdd(maxStreamLen = maxColLen, ID = 1, rowBased = false)(segShape))
//    val adder = Module(new Adder(ID = 1)(segShape))
    colMerger
  }

  val outDMA = for (i <- 0 until numReducer) yield {
    val outD = Module(new outDMA_coo(bufSize = 20, memTensorType = "out"))
    outD
  }

//  val inDMA_time = Counter(2000)
//  val outDMA_time = Counter(2000)
//  val merge_time = Counter(2000)

//  io.inDMA_time := inDMA_time.value
//  io.merge_time := merge_time.value

  /* ================================================================== *
    *                      inDMA_acts & loadNodes                       *
    * ================================================================== */
  for (i <- 0 until numSegments) {
    seg(i).io.start := io.start

    seg(i).io.ind_A_BaseAddr := io.ind_A_BaseAddr(i)
    seg(i).io.ptr_A_BaseAddr := io.ptr_A_BaseAddr(i)
    seg(i).io.val_A_BaseAddr := io.val_A_BaseAddr(i)

    seg(i).io.ind_B_BaseAddr := io.ind_B_BaseAddr(i)
    seg(i).io.ptr_B_BaseAddr := io.ptr_B_BaseAddr(i)
    seg(i).io.val_B_BaseAddr := io.val_B_BaseAddr(i)

    seg(i).io.nnz_A := io.nnz_A(i)
    seg(i).io.nnz_B := io.nnz_B(i)
    seg(i).io.segSize := io.segSize(i)

    io.vme_rd_ind(2 * i + 0) <> seg(i).io.vme_rd_ind(0)
    io.vme_rd_ind(2 * i + 1) <> seg(i).io.vme_rd_ind(1)

    io.vme_rd_ptr(2 * i + 0) <> seg(i).io.vme_rd_ptr(0)
    io.vme_rd_ptr(2 * i + 1) <> seg(i).io.vme_rd_ptr(1)

    io.vme_rd_val(2 * i + 0) <> seg(i).io.vme_rd_val(0)
    io.vme_rd_val(2 * i + 1) <> seg(i).io.vme_rd_val(1)

    sorter(i).io.in <> seg(i).io.out
    sorter(i).io.eopIn := seg(i).io.eop
    sorter(i).io.lastIn := seg(i).io.eop

    VC(i).io.in <> sorter(i).io.out
    VC(i).io.eopIn := sorter(i).io.eopOut

//    arbiter.io.in(i) <> row_merger(i).io.out
//    arbiter.io.eopIn(i) := row_merger(i).io.eopOut
  }

  val isFinished = for (i <- 0 until numSegments) yield {
    val eop = RegInit(init = false.B)
    eop
  }
  when(isFinished.reduceLeft(_&&_) && !VC.map(_.io.out.map(_.valid).reduceLeft(_||_)).reduceLeft(_||_)) {
    isFinished.foreach(a => a := false.B)
  }
  for (i <- 0 until numSegments) {
    when (sorter(i).io.eopOut) {isFinished(i) := true.B}
  }

  val active = Wire(Vec(numSegments, Bool( )))
  for (i <- 0 until numSegments) {
    active(i) := isFinished(i) || sorter(i).io.out.valid
  }

  arbiter.io.activate := active.reduceLeft(_&&_)

  for (i <- 0 until numSegments) {
    for (j <- 0 until numVC) {
      arbiter.io.in(i * numVC + j) <> VC(i).io.out(j)
      arbiter.io.eopIn(i * numVC + j) := VC(i).io.eopOut(j)
    }
  }

  for (i <- 0 until numReducer) {
    io.outDMA_len(i) := outDMA(i).io.outLen

    io.vme_wr_row(i) <> outDMA(i).io.vme_wr_row
    io.vme_wr_col(i) <> outDMA(i).io.vme_wr_col
    io.vme_wr_val(i) <> outDMA(i).io.vme_wr_val

    outDMA(i).io.baddr_row := io.outBaseAddr_row(i)
    outDMA(i).io.baddr_col := io.outBaseAddr_col(i)
    outDMA(i).io.baddr_val := io.outBaseAddr_val(i)

    reducer(i).io.in <> arbiter.io.out(i)

    reducer(i).io.eopIn := arbiter.io.eopOut(i)
    outDMA(i).io.in <> reducer(i).io.out
    outDMA(i).io.last := reducer(i).io.eopOut
  }

  /* ================================================================== *
    *                  last signal of Col_mergers                       *
    * ================================================================== */
  val last = for (i <- 0 until numReducer) yield {
    val lastReg = RegInit(init = false.B)
    lastReg
  }

  for (i <- 0 until numReducer) yield{
    when (reducer(i).io.eopOut) {
      last(i) := true.B
    }
  }
  io.multiplicationDone := last.reduceLeft(_&&_)
  when (last.reduceLeft(_ && _)) {
    last.foreach(a => a := false.B)
  }

  /* ================================================================== *
    *                  Done signal of segments                          *
    * ================================================================== */
  val segDone = for (i <- 0 until numSegments) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }

  for (i <- 0 until numSegments) yield{
    when (seg(i).io.eop) {
      segDone(i) := true.B
    }
  }
  io.inStreamingDone := segDone.reduceLeft(_&&_)
  when (segDone.reduceLeft(_ && _)) {
    segDone.foreach(a => a := false.B)
  }

  /* ================================================================== *
    *         outDot -> row_merger -> col_merger -> outDMA              *
    * ================================================================== */
  val doneR = for (i <- 0 until numReducer) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }
  io.done := doneR.reduceLeft(_ && _)

  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
  }

  for (i <- 0 until numReducer) yield{
    when (outDMA(i).io.done) {
      doneR(i) := true.B
    }
  }
}
