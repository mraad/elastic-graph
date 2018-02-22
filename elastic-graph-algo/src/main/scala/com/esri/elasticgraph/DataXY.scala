package com.esri.elasticgraph

import com.esri.euclid.Euclid

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

  /**
    * Convert to an ArgMin instance.
    *
    * @param euclid a Euclid instance to calculate the distance to.
    * @return an ArgMin instance.
    */
  def toArgMin(euclid: Euclid): ArgMin[DataXY] = {
    ArgMin(this, this distSqr euclid)
  }

}
