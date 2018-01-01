package com.esri.elasticgraph

/**
  * A 2D location of input data.
  *
  * @param x the data horizontal position.
  * @param y the data vertical position.
  */
case class DataXY(x: Double, y: Double) extends Euclid {

  /**
    * Convert to a Node instance.
    *
    * @return a Node instance.
    */
  def toNode(): Node = {
    Node(x, y)
  }

}
