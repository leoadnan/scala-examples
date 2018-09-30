package akka.actors

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await

object Ex13_ForComprehension extends App {
  val futureA = Future(20 + 20)
  val futureB = Future(30 + 30)
  val finalFuture: Future[Int] = for {
    a <- futureA
    b <- futureB
  } yield a + b
  println("Future result is " + Await.ready(finalFuture, 1 seconds))
}