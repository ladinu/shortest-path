package org.ladinu

import org.ladinu.Models._

import scala.annotation.tailrec

trait ShortestPath {

  def shortestPath(start: Node, end: Node, graph: List[Node]): Option[(Double, List[Node])] = {
    val table = pathMap(start, graph)

    @tailrec
    def go(maybeNode: Option[Node], list: List[Node]): List[Node] =
      maybeNode match {
        case Some(currentNode) =>
          table.get(currentNode.name) match {
            case Some(row) => go(row.previousNode, currentNode :: list)
            case None      => list
          }
        case None => list
      }

    table
      .get(end.name)
      .map { row =>
        row.lowestCostFromStart -> go(Some(end), Nil)
      }
  }

  // This method implements Dijkstra’s Shortest Path Algorithm
  // https://www.youtube.com/watch?v=pVfj6mxhdMw
  def pathMap(start: Node, graph: List[Node]): Map[String, Row] = {

    val nodeCount = graph.length
    val table = graph
      .map(node => Row(node, Double.MaxValue, previousNode = None))
      .map(row => row.node.name -> row)
      .toMap + (start.name -> Row(start, 0, previousNode = None))

    val (_, finalTable) = (1 to nodeCount)
      .foldLeft((Set.empty[String], table)) { case ((visitedSet, table), _) =>
        // Find an unvisited node with the smallest known distance from the start node
        val maybeUnvisitedNodeWithLowestCost = table
          .collect {
            case (nodeName, row) if !visitedSet.contains(nodeName) => nodeName -> row
          }
          .minByOption { case (_, row) => row.lowestCostFromStart }

        val newTable = maybeUnvisitedNodeWithLowestCost
          .map { case (_, row) =>
            val currentNode = row.node
            // Find all unvisited neighbors of `currentNode`
            val unvisitedNeighbors = currentNode.edges.filterNot(e => visitedSet.contains(e.to))

            // Calculate the distance of each neighbor from the start node
            val costs = unvisitedNeighbors
              .map(e => e.to -> (e.cost + row.lowestCostFromStart))

            // If the calculated cost is less than the current known cost, update the `lowestCostFromStart`
            // in the row of the table. Also update the `previousNode` to the `currentNode`
            val updatedTable = costs
              .foldLeft(table) { case (table, (nodeName, cost)) =>
                table
                  .get(nodeName)
                  .map { row =>
                    if (row.lowestCostFromStart > cost)
                      row.copy(lowestCostFromStart = cost, previousNode = Some(currentNode))
                    else row
                  }
                  .map(row => table + (nodeName -> row))
                  .getOrElse(table)
              }

            updatedTable
          }
          .getOrElse(table)

        // Update the visitedSet with `currentNode` name
        val newVisitedSet = maybeUnvisitedNodeWithLowestCost
          .map(_._1)
          .map(currentNode => Set(currentNode) ++ visitedSet)
          .getOrElse(visitedSet)

        (newVisitedSet, newTable)
      }
    finalTable
  }
}
