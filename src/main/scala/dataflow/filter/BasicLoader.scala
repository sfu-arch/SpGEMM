package dataflow.filter

import chisel3._
import chisel3.util._

import node._
import config._
import interfaces._
import arbiters._
import memory._

class BasicLoader(implicit val p: Parameters) extends Module with CoreParams {

  val FilterSize = 3*3

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(Bool()))
    val ptr    = Vec(FilterSize,Flipped(Decoupled(new DataBundle())))
    val data   = Vec(FilterSize,Decoupled(new DataBundle()))
  })

  val StackFile = Module(new TypeStackFile(ID=0,Size=32,NReads=9,NWrites=9)
    (WControl=new WriteMemoryController(NumOps=9,BaseSize=2,NumEntries=2))
    (RControl=new ReadMemoryController(NumOps=9,BaseSize=2,NumEntries=2)))

  val Load = for (i <- 0 until FilterSize) yield {
    val ld = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=i,RouteID=i))
    ld
  }

  for (i <- 0 until FilterSize) {
    Load(i).io.enable <> io.enable
    Load(i).io.GepAddr <> io.ptr(i)
    StackFile.io.ReadIn(i) <> Load(i).io.memReq
    Load(i).io.memResp  <> StackFile.io.ReadOut(i)
    io.data(i) <> Load(i).io.Out(0) 
  }

}

/*
  val GEP = for (i <- 0 until FilterSize) yield {
    val gp = Module (new GepOneNode(NumOuts = 1, ID = i)(numByte1 = 0)(p))
    gp
  }

    // Wiring GEP instruction to the function argument
    GEP(i).io.baseAddress <> io.ptr(i)
    GEP(i).io.idx1.valid :=  true.B
    GEP(i).io.idx1.bits.predicate :=  true.B
    GEP(i).io.idx1.bits.data :=  0.U
    GEP(i).io.idx2.valid :=  true.B
    GEP(i).io.idx2.bits.predicate :=  true.B
    GEP(i).io.idx2.bits.data :=  0.U

    Load(i).io.GepAddr.bits.data := (4 + (xlen / 8) * i).U
    Load(i).io.GepAddr.bits.predicate := true.B
    Load(i).io.GepAddr.valid := true.B
*/
