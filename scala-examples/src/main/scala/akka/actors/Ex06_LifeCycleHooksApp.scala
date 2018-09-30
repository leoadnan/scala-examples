package akka.actors

import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.actor.Actor
import akka.pattern.ask
import scala.concurrent.duration._
import akka.util.Timeout
import scala.concurrent.Await

object Ex06_LifeCycleHooksApp extends App {
  case object Error
  case class StopActor(actorRef: ActorRef)

  class LifeCycleActor extends Actor {
    var sum = 1

    override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
      println(s"sum in preRestart is $sum")
    }

    override def preStart(): Unit = println(s"sum in preStart is $sum")

    override def receive = {
      case Error => throw new ArithmeticException("abc")
      case _ => println("default msg")
    }

    override def postStop(): Unit = {
      println(s"sum in postStop is ${sum * 3}")
    }

    override def postRestart(reason: Throwable): Unit = {
      sum = sum * 2
      println(s"sum in postRestart is $sum")
    }
  }

  class Supervisor extends Actor {
    override val supervisorStrategy = OneForOneStrategy(
      maxNrOfRetries = 10,
      withinTimeRange = 1 minute) {
      case _: ArithmeticException => Restart
      case t => super.supervisorStrategy.decider.applyOrElse(t, (_: Any) => Escalate)
    }

    def receive = {
      case (props: Props, name: String) =>
        sender ! context.actorOf(props, name)
      case StopActor(actorRef) => context.stop(actorRef)
    }
  }

  implicit val timeout = Timeout(2 seconds)
  val actorySystem = ActorSystem("Supervision")
  val supervisor = actorySystem.actorOf(Props[Supervisor], "supervisor")
  val childFuture = supervisor ? (Props(new LifeCycleActor), "LifeCycleActor")
  val child = Await.result(childFuture.mapTo[ActorRef], 2 seconds)
  child ! Error
  Thread.sleep(1000)
  supervisor ! StopActor (child)
}