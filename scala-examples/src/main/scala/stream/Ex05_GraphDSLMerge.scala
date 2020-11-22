package stream

import akka.NotUsed
import akka.util.ByteString
import akka.event.Logging.Warning
import java.nio.file.FileSystems
import java.nio.file.Files
import com.typesafe.config.ConfigFactory
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.GraphDSL
import akka.stream.SourceShape
import akka.stream.scaladsl.Merge
import java.nio.file.Paths
import java.nio.file.Path
import akka.stream.ActorMaterializerSettings
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph
import scala.concurrent.Future
import akka.stream.IOResult
import akka.stream.scaladsl.Sink

import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE
import akka.stream.scaladsl.Source
import akka.utils.EventMarshalling

object Ex05_GraphDSLMerge extends App with EventMarshalling {

  val config = ConfigFactory.load()

  val logsDir = {
    val dir = config.getString("log-stream-processor.logs-dir")
    Files.createDirectories(FileSystems.getDefault.getPath(dir))
  }

  val maxJsObject = config.getString("log-stream-processor.max-json-object").toInt

  def logFileSource(logId: String, state: String) = FileIO.fromPath(logsDir.resolve(s"$logId-$state"))

  def mergeNotOk(): Source[ByteString, NotUsed] = {
    val warning = logFileSource("2", "warning").via(LogJson.jsonFramed(maxJsObject))
    val error = logFileSource("3", "error").via(LogJson.jsonFramed(maxJsObject))
    val critical = logFileSource("4", "critical").via(LogJson.jsonFramed(maxJsObject))

    Source.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._

      val warningShape = builder.add(warning)
      val errorShape = builder.add(error)
      val criticalShape = builder.add(critical)
      val merge = builder.add(Merge[ByteString](3))

      warningShape ~> merge
      errorShape ~> merge
      criticalShape ~> merge
      SourceShape(merge.out)
    })
  }

  val outputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.json")
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val runnableGraph = mergeNotOk().toMat(sink)(Keep.right)
  
  runnableGraph.run().foreach { result =>
    println(s"Wrote ${result.count} bytes to '$outputFilePath'.")
    system.terminate()
  }
}