package config.cde
import scala.collection.immutable.{Seq=>Seq, Iterable=>Iterable}
import scala.{collection=>readonly}
import scala.collection.mutable

// Convention: leading _'s on names means private to the outside world
// but accessible to anything in this file.

/** Custom "MatchError" to improve performance of CDE
  * Mirrors [[scala.MatchError]]
  */
final class CDEMatchError(obj: Any = null) extends Exception {
  override def fillInStackTrace() = this

  // Borrowed from scala.MatchError
  // lazy so that objString is only created upon calling getMessage
  private lazy val objString = {
    def ofClass = "of class " + obj.getClass.getName
    if (obj == null) "null"
    else try {
      obj.toString + " (" + ofClass + ")"
    } catch {
      case _: Throwable => "an instance " + ofClass
    }
  }

  override def getMessage = objString
}

class ParameterUndefinedException(field:Any, cause:Throwable=null)
  extends RuntimeException("Parameter " + field + " undefined.", cause)

class KnobUndefinedException(field:Any, cause:Throwable=null)
  extends RuntimeException("Knob " + field + " undefined.", cause)

// Knobs are top level free variables that go into the constraint solver.
final case class Knob[T](name:Any)

// Fields are wrappers around particular a particular parameter's type
class Field[T]


// objects given to the user in mask functions (site,here,up)
abstract class View {
  protected val deftSite: View // when views are queried without a specifying a site this is the default

  // use `this` view's behavior to query for a parameters value as if
  // the original site were `site`
  def apply[T](pname:Any, site:View):T
  def sym[T](pname:Any, site:View):Ex[T]

  // query for a parameters value using the default site
  def apply[T](knob:Knob[T]):T
  final def apply[T](pname:Any):T = apply[T](pname, deftSite)
  final def apply[T](field:Field[T]):T = apply[T](field.asInstanceOf[Any], deftSite)

  final def sym[T](pname:Any):Ex[T] = sym[T](pname, deftSite)
  final def sym[T](field:Field[T]):Ex[T] = sym[T](field.asInstanceOf[Any], deftSite)
}

/* Wrap a View to make the application return the symbolic expression,
 * basically a shorthand to save typing '.sym'
 * before:
 *   val v: View
 *   v.sym[Int]("x") // returns Ex[_]
 * now:
 *   val vs = ViewSym(v)
 *   vs[Int]("xs") // Ex[_]
*/
final case class ViewSym(view:View) {
  def apply[T](f:Any):Ex[T] = view.sym[T](f)
  def apply[T](f:Field[T]):Ex[T] = view.sym[T](f)
  def apply[T](f:Any, site:View):Ex[T] = view.sym[T](f, site)
  def apply[T](f:Field[T], site:View):Ex[T] = view.sym[T](f, site)
}


// internal type to represent functions that evaluate parameter values
abstract class _Lookup {

  def apply[T](pname:Any, site:View):Ex[T]

  // build a new Lookup that just defers to this one
  final def push() = {
    val me = this
    new _Lookup {
      def apply[T](pname:Any, site:View) = me.apply(pname, site)
    }
  }
}

// Internal type used as name in all ExVar[T]'s
sealed abstract class _Var[T]

// Variables which are 'free' parameters when seen from the top level.
final case class _VarKnob[T](kname:Any) extends _Var[T] {
  override def toString = kname.toString
}
// Variables whose values are computed by `expr`. The term 'let' comes
// from the idea of 'let' bindings in functional languages i.e.:
final case class _VarLet[T](pname:Any,expr:Ex[T]) extends _Var[T] {
  override def toString = pname.toString + "{" + expr.toString + "}"
}


object World {
  // An alias for the type of function provided by user to describe parameters that
  // reach the top level. The return of this function can be either:
  //   Knob(k): this parameter maps to the constraint variable `k`
  //   Ex: this parameter is computed using the expression
  //   Any(thing else): variable takes a literal value
  type TopDefs = (/*pname:*/Any,/*site:*/View,/*here:*/View) => Any/*Knob[_] | Ex[_] | Any*/
}

// Worlds collect the variable definitions and constraints seen when building hardware.
abstract class World(
    topDefs: World.TopDefs
  ) {

  val _knobs = new mutable.HashSet[Any]
  abstract class _View extends View {
    val look: _Lookup

    def apply[T](knob:Knob[T]):T = {
      _eval(ExVar[T](_VarKnob[T](knob.name)))
    }
    def apply[T](pname:Any, site:View):T = {
      _eval(look(pname, site).asInstanceOf[Ex[T]])
    }
    def sym[T](pname:Any, site:View):Ex[T] = {
      _bindLet[T](pname,look(pname, site).asInstanceOf[Ex[T]])
    }
  }

  // evaluate an expression against this world
  def _eval[T](e:Ex[T]):T = {
    Ex.eval(e, {
      case v:_VarKnob[_] => {
        _knobs += v.kname
        val e = _knobValue(v.kname)
        if(ParameterDump.knobList.contains(v.kname)) {ParameterDump.addToDump(v.kname,e);e} else e
      }
      case v:_VarLet[_] => _eval(v.expr.asInstanceOf[Ex[T]])
    })
  }

  // create a view whose default site is itself
  def _siteView(look:_Lookup):View = {
    val _look = look
    new _View {
      val look = _look
      val deftSite = this
    }
  }

  // create a View which with a supplied default site
  def _otherView(look:_Lookup, deftSite:View):View = {
    val _look = look
    val _deft = deftSite
    new _View {
      val look = _look
      val deftSite = _deft
    }
  }

  // the top level lookup
  def _topLook():_Lookup = {
    class TopLookup extends _Lookup {

      def apply[T](pname:Any, site:View):Ex[T] = {
        val here = _otherView(this, site)
        (
          try topDefs(pname, site, here)
          catch {
            case e:scala.MatchError => throw new ParameterUndefinedException(pname, e)
            case e:CDEMatchError => throw new ParameterUndefinedException(pname, e)
          }
        ) match {
          case k:Knob[T @unchecked] => ExVar[T](_VarKnob[T](k.name))
          case ex:Ex[T @unchecked] => _bindLet[T](pname,ex)
          case lit => ExLit(lit.asInstanceOf[T])
        }
      }
    }
    new TopLookup
  }

  def _bindLet[T](pname:Any,expr:Ex[T]):Ex[T]

  def _constrain(e:Ex[Boolean]):Unit

  def _knobValue(kname:Any):Any

  def getConstraints:String = ""

  def getKnobs:String = ""
}

// a world responsible for collecting all constraints in the first pass
class Collector(
    topDefs: World.TopDefs,
    knobVal: Any=>Any // maps knob names to default-values
  )
  extends World(topDefs) {

  val _constraints = new mutable.HashSet[Ex[Boolean]]

  def knobs():List[Any] = {
    _knobs.toList
  }

  def constraints():List[Ex[Boolean]] = {
    _constraints.toList
  }

  def _bindLet[T](pname:Any,expr:Ex[T]):Ex[T] = {
    expr match {
      case e:ExVar[T] => expr
      case e:ExLit[T] => expr
      case _ => ExVar[T](_VarLet[T](pname,expr))
    }
  }

  def _constrain(c:Ex[Boolean]) = {
    _constraints += c // add the constraint

    // Also add all equality constraints for all bound variables in the
    // constraint expression and do it recursively for all expressions
    // being bound to.
    var q = List[Ex[_]](c)
    while(!q.isEmpty) {
      val e = q.head  // pop an expression
      q = q.tail
      // walk over the variables in `e`
      for(e <- Ex.unfurl(e)) {
        e match {
          case ExVar(_VarLet(p,e1)) => {
            // form the equality constraint
            val c1 = ExEq[Any](e.asInstanceOf[Ex[Any]], e1.asInstanceOf[Ex[Any]])
            // recurse into the expression if its never been seen before
            if(!_constraints.contains(c1)) {
              _constraints += c1
              q ::= e1 // push
            }
          }
          case _ => {}
        }
      }
    }
  }

  def _knobValue(kname:Any) = {
     try knobVal(kname)
     catch {
       case e:scala.MatchError => throw new KnobUndefinedException(kname, e)
     }
  }

  override def getConstraints:String = if(constraints.isEmpty) "" else constraints.map("( " + _.toString + " )").reduce(_ +"\n" + _) + "\n"

  override def getKnobs:String = if(knobs.isEmpty) "" else {
    knobs.map(_.toString).reduce(_ + "\n" + _) + "\n"
  }
}

// a world instantianted to a specific mapping of knobs to values
class Instance(
    topDefs: World.TopDefs,
    knobVal: Any=>Any
  )
  extends World(topDefs) {

  def _bindLet[T](pname:Any,expr:Ex[T]):Ex[T] = expr
  def _constrain(e:Ex[Boolean]) = {}
  def _knobValue(kname:Any) = {
     try knobVal(kname)
     catch {
       case e:scala.MatchError => throw new KnobUndefinedException(kname, e)
     }
  }
}

object Parameters {
  def root(w:World) = {
    new Parameters(w, w._topLook())
  }
  def empty = Parameters.root(new Collector((a,b,c) => {throw new ParameterUndefinedException(a); a},(a:Any) => {throw new KnobUndefinedException(a); a}))

  // Mask making helpers

  // Lift a regular function into a mask by looking for MatchError's and
  // interpreting those as calls to up
  def makeMask(mask:(Any,View,View,View)=>Any) = {
    (f:Any, site:View, here:View, up:View) => {
      try mask(f,site,here,up)
      catch {case e:MatchError => up.sym[Any](f, site)}
    }
  }

  // Lift a Map to be a mask.
  def makeMask(mask:Map[Any,Any]) = {
    (f:Any, site:View, here:View, up:View) => {
      mask.get(f) match {
        case Some(y) => y
        case None => up.sym[Any](f, site)
      }
    }
  }

  // Lift a PartialFunction to be a mask.
  def makeMask(mask:PartialFunction[Any,Any]) = {
    (f:Any, site:View, here:View, up:View) => {

      if(mask.isDefinedAt(f))
        mask.apply(f)
      else {
        up.sym[Any](f, site)
      }
    }
  }
}

final class Parameters(
    private val _world: World,
    private val _look: _Lookup
  ) {

  private def _site() = _world._siteView(_look)

  // Create a new Parameters that just defers to this one. This is identical
  // to doing an `alter` but not overriding any values.
  def push():Parameters =
    new Parameters(_world, _look.push())

  def apply[T](field:Any):T =
    _world._eval(_look(field, _site())).asInstanceOf[T]

  def apply[T](field:Field[T]):T =
    _world._eval(_look(field, _site())).asInstanceOf[T]

  def constrain(gen:ViewSym=>Ex[Boolean]) = {
    val g = gen(new ViewSym(_site()))
    assert(_world._eval(g), s"Constraint failed: $g")
    _world._constrain(g)
  }

  private def _alter(mask:(/*field*/Any,/*site*/View,/*here*/View,/*up*/View)=>Any) = {
    class KidLookup extends _Lookup {

      def apply[T](f:Any, site:View):Ex[T] = {
        val here = _world._otherView(this, site)
        val up = _world._otherView(_look, site)

        mask(f, site, here, up) match {
          case e:Ex[T @unchecked] => e
          case lit => ExLit(lit.asInstanceOf[T])
        }
      }
    }

    new Parameters(_world, new KidLookup)
  }

  def alter(mask:(/*field*/Any,/*site*/View,/*here*/View,/*up*/View)=>Any) =
    _alter(Parameters.makeMask(mask))

  def alter[T](mask:Map[T,Any]) =
    _alter(Parameters.makeMask(mask.asInstanceOf[Map[Any,Any]]))

  def alterPartial(mask:PartialFunction[Any,Any]) =
    _alter(Parameters.makeMask(mask))

  def getConstraints:String = _world.getConstraints

  def getKnobs:String = _world.getKnobs
}

