package dnn

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.util._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import org.scalatest.{FlatSpec, Matchers}
import dataflow._
import muxes._
import config._
import util._


class ReductionTests(df: NCycle_Reduction[FixedPoint])(implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.activate, false.B)
  // left * right
  df.io.input_vec.zipWithIndex.foreach { case (io, i) => poke(io, (0x20).U) }
  poke(df.io.activate, true.B)
  step(1)
  poke(df.io.activate, false.B)
  for (i <- 0 until df.latency( ) - 1) {
    print(peek(df.io.output))
    print("," + peek(df.io.valid))
    print("\n")
    step(1)
  }
}


class Reduction_Tester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new NCycle_Reduction(FixedPoint(p(XLEN).W, 4.BP), N = 4, pipelined = true, opcode = "Add")) {
      c => new ReductionTests(c)
    } should be(true)
  }
}
