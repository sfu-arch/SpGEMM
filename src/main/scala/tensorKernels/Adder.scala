
package tensorKernels

import chisel3.util.{Decoupled, Valid}
import chisel3.{Flipped, Module, UInt, _}
import config.{Parameters, XLEN}
import dnn.types.{OperatorNRSCAL}
import interfaces.CooDataBundle
import node.Shapes

class NRSCALFU[L <: Shapes : OperatorNRSCAL](shape: => L, lanes: Int, opCode: String)(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val a = Flipped(Valid(shape))
    val b = Flipped(Valid(UInt(p(XLEN).W)))
    val o = Decoupled(shape)
  })


  val start = io.o.ready
  val FU    = OperatorNRSCAL.magic(io.a.bits, io.b.bits, start, lanes, opCode)
  io.o.bits := FU._1
  io.o.valid := FU._2
}

class AdderIO(implicit val p: Parameters) extends Module {
  val io = IO(new Bundle {
    val eopIn = Input(Bool( ))
    val in = Flipped(Decoupled(new CooDataBundle(UInt(p(XLEN).W))))
    val out = Decoupled(new CooDataBundle(UInt(p(XLEN).W)))
    val eopOut = Output(Bool( ))
  })
}

class Adder[L <: Shapes : OperatorNRSCAL](ID: Int)(shape: => L)(implicit p: Parameters)
  extends AdderIO()(p) {

  /*===============================================*
   *                Connections                    *
   *===============================================*/
  io.eopOut := RegNext(io.eopIn)

  val data = RegInit(CooDataBundle.default(0.U(p(XLEN).W)))
  val dataValid = RegInit(false.B)

  val FU = Module(new NRSCALFU(shape, lanes = shape.getLength(), opCode = "Add"))

  FU.io.a.bits := VecInit(io.in.bits.data.asUInt()).asTypeOf(shape)
  FU.io.b.bits := data.data

  FU.io.a.valid := io.in.valid
  FU.io.b.valid := io.in.valid

  FU.io.o.ready := true.B

  dataValid := false.B
  when(io.in.valid){
    dataValid := true.B
    when(data.row =/= io.in.bits.row || data.col =/= io.in.bits.col) {
      data <> io.in.bits
    }.elsewhen(data.row === io.in.bits.row && data.col === io.in.bits.col){
      data.data := FU.io.o.bits.asUInt() //data.data + io.in.bits.data
      data.row := io.in.bits.row
      data.col := io.in.bits.col
      data.valid := io.in.bits.valid
    }
  }

  io.out.bits := data
  io.out.valid := (dataValid && !(data.row === io.in.bits.row && data.col === io.in.bits.col)) || io.eopIn
  io.in.ready := io.out.ready
}