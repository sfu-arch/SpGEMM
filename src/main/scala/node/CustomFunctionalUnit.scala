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

class CustomFunctionalNodeIO(NumIns: Int, NumOuts: Int)
                            (implicit p: Parameters)
  extends HandShakingFusedIO(NumIns, NumOuts)(new DataBundle) {

  override def cloneType = new CustomFunctionalNodeIO(NumIns, NumOuts).asInstanceOf[this.type]
}

class CustomFunctionalNode(NumIns: Int, NumOuts: Int, ID: Int, opCode: String)
                          (sign: Boolean)
                          (implicit p: Parameters)
  extends HandShakingFused(NumIns, NumOuts, ID)(new DataBundle)(p) {
  override lazy val io = IO(new CustomFunctionalNodeIO(NumIns, NumOuts))

  // Printf debugging
  def PrintOut(): Unit = {
    for (i <- 0 until NumIns) yield {
      printf("\"O_%x(D,P)\" : \"%x,%x\",", i.U, InRegs(i).data, InRegs(i).predicate)
    }
  }


  val s_idle :: s_LATCH :: s_COMPUTE :: Nil = Enum(3)
  val state = RegInit(s_idle)

  /*==========================================*
   *           Predicate Evaluation           *
   *==========================================*/

  val predicate = IsInPredicate() & IsEnable()
  val start = IsInValid() & IsEnableValid()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  // See HandShakingFusedDataBundle

  /*============================================*
   *            ACTIONS (possibly dangerous)    *
   *============================================*/

  when(start & state =/= s_COMPUTE) {
    state := s_COMPUTE
  }

  /*==========================================*
   *            Output Handshaking and Reset  *
   *==========================================*/

  //Always keep the output invalid unless all the outputs are ready
  InvalidOut()
  when(IsOutReady() & (state === s_COMPUTE)) {
    // Reset data
    state := s_idle

    // Valid out is a wire
    ValidOut()

    //Reset output
    Reset()
  }

  var signed = if (sign == true) "S" else "U"

  override val printfSigil = opCode + xlen + "_" + signed + "_" + ID + ": "

  if (log == true && (comp contains "OP")) {
    val x = RegInit(0.U(xlen.W))
    x := x + 1.U

    verb match {
      case "high" => {}
      case "med" => {}
      case "low" => {
        printfInfo("Cycle %d : \n{ \"Inputs\": {", x)
        printInValid()
        printf("},\n")
        printf("\"State\": {\"State\": \"%x\",", state)
        PrintOut()
        printf("},\n")
        printf("\"Outputs\": {\"Out\": %x}", io.Out(0).fire())
        printf("}\n")
      }
      case everythingElse => {}
    }
  }
}

/**
  * @note these units should be built manually
  */
//class TestCFU(ID: Int, NumOps: Int, OpCodes: Array[String])(sign: Boolean)(implicit p: Parameters)
class TestCFU(ID: Int, NumOps: Int, NumIns: Int, NumOuts: Int)(sign: Boolean)(implicit p: Parameters)
  extends CustomFunctionalNode(NumIns = NumIns, NumOuts = NumOuts, ID = ID, opCode = "TEST")(sign)(p) {

  io.Out(0).bits.data := ((((((InRegs(0).data & InRegs(1).data) ^ InRegs(2).data) << InRegs(3).data(4, 0).asUInt) & InRegs(4).data) + InRegs(5).data) >> InRegs(6).data(4, 0).asUInt)
  io.Out(0).bits.taskID := InRegs(0).taskID
  io.Out(0).bits.predicate := InRegs(0).predicate & InRegs(1).predicate & InRegs(2).predicate & InRegs(3).predicate & InRegs(4).predicate &
    InRegs(5).predicate & InRegs(6).predicate
//   io.Out(0).bits.valid := true.B

  // Declare chain of FUs
  //val FUs = for (i <- 0 until OpCodes.length) yield {
  //val FU = Module(new UALU(xlen, OpCodes(i)))
  //FU
  //}


  //// The first FU.
  //FUs(0).io.in1 := InRegs(0).data
  //FUs(0).io.in2 := InRegs(1).data
  //io.Out(0).bits.data := FUs(0).io.out
  //io.Out(0).bits.predicate := InRegs(0).predicate & InRegs(1).predicate
  //// The other ones.
  //for (i <- 1 until OpCodes.length) {
  //FUs(i).io.in1 := FUs(i - 1).io.out
  //FUs(i).io.in2 := InRegs(i + 1).data
  //io.Out(i).bits.data := FUs(i).io.out
  //io.Out(i).bits.predicate := io.Out(i - 1).bits.predicate & InRegs(i + 1).predicate
  //}

  //io.Out((OpCodes.length)).bits.data := FUs((OpCodes.length) - 1).io.out
  //io.Out((OpCodes.length)).bits.predicate := io.Out((OpCodes.length) - 2).bits.predicate & InRegs(OpCodes.length).predicate
}
