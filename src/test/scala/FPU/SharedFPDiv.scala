package FPU

/**
  * Created by vnaveen0 on 8/7/17.
  */

import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import config._
import FType._


class SharedFPUTests(c: SharedFPU)
	(implicit p: config.Parameters)
	extends PeekPokeTester(c) {

// 	var readidx = 0
	poke(c.io.InData(0).bits.RouteID, 0)
	poke(c.io.InData(1).bits.RouteID, 1)
	poke(c.io.InData(0).bits.data("field0").data, 0x6C00)
	poke(c.io.InData(0).bits.data("field1").data, 0x4C00)
	poke(c.io.InData(0).bits.data("field2").data, 0)
	poke(c.io.InData(0).valid,1)
	poke(c.io.InData(1).valid,1)
	poke(c.io.InData(1).bits.data("field0").data, 0x6C00)
	poke(c.io.InData(1).bits.data("field1").data, 0x4C00)
	poke(c.io.InData(1).bits.data("field2").data, 1)
	poke(c.io.InData(1).valid,0)
        for( i <- 0 to 68) {
    	step(1)
    }
}


class SharedFPUTester extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new HALFPrecisionFPConfig).toInstance)
  it should "Memory Controller tester" in {
    chisel3.iotesters.Driver(() => new SharedFPU(NumOps=2, PipeDepth=5)(t = p(FTYP))) {
      c => new SharedFPUTests(c)
    } should be(true)
  }
}
