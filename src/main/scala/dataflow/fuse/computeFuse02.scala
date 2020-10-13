package dataflow.fuse

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class ComputeFuse02SDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val data6 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut0 = Decoupled(new DataBundle())
    val dataOut1 = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 3, ID = 0, OpCodes = Array("Xor","And","Xor"))(sign = false))
  val m1 = Module(new Chain(NumOps = 2, ID = 0, OpCodes = Array("ShiftRight","Or"))(sign = false))
  val m2 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "ShiftLeft")(sign = false))

  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> io.data2
  m0.io.In(3) <> io.data3

  m1.io.In(0) <> m0.io.Out(2)
  m1.io.In(1) <> io.data4
  m1.io.In(2) <> io.data5

  m2.io.LeftIO <> m0.io.Out(3)
  m2.io.RightIO <> io.data6

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable
  m2.io.enable <> io.enable


  for(i <- 0 until 2)
    m0.io.Out(i).ready := m2.io.LeftIO.ready

  for(i <- 0 until 3)
    m1.io.Out(i).ready := io.dataOut1.ready

  io.dataOut0 <> m1.io.Out(1)
  io.dataOut1 <> m2.io.Out(0)
}



class ComputeFuse02PDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val data6 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(Bool()))

    val dataOut0 = Decoupled(new DataBundle())
    val dataOut1 = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 5, ID = 0, OpCodes = Array("Xor","And","Xor","ShiftRight","Or"))(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "ShiftLeft")(sign = false))


  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> io.data2
  m0.io.In(3) <> io.data3
  m0.io.In(4) <> io.data4
  m0.io.In(5) <> io.data5

  m1.io.LeftIO <> m0.io.Out(2) 
  m1.io.RightIO <> io.data6

  m0.io.Out(0).ready := m1.io.Out(0).ready
  m0.io.Out(1).ready := m1.io.Out(0).ready
  m0.io.Out(3).ready := m1.io.Out(0).ready
  m0.io.Out(4).ready := m1.io.Out(0).ready
  m0.io.Out(5).ready := m1.io.Out(0).ready

  io.dataOut0.valid := m0.io.Out(4).valid
  io.dataOut0.bits  := m0.io.Out(4).bits
  io.dataOut1 <> m1.io.Out(0) 

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable

}
