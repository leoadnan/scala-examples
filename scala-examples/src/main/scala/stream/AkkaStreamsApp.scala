package stream

import akka.actor.{ ActorSystem, Terminated }
import akka.event.{ Logging, LoggingAdapter }
import akka.stream.{ ActorMaterializer, Materializer }

import scala.concurrent.{ ExecutionContext, Future }

trait AkkaStreamsApp extends App {
  implicit val system = ActorSystem()
  implicit val mat = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  def akkaStreamsExample: Future[_]

  def runExample: Future[Terminated] = (for {
    _ <- akkaStreamsExample
    term <- system.terminate()
  } yield term).recoverWith {
    case cause: Throwable =>
      println(cause, "Exception while executing example")
      system.terminate()
  }

  sys.addShutdownHook {
    system.terminate()
  }

}