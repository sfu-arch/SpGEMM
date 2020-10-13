package accel

import FPU.FType
import chisel3.util._
import chisel3.{when, _}
import config._
import node.{FPvecN, matNxN, vecN}
import shell._
import tensorKernels.{SpMM, SpMM_Block, URAM_Queue}

/** SparseTensorCore.
  *
  * SparseTensorCore is able to perform the linear algebraic computations on sparse tensors.
  */
class SpTensorCore(numSegment: Int, numSorter: Int, numVC: Int, VCDepth: Int, maxRowLen: Int, maxColLen: Int)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val vcr = new VCRClient
    val vme = new VMEMaster
  })

  val cycle_count = new Counter(100000000)


  val S = new FType(8, 24)
  val shape = new FPvecN(1, S, 0)
//  val shape = new vecN(1, 0, false)

//  val block = Module(new SpMM_Block(numSegments = numSegment, numReducer = numReducer, numVC = numVC, VCDepth = VCDepth, maxRowLen = maxRowLen, maxColLen = maxColLen)(shape))
  val block = Module(new SpMM(numSegments = numSegment, numSorter = numSorter, numVC = numVC, VCDepth = VCDepth, sorterDepth = maxRowLen)(shape))

//  val uram = Module(new UltraRAM())
//  val queue = Module(new URAM_Queue(UInt(72.W), entries = 10, pipe = true))

  val sIdle :: sExec :: sFinish :: Nil = Enum(3)

  val state = RegInit(sIdle)
  /* ================================================================== *
     *                      Basic Block signals                         *
     * ================================================================== */
  for (i <- 0 until numSegment) {
    block.io.nnz_A(i) := io.vcr.vals(i * 3 + 0)
    block.io.nnz_B(i) := io.vcr.vals(i * 3 + 1)
    block.io.segSize(i) := io.vcr.vals(i * 3 + 2)

  }

//  queue.io.enq.bits := cycle_count.value
//  queue.io.enq.valid := state === sExec

//  queue.io.deq.ready := state === sExec


  /* ================================================================== *
     *                           Connections                            *
     * ================================================================== */

  val mulTime = RegInit(0.U)
  when(block.io.multiplicationDone) {
    mulTime := cycle_count.value
  }

  val inStreamingTime = RegInit(0.U)
  when(block.io.inStreamingDone) {
    inStreamingTime := cycle_count.value
  }

  io.vcr.ecnt(0).bits := cycle_count.value
  io.vcr.ecnt(1).bits := mulTime
  io.vcr.ecnt(2).bits := inStreamingTime

  for (i <- 0 until numSorter) {
    io.vcr.ecnt(i+3).bits := block.io.outDMA_len(i)
  }

  /* ================================================================== *
    *                    VME Reads and writes                           *
    * ================================================================== */

  block.io.start := false.B

  for (i <- 0 until numSegment) {
    io.vme.rd(6 * i + 0) <> block.io.vme_rd_ptr(2 * i + 0)
    io.vme.rd(6 * i + 1) <> block.io.vme_rd_ind(2 * i + 0)
    io.vme.rd(6 * i + 2) <> block.io.vme_rd_val(2 * i + 0)

    io.vme.rd(6 * i + 3) <> block.io.vme_rd_ptr(2 * i + 1)
    io.vme.rd(6 * i + 4) <> block.io.vme_rd_ind(2 * i + 1)
    io.vme.rd(6 * i + 5) <> block.io.vme_rd_val(2 * i + 1)

    block.io.ptr_A_BaseAddr(i) := io.vcr.ptrs(i * 6 + 0)
    block.io.ind_A_BaseAddr(i) := io.vcr.ptrs(i * 6 + 1)
    block.io.val_A_BaseAddr(i) := io.vcr.ptrs(i * 6 + 2)

    block.io.ptr_B_BaseAddr(i) := io.vcr.ptrs(i * 6 + 3)
    block.io.ind_B_BaseAddr(i) := io.vcr.ptrs(i * 6 + 4)
    block.io.val_B_BaseAddr(i) := io.vcr.ptrs(i * 6 + 5)
  }

  for (i <- 0 until numSorter) {
    io.vme.wr(3 * i + 0) <> block.io.vme_wr_row(i)
    io.vme.wr(3 * i + 1) <> block.io.vme_wr_col(i)
    io.vme.wr(3 * i + 2) <> block.io.vme_wr_val(i)

    block.io.outBaseAddr_row(i) := io.vcr.ptrs((numSegment * 6) + (3 * i) + 0)
    block.io.outBaseAddr_col(i) := io.vcr.ptrs((numSegment * 6) + (3 * i) + 1)
    block.io.outBaseAddr_val(i) := io.vcr.ptrs((numSegment * 6) + (3 * i) + 2)
  }


  switch(state) {
    is(sIdle) {
      when(io.vcr.launch) {
        block.io.start := true.B
        state := sExec
      }
    }
    is(sExec) {
      when(block.io.done) {
        state := sIdle
      }
    }
  }

  val last = state === sExec && block.io.done
  io.vcr.finish := last
  io.vcr.ecnt.map(_.valid).foreach(a => a := last)

  when(state =/= sIdle) {
    cycle_count.inc()
  }
}
