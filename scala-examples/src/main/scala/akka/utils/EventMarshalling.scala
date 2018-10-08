package akka.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import spray.json._

trait EventMarshalling extends DefaultJsonProtocol {
  implicit val dateTimeFormat = new JsonFormat[ZonedDateTime] {
    def write(dateTime: ZonedDateTime) = JsString(dateTime.format(DateTimeFormatter.ISO_INSTANT))
    def read(value: JsValue) = value match {
      case JsString(str) =>
        try {
          ZonedDateTime.parse(str)
        } catch {
          case e: DateTimeParseException =>
            val msg = s"Could not deserialize $str to ZonedDateTime"
            deserializationError(msg)
        }
      case js =>
        val msg = s"Could not deserialize $js to ZonedDateTime."
        deserializationError(msg)
    }
  }

  implicit val eventFormat = jsonFormat7(Event)
  implicit val logIdFormat = jsonFormat2(LogReceipt)
  implicit val errorFormat = jsonFormat2(ParseError)
}