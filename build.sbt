name := "SPAGHETTI"

organization := "edu.sfu.arch"

version := "0.1-SNAPSHOT"


def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    // If we're building with Scala > 2.11, enable the compile option
    //  switch to support our anonymous Bundle definitions:
    //  https://github.com/scala/bug/issues/10047
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

autoAPIMappings := true
scalaVersion := "2.12.8"
crossScalaVersions := Seq("2.12.8", "2.11.12")
scalacOptions :=
  Seq("-deprecation", "-feature", "-unchecked", "-language:reflectiveCalls") ++ scalacOptionsVersion(scalaVersion.value)

/**
  * D - show durations
  * S - show short stack traces
  * F - show full stack traces
  * W - Without color
  **/
testOptions in Test += Tests.Argument("-oDS")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Provide a managed dependency on X if -DXVersion="" is supplied on the command line.
val defaultVersions = Map(
  "chisel3" -> "3.3-SNAPSHOT",
  "chisel-iotesters" -> "1.3-SNAPSHOT",
  "dsptools" -> "1.3-SNAPSHOT"
)

libraryDependencies ++= Seq("chisel3", "chisel-iotesters","dsptools").map {
  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
}

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "org.scalacheck" %% "scalacheck" % "1.13.4",
  "com.lihaoyi" %% "sourcecode" % "0.1.4" // Scala-JVM
)

// Berkley hardfloat. locally published (built with chisel2 scala 2.11)
libraryDependencies ++= Seq("edu.berkeley.cs" %% "dsptools" % "1.3-SNAPSHOT")
libraryDependencies ++= Seq("edu.berkeley.cs" %% "hardfloat" % "1.3-SNAPSHOT")


resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

// Recommendations from http://www.scalatest.org/user_guide/using_scalatest_with_sbt
logBuffered in Test := false

// Disable parallel execution when running te
//  Running tests in parallel on Jenkins currently fails.
parallelExecution in Test := false

