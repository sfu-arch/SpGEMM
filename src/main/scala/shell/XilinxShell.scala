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
import chisel3.{RawModule, withClockAndReset}
import config._
import accel._
import dandelion.shell.ConfigBusMaster


/** XilinxShell.
  *
  * This is a wrapper shell mostly used to match Xilinx convention naming,
  * therefore we can pack VTA as an IP for IPI based flows.
  */
class XilinxShell()(implicit p: Parameters) extends RawModule {

  val hp = p(ShellKey).hostParams
  val mp = p(ShellKey).memParams

  val ap_clk = IO(Input(Clock()))
  val ap_rst_n = IO(Input(Bool()))
  val m_axi_gmem = IO(new XilinxAXIMaster(mp))
  val s_axi_control = IO(new XilinxAXILiteClient(hp))

  val shell = withClockAndReset (clock = ap_clk, reset = ~ap_rst_n) { Module(new SpAccel()) }

  // memory
  m_axi_gmem.AWVALID := shell.io.mem.aw.valid
  shell.io.mem.aw.ready := m_axi_gmem.AWREADY
  m_axi_gmem.AWADDR := shell.io.mem.aw.bits.addr
  m_axi_gmem.AWID := shell.io.mem.aw.bits.id
  m_axi_gmem.AWUSER := shell.io.mem.aw.bits.user
  m_axi_gmem.AWLEN := shell.io.mem.aw.bits.len
  m_axi_gmem.AWSIZE := shell.io.mem.aw.bits.size
  m_axi_gmem.AWBURST := shell.io.mem.aw.bits.burst
  m_axi_gmem.AWLOCK := shell.io.mem.aw.bits.lock
  m_axi_gmem.AWCACHE := shell.io.mem.aw.bits.cache
  m_axi_gmem.AWPROT := shell.io.mem.aw.bits.prot
  m_axi_gmem.AWQOS := shell.io.mem.aw.bits.qos
  m_axi_gmem.AWREGION := shell.io.mem.aw.bits.region
  m_axi_gmem.WID <> DontCare

  m_axi_gmem.WVALID := shell.io.mem.w.valid
  shell.io.mem.w.ready := m_axi_gmem.WREADY
  m_axi_gmem.WDATA := shell.io.mem.w.bits.data
  m_axi_gmem.WSTRB := shell.io.mem.w.bits.strb
  m_axi_gmem.WLAST := shell.io.mem.w.bits.last
  //m_axi_gmem.WID := shell.io.mem.w.bits.id
  m_axi_gmem.WUSER := shell.io.mem.w.bits.user

  shell.io.mem.b.valid := m_axi_gmem.BVALID
  m_axi_gmem.BREADY := shell.io.mem.b.valid
  shell.io.mem.b.bits.resp := m_axi_gmem.BRESP
  shell.io.mem.b.bits.id := m_axi_gmem.BID
  shell.io.mem.b.bits.user := m_axi_gmem.BUSER

  m_axi_gmem.ARVALID := shell.io.mem.ar.valid
  shell.io.mem.ar.ready := m_axi_gmem.ARREADY
  m_axi_gmem.ARADDR := shell.io.mem.ar.bits.addr
  m_axi_gmem.ARID := shell.io.mem.ar.bits.id
  m_axi_gmem.ARUSER := shell.io.mem.ar.bits.user
  m_axi_gmem.ARLEN := shell.io.mem.ar.bits.len
  m_axi_gmem.ARSIZE := shell.io.mem.ar.bits.size
  m_axi_gmem.ARBURST := shell.io.mem.ar.bits.burst
  m_axi_gmem.ARLOCK := shell.io.mem.ar.bits.lock
  m_axi_gmem.ARCACHE := shell.io.mem.ar.bits.cache
  m_axi_gmem.ARPROT := shell.io.mem.ar.bits.prot
  m_axi_gmem.ARQOS := shell.io.mem.ar.bits.qos
  m_axi_gmem.ARREGION := shell.io.mem.ar.bits.region

  shell.io.mem.r.valid := m_axi_gmem.RVALID
  m_axi_gmem.RREADY := shell.io.mem.r.ready
  shell.io.mem.r.bits.data := m_axi_gmem.RDATA
  shell.io.mem.r.bits.resp := m_axi_gmem.RRESP
  shell.io.mem.r.bits.last := m_axi_gmem.RLAST
  shell.io.mem.r.bits.id := m_axi_gmem.RID
  shell.io.mem.r.bits.user := m_axi_gmem.RUSER

  // host
  shell.io.host.aw.valid := s_axi_control.AWVALID
  s_axi_control.AWREADY := shell.io.host.aw.ready
  shell.io.host.aw.bits.addr := s_axi_control.AWADDR

  shell.io.host.w.valid := s_axi_control.WVALID
  s_axi_control.WREADY := shell.io.host.w.ready
  shell.io.host.w.bits.data := s_axi_control.WDATA
  shell.io.host.w.bits.strb := s_axi_control.WSTRB

  s_axi_control.BVALID := shell.io.host.b.valid
  shell.io.host.b.ready := s_axi_control.BREADY
  s_axi_control.BRESP := shell.io.host.b.bits.resp

  shell.io.host.ar.valid := s_axi_control.ARVALID
  s_axi_control.ARREADY := shell.io.host.ar.ready
  shell.io.host.ar.bits.addr := s_axi_control.ARADDR

  s_axi_control.RVALID := shell.io.host.r.valid
  shell.io.host.r.ready := s_axi_control.RREADY
  s_axi_control.RDATA := shell.io.host.r.bits.data
  s_axi_control.RRESP := shell.io.host.r.bits.resp
}

/** XilinxShell.
  *
  * This is a wrapper shell mostly used to match Xilinx convention naming,
  * therefore we can pack VTA as an IP for IPI based flows.
  */
class F1Shell(numSegment: Int, numSorter: Int, numVC: Int, VCDepth: Int, maxRowLen: Int, maxColLen: Int)(implicit p: Parameters) extends RawModule {

  val hp = p(ShellKey).hostParams
  val mp = p(ShellKey).memParams

  val ap_clk = IO(Input(Clock()))
  val ap_rst_n = IO(Input(Bool()))
  val cl_axi_mstr_bus = IO(new XilinxAXIMaster(mp))
  val axi_mstr_cfg_bus = IO(new ConfigBusMaster())


  val shell = withClockAndReset(clock = ap_clk, reset = ~ap_rst_n) {
    Module(new SpAccelF1(numSegment, numSorter, numVC, VCDepth, maxRowLen, maxColLen))
  }

  // memory
  cl_axi_mstr_bus.AWVALID := shell.io.mem.aw.valid
  shell.io.mem.aw.ready := cl_axi_mstr_bus.AWREADY
  cl_axi_mstr_bus.AWADDR := shell.io.mem.aw.bits.addr
  cl_axi_mstr_bus.AWID := shell.io.mem.aw.bits.id
  cl_axi_mstr_bus.AWUSER := shell.io.mem.aw.bits.user
  cl_axi_mstr_bus.AWLEN := shell.io.mem.aw.bits.len
  cl_axi_mstr_bus.AWSIZE := shell.io.mem.aw.bits.size
  cl_axi_mstr_bus.AWBURST := shell.io.mem.aw.bits.burst
  cl_axi_mstr_bus.AWLOCK := shell.io.mem.aw.bits.lock
  cl_axi_mstr_bus.AWCACHE := shell.io.mem.aw.bits.cache
  cl_axi_mstr_bus.AWPROT := shell.io.mem.aw.bits.prot
  cl_axi_mstr_bus.AWQOS := shell.io.mem.aw.bits.qos
  cl_axi_mstr_bus.AWREGION := shell.io.mem.aw.bits.region

  cl_axi_mstr_bus.WVALID := shell.io.mem.w.valid
  shell.io.mem.w.ready := cl_axi_mstr_bus.WREADY
  cl_axi_mstr_bus.WDATA := shell.io.mem.w.bits.data
  cl_axi_mstr_bus.WSTRB := shell.io.mem.w.bits.strb
  cl_axi_mstr_bus.WLAST := shell.io.mem.w.bits.last
  cl_axi_mstr_bus.WID := shell.io.mem.w.bits.id
  cl_axi_mstr_bus.WUSER := shell.io.mem.w.bits.user

  shell.io.mem.b.valid := cl_axi_mstr_bus.BVALID
  cl_axi_mstr_bus.BREADY := shell.io.mem.b.valid
  shell.io.mem.b.bits.resp := cl_axi_mstr_bus.BRESP
  shell.io.mem.b.bits.id := cl_axi_mstr_bus.BID
  shell.io.mem.b.bits.user := cl_axi_mstr_bus.BUSER

  cl_axi_mstr_bus.ARVALID := shell.io.mem.ar.valid
  shell.io.mem.ar.ready := cl_axi_mstr_bus.ARREADY
  cl_axi_mstr_bus.ARADDR := shell.io.mem.ar.bits.addr
  cl_axi_mstr_bus.ARID := shell.io.mem.ar.bits.id
  cl_axi_mstr_bus.ARUSER := shell.io.mem.ar.bits.user
  cl_axi_mstr_bus.ARLEN := shell.io.mem.ar.bits.len
  cl_axi_mstr_bus.ARSIZE := shell.io.mem.ar.bits.size
  cl_axi_mstr_bus.ARBURST := shell.io.mem.ar.bits.burst
  cl_axi_mstr_bus.ARLOCK := shell.io.mem.ar.bits.lock
  cl_axi_mstr_bus.ARCACHE := shell.io.mem.ar.bits.cache
  cl_axi_mstr_bus.ARPROT := shell.io.mem.ar.bits.prot
  cl_axi_mstr_bus.ARQOS := shell.io.mem.ar.bits.qos
  cl_axi_mstr_bus.ARREGION := shell.io.mem.ar.bits.region

  shell.io.mem.r.valid := cl_axi_mstr_bus.RVALID
  cl_axi_mstr_bus.RREADY := shell.io.mem.r.ready
  shell.io.mem.r.bits.data := cl_axi_mstr_bus.RDATA
  shell.io.mem.r.bits.resp := cl_axi_mstr_bus.RRESP
  shell.io.mem.r.bits.last := cl_axi_mstr_bus.RLAST
  shell.io.mem.r.bits.id := cl_axi_mstr_bus.RID
  shell.io.mem.r.bits.user := cl_axi_mstr_bus.RUSER

  // host
  shell.io.host.addr := axi_mstr_cfg_bus.addr
  shell.io.host.wdata := axi_mstr_cfg_bus.wdata
  shell.io.host.wr := axi_mstr_cfg_bus.wr
  shell.io.host.rd := axi_mstr_cfg_bus.rd
  axi_mstr_cfg_bus.ack := shell.io.host.ack
  axi_mstr_cfg_bus.rdata := shell.io.host.rdata


}
