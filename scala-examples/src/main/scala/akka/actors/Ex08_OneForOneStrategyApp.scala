package akka.actors

import akka.actor.Actor
import akka.actor.OneForOneStrategy
import akka.actor.SupervisorStrategy._
import akka.actor.Props
import akka.actor.ActorSystem

object Ex08_OneForOneStrategyApp extends App {
  class Printer extends Actor {
    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      println("Printer: I am restarting because of ArithmeticException")
    }
    def receive = {
      case msg: String => println(s"Printer $msg")
      case msg: Int => 1 / 0
    }
  }
  class IntAdder extends Actor {
    var x = 0
    def receive = {
      case msg: Int =>
        x = x + msg
        println(s"IntAdder: sum is $x")
      case msg: String => throw new IllegalArgumentException
    }
    override def postStop = {
      println("IntAdder: I am getting stopped because I got a string message")
    }
  }
  class SupervisorStrategy extends Actor {
    import scala.concurrent.duration._
    override val supervisorStrategy = OneForOneStrategy(
      maxNrOfRetries = 10,
      withinTimeRange = 1 minute) {
      case _: ArithmeticException => Restart
      case _: NullPointerException => Restart
      case _: IllegalArgumentException => Stop
      case _: Exception => Escalate
    }
    val printer = context.actorOf(Props[Printer])
    val intAdder = context.actorOf(Props[IntAdder])
    def receive = {
      case "Start" =>
        printer ! "Hello printer"
        printer ! 10
        intAdder ! 10
        intAdder ! 10
        intAdder ! "Hello int adder"
    }
  }

  val actorSystem = ActorSystem("Supervision")
  actorSystem.actorOf(Props[SupervisorStrategy]) ! "Start"
}