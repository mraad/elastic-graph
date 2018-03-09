package com.esri.elasticgraph

/**
  * No Operation Iter Listener
  */
class NoopListener extends IterListener {

  // Do nothing
  override def onGraph(graph: Graph): Unit = {}
}

/**
  * Companion Object
  */
object NoopListener extends Serializable {
  def apply(): NoopListener = new NoopListener()
}
