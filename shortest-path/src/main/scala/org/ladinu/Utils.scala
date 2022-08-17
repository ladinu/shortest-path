package org.ladinu

import cats.effect.IO
import io.circe.parser.parse
import org.http4s.Uri
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.ladinu.Models._

import java.nio.charset.Charset
import java.nio.file.{Files, Path}
import scala.util.Try

trait Utils extends Serde {

  def toDOT(input: TrafficMeasurements): String = {
    val body = toGraph(input)
      .map { node =>
        (node.name :: node.edges.map(_.to)).mkString("->")
      }
      .mkString(";\n")

    s"""
        digraph traffic {
          $body
        }
     """.stripMargin
  }
  def toGraph(input: TrafficMeasurements): List[Node] = {

    val graphMap = input
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
      .map(node => node.name -> node)
      .toMap

    // Add all nodes that don't have an edge to other nodes
    graphMap
      .foldLeft(graphMap) { case (graphMap, (_, node)) =>
        node
          .edges
          .map(_.to)
          .foldLeft(graphMap) { (acc, a) =>
            if (acc.contains(a)) {
              acc
            } else {
              acc + (a -> Node(a, Nil))
            }
          }
      }
      .values
      .toList
  }

  // Fetch & parse the measurement data
  def getTrafficData(uri: Uri): IO[TrafficMeasurements] = {
    val result = if (uri.scheme.map(_.value.toLowerCase).contains("file")) {
      // Fetch from disk
      IO.blocking {
        Files
          .readString(Path.of(uri.renderString.drop("file:".length)), Charset.forName("utf-8"))
      }.flatMap(data =>
        IO.fromEither(parse(data))
          .flatMap(json => IO.fromEither(json.as[TrafficMeasurements]))
      )
    } else {
      // Fetch from network
      BlazeClientBuilder[IO]
        .resource
        .use { client: Client[IO] =>
          client
            .expect[TrafficMeasurements](uri)
        }
    }

    result
      .onError(err => IO.println(s"Unable to fetch traffic measurement data: ${err.getMessage}").*>(IO.raiseError(err)))
  }
}
