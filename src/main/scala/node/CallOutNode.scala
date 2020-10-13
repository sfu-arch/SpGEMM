package node

import chisel3._
import chisel3.Module
import junctions._

import config._
import interfaces._
import util._
import utility.UniformPrintfs

class CallOutNodeIO(val argTypes: Seq[Int],
                    NumPredOps: Int,
                    NumSuccOps: Int,
                    NumOuts: Int)(implicit p: Parameters)
  extends HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(new Call(argTypes)) {
  val In = Flipped(new VariableDecoupledData(argTypes)) // Requests from calling block(s)

  //  3.1
  override def cloneType = new CallOutNodeIO(argTypes, NumPredOps, NumSuccOps, NumOuts).asInstanceOf[this.type]
}

class CallOutNode(ID: Int, val argTypes: Seq[Int], NumSuccOps: Int = 0, NoReturn: Bool = false.B)
                 (implicit p: Parameters,
                  name: sourcecode.Name,
                  file: sourcecode.File)
  extends HandShaking(0, NumSuccOps, 1, ID)(new Call(argTypes))(p) with UniformPrintfs {

  override lazy val io = IO(new CallOutNodeIO(argTypes, 0, NumSuccOps, 1)(p))
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = module_name + ": " + node_name + ID + " "


  val s_idle :: s_Done :: Nil = Enum(2)
  val state = RegInit(s_idle)

  val data_R = Reg(new VariableData(argTypes))
  val data_valid_R = Seq.fill(argTypes.length)(RegInit(false.B))

  for (i <- argTypes.indices) {
    io.In.elements(s"field$i").ready := ~data_valid_R(i)
    when(io.In(s"field$i").fire()) {
      data_R(s"field$i") <> io.In(s"field$i").bits
      data_valid_R(i) := true.B
    }
  }

  when(io.enable.fire()) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }

  io.Out(0).bits.data := data_R
  io.Out(0).bits.enable := enable_R

  val error = WireInit(false.B)
  switch(state) {
    is(s_idle) {
      when(enable_valid_R && data_valid_R.reduce(_ & _)) {
        ValidSucc()
        // Fire outputs if we're not returning (even if control=false.B)
        // Otherwise don't fire outputs if control = false and assume CallInNode will fake the response
        //when (NoReturn) || enable_R.control) {
        ValidOut()
        for (i <- argTypes.indices) {
          when(data_R(s"field$i").taskID =/= enable_R.taskID) {
            error := true.B
            printfError("#####%d", error)
            printfError("##### Data[%d] taskID: %d <> Enable taskID: %d\n", i.U, data_R(s"field$i").taskID, enable_R.taskID)
          }
        }
        state := s_Done
      }
    }
    is(s_Done) {
      when(IsOutReady() && IsSuccReady()) {
        // Clear all the data valid states.
        data_valid_R.foreach(_ := false.B)
        // Clear all other state
        Reset()
        // Reset state.
        state := s_idle
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d\n", enable_R.taskID, cycleCount)
        }
      }
    }
  }
}

class CallOutNode2(ID: Int, val argTypes: Seq[Int], NumSuccOps: Int = 0, NoReturn: Bool = false.B)
                  (implicit p: Parameters,
                   name: sourcecode.Name,
                   file: sourcecode.File)
  extends HandShaking(0, NumSuccOps, 1, ID)(new Call(argTypes))(p) with UniformPrintfs {

  override lazy val io = IO(new CallOutNodeIO(argTypes, 0, NumSuccOps, 1)(p))
  val node_name = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize
  val (cycleCount, _) = Counter(true.B, 32 * 1024)
  override val printfSigil = module_name + ": " + node_name + ID + " "


  val s_idle :: s_Done :: Nil = Enum(2)
  val state = RegInit(s_idle)

  val data_R = Reg(new VariableData(argTypes))
  val data_valid_R = RegInit(VecInit(Seq.fill(argTypes.length) {
    false.B
  }))

  for (i <- argTypes.indices) {
    when(io.In(s"field$i").fire()) {
      data_R(s"field$i") := io.In(s"field$i").bits
      data_valid_R(i) := true.B
    }
    io.In.elements(s"field$i").ready := ~data_valid_R(i)
  }

  when(io.enable.fire()) {
    succ_bundle_R.foreach(_ := io.enable.bits)
  }
  io.Out(0).bits.data := data_R
  io.Out(0).bits.enable := enable_R

  val error = WireInit(false.B)
  switch(state) {
    is(s_idle) {
      when(enable_valid_R && data_valid_R.asUInt.andR) {
        ValidSucc()
        // Fire outputs if we're not returning (even if control=false.B)
        // Otherwise don't fire outputs if control = false and assume CallInNode will fake the response
        when(NoReturn || enable_R.control) {
          ValidOut()
          for (i <- argTypes.indices) {
            when(data_R(s"field$i").taskID =/= enable_R.taskID) {
              error := true.B
              printfError("#####%d\n", error)
            }
          }
        }
        state := s_Done
      }
    }
    is(s_Done) {
      when(IsOutReady() && IsSuccReady()) {
        // Clear all the data valid states.
        data_valid_R.foreach(_ := false.B)
        // Clear all other state
        Reset()
        // Reset state.
        state := s_idle
        if (log) {
          printf("[LOG] " + "[" + module_name + "] [TID->%d] " + node_name + ": Output fired @ %d\n", enable_R.taskID, cycleCount)
        }
      }
    }
  }
}
