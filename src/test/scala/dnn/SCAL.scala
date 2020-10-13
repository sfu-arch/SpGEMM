package dnn

import chisel3._
import chisel3.iotesters.PeekPokeTester
import config._
import org.scalatest.{FlatSpec, Matchers}
import FPU._
import node._

// Tester.
class SCALCompTests(df: SCALNode[matNxN])
                   (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)

  poke(df.io.LeftIO.bits.data, 0xFEFEFEFEL)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)


  poke(df.io.RightIO.bits.data, 0x04L)
  poke(df.io.RightIO.valid, true)
  poke(df.io.RightIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}


class FXSCALCompTests(df: SCALNode[FXmatNxN])
                     (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)
  // 0x32 0011.0010 . Fixed point 3.125 in fixed point 4 BP.
  poke(df.io.LeftIO.bits.data, 0x32323232L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  // 0x32 (3.125) * 0x20 (2.0) = 6.25 (0x64 or 100)
  poke(df.io.RightIO.bits.data, 0x20L)
  poke(df.io.RightIO.valid, true)
  poke(df.io.RightIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)
  step(20)
}

class FPSCALCompTests(df: SCALNode[FPmatNxN])
                     (implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.enable.valid, true)
  poke(df.io.enable.bits.control, true)
  // 0x49 = 3.125 (Mini 8 bit format. 3 bit exp, 5 bit mantissa
  poke(df.io.LeftIO.bits.data, 0x49494949L)
  poke(df.io.LeftIO.valid, true)
  poke(df.io.LeftIO.bits.predicate, true)

  // 0x4e - 3.7 . Result : 103.
  poke(df.io.RightIO.bits.data, 0x4eL)
  poke(df.io.RightIO.valid, true)
  poke(df.io.RightIO.bits.predicate, true)

  poke(df.io.Out(0).ready, true.B)

  for (i <- 0 until 20)
    step(1)
}


class SCALCompTester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    //    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
    //      () => new SCALNode(NumOuts = 1, ID = 0, 1, "Add")(new matNxN(2, true))) {
    //      c => new SCALCompTests(c)
    //    } should be(true)

    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new SCALNode(NumOuts = 1, ID = 0, 1, "sqrt")(new FXmatNxN(2, 4))) {
      c => new FXSCALCompTests(c)
    } should be(true)
    //    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
    //      () => new SCALNode(NumOuts = 1, ID = 0, 4, "Mul")(new FPmatNxN(2, t = FType.M))) {
    //      c => new FPSCALCompTests(c)
    //    } should be(true)
  }
}
