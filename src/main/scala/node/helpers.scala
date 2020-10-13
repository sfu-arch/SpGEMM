/*==================================================
  =            Errata (Only compatible with xlen=32b).
             Not fully compatible with MT_D accesses. Data2Sign needs to be fixed     =
===================================================*/


package node

import chisel3._
import chisel3.util._
import utility.Constants._

/**
  * @todo xlen = 64 bit and MT_D not supported yet.
  */
object ReadByteMask {
  def apply(sel: UInt, address: UInt, xlen: Int): UInt = {
    val wordmask = Typ2ByteMask(sel)
    val alignment = address(log2Ceil(xlen / 8) - 1, 0)
    val mask = (wordmask << alignment)
    return mask
  }
}

object Typ2BitMask {
  def apply(sel: UInt): UInt = {
    val mask = Mux(sel === MT_H.asUInt || sel === MT_HU.asUInt, Fill(16, 1.U),
      Mux(sel === MT_B.asUInt || sel === MT_BU.asUInt, Fill(8, 1.U),
        Mux(sel === MT_W.asUInt || sel === MT_WU.asUInt, Fill(32, 1.U),
          Fill(64, 1.U))))
    return mask
  }
}


object ReadBitMask {
  def apply(sel: UInt, address: UInt, xlen: Int): UInt = {
    val wordmask = Typ2BitMask(sel)
    val alignment = Cat(address(log2Ceil(xlen / 8) - 1, 0), 0.U(3.W))
    val mask = (wordmask << alignment)
    return mask
  }
}


object Typ2ByteMask {
  def apply(sel: UInt): UInt = {
    val mask = Mux(sel === MT_H.asUInt || sel === MT_HU.asUInt, Fill(2, 1.U),
      Mux(sel === MT_B.asUInt || sel === MT_BU.asUInt, Fill(1, 1.U),
        Mux(sel === MT_W.asUInt || sel === MT_WU.asUInt, Fill(4, 1.U),
          Fill(8, 1.U))))
    return mask
  }
}

/**
  * @todo This has to change to handle doubles.
  */
object Data2Sign {
  // If xlen changes from 32 this sign extension needs to be fixed here. 
  def apply(data: Bits, typ: Bits, xlen: Int): Bits = {
    //@todo check whether casting Bits to UInt doesn't introduce bug
    val out = Mux(typ.asUInt === MT_H, Cat(Fill(16, data(15)), data(15, 0)),
      Mux(typ.asUInt === MT_HU, Cat(Fill(16, 0.U), data(15, 0)),
        Mux(typ.asUInt === MT_B, Cat(Fill(24, data(7)), data(7, 0)),
          Mux(typ.asUInt === MT_BU, Cat(Fill(24, 0.U), data(7, 0)),
            data(31, 0)))))
    return out
  }
}

object Data2Sign16b {
  // If xlen changes from 32 this sign extension needs to be fixed here. 
  def apply(data: Bits, typ: Bits, xlen: Int): Bits = {
    //@todo check whether casting Bits to UInt doesn't introduce bug
    val out = Mux(typ.asUInt === MT_H, data(15, 0),
      Mux(typ.asUInt === MT_HU, data(15, 0),
        Mux(typ.asUInt === MT_B, Cat(Fill(8, data(7)), data(7, 0)),
          Mux(typ.asUInt === MT_BU, Cat(Fill(8, 0.U), data(7, 0)),
            data(15, 0)))))
    return out
  }
}

/**
  * @todo xlen = 64 bit and MT_D not supported yet.
  */
object IsAlias {
  def apply(lhs: UInt, rhs: UInt, xlen: Int, typ: UInt): Bool = {
    val xlen_bytes = xlen / 8
    val lhsword = lhs(xlen - 1, log2Ceil(xlen / 8))
    val rhsword = rhs(xlen - 1, log2Ceil(xlen / 8))
    val lhsmask = ReadByteMask(typ, lhs, xlen)
    val rhsmask = ReadByteMask(typ, rhs, xlen)
    (lhsword === rhsword) && ((lhsmask & rhsmask) =/= 0.U)
  }
}