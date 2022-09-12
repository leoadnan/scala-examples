name := """scala-examples"""

version := "1.0"

scalaVersion := "2.11.12"

fork in run := true

val akkaVersion = "2.5.16"
val akkaHttpVersion = "10.1.5"

libraryDependencies ++= Seq(

	"com.typesafe.akka" %% "akka-actor"           % akkaVersion withSources() withJavadoc(),
	"com.typesafe.akka" %% "akka-stream"          % akkaVersion withSources() withJavadoc(),
	
	"com.typesafe.akka" %% "akka-http-core"       % akkaHttpVersion withSources() withJavadoc(),
 	"com.typesafe.akka" %% "akka-http"            % akkaHttpVersion withSources() withJavadoc(),
	"com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion withSources() withJavadoc(),

    "com.typesafe.akka" %% "akka-stream-kafka"    % "0.22" withSources() withJavadoc(),
 	
    "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion % "test" withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-testkit"         % akkaVersion % "test" withSources() withJavadoc(),
    
    "org.scalatest"     %% "scalatest"           % "3.0.5"     % "test" withSources() withJavadoc()
    
)