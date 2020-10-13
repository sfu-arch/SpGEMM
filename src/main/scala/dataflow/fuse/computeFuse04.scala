package dataflow.fuse

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class ComputeFuse04SDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {

    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(Bool()))

    val dataOut = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 3, ID = 0, OpCodes = Array("And","ShiftLeft","Xor"))(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "And")(sign = false))
  val m2 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "Or")(sign = false))

  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> io.data2
  m0.io.In(3) <> io.data3

  m1.io.LeftIO <> io.data4
  m1.io.RightIO <> io.data5

  m2.io.LeftIO <> m0.io.Out(2)
  m2.io.RightIO <> m1.io.Out(0)

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable
  m2.io.enable <> io.enable


  for(i <- 0 until 4)
    m0.io.Out(i).ready := m2.io.LeftIO.ready


  io.dataOut <> m1.io.Out(0)
}



class ComputeFuse04PDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 4, ID = 0, OpCodes = Array("And","ShiftLeft","Xor","Or"))(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "And")(sign = false))

  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> io.data2
  m0.io.In(3) <> io.data3
  m0.io.In(4) <> m1.io.Out(0)

  m1.io.LeftIO <> io.data4
  m1.io.RightIO <> io.data5

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable

  for(i <- 0 until 5)
    m0.io.Out(i).ready := io.dataOut.ready

  io.dataOut <> m0.io.Out(3)
}
