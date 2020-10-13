package arbiters

import interfaces._
import chisel3._
import chisel3.util._
import chisel3.Module
import config._
import interfaces._

abstract class AbstractArbiterTree[T <: Data](Nops: Int, gen: T)(implicit p: Parameters)
  extends Module with CoreParams {
  val io = IO(new Bundle {
    val in  = Vec(Nops, Flipped(Decoupled(gen)))
    val out = Decoupled(Output(gen))
  })
}

class ArbiterTree[T <: Data](BaseSize: Int, NumOps: Int, gen: T, Locks: Int)(implicit val p: Parameters)
  extends AbstractArbiterTree(NumOps, gen)(p) {
  require(NumOps > 0)
  require(isPow2(BaseSize))
  val ArbiterReady       = RegInit(true.B)
  var prev               = Seq.fill(0) {
    Module(new LockingRRArbiter(gen, 4, count = Locks)).io
  }
  var toplevel           = Seq.fill(0) {
    Module(new LockingRRArbiter(gen, 4, count = Locks)).io
  }
  var Arbiters_per_Level = (NumOps + BaseSize - 1) / BaseSize
  while (Arbiters_per_Level > 0) {
    val arbiters = Seq.fill(Arbiters_per_Level) {
      Module(new LockingRRArbiter(gen, BaseSize, count = Locks)).io
    }
    if (prev.length != 0) {
      for (i <- 0 until arbiters.length * BaseSize) {
        if (i < prev.length) {
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)) <> prev(i).out
        } else {
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)).valid := false.B
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)).bits <> DontCare
        }
      }
    }

    if (prev.length == 0) {
      toplevel = arbiters
      for (i <- 0 until arbiters.length * BaseSize) {
        if (i < NumOps) {
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)) <> io.in(i)
          // arbiters(i/BaseSize).in(indexcalc(i,BaseSize)).valid := true.B;
        } else {
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)).valid := false.B;
          arbiters(i / BaseSize).in(indexcalc(i, BaseSize)).bits <> DontCare
        }
      }
    }
    prev = arbiters
    if (Arbiters_per_Level == 1) {
      Arbiters_per_Level = 0
    } else {
      Arbiters_per_Level = (Arbiters_per_Level + BaseSize - 1) / BaseSize
    }
  }
  if (NumOps > 0) {
    io.out <> prev(0).out
  }

  object indexcalc {
    def apply(i: Int, BaseSize: Int): Int = {
      i - ((i / BaseSize) * BaseSize)
    }
  }

}

