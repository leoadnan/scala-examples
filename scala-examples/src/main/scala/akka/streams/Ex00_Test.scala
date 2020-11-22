package akka.streams

import scala.concurrent.Future

import akka.Done
import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.RunnableGraph
import akka.stream.ThrottleMode

object Ex00_Test extends App {
  implicit val system = ActorSystem("MyActorSystem")
  implicit val ex = system.dispatcher
  implicit val materializer = ActorMaterializer()

  //Sources
  val sourceFromRange: Source[Int, NotUsed] = Source(1 to 10)
  val sourceFromIterable: Source[Int, NotUsed] = Source(List(1, 2, 3))
  val sourceWithSingleElement: Source[String, NotUsed] = Source.single("just one")
  val sourceEmittingTheSameElement: Source[String, NotUsed] = Source.repeat("again and agian")

  //Sinks
  val sinkPrintingOutElements: Sink[String, Future[Done]] = Sink.foreach[String](println(_))
  val sinkCalculatingASumOfElements: Sink[Int, Future[Int]] = Sink.fold[Int, Int](0)(_ + _)
  val sinkReturningTheFirstElement: Sink[Nothing, Future[Nothing]] = Sink.head
  val sinkNoop: Sink[Any, Future[Done]] = Sink.ignore

  //Flows
  val flowDoublingElements: Flow[Int, Int, NotUsed] = Flow[Int].map(_ * 2)
  val flowFilteringOutOddElements: Flow[Int, Int, NotUsed] = Flow[Int].filter(_ % 2 == 0)
  val flowBatchingElements: Flow[Int, Seq[Int], NotUsed] = Flow[Int].grouped(5)
  val flowBufferingElements = Flow[String].buffer(1000, OverflowStrategy.backpressure) // back-pressures the source if the buffer is full

  // Generating graph/blueprint
  val streamCalculatingSumOfElements: RunnableGraph[Future[Int]] = sourceFromIterable.toMat(sinkCalculatingASumOfElements)(Keep.right)
  val streamCalculatingSumOfDoubledElements: RunnableGraph[Future[Int]] = sourceFromIterable.via(flowDoublingElements).toMat(sinkCalculatingASumOfElements)(Keep.right)

  // running/materialisation of stream
  //  val sumOfElements: Future[Int] = streamCalculatingSumOfElements.run()
  //  sumOfElements.foreach(println) // we expect to see 6
  //  val sumOfDoubledElements: Future[Int] = streamCalculatingSumOfDoubledElements.run()
  //  sumOfDoubledElements.foreach(println) // we expect to see 12

  // runs the stream by attaching specified sink
  //  sourceFromIterable.via(flowDoublingElements).runWith(sinkCalculatingASumOfElements).foreach(println)

  // runs the stream by attaching sink that folds over elements on a stream
  //  Source(List(1, 2, 3)).map(_ * 2).runFold(0)(_ + _).foreach(println)
  
  sourceFromRange.via(flowBatchingElements).runForeach(s=>println(s))

 
}