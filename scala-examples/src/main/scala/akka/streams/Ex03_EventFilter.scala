package akka.streams

import java.nio.file.Paths
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.WRITE

import scala.concurrent.Future

import com.typesafe.config.ConfigFactory

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.utils._
import spray.json._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph
import akka.stream.Supervision
import akka.stream.ActorAttributes
import akka.stream.ActorMaterializerSettings

object Ex03_EventFilter extends App with EventMarshalling {
  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")

  val inputFilePath = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")
  val outputFilePath = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.json")

  import LogStreamProcessor._
  val decider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }
  val graphDecider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  val frame: Flow[ByteString, String, NotUsed] = Framing.delimiter(ByteString("\n"), maxLine)
    .map(_.decodeString("UTF8"))
  val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }
    .withAttributes(ActorAttributes.supervisionStrategy(decider))
  val filter: Flow[Event, Event, NotUsed] = Flow[Event]
    .filter(_.state == "error")
  val serialize: Flow[Event, ByteString, NotUsed] = Flow[Event]
    .map(event => ByteString(event.toJson.compactPrint + System.lineSeparator()))

  val composedFlow: Flow[ByteString, ByteString, NotUsed] = frame.via(parse).via(filter).via(serialize)

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(graphDecider))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(composedFlow).toMat(sink)(Keep.right)

  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to 'outputFilePath'.")
    system.terminate()
  }
}