package akka.actors

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object Ex10_AddFuture extends App {
  val future = Future(1+2).mapTo[Int]
  val sum = Await.result(future, 10 seconds)
  println(s"Future Result $sum")
}