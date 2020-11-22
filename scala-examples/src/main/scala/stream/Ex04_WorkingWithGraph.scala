package stream

import akka.actor.ActorSystem

import akka.stream.FlowShape
import akka.stream.Inlet
import akka.stream.Outlet
import akka.stream.stage.GraphStageLogic
import akka.stream.Attributes
import akka.stream.stage.GraphStage
import akka.stream.stage.InHandler
import akka.stream.stage.OutHandler
import akka.stream.ActorMaterializer
import akka.stream.ClosedShape
import akka.stream.scaladsl._

import scala.util.Random

import scala.concurrent.duration._

object Ex04_WorkingWithGraph extends App {
  implicit val actorSystem = ActorSystem("WorkingWithGraphs")
  implicit val actorMaterializer = ActorMaterializer()

  trait MobileMsg {
    def id = Random.nextInt(1000)
    def toGenMsg(origin: String) = GenericMsg(id, origin)
  }
  class AndroidMsg extends MobileMsg
  class IosMsg extends MobileMsg
  case class GenericMsg(id: Int, origin: String)

  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    //Sources
    val androidNotificationSource = Source.tick(2 seconds, 500 millis, new AndroidMsg)
    val iosNotificationSource = Source.tick(700 millis, 600 millis, new IosMsg)

    //Flow
    val groupAndroid = Flow[AndroidMsg].map(_.toGenMsg("ANDROID")).groupedWithin(5, 5 seconds).async
    val groupIos = Flow[IosMsg].map(_.toGenMsg("IOS")).groupedWithin(5, 5 seconds).async
    def counter = Flow[Seq[GenericMsg]].via(new StatefulCounterFlow())
    def mapper = Flow[Seq[GenericMsg]].mapConcat(_.toList)

    //Junctions
    val aBroadcast = builder.add(Broadcast[Seq[GenericMsg]](2))
    val iBroadcast = builder.add(Broadcast[Seq[GenericMsg]](2))
    val balancer = builder.add(Balance[Seq[GenericMsg]](2))
    val notitificationMerge = builder.add(Merge[Seq[GenericMsg]](2))
    val genericNotitificationMerge = builder.add(Merge[GenericMsg](2))

    def counterSink(s: String) = Sink.foreach[Int](x => println(s"$s: [$x]"))

    //Graph
    androidNotificationSource ~> groupAndroid ~> aBroadcast ~> counter ~> counterSink("andriod")
                                                 aBroadcast ~> notitificationMerge
                                                 iBroadcast ~> notitificationMerge
    iosNotificationSource ~> groupIos         ~> iBroadcast ~> counter ~> counterSink("Ios")
    
    notitificationMerge ~> balancer ~> mapper.async ~> genericNotitificationMerge
                           balancer ~> mapper.async ~> genericNotitificationMerge

    genericNotitificationMerge ~> Sink.foreach(println)
    
    ClosedShape
  })
  
  graph.run()
}

import Ex04_WorkingWithGraph.GenericMsg

class StatefulCounterFlow extends GraphStage[FlowShape[Seq[GenericMsg], Int]] {
  val in: Inlet[Seq[GenericMsg]] = Inlet("IncomingGenericMsg")
  val out: Outlet[Int] = Outlet("OutgoingCount")

  override val shape: FlowShape[Seq[GenericMsg], Int] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = {
    new GraphStageLogic(shape) {
      var count = 0

      setHandler(in, new InHandler {
        override def onPush() = {
          val elem = grab(in)
          count += elem.size
          push(out, count)
        }
      })

      setHandler(out, new OutHandler {
        override def onPull() = {
          pull(in)
        }
      })

    } // end GraphStageLogic class
  } // end createLogin method
}