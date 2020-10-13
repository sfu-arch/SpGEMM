// See LICENSE for license details.

package FPU

import chisel3._
import chisel3.util._
import FPU._
import FType._

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import node._
import dataflow._
import muxes._
import config._
import util._
import interfaces._

// Tester.
class FPUALUTester(df: FPALU, opCode: Int)
                  (implicit p: config.Parameters) extends PeekPokeTester(df) {

  opCode match {
    case AluOpCode.Add => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4400.U)
      assert(peek(df.io.out) == 0x4800)
      print(f"0x${peek(df.io.out)}%X")
    }
    case AluOpCode.Sub => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4400.U)
      assert(peek(df.io.out) == 0)
      print(f"0x${peek(df.io.out)}%X")
    }
    case AluOpCode.LT => {
      poke(df.io.in1.value, 0xC400.U)
      poke(df.io.in2.value, 0x4400.U)
      assert(peek(df.io.out) == 0x1)
      print(f" 0x${peek(df.io.out)}%X")
    }
    case AluOpCode.PassA => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4800.U)
      assert(peek(df.io.out) == 0x4400)
      print(f" 0x${peek(df.io.out)}%X")
    }
    case AluOpCode.PassB => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4800.U)
      assert(peek(df.io.out) == 0x4800)
      print(f" 0x${peek(df.io.out)}%X")
    }
    case AluOpCode.Min => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4800.U)
      assert(peek(df.io.out) == 0x4400)
      print(f" 0x${peek(df.io.out)}%X")
    }
    case AluOpCode.Max => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4800.U)
      assert(peek(df.io.out) == 0x4800)
      print(f" 0x${peek(df.io.out)}%X")
    }
    case AluOpCode.Mac => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4400.U)
      poke(df.io.in3.get.value, 0x4400.U)
      print(f" 0x${peek(df.io.out)}%X")
      assert(peek(df.io.out) == 0x4D00)
    }
    case AluOpCode.EQ => {
      poke(df.io.in1.value, 0x4400.U)
      poke(df.io.in2.value, 0x4400.U)
      print(f" 0x${peek(df.io.out)}%X")
      assert(peek(df.io.out) == 0x1)
    }
  }
}


class FPALUTests extends FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new HALFPrecisionFPConfig).toInstance)
  it should "FP Add tester" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "Add")) {
      c => new FPUALUTester(c, AluOpCode.Add)
    } should be(true)
  }
  it should "FP Sub tester" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "Sub")) {
      c => new FPUALUTester(c, AluOpCode.Sub)
    } should be(true)
  }
  it should "FP LT tester" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "LT")) {
      c => new FPUALUTester(c, AluOpCode.LT)
    } should be(true)
  }
  it should "Equals" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "EQ")) {
      c => new FPUALUTester(c, AluOpCode.EQ)
    } should be(true)
  }
  it should "Pass A" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "PassA")) {
      c => new FPUALUTester(c, AluOpCode.PassA)
    } should be(true)
  }
  it should "Pass B" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "PassB")) {
      c => new FPUALUTester(c, AluOpCode.PassB)
    } should be(true)
  }
  it should "Min" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "Min")) {
      c => new FPUALUTester(c, AluOpCode.Min)
    } should be(true)
  }
  it should "Max" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "Max")) {
      c => new FPUALUTester(c, AluOpCode.Max)
    } should be(true)
  }
  it should "Mac" in {
    chisel3.iotesters.Driver(
      () => new FPALU(new FloatingPoint(t = H), opCode = "Mac")) {
      c => new FPUALUTester(c, AluOpCode.Mac)
    } should be(true)
  }
}