package akka.actors

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.util.Success
import scala.util.Failure

object Ex14_Callback extends App {
  val future = Future (1+2).mapTo[Int]
  
  future onComplete {
    case Success(result) => println(s"result is $result")
    case Failure(fail) => fail.printStackTrace()
  }
  
  println("Executed before callback")
}