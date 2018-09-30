package akka.actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.Terminated
import akka.actor.ActorSystem

object Ex09_DeathWatchApp extends App {
  case object Service
  case object Kill
  class ServiceActor extends Actor {
    def receive = {
      case Service => println("I provide a special service")
    }
  }
  
  class DeathWatchActor extends Actor {
    val child = context.actorOf(Props[ServiceActor], "serviceActor")
    context.watch(child)
    def receive = {
      case Service => child ! Service
      case Kill => context.stop(child)
      case Terminated(child) => println("The service actor has terminated and no longer available")
    }
  }
  val actorSystem = ActorSystem("Supervision")
  val actor = actorSystem.actorOf(Props[DeathWatchActor])
  actor ! Service
  actor ! Service
  Thread.sleep(1000)
  actor ! Kill
  actor ! Service
}