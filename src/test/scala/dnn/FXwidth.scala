package dnn


import chisel3._
import Chisel.iotesters.{ChiselFlatSpec, SteppedHWIOTester}
import chisel3.iotesters._
import config._
import node._
import org.scalatest.{FlatSpec, Matchers}


class WidthTests(df: changeWidth[FXmatNxN])(implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.in, 0x323232L)
  print(peek(df.io.out).toString(16))
}

class Width_Tester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new changeWidth(new FXmatNxN(2, 4), "double")) {
      c => new WidthTests(c)
    } should be(true)
  }
}
