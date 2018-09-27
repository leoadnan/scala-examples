package stream

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph

object Ex01_Basic03 extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mater = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 5)
  val flow: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2).filter(_ % 2 == 0)
  val sink: Sink[Int, Future[Int]] = Sink.fold(0)(_ + _)
  
  val graph: RunnableGraph[Future[Int]] = source.via(flow).toMat(sink)(Keep.right)
  
  val result = graph.run()
  
  result.onComplete(println)
}