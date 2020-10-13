package dnn

import chisel3._
import chisel3.iotesters.PeekPokeTester
import config._
import org.scalatest.{FlatSpec, Matchers}
import FPU._
import node._

// Tester.
class ReduceCompTests(df: ReduceNode[matNxN])
                     (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)

  poke(df.io.LeftIO.bits.data, 0x04FE0201L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}


class FXReduceCompTests(df: ReduceNode[FXmatNxN])
                       (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)
  // 0x32 0011.0010 . Fixed point 3.125 in fixed point 4 BP.
  poke(df.io.LeftIO.bits.data, 0x24242424L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}

class FPReduceCompTests(df: ReduceNode[FPmatNxN])
                       (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)
  // 0x49 = 3.125 (Mini 8 bit format. 3 bit exp, 5 bit mantissa
  poke(df.io.LeftIO.bits.data, 0x49494949L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}


class ReduceCompTester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
//    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
//      () => new ReduceNode(NumOuts = 1, ID = 0, false, "Mul")(new matNxN(2, issign = true))) {
//      c => new ReduceCompTests(c)
//    } should be(true)

        chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
          () => new ReduceNode(NumOuts = 1, ID = 0, false, "Add")(new FXmatNxN(2,4))) {
          c => new FXReduceCompTests(c)
        } should be(true)
    //    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
    //      () => new ReduceNode(NumOuts = 1, ID = 0, 4, "Mul")(new FPmatNxN(2, t = FType.M))) {
    //      c => new FPReduceCompTests(c)
    //    } should be(true)
  }
}
