package akka.actors

import akka.actor.ActorRef
import akka.actor.Actor
import scala.util.Random._
import akka.actor.ActorSystem
import akka.actor.Props

object Ex04_ActorsCommunicationApp extends App {
  object Messages {
    case class Start(actorRef: ActorRef)
    case object GiveMeRandomNumber
    case class Done(result: Int)
  }

  import Messages._
  
  class RandomNumberGeneratorActor extends Actor {
    override def receive: Receive = {
      case GiveMeRandomNumber => {
        println(s"Received a message to generate the random number")
        val result = nextInt
        sender ! Done(result)
      }
    }
  }

  class QueryActor extends Actor {
    override def receive: Receive = {
      case Start(actorRef) => {
        println("Send me the next random number")
        actorRef ! GiveMeRandomNumber
      }
      case Done(result) => {
        println(s"Received a random number $result")
      }
    }
  }

  val actorSystem = ActorSystem("actorSystem")
  val randomGeneratorActor = actorSystem.actorOf(Props[RandomNumberGeneratorActor])
  val queryActor = actorSystem.actorOf(Props[QueryActor])
  queryActor ! Start(randomGeneratorActor)
  actorSystem.terminate()
}