package akka.actors

import akka.actor.Actor
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.pattern.ask
import scala.concurrent.Await

object Ex03_FibonacciActorApp extends App {
  class FibonacciActor extends Actor {
    override def receive: Receive = {
      case num: Int => {
        val result = fib(num)
        sender ! result
      }
    }
    
    private def fib(n: Int): Int = n match {
      case 0 | 1 => n
      case _ => fib(n - 1) + fib(n - 2)
    }
  }
  
  implicit val timeout: Timeout = Timeout(10 seconds)
  val actorSystem = ActorSystem("actorSystem")
  val actor: ActorRef = actorSystem.actorOf(Props[FibonacciActor], name="fibonacci-actor")
  //Asking for result from actor
  val future = (actor ? 5).mapTo[Int]
  val result = Await.result(future, 10 seconds)
  println(result)
  actorSystem.terminate()
}