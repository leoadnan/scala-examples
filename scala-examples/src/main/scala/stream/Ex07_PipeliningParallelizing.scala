package stream

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import scala.util.Random
import akka.stream.scaladsl.GraphDSL
import akka.stream.scaladsl.Balance
import akka.stream.scaladsl.Merge
import akka.stream.FlowShape
import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink

trait Ex07_PipeliningParallelizing extends App {
  implicit val system = ActorSystem("PipeliningParallelizing")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  case class Wash(id: Int)
  case class Dry(id: Int)
  case class Done(id: Int)

  val tasks = (1 to 5).map(Wash)

  def washStage = Flow[Wash].map(wash => {
    val sleepTime = Random.nextInt(3) * 1000
    println(s"Washing ${wash.id}. It will take $sleepTime milliseconds.")
    Thread.sleep(sleepTime)
    Dry(wash.id)
  })

  def dryStage = Flow[Dry].map(dry => {
    val sleepTime = Random.nextInt(3) * 1000
    println(s"Drying ${dry.id}. It will take $sleepTime milliseconds.")
    Thread.sleep(sleepTime)
    Done(dry.id)
  })

  val parallelStage = Flow.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    //A Balance is a stage with one input port and n output ports 
    //that distributes elements in the output ports as soon as there is demand.
    val dispatchLaundry = builder.add(Balance[Wash](3))
    
    //A Merge is a stage with n input ports and one output port. 
    //This stage is responsible for merging different branches into a unique channel.
    val mergeLaundry = builder.add(Merge[Done](3))

    dispatchLaundry.out(0) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(0)
    dispatchLaundry.out(1) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(1)
    dispatchLaundry.out(2) ~> washStage.async ~> dryStage.async ~> mergeLaundry.in(2)

    FlowShape(dispatchLaundry.in, mergeLaundry.out)
  })

  def runGraph(flow: Flow[Wash, Done, NotUsed]) = Source(tasks).via(flow).to(Sink.foreach(println)).run()

}