package org.ladinu

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import org.ladinu.Models._

trait Serde {

  implicit def trafficMeasurementDecoder: Decoder[TrafficMeasurement] = deriveDecoder[TrafficMeasurement]
  implicit def measurementDecoder: Decoder[Measurement] = deriveDecoder[Measurement]

  private val trafficMeasurements = "trafficMeasurements"

  implicit def trafficMeasurementsDecoder: Decoder[TrafficMeasurements] = Decoder.instance { hc =>
    hc.downField(trafficMeasurements).as[List[TrafficMeasurement]].map(TrafficMeasurements)
  }

  implicit def measurementEncoder: Encoder[Measurement] = deriveEncoder[Measurement]
  implicit def trafficMeasurementEncoder: Encoder[TrafficMeasurement] = deriveEncoder[TrafficMeasurement]
  implicit def trafficMeasurementsEncoder: Encoder[TrafficMeasurements] = Encoder.instance { data =>
    Json.obj(trafficMeasurements -> data.measurements.asJson)
  }
}
