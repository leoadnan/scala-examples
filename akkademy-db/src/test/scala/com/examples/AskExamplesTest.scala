package com.examples

import org.scalatest.Matchers
import org.scalatest.FunSpecLike
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import com.examples.Ex02_PingPongMessage._
import scala.concurrent.Await

class AskExamplesTest extends FunSpecLike with Matchers {
  val system = ActorSystem()
  implicit val timeout = Timeout(5 seconds)
  val pongActor = system.actorOf(Props(classOf[PongActor]))
  describe("Pong actor") {
    it("should respond with pong") {
      //Now, we ask the actor for a response to a message:
      //We need to import akka.pattern.ask for this to work.
      val future = pongActor ? "Ping" //uses the implicit timeout
      val result = Await.result(future.mapTo[String], 1 second)
      assert(result == "Pong")
    }

    it("should fail on unknown message") {
      val future = pongActor ? "unknown"
      intercept[Exception] {
        Await.result(future.mapTo[String], 1 second)
      }
    }
  }

  describe("Future Example") {
    import scala.concurrent.ExecutionContext.Implicits.global
    it("should print to console") {
      (pongActor ? "Ping").onSuccess({
        case x: String => println("replied with: " + x)
      })
      Thread.sleep(100)
    }
  }
}