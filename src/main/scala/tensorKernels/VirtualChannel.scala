package tensorKernels

import chisel3.util.{Counter, Decoupled, Queue, RRArbiter}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, ROWLEN, XLEN}
import interfaces.{BoolBundle, CooDataBundle}
import muxes.{Demux, Mux}
import shell.VMECmd


class VirtualChannelIO(N: Int)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {

    val in  = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val out = Vec(N, Decoupled(new CooDataBundle(UInt(p(XLEN).W))))

    val eopIn = Input(Bool( ))
    val eopOut = Vec(N, Output(Bool( )))
  })
}

class VirtualChannel(N: Int, VCDepth: Int)(implicit p: Parameters)
  extends VirtualChannelIO(N)(p) {
  require(N > 0, "Number of VCs should be at least 1")

  val queue = for (i <- 0 until N) yield {
    val queue1 = Module(new Queue(new CooDataBundle(UInt(p(XLEN).W)), entries = VCDepth, pipe = true))
    queue1
  }

  val data = RegInit(CooDataBundle.default(0.U(p(XLEN).W)))
  val valid = RegInit(false.B)

  val demux = Module(new Demux(new CooDataBundle(UInt(p(XLEN).W)), Nops = N))

  val readyMux = Module(new Mux(new BoolBundle(Bool()), Nops = N))

  /* ================================================================== *
    *                         isFinished signals                       *
    * ================================================================== */

  val isFinished = RegInit(init = false.B)

  val sel = Counter(N)


    when (data.row =/= io.in.bits.row && io.in.valid) {
      sel.inc()
    }


  demux.io.sel := sel.value
  readyMux.io.sel := sel.value

  demux.io.en := valid
  readyMux.io.en := valid

  when(valid && readyMux.io.output.data) {
    valid := false.B
  }

  when(io.in.fire()) {
    data <> io.in.bits
    valid := io.in.valid
  }

  dontTouch(data)

  demux.io.input := data

  for (i <- 0 until N) {
    readyMux.io.inputs(i).valid := true.B
    readyMux.io.inputs(i).data := queue(i).io.enq.ready

    queue(i).io.enq.bits := demux.io.outputs(i)
    queue(i).io.enq.valid := valid && demux.io.outputs(i).valid

    io.out(i) <> queue(i).io.deq
  }


  when (io.eopIn) {isFinished := true.B}
  io.eopOut.foreach(a => a := false.B)
  when(isFinished && !queue.map(_.io.deq.valid).reduceLeft(_||_)) {
    isFinished := false.B
    io.eopOut.foreach(a => a := true.B)
  }

  io.in.ready := !isFinished && readyMux.io.output.data



}
