import Dependencies._
import sbt.{Attributed, ThisBuild}

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "markosski"
ThisBuild / organizationName := "eventdriven"
ThisBuild / assemblyMergeStrategy := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith "reference-overrides.conf" => MergeStrategy.concat
  case "application.conf"                            => MergeStrategy.concat
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val commonSettings = List(
  scalacOptions ++= Seq(
    "-encoding", "utf8",
    "-deprecation",
    "-unchecked",
    "-Xlint",
    "-Xfatal-warnings"
  )
)

lazy val webapp = (project in file("webapp")).settings(
    name := "webapp",
    assembly / mainClass := Some("play.core.server.ProdServerStart"),
    assembly / fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value),
    libraryDependencies ++= Seq(
      `jackson-module-scala`,
      `jackson-core`,
      `jackson-databind`,
      `airframe-log`,
      `sttp`,
      munit,
      pureconfig,
      guice
    )
  )
  .dependsOn(core)
  .enablePlugins(PlayScala)

lazy val core = (project in file("core"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    name := "core",
    assembly / mainClass := Some("eventdriven.core.App"),
    libraryDependencies ++= Seq(
      `kafka-clients`,
      `jackson-module-scala`,
      `jackson-core`,
      `jackson-databind`,
      `logback-classic`,
      `slf4j`,
      munit,
      pureconfig
    )
  )

lazy val payments = (project in file("payments"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    name := "payments",
    libraryDependencies ++= Seq(
      `http4s-ember-server`,
      `http4s-dsl`,
      `airframe-log`,
      `sttp`,
      munit
    )
  ).dependsOn(core)

lazy val accounts = (project in file("accounts"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    name := "accounts",
    libraryDependencies ++= Seq(
      `http4s-ember-server`,
      `http4s-dsl`,
      `airframe-log`,
      munit
    )
  ).dependsOn(core)

lazy val transactions = (project in file("transactions"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    Compile / run / mainClass := Some("eventdriven.transactions.web.TransactionsApp"),
    name := "transactions",
    libraryDependencies ++= Seq(
      `http4s-ember-server`,
      `http4s-dsl`,
      `airframe-log`,
      munit
    )
  ).dependsOn(core)
  .enablePlugins(JettyPlugin)

lazy val root = (project in file("."))
  .settings(
    commonSettings
  )
  .aggregate(core, transactions, accounts, payments, webapp)

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
// ThisBuild / publishMavenStyle := true
