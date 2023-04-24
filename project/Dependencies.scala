import sbt._

object Dependencies {
  val Http4sVersion = "0.23.13"
  val LogbackVersion = "1.2.10"
  val AkkaVersion = "2.7.0"
  val AkkaHttpVersion = "10.5.1"
  
  lazy val `munit` = "org.scalameta" %% "munit" % "0.7.29" % "test"
  lazy val `scalatest` = "org.scalatest" %% "scalatest" % "3.2.12" % "test"
  lazy val `kafka-clients` = "org.apache.kafka" % "kafka-clients" % "3.2.0"
  lazy val `scalatra` = "org.scalatra" %% "scalatra" % "2.8.1"
  lazy val `jetty-webapp` = "org.eclipse.jetty" % "jetty-webapp" % "11.0.11" % "container"
  lazy val `javax.servlet-api` = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  lazy val `jackson-module-scala` = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2"
  lazy val `jackson-core` = "com.fasterxml.jackson.core" % "jackson-core" % "2.13.2"
  lazy val `jackson-databind` = "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.2"
  lazy val `airframe-log` = "org.wvlet.airframe" %% "airframe-log" % "22.7.3"
  // lazy val `http4s-ember-server` = "org.http4s" %% "http4s-ember-server" % Http4sVersion
  // lazy val `http4s-dsl` = "org.http4s" %% "http4s-dsl" % Http4sVersion
  lazy val `logback-classic` = "ch.qos.logback" % "logback-classic" % LogbackVersion
  lazy val `slf4j` = "org.slf4j" % "slf4j-api" % "1.7.30"
  lazy val `pureconfig` = "com.github.pureconfig" %% "pureconfig" % "0.17.2"
  lazy val `sttp` = "com.softwaremill.sttp.client3" %% "core" % "3.8.3"
  lazy val `akka` = Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  )
}
