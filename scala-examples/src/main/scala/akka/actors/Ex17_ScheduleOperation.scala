package akka.actors
import scala.concurrent.duration._
import akka.actor.ActorSystem

object Ex17_ScheduleOperation extends App {
  val system = ActorSystem("actorSystem")
  import system.dispatcher
  system.scheduler.scheduleOnce(10 seconds) {
    println(s"sum of (1+2) is ${1+2}")
  }
  system.scheduler.schedule(11 seconds, 2 seconds) {
    println(s"Hello, sorry for disturbing you every 2 seconds")
  }
}