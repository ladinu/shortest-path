package org.ladinu

import org.ladinu.Models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

trait Generators {

  implicit def arbMeasurement: Arbitrary[Measurement] = Arbitrary(
    for {
      startStreet <- arbitrary[String]
      startAvenue <- arbitrary[String]
      transitTime <- arbitrary[Double]
      endStreet <- arbitrary[String]
      endAvenue <- arbitrary[String]
    } yield Measurement(startStreet, startAvenue, transitTime, endStreet, endAvenue)
  )

  implicit def arbTrafficMeasurement: Arbitrary[TrafficMeasurement] = Arbitrary(
    for {
      measurementTime <- arbitrary[Long]
      measurements <- arbitrary[List[Measurement]]
    } yield TrafficMeasurement(measurementTime, measurements)
  )

  implicit def arbTrafficMeasurements: Arbitrary[TrafficMeasurements] = Arbitrary(
    arbitrary[List[TrafficMeasurement]].map(TrafficMeasurements)
  )
}
