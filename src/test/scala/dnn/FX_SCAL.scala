package dnn

import chisel3._
import chisel3.util._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import dataflow._
import muxes._
import config._
import util._


class SCALTests(df: NCycle_SCAL[UInt])(implicit p: config.Parameters) extends PeekPokeTester(df) {
  poke(df.io.activate, false.B)
  // left * right
  df.io.input_vec.zipWithIndex.foreach { case (io, i) => poke(io, (i).U) }
  poke(df.io.scalar, 1.U)
  poke(df.io.activate, true.B)
  step(1)
  poke(df.io.activate, false.B)
  for (i <- 0 until df.latency( ) - 1) {
    for (j <- 0 until df.lanes) {
      //    require(i * df.lanes + j + 1 == peek(df.io.output(j)))
      print(peek(df.io.output(j)) + ",")
    }
    print("\n")
    step(1)
  }
}

class SCAL_Tester extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new Mat_VecConfig).toInstance)
  it should "Typ Compute Tester" in {
    chisel3.iotesters.Driver.execute(Array("--backend-name", "verilator", "--target-dir", "test_run_dir"),
      () => new NCycle_SCAL(UInt(p(XLEN).W), N = 8, lanes = 1, "add")) {
      c => new SCALTests(c)
    } should be(true)
  }
}
