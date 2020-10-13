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

class FPDivNodeTester(c: FPDivSqrtNode) extends PeekPokeTester(c) {
    poke(c.io.a.valid,false)
    poke(c.io.b.valid,false)
    poke(c.io.FUReq.ready,false)
    poke(c.io.FUResp.valid,false)
    poke(c.io.Out(0).ready,true)

     step(1)


    poke(c.io.a.valid, true)
    poke(c.io.a.bits.data,0x42800000)
    poke(c.io.a.bits.taskID, 22)
    poke(c.io.a.bits.predicate, true)
    poke(c.io.b.valid, true)
    poke(c.io.b.bits.data, 0x41800000)
    poke(c.io.b.bits.predicate,true)
    poke(c.io.b.bits.taskID, 22)
    poke(c.io.enable.bits.control,true)
    poke(c.io.enable.valid,true)
    step(1)
    step(1)

    poke(c.io.FUReq.ready,true)

    step(1)
    step(1)
    step(1)
    print(s"t: ${t} io.field0: ${peek(c.io.FUReq.bits.data("field0"))} io.field1: ${peek(c.io.FUReq.bits.data("field1"))} io.field2: ${peek(c.io.FUReq.bits.data("field2"))} \n")
    poke(c.io.FUResp.data,100)
    poke(c.io.FUResp.valid,true)


    step(1)
    step(1)
    step(1)
    // }


}



class FPDivNodeTests extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  it should "FPDivSqrt Node tester" in {
    chisel3.iotesters.Driver(() => new FPDivSqrtNode(NumOuts=1,ID=1,RouteID=0,opCode = "SQRT")(t = S)) { c =>
      new FPDivNodeTester(c)
    } should be(true)
  }
}
