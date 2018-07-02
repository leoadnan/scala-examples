package stream

import java.nio.file.Paths

import akka.actor.ActorSystem

import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

object Ex02_TransformingStreamsApp extends App {
  implicit val system = ActorSystem("TransformingStreamsApp")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val path = Paths.get("src/main/resources/gzipped-file.gz")

  /**
   * Source stages
   */
  val source = FileIO.fromPath(path)

  /**
   * Processing stages
   */
  val gunzip = Flow[ByteString].via(Compression.gunzip())

  val utf8UppercaseMapper = Flow[ByteString].map(_.utf8String.toUpperCase)

  val utf8LowercaseMapper = Flow[ByteString].map(_.utf8String.toLowerCase)

  //Takes one element and outputs from 0 to n elements. Its sematic is same as flatMap
  val splitter = Flow[String].mapConcat(_.split(" ").toList)

  val punctuationMapper = Flow[String].map(_.replaceAll("""[\p{Punct}&&[^.]]""", "").replaceAll(System.lineSeparator(), ""))

  val filterEmptyElements = Flow[String].filter(_.nonEmpty)

  val wordCountFlow = Flow[String]
    .groupBy(100, identity) //demultiplexes the incoming stream into separate output streams, one for each element key.
    //We used identity as our key, so each world would generate a different output stream.
    .map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams //flatten the substreams

  /**
   * Sink stages
   */
  val printlnSink = Sink.foreach(println)

  /**
   * Streams that reuse stages
   */

  val streamUppercase = source
    .via(gunzip)
    .via(utf8UppercaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .via(wordCountFlow)
    .to(printlnSink)

  val streamLowercase = source
    .via(gunzip)
    .via(utf8LowercaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .via(wordCountFlow)
    .to(printlnSink)

  streamUppercase.run()
  streamLowercase.run()

}