package com.esri.elasticgraph

import java.io.PrintWriter

/**
  * Create a file with graph edges at each epoch in WKT format.
  *
  * @param filename the output filename.
  */
case class WKTListener(filename: String) extends IterListener {

  private val pw = new PrintWriter(filename)

  override def onGraph(graph: Graph): Unit = {
    val yyyy = 1998 + graph.numNodes
    graph.edges.foreach(edge => {
      val n1 = graph.findNode(edge.n1)
      val n2 = graph.findNode(edge.n2)
      pw.println(f"$yyyy\tLINESTRING(${n1.x}%.1f ${n1.y}%.1f,${n2.x}%.1f ${n2.y}%.1f)")
    })
  }

  override def close(): Unit = {
    pw.close()
  }
}
