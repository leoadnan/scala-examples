name := "akkademy-db"

version := "1.0"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
     "com.typesafe.akka" %% "akka-actor" % "2.5.13" withSources() withJavadoc(),
     "com.typesafe.akka" %% "akka-testkit" % "2.5.13" % "test" withSources() withJavadoc(),
     "org.scalatest" %% "scalatest" % "3.0.5" % "test"
 )
