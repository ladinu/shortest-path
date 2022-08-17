package org.ladinu

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp with ShortestPath with Utils {
  override def run(args: List[String]): IO[ExitCode] =
    for {

      _ <-
        if (Runtime.version().feature() < 11) {
          IO.println("This program require JVM 11+").*>(IO.raiseError(new Throwable("Invalid Java version")))
        } else {
          IO.unit
        }

      command <- CLI.args.parse(args) match {
        case Left(help) =>
          IO.println(help)
            .*>(IO.raiseError(new Throwable("Unable to parse CLI args")))

        case Right(value) => value.pure[IO]
      }

      result <- command match {
        case CLI.Dot(dataUri) =>
          getTrafficData(dataUri)
            .map(toDOT)
            .flatMap(IO.println)
            .as(ExitCode.Success)

        case CLI.ShortestPath(dataUri, start, end) =>
          for {
            trafficMeasurements <- getTrafficData(dataUri)

            // Construct a graph using the measurement data
            graph = toGraph(trafficMeasurements)

            startNodeStr = start._1 ++ start._2
            endNodeStr = end._1 ++ end._2

            // Find the start and end nodes in the graph
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

          } yield ExitCode.Success

      }
    } yield result
}
