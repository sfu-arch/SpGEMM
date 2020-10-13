package utility

import chisel3._

trait UniformPrintfs {
  val printfSigil = ""

  def pp(prefix: String, message: String, args: Bits*): Unit = {
    printf(prefix + message, args:_*) }

  def printfInfo (m: String, a: Bits*) { pp("\n[INFO] ",  printfSigil++m, a:_*) }
  def printfWarn (m: String, a: Bits*) { pp("\n[WARN] ",  printfSigil++m, a:_*) }
  def printfError(m: String, a: Bits*) { pp("\n[ERROR] ", printfSigil++m, a:_*) }
  def printfDebug(m: String, a: Bits*) { pp("\n[DEBUG] ", printfSigil++m, a:_*) }
  def printfTodo (m: String, a: Bits*) { pp("\n[TODO] ",  printfSigil++m, a:_*) }
}
