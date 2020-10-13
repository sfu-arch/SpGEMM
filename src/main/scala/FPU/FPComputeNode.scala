package FPU

import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, OrderedDecoupledHWIOTester, PeekPokeTester}
import chisel3.Module
import chisel3.testers._
import chisel3.util._
import org.scalatest.{FlatSpec, Matchers}
import config._
import interfaces._
import muxes._
import util._
import node._
import FType._

/**
  * [FPComputeNodeIO description]
  */
class FPComputeNodeIO(NumOuts: Int)
                     (implicit p: Parameters)
  extends HandShakingIONPS(NumOuts)(new DataBundle) {
  // LeftIO: Left input data for computation
  val LeftIO = Flipped(Decoupled(new DataBundle( )))

  // RightIO: Right input data for computation
  val RightIO = Flipped(Decoupled(new DataBundle( )))

  override def cloneType = new FPComputeNodeIO(NumOuts).asInstanceOf[this.type]

}

/**
  * [FPComputeNode description]
  */
class FPComputeNode(NumOuts: Int, ID: Int, opCode: String)
                   (t: FType)
                   (implicit p: Parameters,
                    name: sourcecode.Name,
                    file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/
  require(t.ieeeWidth == xlen, "Floating point width does not match machine width")
  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  val FU = Module(new FPALU(FloatingPoint(t), opCode))
  FU.io.in1.value := left_R.data
  FU.io.in2.value := right_R.data

  // val FU = Module(new FPMAC(xlen, opCode, t))
  // FU.io.in1 := left_R.data
  // FU.io.in2 := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          io.Out.map(_.valid) foreach (_ := true.B)
          when(enable_R.control) {
            out_data_R.data := FU.io.out
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, FU.io.out)
        }
      }
    }
  }

}


class altfp_adder_13(DATA: Int = 32, ADDR: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk_en = Input(Bool( ))
    val clock  = Input(Clock( ))
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  setResource("/verilog/altfp_adder_13.v")

}

class altfp_subtractor_14(DATA: Int = 32, ADDR: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk_en = Input(Bool( ))
    val clock  = Input(Clock( ))
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  setResource("/verilog/altfp_subtractor_14.v")

}

class altfp_multiplier_11(DATA: Int = 32, ADDR: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk_en = Input(Bool( ))
    val clock  = Input(Clock( ))
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  setResource("/verilog/altfp_multiplier_11.v")

}

class altfp_divider_33(DATA: Int = 32, ADDR: Int = 32) extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk_en = Input(Bool( ))
    val clock  = Input(Clock( ))
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  setResource("/verilog/altfp_divider_33.v")

}


class FPAdder(DATA: Int = 32, ADDR: Int = 32) extends Module {
  val io = IO(new Bundle {
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  val fpadder = Module(new altfp_adder_13( ))

  fpadder.io.clock := clock
  fpadder.io.clk_en := true.B
  fpadder.io.dataa <> io.dataa
  fpadder.io.datab <> io.datab
  io.result <> fpadder.io.result

}

class FPSubtractor(DATA: Int = 32, ADDR: Int = 32) extends Module {
  val io = IO(new Bundle {
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  val fpsubtractor = Module(new altfp_subtractor_14( ))

  fpsubtractor.io.clock := clock
  fpsubtractor.io.clk_en := true.B
  fpsubtractor.io.dataa <> io.dataa
  fpsubtractor.io.datab <> io.datab
  io.result <> fpsubtractor.io.result

}

class FPMultiplier(DATA: Int = 32, ADDR: Int = 32) extends Module {
  val io = IO(new Bundle {
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  val fpmultiplier = Module(new altfp_multiplier_11( ))

  fpmultiplier.io.clock := clock
  fpmultiplier.io.clk_en := true.B
  fpmultiplier.io.dataa <> io.dataa
  fpmultiplier.io.datab <> io.datab
  io.result <> fpmultiplier.io.result

}

class FPDivider(DATA: Int = 32, ADDR: Int = 32) extends Module {
  val io = IO(new Bundle {
    val dataa  = Input(UInt(ADDR.W))
    val datab  = Input(UInt(DATA.W))
    val result = Output(UInt(DATA.W))
  })

  val fpdivider = Module(new altfp_divider_33( ))

  fpdivider.io.clock := clock
  fpdivider.io.clk_en := true.B
  fpdivider.io.dataa <> io.dataa
  fpdivider.io.datab <> io.datab
  io.result <> fpdivider.io.result

}


/**
  * [FPComputeNode description]
  */
class FPCustomAdderNode(NumOuts: Int, ID: Int, opCode: String)
                       (t: FType)
                       (implicit p: Parameters,
                        name: sourcecode.Name,
                        file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  //  val FU = Module(new FPUALU(xlen, opCode, t))
  val FU = Module(new FPAdder( ))

  FU.io.dataa := left_R.data
  FU.io.datab := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          when(enable_R.control) {
            out_data_R.data := FU.io.result
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, FU.io.result)
        }
      }
    }
  }

}


class FPCustomSubtractorNode(NumOuts: Int, ID: Int, opCode: String)
                            (t: FType)
                            (implicit p: Parameters,
                             name: sourcecode.Name,
                             file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  //  val FU = Module(new FPUALU(xlen, opCode, t))
  val FU = Module(new FPSubtractor( ))

  FU.io.dataa := left_R.data
  FU.io.datab := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          when(enable_R.control) {
            out_data_R.data := FU.io.result
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, FU.io.result)
        }
      }
    }
  }

}


class FPCustomMultiplierNode(NumOuts: Int, ID: Int, opCode: String)
                            (t: FType)
                            (implicit p: Parameters,
                             name: sourcecode.Name,
                             file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  //  val FU = Module(new FPUALU(xlen, opCode, t))
  val FU = Module(new FPMultiplier( ))

  FU.io.dataa := left_R.data
  FU.io.datab := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          when(enable_R.control) {
            out_data_R.data := FU.io.result
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, FU.io.result)
        }
      }
    }
  }

}


class FPCustomDividerNode(NumOuts: Int, ID: Int, opCode: String)
                         (t: FType)
                         (implicit p: Parameters,
                          name: sourcecode.Name,
                          file: sourcecode.File)
  extends HandShakingNPS(NumOuts, ID)(new DataBundle( ))(p) {
  override lazy val io = IO(new FPComputeNodeIO(NumOuts))

  // Printf debugging
  val node_name   = name.value
  val module_name = file.value.split("/").tail.last.split("\\.").head.capitalize

  override val printfSigil = "[" + module_name + "] " + node_name + ": " + ID + " "
  //  override val printfSigil = "Node (COMP - " + opCode + ") ID: " + ID + " "
  val (cycleCount, _) = Counter(true.B, 32 * 1024)

  /*===========================================*
   *            Registers                      *
   *===========================================*/
  // Left Input
  val left_R       = RegInit(DataBundle.default)
  val left_valid_R = RegInit(false.B)

  // Right Input
  val right_R       = RegInit(DataBundle.default)
  val right_valid_R = RegInit(false.B)

  val task_ID_R = RegNext(next = enable_R.taskID)

  //Output register
  val out_data_R = RegInit(DataBundle.default)

  val s_IDLE :: s_COMPUTE :: Nil = Enum(2)
  val state                      = RegInit(s_IDLE)


  val predicate = left_R.predicate & right_R.predicate // & IsEnable()

  /*===============================================*
   *            Latch inputs. Wire up output       *
   *===============================================*/

  //Instantiate ALU with selected code. IEEE ALU. IEEE in/IEEE out
  //  val FU = Module(new FPUALU(xlen, opCode, t))
  val FU = Module(new FPDivider( ))

  FU.io.dataa := left_R.data
  FU.io.datab := right_R.data

  io.LeftIO.ready := ~left_valid_R
  when(io.LeftIO.fire( )) {
    left_R <> io.LeftIO.bits
    left_valid_R := true.B
  }

  io.RightIO.ready := ~right_valid_R
  when(io.RightIO.fire( )) {
    right_R <> io.RightIO.bits
    right_valid_R := true.B
  }

  // Wire up Outputs
  for (i <- 0 until NumOuts) {
    //io.Out(i).bits.data := FU.io.out
    //io.Out(i).bits.predicate := predicate
    // The taskID's should be identical except in the case
    // when one input is tied to a constant.  In that case
    // the taskID will be zero.  Logical OR'ing the IDs
    // Should produce a valid ID in either case regardless of
    // which input is constant.
    //io.Out(i).bits.taskID := left_R.taskID | right_R.taskID
    io.Out(i).bits := out_data_R
  }

  /*============================================*
   *            State Machine                   *
   *============================================*/
  switch(state) {
    is(s_IDLE) {
      when(enable_valid_R) {
        when(left_valid_R && right_valid_R) {
          ValidOut( )
          when(enable_R.control) {
            out_data_R.data := FU.io.result
            out_data_R.predicate := predicate
            out_data_R.taskID := left_R.taskID | right_R.taskID | enable_R.taskID
          }
          state := s_COMPUTE
        }
      }
    }
    is(s_COMPUTE) {
      when(IsOutReady( )) {
        // Reset data
        //left_R := DataBundle.default
        //right_R := DataBundle.default
        left_valid_R := false.B
        right_valid_R := false.B
        //Reset state
        state := s_IDLE
        //Reset output
        out_data_R.predicate := false.B
        Reset( )
        if (log) {
          printf("[LOG] " + "[" + module_name + "] " + "[TID->%d] "
            + node_name + ": Output fired @ %d, Value: %x\n", task_ID_R, cycleCount, FU.io.result)
        }
      }
    }
  }

}
