package config.cde

import scala.collection.mutable

object Dump {
  def apply[T](key:Any,value:T):T = ParameterDump.apply(key, value)
  def apply[T](knob:Knob[T]):Knob[T] = ParameterDump.apply(knob)
  def apply[T](key_base:String,values:Seq[T]):Seq[T] = {
    values.zipWithIndex.foreach{ case(value, i) => Dump(key_base + "__" + i, value) }
    Dump(key_base + "__COUNT", values.size)
    values
  }
}

object ParameterDump {
  val dump = mutable.Set[Tuple2[Any,Any]]()
  val knobList = mutable.ListBuffer[Any]()
  def apply[T](key:Any,value:T):T = {addToDump(key,value); value}
  def apply[T](knob:Knob[T]):Knob[T] = {knobList += knob.name; knob}
  def addToDump(key:Any,value:Any) = dump += ((key,value))
  def getDump:String = if (!dump.isEmpty) dump.map(_.toString).reduce(_+"\n"+_) + "\n" else ""
}
