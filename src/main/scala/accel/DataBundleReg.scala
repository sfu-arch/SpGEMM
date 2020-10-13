package accel

import chisel3._
import chisel3.util._
import dataflow._
import junctions._
import config._
import interfaces._

/**
  * The SimpleReg class creates a set of memory mapped configuration and status
  * registers accessible via a Nasti (AXI4) slave interface.  Each register is
  * the native width of the Nasti data bus.
  *
  * The control registers are read/write access.  The content of the control
  * registers is output on io.ctrl interface.
  *
  * The status registers are read only. The status registers are non-latching
  * and reflect the live state of the io.stat interface.
  *
  * The registers are mapped into two banks: control and status.
  * The control register bank is mapped into the lower address range starting
  * at the base offset of the accelerator slave interface.  The status registers
  * are mapped starting at the base address + 2048. E.g. for 3 control registers
  * and 2 status registers:
  *
  * Base Address -> Ctrl Reg 0
  *                 Ctrl Reg 1
  *                 Ctrl Reg 2
  *                 Unused
  *                 ...
  * Base+2048    -> Stat Reg 0
  *                 Stat Reg 1
  *                 Unused
  *                 Unused
  *
  * @note Since the control registers are readable, unused register bits will
  * not be automatically removed.  Pack the registers efficiently to save
  * logic.  Unused status register bits will automatically be removed from
  * the final logic.
  *
  * @param cNum The number of configuration registers
  * @param sNum The number of status registers
  * @param p    Implicit project parameters
  *
  * @note io.nasti A Nasti bus slave interface to a processor core
  * @note io.ctrl  A vector of 'cNum' control registers
  * @note io.stat  A vector of 'sNum' status registers
  */

abstract class DataBundleRegIO(cNum : Int, sNum: Int)(implicit val p: Parameters) extends Module with CoreParams
{
  val io = IO(
    new Bundle {
      val nasti = Flipped(new NastiIO())
      val start = Output(Bool())
      val init = Output(Bool())
      val done = Input(Bool())
      val ready = Input(Bool())
      val ctrl  = Vec(cNum,Decoupled(new DataBundle()))
      val stat  = Vec(sNum,Flipped(Decoupled(new DataBundle())))
    }
  )
}

class DataBundleReg(cNum : Int, sNum: Int)(implicit p: Parameters) extends DataBundleRegIO(cNum,sNum)(p) {

  val numCfg = if (xlen == 32) 2 else 1;
  val ctrlBank = RegInit(VecInit(Seq.fill(cNum+numCfg)(VecInit(Seq.fill(xlen/8)(0.U(8.W))))))
  val statBank = RegInit(VecInit(Seq.fill(sNum+numCfg)(0.U(xlen.W))))
  val wordSelBits = log2Ceil(xlen/8)
  val regSelBits = log2Ceil(math.max(cNum+numCfg, sNum+numCfg))
  val bankSelBit = 11
  val valid = RegInit(VecInit(Seq.fill(cNum)(false.B)))
  //val valid = RegInit(init=0.U(cNum.W))


  // register for write address channel ready signal
  val writeAddrReadyReg = RegInit(false.B)
  val canDoWrite = io.nasti.aw.valid && io.nasti.w.valid && !writeAddrReadyReg
  writeAddrReadyReg := canDoWrite
  io.nasti.aw.ready := writeAddrReadyReg

  // register for keeping write address
  val writeAddrReg = RegInit(0.U(32.W))
  when (canDoWrite) {writeAddrReg := io.nasti.aw.bits.addr}

  val writeReadyReg = RegNext(canDoWrite, false.B)
  io.nasti.w.ready := writeReadyReg

  // register bank write
  val doWrite = writeReadyReg && io.nasti.w.valid && writeAddrReadyReg && io.nasti.aw.valid
  val writeRegSelect = writeAddrReg(regSelBits+wordSelBits-1, wordSelBits)
  val writeBankSelect = writeAddrReg(bankSelBit)

  for(i <- 0 until cNum) {
    when(io.ctrl(i).ready) {
      valid(i) := false.B
    }
    io.ctrl(i).bits.predicate := true.B // default
  }

  when (writeBankSelect === 0.U && doWrite) {
    for (i <- 0 until xlen/8) {
      when (io.nasti.w.bits.strb(i) === 1.U) {
        ctrlBank(writeRegSelect)(i) :=  io.nasti.w.bits.data(8*i+7,8*i)
      }
    }
    valid(writeRegSelect-numCfg.U) := true.B
  } .otherwise {
    ctrlBank(0) := Seq.fill(xlen / 8)(0.asUInt(8.W)) // clear start & init bits
  }

  val ready = RegInit(init=0.U(cNum.W))
  //val anyvalid = (valid =/= 0.U)
  val anyvalid = valid.contains(true.B)
  for(i <- 0 until cNum) {
    io.ctrl(i).bits.data := Cat(ctrlBank(i+numCfg).reverse)
// //     io.ctrl(i).bits.valid := valid(i)
    io.ctrl(i).valid := valid(i)
  }

  io.start := ctrlBank(0)(0)(0);
  io.init  := ctrlBank(0)(0)(1);

  // write response generation
  io.nasti.b.bits.resp   := 0.U        // always OK
  val writeRespValidReg = RegInit(false.B)
  writeRespValidReg  := doWrite && !writeRespValidReg
  io.nasti.b.valid   := writeRespValidReg
  io.nasti.b.bits.id := io.nasti.aw.bits.id
  io.nasti.b.bits.user := 0.U

  // read address ready generation
  val readAddrReadyReg = RegInit(false.B)
  val canDoRead = !readAddrReadyReg && io.nasti.ar.valid
  readAddrReadyReg := canDoRead
  io.nasti.ar.ready := readAddrReadyReg

  // read address latching
  val readAddrReg = RegInit(0.U(32.W))
  when (canDoRead) { readAddrReg := io.nasti.ar.bits.addr }

  // read data valid and response generation
  val readValidReg = RegInit(false.B)
  val doRead = readAddrReadyReg && io.nasti.ar.valid && !readValidReg
  readValidReg := doRead

  io.nasti.r.valid := readValidReg
  io.nasti.r.bits.last  := readValidReg
  io.nasti.r.bits.resp := 0.U    // always OK
  io.nasti.r.bits.id := io.nasti.ar.bits.id
  io.nasti.r.bits.user := 0.U

  statBank(0):= Cat(anyvalid, io.ready, io.done)
  for(i <- 0 until sNum) {
//    if (io.stat(i).valid === true.B) {
      statBank(i+numCfg) := io.stat(i).bits.data
//    }
    io.stat(i).ready := true.B
  }

  // register bank read
  val readRegSelect = readAddrReg(regSelBits+wordSelBits-1, wordSelBits)
  val readBankSelect = readAddrReg(bankSelBit)
  val outputReg = RegInit(0.U(xlen.W))
  when (readBankSelect === 0.U ) {
    outputReg := Cat(ctrlBank(readRegSelect).reverse)
  } .otherwise {
    outputReg := statBank(readRegSelect)
  }
  io.nasti.r.bits.data := outputReg

  // Info
  val countOn = true.B // increment counter every clock cycle
  val (counterValue, counterWrap) = Counter(countOn, 64*1024)
  //clockCycles := clockCycles + 1.U
/*
  when (io.nasti.ar.fire()) {
    printf("\nSTATUS START:  %d\n", counterValue)
  }
  when (io.nasti.r.fire()) {
    printf("\nSTATUS END: %d\n", counterValue)
  }
  when (io.nasti.aw.fire()) {
    printf("\nCONFIG START:  %d\n", counterValue)
  }
  when (io.nasti.b.fire()) {
    printf("\nCONFIG END: %d\n", counterValue)
  }
*/
}

