package org.ladinu

import org.ladinu.Models._
import org.scalatest.OptionValues
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class ShortestPathTest extends AnyFunSuite with OptionValues with ShortestPath {

  test("Return None when graph does not contain end node") {
    val nodeA = Node("A", List(Edge("B", 1)))
    val nodeB = Node("B", List(Edge("A", 1)))

    val graph = List(nodeA, nodeB)

    shortestPath(nodeA, Node("C", Nil), graph) shouldBe empty
  }

  test("Return None when graph is empty") {
    val startNode = Node("A", List())
    val endNode = Node("C", List(Edge("B", 5.0), Edge("E", 5.0)))
    shortestPath(startNode, endNode, Nil) shouldBe empty
  }

  test("Return shortest between two nodes in the graph") {
    val startNode = Node("A", List(Edge("B", 6.0), Edge("D", 1.0)))
    val endNode = Node("C", List(Edge("B", 5.0), Edge("E", 5.0)))

    val graph = List(
      startNode,
      Node("A", List(Edge("B", 6.0), Edge("D", 1.0))),
      Node("B", List(Edge("A", 6.0), Edge("D", 2.0), Edge("C", 5.0))),
      endNode,
      Node("D", List(Edge("A", 1.0), Edge("B", 2.0), Edge("E", 1.0))),
      Node("E", List(Edge("D", 1.0), Edge("B", 2.0), Edge("C", 5.0)))
    )

    val (cost, path) = shortestPath(startNode, endNode, graph).value

    cost shouldBe 7.0
    path.map(_.name) should contain theSameElementsInOrderAs List("A", "D", "E", "C")
  }
}
