package org.ladinu

import com.monovore.decline.{Command, Opts}
import cats.implicits._
import org.http4s.Uri

import scala.util.Try

object CLI {

  sealed trait Cmd
  type Intersection = (String, String)
  final case class ShortestPath(dataUri: Uri, start: Intersection, end: Intersection) extends Cmd

  val dataUri: Opts[Uri] = Opts
    .option[String](long = "data-uri", help = "file:// or https:// ")
    .mapValidated { urlStr =>
      Uri.fromString(urlStr).toValidated.leftMap(_.sanitized).toValidatedNel
    }

  def intersectionOpt(long: String, help: String): Opts[Intersection] =
    Opts
      .option[String](long, help)
      .mapValidated { str =>
        Try {
          str.split(":").toList
        }
          .toValidated
          .leftMap(_.getMessage)
          .andThen {
            case street :: avenue :: Nil => (street -> avenue).valid
            case _                       => "".invalid
          }
          .toValidatedNel
      }

  val startOpt: Opts[Intersection] = intersectionOpt("start", "Starting intersection. For example 1:A")
  val endOpt: Opts[Intersection] = intersectionOpt("end", "Finishing intersection. For example 4:B")

  val shortestPath: Opts[Cmd] = Opts
    .subcommand(name = "shortest-path", "Find the shortest path between two intersections") {
      (dataUri, startOpt, endOpt).mapN(ShortestPath)
    }

  val args: Command[Cmd] = Command(name = "app", header = "Solution by Ladinu Chandrasinghe") {
    shortestPath
  }
}
