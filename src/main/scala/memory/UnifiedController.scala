package memory

import scala.math._
import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec}

import regfile._
import config._
import util._
import interfaces._
import muxes._
import accel._
import memory._
import utility.Constants._
import utility.UniformPrintfs

//XXX
//TODO put VEC insid Outputs
//OUTPUT(VECXXX)
//
//TODO Make readOut and readValid as a bundle
//

/**
  * @param Size    : Size of Register file to be allocated and managed
  * @param NReads  : Number of static reads to be connected. Controls size of arbiter and Demux
  * @param NWrites : Number of static writes to be connected. Controls size of arbiter and Demux
  */

class UnifiedController(ID: Int,
                        Size: Int,
                        NReads: Int,
                        NWrites: Int)(WControl: => WController)(RControl: => RController)(RWArbiter: => ReadWriteArbiter)(implicit val p: Parameters)
  extends Module
    with CoreParams
    with UniformPrintfs {

  val io = IO(new Bundle {
    val WriteIn  = Vec(NWrites, Flipped(Decoupled(new WriteReq( ))))
    val WriteOut = Vec(NWrites, Output(new WriteResp( )))
    val ReadIn   = Vec(NReads, Flipped(Decoupled(new ReadReq( ))))
    val ReadOut  = Vec(NReads, Output(new ReadResp( )))

    //orig
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq  = Decoupled(new MemReq)

  })

  require(Size > 0)
  require(isPow2(Size))

  /*====================================
   =            Declarations            =
   ====================================*/
  //  val memReq_R  = RegInit(MemReq.default)
  //val memResp_R = RegInit(MemResp.default)

  // Initialize a vector of register files (as wide as type).
  val WriteController  = Module(WControl)
  val ReadController   = Module(RControl)
  val ReadWriteArbiter = Module(RWArbiter)

  /*================================================
  =            Wiring up input arbiters            =
  ================================================*/

  // Connect up Write ins with arbiters
  for (i <- 0 until NWrites) {
    WriteController.io.WriteIn(i) <> io.WriteIn(i)
    io.WriteOut(i) <> WriteController.io.WriteOut(i)
  }

  // Connect up Read ins with arbiters
  for (i <- 0 until NReads) {
    ReadController.io.ReadIn(i) <> io.ReadIn(i)
    io.ReadOut(i) <> ReadController.io.ReadOut(i)
  }

  // Connect Read/Write Controllers to ReadWrite Arbiter
  ReadWriteArbiter.io.ReadMemReq <> ReadController.io.MemReq
  ReadController.io.MemResp <> ReadWriteArbiter.io.ReadMemResp

  ReadWriteArbiter.io.WriteMemReq <> WriteController.io.MemReq
  WriteController.io.MemResp <> ReadWriteArbiter.io.WriteMemResp


  ReadWriteArbiter.io.MemReq.ready := io.MemReq.ready
  io.MemReq.bits := ReadWriteArbiter.io.MemReq.bits
  io.MemReq.valid := ReadWriteArbiter.io.MemReq.valid
  ReadWriteArbiter.io.MemResp <> io.MemResp

  //--------------------------

  //------------------------------------------------------------------------------------
  /// Printf debugging
  override val printfSigil = "Unified: " + ID + " Type " + (Typ_SZ)

  //  verb match {
  //    case "high"  => {printf(p" .addr: $cacheReq_R.addr")}
  //    case "med"   => {printf(p" state: $state")}
  //    case "low"   => {printf(p" state: $state")}
  //  }

  // printf(p"\n : ${ReadController.io.MemReq.fire()} Tag: ${ReadReq.tag} ")
  // printf(p"\n Cache Request ${WriteController.io.MemReq}")
  //  printf(p"Demux out:  ${io.WriteOut(0)}")

}
