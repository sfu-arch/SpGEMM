package accel


import arbiters.TypeStackFile
import chisel3.util._
import chisel3.{Module, RegInit, when, _}
import config._
import control.BasicBlockNoMaskNode
import dnn.wrappers.SystolicSquareWrapper
import dnn.{DotNode, ReduceNode}
import dnnnode.MIMOQueue
import interfaces.{ControlBundle, CooDataBundle, DataBundle}
import junctions.SplitCallNew
import memory.{ReadTypMemoryController, WriteTypMemoryController}
import node.{FXmatNxN, TypLoad, TypStore}
import shell._
import tensorKernels.URAM_Queue


 /** Register File.
  *
  * Six 32-bit register file.
  *
  * -------------------------------
  *  Register description    | addr
  * -------------------------|-----
  *  Control status register | 0x00
  *  Cycle counter           | 0x04
  *  Constant value          | 0x08
  *  Vector length           | 0x0c
  *  Input pointer lsb       | 0x10
  *  Input pointer msb       | 0x14
  *  Output pointer lsb      | 0x18
  *  Output pointer msb      | 0x1c
  * -------------------------------

  * ------------------------------
  *  Control status register | bit
  * ------------------------------
  *  Launch                  | 0
  *  Finish                  | 1
  * ------------------------------
  */


/*
+------------------+                          +-----------------+
|                  | f(bits)+--------+        |                 |
|   VMEReadMaster  +------->+Buffers +-------->VMEWriteMaster   |
|                  |        +--------+        |                 |
+------------------+                          +-----------------+

 */

class DNNCoreTest(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val vcr = new VCRClient
    val vme = new VMEMaster
  })

  val buffer = Module(new URAM_Queue(UInt(p(XLEN).W),40))
//   val buffer = Module(new Queue(UInt(p(XLEN).W),40))
//  val buffer = Module(new MIMOQueue(UInt(p(XLEN).W), entries = 40, io.vme.rd(0).data.bits.getWidth / p(XLEN), NumOuts = 2))

  val sIdle :: sReq :: sBusy :: Nil = Enum(3)
  val Rstate = RegInit(sIdle)
  val Wstate = RegInit(sIdle)

  val cycle_count = new Counter(200)

  when (Rstate =/= sIdle) {
    cycle_count.inc( )
  }


//   buffer.io.clear := false.B

  io.vcr.ecnt(0.U).bits := cycle_count.value

  // Read state machine
  switch (Rstate) {
    is (sIdle) {
      when (io.vcr.launch) {
        cycle_count.value := 0.U
        Rstate := sReq
      }
    }
    is (sReq) {
      when (io.vme.rd(0).cmd.fire()) {
        Rstate := sBusy
      }
    }
  }
  // Write state machine
  switch (Wstate) {
    is (sIdle) {
      when (io.vcr.launch) {
        Wstate := sReq
      }
    }
    is (sReq) {
      when (io.vme.wr(0).cmd.fire()) {
        Wstate := sBusy
      }
    }
  }


  io.vme.rd(0).cmd.bits.addr := io.vcr.ptrs(0)
  io.vme.rd(0).cmd.bits.len := io.vcr.vals(1)
  io.vme.rd(0).cmd.valid := false.B

  io.vme.wr(0).cmd.bits.addr := io.vcr.ptrs(1)
  io.vme.wr(0).cmd.bits.len := io.vcr.vals(1)
  io.vme.wr(0).cmd.valid := false.B

  when(Rstate === sReq) {
    io.vme.rd(0).cmd.valid := true.B
  }

  when(Wstate === sReq) {
    io.vme.wr(0).cmd.valid := true.B
  }

  // Final
  val last = Wstate === sBusy && io.vme.wr(0).ack
  io.vcr.finish := last
  io.vcr.ecnt(0).valid := last

  when(io.vme.wr(0).ack) {
    Rstate := sIdle
    Wstate := sIdle
  }


   buffer.io.enq.valid := io.vme.rd(0).data.valid
   buffer.io.enq.bits := io.vme.rd(0).data.bits.asTypeOf(buffer.io.enq.bits) + io.vcr.vals(0)
   io.vme.rd(0).data.ready := buffer.io.enq.ready

   /*val res = Wire(Vec(2, UInt(p(XLEN).W)))
   for (i <- 0 until 2) {
     res(i) := buffer.io.deq.bits(i) + io.vcr.vals(0)
   }*/

//   io.vme.wr(0).data <> buffer.io.deq

   io.vme.wr(0).data.bits := buffer.io.deq.bits + io.vcr.vals(0)
   io.vme.wr(0).data.valid := buffer.io.deq.valid
   buffer.io.deq.ready := io.vme.wr(0).data.ready
}

