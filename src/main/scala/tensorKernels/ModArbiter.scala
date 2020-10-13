package tensorKernels

import chisel3.util.{Decoupled, log2Ceil}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, ROWLEN, XLEN}
import interfaces.{BoolBundle, CooDataBundle}
import muxes.{Demux, Mux}


class ModArbiterIO(numIns: Int, numOuts: Int)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {

    val in  = Flipped(Vec(numIns, Decoupled(new CooDataBundle(UInt(p(XLEN).W)))))
    val out = Vec(numOuts, Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
//    val chosen = Vec(numOuts, Output(UInt(log2Ceil(numIns).W)))
    val activate = Input(Bool( ))

    val eopIn = Vec(numIns, Input(Bool( )))

    val eopOut = Vec(numOuts, Output(Bool( )))
  })
}

class ModArbiter(numIns: Int, numOuts: Int)(implicit p: Parameters)
  extends ModArbiterIO(numIns, numOuts)(p) {

  val arbiter = for (i <- 0 until numOuts) yield {
    val arb = Module(new MinArbiter(n = numIns))
    arb
  }

  val demux = for (i <- 0 until numIns) yield {
    val dem = Module(new Demux(new CooDataBundle(UInt(p(XLEN).W)), Nops = numOuts))
    dem
  }
  val readyMux = for (i <- 0 until numIns) yield {
    val mux = Module(new Mux(new BoolBundle(Bool()), Nops = numOuts))
    mux
  }

  /* ================================================================== *
    *                         isFinished signals                       *
    * ================================================================== */

  val isFinished = for (i <- 0 until numIns) yield {
    val eop = RegInit(init = false.B)
    eop
  }

  for (i <- 0 until numIns) {
    when (io.eopIn(i)) {isFinished(i) := true.B}
  }


  io.eopOut.foreach(a => a := false.B)

  when(isFinished.reduceLeft(_&&_)) {
    isFinished.foreach(a => a := false.B)
    io.eopOut.foreach(a => a := true.B)
  }

  val active = Wire(Vec(numIns, Bool( )))
  for (i <- 0 until numIns) {
    active(i) := isFinished(i) || io.in(i).valid
  }

  /* ================================================================== *
   *                       calculating minQuotient                      *
   * ================================================================== */

//  val quotient = Vec(numIns, Wire(UInt(p(ROWLEN).W)))
  val quotient = Wire(Vec(numIns, UInt(p(ROWLEN).W)))
  for (i <- 0 until numIns) {
    quotient(i) := io.in(i).bits.row / numOuts.U
  }

  var min = numIns-1
  val minQuotient = Wire(UInt(p(ROWLEN).W))
  minQuotient := quotient(min)

  for (i <- numIns-1 to 0 by -1) {
    when (((quotient(i) < quotient(min) && io.in(min).valid) || !io.in(min).valid) && io.in(i).valid && !isFinished(i)) {
      min = i
      minQuotient := quotient(i)
    }
  }
  /* ================================================================== *
     *                         isFinished signals                       *
     * ================================================================== */


  for (i <- 0 until numOuts) {
//    arbiter(i).io.active := active.reduceLeft(_&&_)
    arbiter(i).io.active := io.activate
  }

  for (i <- 0 until numOuts) {
    for (j <- 0 until numIns) {
      arbiter(i).io.in(j).bits := demux(j).io.outputs(i)
      arbiter(i).io.in(j).valid := demux(j).io.outputs(i).valid
      readyMux(j).io.inputs(i).valid := true.B
      readyMux(j).io.inputs(i).data := arbiter(i).io.in(j).ready
    }
  }


  for (i <- 0 until numIns) {
    io.in(i).ready := readyMux(i).io.output.data && readyMux(i).io.output.valid
    demux(i).io.input := io.in(i).bits
//    demux(i).io.en := io.in(i).valid && active.reduceLeft(_&&_) && (quotient(i) === minQuotient)
    demux(i).io.en := io.in(i).valid && io.activate && (quotient(i) === minQuotient)
    readyMux(i).io.en := demux(i).io.en
    demux(i).io.sel := io.in(i).bits.row % numOuts.U
    readyMux(i).io.sel := io.in(i).bits.row % numOuts.U
  }

  /* ================================================================== *
      *                         isFinished signals                       *
      * ================================================================== */

  for (i <- 0 until numOuts) {
    io.out(i) <> arbiter(i).io.out
  }






}
