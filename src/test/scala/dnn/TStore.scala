package dnn

/**
  * Created by nvedula on 15/5/17.
  */


import chisel3.iotesters.PeekPokeTester
import config._
import dnnnode.TStore
import node.matNxN
import org.scalatest.{FlatSpec, Matchers}
import utility._

class TStoreNodeTests(df: TStore[matNxN]) (implicit p: config.Parameters) extends PeekPokeTester(df) {
    poke(df.io.GepAddr.valid,false)
    poke(df.io.enable.valid,false)
    poke(df.io.inData.valid,false)
    poke(df.io.PredOp(0).valid,true)
    poke(df.io.tensorReq.ready,false)
    poke(df.io.tensorResp.valid,false)


    poke(df.io.SuccOp(0).ready,true)
    poke(df.io.Out(0).ready,false)


    for (t <- 0 until 20) {

     step(1)

      //IF ready is set
      // send address
      if (peek(df.io.GepAddr.ready) == 1) {
        poke(df.io.GepAddr.valid, true)
        poke(df.io.GepAddr.bits.data, 12)
        poke(df.io.GepAddr.bits.predicate, true)
        poke(df.io.inData.valid, true)
        poke(df.io.inData.bits.data, t+1)
        poke(df.io.inData.bits.predicate,true)
// //         poke(c.io.inData.bits.valid,true)
        poke(df.io.enable.bits.control,true)
        poke(df.io.enable.valid,true)
      }

      if((peek(df.io.tensorReq.valid) == 1) && (t > 4))
      {
        poke(df.io.tensorReq.ready,true)
      }

      if (t > 5 && peek(df.io.tensorReq.ready) == 1)
      {
        // poke(c.io.memReq.ready,false)
        // poke(c.io.memResp.data,t)
        poke(df.io.tensorResp.valid,true)
      }
          printf(s"t: ${t}  io.Out: ${peek(df.io.Out(0))} \n")

    }


}



import utility.Constants._

class TStoreNodeTester extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  it should "TStore Node tester" in {
    chisel3.iotesters.Driver(() => new TStore(NumPredOps=1,NumSuccOps=1,NumOuts=1,ID=1,RouteID=0)(new matNxN(2, false))) { c =>
      new TStoreNodeTests(c)
    } should be(true)
  }
}
