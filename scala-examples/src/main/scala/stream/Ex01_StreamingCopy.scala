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

  //A source to read from
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(inputFilePath)

  //A sink to write to
  val sink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(outputFilePath, Set(CREATE, WRITE, TRUNCATE_EXISTING))

  //The lines that create a source and a sink are declarative. 
  //They don’t create files or open file handles, but simply capture 
  //all the information that will be needed later, once the RunnableGraph is run.

  val runnableGraph: RunnableGraph[Future[IOResult]] = source.to(sink)
  //Connecting a source and a sink creates a RunnableGraph.
  //It’s also important to note that creating the RunnableGraph doesn’t start anything. 
  //It simply defines a blueprint for how to copy.

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer() //The materializer eventually creates actors that execute the graph.

  
  //Running the graph returns a Future[IOResult]; in this case,the IOResult contains a count of bytes read from the source.
  runnableGraph.run().foreach { result =>
    println(s"$result")
    println(s"${result.status}, ${result.count} bytes read.")
    system.terminate()
  }
  
  //Running the runnableGraph results in the bytes being copied 
  //from source to sink - from a file to a file in this case. 
  //A graph is said to be materialized once it is run.

  
  //The graph is stopped in this case once all data is copied. 


//  import akka.Done
//  import akka.stream.scaladsl.Keep
//
//  //Keeps the IOResult of reading the file
//  val graphLeft: RunnableGraph[Future[IOResult]] = source.toMat(sink)(Keep.left)
//
//  //Keeps the IOResult of writing the file
//  val graphRight: RunnableGraph[Future[IOResult]] = source.toMat(sink)(Keep.right)
//
//  //Keeps both
//  val graphBoth: RunnableGraph[(Future[IOResult], Future[IOResult])] = source.toMat(sink)(Keep.both)
//
//  //A custom function that just indicates the stream is done
//  val graphCustom: RunnableGraph[Future[Done]] = source.toMat(sink) { (l, r) =>
//    Future.sequence(List(l, r)).map(_ => Done)
//  }
}