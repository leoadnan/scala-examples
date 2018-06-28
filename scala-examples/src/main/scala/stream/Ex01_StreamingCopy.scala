package stream

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
import java.nio.file.StandardOpenOption.CREATE
import java.nio.file.StandardOpenOption.WRITE

import scala.concurrent.Future

import akka.stream.IOResult
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Ex01_StreamingCopy extends App {

  val inputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/logfile.log")
  val outputFilePath: Path = Paths.get("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/copy_logfile.log")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)

  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer() //The materializer eventually creates actors that execute the graph.

  //Running the graph returns a Future[IOResult]; in this case,the IOResult contains a count of bytes read from the source.
  runnableGraph.run().foreach { result =>
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
}