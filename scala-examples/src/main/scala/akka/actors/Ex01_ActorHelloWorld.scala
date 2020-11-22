package akka.actors

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorPath
import akka.actor.ActorRef

object Ex01_ActorHelloWorld extends App {
  case class Name(n: String)
  
  class GreetingActor extends Actor {
    override def receive() = {
      case Name(n) => println("Hello " + n)
      case s: String => println("!Hello " + s)
    }
  }
  
  val actorSystem = ActorSystem("HelloWorldActorSystem")
  val actor = actorSystem.actorOf(Props[GreetingActor], name = "greeting-actor")
  
  println(actorSystem)
  println(actor)
  actor ! new Name("Adnan Ahmed")
  actor ! "Adnan Ahmed"
  
  actorSystem.terminate()
}