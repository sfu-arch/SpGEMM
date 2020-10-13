// See LICENSE for license details.

package accel

import accel.coredf.TestCore
import chisel3._
import chisel3.util._
import chisel3.testers._
import junctions._
import config._
import accel.coredf._

class Command extends Bundle {
  val opCode = UInt()
  val op0 = UInt()
  val op1 = UInt()
  val op2 = UInt()
  val op3 = UInt()
}

object Command {
  def apply(opCode: UInt, op0: UInt = 0.U, op1: UInt = 0.U, op2: UInt = 0.U, op3: UInt = 0.U): Command = {
    val c = Wire(new Command)
    c.opCode := opCode
    c.op0 := op0
    c.op1 := op1
    c.op2 := op2
    c.op3 := op3
    c
  }
}

class AccelTester(accel: => Accelerator)(implicit val p: config.Parameters) extends BasicTester with CacheParams {

  /* NastiMaster block to emulate CPU */
  val hps = Module(new NastiMaster)
  /* Target Design */
  val dut = Module(accel)
  /* Memory model interface */
  val dutMem = Module(new NastiMemSlave) //Wire(new NastiIO)

  // Connect CPU to DUT
  dut.io.h2f <> hps.io.nasti

  // Connect DUT to Memory Model
  dutMem.io.nasti <> dut.io.f2h

  /* Tests */
  val nopCmd :: rdCmd :: wrCmd :: pollCmd :: Nil = Enum(4) // OpCodes
  val testVec = Seq(
    //       Op,       Address,         Data,          Data Mask
    Command(rdCmd, "h_C000_0800".U, "h_0000_0002".U, "h_0000_0003".U),   // Check Init/Done status reg
    Command(rdCmd, "h_C000_0804".U, "h_0000_0000".U, "h_FFFF_FFFF".U),   // Read 'Unused' space
    Command(rdCmd, "h_C000_0808".U, "h_55AA_0001".U, "h_FFFF_0000".U),   // Check Version status reg
    Command(rdCmd, "h_C000_080C".U, "h_0000_0000".U, "h_FFFF_FFFF".U),   // Check Core status reg
    Command(rdCmd, "h_C000_0810".U, "h_0000_0000".U, "h_FFFF_FFFF".U),   // Check Cache status reg
    // Start incrementing data write test
    Command(wrCmd, "h_C000_0000".U, "h_0000_0002".U, "h_F".U),           // Set Init bit
    Command(wrCmd, "h_C000_0008".U, "h_0000_0000".U, "h_F".U),           // Set Read/Write bit to zero (write)
    Command(wrCmd, "h_C000_000C".U, "h_2000_0000".U, "h_F".U),           // Set address
    Command(rdCmd, "h_C000_000C".U, "h_2000_0000".U, "h_FFFF_FFFF".U),   // Read back address
    Command(wrCmd, "h_C000_0010".U, "h_0000_0400".U, "h_F".U),           // Set test length
    Command(rdCmd, "h_C000_0010".U, "h_0000_0400".U, "h_FFFF_FFFF".U),   // Read back length
    Command(wrCmd, "h_C000_0000".U, "h_0000_0001".U, "h_F".U),           // Set start bit
    Command(pollCmd, "h_C000_0800".U, "h_0000_0001".U, "h_0000_0001".U), // Poll until done bit set
    Command(rdCmd, "h_C000_080C".U, "h_0000_0005".U, "h_0000_000F".U),   // Check Core status reg
    // Start read back test
    Command(wrCmd, "h_C000_0000".U, "h_0000_0002".U, "h_F".U),           // Set Init bit
    Command(wrCmd, "h_C000_0008".U, "h_0000_0001".U, "h_F".U),           // Set Read/Write bit to one (read)
    Command(wrCmd, "h_C000_0000".U, "h_0000_0001".U, "h_F".U),           // Set start bit
    Command(pollCmd, "h_C000_0800".U, "h_0000_0001".U, "h_0000_0001".U), // Poll until done bit set
    Command(rdCmd, "h_C000_080C".U, "h_0000_0005".U, "h_0000_000F".U),   // Check Core status reg
    Command(nopCmd)
  )

  val sIdle :: sNastiReadReq :: sNastiReadResp :: sNastiWriteReq :: sDone :: Nil = Enum(5)
  val testState = RegInit(sIdle)
  val (testCnt, testDone) = Counter(testState === sDone, testVec.size)
  val req = RegInit(NastiMasterReq())
  val reqValid = RegInit(false.B)
  val pollingRead = RegInit(false.B)

  switch(testState) {
    is(sIdle) {
      switch(VecInit(testVec)(testCnt).opCode) {
        is(rdCmd) {
          req.read := true.B
          req.addr := VecInit(testVec)(testCnt).op0
          req.tag := testCnt % 16.U
          reqValid := true.B
          testState := sNastiReadReq
          pollingRead := false.B
        }
        is(wrCmd) {
          req.read := false.B
          req.addr := VecInit(testVec)(testCnt).op0
          req.data := VecInit(testVec)(testCnt).op1
          req.mask := VecInit(testVec)(testCnt).op2
          req.tag := testCnt % 16.U
          reqValid := true.B
          testState := sNastiWriteReq
        }
        is(pollCmd) {
          req.read := true.B
          req.addr := VecInit(testVec)(testCnt).op0
          req.tag := testCnt % 16.U
          reqValid := true.B
          testState := sNastiReadReq
          pollingRead := true.B
        }
        is(nopCmd) {
          testState := sDone
        }
      }
    }
    is(sNastiReadReq) {
      when(hps.io.req.ready) {
        reqValid := false.B
        testState := sNastiReadResp
      }
    }
    is(sNastiReadResp) {
      when(hps.io.resp.valid && (hps.io.resp.bits.tag === testCnt % 16.U)) {
        val expected = VecInit(testVec)(testCnt).op1
        val mask     = VecInit(testVec)(testCnt).op2
        when((hps.io.resp.bits.data & mask) =/= (expected & mask)) {
          when(!pollingRead) {
            printf("Read fail. Expected: 0x%x. Received: 0x%x.", expected, hps.io.resp.bits.data)
            assert(false.B)
          }.otherwise {
            testState := sIdle
          }
        }.otherwise {
          testState := sDone
          pollingRead := false.B
        }
      }
    }
    is(sNastiWriteReq) {
      reqValid := false.B
      testState := sDone
    }
    is(sDone) {
      testState := sIdle
    }
  }
  hps.io.req.bits := req;
  hps.io.req.valid := reqValid

  when(testDone) {
    stop();
    stop()
  }
}

class AccelTests extends org.scalatest.FlatSpec {
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
//  "Accel" should "pass" in {
//    assert(TesterDriver execute (() => new AccelTester(new Accelerator(3,3,new Core(3,3)))))
//  }
}
