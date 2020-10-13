
package tensorKernels


import chisel3.util.{Counter, Decoupled, Queue, isPow2}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import interfaces.{BoolBundle, CooDataBundle, CustomDataBundle, DataBundle, ValidT}
import muxes.{Demux, Mux}

class MergeNodeIO()(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val eopIn = Input(Bool( ))
    val lastIn = Input(Bool( ))
    val eopOut = Output(Bool( ))
    val lastOut = Output(Bool( ))
    val in1 = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val in2 = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val out1 = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val out2 = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
  })
}

class MergeNode(level: Int, ID: Int, rowBased: Boolean, lastLevel: Int)(implicit p: Parameters)
  extends MergeNodeIO()(p) {
  require(level > 0, "Level must be greater than zero")
  require(isPow2(level), "Level must be power of two")

  val queue1 = Module(new Queue(new CooDataBundle(UInt(p(XLEN).W)), entries = level, pipe = true))
  val queue2 = Module(new Queue(new CooDataBundle(UInt(p(XLEN).W)), entries = level, pipe = true))

  val popCnt1 = Counter(level + 1)
  val popCnt2 = Counter(level + 1)

  val flushing = RegInit(false.B)
  val last = RegInit(false.B)

  when(io.eopIn) {
    flushing := true.B
    when(io.lastIn){last := true.B}
  }

  val mux = Module(new Mux(new CooDataBundle(UInt(p(XLEN).W)), Nops = 2))
  mux.io.en := false.B
  mux.io.sel := 0.U
  val demux = Module(new Demux(new CooDataBundle(UInt(p(XLEN).W)), Nops = 2))
  demux.io.en := mux.io.output.valid
  demux.io.sel := 0.U

  val demuxSel = RegInit(false.B)
  demux.io.sel := demuxSel

  /*===============================================*
   *                ready Mux/Demux                *
   *===============================================*/
  val readyMux = Module(new Mux(new BoolBundle(Bool()), Nops = 2))
  val readyDemux = Module(new Demux(BoolBundle(Bool()), Nops = 2))
  readyMux.io.en := true.B
  readyDemux.io.en := mux.io.en
  readyMux.io.sel := demuxSel
  readyDemux.io.sel := 0.U

  readyMux.io.inputs(0).data := io.out1.ready
  readyMux.io.inputs(0).valid := true.B
  readyMux.io.inputs(1).data := io.out2.ready
  readyMux.io.inputs(1).valid := true.B

  readyDemux.io.input := readyMux.io.output
  queue1.io.deq.ready := readyDemux.io.outputs(0).valid && readyDemux.io.outputs(0).data
  queue2.io.deq.ready := readyDemux.io.outputs(1).valid && readyDemux.io.outputs(1).data


  io.out1.valid := demux.io.outputs(0).valid
  io.out2.valid := demux.io.outputs(1).valid

  /*===============================================*
   *                Connections                    *
   *===============================================*/

  queue1.io.enq <> io.in1
  queue1.io.enq.valid := io.in1.valid && !flushing
  queue2.io.enq <> io.in2
  queue2.io.enq.valid := io.in2.valid && !flushing

  io.in1.ready := queue1.io.enq.ready && !flushing
  io.in2.ready := queue2.io.enq.ready && !flushing

  mux.io.inputs(0) <> queue1.io.deq.bits
  mux.io.inputs(1) <> queue2.io.deq.bits

  io.out1.bits <> demux.io.outputs(0)
  io.out2.bits <> demux.io.outputs(1)
  demux.io.input <> mux.io.output

  if(rowBased){
    when(queue1.io.deq.valid && queue2.io.deq.valid){
      mux.io.en := true.B
      when((popCnt1.value < level.U) && (popCnt2.value === level.U ||
        ((queue1.io.deq.bits.row < queue2.io.deq.bits.row) ||
          ((queue1.io.deq.bits.row === queue2.io.deq.bits.row) && (queue1.io.deq.bits.col <= queue2.io.deq.bits.col))) )) {
        mux.io.sel := 0.U
        readyDemux.io.sel := 0.U
      }.elsewhen((popCnt2.value < level.U) && (popCnt1.value === level.U ||
        ((queue1.io.deq.bits.row > queue2.io.deq.bits.row) ||
          ((queue1.io.deq.bits.row === queue2.io.deq.bits.row) && (queue1.io.deq.bits.col > queue2.io.deq.bits.col))) )){
        mux.io.sel := 1.U
        readyDemux.io.sel := 1.U
      }
    }.elsewhen(queue1.io.deq.valid && !queue2.io.deq.valid && (flushing || popCnt2.value === level.U)){
      mux.io.en := true.B
      mux.io.sel := 0.U
      readyDemux.io.sel := 0.U
    }.elsewhen(!queue1.io.deq.valid && queue2.io.deq.valid && (flushing || popCnt1.value === level.U)){
      mux.io.en := true.B
      mux.io.sel := 1.U
      readyDemux.io.sel := 1.U
    }
  } else {
    when(queue1.io.deq.valid && queue2.io.deq.valid){
      mux.io.en := true.B
      when((popCnt1.value < level.U) && ((queue1.io.deq.bits.col <= queue2.io.deq.bits.col) || popCnt2.value === level.U)) {
        mux.io.sel := 0.U
        readyDemux.io.sel := 0.U
      }.elsewhen((popCnt2.value < level.U) && ((queue1.io.deq.bits.col > queue2.io.deq.bits.col) || popCnt1.value === level.U)){
        mux.io.sel := 1.U
        readyDemux.io.sel := 1.U
      }
    }.elsewhen(queue1.io.deq.valid && !queue2.io.deq.valid && (flushing || popCnt2.value === level.U)){
      mux.io.en := true.B
      mux.io.sel := 0.U
      readyDemux.io.sel := 0.U
    }.elsewhen(!queue1.io.deq.valid && queue2.io.deq.valid && (flushing || popCnt1.value === level.U)){
      mux.io.en := true.B
      mux.io.sel := 1.U
      readyDemux.io.sel := 1.U
    }
  }

    when(queue1.io.deq.fire()) {
    popCnt1.inc()
   when(popCnt2.value + popCnt1.value === (2 * level).U -1.U){
      demuxSel := !demuxSel
      popCnt1.value := 0.U
      popCnt2.value := 0.U
    }
  }

  when(queue2.io.deq.fire()) {
    popCnt2.inc()
    when(popCnt2.value + popCnt1.value === (2 * level).U -1.U){
      demuxSel := !demuxSel
      popCnt1.value := 0.U
      popCnt2.value := 0.U
    }
  }


  if(lastLevel == level) {
    demuxSel := false.B
  }

  io.eopOut := false.B
  io.lastOut := false.B

  when(!queue1.io.deq.valid && !queue2.io.deq.valid && flushing) {
    flushing := false.B
    io.eopOut := true.B
    demuxSel := false.B
    popCnt1.value := 0.U
    popCnt2.value := 0.U
    when(last){
      io.lastOut := true.B
      last := false.B
    }
  }
}