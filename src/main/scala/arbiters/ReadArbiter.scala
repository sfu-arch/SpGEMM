package arbiters
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

class  WordRegFile(Size: Int, NReads: Int, NWrites: Int)(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
  val ReadIn    = Vec(NReads,Flipped(Decoupled(new ReadReq())))
  val ReadOut   = Vec(NReads,Output(new ReadResp()))
  //val ReadValids= Vec(NReads,Output(Bool()))
  val WriteIn   = Vec(NWrites,Flipped(Decoupled(new WriteReq())))
  val WriteOut  = Vec(NWrites,Output(new WriteResp()))
  })

  // Initialize a vector of register files (as wide as type).
  val RegFile     = Module(new RFile(Size)(p))

  // Read in parallel after shifting.
  // seq
  // for (i <- 0 until Typ_SZ)
  // {


  // }

  // -------------------------- Read Arbiter Logic -----------------------------------------
  // Parameters. 10 is the number of loads assigned to the stack segment in the ll file
  val ReadReqArbiter  = Module(new RRArbiter(new ReadReq(),NReads));
  val ReadRespDeMux   = Module(new Demux(new ReadResp(),NReads));

  // Arbiter output latches
  val ReadArbiterReg   = RegNext(next = ReadReqArbiter.io.out.bits)
  val ReadInputChosen  = RegNext(init = 0.U(max(1,log2Ceil(NReads)).W),next=ReadReqArbiter.io.chosen)
  val ReadInputValid   = RegNext(init  = false.B,next=ReadReqArbiter.io.out.valid)

  // Demux input latches. chosen and valid delayed by 1 cycle for RFile read to return
  val ReadOutputChosen = RegNext(init = 0.U(max(1,log2Ceil(NReads)).W), next = ReadInputChosen)
  val ReadOutputValid  = RegNext(init = false.B, next = ReadInputValid)

  // Connect up Read ins with arbiters
  for (i <- 0 until NReads) {
    ReadReqArbiter.io.in(i) <> io.ReadIn(i)
    io.ReadOut(i) <> ReadRespDeMux.io.outputs(i)
  }

  // Activate arbiter
  ReadReqArbiter.io.out.ready := true.B

  // Feed arbiter output to Regfile input port.
  RegFile.io.raddr1 := ReadArbiterReg.address
  // Feed Regfile output port to Demux port
  ReadRespDeMux.io.input.data   := RegFile.io.rdata1

  ReadRespDeMux.io.sel := ReadOutputChosen
  ReadRespDeMux.io.en := ReadOutputValid


  // -------------------------- Write Arbiter Logic -----------------------------------------
  // Parameters. 10 is the number of loads assigned to the stack segment in the ll file
  val WriteReqArbiter  = Module(new RRArbiter(new WriteReq(),NWrites));
  val WriteRespDeMux   = Module(new Demux(new WriteResp(),NWrites));

  // Arbiter output latches
  val WriteArbiterReg   = RegNext(next = WriteReqArbiter.io.out.bits)
  val WriteInputChosen  = RegNext(init = 0.U(max(1,log2Ceil(NWrites)).W),next=WriteReqArbiter.io.chosen)
  val WriteInputValid   = RegNext(init  = false.B,next=WriteReqArbiter.io.out.valid)

  // Demux input latches. chosen and valid delayed by 1 cycle for RFile Write to return
  val WriteOutputChosen = RegNext(init = 0.U(max(1,log2Ceil(NWrites)).W), next = WriteInputChosen)
  val WriteOutputValid  = RegNext(init = false.B, next = WriteInputValid)

  // Connect up Write ins with arbiters
  for (i <- 0 until NWrites) {
    WriteReqArbiter.io.in(i) <> io.WriteIn(i)
    io.WriteOut(i) <> WriteRespDeMux.io.outputs(i)
  }

  // Activate arbiter.   // Feed write  arbiter output to Regfile input port.
  WriteReqArbiter.io.out.ready := true.B

  RegFile.io.wen := WriteInputValid
  RegFile.io.waddr := WriteArbiterReg.address
  RegFile.io.wdata := WriteArbiterReg.data
  RegFile.io.wmask := WriteArbiterReg.mask

  // Feed regfile output port to Write Demux port. Only need to send valid back to operation.
  // In reality redundant as arbiter ready signal already indicates write acquired write port.
  // This signal guarantees the data has propagated to the Registerfile
  //WriteRespDeMux.io.input.valid   := 1.U
  WriteRespDeMux.io.sel := WriteOutputChosen
  WriteRespDeMux.io.en := WriteOutputValid
  WriteRespDeMux.io.input.done := true.B
  WriteRespDeMux.io.input.RouteID := 0.U
  WriteRespDeMux.io.input.valid := false.B

  ReadRespDeMux.io.input.valid := false.B
  ReadRespDeMux.io.input.RouteID := 0.U

  RegFile.io.raddr2 := 0.U


  //DEBUG prinln
  //TODO make them as a flag
  // printf("Write Data:%x\n", {WriteArbiterReg.data})
}
