// package dataflow

// import chisel3._
// import chisel3.util._
// import chisel3.Module
// import chisel3.testers._
// import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
// import org.scalatest.{Matchers, FlatSpec} 

// import muxes._
// import config._
// import util._
// import interfaces._
// import regfile._
// import node._
// import alloca._


// //TODO uncomment if you remove StackCentral.scala file
// //
// abstract class AllocaTestIO(implicit val p: Parameters) extends Module with CoreParams {
//   val io = IO(new Bundle {
//     val result  = Output(xlen.U)
//     val resultReady = Input(Bool())
//     val resultValid = Output(Bool())
//    })
// }

// class AllocaTest(implicit p: Parameters) extends AllocaTestIO()(p){
  

//   // Containig number of bytes Alloca require
//   val reg1 = Module(new InputRegFile(Array(1.U, 4.U, 3.U, 4.U))(p))

//   val m0 = Module(new AllocaNode(0)(p))
//   val m1 = Module(new newCentralStack()(p))

//   m0.io.sizeinput  <> reg1.io.Data
//   printf(p"Reg data:  ${reg1.io.Data}\n")
//   m0.io.allocareq  <> m1.io.AllocaIn(0)
//   printf(p"Alloca req:  ${m0.io.allocareq}\n")
//   m0.io.allocaresp.allocaaddr <> m1.io.AllocaOut(0)
//   printf(p"Alloca  resp:  ${m0.io.allocaresp}\n")
//   m0.io.allocaresp.valid := m1.io.Valids(0)


//   m0.io.addressout.ready := io.resultReady
//   io.result := m0.io.addressout.bits
//   io.resultValid := m0.io.addressout.valid

// }
