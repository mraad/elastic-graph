package com.esri.elasticgraph

import scala.math._

/**
  * The learning parameters.
  *
  * @param distSqr      the robust distance squared.
  * @param el           the elasticity factor.
  * @param mu           the bending factor.
  * @param maxNodes     the max nodes in a graph.
  * @param maxStarNodes the max nodes off a star node.
  * @param minAngle     the min angle between 2 nodes off a star node.
  * @param cutEdgesOnly apply only cut edge grammar flag.
  */
case class Param(distSqr: Double, el: Double, mu: Double, maxNodes: Int, maxStarNodes: Int, minAngle: Double, cutEdgesOnly: Boolean) {

  override def toString: String = {
    val dist = sqrt(distSqr)
    f"Param(robustDist=$dist%.3f,el=$el%.5f,mu=$mu%.5f,maxNodes=$maxNodes,minAngle=$minAngle%.0f,cutEdgesOnly=$cutEdgesOnly)"
  }

}
