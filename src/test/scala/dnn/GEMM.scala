package dnn

import chisel3._
import chisel3.iotesters.PeekPokeTester
import config._
import node._
import org.scalatest.{FlatSpec, Matchers}


// Tester.
class GEMMCompTests(df: GEMMNode[matNxN])
                   (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)

  poke(df.io.LeftIO.bits.data, 0x01020304L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)


  poke(df.io.RightIO.bits.data, 0x04030201L)
  poke(df.io.RightIO.valid, true)
  poke(df.io.RightIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}


class GEMMCompTester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new GEMMNode(NumOuts = 1, ID = 0)(new matNxN(2))) {
      c => new GEMMCompTests(c)
    } should be(true)
  }
}
