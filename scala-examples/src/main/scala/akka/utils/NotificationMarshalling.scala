package akka.utils

import spray.json._

trait NotificationMarshalling extends EventMarshalling /*with DefaultJsonProtocol*/ {
  implicit val summary = jsonFormat1(Summary)
}