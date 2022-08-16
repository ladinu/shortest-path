package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import io.circe.parser._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.client.Client
import org.ladinu.Models._

import java.nio.charset.Charset
import java.nio.file.{Files, Path}
import scala.util.Try

object Main extends IOApp with Serde with ShortestPath {
  override def run(args: List[String]): IO[ExitCode] =
    for {

      command <- CLI.args.parse(args) match {
        case Left(help)   => IO.println(help).*>(IO.raiseError(new Throwable("Unable to parse CLI args")))
        case Right(value) => value.pure[IO]
      }

      result <- command match {
        case CLI.ShortestPath(dataUri, start, end) =>
          for {
            // Fetch & parse the measurement data
            trafficMeasurements <-
              if (dataUri.scheme.map(_.value.toLowerCase).contains("file")) {
                // Fetch from disk
                IO.blocking {
                  Files
                    .readString(Path.of(dataUri.renderString.drop("file:".length)), Charset.forName("utf-8"))
                }.flatMap(data =>
                  IO.fromEither(parse(data))
                    .flatMap(json => IO.fromEither(json.as[TrafficMeasurements]))
                    .map(_.measurements)
                )
              } else {
                // Fetch from network
                BlazeClientBuilder[IO]
                  .resource
                  .use { client: Client[IO] =>
                    client
                      .expect[TrafficMeasurements](dataUri)
                      .map(_.measurements)
                  }
              }

            // Construct a graph using the measurement data
            graph = trafficMeasurements
              .flatMap(_.measurements)
              .groupBy(m => (m.startStreet, m.startAvenue, m.endStreet, m.endAvenue))
              .flatMap { case ((startStreet, startAve, endStreet, endAve), measurements) =>
                // Calculate the average
                Try {
                  val avgTransitTime = measurements.map(_.transitTime).sum / measurements.length
                  Measurement(startAve, startStreet, avgTransitTime, endAve, endStreet)
                }.toOption
              }
              .groupBy(a => a.startStreet -> a.startAvenue)
              .map { case ((street, ave), measurements) =>
                Node(
                  s"$street$ave",
                  edges = measurements.map(m => Edge(s"${m.endStreet}${m.endAvenue}", m.transitTime)).toList
                )
              }
              .toList

            // Find the start and end nodes in the graph
            startNodeStr = start._1 ++ start._2
            endNodeStr = end._1 ++ end._2

//            _ = (start, end)
//
//            startNodeStr = "A26"
//            endNodeStr = "T9"

            (startNode, endNode) <- IO.fromOption(
              graph
                .find(node => node.name === startNodeStr)
                .flatMap { startNode =>
                  graph
                    .find(node => node.name === endNodeStr)
                    .map { endNode =>
                      startNode -> endNode
                    }
                }
            )(new Throwable("Unable to find start/end node in graph"))

            (transitTime, segments) <- IO.fromOption(shortestPath(startNode, endNode, graph))(
              new Throwable("something went wrong") // Should not see this since we are checking nodes exists above
            )

            _ <- IO.println(s"Starting intersection: $startNodeStr")
            _ <- IO.println(s"Ending intersection  : $endNodeStr")
            _ <- IO.println(s"Segments             : ${segments.map(_.name).mkString(" -> ")}")
            _ <- IO.println(s"Total transit time   : $transitTime")

            //            _ <- IO.println(
            //              table
            //                .values
            //                .toList
            //                .sortBy(_.node.name)
            //                .map { r =>
            //                  s"""${r.node.name} -> ${r.lowestCostFromStart} -> ${r.previousNode.map(_.name)}"""
            //                }
            //                .mkString("\n")
            //            )
          } yield ExitCode.Success

      }
    } yield result
}
