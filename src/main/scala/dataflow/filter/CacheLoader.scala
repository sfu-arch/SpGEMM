package dataflow.filter

import chisel3._
import chisel3.util._

import accel._
import node._
import config._
import interfaces._
import arbiters._
import memory._

class CacheLoader(FilterSize : Int)(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(new ControlBundle()))
    val ptr    = Vec(FilterSize,Flipped(Decoupled(new DataBundle())))
    val cache  = Flipped(new CacheIO)
    val data   = Vec(FilterSize,Decoupled(new DataBundle()))
  })

  val CacheMem = Module(new UnifiedController(ID=0,Size=32,NReads=FilterSize,NWrites=FilterSize)
    (WControl=new WriteTypMemoryController(NumOps=FilterSize,BaseSize=2,NumEntries=2))
    (RControl=new ReadTypMemoryController(NumOps=FilterSize,BaseSize=2,NumEntries=2))
    (RWArbiter=new ReadWriteArbiter()))

  val Load = for (i <- 0 until FilterSize) yield {
    val ld = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=i,RouteID=i))
    ld
  }

  for (i <- 0 until FilterSize) {
    Load(i).io.enable <> io.enable
    Load(i).io.GepAddr <> io.ptr(i)
    CacheMem.io.ReadIn(i) <> Load(i).io.memReq
    Load(i).io.memResp  <> CacheMem.io.ReadOut(i)
    io.data(i) <> Load(i).io.Out(0) 
  }

  io.cache.abort := false.B
  io.cache.req <> CacheMem.io.MemReq
  CacheMem.io.MemResp <> io.cache.resp
}

