package stream

import stream.SinkActor.{ InitSinkActor, AckSinkActor, CompletedSinkActor }

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props

import akka.pattern.ask
import akka.util.Timeout

import akka.stream.ActorMaterializer
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._

import scala.concurrent.duration._

class StringCleanerActor extends Actor {
  def receive = {
    case s: String =>
      println(s"Cleaning [$s] in StringCleaner")
      sender ! s.replaceAll("""[\p{Punct}&&[^.]]""", "").replaceAll(System.lineSeparator(), "")
  }
}

class SinkActor extends Actor {
  def receive = {
    case InitSinkActor =>
      println("SinkActor initialized")
      sender ! AckSinkActor
    case something =>
      println(s"Received [$something] in SinkActor")
      sender ! AckSinkActor
  }
}

object SinkActor {
  case object CompletedSinkActor
  case object AckSinkActor
  case object InitSinkActor
}

class SourceActor(sourceQueue: SourceQueueWithComplete[String]) extends Actor {
  import SourceActor._
  import context.dispatcher

  override def preStart() = {
    context.system.scheduler.schedule(0 seconds, 5 seconds, self, Tick)
  }
  def receive = {
    case Tick =>
      println(s"Offering element from SourceActor")
      sourceQueue.offer("Integrating!!### Akka$$$ Actors? with}{ Akka** Streams")
  }
}

object SourceActor {
  case object Tick
  def props(sourceQueue: SourceQueueWithComplete[String]) = Props(new SourceActor(sourceQueue))
}


object Ex09_WithActorIntegration extends App {

  implicit val actorSystem = ActorSystem("IntegratingWithActors")
  implicit val actorMaterializer = ActorMaterializer()

  implicit val askTimeout = Timeout(5 seconds)
  
  val stringCleaner = actorSystem.actorOf(Props[StringCleanerActor])
  val sinkActor = actorSystem.actorOf(Props[SinkActor])

  val source = Source.queue[String](100, OverflowStrategy.backpressure)
  val sink = Sink.actorRefWithAck[String](sinkActor, InitSinkActor, AckSinkActor, CompletedSinkActor)
  
  val queue = source
    .mapAsync(parallelism = 5)(elem => (stringCleaner ? elem).mapTo[String])
    .to(sink)
    .run()

  actorSystem.actorOf(SourceActor.props(queue))
}