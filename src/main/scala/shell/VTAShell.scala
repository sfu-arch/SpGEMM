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

package shell

import chisel3._
import config._

/** Shell parameters. */
case class ShellParams(
  hostParams: AXIParams,
  memParams: AXIParams,
  vcrParams: VCRSimParams,
  vmeParams: VMESimParams,
)

case object ShellKey extends Field[ShellParams]

/** VTAShell.
  *
  * The VTAShell is based on a VME, VCR and core. This creates a complete VTA
  * system that can be used for simulation or real hardware.
  */
class VTAShell(implicit p: Parameters) extends Module {
  val io = IO(new Bundle{
    val host = new AXILiteClient(p(ShellKey).hostParams)
    val mem = new AXIMaster(p(ShellKey).memParams)
  })

  val vcr = Module(new VCR)
  val vme = Module(new VME)

  vcr.io.vcr.ecnt(0) <> DontCare
  vcr.io.vcr.finish := false.B

  vme.io.vme.rd(0) <> DontCare
  vme.io.vme.wr(0) <> DontCare
//  vme.io.vme.wr(0) <> DontCare
//
  /* Insert Core Here */
 // val core = Module(new Core)
//
  /* Connect Control Status Registers */
//  core.io.vcr <> vcr.io.vcr
//  vme.io.vme <> core.io.vme
//
  /* Connect AXI */
  io.host <> vcr.io.host
 io.mem <> vme.io.mem

}
