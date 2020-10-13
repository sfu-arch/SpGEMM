/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package accel

import chisel3._
import chisel3.util._
import config._
import dandelion.shell.{ConfigBusMaster, DCRF1}
import shell._
import dnn.memory.CoreParams

  /*
              +---------------------------+
              |   AXISimShell (DPI<->AXI) |
              |                           |
              | +-------------+           |
              | |  VTASim     |           |
              | |             |           |
              | +-------------+           |        TestAccel2
              |                           |     +-----------------+
driver_main.cc| +-------------+Master Client    |                 |
         +--->+ |  VTAHost    +-----------------------------------X
              | |             |   AXI-Lite|     || VCR Control RegX
              | +-------------+           |     +-----------------|
              |                           |     |                 |
              | +--------------+          |     |                 |
              | |   VTAMem     ^Client Master   |                 |
              | |              <----------+-----------------------+
              | +--------------+  AXI     |     ||  VMem Interface|
              +---------------------------+     +-----------------+
*/


/*
  To use DNNAccel.v in CycloneV, change host interface from AXILiteClient to AXIClient and uncomment following lines:

  io.host.b.bits.id := io.host.w.bits.id
  io.host.r.bits.id := io.host.ar.bits.id

  io.host.b.bits.user <> DontCare
  io.host.r.bits.user <> DontCare
  io.host.r.bits.last := 1.U
 */


/* Receives a counter value as input. Waits for N cycles and then returns N + const as output */
class SpAccel(numSegment: Int = 1, numSorter: Int = 1, numVC: Int = 1, VCDepth: Int = 2, maxRowLen: Int = 4000, maxColLen: Int = 2000)(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val host = new AXILiteClient(p(ShellKey).hostParams)
//    val host = new AXIClient(p(ShellKey).hostParams)
    val mem = new AXIMaster(p(ShellKey).memParams)
  })

  val vcr = Module(new VCR)
  val vme = Module(new VME)
//  val core = Module(new DNNCoreTest)
  val core = Module(new SpTensorCore(numSegment = numSegment, numSorter = numSorter, numVC = numVC, VCDepth = VCDepth, maxRowLen = maxRowLen, maxColLen = maxColLen))

  /* ================================================================== *
   *                       Host to VCR Connection                       *
   * ================================================================== */
  io.host.aw.ready := vcr.io.host.aw.ready
  vcr.io.host.aw.valid := io.host.aw.valid
  vcr.io.host.aw.bits.addr := io.host.aw.bits.addr
  io.host.w.ready := vcr.io.host.w.ready
  vcr.io.host.w.valid := io.host.w.valid
  vcr.io.host.w.bits.data := io.host.w.bits.data
  vcr.io.host.w.bits.strb := io.host.w.bits.strb
  vcr.io.host.b.ready := io.host.b.ready
  io.host.b.valid := vcr.io.host.b.valid
  io.host.b.bits.resp := vcr.io.host.b.bits.resp
//  io.host.b.bits.id := io.host.w.bits.id

  io.host.ar.ready := vcr.io.host.ar.ready
  vcr.io.host.ar.valid := io.host.ar.valid
  vcr.io.host.ar.bits.addr := io.host.ar.bits.addr
  vcr.io.host.r.ready := io.host.r.ready
  io.host.r.valid := vcr.io.host.r.valid
  io.host.r.bits.data := vcr.io.host.r.bits.data
  io.host.r.bits.resp := vcr.io.host.r.bits.resp
//  io.host.r.bits.id := io.host.ar.bits.id

//  io.host.b.bits.user <> DontCare
//  io.host.r.bits.user <> DontCare
//  io.host.r.bits.last := 1.U
  /* ================================================================== *
   *                       VCR to Core Connection                       *
   * ================================================================== */
  //  core.io.vcr <> vcr.io.vcr
  core.io.vcr.launch := vcr.io.vcr.launch
  vcr.io.vcr.finish := core.io.vcr.finish
  vcr.io.vcr.ecnt := core.io.vcr.ecnt
  core.io.vcr.vals := vcr.io.vcr.vals
  core.io.vcr.ptrs := vcr.io.vcr.ptrs
  /* ================================================================== *
   *                       Core to VME Connection                       *
   * ================================================================== */
  vme.io.vme <> core.io.vme
  /* ================================================================== *
   *                        VME to Mem Connection                       *
   * ================================================================== */
  io.mem <> vme.io.mem

}



/* Receives a counter value as input. Waits for N cycles and then returns N + const as output */
class SpAccelF1(numSegment: Int = 1, numSorter: Int = 1, numVC: Int = 1, VCDepth: Int = 2, maxRowLen: Int = 4000, maxColLen: Int = 2000)(implicit p: Parameters) extends Module {
  val io = IO(new Bundle {
    val host = new ConfigBusMaster()
    val mem = new AXIMaster(p(ShellKey).memParams)
  })

//  val debug = RegInit(VecInit(Seq.fill(10)(false.B)))
//  io.debug zip debug map {case(a,b) => a:=b}

  val vcr = Module(new DCRF1)
  val vme = Module(new VME)
  //  val core = Module(new DNNCoreTest)
  val core = Module(new SpTensorCore(numSegment = numSegment, numSorter = numSorter, numVC = numVC, VCDepth = VCDepth, maxRowLen = maxRowLen, maxColLen = maxColLen))

  /* ================================================================== *
   *                       Host to VCR Connection                       *
   * ================================================================== */

  io.host <> vcr.io.host
//  io.host.aw.ready := vcr.io.host.aw.ready
//  vcr.io.host.aw.valid := io.host.aw.valid
//  vcr.io.host.aw.bits.addr := io.host.aw.bits.addr
//  io.host.w.ready := vcr.io.host.w.ready
//  vcr.io.host.w.valid := io.host.w.valid
//  vcr.io.host.w.bits.data := io.host.w.bits.data
//  vcr.io.host.w.bits.strb := io.host.w.bits.strb
//  vcr.io.host.b.ready := io.host.b.ready
//  io.host.b.valid := vcr.io.host.b.valid
//  io.host.b.bits.resp := vcr.io.host.b.bits.resp
//  //  io.host.b.bits.id := io.host.w.bits.id
//
//  io.host.ar.ready := vcr.io.host.ar.ready
//  vcr.io.host.ar.valid := io.host.ar.valid
//  vcr.io.host.ar.bits.addr := io.host.ar.bits.addr
//  vcr.io.host.r.ready := io.host.r.ready
//  io.host.r.valid := vcr.io.host.r.valid
//  io.host.r.bits.data := vcr.io.host.r.bits.data
//  io.host.r.bits.resp := vcr.io.host.r.bits.resp
  //  io.host.r.bits.id := io.host.ar.bits.id

  //  io.host.b.bits.user <> DontCare
  //  io.host.r.bits.user <> DontCare
  //  io.host.r.bits.last := 1.U
  /* ================================================================== *
   *                       VCR to Core Connection                       *
   * ================================================================== */
  //  core.io.vcr <> vcr.io.vcr
  core.io.vcr.launch := vcr.io.dcr.launch
  vcr.io.dcr.finish := core.io.vcr.finish
  vcr.io.dcr.ecnt := core.io.vcr.ecnt
  core.io.vcr.vals := vcr.io.dcr.vals
  core.io.vcr.ptrs := vcr.io.dcr.ptrs
  /* ================================================================== *
   *                       Core to VME Connection                       *
   * ================================================================== */
  vme.io.vme <> core.io.vme
  /* ================================================================== *
   *                        VME to Mem Connection                       *
   * ================================================================== */
  io.mem <> vme.io.mem


}


