package dataflow.fuse

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class ComputeFuse03SDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val data6 = Flipped(Decoupled(new DataBundle()))
    val data7 = Flipped(Decoupled(new DataBundle()))
    val data8 = Flipped(Decoupled(new DataBundle()))
    val data9 = Flipped(Decoupled(new DataBundle()))
    val data10 = Flipped(Decoupled(new DataBundle()))
    val data11 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut0 = Decoupled(new DataBundle())
    val dataOut1 = Decoupled(new DataBundle())

  })



  val m0 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "And")(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "Xor")(sign = false))
  val m2 = Module(new ComputeNode(NumOuts = 2, ID = 0, opCode = "Xor")(sign = false))
  val m3 = Module(new Chain(NumOps = 4, ID = 0, OpCodes = Array("ShiftLeft","And","Add", "ShiftRight"))(sign = false))
  val m4 = Module(new Chain(NumOps = 4, ID = 0, OpCodes = Array("ShiftLeft","And","Add", "ShiftRight"))(sign = false))

  m0.io.LeftIO <> io.data0
  m0.io.RightIO <> io.data1

  m1.io.LeftIO <> io.data2
  m1.io.RightIO <> io.data3

  m2.io.LeftIO <> m0.io.Out(0)
  m2.io.RightIO <> m1.io.Out(0)

  m3.io.In(0) <> m2.io.Out(0)
  m3.io.In(1) <> io.data4
  m3.io.In(2) <> io.data5
  m3.io.In(3) <> io.data6
  m3.io.In(4) <> io.data7

  m4.io.In(0) <> m2.io.Out(1)
  m4.io.In(1) <> io.data8
  m4.io.In(2) <> io.data9
  m4.io.In(3) <> io.data10
  m4.io.In(4) <> io.data11

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable
  m2.io.enable <> io.enable


  for(i <- 0 until 4)
    m3.io.Out(i).ready := io.dataOut0.ready

  for(i <- 0 until 4)
    m4.io.Out(i).ready := io.dataOut1.ready

  io.dataOut0 <> m3.io.Out(3)
  io.dataOut1 <> m4.io.Out(3)

}



class ComputeFuse03PDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val data6 = Flipped(Decoupled(new DataBundle()))
    val data7 = Flipped(Decoupled(new DataBundle()))
    val data8 = Flipped(Decoupled(new DataBundle()))
    val data9 = Flipped(Decoupled(new DataBundle()))
    val data10 = Flipped(Decoupled(new DataBundle()))
    val data11 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut0 = Decoupled(new DataBundle())
    val dataOut1 = Decoupled(new DataBundle())

  })

  val m0 = Module(new Chain(NumOps = 6, ID = 0, OpCodes = Array("And","Xor","ShiftLeft","And","Add", "ShiftRight"))(sign = false))
  val t0 = Module(new TestCFU(ID = 0, NumOps = 6, NumIns = 7, NumOuts = 1)(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "Xor")(sign = false))
  val m2 = Module(new Chain(NumOps = 4, ID = 0, OpCodes = Array("ShiftLeft","And","Add", "ShiftRight"))(sign = false))


  m0.io.In(0) <> io.data0
  m0.io.In(1) <> io.data1
  m0.io.In(2) <> m1.io.Out(0)
  m0.io.In(3) <> io.data4
  m0.io.In(4) <> io.data5
  m0.io.In(5) <> io.data6
  m0.io.In(6) <> io.data7

  m1.io.LeftIO <> io.data2
  m1.io.RightIO <> io.data3

  m2.io.In(0) <> m0.io.Out(1)
  m2.io.In(1) <> io.data8
  m2.io.In(2) <> io.data9
  m2.io.In(3) <> io.data10
  m2.io.In(4) <> io.data11

  m0.io.enable <> io.enable
  m1.io.enable <> io.enable
  m2.io.enable <> io.enable

  for(i <- 0 until 7)
    m0.io.Out(i).ready := m2.io.Out(1).ready

  for(i <- 0 until 5)
    m2.io.Out(i).ready := io.dataOut1.ready

  io.dataOut0 <> m0.io.Out(5)
  io.dataOut1 <> m2.io.Out(3)

}

class ComputeFuse03CDF(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val data0 = Flipped(Decoupled(new DataBundle()))
    val data1 = Flipped(Decoupled(new DataBundle()))
    val data2 = Flipped(Decoupled(new DataBundle()))
    val data3 = Flipped(Decoupled(new DataBundle()))
    val data4 = Flipped(Decoupled(new DataBundle()))
    val data5 = Flipped(Decoupled(new DataBundle()))
    val data6 = Flipped(Decoupled(new DataBundle()))
    val data7 = Flipped(Decoupled(new DataBundle()))
    val data8 = Flipped(Decoupled(new DataBundle()))
    val data9 = Flipped(Decoupled(new DataBundle()))
    val data10 = Flipped(Decoupled(new DataBundle()))
    val data11 = Flipped(Decoupled(new DataBundle()))
    val enable = Flipped(Decoupled(new ControlBundle))

    val dataOut0 = Decoupled(new DataBundle())
    val dataOut1 = Decoupled(new DataBundle())

  })

  val t0 = Module(new TestCFU(ID = 0, NumOps = 6, NumIns = 7, NumOuts = 1)(sign = false))
  val m1 = Module(new ComputeNode(NumOuts = 1, ID = 0, opCode = "Xor")(sign = false))
  val m2 = Module(new Chain(NumOps = 4, ID = 0, OpCodes = Array("ShiftLeft","And","Add", "ShiftRight"))(sign = false))


  t0.io.In(0) <> io.data0
  t0.io.In(1) <> io.data1
  t0.io.In(2) <> m1.io.Out(0)
  t0.io.In(3) <> io.data4
  t0.io.In(4) <> io.data5
  t0.io.In(5) <> io.data6
  t0.io.In(6) <> io.data7

  m1.io.LeftIO <> io.data2
  m1.io.RightIO <> io.data3

  m2.io.In(0) <> t0.io.Out(0)
  m2.io.In(1) <> io.data8
  m2.io.In(2) <> io.data9
  m2.io.In(3) <> io.data10
  m2.io.In(4) <> io.data11

  t0.io.enable <> io.enable
  m1.io.enable <> io.enable
  m2.io.enable <> io.enable

//  for(i <- 0 until 7)
//    m0.io.Out(i).ready := m2.io.Out(1).ready

  for(i <- 0 until 5)
    m2.io.Out(i).ready := io.dataOut1.ready

  io.dataOut0 <> t0.io.Out(0)
  io.dataOut1 <> m2.io.Out(3)

}
