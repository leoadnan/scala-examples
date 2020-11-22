package akka.streams

import akka.stream.scaladsl.Source
import akka.NotUsed
import akka.stream.scaladsl.Flow
import java.io.File
import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import akka.Done
import akka.stream.scaladsl.RunnableGraph
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Ex01_SimpleStreamsApp extends App {
  val fileList: List[String] = List(
    "src/main/resources/testfile1.txt",
    "src/main/resources/testfile2.txt", "src/main/resources/testfile3.txt")

  val source: Source[String, NotUsed] = Source(fileList)
  val flow: Flow[String, File, NotUsed] = Flow[String]
    .map(new File(_))
    .filter(_.exists())
    .filter(_.length() != 0)
  val sink: Sink[File, Future[Done]] = Sink.foreach(f => println(s"Absolute Path: ${f.getAbsoluteFile}"))
  val runnableGraph: RunnableGraph[NotUsed] = source.via(flow).to(sink)
  implicit val actorSystem = ActorSystem("SimpleStreamApplication")
  implicit val materializer = ActorMaterializer()
  runnableGraph.run()
  
  val stream = Source(fileList)
    .map(new File(_))
    .filter(_.exists())
    .filter(_.length()!=0)
    .to(Sink.foreach(f => println(s"*Absolute Path: ${f.getAbsoluteFile}")))
  stream.run()
}