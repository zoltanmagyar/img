name := "img"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.4.0",
  "co.fs2" %% "fs2-io" % "2.4.0",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % "2.6.8" % Test,
  "org.scalatest" %% "scalatest" % "3.2.0" % Test
)
