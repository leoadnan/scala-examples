package akka.streams

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

import scala.concurrent.Future

import com.typesafe.config.ConfigFactory

import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.stream.scaladsl.Flow
import akka.NotUsed
import akka.stream.scaladsl.Framing
import akka.utils._

object Ex03_EventFilter extends App with EventMarshalling {
  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")

  val inputFilePath = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")
  val outputFilePath = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.json")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  val frame: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), maxLine)
    .map(_.decodeString("UTF8"))
  val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
}