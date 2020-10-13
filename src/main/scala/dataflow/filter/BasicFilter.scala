package dataflow.filter

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class BasicFilter(implicit val p: Parameters) extends Module with CoreParams {

  val FilterSize = 3*3

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(new ControlBundle()))
    val data   = Vec(FilterSize,Flipped(Decoupled(new DataBundle())))
    val kern   = Vec(FilterSize,Flipped(Decoupled(new DataBundle())))
    val sum    = Decoupled(new DataBundle())
  })

  val Multiplier = for (i <- 0 until FilterSize) yield {
    val mul = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "mul")(sign = false))
    mul
  }

  for (i <- 0 until FilterSize) {
    Multiplier(i).io.LeftIO <> io.data(i)
    Multiplier(i).io.RightIO <> io.kern(i)
    Multiplier(i).io.enable <> io.enable
  }

  val Adder = for (i <- 0 until FilterSize-1) yield {
    val add = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "Add")(sign = false))
    add
  }
  // First row
  Adder(0).io.LeftIO <> Multiplier(0).io.Out(0)
  Adder(0).io.RightIO <> Multiplier(1).io.Out(0)
  Adder(1).io.LeftIO <> Multiplier(2).io.Out(0)
  Adder(1).io.RightIO <> Multiplier(3).io.Out(0)
  Adder(2).io.LeftIO <> Multiplier(4).io.Out(0)
  Adder(2).io.RightIO <> Multiplier(5).io.Out(0)
  Adder(3).io.LeftIO <> Multiplier(6).io.Out(0)
  Adder(3).io.RightIO <> Multiplier(7).io.Out(0)
  // Second row
  Adder(4).io.LeftIO <> Adder(0).io.Out(0)
  Adder(4).io.RightIO <> Adder(1).io.Out(0)
  Adder(5).io.LeftIO <> Adder(2).io.Out(0)
  Adder(5).io.RightIO <> Adder(3).io.Out(0)
  // Third Row
  Adder(6).io.LeftIO <> Adder(4).io.Out(0)
  Adder(6).io.RightIO <> Adder(5).io.Out(0)
  // Last Row
  Adder(7).io.LeftIO <> Adder(6).io.Out(0)
  Adder(7).io.RightIO <> Multiplier(8).io.Out(0)

  for (i <- 0 until FilterSize-1) {
    Adder(i).io.enable <> io.enable
  }

  io.sum <> Adder(7).io.Out(0)

  // Info
  val countOn = true.B // increment counter every clock cycle
  val (counterValue, counterWrap) = Counter(countOn, 64*1024)

  val active = RegInit(init = false.B)
  val active_r = RegInit(init = false.B)
  active := Multiplier(0).io.Out(0).valid || Multiplier(1).io.Out(0).valid || Multiplier(2).io.Out(0).valid ||
    Multiplier(3).io.Out(0).valid || Multiplier(4).io.Out(0).valid || Multiplier(5).io.Out(0).valid ||
    Multiplier(6).io.Out(0).valid || Multiplier(7).io.Out(0).valid || Multiplier(8).io.Out(0).valid ||
    Adder(0).io.Out(0).valid || Adder(1).io.Out(0).valid || Adder(2).io.Out(0).valid ||
    Adder(3).io.Out(0).valid || Adder(4).io.Out(0).valid || Adder(5).io.Out(0).valid ||
    Adder(6).io.Out(0).valid || Adder(7).io.Out(0).valid

  active_r := active
  when (active && !active_r) {
    printf("\nCOMPUTE START:  %d\n", counterValue)
  }
  when (!active && active_r) {
    printf("\nCOMPUTE END:  %d\n", counterValue)
  }

}

