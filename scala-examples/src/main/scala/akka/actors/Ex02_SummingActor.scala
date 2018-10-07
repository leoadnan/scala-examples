package akka.actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.concurrent.duration._
import java.util.Date

object Ex02_SummingActor extends App {
  class SummingActor() extends Actor {
    private var sum = 0
    override def receive: Receive = {
      case x: Int => {
        sum = sum + x
        println(s"my state as sum is $sum")
      }
      case _ => {
        println("I don't know what you talking about")
      }
    }
  }

  val actorSystem: ActorSystem = ActorSystem("actorSystem")
  val actor = actorSystem.actorOf(Props(classOf[SummingActor]), name = "summing-actor")
  actor ! 5
  actorSystem.terminate()
}