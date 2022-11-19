
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "eventdriven"
ThisBuild / version := "1.0-SNAPSHOT"

assembly / mainClass := Some("play.core.server.ProdServerStart")
assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value)
ThisBuild / assemblyMergeStrategy := {
  case PathList(ps@_*) if ps.last endsWith "module-info.class" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith "reference-overrides.conf" => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "webapp"
  )
  .enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.13.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.2"
libraryDependencies += "com.softwaremill.sttp.client3" %% "core" % "3.8.3"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.2"
libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "22.7.3"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "eventdriven.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "eventdriven.binders._"
