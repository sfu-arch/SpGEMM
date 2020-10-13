package config


import chisel3._
import config.cde._
import chisel3.util._
import config._
import shell.{AXIParams, VCRParams, VMEParams}
import shell.AXIParams
import util._
//import examples._
import regfile._
import junctions._
import accel._
import FPU._
import FType._
import shell._

case object XLEN extends Field[Int]

case object TLEN extends Field[Int]

case object GLEN extends Field[Int]

case object MSHRLEN extends Field[Int]

case object TYPSZ extends Field[Int]

case object VERBOSITY extends Field[String]

case object COMPONENTS extends Field[String]

case object TRACE extends Field[Boolean]

case object CTRACE extends Field[Boolean]

case object BuildRFile extends Field[Parameters => AbstractRFile]

case object FTYP extends Field[FType]

case object ROWLEN extends Field[Int]

case object COLLEN extends Field[Int]

abstract trait CoreParams {
  implicit val p: Parameters
  val xlen    = p(XLEN)
  val tlen    = p(TLEN)
  val glen    = p(GLEN)
  val Typ_SZ  = p(TYPSZ)
  val Beats   = Typ_SZ / xlen
  val mshrlen = p(MSHRLEN)
  val Ftyp    = p(FTYP)
  // Debugging dumps
  val log     = p(TRACE)
  val clog    = p(CTRACE)
  val verb    = p(VERBOSITY)
  val comp    = p(COMPONENTS)

  val rowLen    = p(ROWLEN)
  val colLen    = p(COLLEN)
}

abstract class CoreBundle(implicit val p: Parameters) extends ParameterizedBundle( )(p) with CoreParams


class MiniConfig extends Config((site, here, up) => {
  // Core
  case XLEN => 32 //8
  case TLEN => 10
  case GLEN => 16
  // # Max bits of cache request tag.
  case MSHRLEN => 8
  case TYPSZ => 64
  case VERBOSITY => "low"
  //    case COMPONENTS => "TYPLOAD;TYPOP;TYPSTORE"
  case COMPONENTS => ""
  // Max size of type memory system may see
  case TRACE => true
  case CTRACE => false
  case BuildRFile => (p: Parameters) => Module(new RFile(32)(p))

  case ROWLEN => 32
  case COLLEN => 32
  //-------------------------
  // Cache
  case NWays => 1 // TODO: set-associative
  case NSets => 256
  case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
  // NastiIO
  case NastiKey => new NastiParameters(
    idBits = 12,
    dataBits = 32,
    addrBits = 32)

  case FTYP => site(XLEN) match {
    case 32 => S
    case 64 => D
    case 16 => H
    case _ => S
  }

  //-------------------------
  // Cache
  case NWays => 1 // TODO: set-associative
  case NSets => 256
  case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
  // NastiIO
  case NastiKey => new NastiParameters(
    idBits = 13,
    dataBits = 32,
    addrBits = 16)

  case FTYP => site(XLEN) match {
    case 32 => S
    case 64 => D
    case 16 => H
    case _ => S
  }

}

)


class Mat_VecConfig extends Config((site, here, up) => {
  // Core
  case XLEN => 8
  case TLEN => 10
  case GLEN => 16
  // # Max bits of cache request tag.
  case MSHRLEN => 8
  case TYPSZ => 64
  case VERBOSITY => "low"
  //    case COMPONENTS => "TYPLOAD;TYPOP;TYPSTORE"
  case COMPONENTS => "TYPOP"
  // Max size of type memory system may see
  case TRACE => true
  case CTRACE => false
  case BuildRFile => (p: Parameters) => Module(new RFile(32)(p))

  //-------------------------
  // Cache
  case NWays => 1 // TODO: set-associative
  case NSets => 256
  case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
  // NastiIO
  case NastiKey => new NastiParameters(
    idBits = 12,
    dataBits = 32,
    addrBits = 32)

  case FTYP => site(XLEN) match {
    case 32 => S
    case 64 => D
    case 16 => H
    case _ => S
  }

  //-------------------------
  // Cache
  case NWays => 1 // TODO: set-associative
  case NSets => 256
  case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
  // NastiIO
  case NastiKey => new NastiParameters(
    idBits = 12,
    dataBits = 32,
    addrBits = 32)

  case FTYP => site(XLEN) match {
    case 32 => S
    case 64 => D
    case 16 => H
    case _ => S
  }

}

)


/** PynqConfig. Shell configuration for Pynq */
//class PynqConfig extends Config((site, here, up) => {
//  case ShellKey => ShellParams(
//    hostParams = AXIParams(
//      coherent = false,
//      addrBits = 16,
//      dataBits = 32,
//      lenBits = 8,
//      userBits = 1),
//    memParams = AXIParams(
//      coherent = true,
//      addrBits = 32,
//      dataBits = 64,
//      lenBits = 8,
//      userBits = 1),
//    vcrParams = VCRParams( ),
//    vmeParams = VMEParams( ))
//})