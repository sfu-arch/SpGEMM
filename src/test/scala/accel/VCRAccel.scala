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

import accel.SpAccelMain.{VCDepth, numVC}
import chisel3._
import chisel3.MultiIOModule
import shell._
import vta.shell._
import shell.De10Config
import config._
import accel._
import chisel3.util._
import dnn.memory._



/** Test. This generates a testbench file for simulation */
class TestAccelAWS(numSegment: Int, numSorter: Int, numVC: Int, VCDepth: Int, maxRowLen: Int, maxColLen: Int)(implicit p: Parameters) extends MultiIOModule {
  val sim_clock = IO(Input(Clock()))
  val sim_wait = IO(Output(Bool()))
  val sim_shell = Module(new AXISimShell)
  val vta_shell = Module(new SpAccel(numSegment, numSorter, numVC, VCDepth, maxRowLen, maxColLen))
  sim_shell.sim_clock := sim_clock
  sim_wait := sim_shell.sim_wait

  sim_shell.mem.ar <> vta_shell.io.mem.ar
  sim_shell.mem.aw <> vta_shell.io.mem.aw
  vta_shell.io.mem.r <> sim_shell.mem.r
  vta_shell.io.mem.b <> sim_shell.mem.b
  sim_shell.mem.w <> vta_shell.io.mem.w



  vta_shell.io.host.ar <> sim_shell.host.ar
  vta_shell.io.host.aw <> sim_shell.host.aw
  sim_shell.host.r <> vta_shell.io.host.r
  sim_shell.host.b <> vta_shell.io.host.b
  vta_shell.io.host.w <> sim_shell.host.w

}

class SpaghettiSimAccel(numSegment: Int, numSorter: Int, numVC: Int, VCDepth: Int, maxRowLen: Int, maxColLen: Int)
                       (implicit val p: Parameters) extends MultiIOModule {
  val sim_clock = IO(Input(Clock()))
  val sim_wait = IO(Output(Bool()))
  val sim_shell = Module(new AXISimShell)
  val vta_shell = Module(new SpAccel(numSegment, numSorter, numVC, VCDepth, maxRowLen, maxColLen))
  sim_shell.sim_clock := sim_clock
  sim_wait := sim_shell.sim_wait

  sim_shell.mem.ar <> vta_shell.io.mem.ar
  sim_shell.mem.aw <> vta_shell.io.mem.aw
  vta_shell.io.mem.r <> sim_shell.mem.r
  vta_shell.io.mem.b <> sim_shell.mem.b
  sim_shell.mem.w <> vta_shell.io.mem.w


  vta_shell.io.host.ar <> sim_shell.host.ar
  vta_shell.io.host.aw <> sim_shell.host.aw
  sim_shell.host.r <> vta_shell.io.host.r
  sim_shell.host.b <> vta_shell.io.host.b
  vta_shell.io.host.w <> sim_shell.host.w

}

/**
  * Configurations for various FPGA platforms
  * @param numSegment
  * @param numSorter
  */

class DefaultDe10Config(numSegment: Int = 1, numSorter: Int = 1)
  extends Config(new De10Config(numSegments = numSegment, numSorter = numSorter) ++
    new CoreConfig ++ new MiniConfig)

class DefaultPynqConfig(numSegment: Int = 1, numSorter: Int = 1)
  extends Config(new PynqConfig(numSegments = numSegment, numSorter = numSorter) ++
    new CoreConfig ++ new MiniConfig)

class DefaultAWSConfig(numSegment: Int = 1, numSorter: Int = 1)
  extends Config(new AWSConfig(numSegments = numSegment, numSorter = numSorter) ++
    new CoreConfig ++ new MiniConfig)



object SpaghettiSimAccelMain extends App {
  var numSegment = 1
  var numSorter = 1
  var numColMerger = 1
  var numVC = 1
  var VCDepth = 2
  var maxRowLen = 4000
  var maxColLen = 4000

  args.sliding(2, 2).toList.collect {
    case Array("--numSegment", argCtrl: String) => numSegment = argCtrl.toInt
    case Array("--numSorter", argCtrl: String) => numSorter = argCtrl.toInt
    case Array("--numColMerger", argCtrl: String) => numColMerger = argCtrl.toInt
    case Array("--numVC", argCtrl: String) => numVC = argCtrl.toInt
    case Array("--VCDepth", argCtrl: String) => VCDepth = argCtrl.toInt
    case Array("--maxRowLen", argCtrl: String) => maxRowLen = argCtrl.toInt
    case Array("--maxColLen", argCtrl: String) => maxColLen = argCtrl.toInt
  }

  implicit val p: Parameters = new DefaultDe10Config(numSegment = numSegment, numSorter = numSorter)
  chisel3.Driver.execute(args.take(4), () => new SpaghettiSimAccel(numSegment = numSegment, numSorter = numSorter, numVC, VCDepth, maxRowLen, maxColLen))
}


object TestXilinxShellMain extends App {
  implicit val p: Parameters = new DefaultPynqConfig
  chisel3.Driver.execute(args, () => new XilinxShell())
}
object TestVTAShell2Main extends App {
  implicit val p: Parameters = new DefaultDe10Config(numSegment = 1, numSorter = 1)
  chisel3.Driver.execute(args, () => new NoneAccel())
}

object TestAccelAWSMain extends App {
  var numSegment = 1
  var numSorter = 1
  var numColMerger = 1
  var numVC = 1
  var VCDepth = 2
  var maxRowLen = 4000
  var maxColLen = 4000

  args.sliding(2, 2).toList.collect {
    case Array("--numSegment", argCtrl: String) => numSegment = argCtrl.toInt
    case Array("--numSorter", argCtrl: String) => numSorter = argCtrl.toInt
    case Array("--numColMerger", argCtrl: String) => numColMerger = argCtrl.toInt
    case Array("--numVC", argCtrl: String) => numVC = argCtrl.toInt
    case Array("--VCDepth", argCtrl: String) => VCDepth = argCtrl.toInt
    case Array("--maxRowLen", argCtrl: String) => maxRowLen = argCtrl.toInt
    case Array("--maxColLen", argCtrl: String) => maxColLen = argCtrl.toInt
  }
  implicit val p: Parameters = new DefaultAWSConfig(numSegment = numSegment, numSorter = numSorter)
  chisel3.Driver.execute(args.take(4), () => new F1Shell(numSegment = numSegment, numSorter = numSorter, numVC, VCDepth, maxRowLen, maxColLen))
}

object SpAccelMain extends App {

  var numSegment = 1
  var numSorter = 1
  var numColMerger = 1
  var numVC = 1
  var VCDepth = 2
  var maxRowLen = 4000
  var maxColLen = 4000


  args.sliding(2, 2).toList.collect {
    case Array("--numSegment", argCtrl: String) => numSegment = argCtrl.toInt
    case Array("--numSorter", argCtrl: String) => numSorter = argCtrl.toInt
    case Array("--numColMerger", argCtrl: String) => numColMerger = argCtrl.toInt
    case Array("--numVC", argCtrl: String) => numVC = argCtrl.toInt
    case Array("--VCDepth", argCtrl: String) => VCDepth = argCtrl.toInt
    case Array("--maxRowLen", argCtrl: String) => maxRowLen = argCtrl.toInt
    case Array("--maxColLen", argCtrl: String) => maxColLen = argCtrl.toInt

  }
  implicit val p: Parameters = new DefaultDe10Config
  chisel3.Driver.execute(args.take(4), () => new SpAccel(numSegment, numSorter, numVC, VCDepth, maxRowLen, maxColLen))
}

