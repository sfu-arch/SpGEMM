package tensorKernels

import chisel3.util.{Decoupled, log2Ceil}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import interfaces.CooDataBundle


class MinArbiterIO(n: Int)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {

    val in  = Flipped(Vec(n, Decoupled(new CooDataBundle(UInt(p(XLEN).W)))))
    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val chosen = Output(UInt(log2Ceil(n).W))
    val active = Input(Bool ())

//    val eopIn = Vec(n, Input(Bool( )))
//    val lastIn = Vec(n, Input(Bool( )))
//
//    val eopOut = Output(Bool( ))
//    val lastOut = Output(Bool( ))
  })
}

class MinArbiter(n: Int)(implicit p: Parameters)
  extends MinArbiterIO(n)(p) {

  var chosen = n-1
  io.chosen := (n-1).asUInt
//  io.eopOut := io.eopIn(n-1)
//  io.lastOut := io.lastIn(n-1)
  io.out.bits := io.in(n-1).bits




  val grant = Wire(Vec(n, Bool( )))
  grant.foreach(a => a := false.B)
  grant(n-1) := true.B

  for (i <- n-1 to 0 by -1) {
    when (io.active && io.in(i).valid &&
      ((io.in(chosen).valid && io.in(i).bits.row < io.in(chosen).bits.row) || (!io.in(chosen).valid))  ) {
      grant.foreach(a => a := false.B)
      grant(i) := true.B
      chosen = i
      io.chosen := i.asUInt
      io.out.bits := io.in(i).bits
//      io.eopOut := io.eopIn(i)
//      io.lastOut := io.lastIn(i)
    }
  }

  for ((in, g) <- io.in zip grant)
    in.ready := g && io.out.ready && io.active && io.in.map(_.valid).reduceLeft(_||_)

  io.out.valid := io.active && io.in.map(_.valid).reduceLeft(_||_)

}