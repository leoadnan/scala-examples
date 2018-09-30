package akka.actors

import akka.actor.ActorSystem
import java.io.File

object Ex01_HelloActor extends App {
  val actorSystem = ActorSystem("HelloAkka")
  println(actorSystem)
  actorSystem.terminate()
}