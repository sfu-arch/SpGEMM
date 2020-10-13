package regfile

import chisel3._
import chisel3.util._
import chisel3.Module
import chisel3.testers._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import org.scalatest.{Matchers, FlatSpec} 

//import examples._
import muxes._
import config._
import util._
import interfaces._


abstract class InRegFile(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle{
    val Data = Decoupled(UInt(xlen.W))
  })
}

/**
 * Custom counter which counts indexes
 * @param inc   if it's enable start counting
 * @param maxN  Max number which counter sets to zero after that
 * @param indx  Counter output
 */
class IndexCounter(implicit val p: Parameters) extends Module with CoreParams{
  val io = IO(new Bundle{
    val inc   = Input(Bool())
    val maxN  = Input(UInt(xlen.W))
    val indx  = Output(UInt(xlen.W))
  })
    val in_reg = RegInit(0.U(10.W))

    io.indx := in_reg

    when(io.inc){
      in_reg := Mux( io.maxN > in_reg, in_reg + 1.U, 0.U )
    }

}

/**
 * This class generates local input register file for each node
 * @param inData    An array of input data
 */

class InputRegFile (val inData : Array[UInt])(implicit p: Parameters) extends InRegFile()(p){

  val ROM    = VecInit(inData)
  val Valids = RegInit(VecInit(Seq.fill(inData.size)(true.B)))

  val counter = Module(new IndexCounter())

  counter.io.inc  := io.Data.ready
  counter.io.maxN := inData.size.U - 1.U

  io.Data.bits  := ROM(counter.io.indx)
  io.Data.valid := Valids(counter.io.indx)

  // Set valid signal to false if you read the data
  when(io.Data.ready){
    Valids(counter.io.indx) := false.B
  }

}

