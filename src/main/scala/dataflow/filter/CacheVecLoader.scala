package dataflow.filter

import chisel3._
import chisel3.util._

import accel._
import node._
import config._
import interfaces._
import arbiters._
import memory._

class CacheVecLoader(FilterSize : Int)(implicit val p: Parameters) extends Module with CoreParams {

  val io = IO(new Bundle {
    val enable = Flipped(Decoupled(new ControlBundle()))
    val ptr    = Vec(FilterSize,Flipped(Decoupled(new TypBundle())))
    val sum    = Flipped(Decoupled(new TypBundle()))
    val cache  = Flipped(new CacheIO)
    val data   = Vec(FilterSize,Decoupled(new TypBundle()))
  })

  val CacheMem = Module(new UnifiedTypController(ID=0,Size=32,NReads=FilterSize,NWrites=2)
    (WControl=new WriteTypMemoryController(NumOps=2,BaseSize=2,NumEntries=2))
    (RControl=new ReadTypMemoryController(NumOps=FilterSize,BaseSize=2,NumEntries=2))
    (RWArbiter=new ReadWriteArbiter()))

  val Load = for (i <- 0 until FilterSize) yield {
    val ld = Module(new TypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=i,RouteID=i))
    ld
  }

  val Store = Module(new TypStore(NumPredOps=0, NumSuccOps=0, NumOuts=1,ID=0,RouteID=0))
  Store.io.enable <> io.enable
  Store.io.GepAddr <> io.ptr(0)
  Store.io.inData <> io.sum
  CacheMem.io.WriteIn(0) <> Store.io.memReq
  Store.io.memResp  <> CacheMem.io.ReadOut(0)
  Store.io.Out(0).ready := true.B

  for (i <- 0 until FilterSize) {
    Load(i).io.enable <> io.enable
    Load(i).io.GepAddr <> io.ptr(i)
    CacheMem.io.ReadIn(i) <> Load(i).io.memReq
    Load(i).io.memResp  <> CacheMem.io.ReadOut(i)
    io.data(i) <> Load(i).io.Out(0) 
  }

  io.cache.req <> CacheMem.io.MemReq
  CacheMem.io.MemResp <> io.cache.resp
  io.cache.abort := false.B

}

