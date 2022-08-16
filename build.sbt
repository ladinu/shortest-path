import sbt.Keys.{scalaVersion, _}
import sbt.{addCompilerPlugin, _}

import scala.language.postfixOps

val org = "org.ladinu"

addCommandAlias(
  name = "check",
  value = ";scalafmtCheckAll ;compile:scalafix --check ;test:scalafix --check"
)

val commonScalacOptions =
  Seq(
    "-encoding",
    "UTF-8",
    "-Ypatmat-exhaust-depth",
    "off",
    "-Wconf:msg=While parsing annotations in:silent",
    "-Yrangepos",
    "-Ywarn-dead-code",
    "-Ywarn-unused",
    "-Ywarn-value-discard",
    "-Xlint:-nullary-unit",
    "-Xfatal-warnings",
    "-deprecation",
    "-feature",
    "-explaintypes",
    "-unchecked",
    "-language:higherKinds"
  )

val commonSettings: Seq[Setting[_]] = Seq(
  Test / parallelExecution := true,
  scalaVersion := "2.13.6",
  version := "0.0.1",
  organization := org,
  Test / logBuffered := false,
  addCompilerPlugin(scalafixSemanticdb),
  ThisBuild / scalafixDependencies += "com.github.vovapolu" %% "scaluzzi" % "0.1.12",
  scalacOptions := commonScalacOptions,
  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
)

lazy val projectDeps = new {
  val Http4sVersion = "0.23.6"
  val CirceVersion = "0.14.1"
  val CatsEffectVersion = "3.3.0"
  val DeclineVersion = "2.1.0"
}

lazy val `shortest-path` = project
  .in(file("shortest-path"))
  .settings(commonSettings: _*)
  .settings(
    packGenerateWindowsBatFile := false,
    packResourceDir += (baseDirectory.value / "bin" -> "node"),
    packResourceDir += (baseDirectory.value / "src" / "main" / "resources" -> "conf"),
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % projectDeps.CatsEffectVersion,
      "org.http4s" %% "http4s-blaze-client" % projectDeps.Http4sVersion,
      "org.http4s" %% "http4s-circe" % projectDeps.Http4sVersion,
      "org.http4s" %% "http4s-dsl" % projectDeps.Http4sVersion,
      "org.http4s" %% "http4s-circe" % projectDeps.Http4sVersion,
      "io.circe" %% "circe-core" % projectDeps.CirceVersion,
      "io.circe" %% "circe-generic" % projectDeps.CirceVersion,
      "io.circe" %% "circe-parser" % projectDeps.CirceVersion,
      "com.monovore" %% "decline" % projectDeps.DeclineVersion
    )
  )
  .enablePlugins(PackPlugin)

lazy val root = project
  .in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "shortest-path"
  )
  .dependsOn(
    `shortest-path`
  )
  .aggregate(
    `shortest-path`
  )
