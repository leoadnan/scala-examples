package akka.utils

import spray.json._

trait MetricMarshalling extends EventMarshalling /*with DefaultJsonProtocol*/ {
  implicit val metric = jsonFormat5(Metric)
}