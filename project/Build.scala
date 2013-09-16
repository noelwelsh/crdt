import sbt._
import sbt.Keys._
import sbt.Keys.{`package` => packageKey}

object Build extends Build {

  lazy val crdtResolvers = Seq(
    "Sonatype"      at "http://oss.sonatype.org/content/repositories/releases",
    "Sonatype Snap" at "http://oss.sonatype.org/content/repositories/snapshots",
    "Typesafe"      at "http://repo.typesafe.com/typesafe/releases"
  )

  lazy val algebirdCore = "com.twitter" %% "algebird-core" % "0.2.0"

  lazy val scalaCheck = "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
  lazy val specs2     = "org.specs2"     %% "specs2"     % "2.2"    % "test"

  lazy val crdtSettings: Seq[Setting[_]] = Seq(
    organization := "Untyped",
    scalaVersion := "2.10.2",
    crossScalaVersions := Seq("2.10.2"),
    resolvers ++= crdtResolvers,
    libraryDependencies ++= Seq(
      algebirdCore,
      specs2,
      scalaCheck
    )
  )

  lazy val root: Project =
    Project(
      id = "root",
      base = file(".")
    ).configs(
    ).settings(
      Project.defaultSettings ++
      crdtSettings ++
      Seq(
        exportJars                 := true,
        artifactPath in Compile   <<= crossTarget(_ / "crdt.jar")
      ) : _*
    )

}
