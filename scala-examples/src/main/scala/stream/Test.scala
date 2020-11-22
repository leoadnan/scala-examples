package stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.duration._
import akka.stream.scaladsl.Flow
import akka.stream.ActorMaterializerSettings

object Test extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val mater = ActorMaterializer( ActorMaterializerSettings(system)
     .withInputBuffer(initialSize = 1, maxSize = 32))

  case class Tick()

  val fastSource = Source.tick(1 second, 1 second, Tick())
  val slowSource = Source.tick(3 second, 3 second, Tick())

  val asyncZip = Flow[Int].zip(slowSource)

  fastSource.conflateWithSeed(seed = (_) => 1)((count, _) => count + 1)
    .log("Before AsyncZip")
    .via(asyncZip)
    .take(10)
    .log("After AsyncZip")
    .runForeach { case (i, t) => println(s"Received: $i") }
}