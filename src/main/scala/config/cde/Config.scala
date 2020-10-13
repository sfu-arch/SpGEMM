package config.cde

class Config(
  val topDefinitions: World.TopDefs = { (a,b,c) => throw new CDEMatchError(a) },
  val topConstraints: List[ViewSym=>Ex[Boolean]] = List( ex => ExLit[Boolean](true) ),
  val knobValues: Any => Any = { case x => throw new CDEMatchError(x) }
) {
  import Implicits._
  type Constraint = ViewSym=>Ex[Boolean]

  def this(that: Config) = this(that.topDefinitions,
                                      that.topConstraints,
                                      that.knobValues)

  def ++(that: Config) = {
    new Config(this.addDefinitions(that.topDefinitions),
                      this.addConstraints(that.topConstraints),
                      this.addKnobValues(that.knobValues))
  }

  def addDefinitions(that: World.TopDefs): World.TopDefs = {
    (pname,site,here) => {
      try this.topDefinitions(pname, site, here)
      catch {
        case e: scala.MatchError => that(pname, site, here)
        case e: CDEMatchError => that(pname, site, here)
      }
    }
  }

  def addConstraints(that: List[Constraint]):List[Constraint] = {
    this.topConstraints ++ that
  }


  def addKnobValues(that: Any=>Any): Any=>Any = { case x =>
    try this.knobValues(x)
    catch {
      case e: scala.MatchError => that(x)
      case e: CDEMatchError => that(x)
    }
  }

  def toCollector = new Collector(this.topDefinitions, this.knobValues)
  def toInstance = new Instance(this.topDefinitions, this.knobValues)
  override def toString = this.getClass.getSimpleName
}
