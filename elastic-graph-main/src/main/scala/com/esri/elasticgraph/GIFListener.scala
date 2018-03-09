package com.esri.elasticgraph

import java.awt.image.BufferedImage
import java.awt.{BasicStroke, Color, RenderingHints}
import java.io.File

import com.esri.euclid.{Euclid, Extent}
import javax.imageio.stream.FileImageOutputStream
import net.kroo.elliot.GifSequenceWriter

import scala.collection.JavaConversions._

/**
  * Write sequence of graphs at each epoch in an animated GIF.
  *
  * @param filename the animated GIF output file name.
  * @param datum    the input data.
  */
case class GIFListener(filename: String, datum: Seq[Euclid]) extends IterListener with AutoCloseable {

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
    val xoff = extent.width / 10.0
    val yoff = extent.height / 10.0
    val xmin = extent.xmin - xoff
    val ymin = extent.ymin - yoff
    val xmax = extent.xmax + xoff
    val ymax = extent.ymax + yoff
    val xdel = xmax - xmin
    val ydel = ymax - ymin

    val size = 300

    def toX(data: Euclid) = {
      (size * (data.x - xmin) / xdel).toInt
    }

    def toY(data: Euclid) = {
      (size * (1.0 - (data.y - ymin) / ydel)).toInt
    }

    val stroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

    val output = new FileImageOutputStream(new File(filename))
    try {
      val writer = new GifSequenceWriter(output, BufferedImage.TYPE_INT_RGB, 500, false)
      try {
        graphs.foreach(graph => {
          val bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
          val g = bi.createGraphics()
          try {
            g.setRenderingHints(Map(
              RenderingHints.KEY_ANTIALIASING -> RenderingHints.VALUE_ANTIALIAS_ON,
              RenderingHints.KEY_RENDERING -> RenderingHints.VALUE_RENDER_QUALITY
            ))
            g.setBackground(Color.WHITE)
            g.clearRect(0, 0, size, size)
            g.setColor(Color.BLACK)
            datum.foreach(data => {
              val x = toX(data) - 1
              val y = toY(data) - 1
              g.fillRect(x, y, 3, 3)
            })
            g.setColor(Color.RED)
            g.setStroke(stroke)
            graph.edges.foreach(edge => {
              val n1 = graph.findNode(edge.n1)
              val n2 = graph.findNode(edge.n2)
              val x1 = toX(n1)
              val y1 = toY(n1)
              val x2 = toX(n2)
              val y2 = toY(n2)
              g.drawLine(x1, y1, x2, y2)
            })
            g.setColor(Color.YELLOW)
            graph.nodes.foreach(node => {
              val x = toX(node)
              val y = toY(node)
              g.drawOval(x - 2, y - 2, 5, 5)
            })
          } finally {
            g.dispose()
          }
          writer.writeToSequence(bi)
        })
      } finally {
        writer.close()
      }
    } finally {
      output.close()
    }
  }
}
