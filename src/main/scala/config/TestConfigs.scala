package config

import chisel3._
import chisel3.util._
import config._
import util._
import regfile._
import junctions._
import accel._

class TypeStackFileVerilog16bConfig extends Config((site, here, up) => {
    // Core
    case XLEN       => 16
    case TLEN       => 32
    case GLEN       => 16
    // # Max bits of cache request tag. 
    case MSHRLEN  => 8
    case TYPSZ      => 32
    case VERBOSITY  => "low"
    case COMPONENTS => "TYPLOAD;TYPOP"
    // Max size of type memory system may see
    case TRACE      => true
    case BuildRFile => (p: Parameters) => Module(new RFile(32)(p))

    //-------------------------
    // Cache
    case NWays => 1 // TODO: set-associative
    case NSets => 256
    case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
    // NastiIO
    case NastiKey => new NastiParameters(
      idBits   = 12,
      dataBits = 32,
      addrBits = 32)
  }
)

class MixedDataflowConfig extends Config((site, here, up) => {
    // Core
    case XLEN       => 16
    case TLEN       => 32
    case GLEN       => 16
    // # Max bits of cache request tag. 
    case MSHRLEN  => 8
    case TYPSZ      => 32
    case VERBOSITY  => "low"
    case COMPONENTS => "OP"
    // Max size of type memory system may see
    case TRACE      => true
    case BuildRFile => (p: Parameters) => Module(new RFile(32)(p))

    //-------------------------
    // Cache
    case NWays => 1 // TODO: set-associative
    case NSets => 256
    case CacheBlockBytes => 4 * (here(XLEN) >> 3) // 4 x 32 bits = 16B
    // NastiIO
    case NastiKey => new NastiParameters(
      idBits   = 12,
      dataBits = 32,
      addrBits = 32)
  }
)
