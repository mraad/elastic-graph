package com.esri.elasticgraph

/**
  * Trait to represent a 2D location.
  */
trait Euclid extends Serializable {

  /**
    * @return the horizontal position.
    */
  def x(): Double

  /**
    * @return the vertical position.
    */
  def y(): Double

  /**
    * Calculate the distance squared to another Euclid instance.
    *
    * @param e the other Euclid instance.
    * @return the distance squared to the other Euclid instance.
    */
  def distSqr(e: Euclid): Double = {
    val dx = e.x - x
    val dy = e.y - y
    dx * dx + dy * dy
  }

}
