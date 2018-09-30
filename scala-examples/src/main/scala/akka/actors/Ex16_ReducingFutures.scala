package akka.actors
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure
import akka.util.Timeout

object Ex16_ReducingFutures extends App {
  val timeout = Timeout(10 seconds)
  val listOfFutures = (1 to 10).map(Future(_))
  val finalFuture = Future.reduce(listOfFutures)(_+_)
  println(s"sum of numbers from 1 to 10 is ${Await.result(finalFuture, 10 seconds)}")
}