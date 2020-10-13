package node

import chisel3._
import chisel3.util._
import org.scalacheck.Prop.False
import config._
import interfaces._
import utility._
import Constants._
import utility.UniformPrintfs


/*===========================================================
=            Handshaking IO definitions                     =
===========================================================*/

/**
  * @note
  * There are three types of handshaking:
  * 1)   There is no ordering -> (No PredOp/ No SuccOp)
  * it has only vectorized output
  * @note HandshakingIONPS
  * @todo Put special case for singl output vs two outputs
  *       2)  There is ordering    -> (PredOp/ SuccOp)
  *       vectorized output/succ/pred
  * @note HandshakingIOPS
  *       3)  There is vectorized output + vectorized input
  *       No ordering
  * @todo needs to be implimented
  * @note HandshakingFusedIO
  *       4)  Control handshaking -> The only input is enable signal
  * @note HandshakingCtrl
  *       5) Control handshaking (PHI) -> There is mask and enable signal
  * @note HandshakingCtrlPhi
  *
  */

/**
  * @note Type1
  *       Handshaking IO with no ordering.
  * @note IO Bundle for Handshaking
  * @param NumOuts Number of outputs
  *
  */
class HandShakingIONPS[T <: Data](val NumOuts: Int)(gen: T)(implicit p: Parameters)
  extends CoreBundle( )(p) {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))
  // Output IO
  val Out    = Vec(NumOuts, Decoupled(gen))

  override def cloneType = new HandShakingIONPS(NumOuts)(gen).asInstanceOf[this.type]

}

/**
  * @note Type2
  *       Handshaking IO.
  * @note IO Bundle for Handshaking
  *       PredOp: Vector of RvAckIOs
  *       SuccOp: Vector of RvAckIOs
  *       Out      : Vector of Outputs
  * @param NumPredOps Number of parents
  * @param NumSuccOps Number of successors
  * @param NumOuts    Number of outputs
  *
  *
  */
class HandShakingIOPS[T <: Data](val NumPredOps: Int,
                                 val NumSuccOps: Int,
                                 val NumOuts: Int)(gen: T)(implicit p: Parameters)
  extends CoreBundle( )(p) {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))
  // Predecessor Ordering
  val PredOp = Vec(NumPredOps, Flipped(Decoupled(new ControlBundle)))
  // Successor Ordering
  val SuccOp = Vec(NumSuccOps, Decoupled(new ControlBundle( )))
  // Output IO
  val Out    = Vec(NumOuts, Decoupled(gen))

  override def cloneType = new HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(gen).asInstanceOf[this.type]

}

/**
  * @note Type3
  *       Handshaking IO with no ordering.
  * @note IO Bundle for Handshaking
  * @param NumIns  Number of Inputs
  * @param NumOuts Number of outputs
  *
  */
class HandShakingFusedIO[T <: Data](val NumIns: Int, val NumOuts: Int)(gen: T)(implicit p: Parameters)
  extends CoreBundle( )(p) {
  // Predicate enable
  val enable = Flipped(Decoupled(new ControlBundle))
  // Input IO
  val In     = Flipped(Vec(NumIns, Decoupled(gen)))
  // Output IO
  val Out    = Vec(NumOuts, Decoupled(gen))

  override def cloneType = new HandShakingFusedIO(NumIns, NumOuts)(gen).asInstanceOf[this.type]

}

/**
  * @note Type4
  *       Handshaking IO with no ordering for control nodes
  * @note IO Bundle for Handshaking
  * @param NumInputs Number of input basicBlocks
  * @param NumOuts   Number of outputs (Num Inst.)
  *
  */
class HandShakingCtrlMaskIO(val NumInputs: Int,
                            val NumOuts: Int,
                            val NumPhi: Int)(implicit p: Parameters)
  extends CoreBundle( )(p) {

  // Output IO
  val MaskBB = Vec(NumPhi, Decoupled(UInt(NumInputs.W)))
  val Out    = Vec(NumOuts, Decoupled(new ControlBundle))

  override def cloneType = new HandShakingCtrlMaskIO(NumInputs, NumOuts, NumPhi).asInstanceOf[this.type]
}

/**
  * @note Type5
  *       Handshaking IO with no ordering for control nodes
  * @note IO Bundle for Handshaking
  * @param NumOuts Number of outputs (Num Inst.)
  *
  */
class HandShakingCtrlNoMaskIO(val NumOuts: Int)(implicit p: Parameters)
  extends CoreBundle( )(p) {
  // Output IO
  val Out = Vec(NumOuts, Decoupled(new ControlBundle))

  override def cloneType = new HandShakingCtrlNoMaskIO(NumOuts).asInstanceOf[this.type]
}

/*==============================================================
=            Handshaking Implementations                       =
==============================================================*/

/**
  * @brief Handshaking between data nodes with no ordering.
  * @details Sets up base registers and hand shaking registers
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @return Module
  */

class HandShakingNPS[T <: Data](val NumOuts: Int,
                                val ID: Int)(gen: T)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingIONPS(NumOuts)(gen))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(ID.U)

  // Enable
  val enable_R       = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  // Output Handshaking
  val out_ready_R = Seq.fill(NumOuts)(RegInit(false.B))
  val out_valid_R = Seq.fill(NumOuts)(RegInit(false.B))

  /*============================*
   *           Wiring           *
   *============================*/

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    when(io.Out(i).fire( )) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }


  // Wire up enable READY and VALIDs
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire( )) {
    enable_valid_R := io.enable.valid
    enable_R <> io.enable.bits
  }

  /*===================================*
   *            Helper Checks          *
   *===================================*/
  def IsEnable(): Bool = {
    enable_R.control
  }

  def IsEnableValid(): Bool = {
    enable_valid_R
  }

  def ResetEnable(): Unit = {
    enable_valid_R := false.B
  }

  // OUTs
  def IsOutReady(): Bool = {
    if (NumOuts == 0) {
      return true.B
    } else {
      val fire_mask = (out_ready_R zip io.Out.map(_.fire)).map { case (a, b) => a | b }
      fire_mask reduce {_ & _}
    }
  }

  def IsOutValid(): Bool = {
    //    out_valid_R.asUInt.andR
    if (NumOuts == 0) {
      return true.B
    } else {
      out_valid_R.reduceLeft(_ && _)
    }
  }

  def ValidOut(): Unit = {
    (out_valid_R zip io.Out.map(_.fire)).foreach{ case (a,b) => a := b ^ true.B}
  }

  def InvalidOut(): Unit = {
    out_valid_R.foreach(_ := false.B)
  }

  def Reset(): Unit = {
    out_ready_R.foreach(_ := false.B)
    enable_valid_R := false.B
  }
}

class HandShakingFused[T <: PredicateT](val NumIns: Int, val NumOuts: Int,
                                        val ID: Int)(gen: T)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingFusedIO(NumIns, NumOuts)(new DataBundle))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(ID.U)

  // Enable
  val enable_R       = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  // Input Handshaking
  val in_predicate_W = WireInit(VecInit(Seq.fill(NumIns) {
    false.B
  }))
  val in_valid_R     = RegInit(VecInit(Seq.fill(NumIns) {
    false.B
  }))

  // Seq of registers. This has to be an array and not a vector
  // When vector it will try to instantiate registers; not possible since only
  // type description available here.
  // Do not try to dynamically dereference ops.
  val InRegs = for (i <- 0 until NumIns) yield {
    val InReg = Reg(gen)
    InReg
  }


  // Wire
  val out_valid_W = WireInit(VecInit(Seq.fill(NumOuts) {
    false.B
  }))
  val out_ready_W = WireInit(VecInit(Seq.fill(NumOuts) {
    false.B
  }))

  /*============================*
   *           Wiring           *
   *============================*/

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_W(i)
    out_ready_W(i) := io.Out(i).ready
  }


  // Wire up enable READY and VALIDs
  for (i <- 0 until NumIns) {
    io.In(i).ready := ~in_valid_R(i)
    in_predicate_W(i) := InRegs(i).predicate
    when(io.In(i).fire( )) {
      in_valid_R(i) := io.In(i).valid
      InRegs(i) := io.In(i).bits
      //InRegs(i).valid := io.In(i).valid
      InRegs(i).predicate := io.In(i).bits.predicate
    }
  }

  // Wire up enable READY and VALIDs
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire( )) {
    enable_valid_R := io.enable.valid
    enable_R <> io.enable.bits
  }

  /*===================================*
   *            Helper Checks          *
   *===================================*/
  def IsEnable(): Bool = {
    enable_R.control
  }

  def IsEnableValid(): Bool = {
    enable_valid_R
  }

  def ResetEnable(): Unit = {
    enable_valid_R := false.B
  }

  // Predicate.
  def IsInPredicate(): Bool = {
    in_predicate_W.asUInt.andR
  }

  // Ins
  def IsInValid(): Bool = {
    in_valid_R.asUInt.andR
  }

  def ValidIn(): Unit = {
    in_valid_R := VecInit(Seq.fill(NumOuts) {
      true.B
    })
  }

  def printInValid(): Unit = {
    for (i <- 0 until NumIns) yield {
      if (i != (NumIns - 1)) {
        printf("\"In(%x)\" : %x ,", i.U, in_valid_R(i))
      } else {
        printf("\"In(%x)\" : %x ", i.U, in_valid_R(i))
      }
    }
  }

  def InvalidIn(): Unit = {
    in_valid_R := VecInit(Seq.fill(NumOuts) {
      false.B
    })
  }

  // OUTs
  def IsOutReady(): Bool = {
    out_ready_W.asUInt.andR
  }

  def IsOutValid(): Bool = {
    out_valid_W.asUInt.andR
  }

  def ValidOut(): Unit = {
    out_valid_W := VecInit(Seq.fill(NumOuts) {
      true.B
    })
  }

  def InvalidOut(): Unit = {
    out_valid_W := VecInit(Seq.fill(NumOuts) {
      false.B
    })
  }

  def Reset(): Unit = {
    enable_valid_R := false.B
    in_valid_R := VecInit(Seq.fill(NumIns) {
      false.B
    })
  }
}

/**
  * @brief Handshaking between data nodes with no ordering.
  * @details Sets up base registers and hand shaking registers
  * @param NumOuts Number of outputs
  * @param ID      Node id
  * @return Module
  */

class HandShakingCtrlNPS(val NumOuts: Int,
                         val ID: Int)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingIONPS(NumOuts)(new ControlBundle))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(ID.U)

  // Enable
  val enable_R       = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  // Output Handshaking
  val out_ready_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))

  // Wire
  // val out_ready_W   = Wire(Vec(Seq.fill(NumOuts)(false.B)))

  /*============================*
   *           Wiring           *
   *============================*/

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    when(io.Out(i).fire( )) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }

  // Wire up enable READY and VALIDs
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire( )) {
    enable_valid_R := io.enable.valid
    enable_R := io.enable.bits
  }

  /*===================================*
   *            Helper Checks          *
   *===================================*/
  def IsEnable(): Bool = {
    enable_R.control
  }

  def IsEnableValid(): Bool = {
    enable_valid_R
  }

  def ResetEnable(): Unit = {
    enable_valid_R := false.B
  }

  // OUTs
  def IsOutReady(): Bool = {
    out_ready_R.asUInt.andR
  }

  def IsOutValid(): Bool = {
    //    out_valid_R.asUInt.andR
    if (NumOuts == 0) {
      return true.B
    } else {
      out_valid_R.reduceLeft(_ && _)
    }
  }

  def ValidOut(): Unit = {
    (out_valid_R zip io.Out.map(_.fire)).foreach{ case (a,b) => a := b ^ true.B}
  }

  def InvalidOut(): Unit = {
    out_valid_R.foreach(_ := false.B)
  }

  def Reset(): Unit = {
    out_ready_R.foreach(_ := false.B)
    enable_valid_R := false.B
  }
}

/**
  * @brief Handshaking between data nodes.
  * @details Sets up base registers and hand shaking registers
  * @param NumPredOps Number of parents
  * @param NumSuccOps Number of successors
  * @param NumOuts    Number of outputs
  * @param ID         Node id
  * @return Module
  */

class HandShaking[T <: Data](val NumPredOps: Int,
                             val NumSuccOps: Int,
                             val NumOuts: Int,
                             val ID: Int)(gen: T)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(gen))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(ID.U)

  // Enable
  val enable_R       = RegInit(ControlBundle.default)
  val enable_valid_R = RegInit(false.B)

  // Predecessor Handshaking
  val pred_valid_R  = Seq.fill(NumPredOps)(RegInit(false.B))
  val pred_bundle_R = Seq.fill(NumPredOps)(RegInit(ControlBundle.default))

  // Successor Handshaking. Registers needed
  val succ_ready_R  = Seq.fill(NumSuccOps)(RegInit(false.B))
  val succ_valid_R  = Seq.fill(NumSuccOps)(RegInit(false.B))
  val succ_bundle_R = Seq.fill(NumSuccOps)(RegInit(ControlBundle.default))

  // Output Handshaking
  val out_ready_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))

  // Wire
  val out_ready_W  = WireInit(VecInit(Seq.fill(NumOuts) {
    false.B
  }))
  val succ_ready_W = Seq.fill(NumSuccOps)(WireInit(false.B))

  /*==============================
  =            Wiring            =
  ==============================*/
  // Wire up Successors READYs and VALIDs
  for (i <- 0 until NumSuccOps) {
    io.SuccOp(i).valid := succ_valid_R(i)
    io.SuccOp(i).bits := succ_bundle_R(i)
    succ_ready_W(i) := io.SuccOp(i).ready
    when(io.SuccOp(i).fire( )) {
      succ_ready_R(i) := io.SuccOp(i).ready
      succ_valid_R(i) := false.B
    }
  }

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    out_ready_W(i) := io.Out(i).ready
    when(io.Out(i).fire( )) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }
  // Wire up Predecessor READY and VALIDs
  for (i <- 0 until NumPredOps) {
    io.PredOp(i).ready := ~pred_valid_R(i)
    when(io.PredOp(i).fire( )) {
      pred_valid_R(i) := io.PredOp(i).valid
      pred_bundle_R(i) := io.PredOp(i).bits
    }
  }

  //Enable is an input
  // Wire up enable READY and VALIDs
  io.enable.ready := ~enable_valid_R
  when(io.enable.fire( )) {
    enable_valid_R := io.enable.valid
    enable_R := io.enable.bits
  }

  /*=====================================
  =            Helper Checks            =
  =====================================*/
  def IsEnable(): Bool = {
    return enable_R.control
  }

  def IsEnableValid(): Bool = {
    enable_valid_R
  }

  def ResetEnable(): Unit = {
    enable_valid_R := false.B
  }

  // Check if Predecssors have fired
  def IsPredValid(): Bool = {
    if (NumPredOps == 0) {
      return true.B
    } else {
      VecInit(pred_valid_R).asUInt.andR
    }
  }

  // Fire Predecessors
  def ValidPred(): Unit = {
    pred_valid_R.map {
      _ := true.B
    }
    // pred_valid_R := Seq.fill(NumPredOps) {
    //   true.B
    // }
  }

  // Clear predessors
  def InvalidPred(): Unit = {
    pred_valid_R.foreach {
      _ := false.B
    }
    // pred_valid_R := Vec(Seq.fill(NumPredOps) {
    //   false.B
    // })
  }

  // Successors
  def IsSuccReady(): Bool = {
    if (NumSuccOps == 0) {
      return true.B
    } else {
      VecInit(succ_ready_R).asUInt.andR | VecInit(succ_ready_W).asUInt.andR
    }
  }

  def ValidSucc(): Unit = {
    succ_valid_R.foreach {
      _ := true.B
    }
  }

  def InvalidSucc(): Unit = {
    succ_valid_R.foreach {
      _ := false.B
    }
  }

  // OUTs
  def IsOutReady(): Bool = {
    out_ready_R.asUInt.andR | out_ready_W.asUInt.andR
  }

  def ValidOut(): Unit = {
    (out_valid_R zip io.Out.map(_.fire)).foreach{ case (a,b) => a := b ^ true.B}
  }

  def InvalidOut(): Unit = {
    out_valid_R := VecInit(Seq.fill(NumOuts)(false.B))
  }

  def Reset(): Unit = {
    pred_valid_R.foreach {
      _ := false.B
    }

    succ_ready_R.foreach {
      _ := false.B
    }

    out_ready_R := VecInit(Seq.fill(NumOuts) {
      false.B
    })
    enable_valid_R := false.B
  }
}

/**
  * @brief Handshaking between control nodes.
  * @details Sets up base registers and hand shaking registers
  * @param NumInputs Number of basick block inputs
  * @param NumOuts   Number of outputs
  * @param NumPhi    Number existing phi node
  * @param BID       Basic block id
  * @return Module
  */

class HandShakingCtrlMask(val NumInputs: Int,
                          val NumOuts: Int,
                          val NumPhi: Int,
                          val BID: Int)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingCtrlMaskIO(NumInputs, NumOuts, NumPhi))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(BID.U)

  // Output Handshaking
  val out_ready_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))

  // Mask handshaking
  val mask_ready_R = Seq.fill(NumPhi)(RegInit(false.B))
  val mask_valid_R = Seq.fill(NumPhi)(RegInit(false.B))

  /*============================*
   *           Wiring           *
   *============================*/

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    when(io.Out(i).fire( )) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }

  // Wire up MASK Readys and Valids
  for (i <- 0 until NumPhi) {
    io.MaskBB(i).valid := mask_valid_R(i)
    when(io.MaskBB(i).fire( )) {
      // Detecting when to reset
      mask_ready_R(i) := io.MaskBB(i).ready
      // Propagating mask
      mask_valid_R(i) := false.B
    }

  }

  /*===================================*
   *            Helper Checks          *
   *===================================*/
  // OUTs
  def IsOutReady(): Bool = {
    out_ready_R.asUInt.andR
  }

  def IsMaskReady(): Bool = {
    if (NumPhi == 0) {
      return true.B
    } else {
      VecInit(mask_ready_R).asUInt.andR
    }
  }

  def IsOutValid(): Bool = {
    out_valid_R.asUInt.andR
  }

  def IsMaskValid(): Bool = {
    if (NumPhi == 0) {
      return true.B
    } else {
      VecInit(mask_valid_R).asUInt.andR
    }
  }

  def ValidOut(): Unit = {
    out_valid_R := VecInit(Seq.fill(NumOuts)(true.B))
    mask_valid_R.foreach {
      _ := true.B
    }
  }

  def InvalidOut(): Unit = {
    out_valid_R := VecInit(Seq.fill(NumOuts)(false.B))
    mask_valid_R.foreach {
      _ := false.B
    }
  }

  def Reset(): Unit = {
    out_ready_R := VecInit(Seq.fill(NumOuts)(false.B))
    mask_ready_R.foreach {
      _ := false.B
    }
  }
}

/**
  * @brief Handshaking between control nodes.
  * @details Sets up base registers and hand shaking registers
  * @param NumInputs Number of basick block inputs
  * @param NumOuts   Number of outputs
  * @param BID       Basic block id
  * @return Module
  */

class HandShakingCtrlNoMask(val NumInputs: Int,
                            val NumOuts: Int,
                            val BID: Int)(implicit val p: Parameters)
  extends Module with CoreParams with UniformPrintfs {

  lazy val io = IO(new HandShakingCtrlNoMaskIO(NumOuts))

  /*=================================
  =            Registers            =
  =================================*/
  // Extra information
  val token    = RegInit(0.U)
  val nodeID_R = RegInit(BID.U)

  // Output Handshaking
  val out_ready_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))
  val out_valid_R = RegInit(VecInit(Seq.fill(NumOuts)(false.B)))

  /*============================*
   *           Wiring           *
   *============================*/

  // Wire up OUT READYs and VALIDs
  for (i <- 0 until NumOuts) {
    io.Out(i).valid := out_valid_R(i)
    when(io.Out(i).fire( )) {
      // Detecting when to reset
      out_ready_R(i) := io.Out(i).ready
      // Propagating output
      out_valid_R(i) := false.B
    }
  }

  /*===================================*
   *            Helper Checks          *
   *===================================*/
  // OUTs
  def IsOutReady(): Bool = {
    out_ready_R.asUInt.andR
  }

  def IsOutValid(): Bool = {
    out_valid_R.asUInt.andR
  }

  def ValidOut(): Unit = {
    out_valid_R := VecInit(Seq.fill(NumOuts) {
      true.B
    })
  }

  def InvalidOut(): Unit = {
    out_valid_R := VecInit(Seq.fill(NumOuts)(false.B))

  }

  def Reset(): Unit = {
    out_ready_R := VecInit(Seq.fill(NumOuts)(false.B))
    InvalidOut( )
  }

}


class HandShakingAliasIO[T <: Data](NumPredOps: Int,
                                    NumSuccOps: Int,
                                    val NumAliasPredOps: Int = 0,
                                    val NumAliasSuccOps: Int = 0,
                                    NumOuts: Int
                                   )(gen: T)(implicit p: Parameters)
  extends HandShakingIOPS(NumPredOps, NumSuccOps, NumOuts)(gen) {
  val InA  = (new Bundle {
    val In     = Vec(NumAliasPredOps, Flipped(Decoupled(new DataBundle)))
    // Predessor
    val PredOp = Vec(NumAliasPredOps, Flipped(Decoupled(new ControlBundle)))
  })
  val OutA = (new Bundle {
    /* Need to explicitly specify output when mixing directions */
    // Successor Ordering
    val SuccOp = Output(Vec(NumAliasSuccOps, Decoupled(new ControlBundle)))
    // Output IO
    val Out    = Output(Vec(NumAliasSuccOps, Decoupled(new DataBundle)))
  })

  override def cloneType = new HandShakingAliasIO(NumPredOps, NumSuccOps, NumAliasPredOps, NumAliasSuccOps, NumOuts)(gen).asInstanceOf[this.type]
}

class HandShakingAlias[T <: Data](NumPredOps: Int,
                                  NumSuccOps: Int,
                                  val NumAliasPredOps: Int = 0,
                                  val NumAliasSuccOps: Int = 0,
                                  NumOuts: Int,
                                  ID: Int)(gen: T)(implicit p: Parameters)
  extends HandShaking(NumPredOps, NumSuccOps, NumOuts, ID)(gen)(p) {

  override lazy val io = IO(new HandShakingAliasIO(NumPredOps, NumSuccOps, NumAliasPredOps, NumAliasSuccOps, NumOuts)(gen))

  // Alias Predecessor Handshaking
  val alias_pred_valid_R  = Seq.fill(NumAliasPredOps)(RegInit(false.B))
  val alias_pred_bundle_R = Seq.fill(NumAliasPredOps)(RegInit(ControlBundle.default))

  // Alias input
  val alias_in_valid_R  = Seq.fill(NumAliasPredOps)(RegInit(false.B))
  val alias_in_bundle_R = Seq.fill(NumAliasPredOps)(RegInit(DataBundle.default))

  // Alias Successor HandShaking
  val alias_succ_ready_R  = Seq.fill(NumAliasSuccOps)(RegInit(false.B))
  val alias_succ_valid_R  = Seq.fill(NumAliasSuccOps)(RegInit(false.B))
  val alias_succ_bundle_R = Seq.fill(NumAliasSuccOps)(RegInit(ControlBundle.default))

  // Alias output
  val alias_out_ready_R = Seq.fill(NumAliasSuccOps)(RegInit(false.B))
  val alias_out_valid_R = Seq.fill(NumAliasSuccOps)(RegInit(false.B))

  // Wire
  val alias_out_ready_W = for (i <- 0 until NumAliasSuccOps) yield {
    io.OutA.Out(i).ready
  }

  val alias_succ_ready_W = Seq.fill(NumAliasSuccOps)(WireInit(false.B))


  //  printf(p"\n Succ: ${io.Alias.SuccOp(0)} Out ${io.Alias.Out(0)} \n")
  // Why not a CAM?
  // Initially, a CAM was planned. However there are a number of challenges
  // 1) The CAM has a single write port which means all incoming aliases have to be multiplexed
  //    But this cannot be achieved in a single step as we also need to coordinate the ready-valid signals for for
  //    each incoming alias source separately.
  // 2) We would need arbitration logic in front of the CAM to access the single write port
  // 3) Alternatively, we use registers and hope the compiler and user will prudently use it.

  // Wire up Predecessor READY and VALIDs
  for (i <- 0 until NumAliasPredOps) {
    io.InA.PredOp(i).ready := ~alias_pred_valid_R(i)
    when(io.InA.PredOp(i).fire( )) {
      alias_pred_valid_R(i) := io.InA.PredOp(i).valid
      alias_pred_bundle_R(i) := io.InA.PredOp(i).bits
    }
  }

  for (i <- 0 until NumAliasPredOps) {
    io.InA.In(i).ready := ~alias_in_valid_R(i)
    when(io.InA.In(i).fire( )) {
      alias_in_valid_R(i) := io.InA.In(i).valid
      alias_in_bundle_R(i) := io.InA.In(i).bits
    }
  }

  def AliasInfoAvail(): Bool = {
    if (NumAliasPredOps == 0) {
      true.B
    } else {
      alias_in_valid_R.reduceLeft(_ && _)
    }
  }

  def AliasReady(address: UInt): Bool = {

    val alias_equals = (0 until NumAliasPredOps).map(i => IsAlias(alias_in_bundle_R(i).data, address, xlen, MT_W))

    if (NumAliasPredOps != 0) {
      val hits = VecInit(alias_equals).asUInt // % Turn into a bit vector
      val pred_valid = VecInit(alias_pred_valid_R).asUInt
      val waitlist = (pred_valid | ~hits)
      val result = waitlist.andR && AliasInfoAvail( )
      printf(p"\n Alias_R (): ${VecInit(alias_in_valid_R)} " +
        p"Alias addr: ${alias_in_bundle_R(0).data} hits: ${hits} " +
        p"pred_valid ${pred_valid} waitlist ${waitlist} result ${result}")
      result
    } else {
      true.B
    }
  }

  // Hit  Arrived
  // 1     1        1
  // 1     0        0
  // 0     1        1
  // 0     0        1

  def ConnectAliasInfo(address: UInt, taskID: UInt): Unit = {
    // Wire up OUT READYs and VALIDs
    for (i <- 0 until NumAliasSuccOps) {
      io.OutA.Out(i).valid := alias_out_valid_R(i)
      io.OutA.Out(i).bits.data := address
      io.OutA.Out(i).bits.taskID := taskID
      io.OutA.Out(i).bits.predicate := 0.U
      when(io.OutA.Out(i).fire( )) {
        // Detecting when to reset
        alias_out_ready_R(i) := io.OutA.Out(i).ready
        // Propagating output
        alias_out_valid_R(i) := false.B
      }
    }
  }

  //
  //   // Wire up Successors READYs and VALIDs
  for (i <- 0 until NumAliasSuccOps) {
    io.OutA.SuccOp(i).valid := alias_succ_valid_R(i)
    io.OutA.SuccOp(i).bits := alias_succ_bundle_R(i)
    alias_succ_ready_W(i) := io.OutA.SuccOp(i).ready
    when(io.OutA.SuccOp(i).fire( )) {
      alias_succ_ready_R(i) := io.OutA.SuccOp(i).ready
      alias_succ_valid_R(i) := false.B
    }
  }

  /*=================================
  =            Helpers            =
  =================================*/

  // Check if Predecssors have fired
  def IsAliasPredValid(): Bool = {
    if (NumAliasPredOps == 0) {
      return true.B
    } else {
      VecInit(alias_pred_valid_R).asUInt.andR
    }
  }

  // Fire Predecessors
  def ValidAliasPred(): Unit = {
    alias_pred_valid_R.map {
      _ := true.B
    }
  }

  // Clear predessors
  def InvalidAliasPred(): Unit = {
    alias_pred_valid_R.foreach {
      _ := false.B
    }
  }

  // Successors
  def IsAliasSuccReady(): Bool = {
    if (NumAliasSuccOps == 0) {
      return true.B
    } else {
      VecInit(alias_succ_ready_R).asUInt.andR | VecInit(alias_succ_ready_W).asUInt.andR
    }
  }

  def ValidAliasSucc(): Unit = {
    alias_succ_valid_R.foreach {
      _ := true.B
    }
  }

  def InvalidAliasSucc(): Unit = {
    alias_succ_valid_R.foreach {
      _ := false.B
    }
  }

  // OUTs
  def IsAliasOutReady(): Bool = {
    if (NumAliasSuccOps == 0) {
      return true.B
    } else {
      VecInit(alias_out_ready_R).asUInt.andR | VecInit(alias_out_ready_W).asUInt.andR
    }
  }

  def ValidAliasOut(): Unit = {
    alias_out_valid_R.foreach {
      _ := true.B
    }
  }

  def InvalidAliasOut(): Unit = {
    alias_out_valid_R.foreach {
      _ := false.B
    }
  }

  override def Reset(): Unit = {
    pred_valid_R.foreach {
      _ := false.B
    }

    succ_ready_R.foreach {
      _ := false.B
    }

    out_ready_R.foreach {
      _ := false.B
    }

    enable_valid_R := false.B

    alias_in_valid_R.foreach {
      _ := false.B
    }
    alias_pred_valid_R.foreach {
      _ := false.B
    }

    alias_succ_ready_R.foreach {
      _ := false.B
    }

    alias_out_ready_R.foreach {
      _ := false.B
    }
  }

}

// Helper function for providing CAM valids and hits
// Check if all alias have completed so that we can release the resource.

// missed already | hit and arrived   .andR
// (~hits | ioAliasPred).andR. This means I can start memory op
