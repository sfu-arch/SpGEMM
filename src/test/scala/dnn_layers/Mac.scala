package dnn_layers


import chisel3._
import chisel3.iotesters.PeekPokeTester
import config._
import node._
import org.scalatest.{FlatSpec, Matchers}
import FPU._
import dnn.MacNode
//import dnn.DotNode

// Tester.

class FXMacCompTests(df: MacNode[FXmatNxN])
                    (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)
  // 0x32 0011.0010 . Fixed point 3.125 in fixed point 4 BP.
  poke(df.io.LeftIO.bits.data, 0x49494949L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  // 0x32 (3.125) * 0x20 (2.0) = 6.25 (0x64 or 100)
  poke(df.io.RightIO.bits.data, 0x40404040L)
  poke(df.io.RightIO.valid, true)
  poke(df.io.RightIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}



class MacCompTester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new MacNode(NumOuts = 1, ID = 0, 4)(new FXmatNxN(2,4))) {
      c => new FXMacCompTests(c)
    } should be(true)
  }
}
