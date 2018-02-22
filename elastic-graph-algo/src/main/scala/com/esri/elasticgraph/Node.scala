package com.esri.elasticgraph

import com.esri.euclid.Euclid

import scala.math._

/**
  * Node in a Graph.
  *
  * @param id the node identifier.
  * @param x  the node horizontal position.
  * @param y  the node vertical position.
  */
case class Node(id: Int, x: Double, y: Double) extends Euclid {

  /**
    * @return hash code based on node id.
    */
  override def hashCode(): Int = id

  /**
    * Two nodes are the same if they have the same id value.
    *
    * @param obj the other node.
    * @return true if they have the same id value.
    */
  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case that: Node => that.id == id
      case _ => false
    }
  }

  /**
    * Is the same as a provided node.
    *
    * @param that provided node.
    * @return true if they have the same id, false otherwise.
    */
  def ===(that: Long): Boolean = id == that

  /**
    * Create a Node instance that is halfway to another node.
    *
    * @param that the another node.
    * @return a new Node instance that is halfway to the other node.
    */
  def mid(that: Node): Node = Node((this.x + that.x) * 0.5, (this.y + that.y) * 0.5)

  /**
    * Create a new node that is at the opposite direction around provided center node.
    *
    * @param center the center node to flip around.
    * @return a new Node instance.
    */
  def flipAround(center: Node): Node = {
    val dx = this.x - center.x
    val dy = this.y - center.y
    Node(center.x - dx, center.y - dy)
  }

  /**
    * Create a new node at a polar location from this node.
    *
    * @param p a Polar reference.
    * @return a new Node at polar location.
    */
  def +(p: Polar): Node = {
    val rad = p.deg.toRadians
    val nx = x + p.len * cos(rad)
    val ny = y + p.len * sin(rad)
    Node(nx, ny)
  }
}

/**
  * Companion object.
  */
object Node extends Serializable {
  /**
    * Placeholder for a default Node instance.
    */
  val zero = Node(NodeID.next, 0.0, 0.0)

  /**
    * Create a new Node instance.
    *
    * @param x the node horizontal position.
    * @param y the node vertical position.
    * @return a new Node instance.
    */
  def apply(x: Double, y: Double): Node = new Node(NodeID.next, x, y)
}
