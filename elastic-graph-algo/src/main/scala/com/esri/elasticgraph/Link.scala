package com.esri.elasticgraph

/**
  * A link references an edge with precomputed information.
  *
  * @param edge Edge reference.
  * @param n1   Node reference.
  * @param n2   Node reference.
  * @param dx   the edge horizontal difference.
  * @param dy   the edge vertical difference.
  * @param dd   the edge length squared.
  * @deprecated
  */
case class Link(edge: Edge, n1: Node, n2: Node, dx: Double, dy: Double, dd: Double) {

  /**
    * See {@link #distSqr(Double,Double)}
    */
  def distSqr(data: DataXY): Double = {
    distSqr(data.x, data.y)
  }

  /**
    * Calculated the distance of a perpendicular line from a given x,y to this link.
    *
    * @param x a horizontal position.
    * @param y a vertical position.
    * @return the perpendicular distance, otherwise PositiveInfinity.
    */
  def distSqr(x: Double, y: Double): Double = {
    val px = x - n1.x
    val py = y - n1.y
    val d = px * dx + py * dy
    if (d < 0.0)
      Double.PositiveInfinity
    else if (d > dd)
      Double.PositiveInfinity
    else
      px * px + py * py - (d * d) / dd // Assuming dd will _never_ be zero !!!
  }

}

/**
  * Companion object.
  */
object Link extends Serializable {
  /**
    * Create a Link instance.
    *
    * @param edge an Edge reference.
    * @param n1   a Node reference.
    * @param n2   a Node reference.
    * @return a Link instance.
    */
  def apply(edge: Edge, n1: Node, n2: Node): Link = {
    val dx = n2.x - n1.x
    val dy = n2.y - n1.y
    val dd = dx * dx + dy * dy
    new Link(edge, n1, n2, dx, dy, dd)
  }
}
