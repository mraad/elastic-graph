package com.esri.elasticgraph

import java.io.PrintWriter

import com.esri.euclid.{Euclid, Extent}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Write results as JavaScript data structure to be viewed with index.html
  *
  * @param filename the javascript output file name.
  * @param datum    the input data.
  */
case class JSListener(filename: String, datum: Seq[Euclid]) extends IterListener with AutoCloseable {

  var graphs = Seq.empty[Graph]

  /**
    * Collect locally the current graph at an epoch.
    *
    * @param graph the graph at an iteration epoch.
    */
  override def onGraph(graph: Graph): Unit = {
    graphs = graphs :+ graph
  }

  /**
    * Write the collected graphs.
    */
  override def close(): Unit = {
    val extent = datum.foldLeft(Extent())(_ + _)
    val offX = extent.width / 10.0
    val offY = extent.height / 10.0
    val xmin = extent.xmin - offX
    val ymin = extent.ymin - offY
    val xmax = extent.xmax + offX
    val ymax = extent.ymax + offY
    val xdel = xmax - xmin
    val ydel = ymax - ymin

    val doc = (
      ("xmin" -> xmin) ~
        ("ymin" -> ymin) ~
        ("xdel" -> xdel) ~
        ("ydel" -> ydel) ~
        ("datum" -> datum.toList.map(data =>
          (("x" -> data.x) ~ ("y" -> data.y))
        )) ~
        ("links" -> graphs.map(graph => {
          graph.edges.map(edge => {
            val n1 = graph.findNode(edge.n1)
            val n2 = graph.findNode(edge.n2)
            (("mx" -> n1.x) ~ ("my" -> n1.y) ~ ("lx" -> n2.x) ~ ("ly" -> n2.y))
          }).toList
        }))
      )

    val pw = new PrintWriter(filename)
    try {
      pw.print("var doc=")
      pw.print(compact(render(doc)))
      pw.println(";")
    } finally {
      pw.close()
    }
  }
}
