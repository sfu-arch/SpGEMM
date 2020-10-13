package dataflow.filter

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class VecFilter(implicit val p: Parameters) extends Module with CoreParams {

  val FilterSize = 3

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(new ControlBundle( )))
    val data   = Vec(FilterSize, Flipped(Decoupled(new TypBundle( ))))
    val kern   = Vec(FilterSize, Flipped(Decoupled(new TypBundle( ))))
    val sum    = Decoupled(new TypBundle( ))
  })

  val Multiplier = for (i <- 0 until FilterSize) yield {
    val mul = Module(new TypCompute(NumOuts = 1, ID = 0, opCode = "Mul")(sign = false)(new vecN(3)))
    mul
  }

  for (i <- 0 until FilterSize) {
    Multiplier(i).io.LeftIO <> io.data(i)
    Multiplier(i).io.RightIO <> io.kern(i)
    Multiplier(i).io.enable <> io.enable
  }

  val Adder = for (i <- 0 until FilterSize - 1) yield {
    val add = Module(new TypCompute(NumOuts = 1, ID = 0, opCode = "Add")(sign = false)(new vecN(3)))
    add
  }

  // First row
  Adder(0).io.LeftIO <> Multiplier(0).io.Out(0)
  Adder(0).io.RightIO <> Multiplier(1).io.Out(0)
  Adder(0).io.enable <> io.enable
  // Second Row
  Adder(1).io.LeftIO <> Adder(0).io.Out(0)
  Adder(1).io.RightIO <> Multiplier(2).io.Out(0)
  Adder(1).io.enable <> io.enable

  io.sum <> Adder(1).io.Out(0)

  // Info
  val countOn = true.B // increment counter every clock cycle
  val (counterValue, counterWrap) = Counter(countOn, 64 * 1024)

  val active   = RegInit(init = false.B)
  val active_r = RegInit(init = false.B)
  active := Multiplier(0).io.Out(0).valid || Multiplier(1).io.Out(0).valid || Multiplier(2).io.Out(0).valid ||
    Adder(0).io.Out(0).valid || Adder(1).io.Out(0).valid
  active_r := active
  when(active && !active_r) {
    printf("\nCOMPUTE START:  %d\n", counterValue)
  }
  when(!active && active_r) {
    printf("\nCOMPUTE END:  %d\n", counterValue)
  }

}

