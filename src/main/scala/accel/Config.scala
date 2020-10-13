// See LICENSE for license details.

package accel

import chisel3.Module
import config._
import junctions._
import regfile.RFile
import shell._


//class AcceleratorConfig extends MiniConfig()

class AccelConfig extends MiniConfig( ) {

}

class VecFilterDFConfig extends Config((site, here, up) => {
  // Core
  case XLEN => 32
  case TLEN => 32
  case GLEN => 16
  // # Max bits of cache request tag.
  case MSHRLEN => 8
  case TYPSZ => 96
  case VERBOSITY => "low"
  case COMPONENTS => "TYPLOAD;TYPOP"
  // Max size of type memory system may see
  case TRACE => true
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
}
)

