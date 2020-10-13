package muxes

import scala.math._
import chisel3._
import chisel3.Module
import config._
import util._
import interfaces._

/**
  * Created by vnaveen0 on 7/9/17.
  */

//class ReadReqT(implicit p: Parameters) extends ReadReq with ValidT {}


class  TestMux(NReads: Int)(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val ReadIn    = Input(Vec(NReads,new ReadResp()))
    val EN = Input(Bool())
    val SEL = Input(UInt(max(1, log2Ceil(NReads)).W))


    val ReadOut = Output(new ReadResp())
  })

    val RMux   = Module(new Mux(new ReadResp(),NReads))
    // Connect up Read ins with arbiters
    for (i <- 0 until NReads) {
      RMux.io.inputs(i) <> io.ReadIn(i)
    }

    RMux.io.sel <> io.SEL
    RMux.io.en := io.EN
    io.ReadOut  <> RMux.io.output

//  val  EN = RegInit(true.B)
//  val  SEL = RegInit(1.U(2.W))
//
//
//  val x = io.SEL
//  when(io.EN) {
//    io.ReadOut := io.ReadIn(x)
//
//  }.otherwise {
//    io.ReadOut.valid := false.B
//  }

}
