package stream

import java.nio.file.{ Path, Paths }
import java.nio.file.StandardOpenOption
import java.nio.file.StandardOpenOption._

import scala.concurrent.Future

import akka.actor.ActorSystem

import akka.NotUsed
import akka.stream.{ ActorMaterializer, IOResult }
import akka.stream.scaladsl._
import akka.stream.scaladsl.JsonFraming

import akka.util.ByteString

import spray.json._

import com.typesafe.config.{ Config, ConfigFactory }
import akka.utils._

object Ex03_BidiEventFilter extends App with EventMarshalling {
  val config = ConfigFactory.load()
  val maxLine = config.getInt("log-stream-processor.max-line")
  val maxJsonObject = config.getInt("log-stream-processor.max-json-object")

  val inputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")
  val outputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.json")

  //A source to read from
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)

  //A sink to write to
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  val contentType = "text"

  val inFlow: Flow[ByteString, Event, NotUsed] =
    if (contentType == "json") {
      //Framing for streaming JSON; maxJsonObject is the maximum number of bytes for any JsonObject
      JsonFraming.objectScanner(maxJsonObject).map(_.decodeString("UTF8").parseJson.convertTo[Event])
      //JsonFraming frames incoming bytes into JSON objects. We use spray-json here to parse the bytes containing a JSON object
    } else {
      Framing.delimiter(ByteString("\n"), maxLine)
        .map(_.decodeString("UTF8"))
        .map(LogStreamProcessor.parseLineEx)
        .collect { case Some(event) => event }
    }

  val outFlow: Flow[Event, ByteString, NotUsed] =
    if (contentType == "json") {
      Flow[Event].map(event => ByteString(event.toJson.compactPrint))
    } else {
      Flow[Event].map { event =>
        ByteString(LogStreamProcessor.logLine(event))
        //LogStreamProcessor.logLine method serializes an event to a log line
      }
    }

  val bidiFlow = BidiFlow.fromFlows(inFlow, outFlow)

  //fromFlows creates a BidiFlow from two flows, for deserialization and serialization.

  //The BidiFlow can be joined on top of the filter flow with join

  val filter: Flow[Event, Event, NotUsed] = Flow[Event].filter(_.state == "error")

  val flow = bidiFlow.join(filter)

  //Another way to think about a BidiFlow is that it provides two flows that you can connect
  //before and after an existing flow, to adapt on the input side and on the output side of
  //the flow in question.

  val runnableGraph: RunnableGraph[Future[IOResult]] =
    source.via(flow).toMat(sink)(Keep.right)

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to '$outputFilePath'.")
    system.terminate()
  }
}