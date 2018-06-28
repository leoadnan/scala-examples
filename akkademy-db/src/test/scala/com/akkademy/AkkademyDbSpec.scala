package com.akkademy

import org.scalatest.BeforeAndAfterEach
import org.scalatest.Matchers
import org.scalatest.FunSpecLike
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import com.akkademy.messages.SetRequest

class AkkademyDbSpec extends FunSpecLike with Matchers with BeforeAndAfterEach {
  implicit val system = ActorSystem()
  describe("akkademyDb") {
    describe("given SetRequest") {
      it("should place key/value into map") {
        val actorRef = TestActorRef(new AkkademyDb)
        actorRef ! SetRequest("key", "value")
        val akkademyDb = actorRef.underlyingActor
        akkademyDb.map.get("key") should equal(Some("value"))
      }
    }
  }
}