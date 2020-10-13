package dataflow

import accel._
import arbiters._
import chisel3._
import chisel3.util._
import chisel3.Module._
import chisel3.testers._
import chisel3.iotesters._
import config._
import control._
import interfaces._
import junctions._
import loop._
import memory._
import muxes._
import node._
import org.scalatest._
import regfile._
import stack._
import util._


  /* ================================================================== *
   *                   PRINTING PORTS DEFINITION                        *
   * ================================================================== */

abstract class test02_nonOptDFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test02_nonOptDF(implicit p: Parameters) extends test02_nonOptDFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID=0, Size=32, NReads=4, NWrites=4)
		 (WControl=new WriteMemoryController(NumOps=4, BaseSize=2, NumEntries=2))
		 (RControl=new ReadMemoryController(NumOps=4, BaseSize=2, NumEntries=2))
		 (RWArbiter=new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val StackPointer = Module(new Stack(NumOps = 3))

  val InputSplitter = Module(new SplitCallNew(List(1,1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 13, BID = 0))

  val bb_if_then1 = Module(new BasicBlockNoMaskNode(NumInputs = 1, NumOuts = 5, BID = 1))

  val bb_if_end2 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 2, NumPhi=0, BID = 2))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %a.addr = alloca i32, align 4
  val alloca_a_addr0 = Module(new AllocaNode(NumOuts=3, ID = 0, RouteID=0))

  //  %b.addr = alloca i32, align 4
  val alloca_b_addr1 = Module(new AllocaNode(NumOuts=2, ID = 1, RouteID=0))

  //  %sum = alloca i32, align 4
  val alloca_sum2 = Module(new AllocaNode(NumOuts=3, ID = 2, RouteID=0))

  //  store i32 %a, i32* %a.addr, align 4
  val st_3 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=3, RouteID=0))

  //  store i32 %b, i32* %b.addr, align 4
  val st_4 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=4, RouteID=1))

  //  store i32 0, i32* %sum, align 4
  val st_5 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=5, RouteID=2))

  //  %0 = load i32, i32* %a.addr, align 4
  val ld_6 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=6, RouteID=0))

  //  %div = udiv i32 %0, 2
  val binaryOp_div7 = Module(new ComputeNode(NumOuts = 1, ID = 7, opCode = "udiv")(sign=false))

  //  %cmp = icmp eq i32 %div, 4
  val icmp_cmp8 = Module(new IcmpNode(NumOuts = 1, ID = 8, opCode = "eq")(sign=false))

  //  br i1 %cmp, label %if.then, label %if.end
  val br_9 = Module(new CBranchNode(ID = 9))

  //  %1 = load i32, i32* %a.addr, align 4
  val ld_10 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=10, RouteID=1))

  //  %2 = load i32, i32* %b.addr, align 4
  val ld_11 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=11, RouteID=2))

  //  %add = add i32 %1, %2
  val binaryOp_add12 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "add")(sign=false))

  //  store i32 %add, i32* %sum, align 4
  val st_13 = Module(new UnTypStore(NumPredOps=0, NumSuccOps=0, ID=13, RouteID=3))

  //  br label %if.end
  val br_14 = Module(new UBranchNode(ID = 14))

  //  %3 = load i32, i32* %sum, align 4
  val ld_15 = Module(new UnTypLoad(NumPredOps=0, NumSuccOps=0, NumOuts=1, ID=15, RouteID=3))

  //  ret i32 %3
  val ret_16 = Module(new RetNode2(retTypes=List(32), ID = 16))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstNode(value = 0, NumOuts = 1, ID = 0))

  //i32 2
  val const1 = Module(new ConstNode(value = 2, NumOuts = 1, ID = 1))

  //i32 4
  val const2 = Module(new ConstNode(value = 4, NumOuts = 1, ID = 2))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn <> InputSplitter.io.Out.enable

  bb_if_then1.io.predicateIn <> br_9.io.Out(0)

  bb_if_end2.io.predicateIn(0) <> br_9.io.Out(1)

  bb_if_end2.io.predicateIn(1) <> br_14.io.Out(0)



  /* ================================================================== *
   *                   PRINTING PARALLEL CONNECTIONS                    *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP -> PREDICATE INSTRUCTION                    *
   * ================================================================== */



  /* ================================================================== *
   *                   ENDING INSTRUCTIONS                              *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP INPUT DATA DEPENDENCIES                     *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP DATA LIVE-IN DEPENDENCIES                   *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP DATA LIVE-OUT DEPENDENCIES                  *
   * ================================================================== */



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  const1.io.enable <> bb_entry0.io.Out(1)

  const2.io.enable <> bb_entry0.io.Out(2)

  alloca_a_addr0.io.enable <> bb_entry0.io.Out(3)

  alloca_b_addr1.io.enable <> bb_entry0.io.Out(4)

  alloca_sum2.io.enable <> bb_entry0.io.Out(5)

  st_3.io.enable <> bb_entry0.io.Out(6)

  st_4.io.enable <> bb_entry0.io.Out(7)

  st_5.io.enable <> bb_entry0.io.Out(8)

  ld_6.io.enable <> bb_entry0.io.Out(9)

  binaryOp_div7.io.enable <> bb_entry0.io.Out(10)

  icmp_cmp8.io.enable <> bb_entry0.io.Out(11)

  br_9.io.enable <> bb_entry0.io.Out(12)


  ld_10.io.enable <> bb_if_then1.io.Out(0)

  ld_11.io.enable <> bb_if_then1.io.Out(1)

  binaryOp_add12.io.enable <> bb_if_then1.io.Out(2)

  st_13.io.enable <> bb_if_then1.io.Out(3)

  br_14.io.enable <> bb_if_then1.io.Out(4)


  ld_15.io.enable <> bb_if_end2.io.Out(0)

  ret_16.io.In.enable <> bb_if_end2.io.Out(1)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */

  alloca_a_addr0.io.allocaInputIO.bits.size      := 1.U
  alloca_a_addr0.io.allocaInputIO.bits.numByte   := 4.U
  alloca_a_addr0.io.allocaInputIO.bits.predicate := true.B
  alloca_a_addr0.io.allocaInputIO.bits.valid     := true.B
  alloca_a_addr0.io.allocaInputIO.valid          := true.B



  alloca_b_addr1.io.allocaInputIO.bits.size      := 1.U
  alloca_b_addr1.io.allocaInputIO.bits.numByte   := 4.U
  alloca_b_addr1.io.allocaInputIO.bits.predicate := true.B
  alloca_b_addr1.io.allocaInputIO.bits.valid     := true.B
  alloca_b_addr1.io.allocaInputIO.valid          := true.B



  alloca_sum2.io.allocaInputIO.bits.size      := 1.U
  alloca_sum2.io.allocaInputIO.bits.numByte   := 4.U
  alloca_sum2.io.allocaInputIO.bits.predicate := true.B
  alloca_sum2.io.allocaInputIO.bits.valid     := true.B
  alloca_sum2.io.allocaInputIO.valid          := true.B





  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  StackPointer.io.InData(0) <> alloca_a_addr0.io.allocaReqIO

  alloca_a_addr0.io.allocaRespIO <> StackPointer.io.OutData(0)

  StackPointer.io.InData(1) <> alloca_b_addr1.io.allocaReqIO

  alloca_b_addr1.io.allocaRespIO <> StackPointer.io.OutData(1)

  StackPointer.io.InData(2) <> alloca_sum2.io.allocaReqIO

  alloca_sum2.io.allocaRespIO <> StackPointer.io.OutData(2)

  MemCtrl.io.WriteIn(0) <> st_3.io.memReq

  st_3.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_4.io.memReq

  st_4.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_5.io.memReq

  st_5.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.ReadIn(0) <> ld_6.io.memReq

  ld_6.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_10.io.memReq

  ld_10.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_11.io.memReq

  ld_11.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.WriteIn(3) <> st_13.io.memReq

  st_13.io.memResp <> MemCtrl.io.WriteOut(3)

  MemCtrl.io.ReadIn(3) <> ld_15.io.memReq

  ld_15.io.memResp <> MemCtrl.io.ReadOut(3)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  st_5.io.inData <> const0.io.Out(0)

  binaryOp_div7.io.RightIO <> const1.io.Out(0)

  icmp_cmp8.io.RightIO <> const2.io.Out(0)

  st_3.io.GepAddr <> alloca_a_addr0.io.Out(0)

  ld_6.io.GepAddr <> alloca_a_addr0.io.Out(1)

  ld_10.io.GepAddr <> alloca_a_addr0.io.Out(2)

  st_4.io.GepAddr <> alloca_b_addr1.io.Out(0)

  ld_11.io.GepAddr <> alloca_b_addr1.io.Out(1)

  st_5.io.GepAddr <> alloca_sum2.io.Out(0)

  st_13.io.GepAddr <> alloca_sum2.io.Out(1)

  ld_15.io.GepAddr <> alloca_sum2.io.Out(2)

  binaryOp_div7.io.LeftIO <> ld_6.io.Out(0)

  icmp_cmp8.io.LeftIO <> binaryOp_div7.io.Out(0)

  br_9.io.CmpIO <> icmp_cmp8.io.Out(0)

  binaryOp_add12.io.LeftIO <> ld_10.io.Out(0)

  binaryOp_add12.io.RightIO <> ld_11.io.Out(0)

  st_13.io.inData <> binaryOp_add12.io.Out(0)

  ret_16.io.In.data("field0") <> ld_15.io.Out(0)

  st_3.io.inData <> InputSplitter.io.Out.data.elements("field0")(0)

  st_4.io.inData <> InputSplitter.io.Out.data.elements("field1")(0)

  st_3.io.Out(0).ready := true.B

  st_4.io.Out(0).ready := true.B

  st_5.io.Out(0).ready := true.B

  st_13.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_16.io.Out

}

import java.io.{File, FileWriter}
object test02nonOptMain extends App {
  val dir = new File("RTL/test02") ; dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test02_nonOptDF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
