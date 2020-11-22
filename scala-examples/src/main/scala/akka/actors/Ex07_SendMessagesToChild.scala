package akka.actors

import akka.actor.Actor
import scala.collection.mutable.ListBuffer
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

object Ex07_SendMessagesToChild extends App {
  case class DoubleValue(x: Int)
  case object CreateChild
  case object Send
  case class Response(x: Int)

  class DoubleActor extends Actor {
    def receive = {
      case DoubleValue(number) => {
        println(s"${self.path.name} Got the number $number")
        sender ! Response(number * 2)
      }
    }
  }

  class ParentActor extends Actor {
    val random = new scala.util.Random
    var childs = ListBuffer[ActorRef]()
    def receive = {
      case CreateChild => {
        childs ++= List(context.actorOf(Props[DoubleActor]))
      }
      case Send => {
        println(s"Sending messages to child")
        childs.foreach(_ ! DoubleValue(random.nextInt(10)))
      }
      case Response(x) => println(s"Parent: Response from child ${sender.path.name} is $x")
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val parent = actorSystem.actorOf(Props[ParentActor], "parent")
  parent ! CreateChild
  parent ! CreateChild
  parent ! CreateChild
  parent ! Send
  actorSystem.terminate()
}