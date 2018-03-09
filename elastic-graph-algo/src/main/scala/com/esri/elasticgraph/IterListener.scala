package com.esri.elasticgraph

/**
  * Learning iterator trait.
  */
trait IterListener extends Serializable {

  /**
    * @param graph the graph at an iteration epoch.
    */
  def onGraph(graph: Graph): Unit
}
