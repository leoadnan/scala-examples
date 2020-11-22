package akka.actors

import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.pattern.ask
import scala.concurrent.Await

object Ex11_FutureWithActor extends App {
  class ComputationActor extends Actor {
    def receive = {
      case (a:Int, b:Int) => sender ! (a+b)
    }
  }
  implicit val timeout = Timeout(10 seconds)
  val actorSystem = ActorSystem("actorSystem")
  val actor = actorSystem.actorOf(Props[ComputationActor])
  val future = (actor ? (2,3)).mapTo[Int]
  val sum = Await.result(future, 10 seconds)
  println(s"Future Result $sum")
}