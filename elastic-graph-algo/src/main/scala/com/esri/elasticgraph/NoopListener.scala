package com.esri.elasticgraph

/**
  * No Operation Iter Listener
  */
class NoopListener extends IterListener {

  // Do nothing
  override def onGraph(graph: Graph): Unit = {}

  // Do nothing
  override def close(): Unit = {}
}

/**
  * Companion Object
  */
object NoopListener extends Serializable {
  def apply(): NoopListener = new NoopListener()
}
