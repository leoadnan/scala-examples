package actors

import java.io.File

import scala.io.Source

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.ActorSystem

object WordCount {

  case class StartCounting(docRoot: String, noOfActors: Int)
  case class FileToCount(fileName: String)
  case class WordCount(fileName: String, count: Int)

  class WordCountWorker extends Actor {
    private def countWord(fileName: String) = {
      val dataFile = new File(fileName)
      Source.fromFile(dataFile).getLines.foldRight(0)(_.split(" ").size + _)
    }

    def receive = {
      case FileToCount(fileName: String) => {
        val count = countWord(fileName)
        sender ! WordCount(fileName, count)
      }
    }

    override def postStop(): Unit = {
      println(s"Worker actor is stopped: ${self}")
    }
  }

  class WordCountMaster extends Actor {
    var fileNames: Seq[String] = Nil
    var sortedCount: Seq[(String, Int)] = Nil

    private def scanFiles(docRoot: String) = {
      new File(docRoot).listFiles.map(_ + "")
    }
    private def createWorkers(noOfActors: Int) = {
      for (i <- 0 until noOfActors) yield context.actorOf(Props[WordCountWorker], name = s"worker-${i}")
    }
    private def beginCount(fileNames: Seq[String], workers: Seq[ActorRef]) = {
      fileNames.zipWithIndex.foreach { e =>
        workers(e._2 % workers.size) ! FileToCount(e._1)
      }
    }

    def receive = {
      case StartCounting(docRoot: String, noOfActors: Int) => {
        val workers = createWorkers(noOfActors)
        fileNames = scanFiles(docRoot)
        beginCount(fileNames, workers)
      }
      case WordCount(fileName: String, count: Int) => {
        sortedCount = (fileName, count) +: sortedCount
        sortedCount = sortedCount.sortWith(_._2 < _._2)
        if (sortedCount.size == fileNames.size) {
          println("final result " + sortedCount)
          context.system.terminate()
        }
      }
    }

    override def postStop(): Unit = {
      println(s"Master actor is stopped: ${self}")
    }
  }
  def main(args: Array[String]) {
    val system = ActorSystem("word-count-system")
    val m = system.actorOf(Props[WordCountMaster], name = "master")
    m ! StartCounting("/Users/adnan/Personal-GitHub/scala-examples/scala-examples/src/main/scala/actors/", 2)
  }
}