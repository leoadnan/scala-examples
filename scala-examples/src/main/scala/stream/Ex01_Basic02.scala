package stream

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import scala.concurrent.Future
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph

object Ex01_Basic02 extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mater = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 5)
  val sink: Sink[Int, Future[Int]] = Sink.fold(0)(_ + _)
  val dataFlow: RunnableGraph[Future[Int]] = source.toMat(sink)(Keep.right)

  val fut: Future[Int] = dataFlow.run
  fut.onComplete(println)
}