package memory


import Chisel.experimental.chiselName
import accel.Cache
import chisel3._
import chisel3.Module
import chisel3.util._
import junctions._
import muxes.{Demux, DemuxGen}

import scala.collection.immutable


// Config
import config._
import utility._
import interfaces._
import scala.math._

class cacheserving(val NumTileBits: Int, val NumSlotBits: Int) extends Bundle {
  val tile_idx = UInt(NumTileBits.W)
  val slot_idx = UInt(NumSlotBits.W)

  override def cloneType = new cacheserving(NumTileBits, NumSlotBits).asInstanceOf[this.type]
}

object cacheserving {
  def default()(
    implicit p: Parameters
  ): cacheserving = {
    val wire = Wire(new cacheserving(32, 32))
    wire.slot_idx := 1.U
    wire.tile_idx := 1.U
    wire
  }

  def default(NumTileBits: Int,
              NumSlotBits: Int
             )(
               implicit p: Parameters
             ): cacheserving = {
    val wire = Wire(new cacheserving(32, 32))
    wire.slot_idx := 1.U
    wire.tile_idx := 1.U
    wire
  }

}


class NParallelCacheIO(val NumTiles: Int = 1, val NumBanks: Int = 1)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {
  val io = IO(new Bundle {
    val cpu   = new Bundle {
      val MemReq  = Vec(NumTiles, Flipped(Decoupled(new MemReq)))
      val MemResp = Vec(NumTiles, Output(Valid(new MemResp)))

    }
    val nasti = Vec(NumBanks, new NastiIO)
    val stat  = Output(Vec(NumBanks, UInt(xlen.W)))
  })
}


class NParallelCache(NumTiles: Int = 1, NumBanks: Int = 1)(implicit p: Parameters) extends NParallelCacheIO(NumTiles, NumBanks)(p) {

  //  Declare a vector of cache objects
  val caches = for (i <- 0 until NumBanks) yield {
    val cache1 = Module(new Cache(i))
    io.stat(i) := cache1.io.stat
    cache1
  }
  /*============================*
   *    Wiring  Cache to CPU  *
   *============================*/
  assert(isPow2(NumBanks) && NumBanks != 0)
  //
  val NumBankBits = max(1, log2Ceil(NumBanks))
  val NumTileBits = max(1, log2Ceil(NumBanks))

  //  Per-Tile stateink

  val fetch_queues = for (i <- 0 until NumTiles) yield {
    val fq = Module(new PeekQueue(new MemReq( ), 6))
    fq
  }

  val slot_arbiters = for (i <- 0 until NumTiles) yield {
    val slot_arbiter = Module(new RRArbiter(new Bool, NumBanks))
    slot_arbiter
  }


  val fq_io_deq_bits = fetch_queues map {
    _.io.deq.bits
  }
  val slots          = RegInit(VecInit(Seq.fill(NumTiles)(VecInit(Seq.fill(NumBanks)(CacheSlotsBundle.default(NumTiles))))))

  // Per-Cache bank state
  //  val slots         = RegInit(VecInit(Seq.fill(NumTiles)(VecInit(Seq.fill(NumBanks)(CacheSlotsBundle.default(NumTiles))))))
  val cache_ready   = VecInit(caches.map(_.io.cpu.req.ready))
  val cache_req_io  = VecInit(caches.map(_.io.cpu.req))
  val cache_resp_io = VecInit(caches.map(_.io.cpu.resp))
  val cache_serving = RegInit(VecInit(Seq.fill(NumBanks)(cacheserving.default(NumTileBits, NumBankBits))))


  var bankidxseq = for (i <- 0 until NumTiles) yield {
    if (NumBanks == 1) {
      0.U
    } else {
      fq_io_deq_bits(i).addr(caches(0).bankbitindex + NumBankBits - 1, caches(0).bankbitindex)
    }
  }

  printf(p"\n FQ 1: ${fetch_queues(1).io.deq}")

  //  Input to queue
  for (i <- 0 until NumTiles) {
    fetch_queues(i).io.enq.bits <> io.cpu.MemReq(i).bits.clone_and_set_tile_id(i.U)
    fetch_queues(i).io.enq.valid := io.cpu.MemReq(i).valid
    io.cpu.MemReq(i).ready := fetch_queues(i).io.enq.ready
    fetch_queues(i).io.recycle := false.B
  }


  /* [HACK] Leave this in here, otherwise FIRRTL is going to complain about type inferences.
   * There is some trouble with type inference if you reach into cache io through caches before mapping it using a map */
  cache_req_io foreach {
    _.valid := false.B
  }

  caches foreach { c =>
    c.io.cpu.abort := false.B
  }

  val picker_matrix = for (i <- 0 until NumBanks) yield {
    bankidxseq.zipWithIndex.map {
      case (bank, index) =>
        (bank === i.U) & (fetch_queues(index).io.deq.fire)
    }
  }

  val picked_matrix = for (i <- 0 until NumBanks) yield {
    PriorityEncoderOH(picker_matrix(i))
  }

  val tile_picked = picked_matrix.map {
    VecInit(_).asUInt
  }.reduce {
    _ | _
  }

  val prioritymuxes = for (i <- 0 until NumBanks) yield {
    Mux1H(picked_matrix(i), fq_io_deq_bits)
  }

  for (i <- 0 until NumTiles) {

    var slot_arbiter = slot_arbiters(i)
    val slot_idx = slot_arbiter.io.chosen


    for (j <- 0 until NumBanks) {
      slot_arbiter.io.in(j).valid := ~(slots(i)(j).alloc)
      slot_arbiter.io.in(j).bits := DontCare
    }

    //  Handshaking fetch queue with slot arbiter
    fetch_queues(i).io.deq.ready := slot_arbiter.io.out.valid
    slot_arbiter.io.out.ready := fetch_queues(i).io.deq.valid


    //  Queueing Logic.
    when(fetch_queues(i).io.deq.fire) {
      when(cache_ready(bankidxseq(i)) && tile_picked(i)) {
        //  Fetch queue fires only if slot is free.
        //  Slot is free and cache is ready

        slots(i)(slot_idx).tile := i.U
        slots(i)(slot_idx).alloc := true.B
        // Setting cache metadata before sending request request.
        cache_req_io(bankidxseq(i)).valid := true.B
        cache_serving(bankidxseq(i)).slot_idx := slot_idx
        cache_serving(bankidxseq(i)).tile_idx := i.U
      }.otherwise {
        //    Cache is not ready
        //      Recycling logic
        fetch_queues(i).io.recycle := true.B
      }
    }


    for (j <- 0 until NumBanks) {
      caches(j).io.cpu.req.bits <> prioritymuxes(j)
      when(cache_resp_io(j).valid) {
        slots(cache_serving(j).tile_idx)(cache_serving(j).slot_idx).ready := true.B
        slots(cache_serving(j).tile_idx)(cache_serving(j).slot_idx).bits := cache_resp_io(j).bits
      }
    }


    var resp_arbiter = Module(new RRArbiter(
      new MemResp, NumBanks))

    for (j <- 0 until NumBanks) {
      resp_arbiter.io.in(j).bits := slots(i)(j).bits
      resp_arbiter.io.in(j).bits.tile := slots(i)(j).tile
      resp_arbiter.io.in(j).valid := slots(i)(j).fire
      when(resp_arbiter.io.in(j).fire) {
        slots(i)(j).alloc := false.B
        slots(i)(j).ready := false.B
      }
    }

    resp_arbiter.io.out.ready := true.B
    io.cpu.MemResp(i).valid := false.B
    io.cpu.MemResp(i).bits := resp_arbiter.io.out.bits
    when(resp_arbiter.io.out.fire( )) {
      io.cpu.MemResp(i).valid := true.B
    }
  }

  printf(p"\n Cache Bundle: ${cache_req_io(0)} CB 1: ${cache_req_io(1)}")
  printf(p"\n Slot arbiter 1: ${slot_arbiters(1).io.out}")

  //  printf(p"\n : deq fire: ${fetch_queues(0).io.deq.fire},${fetch_queues(1).io.deq.fire} Bankidxs: ${VecInit(bankidxseq)} Picker matrix : ${VecInit(picker_matrix(0))}  \n")
  //  printf(p"\n slots ${slots(1)(0)} \n ${slots(1)(1)}")
  //  Debug statements
  //  printf(p"\nRecycle: ${fetch_queue.io.recycle} \n Arbiter : ${fetch_arbiter.io.out} \n Queue: ${fetch_queue.io.enq}")

  /*============================*
   *    Wiring  Cache to NASTI  *
   *============================*/

  io.nasti zip caches foreach {
    case (ionasti, cach) => {
      ionasti <> cach.io.nasti
    }
  }
  printf(p"\n Stat: ${io.stat}")

}

import java.io.{File, FileWriter}

object NParallelCacheMain extends App {
  val dir = new File("RTL/NParallelCache");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new NCache(1, 1)))

  val verilogFile   = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close( )
}
