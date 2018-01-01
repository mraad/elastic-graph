package com.esri.elasticgraph

/**
  * A unique node id generator.
  */
object NodeID extends Serializable {

  private val id = new java.util.concurrent.atomic.AtomicInteger()

  /**
    * @return next id identifier.
    */
  def next(): Int = id.getAndIncrement()
}
