package akka.utils

import java.nio.file.Path
import java.time.ZonedDateTime
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import akka.util.ByteString
import akka.stream.IOResult
import akka.stream.scaladsl.{ FileIO, Framing, Source, Flow}
import spray.json._

/**
 * Contains methods for processing logs and events.
 */
object LogStreamProcessor extends EventMarshalling {
  /**
   * Rolls up events that match predicate.
   */
  def rollup[T](
    source: Source[Event, T],
    predicate: Event => Boolean,
    nrEvents: Int,
    duration: FiniteDuration): Source[Seq[Event], T] =
    source.filter(predicate).groupedWithin(nrEvents, duration)

  def groupByHost[T](source: Source[Event, T]) = { //: SubFlow[Event, T, Source[Event, T]#Repr, RunnableGraph[T]] = {
    // how does the max work?
    // how to work with graphs without the graph DSL? (known, unknown hosts maybe)
    source.groupBy(10, e => (e.host, e.service))
  }

  /**
   * parses text log line into an Event
   */
  def parseLineEx(line: String): Option[Event] = {
    if (!line.isEmpty) {
      line.split("\\|") match {
        case Array(host, service, state, time, desc, tag, metric) =>
          val t = tag.trim
          val m = metric.trim
          Some(Event(
            host.trim,
            service.trim,
            state.trim,
            ZonedDateTime.parse(time.trim),
            desc.trim,
            if (t.nonEmpty) Some(t) else None,
            if (m.nonEmpty) Some(m.toDouble) else None))
        case Array(host, service, state, time, desc) =>
          Some(Event(
            host.trim,
            service.trim,
            state.trim,
            ZonedDateTime.parse(time.trim),
            desc.trim))
        case x =>
          throw new LogParseException(s"Failed on line: $line")
      }
    } else None
  }

  def logLine(event: Event) = {
    s"""${event.host} | ${event.service} | ${event.state} | ${event.time.toString} | ${event.description} ${if (event.tag.nonEmpty) "|" + event.tag.get else "|"} ${if (event.metric.nonEmpty) "|" + event.metric.get else "|"}\n"""
  }

  case class LogParseException(msg: String) extends Exception(msg)
}