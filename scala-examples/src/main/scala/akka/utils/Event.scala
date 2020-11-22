package akka.utils

import java.time.ZonedDateTime

case class Event(
  host: String,
  service: String,
  state: String,
  time: ZonedDateTime,
  description: String,
  tag: Option[String] = None,
  metric: Option[Double] = None)