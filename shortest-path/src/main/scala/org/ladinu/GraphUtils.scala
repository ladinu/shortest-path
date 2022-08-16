package org.ladinu

import org.ladinu.Models._

import scala.util.Try

trait GraphUtils {

  def toGraph(input: TrafficMeasurements): List[Node] =
    input
      .measurements
      .flatMap(_.measurements)
      .groupBy(m => (m.startStreet, m.startAvenue, m.endStreet, m.endAvenue))
      .flatMap { case ((startStreet, startAve, endStreet, endAve), measurements) =>
        // Calculate the average
        Try {
          val avgTransitTime = measurements.map(_.transitTime).sum / measurements.length
          Measurement(
            startStreet = startStreet,
            startAvenue = startAve,
            transitTime = avgTransitTime,
            endStreet = endStreet,
            endAvenue = endAve
          )
        }.toOption
      }
      .groupBy(a => a.startStreet -> a.startAvenue)
      .map { case ((street, ave), measurements) =>
        Node(
          s"$ave$street",
          edges = measurements.map(m => Edge(s"${m.endAvenue}${m.endStreet}", m.transitTime)).toList
        )
      }
      .toList
}
