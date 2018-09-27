package stream

import scala.concurrent.Future

import akka.NotUsed
import akka.Done

import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.RunnableGraph
import akka.stream.ActorMaterializer

import akka.actor.ActorSystem

object Ex01_Basic extends App {
  val source: Source[Int, NotUsed] = Source(1 to 10)

  val sink: Sink[Int, Future[Done]] = Sink.foreach[Int](println)
  //The Future will be completed when the Sink has finished processing all of its elements

  val dataFlow: RunnableGraph[NotUsed] = source.to(sink)
  //The result of linking the two components together is a RunnableGraph that will
  //materialize to a NotUsed, which comes from the materialized type from the Source.

  implicit val system = ActorSystem()
  implicit val mater = ActorMaterializer()
  dataFlow.run
}