package com.esri.elasticgraph

import breeze.linalg.DenseMatrix

/**
  * A graph with immutable nodes and edges.
  *
  * @param nodes the nodes of the graph.
  * @param edges the edges of the graph.
  */
class Graph(val nodes: Array[Node], val edges: Array[Edge]) extends Serializable {

  /**
    * @return the number of nodes in this graph.
    */
  def numNodes(): Int = nodes.length

  /**
    * Find a node given a node id.
    *
    * @param id the node id.
    * @return a Node instance associated with given node id.
    */
  def findNode(id: Int): Node = nodes.find(_ === id).getOrElse(Node.zero)

  /**
    * Find the nodes connected with that node.
    *
    * @param node the center node.
    * @return an array of nodes.
    */
  private def findNeighbors(node: Node): Array[Node] = {
    val n1 = edges.withFilter(node === _.n2).map(edge => findNode(edge.n1))
    val n2 = edges.withFilter(node === _.n1).map(edge => findNode(edge.n2))
    n1 ++ n2
  }

  // Map of node to neighboring nodes
  private val neighborhood = nodes.map(node => node -> findNeighbors(node)).toMap

  val (stars, leafs) = neighborhood.partition {
    case (_, nodes) => nodes.length > 1
  }

  /**
    * Cut an edge by inserting a node in the middle of the existing nodes.
    *
    * 0 <----> 1  becomes 0 <----> 2 <----> 1
    *
    * @param edge the edge to cut.
    * @return a Graph instance.
    */
  def cutEdge(edge: Edge): Graph = {
    val n1 = findNode(edge.n1)
    val n2 = findNode(edge.n2)
    val node = n1 mid n2
    new Graph(nodes :+ node, edges.filterNot(_ == edge) ++ Seq(Edge(edge.n1, node.id), Edge(node.id, edge.n2)))
  }

  /**
    * Create an edge from an existing star node to a new node.
    *
    * @param star the star node.
    * @param node the new node to add and form an edge.
    * @return a new Graph instance.
    */
  def addEdge(star: Node, node: Node): Graph = {
    new Graph(
      nodes :+ node,
      edges :+ Edge(star.id, node.id)
    )
  }

  /**
    * Convert the nodes of a star to Polar instance.
    *
    * @param star  reference to a star node.
    * @param nodes the nodes of a star.
    * @return an array of Polar instance.
    */
  def convertNodesToPolars(star: Node, nodes: Array[Node]): Array[Polar] = {
    val polars = nodes
      .map(Polar(star, _))
      .sortBy(_.deg)
    polars :+ polars.head
  }

  /**
    * Create Graph instances by cutting edges and adding nodes to star nodes.
    *
    * @param param the learning parameters.
    * @param datum the input data.
    * @return array of Graph instances.
    */
  def subGraphs(param: Param, datum: Array[DataXY]): Array[Graph] = {
    // Cut the edges.
    val s1 = edges.map(cutEdge(_))
    // Create new nodes based on leaf nodes and opposite direction of associated stars
    val s2 = leafs.map {
      case (leaf, nodes) => {
        val star = nodes.head
        val node = star flipAround leaf
        addEdge(leaf, node)
      }
    }
    if (stars.isEmpty || param.cutEdgesOnly) {
      s1 ++ s2
    } else {
      // TODO - Create sub star collection with nodes less that param.maxNodes.
      // Associate each data with a star.  The distance to the star has to be less that the robust distance.
      val group = datum
        .map(data => {
          val bestNode = stars
            .map {
              case (star, _) => ArgMin(star, star distSqr data)
            }
            .min
          val id = if (bestNode.min < param.distSqr) bestNode.arg.id else Node.zero.id
          ArgMin(data, 0.0, id)
        })
        .groupBy(_.index)
      // Create new edges based on the data that is associated with each star.
      val s3 = stars
        .withFilter {
          case (star, _) => group contains star.id
        }
        .map {
          case (star, _) => {
            val arr = group(star.id)
            val nx = arr.map(_.arg.x).sum / arr.length
            val ny = arr.map(_.arg.y).sum / arr.length
            addEdge(star, Node(nx, ny))
          }
        }
      s1 ++ s2 ++ s3
    }
  }

  /**
    * Check if this graph is valid.
    * An invalid graph will have a star with 2 nodes having an angle between them less than param.minAngle.
    *
    * TODO check if a edge crosses another edge
    *
    * @param param the learning parameters
    * @return true if this graph is value, false otherwise.
    */
  def isValid(param: Param): Boolean = {
    !stars.exists {
      case (star, nodes) => {
        val polars = convertNodesToPolars(star, nodes)
        val minDeg = polars
          .sliding(2)
          .map {
            case Array(head, last) => {
              val d = last.deg - head.deg
              if (d < 0.0) 360 + d else d
            }
          }
          .min
        minDeg < param.minAngle
      }
    }
  }

  /**
    * Create elastic matrix.
    *
    * @param el common elasticity factor.
    * @param mu common bending factor.
    * @return a 2D matrix.
    */
  def createMatrix(el: Double, mu: Double): DenseMatrix[Double] = {
    val mat = DenseMatrix.zeros[Double](nodes.length, nodes.length)

    val indices = nodes.map(_.id).zipWithIndex.map {
      case (n, i) => n -> i
    }.toMap

    edges.foreach(edge => {
      val i = indices(edge.n1)
      val j = indices(edge.n2)
      mat(i, i) += el
      mat(i, j) -= el
      mat(j, i) -= el
      mat(j, j) += el
    })

    stars.foreach {
      case (star, nodes) => {
        val i = indices(star.id)
        mat(i, i) += mu
        val v1 = mu / nodes.length
        val v2 = v1 / nodes.length
        nodes.foreach(node => {
          val j = indices(node.id)
          mat(i, j) -= v1
          mat(j, i) -= v1
          nodes.foreach(node => {
            val k = indices(node.id)
            mat(j, k) += v2
          })
        })
      }
    }

    mat
  }

  /**
    * Calc energy of this graph.
    *
    * @param el common elasticity factor.
    * @param mu common bending factor.
    * @param d  the pre-calculated distance of each data point to the graph.
    * @return the graph energy.
    */
  def calcEnergy(el: Double, mu: Double, d: Array[Double]): Double = {
    // average the distance of the data to the graph.
    val e1 = d.sum / d.length
    // Sum the squared length of the edges.
    val e2 = edges.map(edge => {
      val n1 = findNode(edge.n1)
      val n2 = findNode(edge.n2)
      n1 distSqr n2
    }).sum
    // Sum the squared deviation of each star to its neighbors.
    val e3 = stars.map {
      case (star, nodes) => {
        // calc avg location of the nodes of a star and calc squared deviation from star location.
        val dx = star.x - nodes.map(_.x).sum / nodes.length
        val dy = star.y - nodes.map(_.y).sum / nodes.length
        dx * dx + dy * dy
      }
    }.sum
    e1 + el * e2 + mu * e3
  }

}

/**
  * Companion object.
  */
object Graph extends Serializable {
  /**
    * Create a Graph instance given two nodes.
    *
    * @param n1 the first node of a graph.
    * @param n2 the second node of a graph.
    * @return a Graph instance.
    */
  def apply(n1: Node, n2: Node): Graph = {
    val nodes = Array(n1, n2)
    val edges = Array(Edge(n1.id, n2.id))
    new Graph(nodes, edges)
  }
}
