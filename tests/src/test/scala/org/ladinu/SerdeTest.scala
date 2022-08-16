package org.ladinu

import io.circe.Json
import org.ladinu.Models.TrafficMeasurements
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import io.circe.syntax._
import io.circe.parser._
import org.scalatest.matchers.must.Matchers._
import org.scalatest.EitherValues

class SerdeTest extends AnyFunSuite with Serde with Generators with EitherValues {

  test("TrafficMeasurement serialization and deserialization should be symmetric") {
    forAll { data: TrafficMeasurements =>
      parse(data.asJson.noSpaces).getOrElse(Json.Null).as[TrafficMeasurements].value mustEqual data
    }
  }
}
