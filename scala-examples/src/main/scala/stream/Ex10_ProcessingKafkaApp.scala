package stream

import akka.actor.ActorSystem

import akka.stream.ActorMaterializer

import akka.kafka.Subscriptions
import akka.kafka.ConsumerSettings

import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.ConsumerRecord

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.ByteArraySerializer

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer

import akka.stream.scaladsl.RunnableGraph
import akka.stream.scaladsl.GraphDSL

import akka.stream.scaladsl.Source
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Flow
import akka.stream.ClosedShape

import scala.concurrent.duration._

object Ex10_ProcessingKafkaApp extends App {
  implicit val actorSystem = ActorSystem("SimpleStream")
  implicit val actorMaterializer = ActorMaterializer()

  val bootstrapServers = "localhost:9092"
  val kafkaTopic = "akka_streams_topic"
  val partition = 0

  val subscription = Subscriptions.assignment(new TopicPartition(kafkaTopic, partition))

  val consumerSettings = ConsumerSettings(actorSystem, new ByteArrayDeserializer, new StringDeserializer)
    .withBootstrapServers(bootstrapServers)
    .withGroupId("akka_streams_group")
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerSettings = ProducerSettings(actorSystem, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(bootstrapServers)

  val runnableGraph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val tickSource = Source.tick(0 seconds, 5 seconds, "Hello from Akka Streams using Kafka!")
    val kafkaSource = Consumer.plainSource(consumerSettings, subscription)
    val kafkaSink = Producer.plainSink(producerSettings)
    val printlnSink = Sink.foreach(println)

    val mapToProducerRecord = Flow[String].map(elem => new ProducerRecord[Array[Byte], String](kafkaTopic, elem))
    val mapFromConsumerRecord = Flow[ConsumerRecord[Array[Byte], String]].map(record => record.value())

    tickSource ~> mapToProducerRecord ~> kafkaSink
    kafkaSource ~> mapFromConsumerRecord ~> printlnSink
    
    ClosedShape
  })
  
  runnableGraph.run()
}