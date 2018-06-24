name := """scala-examples"""

version := "1.0"

scalaVersion := "2.12.3"

fork in run := true

libraryDependencies ++= Seq(

	"com.typesafe.akka" %% "akka-actor" % "2.5.4" withSources() withJavadoc()
	    
)