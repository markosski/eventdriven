import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "markosski"
ThisBuild / organizationName := "eventdriven"
ThisBuild / assemblyMergeStrategy := {
  case PathList("javax", "servlet", xs @ _*)         => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.discard
  case "application.conf"                            => MergeStrategy.concat
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}

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
      munit
    )
  )

lazy val payments = (project in file("payments"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    name := "payments",
    libraryDependencies ++= Seq(
      `http4s-ember-server`,
      `http4s-dsl`,
      `logback-classic`,
      `airframe-log`,
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
      `logback-classic`,
      `airframe-log`,
      munit
    )
  ).dependsOn(core)

lazy val transactions = (project in file("transactions"))
  .settings(
    testFrameworks += new TestFramework("munit.Framework"),
    Compile / run / mainClass := Some("eventdriven.transactions.infrastructure.web.TransactionsApp"),
    name := "transactions",
    libraryDependencies ++= Seq(
      `http4s-ember-server`,
      `http4s-dsl`,
      `logback-classic`,
      `airframe-log`,
      munit
    )
  ).dependsOn(core)
  .enablePlugins(JettyPlugin)

lazy val root = (project in file("."))
  .aggregate(core, transactions, accounts, payments)

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
