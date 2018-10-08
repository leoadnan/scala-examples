package akka.streams

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import scala.concurrent.Future
import akka.stream.scaladsl.Keep
import scala.concurrent.ExecutionContext.Implicits.global

object Ex01_HelloWorld extends App {
  implicit val actorSystem = ActorSystem("HelloSystem")
  implicit val materializer = ActorMaterializer()

  val numberSource = Source(1 to 5)
  val numberSink = Sink.foreach(println)

  //Connecting source to sink. it creates a runnable graph
  val numberGraph = numberSource.to(numberSink)
  numberGraph.run()

  Thread.sleep(1000)
  println("=" * 10)

  //using flow to process the numbers
  val numberFlow = Flow[Int].map(_ * 2)
  val numberGraphWithFlow = numberSource.via(numberFlow).to(numberSink)
  numberGraphWithFlow.run()

  Thread.sleep(1000)
  println("=" * 10)

  val helloWorldSource = Source.single("Akka Streams HelloWorld")
  val helloWorldSink = Sink.foreach(print)
  val helloWorldFlow = Flow[String].map(str => str.toUpperCase)
  val helloWorldGraph = helloWorldSource.via(helloWorldFlow).to(helloWorldSink)
  helloWorldGraph.run
  Thread.sleep(1000)
  println()

  val foldSink: Sink[Int, Future[Int]] = Sink.fold(0)(_ + _)
  val foldGraph = numberSource.filter(_ % 2 == 0).toMat(foldSink)(Keep.right)
  foldGraph.run() onSuccess {
    case x => println(x)
  }

  val multiplier = Flow[Int].map(_ * 2)
  multiplier.runWith(numberSource, foldSink)._2.onSuccess {
    case x => println(x)
  }
  
  actorSystem.terminate
}