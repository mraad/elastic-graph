package com.esri.elasticgraph

import breeze.linalg.{DenseMatrix, DenseVector}

import scala.annotation.tailrec
import scalaxy.loops._

/**
  * Optimize the locations of the nodes of a graph for a given data set.
  *
  * @param param the learning parameters.
  * @param datum optimize the graph nodes based on this data set.
  * @param graph the graph to optimize.
  */
class Optimize(param: Param, datum: Array[DataXY], graph: Graph) extends Serializable {

  private val matOrig = graph.createMatrix(param.el, param.mu)
  private val matComp = DenseMatrix.zeros[Double](graph.numNodes, graph.numNodes)

  // data to node classification
  private val k = Array.ofDim[Int](datum.length)
  // distance of data to node
  private val d = Array.ofDim[Double](datum.length)
  // number of data per node
  private val n = Array.ofDim[Double](graph.numNodes)
  // node x positions
  private val x = DenseVector.zeros[Double](graph.numNodes)
  // node y positions
  private val y = DenseVector.zeros[Double](graph.numNodes)

  /**
    * Reset the number of data/nodes and the node positions.
    */
  private def reset() = {
    for (i <- 0 until n.length optimized) {
      n(i) = 0.0
      x(i) = 0.0
      y(i) = 0.0
    }
  }

  /**
    * Find the best node for a given data point.
    *
    * @param nodes the nodes
    * @param data  the data.
    * @return ArgMin instance with best node, distance and node index.
    */
  private def findBestNode(nodes: Array[Node], data: DataXY): ArgMin[Node] = {
    nodes.zipWithIndex.map {
      case (node, nodeIndex) => ArgMin(node, node distSqr data, nodeIndex)
    }.min
  }

  /**
    * Classify data per node based on robust distance.
    *
    * @param nodes the new nodes to use to classify data points.
    * @return Tuple (boolean,boolean) - the first item is an indication if a data class changed, the second item is an indication if all input data is outside the robust radius from a node.
    */
  def classify(nodes: Array[Node]): (Boolean, Boolean) = {
    var changed = 0
    var outside = 0
    reset()
    for (dataIndex <- 0 until datum.length optimized) {
      val data = datum(dataIndex)
      val argMin = findBestNode(nodes, data)
      if (argMin.min <= param.distSqr) {
        x(argMin.index) += data.x
        y(argMin.index) += data.y
        n(argMin.index) += 1.0
        if (k(dataIndex) != argMin.index) {
          changed += 1
        }
        k(dataIndex) = argMin.index
        d(dataIndex) = argMin.min
      } else {
        if (k(dataIndex) >= 0) {
          changed += 1
        }
        k(dataIndex) = -1 - argMin.index
        d(dataIndex) = param.distSqr
        outside += 1
      }
    }
    (changed == 0, outside == datum.length)
  }

  val iterMax = 21

  @tailrec
  private def optimizeRec(oldNodes: Array[Node], iterNum: Int = 0): Array[Node] = {
    // Keep looping until either:
    // 1 - we reached a max iteration count
    // 2 - the data to node classification did not change
    // 3 - all data is "far" from each node.
    if (iterNum == iterMax) {
      oldNodes
    } else {
      val (noChange, allOutside) = classify(oldNodes)
      if (noChange || allOutside) {
        oldNodes
      } else {
        for (i <- 0 until matOrig.rows optimized) {
          for (j <- 0 until matOrig.cols optimized) {
            matComp(i, j) = matOrig(i, j)
          }
          matComp(i, i) += n(i) / datum.length
          x(i) /= datum.length
          y(i) /= datum.length
        }
        val newX = matComp \ x
        val newY = matComp \ y
        val newNodes = new Array[Node](oldNodes.length)
        for (i <- 0 until oldNodes.length optimized) {
          newNodes(i) = Node(oldNodes(i).id, newX(i), newY(i))
        }
        optimizeRec(newNodes, iterNum + 1)
      }
    }
  }

  /**
    * Optimize the nodes locations.
    *
    * @return ArgMin instance with a new Graph instance and its associated energy.
    */
  def optimize(): ArgMin[Graph] = {
    val nodesNew = optimizeRec(graph.nodes)
    val graphNew = new Graph(nodesNew, graph.edges)
    val energy = graphNew.calcEnergy(param.el, param.mu, d)
    ArgMin(graphNew, energy)
  }
}

/**
  * Companion object.
  */
object Optimize extends Serializable {
  /**
    * Create an Optimizer instance.
    */
  def apply(param: Param, datum: Array[DataXY], graph: Graph): Optimize = {
    new Optimize(param, datum, graph)
  }
}
