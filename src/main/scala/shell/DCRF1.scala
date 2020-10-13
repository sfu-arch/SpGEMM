package dandelion.shell

import chisel3._
import chisel3.util._
import config._
import shell.{ShellKey, VCRMaster}
import util._

/** DCRBase. Parametrize base class. */
abstract class DCRBase(implicit p: Parameters)
  extends GenericParameterizedBundle(p)

/** ConfigBus.
 *
 * F1 specific config bus to convert AXILite to config commands
 */
class ConfigBusSlave(implicit p: Parameters) extends DCRBase {
  val cp = p(ShellKey).hostParams
  val addr = Output(UInt(cp.addrBits.W))
  val wdata = Output(UInt(cp.dataBits.W))
  val wr = Output(Bool())
  val rd = Output(Bool())
  val ack = Input(Bool())
  val rdata = Input(UInt(cp.dataBits.W))

  def tieoff() {
    addr := 0.U
    wdata := 0.U
    wr := false.B
    rd := false.B
  }
}

class ConfigBusMaster(implicit val p: Parameters) extends DCRBase {
  val cp = p(ShellKey).hostParams
  val addr = Input(UInt(cp.addrBits.W))
  val wdata = Input(UInt(cp.dataBits.W))
  val wr = Input(Bool())
  val rd = Input(Bool())
  val ack = Output(Bool())
  val rdata = Output(UInt(cp.dataBits.W))

  def tieoff() {
    ack := false.B
    rdata := 0.U
  }
}

/** Dandelion Control Registers (DCRF1).
 *
 * This unit provides control registers (32 and 64 bits) to be used by a control'
 * unit, typically a host processor. These registers are read-only by the core
 * at the moment but this will likely change once we add support to general purpose
 * registers that could be used as event counters by the Core unit.
 *
 * DCRF1 is customized to connect with F1 instance shells not the regular FPGAs
 */
class DCRF1(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val host = new ConfigBusMaster
    val dcr = new VCRMaster
  })

  val dp = p(ShellKey).vcrParams
  val mp = p(ShellKey).memParams
  val hp = p(ShellKey).hostParams

  // Write control (AW, W, B)
  val waddr = RegInit("h_ffff".U(hp.addrBits.W)) // init with invalid address
  val wdata = io.host.wdata
  val sWriteActive :: sWriteStretch :: sWriteResponse :: Nil = Enum(3)
  val wstate = RegInit(sWriteActive)

  // read control (AR, R)
  val sReadAddress :: sReadStretch :: sReadData :: Nil = Enum(3)
  val rstate = RegInit(sReadAddress)
  val rdata = RegInit(0.U(dp.regBits.W))

  // registers
  val nPtrs = if (mp.addrBits == 32) dp.nPtrs else 2 * dp.nPtrs
  val nTotal = dp.nCtrl + dp.nECnt + dp.nVals + nPtrs

  val reg = Seq.fill(nTotal)(RegInit(0.U(dp.regBits.W)))
  val addr = Seq.tabulate(nTotal)(i => (i * 4) + 0x500)
  val reg_map = (addr zip reg) map { case (a, r) => a.U -> r }
  val eo = dp.nCtrl
  val vo = eo + dp.nECnt
  val po = vo + dp.nVals

  io.host.ack := false.B
  io.host.rdata := rdata

  val isWriteData = wstate === sWriteStretch


  switch(wstate) {
    is(sWriteActive) {
      when(io.host.wr) {
        wstate := sWriteStretch
        waddr := io.host.addr
      }
    }
    is(sWriteStretch) {
      wstate := sWriteResponse
    }
    is(sWriteResponse) {
      wstate := sWriteActive
      io.host.ack := true.B
    }
  }

  switch(rstate) {
    is(sReadAddress) {
      when(io.host.rd) {
        rstate := sReadStretch
      }
    }
    is(sReadStretch) {
      rstate := sReadData
    }
    is(sReadData) {
      rstate := sReadAddress
      io.host.ack := true.B
    }
  }

  when(io.dcr.finish) {
    reg(0) := "b_10".U
  }.elsewhen(io.host.wr && addr(0).U === waddr && isWriteData) {
    reg(0) := wdata
  }

  for (i <- 0 until dp.nECnt) {
    when(io.dcr.ecnt(i).valid) {
      reg(eo + i) := io.dcr.ecnt(i).bits
    }.elsewhen(io.host.wr && addr(eo + i).U === waddr) {
      reg(eo + i) := wdata
    }
  }

  for (i <- 0 until (dp.nVals + nPtrs)) {
    when(io.host.wr && addr(vo + i).U === waddr && isWriteData) {
      printf(p"Write add: ${waddr} : ${wdata}\n")
      reg(vo + i) := wdata
    }
  }

  when(io.host.rd) {
    rdata := MuxLookup(io.host.addr, 0.U, reg_map)
  }

  io.dcr.launch := reg(0)(0)

  for (i <- 0 until dp.nVals) {
    io.dcr.vals(i) := reg(vo + i)
  }

  if (mp.addrBits == 32) { // 32-bit pointers
    for (i <- 0 until nPtrs) {
      io.dcr.ptrs(i) := reg(po + i)
    }
  } else { // 64-bits pointers
    for (i <- 0 until (nPtrs / 2)) {
      io.dcr.ptrs(i) := Cat(reg(po + 2 * i + 1), reg(po + 2 * i))
    }
  }
}
