package akka.streams

import java.nio.file.Paths
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Compression
import akka.util.ByteString
import akka.stream.scaladsl.Sink

object Ex04_TransformingStreamsApp extends App {
  implicit val system = ActorSystem("TransformingStreamsApp")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val path = Paths.get("src/main/resources/gzipped-file.gz")

  val source = FileIO.fromPath(path)
  val gunzip = Flow[ByteString].via(Compression.gunzip())
  val utf8LowercaseMapper = Flow[ByteString].map(_.utf8String.toLowerCase)
  val splitter = Flow[String].mapConcat(_.split(System.lineSeparator()).toList).mapConcat(_.split(" ").toList)
  val punctuationMapper = Flow[String].map(_.replaceAll("""[\p{Punct}&&[^.]]""", ""))
  val filterEmptyElements = Flow[String].filter(_.nonEmpty)
  val wordCountFlow = Flow[String]
    .groupBy(100, identity)
    .map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams

  val printlnSink = Sink.foreach(println)

  val streamLowercase = source
    .via(gunzip)
    .via(utf8LowercaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .via(wordCountFlow)
    .to(printlnSink)

  streamLowercase.run()
}