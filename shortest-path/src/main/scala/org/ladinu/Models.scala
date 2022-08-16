package org.ladinu

object Models {

  // Graph models
  final case class Node(name: String, edges: List[Edge])
  final case class Edge(to: String, cost: Double)
  final case class Row(node: Node, lowestCostFromStart: Double, previousNode: Option[Node])

  // Data file models
  final case class TrafficMeasurements(measurements: List[TrafficMeasurement])
  final case class TrafficMeasurement(measurementTime: Long, measurements: List[Measurement])
  final case class Measurement(
      startStreet: String,
      startAvenue: String,
      transitTime: Double,
      endStreet: String,
      endAvenue: String
  )
}
