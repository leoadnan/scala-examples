package stream

import java.time.ZonedDateTime

import akka.NotUsed
import akka.actor.ActorSystem

import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.stream.ActorMaterializer
import akka.stream.IOResult

import akka.util.ByteString
import com.typesafe.config.ConfigFactory

import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.Paths
import java.nio.file.Path

import spray.json._
import scala.concurrent.Future
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Keep
import akka.stream.Supervision
import akka.stream.ActorAttributes
import akka.stream.ActorMaterializerSettings

object Ex02_EventFilter extends App with EventMarshalling {
  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")

  val inputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")
  val outputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.json")

  //A source to read from
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)

  //A sink to write to
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8")) //Decodes every framed ByteString to a String log event line

  import LogStreamProcessor.LogParseException

  //Defines a decider, similar to supervision in actors
  val decider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }
  val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map(LogStreamProcessor.parseLineEx)
    //Parses the string using the parseLineEx
    //method in the LogStreamProcessor object,
    //which returns an Option[Event], or None if
    //an empty line is encountered

    .collect { case Some(e) => e }
    //Discards empty lines and extracts
    //the event in the Some case

    .withAttributes(ActorAttributes.supervisionStrategy(decider))
  //Passes the supervisor through attributes

  //The Flow[String] creates a Flow that takes String elements as input and provides String elements as output.

  //The NotUsed type is used to indicate that the materialized value isn’t important and shouldn’t be used.

  //The parse flow takes Strings and outputs Events.

  val filter: Flow[Event, Event, NotUsed] = Flow[Event].filter(_.state == "error")

  val serialize: Flow[Event, ByteString, NotUsed] = Flow[Event]
    .map(event => ByteString(event.toJson.compactPrint)) //Serializes to JSON using the spray-json library

  //Flows can be composed using via. The next listing shows the definition of the com- plete event filter flow and how it’s materialized.
  val composedFlow: Flow[ByteString, ByteString, NotUsed] = frame.via(parse).via(filter).via(serialize)

  val graphDecider: Supervision.Decider = {
    case _: LogParseException => Supervision.Resume
    case _ => Supervision.Stop
  }

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(graphDecider))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.via(composedFlow).toMat(sink)(Keep.right)

  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to '$outputFilePath'.")
    system.terminate()
  }

  //One flow for the event filter but not materialized
  val flow: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8")).map(LogStreamProcessor.parseLineEx)
      .collect { case Some(e) => e }
      .filter(_.state == "error")
      .map(event => ByteString(event.toJson.compactPrint))

}

case class Event(
  host: String,
  service: String,
  state: String,
  time: ZonedDateTime,
  description: String,
  tag: Option[String] = None,
  metric: Option[Double] = None)

case class LogReceipt(logId: String, written: Long)
case class ParseError(logId: String, msg: String)    

case class Metric(
  service: String, 
  time: ZonedDateTime, 
  metric: Double, 
  tag: String, 
  drift: Int = 0
)

case class Summary(events: Vector[Event])
