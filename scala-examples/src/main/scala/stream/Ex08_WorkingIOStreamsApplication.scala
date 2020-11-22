package stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.ByteString
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Tcp

object Ex08_WorkingIOStreamsApplication extends App {
  implicit val actorSystem = ActorSystem("WorkingIOStreams")
  implicit val actorMaterializer = ActorMaterializer()

  val MaxGroups = 1000

  val wordCountFlow = Flow[ByteString]
    .map(_.utf8String.toUpperCase())
    .mapConcat(_.split(" ").toList)
    .collect {
      case w if w.nonEmpty =>
        w.replaceAll("""[p{Punct}&&[^.]]""", "").replaceAll(System.lineSeparator(), "")
    }
    .groupBy(1000, identity)
    .map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams
    .map(x => ByteString(s"[${x._1} => ${x._2}]\n"))

  val connections = Tcp().bind("127.0.0.1", 1234)

  connections.runForeach(conn => conn.handleWith(wordCountFlow))
}