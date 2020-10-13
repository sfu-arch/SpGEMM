package verilogmain

//            liveIn_R(i).predicate := io.latchEnable.bits.control
//liveIn_R(i).predicate := io.latchEnable.bits.control
import java.io.{File, FileWriter}

import node._
import config._
import interfaces._
import arbiters._
import memory._
import dataflow._
import config._
import util._
import interfaces._


object Main extends App {
  val dir = new File(args(0)) ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new TypeMemDataFlow()))

  val verilog = new FileWriter(new File(dir, s"${chirrtl.main}.v"))
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilog.write(compiledStuff.value)
  verilog.close
}
