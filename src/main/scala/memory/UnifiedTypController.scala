package memory

/**
  * Created by vnaveen0 on 10/7/17.
  */

import scala.math._
import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters.{ ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester }
import org.scalatest.{ Matchers, FlatSpec }

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

class UnifiedTypController (ID: Int,
  Size: Int,
  NReads: Int,
  NWrites: Int)(WControl: => WriteTypMemoryController)(RControl: => ReadTypMemoryController)(RWArbiter: => ReadWriteArbiter )(implicit val p: Parameters)
  extends Module
  with CoreParams
  with UniformPrintfs {

  val io = IO(new Bundle {
    val WriteIn = Vec(NWrites, Flipped(Decoupled(new WriteReq())))
    val WriteOut = Vec(NWrites, Output(new WriteResp()))
    val ReadIn = Vec(NReads, Flipped(Decoupled(new ReadReq())))
    val ReadOut = Vec(NReads, Output(new ReadResp()))

    //orig
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)

  })

  require(Size > 0)
  require(isPow2(Size))

/*====================================
 =            Declarations            =
 ====================================*/
  val cacheReq_R = RegInit(MemReq.default)
  val cacheResp_R = RegInit(MemResp.default)

  // Initialize a vector of register files (as wide as type).
  val WriteController = Module(WControl)
  val ReadController = Module(RControl)
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

  // Connecting MemReq/Resp
  val (sIdle :: sReq :: sResp :: sDone :: Nil) = Enum(4)
  val state = RegInit(init = sIdle)

  switch(state) {
    is(sIdle){
      when(ReadWriteArbiter.io.MemReq.fire()) {
        cacheReq_R := ReadWriteArbiter.io.MemReq.bits
        state := sReq
      }
    }

    is(sReq) {

      ReadWriteArbiter.io.MemReq.ready := false.B
      when(io.MemReq.fire()) {
        state := sResp
      }
    }

    is(sResp){
      when(io.MemResp.valid){
        cacheResp_R := io.MemResp.bits
        state := sDone
      }
    }

    is(sDone) {
      when(ReadWriteArbiter.io.MemResp.fire()) {
        state := sIdle
      }
    }
  }

  ReadWriteArbiter.io.MemReq.ready := state === sIdle
  io.MemReq.valid       := state === sReq
  io.MemReq.bits        := cacheReq_R

  ReadWriteArbiter.io.MemResp.valid := state === sDone
  ReadWriteArbiter.io.MemResp.bits := cacheResp_R


  //--------------------------

  //------------------------------------------------------------------------------------
  /// Printf debugging
  override val printfSigil = "Unified: " + ID + " Type " + (Typ_SZ)

//  verb match {
//    case "high"  => {printf(p" MemReq_R.addr: $cacheReq_R.addr")}
//    case "med"   => {printf(p" state: $state")}
//    case "low"   => {printf(p" state: $state")}
//  }

  // printf(p"\n : ${ReadController.io.MemReq.fire()} Tag: ${ReadReq.tag} ")
  // printf(p"\n Cache Request ${WriteController.io.MemReq}")
  //  printf(p"Demux out:  ${io.WriteOut(0)}")

}
