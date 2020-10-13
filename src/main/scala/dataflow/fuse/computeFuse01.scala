package dataflow.fuse

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class ComputeFuse01DF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 3, ID = 0, OpCodes = Array("And","Xor","Add"))(sign = false)(p))

  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> io.data2
  m0.io.In(3) <> io.data3

  m0.io.enable <> io.enable

  io.dataOut <> m0.io.Out(2)

  for(i <- 0 until 4)
    m0.io.Out(i).ready := io.dataOut.ready
}

