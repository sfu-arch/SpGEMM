package dataflow

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._
import FPU._
import FType._

class FPDivDataFlow(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val dummy = Input(UInt {
      32.W
    })
  })

  val SharedDiv = Module(new SharedFPU(NumOps = 1, 32)(t = S))

  val FPDiv = Module(new FPDivSqrtNode(NumOuts = 1, ID = 0, RouteID = 0, "SQRT")(t = S))


  SharedDiv.io.InData(0) <> FPDiv.io.FUReq
  FPDiv.io.FUResp <> SharedDiv.io.OutData(0)

  FPDiv.io.a.bits.data := 0x40800000.U
  FPDiv.io.a.bits.predicate := true.B
  FPDiv.io.a.valid := true.B
  FPDiv.io.a.bits.taskID := 0.U

  FPDiv.io.b.bits.data := 0x40800000.U
  FPDiv.io.b.bits.predicate := true.B
  FPDiv.io.b.valid := true.B
  FPDiv.io.b.bits.taskID := 0.U

  FPDiv.io.enable.bits.control := true.B
  FPDiv.io.enable.valid := true.B
  FPDiv.io.enable.bits.taskID := 0.U
  FPDiv.io.Out(0).ready := true.B

  //printf("\n \"Outputs\": {\"Out\": %x, %x}", FPDiv.io.Out(0).bits.data, FPDiv.io.Out(0).fire( ))


}
