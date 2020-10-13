package dnn

import chisel3.util.{Decoupled, Enum, Valid}
import chisel3.{Bundle, Flipped, Module, Output, RegInit, UInt, printf, when, _}
import config.{COLLEN, Parameters, ROWLEN, XLEN}
import dnn.types.{OperatorCooSCAL, OperatorSCAL}
import interfaces.{CooDataBundle, CustomDataBundle}
import node.Shapes

class CooSCALFU[L <: Shapes : OperatorCooSCAL](left: => L, lanes: Int, opCode: String)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(left))
    val b = Flipped(Valid(UInt(p(XLEN).W)))
    val o = Decoupled(left)
  })


  val start = io.o.ready && io.a.valid && io.b.valid
  val FU    = OperatorCooSCAL.magic(io.a.bits, io.b.bits, start, lanes, opCode)
  io.o.bits := FU._1
  val latency = FU._2
  val valid = RegInit(false.B)
  when(io.o.fire()){
    valid := false.B
  }

  when(start){
    valid := true.B
  }

  io.o.valid := valid
}

class CooSCALIO[L <: Shapes](left: => L)(implicit val p: Parameters) extends Module{
  val io = IO(new Bundle() {
    val vec = Vec(left.getLength(), Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W)))))

    val scal = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))

    val out = Vec(left.getLength(), Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
  })
}

class CooSCALNode[L <: Shapes : OperatorCooSCAL](N: Int, ID: Int, opCode: String)(shape: => L)(implicit p: Parameters)
  extends CooSCALIO(shape)(p) {

  require(shape.getLength() == N, "shape does not match with number of multipliers")

 /*===============================================*
   *            Latch inputs. Wire up left       *
   *===============================================*/
  val FU = Module(new CooSCALFU(shape, lanes = shape.getLength(), opCode))
  FU.io.a.bits := VecInit(io.vec.map(_.bits.data.asUInt())).asTypeOf(shape)
  FU.io.b.bits := io.scal.bits.data

  FU.io.a.valid := io.vec.map(_.valid).reduceLeft(_&&_)
  FU.io.b.valid := io.scal.valid

  FU.io.o.ready := io.out.map(_.ready).reduceLeft(_&&_)
  io.scal.ready := io.out.map(_.ready).reduceLeft(_&&_) && io.vec.map(_.valid).reduceLeft(_&&_)
  io.vec.map(_.ready).foreach(a => a := io.out.map(_.ready).reduceLeft(_&&_) && io.scal.valid)


  val row = for (i <- 0 until shape.getLength()) yield {
    val r = Reg(UInt(p(ROWLEN).W))
    r
  }
  val col = for (i <- 0 until shape.getLength()) yield {
    val c = Reg(UInt(p(COLLEN).W))
    c
  }

  for (i <- 0 until shape.getLength()) {
    io.out(i).bits.data := FU.io.o.bits.asUInt()(p(XLEN) * (i + 1) - 1, p(XLEN) * i)
    io.out(i).valid := FU.io.o.valid

    when(io.out.map(_.ready).reduceLeft(_&&_)) {
      row(i) := io.vec(i).bits.row
      col(i) := io.scal.bits.col
    }
    io.out(i).bits.row := row(i)
    io.out(i).bits.col := col(i)

    io.out(i).bits.valid := true.B
  }
}


