package arbiters
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

class TypeStackFile(ID: Int,
  Size: Int,
  NReads: Int,
  NWrites: Int)(WControl: => WController)(RControl: => RController)(implicit val p: Parameters)
  extends Module
  with CoreParams
  with UniformPrintfs {

  val io = IO(new Bundle {
    val WriteIn = Vec(NWrites, Flipped(Decoupled(new WriteReq())))
    val WriteOut = Vec(NWrites, Output(new WriteResp()))
    val ReadIn = Vec(NReads, Flipped(Decoupled(new ReadReq())))
    val ReadOut = Vec(NReads, Output(new ReadResp()))
    //val RegFi = Flipped(new RegFileBundle())
  })

  require(Size > 0)
  require(isPow2(Size))

/*====================================
 =            Declarations            =
 ====================================*/

  // Initialize a vector of register files (as wide as type).
  val RegFile = Module(new RFile(Size*(1<<tlen))(p))
  val WriteController = Module(WControl)
  val ReadController = Module(RControl)

  // Write registers
  val WriteReq     = RegNext(next = WriteController.io.MemReq.bits)
  val WriteValid   = RegNext(init  = false.B,next=WriteController.io.MemReq.fire())

  val ReadReq     = RegNext(next = ReadController.io.MemReq.bits)
  val ReadValid   = RegNext(init  = false.B, next=ReadController.io.MemReq.fire())

  
  val xlen_bytes = xlen / 8
  val wordindex = log2Ceil(xlen_bytes)
  val frameindex = tlen

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

  WriteController.io.MemReq.ready := true.B
  ReadController.io.MemReq.ready := true.B


  WriteController.io.MemResp.valid := false.B
  ReadController.io.MemResp.valid := false.B

/*==========================================
=            Write Controller.             =
==========================================*/

  RegFile.io.wen := WriteController.io.MemReq.fire()
  val waddr = Cat(WriteController.io.MemReq.bits.taskID, WriteController.io.MemReq.bits.addr(wordindex + log2Ceil(Size) - 1, wordindex))
  RegFile.io.waddr := waddr
  RegFile.io.wdata := WriteController.io.MemReq.bits.data
  RegFile.io.wmask := WriteController.io.MemReq.bits.mask

  RegFile.io.raddr2 := 0.U


  WriteController.io.MemResp.bits.data := 0.U
  WriteController.io.MemResp.valid    := WriteValid
  WriteController.io.MemResp.bits.tag := WriteReq.tag
  WriteController.io.MemResp.bits.iswrite := true.B
  WriteController.io.MemResp.bits.valid := true.B
  WriteController.io.MemResp.bits.tile := 0.U


/*==============================================
=            Read Memory Controller            =
==============================================*/
  val raddr = Cat(ReadController.io.MemReq.bits.taskID, ReadController.io.MemReq.bits.addr(wordindex + log2Ceil(Size) - 1, wordindex))
  RegFile.io.raddr1 := raddr

  ReadController.io.MemResp.valid     := ReadValid
  ReadController.io.MemResp.bits.tag  := ReadReq.tag
  ReadController.io.MemResp.bits.data := RegFile.io.rdata1
  ReadController.io.MemResp.bits.valid  := true.B
  ReadController.io.MemResp.bits.data := 0.U
  ReadController.io.MemResp.bits.tile := 0.U

  ReadController.io.MemResp.bits.iswrite := false.B

  /// Printf debugging
  override val printfSigil = "RFile: " + ID + " Type " + (Typ_SZ)


  // printf(p"\n : ${ReadController.io.MemReq.fire()} Tag: ${ReadReq.tag} ")
  // printf(p"\n Cache Request ${WriteController.io.MemReq}")
   // printf(p"Demux out:  ${io.WriteOut(0)}")
  // Read in parallel after shifting.
  // seq
  // for (i <- 0 until Typ_SZ)
  // {
}
// val TS = new TypeStackFile
// val Reg = new RegFile();;;
// Reg.io <> TS.io.RegFi