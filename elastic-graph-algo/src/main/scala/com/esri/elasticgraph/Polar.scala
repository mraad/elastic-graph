package com.esri.elasticgraph

import scala.math.{atan2, sqrt}

/**
  * Class to represent a polar coordinate.
  *
  * @param len the length from a center location.
  * @param deg the angle in degrees from the horizon.
  */
case class Polar(len: Double, deg: Double) {

  /**
    * Find a new polar location between this polar and supplied polar location.
    *
    * @param that the other polar location.
    * @param l    the new length.
    * @return a new polar location between this and that polar location.
    */
  def between(that: Polar, l: Double): Polar = {
    val d = that.deg - this.deg
    val a = this.deg + (if (d < 0.0) 360 + d else d) * 0.5
    Polar(l, a)
  }

  /**
    * Create a 180 degree offset polar location.
    *
    * @return a new polar location that is 180 degree off.
    */
  def one80(): Polar = {
    Polar(len, deg + 180.0)
  }

}

/**
  * Companion object.
  */
object Polar extends Serializable {
  /**
    * Create a Polar instance from a star and one of its nodes.
    *
    * @param star the star node as the center.
    * @param node a node off the star.
    * @return a Polar instance.
    */
  def apply(star: Node, node: Node): Polar = {
    val dx = node.x - star.x
    val dy = node.y - star.y
    val l = sqrt(dx * dx + dy * dy)
    val a = atan2(dy, dx)
    Polar(l, a.toDegrees)
  }
}
