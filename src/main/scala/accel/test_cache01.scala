package cache

import FPU._
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

abstract class test_cache01DFIO(implicit val p: Parameters) extends Module with CoreParams {
  val io = IO(new Bundle {
    val in = Flipped(Decoupled(new Call(List(32, 32, 32))))
    val MemResp = Flipped(Valid(new MemResp))
    val MemReq = Decoupled(new MemReq)
    val out = Decoupled(new Call(List(32)))
  })
}

class test_cache01DF(implicit p: Parameters) extends test_cache01DFIO()(p) {


  /* ================================================================== *
   *                   PRINTING MEMORY MODULES                          *
   * ================================================================== */

  val MemCtrl = Module(new UnifiedController(ID = 0, Size = 32, NReads = 8, NWrites = 8)
  (WControl = new WriteMemoryController(NumOps = 8, BaseSize = 2, NumEntries = 2))
  (RControl = new ReadMemoryController(NumOps = 8, BaseSize = 2, NumEntries = 2))
  (RWArbiter = new ReadWriteArbiter()))

  io.MemReq <> MemCtrl.io.MemReq
  MemCtrl.io.MemResp <> io.MemResp

  val InputSplitter = Module(new SplitCallNew(List(8, 8, 1)))
  InputSplitter.io.In <> io.in



  /* ================================================================== *
   *                   PRINTING LOOP HEADERS                            *
   * ================================================================== */



  /* ================================================================== *
   *                   PRINTING BASICBLOCK NODES                        *
   * ================================================================== */

  val bb_entry0 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 3, BID = 0))

  val bb_if_then1 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 37, BID = 1))

  val bb_if_else2 = Module(new BasicBlockNoMaskFastNode(NumInputs = 1, NumOuts = 29, BID = 2))

  val bb_if_end3 = Module(new BasicBlockNode(NumInputs = 2, NumOuts = 24, NumPhi = 8, BID = 3))



  /* ================================================================== *
   *                   PRINTING INSTRUCTION NODES                       *
   * ================================================================== */

  //  %tobool = icmp eq i32 %read, 0, !UID !10
  val icmp_tobool0 = Module(new IcmpNode(NumOuts = 1, ID = 0, opCode = "eq")(sign = false))

  //  br i1 %tobool, label %if.else, label %if.then, !UID !11, !BB_UID !12
  val br_1 = Module(new CBranchNodeVariable(NumTrue = 1, NumFalse = 1, NumPredecessor = 0, ID = 1))

  //  %0 = load i32, i32* %a, align 4, !tbaa !13, !UID !17
  val ld_2 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 2, RouteID = 0))

  //  %mul = shl i32 %0, 1, !UID !18
  val binaryOp_mul3 = Module(new ComputeNode(NumOuts = 1, ID = 3, opCode = "shl")(sign = false))

  //  %arrayidx1 = getelementptr inbounds i32, i32* %a, i32 1, !UID !19
  val Gep_arrayidx14 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 4)(ElementSize = 4, ArraySize = List()))

  //  %1 = load i32, i32* %arrayidx1, align 4, !tbaa !13, !UID !20
  val ld_5 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 5, RouteID = 1))

  //  %mul2 = shl i32 %1, 2, !UID !21
  val binaryOp_mul26 = Module(new ComputeNode(NumOuts = 1, ID = 6, opCode = "shl")(sign = false))

  //  %arrayidx3 = getelementptr inbounds i32, i32* %a, i32 2, !UID !22
  val Gep_arrayidx37 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 7)(ElementSize = 4, ArraySize = List()))

  //  %2 = load i32, i32* %arrayidx3, align 4, !tbaa !13, !UID !23
  val ld_8 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 8, RouteID = 2))

  //  %mul4 = mul i32 %2, 6, !UID !24
  val binaryOp_mul49 = Module(new ComputeNode(NumOuts = 1, ID = 9, opCode = "mul")(sign = false))

  //  %arrayidx5 = getelementptr inbounds i32, i32* %a, i32 3, !UID !25
  val Gep_arrayidx510 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 10)(ElementSize = 4, ArraySize = List()))

  //  %3 = load i32, i32* %arrayidx5, align 4, !tbaa !13, !UID !26
  val ld_11 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 11, RouteID = 3))

  //  %mul6 = shl i32 %3, 3, !UID !27
  val binaryOp_mul612 = Module(new ComputeNode(NumOuts = 1, ID = 12, opCode = "shl")(sign = false))

  //  %4 = load i32, i32* %b, align 4, !tbaa !13, !UID !28
  val ld_13 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 13, RouteID = 4))

  //  %mul8 = shl i32 %4, 1, !UID !29
  val binaryOp_mul814 = Module(new ComputeNode(NumOuts = 1, ID = 14, opCode = "shl")(sign = false))

  //  %arrayidx9 = getelementptr inbounds i32, i32* %b, i32 1, !UID !30
  val Gep_arrayidx915 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 15)(ElementSize = 4, ArraySize = List()))

  //  %5 = load i32, i32* %arrayidx9, align 4, !tbaa !13, !UID !31
  val ld_16 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 16, RouteID = 5))

  //  %mul10 = shl i32 %5, 2, !UID !32
  val binaryOp_mul1017 = Module(new ComputeNode(NumOuts = 1, ID = 17, opCode = "shl")(sign = false))

  //  %arrayidx11 = getelementptr inbounds i32, i32* %b, i32 2, !UID !33
  val Gep_arrayidx1118 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 18)(ElementSize = 4, ArraySize = List()))

  //  %6 = load i32, i32* %arrayidx11, align 4, !tbaa !13, !UID !34
  val ld_19 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 19, RouteID = 6))

  //  %mul12 = mul i32 %6, 6, !UID !35
  val binaryOp_mul1220 = Module(new ComputeNode(NumOuts = 1, ID = 20, opCode = "mul")(sign = false))

  //  %arrayidx13 = getelementptr inbounds i32, i32* %b, i32 3, !UID !36
  val Gep_arrayidx1321 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 21)(ElementSize = 4, ArraySize = List()))

  //  %7 = load i32, i32* %arrayidx13, align 4, !tbaa !13, !UID !37
  val ld_22 = Module(new UnTypLoad(NumPredOps = 0, NumSuccOps = 0, NumOuts = 1, ID = 22, RouteID = 7))

  //  %mul14 = shl i32 %7, 3, !UID !38
  val binaryOp_mul1423 = Module(new ComputeNode(NumOuts = 1, ID = 23, opCode = "shl")(sign = false))

  //  br label %if.end, !UID !39, !BB_UID !40
  val br_24 = Module(new UBranchNode(ID = 24))

  //  store i32 1, i32* %a, align 4, !tbaa !13, !UID !41
  val st_25 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 25, RouteID = 0))

  //  %arrayidx16 = getelementptr inbounds i32, i32* %a, i32 1, !UID !42
  val Gep_arrayidx1626 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 26)(ElementSize = 4, ArraySize = List()))

  //  store i32 2, i32* %arrayidx16, align 4, !tbaa !13, !UID !43
  val st_27 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 27, RouteID = 1))

  //  %arrayidx17 = getelementptr inbounds i32, i32* %a, i32 3, !UID !44
  val Gep_arrayidx1728 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 28)(ElementSize = 4, ArraySize = List()))

  //  store i32 3, i32* %arrayidx17, align 4, !tbaa !13, !UID !45
  val st_29 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 29, RouteID = 2))

  //  %arrayidx18 = getelementptr inbounds i32, i32* %a, i32 4, !UID !46
  val Gep_arrayidx1830 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 30)(ElementSize = 4, ArraySize = List()))

  //  store i32 4, i32* %arrayidx18, align 4, !tbaa !13, !UID !47
  val st_31 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 31, RouteID = 3))

  //  store i32 5, i32* %b, align 4, !tbaa !13, !UID !48
  val st_32 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 32, RouteID = 4))

  //  %arrayidx20 = getelementptr inbounds i32, i32* %b, i32 1, !UID !49
  val Gep_arrayidx2033 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 33)(ElementSize = 4, ArraySize = List()))

  //  store i32 6, i32* %arrayidx20, align 4, !tbaa !13, !UID !50
  val st_34 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 34, RouteID = 5))

  //  %arrayidx21 = getelementptr inbounds i32, i32* %b, i32 2, !UID !51
  val Gep_arrayidx2135 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 35)(ElementSize = 4, ArraySize = List()))

  //  store i32 7, i32* %arrayidx21, align 4, !tbaa !13, !UID !52
  val st_36 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 36, RouteID = 6))

  //  %arrayidx22 = getelementptr inbounds i32, i32* %b, i32 3, !UID !53
  val Gep_arrayidx2237 = Module(new GepNode(NumIns = 1, NumOuts = 1, ID = 37)(ElementSize = 4, ArraySize = List()))

  //  store i32 8, i32* %arrayidx22, align 4, !tbaa !13, !UID !54
  val st_38 = Module(new UnTypStore(NumPredOps = 0, NumSuccOps = 0, ID = 38, RouteID = 7))

  //  br label %if.end, !UID !55, !BB_UID !56
  val br_39 = Module(new UBranchNode(ID = 39))

  //  %s1.0 = phi i32 [ %mul, %if.then ], [ 1, %if.else ], !UID !57
  val phis1_040 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 40, Res = true))

  //  %s2.0 = phi i32 [ %mul2, %if.then ], [ 2, %if.else ], !UID !58
  val phis2_041 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 41, Res = true))

  //  %s3.0 = phi i32 [ %mul4, %if.then ], [ 3, %if.else ], !UID !59
  val phis3_042 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 42, Res = true))

  //  %s4.0 = phi i32 [ %mul6, %if.then ], [ 4, %if.else ], !UID !60
  val phis4_043 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 43, Res = true))

  //  %s5.0 = phi i32 [ %mul8, %if.then ], [ 5, %if.else ], !UID !61
  val phis5_044 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 44, Res = true))

  //  %s6.0 = phi i32 [ %mul10, %if.then ], [ 6, %if.else ], !UID !62
  val phis6_045 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 45, Res = true))

  //  %s7.0 = phi i32 [ %mul12, %if.then ], [ 7, %if.else ], !UID !63
  val phis7_046 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 46, Res = true))

  //  %s8.0 = phi i32 [ %mul14, %if.then ], [ 8, %if.else ], !UID !64
  val phis8_047 = Module(new PhiFastNode(NumInputs = 2, NumOutputs = 1, ID = 47, Res = true))

  //  %add = add i32 %s2.0, %s1.0, !UID !65
  val binaryOp_add48 = Module(new ComputeNode(NumOuts = 1, ID = 48, opCode = "add")(sign = false))

  //  %add23 = add i32 %add, %s3.0, !UID !66
  val binaryOp_add2349 = Module(new ComputeNode(NumOuts = 1, ID = 49, opCode = "add")(sign = false))

  //  %add24 = add i32 %add23, %s4.0, !UID !67
  val binaryOp_add2450 = Module(new ComputeNode(NumOuts = 1, ID = 50, opCode = "add")(sign = false))

  //  %add25 = add i32 %add24, %s5.0, !UID !68
  val binaryOp_add2551 = Module(new ComputeNode(NumOuts = 1, ID = 51, opCode = "add")(sign = false))

  //  %add26 = add i32 %add25, %s6.0, !UID !69
  val binaryOp_add2652 = Module(new ComputeNode(NumOuts = 1, ID = 52, opCode = "add")(sign = false))

  //  %add27 = add i32 %add26, %s7.0, !UID !70
  val binaryOp_add2753 = Module(new ComputeNode(NumOuts = 1, ID = 53, opCode = "add")(sign = false))

  //  %add28 = add i32 %add27, %s8.0, !UID !71
  val binaryOp_add2854 = Module(new ComputeNode(NumOuts = 1, ID = 54, opCode = "add")(sign = false))

  //  ret i32 %add28, !UID !72, !BB_UID !73
  val ret_55 = Module(new RetNode2(retTypes = List(32), ID = 55))



  /* ================================================================== *
   *                   PRINTING CONSTANTS NODES                         *
   * ================================================================== */

  //i32 0
  val const0 = Module(new ConstFastNode(value = 0, ID = 0))

  //i32 1
  val const1 = Module(new ConstFastNode(value = 1, ID = 1))

  //i32 1
  val const2 = Module(new ConstFastNode(value = 1, ID = 2))

  //i32 2
  val const3 = Module(new ConstFastNode(value = 2, ID = 3))

  //i32 2
  val const4 = Module(new ConstFastNode(value = 2, ID = 4))

  //i32 6
  val const5 = Module(new ConstFastNode(value = 6, ID = 5))

  //i32 3
  val const6 = Module(new ConstFastNode(value = 3, ID = 6))

  //i32 3
  val const7 = Module(new ConstFastNode(value = 3, ID = 7))

  //i32 1
  val const8 = Module(new ConstFastNode(value = 1, ID = 8))

  //i32 1
  val const9 = Module(new ConstFastNode(value = 1, ID = 9))

  //i32 2
  val const10 = Module(new ConstFastNode(value = 2, ID = 10))

  //i32 2
  val const11 = Module(new ConstFastNode(value = 2, ID = 11))

  //i32 6
  val const12 = Module(new ConstFastNode(value = 6, ID = 12))

  //i32 3
  val const13 = Module(new ConstFastNode(value = 3, ID = 13))

  //i32 3
  val const14 = Module(new ConstFastNode(value = 3, ID = 14))

  //i32 1
  val const15 = Module(new ConstFastNode(value = 1, ID = 15))

  //i32 1
  val const16 = Module(new ConstFastNode(value = 1, ID = 16))

  //i32 2
  val const17 = Module(new ConstFastNode(value = 2, ID = 17))

  //i32 3
  val const18 = Module(new ConstFastNode(value = 3, ID = 18))

  //i32 3
  val const19 = Module(new ConstFastNode(value = 3, ID = 19))

  //i32 4
  val const20 = Module(new ConstFastNode(value = 4, ID = 20))

  //i32 4
  val const21 = Module(new ConstFastNode(value = 4, ID = 21))

  //i32 5
  val const22 = Module(new ConstFastNode(value = 5, ID = 22))

  //i32 1
  val const23 = Module(new ConstFastNode(value = 1, ID = 23))

  //i32 6
  val const24 = Module(new ConstFastNode(value = 6, ID = 24))

  //i32 2
  val const25 = Module(new ConstFastNode(value = 2, ID = 25))

  //i32 7
  val const26 = Module(new ConstFastNode(value = 7, ID = 26))

  //i32 3
  val const27 = Module(new ConstFastNode(value = 3, ID = 27))

  //i32 8
  val const28 = Module(new ConstFastNode(value = 8, ID = 28))

  //i32 1
  val const29 = Module(new ConstFastNode(value = 1, ID = 29))

  //i32 2
  val const30 = Module(new ConstFastNode(value = 2, ID = 30))

  //i32 3
  val const31 = Module(new ConstFastNode(value = 3, ID = 31))

  //i32 4
  val const32 = Module(new ConstFastNode(value = 4, ID = 32))

  //i32 5
  val const33 = Module(new ConstFastNode(value = 5, ID = 33))

  //i32 6
  val const34 = Module(new ConstFastNode(value = 6, ID = 34))

  //i32 7
  val const35 = Module(new ConstFastNode(value = 7, ID = 35))

  //i32 8
  val const36 = Module(new ConstFastNode(value = 8, ID = 36))



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE INSTRUCTION              *
   * ================================================================== */

  bb_entry0.io.predicateIn(0) <> InputSplitter.io.Out.enable

  bb_if_then1.io.predicateIn(0) <> br_1.io.FalseOutput(0)

  bb_if_else2.io.predicateIn(0) <> br_1.io.TrueOutput(0)

  bb_if_end3.io.predicateIn(1) <> br_24.io.Out(0)

  bb_if_end3.io.predicateIn(0) <> br_39.io.Out(0)



  /* ================================================================== *
   *                   BASICBLOCK -> PREDICATE LOOP                     *
   * ================================================================== */



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
   *                   LOOP LIVE OUT DEPENDENCIES                       *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP CARRY DEPENDENCIES                          *
   * ================================================================== */



  /* ================================================================== *
   *                   LOOP DATA CARRY DEPENDENCIES                     *
   * ================================================================== */



  /* ================================================================== *
   *                   BASICBLOCK -> ENABLE INSTRUCTION                 *
   * ================================================================== */

  const0.io.enable <> bb_entry0.io.Out(0)

  icmp_tobool0.io.enable <> bb_entry0.io.Out(1)

  br_1.io.enable <> bb_entry0.io.Out(2)


  const1.io.enable <> bb_if_then1.io.Out(0)

  const2.io.enable <> bb_if_then1.io.Out(1)

  const3.io.enable <> bb_if_then1.io.Out(2)

  const4.io.enable <> bb_if_then1.io.Out(3)

  const5.io.enable <> bb_if_then1.io.Out(4)

  const6.io.enable <> bb_if_then1.io.Out(5)

  const7.io.enable <> bb_if_then1.io.Out(6)

  const8.io.enable <> bb_if_then1.io.Out(7)

  const9.io.enable <> bb_if_then1.io.Out(8)

  const10.io.enable <> bb_if_then1.io.Out(9)

  const11.io.enable <> bb_if_then1.io.Out(10)

  const12.io.enable <> bb_if_then1.io.Out(11)

  const13.io.enable <> bb_if_then1.io.Out(12)

  const14.io.enable <> bb_if_then1.io.Out(13)

  ld_2.io.enable <> bb_if_then1.io.Out(14)

  binaryOp_mul3.io.enable <> bb_if_then1.io.Out(15)

  Gep_arrayidx14.io.enable <> bb_if_then1.io.Out(16)

  ld_5.io.enable <> bb_if_then1.io.Out(17)

  binaryOp_mul26.io.enable <> bb_if_then1.io.Out(18)

  Gep_arrayidx37.io.enable <> bb_if_then1.io.Out(19)

  ld_8.io.enable <> bb_if_then1.io.Out(20)

  binaryOp_mul49.io.enable <> bb_if_then1.io.Out(21)

  Gep_arrayidx510.io.enable <> bb_if_then1.io.Out(22)

  ld_11.io.enable <> bb_if_then1.io.Out(23)

  binaryOp_mul612.io.enable <> bb_if_then1.io.Out(24)

  ld_13.io.enable <> bb_if_then1.io.Out(25)

  binaryOp_mul814.io.enable <> bb_if_then1.io.Out(26)

  Gep_arrayidx915.io.enable <> bb_if_then1.io.Out(27)

  ld_16.io.enable <> bb_if_then1.io.Out(28)

  binaryOp_mul1017.io.enable <> bb_if_then1.io.Out(29)

  Gep_arrayidx1118.io.enable <> bb_if_then1.io.Out(30)

  ld_19.io.enable <> bb_if_then1.io.Out(31)

  binaryOp_mul1220.io.enable <> bb_if_then1.io.Out(32)

  Gep_arrayidx1321.io.enable <> bb_if_then1.io.Out(33)

  ld_22.io.enable <> bb_if_then1.io.Out(34)

  binaryOp_mul1423.io.enable <> bb_if_then1.io.Out(35)

  br_24.io.enable <> bb_if_then1.io.Out(36)


  const15.io.enable <> bb_if_else2.io.Out(0)

  const16.io.enable <> bb_if_else2.io.Out(1)

  const17.io.enable <> bb_if_else2.io.Out(2)

  const18.io.enable <> bb_if_else2.io.Out(3)

  const19.io.enable <> bb_if_else2.io.Out(4)

  const20.io.enable <> bb_if_else2.io.Out(5)

  const21.io.enable <> bb_if_else2.io.Out(6)

  const22.io.enable <> bb_if_else2.io.Out(7)

  const23.io.enable <> bb_if_else2.io.Out(8)

  const24.io.enable <> bb_if_else2.io.Out(9)

  const25.io.enable <> bb_if_else2.io.Out(10)

  const26.io.enable <> bb_if_else2.io.Out(11)

  const27.io.enable <> bb_if_else2.io.Out(12)

  const28.io.enable <> bb_if_else2.io.Out(13)

  st_25.io.enable <> bb_if_else2.io.Out(14)

  Gep_arrayidx1626.io.enable <> bb_if_else2.io.Out(15)

  st_27.io.enable <> bb_if_else2.io.Out(16)

  Gep_arrayidx1728.io.enable <> bb_if_else2.io.Out(17)

  st_29.io.enable <> bb_if_else2.io.Out(18)

  Gep_arrayidx1830.io.enable <> bb_if_else2.io.Out(19)

  st_31.io.enable <> bb_if_else2.io.Out(20)

  st_32.io.enable <> bb_if_else2.io.Out(21)

  Gep_arrayidx2033.io.enable <> bb_if_else2.io.Out(22)

  st_34.io.enable <> bb_if_else2.io.Out(23)

  Gep_arrayidx2135.io.enable <> bb_if_else2.io.Out(24)

  st_36.io.enable <> bb_if_else2.io.Out(25)

  Gep_arrayidx2237.io.enable <> bb_if_else2.io.Out(26)

  st_38.io.enable <> bb_if_else2.io.Out(27)

  br_39.io.enable <> bb_if_else2.io.Out(28)


  const29.io.enable <> bb_if_end3.io.Out(0)

  const30.io.enable <> bb_if_end3.io.Out(1)

  const31.io.enable <> bb_if_end3.io.Out(2)

  const32.io.enable <> bb_if_end3.io.Out(3)

  const33.io.enable <> bb_if_end3.io.Out(4)

  const34.io.enable <> bb_if_end3.io.Out(5)

  const35.io.enable <> bb_if_end3.io.Out(6)

  const36.io.enable <> bb_if_end3.io.Out(7)

  phis1_040.io.enable <> bb_if_end3.io.Out(8)

  phis2_041.io.enable <> bb_if_end3.io.Out(9)

  phis3_042.io.enable <> bb_if_end3.io.Out(10)

  phis4_043.io.enable <> bb_if_end3.io.Out(11)

  phis5_044.io.enable <> bb_if_end3.io.Out(12)

  phis6_045.io.enable <> bb_if_end3.io.Out(13)

  phis7_046.io.enable <> bb_if_end3.io.Out(14)

  phis8_047.io.enable <> bb_if_end3.io.Out(15)

  binaryOp_add48.io.enable <> bb_if_end3.io.Out(16)

  binaryOp_add2349.io.enable <> bb_if_end3.io.Out(17)

  binaryOp_add2450.io.enable <> bb_if_end3.io.Out(18)

  binaryOp_add2551.io.enable <> bb_if_end3.io.Out(19)

  binaryOp_add2652.io.enable <> bb_if_end3.io.Out(20)

  binaryOp_add2753.io.enable <> bb_if_end3.io.Out(21)

  binaryOp_add2854.io.enable <> bb_if_end3.io.Out(22)

  ret_55.io.In.enable <> bb_if_end3.io.Out(23)




  /* ================================================================== *
   *                   CONNECTING PHI NODES                             *
   * ================================================================== */

  phis1_040.io.Mask <> bb_if_end3.io.MaskBB(0)

  phis2_041.io.Mask <> bb_if_end3.io.MaskBB(1)

  phis3_042.io.Mask <> bb_if_end3.io.MaskBB(2)

  phis4_043.io.Mask <> bb_if_end3.io.MaskBB(3)

  phis5_044.io.Mask <> bb_if_end3.io.MaskBB(4)

  phis6_045.io.Mask <> bb_if_end3.io.MaskBB(5)

  phis7_046.io.Mask <> bb_if_end3.io.MaskBB(6)

  phis8_047.io.Mask <> bb_if_end3.io.MaskBB(7)



  /* ================================================================== *
   *                   PRINT ALLOCA OFFSET                              *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING MEMORY CONNECTIONS                    *
   * ================================================================== */

  MemCtrl.io.ReadIn(0) <> ld_2.io.memReq

  ld_2.io.memResp <> MemCtrl.io.ReadOut(0)

  MemCtrl.io.ReadIn(1) <> ld_5.io.memReq

  ld_5.io.memResp <> MemCtrl.io.ReadOut(1)

  MemCtrl.io.ReadIn(2) <> ld_8.io.memReq

  ld_8.io.memResp <> MemCtrl.io.ReadOut(2)

  MemCtrl.io.ReadIn(3) <> ld_11.io.memReq

  ld_11.io.memResp <> MemCtrl.io.ReadOut(3)

  MemCtrl.io.ReadIn(4) <> ld_13.io.memReq

  ld_13.io.memResp <> MemCtrl.io.ReadOut(4)

  MemCtrl.io.ReadIn(5) <> ld_16.io.memReq

  ld_16.io.memResp <> MemCtrl.io.ReadOut(5)

  MemCtrl.io.ReadIn(6) <> ld_19.io.memReq

  ld_19.io.memResp <> MemCtrl.io.ReadOut(6)

  MemCtrl.io.ReadIn(7) <> ld_22.io.memReq

  ld_22.io.memResp <> MemCtrl.io.ReadOut(7)

  MemCtrl.io.WriteIn(0) <> st_25.io.memReq

  st_25.io.memResp <> MemCtrl.io.WriteOut(0)

  MemCtrl.io.WriteIn(1) <> st_27.io.memReq

  st_27.io.memResp <> MemCtrl.io.WriteOut(1)

  MemCtrl.io.WriteIn(2) <> st_29.io.memReq

  st_29.io.memResp <> MemCtrl.io.WriteOut(2)

  MemCtrl.io.WriteIn(3) <> st_31.io.memReq

  st_31.io.memResp <> MemCtrl.io.WriteOut(3)

  MemCtrl.io.WriteIn(4) <> st_32.io.memReq

  st_32.io.memResp <> MemCtrl.io.WriteOut(4)

  MemCtrl.io.WriteIn(5) <> st_34.io.memReq

  st_34.io.memResp <> MemCtrl.io.WriteOut(5)

  MemCtrl.io.WriteIn(6) <> st_36.io.memReq

  st_36.io.memResp <> MemCtrl.io.WriteOut(6)

  MemCtrl.io.WriteIn(7) <> st_38.io.memReq

  st_38.io.memResp <> MemCtrl.io.WriteOut(7)



  /* ================================================================== *
   *                   PRINT SHARED CONNECTIONS                         *
   * ================================================================== */



  /* ================================================================== *
   *                   CONNECTING DATA DEPENDENCIES                     *
   * ================================================================== */

  icmp_tobool0.io.RightIO <> const0.io.Out

  binaryOp_mul3.io.RightIO <> const1.io.Out

  Gep_arrayidx14.io.idx(0) <> const2.io.Out

  binaryOp_mul26.io.RightIO <> const3.io.Out

  Gep_arrayidx37.io.idx(0) <> const4.io.Out

  binaryOp_mul49.io.RightIO <> const5.io.Out

  Gep_arrayidx510.io.idx(0) <> const6.io.Out

  binaryOp_mul612.io.RightIO <> const7.io.Out

  binaryOp_mul814.io.RightIO <> const8.io.Out

  Gep_arrayidx915.io.idx(0) <> const9.io.Out

  binaryOp_mul1017.io.RightIO <> const10.io.Out

  Gep_arrayidx1118.io.idx(0) <> const11.io.Out

  binaryOp_mul1220.io.RightIO <> const12.io.Out

  Gep_arrayidx1321.io.idx(0) <> const13.io.Out

  binaryOp_mul1423.io.RightIO <> const14.io.Out

  st_25.io.inData <> const15.io.Out

  Gep_arrayidx1626.io.idx(0) <> const16.io.Out

  st_27.io.inData <> const17.io.Out

  Gep_arrayidx1728.io.idx(0) <> const18.io.Out

  st_29.io.inData <> const19.io.Out

  Gep_arrayidx1830.io.idx(0) <> const20.io.Out

  st_31.io.inData <> const21.io.Out

  st_32.io.inData <> const22.io.Out

  Gep_arrayidx2033.io.idx(0) <> const23.io.Out

  st_34.io.inData <> const24.io.Out

  Gep_arrayidx2135.io.idx(0) <> const25.io.Out

  st_36.io.inData <> const26.io.Out

  Gep_arrayidx2237.io.idx(0) <> const27.io.Out

  st_38.io.inData <> const28.io.Out

  phis1_040.io.InData(1) <> const29.io.Out

  phis2_041.io.InData(1) <> const30.io.Out

  phis3_042.io.InData(1) <> const31.io.Out

  phis4_043.io.InData(1) <> const32.io.Out

  phis5_044.io.InData(1) <> const33.io.Out

  phis6_045.io.InData(1) <> const34.io.Out

  phis7_046.io.InData(1) <> const35.io.Out

  phis8_047.io.InData(1) <> const36.io.Out

  br_1.io.CmpIO <> icmp_tobool0.io.Out(0)

  binaryOp_mul3.io.LeftIO <> ld_2.io.Out(0)

  phis1_040.io.InData(0) <> binaryOp_mul3.io.Out(0)

  ld_5.io.GepAddr <> Gep_arrayidx14.io.Out(0)

  binaryOp_mul26.io.LeftIO <> ld_5.io.Out(0)

  phis2_041.io.InData(0) <> binaryOp_mul26.io.Out(0)

  ld_8.io.GepAddr <> Gep_arrayidx37.io.Out(0)

  binaryOp_mul49.io.LeftIO <> ld_8.io.Out(0)

  phis3_042.io.InData(0) <> binaryOp_mul49.io.Out(0)

  ld_11.io.GepAddr <> Gep_arrayidx510.io.Out(0)

  binaryOp_mul612.io.LeftIO <> ld_11.io.Out(0)

  phis4_043.io.InData(0) <> binaryOp_mul612.io.Out(0)

  binaryOp_mul814.io.LeftIO <> ld_13.io.Out(0)

  phis5_044.io.InData(0) <> binaryOp_mul814.io.Out(0)

  ld_16.io.GepAddr <> Gep_arrayidx915.io.Out(0)

  binaryOp_mul1017.io.LeftIO <> ld_16.io.Out(0)

  phis6_045.io.InData(0) <> binaryOp_mul1017.io.Out(0)

  ld_19.io.GepAddr <> Gep_arrayidx1118.io.Out(0)

  binaryOp_mul1220.io.LeftIO <> ld_19.io.Out(0)

  phis7_046.io.InData(0) <> binaryOp_mul1220.io.Out(0)

  ld_22.io.GepAddr <> Gep_arrayidx1321.io.Out(0)

  binaryOp_mul1423.io.LeftIO <> ld_22.io.Out(0)

  phis8_047.io.InData(0) <> binaryOp_mul1423.io.Out(0)

  st_27.io.GepAddr <> Gep_arrayidx1626.io.Out(0)

  st_29.io.GepAddr <> Gep_arrayidx1728.io.Out(0)

  st_31.io.GepAddr <> Gep_arrayidx1830.io.Out(0)

  st_34.io.GepAddr <> Gep_arrayidx2033.io.Out(0)

  st_36.io.GepAddr <> Gep_arrayidx2135.io.Out(0)

  st_38.io.GepAddr <> Gep_arrayidx2237.io.Out(0)

  binaryOp_add48.io.RightIO <> phis1_040.io.Out(0)

  binaryOp_add48.io.LeftIO <> phis2_041.io.Out(0)

  binaryOp_add2349.io.RightIO <> phis3_042.io.Out(0)

  binaryOp_add2450.io.RightIO <> phis4_043.io.Out(0)

  binaryOp_add2551.io.RightIO <> phis5_044.io.Out(0)

  binaryOp_add2652.io.RightIO <> phis6_045.io.Out(0)

  binaryOp_add2753.io.RightIO <> phis7_046.io.Out(0)

  binaryOp_add2854.io.RightIO <> phis8_047.io.Out(0)

  binaryOp_add2349.io.LeftIO <> binaryOp_add48.io.Out(0)

  binaryOp_add2450.io.LeftIO <> binaryOp_add2349.io.Out(0)

  binaryOp_add2551.io.LeftIO <> binaryOp_add2450.io.Out(0)

  binaryOp_add2652.io.LeftIO <> binaryOp_add2551.io.Out(0)

  binaryOp_add2753.io.LeftIO <> binaryOp_add2652.io.Out(0)

  binaryOp_add2854.io.LeftIO <> binaryOp_add2753.io.Out(0)

  ret_55.io.In.data("field0") <> binaryOp_add2854.io.Out(0)

  ld_2.io.GepAddr <> InputSplitter.io.Out.data.elements("field0")(0)

  Gep_arrayidx14.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(1)

  Gep_arrayidx37.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(2)

  Gep_arrayidx510.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(3)

  st_25.io.GepAddr <> InputSplitter.io.Out.data.elements("field0")(4)

  Gep_arrayidx1626.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(5)

  Gep_arrayidx1728.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(6)

  Gep_arrayidx1830.io.baseAddress <> InputSplitter.io.Out.data.elements("field0")(7)

  ld_13.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(0)

  Gep_arrayidx915.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(1)

  Gep_arrayidx1118.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(2)

  Gep_arrayidx1321.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(3)

  st_32.io.GepAddr <> InputSplitter.io.Out.data.elements("field1")(4)

  Gep_arrayidx2033.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(5)

  Gep_arrayidx2135.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(6)

  Gep_arrayidx2237.io.baseAddress <> InputSplitter.io.Out.data.elements("field1")(7)

  icmp_tobool0.io.LeftIO <> InputSplitter.io.Out.data.elements("field2")(0)

  st_25.io.Out(0).ready := true.B

  st_27.io.Out(0).ready := true.B

  st_29.io.Out(0).ready := true.B

  st_31.io.Out(0).ready := true.B

  st_32.io.Out(0).ready := true.B

  st_34.io.Out(0).ready := true.B

  st_36.io.Out(0).ready := true.B

  st_38.io.Out(0).ready := true.B



  /* ================================================================== *
   *                   PRINTING OUTPUT INTERFACE                        *
   * ================================================================== */

  io.out <> ret_55.io.Out

}

import java.io.{File, FileWriter}

object test_cache01Top extends App {
  val dir = new File("RTL/test_cache01Top");
  dir.mkdirs
  implicit val p = config.Parameters.root((new MiniConfig).toInstance)
  val chirrtl = firrtl.Parser.parse(chisel3.Driver.emit(() => new test_cache01DF()))

  val verilogFile = new File(dir, s"${chirrtl.main}.v")
  val verilogWriter = new FileWriter(verilogFile)
  val compileResult = (new firrtl.VerilogCompiler).compileAndEmit(firrtl.CircuitState(chirrtl, firrtl.ChirrtlForm))
  val compiledStuff = compileResult.getEmittedCircuit
  verilogWriter.write(compiledStuff.value)
  verilogWriter.close()
}
