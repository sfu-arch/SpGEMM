
package dnn.memory

import chisel3._
import chisel3.util._
import config._
import dnnnode.MIMOQueue
import interfaces.CooDataBundle
import shell._
//import vta.util.config._
import dnn.memory.ISA._


/** StreamLoad.
  *
  * Load 1D and 2D tensors from main memory (DRAM) to input/weight
  * scratchpads (SRAM). Also, there is support for zero padding, while
  * doing the load. Zero-padding works on the y and x axis, and it is
  * managed by TensorPadCtrl. The TensorDataCtrl is in charge of
  * handling the way tensors are stored on the scratchpads.
  */
class StreamLoad(bufSize: Int, tensorType: String = "none", debug: Boolean = false)(
  implicit p: Parameters)
  extends Module {
  val tp = new TensorParams(tensorType)
  val mp = p(ShellKey).memParams
  val io = IO(new Bundle {
    val start = Input(Bool())
    val done = Output(Bool())
    val inst = Input(UInt(INST_BITS.W))
    val baddr = Input(UInt(mp.addrBits.W))
    val vme_rd = new VMEReadMaster
    val out = Decoupled(UInt(p(XLEN).W))
  })

//  val memBlockBits = mp.dataBits
//  val width =  memBlockBits / p(XLEN)
  require(bufSize > math.pow(2, mp.lenBits) * tp.tensorWidth, "bufSize should be greater than size of each stream chunk")


//  val sizeFactor = 1 //width / width
//  val strideFactor = 2//width //tp.tensorLength * tp.tensorWidth
  val sizeFactor = tp.tensorLength * tp.numMemBlock
  val strideFactor = tp.tensorLength * tp.tensorWidth

  val dec = io.inst.asTypeOf(new MemDecode)
  val dataCtrl = Module(
    new TensorDataCtrl(tensorType, sizeFactor, strideFactor))
  val dataCtrlDone = RegInit(false.B)

  val tag = Reg(UInt(log2Ceil(tp.numMemBlock).W))
  val set = Reg(UInt(log2Ceil(tp.tensorLength).W))


  val queue = Module(new MIMOQueue(UInt(p(XLEN).W), entries = bufSize, tp.tensorWidth, NumOuts = 1))
  queue.io.clear := false.B

  val sIdle :: sReadCmd :: sReadData :: Nil =
    Enum(3)
  val state = RegInit(sIdle)

  // control
  switch(state) {
    is(sIdle) {
      when(io.start) {
        state := sReadCmd
      }
    }
    is(sReadCmd) {
      when(io.vme_rd.cmd.fire()) {
        state := sReadData
      }
    }
    is(sReadData) {
      when(io.vme_rd.data.valid) {
        when(dataCtrl.io.done) {
          state := sIdle
        }.elsewhen(dataCtrl.io.stride || dataCtrl.io.split) {
          state := sReadCmd
        }
      }
    }
  }

  // data controller
  dataCtrl.io.start := state === sIdle & io.start
  dataCtrl.io.inst := io.inst
  dataCtrl.io.baddr := io.baddr
  dataCtrl.io.xinit := io.vme_rd.cmd.fire()
  dataCtrl.io.xupdate := io.vme_rd.data.fire()
  dataCtrl.io.yupdate := io.vme_rd.data.fire()

  when(state === sIdle) {
    dataCtrlDone := false.B
  }.elsewhen(io.vme_rd.data.fire() && dataCtrl.io.done) {
    dataCtrlDone := true.B
  }

  val reqSize = Wire(UInt(p(XLEN).W))
  reqSize := (dataCtrl.io.len * tp.tensorWidth.U) + tp.tensorWidth.U
  val check = Wire(Bool ())
  check := false.B
  when(reqSize < (bufSize.U - queue.io.count)) {
    check := true.B
  }

  // read-from-dram
  io.vme_rd.cmd.valid := (state === sReadCmd) && check
  io.vme_rd.cmd.bits.addr := dataCtrl.io.addr
  io.vme_rd.cmd.bits.len := dataCtrl.io.len

  io.vme_rd.data.ready := queue.io.enq.ready && state === sReadData

  // write-to-sram

  queue.io.enq.bits := io.vme_rd.data.bits.asTypeOf(queue.io.enq.bits)
  queue.io.enq.valid := io.vme_rd.data.valid

  io.out.bits := queue.io.deq.bits.asUInt()
  io.out.valid := queue.io.deq.valid
  queue.io.deq.ready := io.out.ready

  // done
  val done_no_pad = io.vme_rd.data.fire() & dataCtrl.io.done & dec.xpad_1 === 0.U & dec.ypad_1 === 0.U
  io.done := done_no_pad
}
