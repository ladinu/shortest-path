package org.ladinu

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.ladinu.Models._

trait Serde {

  implicit def trafficMeasurementDecoder: Decoder[TrafficMeasurement] = deriveDecoder[TrafficMeasurement]
  implicit def trafficMeasurement: Decoder[Measurement] = deriveDecoder[Measurement]
  implicit def trafficMeasurements: Decoder[TrafficMeasurements] = Decoder.instance { hc =>
    hc.downField("trafficMeasurements").as[List[TrafficMeasurement]].map(TrafficMeasurements)
  }
}
