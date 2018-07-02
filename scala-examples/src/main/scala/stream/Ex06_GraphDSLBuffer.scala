package stream

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import scala.concurrent.Future
import scala.concurrent.duration._

import com.typesafe.config.ConfigFactory

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.Graph
import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.stream.scaladsl.Broadcast
import akka.stream.scaladsl.MergePreferred
import akka.stream.OverflowStrategy

import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Framing
import akka.stream.scaladsl.Keep

object Ex06_GraphDSLBuffer extends App{
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

  def processEvents(): FlowLike = {
    val jsFlow = LogJson.jsonOutFlow
    val notifyOutFlow = LogJson.notifyOutFlow
    val metricOutFlow = LogJson.metricOutFlow

    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._
      val nrWarnings = 100
      val nrErrors = 10
      val archBufSize = 100000
      val warnBufSize = 100
      val errBufSize = 1000
      val errDuration = 10 seconds
      val warnDuration = 1 minute

      val toMetric = Flow[Event].collect {
        case Event(_, service, _, time, _, Some(tag), Some(metric)) =>
          Metric(service, time, metric, tag)
      }

      val recordDrift = Flow[Metric]
        .expand { metric =>
          Iterator.from(0).map(d => metric.copy(drift = d))
        }

      val bcast = builder.add(Broadcast[Event](5))
      val wbcast = builder.add(Broadcast[Event](2))
      val ebcast = builder.add(Broadcast[Event](2))
      val cbcast = builder.add(Broadcast[Event](2))
      val okcast = builder.add(Broadcast[Event](2))

      val mergeNotify = builder.add(MergePreferred[Summary](2))
      val archive = builder.add(jsFlow)

      val toNot = Flow[Event].map(e => Summary(Vector(e)))

      val ok = Flow[Event].filter(_.state == "ok")
      val warning = Flow[Event].filter(_.state == "warning")
      val error = Flow[Event].filter(_.state == "error")
      val critical = Flow[Event].filter(_.state == "critical")

      def rollup(nr: Int, duration: FiniteDuration) = {
        Flow[Event].groupedWithin(nr, duration)
          .map(events => Summary(events.toVector))
      }

      val rollupErr = rollup(nrErrors, errDuration)
      val rollupWarn = rollup(nrWarnings, warnDuration)

      val archBuf = Flow[Event].buffer(archBufSize, OverflowStrategy.fail)
      val warnBuf = Flow[Event].buffer(warnBufSize, OverflowStrategy.dropHead)
      val errBuf = Flow[Event].buffer(errBufSize, OverflowStrategy.backpressure)
      val metricBuf = Flow[Event].buffer(errBufSize, OverflowStrategy.dropHead)

      bcast ~> archBuf ~> archive.in
      bcast ~> ok ~> okcast
      bcast ~> warning ~> wbcast
      bcast ~> error ~> ebcast
      bcast ~> critical ~> cbcast

      okcast ~> jsFlow ~> logFileSink("1", "ok")
      okcast ~> metricBuf ~>
        toMetric ~> recordDrift ~> metricOutFlow ~> metricsSink

      cbcast ~> jsFlow ~> logFileSink("4", "critical")
      cbcast ~> toNot ~> mergeNotify.preferred

      ebcast ~> jsFlow ~> logFileSink("3", "error")
      ebcast ~> errBuf ~> rollupErr ~> mergeNotify.in(0)

      wbcast ~> jsFlow ~> logFileSink("2", "warning")
      wbcast ~> warnBuf ~> rollupWarn ~> mergeNotify.in(1)

      mergeNotify ~> notifyOutFlow ~> notificationSink

      FlowShape(bcast.in, archive.out)
    })
  }

  def logFileSink(logId: String, state: String) =
    FileIO.toPath(logsDir.resolve(s"$logId-$state"), Set(CREATE, WRITE, TRUNCATE_EXISTING))
  // this is just for testing,
  // in a realistice application this would be a Sink to some other
  // service, for instance a Kafka Sink, or an email service.
  val notificationSink = FileIO.toPath(logsDir.resolve("notifications.json"), Set(CREATE, WRITE, TRUNCATE_EXISTING))
  val metricsSink = FileIO.toPath(logsDir.resolve("metrics.json"), Set(CREATE, WRITE, TRUNCATE_EXISTING))

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val frame: Flow[ByteString, String, NotUsed] =
    Framing.delimiter(ByteString("\n"), maxLine)
      .map(_.decodeString("UTF8")) //Decodes every framed ByteString to a String log event line

  val parse: Flow[String, Event, NotUsed] = Flow[String]
    .map(LogStreamProcessor.parseLineEx)
    .collect { case Some(e) => e }

  val runnableGraph = source.via(frame).via(parse).via(processEvents()).toMat(logFileSink("complete", "all"))(Keep.right)

  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to 'outputFilePath'.")
    system.terminate()
  }
}