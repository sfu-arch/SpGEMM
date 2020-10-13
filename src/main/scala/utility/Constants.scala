/*==================================================
=            Errata MT_D accesses not supported     =
===================================================*/

package utility

import chisel3._
/**
 * @todo   MT_D double access not supported yet.
 * 
 */
trait MemoryOpConstants 
{
   val MT_X  = 0.U(3.W)
   val MT_B  = 1.U(3.W)
   val MT_H  = 2.U(3.W)
   val MT_W  = 3.U(3.W)
   val MT_D  = 4.U(3.W)
   val MT_BU = 5.U(3.W)
   val MT_HU = 6.U(3.W)
   val MT_WU = 7.U(3.W)
   // Maximum size of access type
   val MT_MAX_SIZE = 2

   object Margin extends Enumeration {
      type Margin = Value;
      val MT_X,MT_B,MT_H,MT_W,MT_D,MT_BU,MT_HU,MT_WU,MT_2x2=Value
   }
   import Margin._
   var Type = scala.collection.mutable.Map
                                          (MT_X -> 1, 
                                           MT_B -> 1,
                                           MT_H -> 1,
                                           MT_W -> 1,
                                           MT_D -> 2,
                                           MT_BU ->1,
                                           MT_HU ->1,
                                           MT_WU -> 1,
                                           MT_2x2 -> 4
                                           )


}

object Constants extends MemoryOpConstants 
{


}

