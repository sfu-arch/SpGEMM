package tensorKernels

import chisel3._
import config._
import dnn.memory._
import dnn.types.{OperatorCooSCAL, OperatorDot, OperatorNRSCAL, OperatorReduction}
import node.Shapes
import shell._


/** Sparse Matrix-Matrix Multiplication.
  *
  */

class SpMM_IO(numSegments: Int, numSorter: Int)(implicit val p: Parameters)
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

    val outBaseAddr_row = Vec(numSorter, Input(UInt(mp.addrBits.W)))
    val outBaseAddr_col = Vec(numSorter, Input(UInt(mp.addrBits.W)))
    val outBaseAddr_val = Vec(numSorter, Input(UInt(mp.addrBits.W)))

    val nnz_A = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val nnz_B = Vec(numSegments, Input(UInt(mp.addrBits.W)))
    val segSize = Vec(numSegments, Input(UInt(mp.addrBits.W)))

    val vme_rd_ptr = Vec(2 * numSegments, new VMEReadMaster)
    val vme_rd_ind = Vec(2 * numSegments, new VMEReadMaster)
    val vme_rd_val = Vec(2 * numSegments, new VMEReadMaster)

    val vme_wr_row = Vec(numSorter, new VMEWriteMaster)
    val vme_wr_col = Vec(numSorter, new VMEWriteMaster)
    val vme_wr_val = Vec(numSorter, new VMEWriteMaster)

    val multiplicationDone = Output(Bool ( ))
    val inStreamingDone = Output(Bool ( ))


    val outDMA_len = Vec(numSorter, Output(UInt(mp.addrBits.W)))
  })
}

class SpMM[L <: Shapes : OperatorDot : OperatorReduction : OperatorNRSCAL : OperatorCooSCAL]
(numSegments: Int, numSorter: Int, numVC: Int, VCDepth: Int, sorterDepth: Int)
(segShape: => L)(implicit p: Parameters)
  extends SpMM_IO(numSegments, numSorter)(p) {

  val seg = for (i <- 0 until numSegments) yield {
    val outDot = Module(new OuterDot(memTensorType = "inp", maxRowLen = sorterDepth)(segShape))
    outDot
  }

  val VC = for (i <- 0 until numSegments) yield {
    val channel = Module(new VirtualChannel(N = numVC, VCDepth = VCDepth))
    channel
  }

  val allocator = Module(new Allocator(numIns = numSegments * numVC, numOuts = numSorter))

  val sorter = for (i <- 0 until numSorter) yield {
    val sortNode = Module(new MergeSort(maxStreamLen = sorterDepth, ID = 1, rowBased = true))
    sortNode
  }

  val reducer = for (i <- 0 until numSorter) yield {
    val adder = Module(new Adder(ID = 1)(segShape))
    adder
  }

  val outDMA = for (i <- 0 until numSorter) yield {
    val outD = Module(new outDMA_coo(bufSize = 20, memTensorType = "out"))
    outD
  }

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

    VC(i).io.in <> seg(i).io.out
    seg(i).io.out.ready := true.B
    VC(i).io.eopIn := seg(i).io.eop


//    sorter(i).io.in <> seg(i).io.out
//    sorter(i).io.eopIn := seg(i).io.eop

//    VC(i).io.in <> sorter(i).io.out
//    VC(i).io.eopIn := sorter(i).io.eopOut

//    arbiter.io.in(i) <> row_merger(i).io.out
//    arbiter.io.eopIn(i) := row_merger(i).io.eopOut
  }

  /*val isFinished = for (i <- 0 until numSegments) yield {
    val eop = RegInit(init = false.B)
    eop
  }
  when(isFinished.reduceLeft(_&&_) && !VC.map(_.io.out.map(_.valid).reduceLeft(_||_)).reduceLeft(_||_)) {
    isFinished.foreach(a => a := false.B)
  }

  for (i <- 0 until numSegments) {
    when (sorter(i).io.eopOut) {isFinished(i) := true.B}
  }*/

//  val active = Wire(Vec(numSegments, Bool( )))
//  for (i <- 0 until numSegments) {
//    active(i) := isFinished(i) || sorter(i).io.out.valid
//  }

//  allocator.io.activate := active.reduceLeft(_&&_)

  for (i <- 0 until numSegments) {
    for (j <- 0 until numVC) {
      allocator.io.in(i * numVC + j) <> VC(i).io.out(j)
      allocator.io.eopIn(i * numVC + j) := VC(i).io.eopOut(j)
    }
  }

  for (i <- 0 until numSorter) {
    sorter(i).io.in <> allocator.io.out(i)
    sorter(i).io.eopIn := allocator.io.eopOut(i)
    sorter(i).io.lastIn := allocator.io.eopOut(i)

    io.outDMA_len(i) := outDMA(i).io.outLen

    io.vme_wr_row(i) <> outDMA(i).io.vme_wr_row
    io.vme_wr_col(i) <> outDMA(i).io.vme_wr_col
    io.vme_wr_val(i) <> outDMA(i).io.vme_wr_val

    outDMA(i).io.baddr_row := io.outBaseAddr_row(i)
    outDMA(i).io.baddr_col := io.outBaseAddr_col(i)
    outDMA(i).io.baddr_val := io.outBaseAddr_val(i)

    reducer(i).io.in <> sorter(i).io.out
    reducer(i).io.eopIn := sorter(i).io.eopOut

    outDMA(i).io.in <> reducer(i).io.out
    outDMA(i).io.last := reducer(i).io.eopOut
  }

  /* ================================================================== *
    *                  last signal of Col_mergers                       *
    * ================================================================== */
  val last = for (i <- 0 until numSorter) yield {
    val lastReg = RegInit(init = false.B)
    lastReg
  }

  for (i <- 0 until numSorter) yield{
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
  val doneR = for (i <- 0 until numSorter) yield {
    val doneReg = RegInit(init = false.B)
    doneReg
  }
//  io.done := doneR.reduceLeft(_ && _)
  io.done := segDone.reduceLeft(_ && _)

  when (doneR.reduceLeft(_ && _)) {
    doneR.foreach(a => a := false.B)
  }

  for (i <- 0 until numSorter) yield{
    when (outDMA(i).io.done) {
      doneR(i) := true.B
    }
  }
}
