package tensorKernels

import chisel3.util.{ArbiterCtrl, Decoupled, log2Ceil}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import interfaces.CooDataBundle

//private object ArbiterController {
//  def apply(request: Seq[Bool]): Seq[Bool] = request.length match {
//    case 0 => Seq()
//    case 1 => Seq(true.B)
//    case _ => true.B +: request.tail.init.scanLeft(request.head)(_ || _).map(!_)
//  }
//}

class WeightedArbiterIO(n: Int)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {

    val in  = Flipped(Vec(n, Decoupled(new CooDataBundle(UInt(p(XLEN).W)))))
    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val chosen = Output(UInt(log2Ceil(n).W))

    val eopIn = Vec(n, Input(Bool( )))
    val lastIn = Vec(n, Input(Bool( )))

    val eopOut = Output(Bool( ))
    val lastOut = Output(Bool( ))
  })
}

class WeightedArbiter(n: Int)(implicit p: Parameters)
  extends WeightedArbiterIO(n)(p) {

  var chosen = n-1
  io.chosen := (n-1).asUInt
  io.eopOut := io.eopIn(n-1)
  io.lastOut := io.lastIn(n-1)
  io.out.bits := io.in(n-1).bits

  val isFinished = for (i <- 0 until n) yield {
    val eop = RegInit(init = false.B)
    eop
  }

  for (i <- 0 until n) {
    when (io.eopIn(i)) {isFinished(i) := true.B}
  }

  when(isFinished.reduceLeft(_&&_)) {isFinished.foreach(a => a := false.B)}
  val valid = Wire(Vec(n, Bool( )))
  for (i <- 0 until n) {
    valid(i) := isFinished(i) || io.in(i).valid
  }

  val grant = Wire(Vec(n, Bool( )))
  grant.foreach(a => a := false.B)
  grant(n-1) := true.B

  for (i <- n-1 to 0 by -1) {
    when (valid.reduceLeft(_&&_) && io.in(i).bits.row < io.in(chosen).bits.row && !isFinished(i)) {
      grant.foreach(a => a := false.B)
      grant(i) := true.B
      chosen = i
      io.chosen := i.asUInt
      io.out.bits := io.in(i).bits
      io.eopOut := io.eopIn(i)
      io.lastOut := io.lastIn(i)
    }
  }

  for ((in, g) <- io.in zip grant)
    in.ready := g && io.out.ready && valid.reduceLeft(_&&_)

  io.out.valid := valid.reduceLeft(_&&_)

}