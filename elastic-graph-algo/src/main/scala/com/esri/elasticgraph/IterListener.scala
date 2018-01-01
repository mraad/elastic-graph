package com.esri.elasticgraph

/**
  * Learning iterator trait.
  */
trait IterListener extends AutoCloseable with Serializable {

  /**
    * @param graph the graph at an iteration epoch.
    */
  def onGraph(graph: Graph): Unit

}
