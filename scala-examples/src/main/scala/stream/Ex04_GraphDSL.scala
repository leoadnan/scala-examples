package stream

import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE

import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import akka.util.ByteString
import scala.concurrent.Future
import akka.stream.IOResult
import akka.stream.Graph
import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Broadcast
import java.nio.file.Files
import java.nio.file.FileSystems
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.stream.ActorMaterializerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Keep

object Ex04_GraphDSL extends App with EventMarshalling {
  val config = ConfigFactory.load()

  val logsDir = {
    val dir = config.getString("log-stream-processor.logs-dir")
    Files.createDirectories(FileSystems.getDefault.getPath(dir))
  }

  val maxLine = config.getInt("log-stream-processor.max-line")

  val inputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")

  //A source to read from
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)

  type FlowLike = Graph[FlowShape[Event, ByteString], NotUsed]

  def processStates(): FlowLike = {
    val jsFlow = LogJson.jsonOutFlow

    //A flow will be created from the Graph.
    Flow.fromGraph(g = GraphDSL.create() { implicit builder => //builder is a GraphDSL.Builder

      //Brings the DSL methods into scope
      import GraphDSL.Implicits._

      //Adds a Broadcast node to the Graph
      val bcast = builder.add(Broadcast[Event](5))

      //Adds a Flow node to the Graph to pass through all events, unchanged in JSON format
      val js = builder.add(jsFlow)

      val ok = Flow[Event].filter(_.state == "ok")
      val warning = Flow[Event].filter(_.state == "warning")
      val error = Flow[Event].filter(_.state == "error")
      val critical = Flow[Event].filter(_.state == "critical")

      //One of the Broadcast outputs writes directly to the inlet of the js node for all events.
      bcast ~> js.in

      //For every other output, a filter is added in front of the JSON Flow.
      bcast ~> ok ~> jsFlow ~> logFileSink("1", "ok")
      bcast ~> warning ~> jsFlow ~> logFileSink("2", "warning")
      bcast ~> error ~> jsFlow ~> logFileSink("3", "error")
      bcast ~> critical ~> jsFlow ~> logFileSink("4", "critical")

      //Creates a Flow-shaped Graph out of the inlet of the Broadcast and the outlet of the JSON Flow
      FlowShape(bcast.in, js.out)
    })
  }

  //  def logFileSource(logId: String, state: String) = FileIO.fromPath(logStateFile(logId, state))
  def logFileSink(logId: String, state: String) =
    FileIO.toPath(logsDir.resolve(s"$logId-$state"), Set(CREATE, WRITE, TRUNCATE_EXISTING))

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8")) //Decodes every framed ByteString to a String log event line

  val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }

  val runnableGraph = source.via(frame).via(parse).via(processStates()).toMat(logFileSink("complete", "all"))(Keep.right)

   runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to 'outputFilePath'.")
    system.terminate()
  }
}