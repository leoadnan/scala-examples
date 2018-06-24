package actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props

object ActorHelloWorld extends App {

  case class Name(s: String)

  class GreetingActor extends Actor {
    def receive = {
      case Name(n) => println("Hello " + n)
      case s: String => println("!Hello " + s)
    }
  }

  val actorSystem = ActorSystem("greetings")
  val a = actorSystem.actorOf(Props[GreetingActor], name = "greeting-actor")

  a ! Name("Adnan Ahmed")

  a ! "Adnan Ahmed"
  Thread.sleep(500)

}