package tensorKernels

import chisel3.util.Decoupled
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import dnn.types.OperatorNRSCAL
import interfaces.CooDataBundle
import node.{Shapes, vecN}

class MergeAddIO(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val eopIn = Input(Bool( ))
    val in = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val eopOut = Output(Bool( ))
  })
}

class MergeAdd[L <: Shapes : OperatorNRSCAL](maxStreamLen: Int, ID: Int, rowBased: Boolean)(shape: => L)(implicit p: Parameters)
  extends MergeAddIO()(p) {

  /*===============================================*
   *                Connections                    *
   *===============================================*/

  val merger = Module(new MergeSort(maxStreamLen = maxStreamLen, ID = 1, rowBased = rowBased))
  val adder = Module(new Adder(ID = 1)(shape))

  val data = RegInit(CooDataBundle.default(0.U(p(XLEN).W)))
  val valid = RegInit(false.B)
//  val valid = RegNext(io.in.valid)
  val lastR = RegInit(false.B)

  when(io.eopIn) {
    lastR := true.B
  }
  when(merger.io.in.ready && lastR) {
    lastR := false.B
  }
  when(merger.io.in.ready && io.in.valid) {
    data <> io.in.bits
//    valid := io.in.valid
  }
  when(merger.io.in.ready) {
    valid := io.in.valid
  }


  merger.io.lastIn := merger.io.in.ready && lastR
  merger.io.eopIn := false.B
  when((io.in.bits.row =/= data.row && io.in.valid) || (merger.io.in.ready && lastR)) {
    merger.io.eopIn := true.B
  }

  io.in.ready := merger.io.in.ready
  merger.io.in.bits := data
  merger.io.in.valid := valid

  adder.io.eopIn := merger.io.lastOut
//  adder.io.eopIn := merger.io.eopOut
  adder.io.in <> merger.io.out

  /* ================================================================== *
     *                        Adder & out                               *
     * ================================================================== */

  io.out <> adder.io.out
  io.eopOut := adder.io.eopOut

}