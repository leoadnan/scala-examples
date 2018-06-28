package actors

import akka.actor.Actor
import akka.actor.Status
import akka.actor.ActorSystem
import akka.actor.Props

object Ex02_PingPongMessage {

  object PongActor {
    def props(msg: String) : Props = {
      Props(classOf[PongActor], msg)
    }
  }
  class PongActor(msg: String) extends Actor {
    override def receive = {
      case "Ping" => sender ! msg
      case _ => sender ! Status.Failure(new Exception("unknown message"))
    }
  }

  /**
   * Reply with akka.actor.Status.Failure: 
   * The last piece to note is the reply with akka.actor.Status.Failure
   * in the case of an unknown message.
   * The actor will never reply with a failure
   * —even if the actor itself fails—
   * so you will always need to ensure you send back a failure 
   * if you want to notify anyone who is asking that there was an issue. 
   * Sending back a failure will cause a placeholder future to be marked as a failure.
   */
  
  def main(args: Array[String]) = {
    val actorSystem = ActorSystem("PingPong")
    val pingPongActorRef = actorSystem.actorOf(PongActor.props("Pong"), name="PingPongActor")
    println(pingPongActorRef.path)
  }
}