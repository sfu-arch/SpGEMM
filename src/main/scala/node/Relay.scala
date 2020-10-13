package node

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester, OrderedDecoupledHWIOTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{Matchers, FlatSpec} 

import config._
import interfaces._
import muxes._
import util._


/**
 * This nodes recieve an input from a node and then
 * relay it to other nodes. Nodes using their token ID
 * can register their request inside relay node.
 * And base on token ID relay node understand when 
 * it's ready to recieve a new data
 *
 * @param NumConsum Number of consumer
 * @param DataIN    Input data from previous node
 * @param TokenIn   Input token from previous node
 */

abstract class RelayNode(NumConsum: Int)(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    // Inputs should be fed only when Ready is HIGH
    // Inputs are always latched.
    // If Ready is LOW; Do not change the inputs as this will cause a bug
    val DataIn    = Flipped(Decoupled(UInt(xlen.W)))
    val TokenIn   = Input(UInt(tlen.W))

    // The interface has to be prepared to latch the output on every cycle as long as ready is enabled
    // The output will appear only for one cycle and it has to be latched. 
    // The output WILL NOT BE HELD (not matter the state of ready/valid)
    // Ready simply ensures that no subsequent valid output will appear until Ready is HIGH
    val OutIO = Vec(NumConsum, new RelayOutput())
    //val OutIO  = Output(Vec(NumConsum, new Bundle{
        //val DataNode = Decoupled(UInt(xlen.W))
        //val TokenNode = Input(UInt(tlen.W))
      //}))
    })

}

class RelayDecoupledNode(val NumConsum: Int = 2)(implicit p : Parameters) extends RelayNode(NumConsum)(p){

  //Latch input data from parent node
  val dataIn_reg  = RegInit(0.U(tlen.W))
  val dataV_reg   = RegInit(false.B)
  val tokenIn_reg = RegInit(0.U(tlen.W))

  printf(p"dataIn reg:  ${dataIn_reg}\n")
  printf(p"dataV_reg:   ${dataV_reg}\n")
  printf(p"tokenIn_reg: ${tokenIn_reg}\n")


  val tokenConsum_reg = Seq.fill(NumConsum)(RegInit(false.B))

  val tokenCMP_w  = Seq.fill(NumConsum){WireInit(false.B)}


  io.DataIn.ready := ~dataV_reg

  when(io.DataIn.fire()){
    dataV_reg  := true.B
    dataIn_reg := io.DataIn.bits
    tokenIn_reg  := io.TokenIn
  }

  for(i <- 0 until NumConsum){
    io.OutIO(i).DataNode.bits   := dataIn_reg
    io.OutIO(i).DataNode.valid  := dataV_reg
  }

  printf(p"io.OutIO.DataNode: ${io.OutIO}\n")

  for(i <- 0 until NumConsum){
    when(io.OutIO(i).DataNode.ready){
      tokenConsum_reg(i) := io.OutIO(i).TokenNode
    }
  }

  for(i <- 0 until NumConsum){
    when(tokenIn_reg =/= tokenConsum_reg(i)){
      tokenCMP_w(i) := 1.U
    }.otherwise{
      tokenCMP_w(i) := 0.U
    }
  }
  //
  //tokenCMP_w(0) := false.B

  when(tokenCMP_w.reduce(_ & _)){
    dataV_reg := false.B
  }

}

