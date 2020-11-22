package akka.actors

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorSystem

object Ex05_ParentChildActors extends App {
  case object CreateChild
  case class Greet(msg: String)

  class ChildActor extends Actor {
    def receive = {
      case Greet(msg) => println(s"My parent [${self.path.parent}] greeted me [${self.path}] $msg")
    }
  }
  
  class ParentActor extends Actor {
    def receive = {
      case CreateChild => {
        val child = context.actorOf(Props[ChildActor], name="child")
        child ! Greet("Hello Child")
      }
    }
  }
  
  val actorSystem = ActorSystem("Supervision")
  val parent = actorSystem.actorOf(Props[ParentActor], name="parent")
  parent ! CreateChild
  
  actorSystem.terminate()
}