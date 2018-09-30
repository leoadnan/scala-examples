package akka.actors

import akka.actor.Actor
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props

object Ex12_FutureInsideActor extends App {
  class FutureActor extends Actor {
    import context.dispatcher
    def receive = {
      case (a: Int, b: Int) =>
        val f = Future(a + b).mapTo[Int]
        val sum = (Await.result(f, 5 seconds))
        println(s"sum is $sum")
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val actor = actorSystem.actorOf(Props[FutureActor])
  actor ! (10,20)
}