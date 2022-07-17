import sbt._

object Dependencies {
  lazy val `munit` = "org.scalameta" %% "munit" % "0.7.29" % "test"
  lazy val `scalatest` = "org.scalatest" %% "scalatest" % "3.2.12" % "test"
  lazy val `kafka-clients` = "org.apache.kafka" % "kafka-clients" % "3.2.0"
  lazy val `scalatra` = "org.scalatra" %% "scalatra" % "2.8.1"
  lazy val `jetty-webapp` = "org.eclipse.jetty" % "jetty-webapp" % "11.0.11" % "container"
  lazy val `javax.servlet-api` = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  lazy val `jackson-module-scala` = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2"
  lazy val `jackson-core` = "com.fasterxml.jackson.core" % "jackson-core" % "2.13.2"
  lazy val `jackson-databind` = "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.2"
}
