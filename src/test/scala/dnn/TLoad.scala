package dnn

import chisel3.iotesters.PeekPokeTester
import config._
import dnnnode.TLoad
import node.matNxN
import org.scalatest.{FlatSpec, Matchers}
import utility._

class TLoadNodeTests(df: TLoad[matNxN]) (implicit p: config.Parameters) extends PeekPokeTester(df) {
  def N = false
  def Y = true
  val Control = Map(
        "Default"  -> List(N,N,N,N,N,N,N,N,N,N),
        "Active"   -> List(N,N, N,N, Y, N,N, Y,Y),
        "Input"   -> List(Y,Y, Y,Y, Y, N,N, Y,Y),
        "~Input"    -> List(Y,Y, Y,N, Y, N,N, Y,Y),
        "~Control"    -> List(Y,N, Y,N, Y, N,N, Y,Y)
      ).withDefaultValue(List(N,N,N,N,N,N,N,N,N,N))

  val sigs = Seq(df.io.enable.valid, df.io.enable.bits.control,
    df.io.GepAddr.valid, df.io.GepAddr.bits.predicate,
    df.io.PredOp(0).valid,
    df.io.tensorReq.ready,
    df.io.tensorResp.valid,
    df.io.SuccOp(0).ready,
    df.io.Out(0).ready
    )

  sigs zip Control("Default") map {case(s,d) => poke(s,d)}
  sigs zip Control("Active") map {case(s,d) => poke(s,d)}


    for (t <- 0 until 20) {
             step(1)

      if (peek(df.io.GepAddr.ready) == 1) {
        sigs zip Control("~Control") map {case(s,d) => poke(s,d)}
        poke(df.io.GepAddr.bits.data, 12)
      }

       if((peek(df.io.tensorReq.valid) == 1) && (t > 4))
      {
        poke(df.io.tensorReq.ready,true)
      }

      if (t > 5 && peek(df.io.tensorReq.ready) == 1)
      {
//        poke(df.io.tensorResp.data,t)
        poke(df.io.tensorResp.data, 0xdeadbeef+t)
        poke(df.io.tensorResp.valid,true)
      }
    }

}

import utility.Constants._

class TLoadNodeTester extends  FlatSpec with Matchers {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  it should "TLoad Node tester" in {
    chisel3.iotesters.Driver(() => new TLoad(NumPredOps=1,NumSuccOps=1,NumOuts=1,ID=1,RouteID=0)(new matNxN(2, false))) { c =>
      new TLoadNodeTests(c)
    } should be(true)
  }
}
