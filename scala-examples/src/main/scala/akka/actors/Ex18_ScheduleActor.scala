package akka.actors
import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

object Ex18_ScheduleActor extends App {
  
  class RandomIntAdder extends Actor {
    val r = scala.util.Random
    def receive = {
      case "tick" =>
        val randomInta = r.nextInt(10)
        val randomIntb = r.nextInt(10)
        println(s"sum of $randomInta and $randomIntb is ${randomInta + randomIntb}")
    }
  }
  
  val system = ActorSystem("actorSystem")
  import system.dispatcher
  val actor = system.actorOf(Props[RandomIntAdder])
  system.scheduler.scheduleOnce(10 seconds, actor, "tick")
  system.scheduler.schedule(11 seconds, 2 seconds, actor, "tick")
}