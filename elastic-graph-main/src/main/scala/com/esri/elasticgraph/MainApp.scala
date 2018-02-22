package com.esri.elasticgraph

import scala.io.Source
import scala.util.Random

/**
  * The main application for this project.
  */
object MainApp extends App {

  var outputPath = "web/doc.js"
  var outputFormat = "js"
  var inputPath = "data/tree23.data"
  var inputDrop = 0
  var inputSep = "\t"
  var inputX = 0
  var inputY = 1
  var el = 0.0001
  var mu = 0.0001
  var minAngle = 20.0
  var maxNodes = 25
  var maxStarNodes = 4
  var cutEdgesOnly = false
  var shuffleTake = -1
  var dirDistTrue = false
  var robustDistArg = Double.NaN
  var indexSizeArg = Double.NaN

  // Minimalist and simple command line argument parser
  args.sliding(2, 2).foreach {
    case Array("--el", arg) => el = arg.toDouble
    case Array("--mu", arg) => mu = arg.toDouble
    case Array("--inputPath", arg) => inputPath = arg
    case Array("--inputDrop", arg) => inputDrop = arg.toInt
    case Array("--inputSep", arg) => inputSep = arg
    case Array("--inputX", arg) => inputX = arg.toInt
    case Array("--inputY", arg) => inputY = arg.toInt
    case Array("--maxNodes", arg) => maxNodes = arg.toInt
    case Array("--maxStarNodes", arg) => maxStarNodes = arg.toInt
    case Array("--minAngle", arg) => minAngle = arg.toDouble
    case Array("--cutEdgesOnly", arg) => cutEdgesOnly = arg.equalsIgnoreCase("true")
    case Array("--outputPath", arg) => outputPath = arg
    case Array("--outputFormat", arg) => outputFormat = arg.toLowerCase
    case Array("--shuffleTake", arg) => shuffleTake = arg.toInt
    case Array("--dirDistMode", arg) => dirDistTrue = arg.equalsIgnoreCase("true")
    case Array("--robustDist", arg) => robustDistArg = arg.toDouble
    case Array("--indexSize", arg) => indexSizeArg = arg.toDouble
    case rest => {
      val text = rest.mkString(" ")
      println(s"${Console.RED}Invalid command line argument ($text), exiting.${Console.RESET}")
      sys.exit(-1)
    }
  }

  private def loadDatum(): Array[DataXY] = {
    val list = Source
      .fromFile(inputPath)
      .getLines
      .drop(inputDrop)
      .map(line => {
        val tokens = line.split(inputSep)
        val x = tokens(inputX).toDouble
        val y = tokens(inputY).toDouble
        DataXY(x, y)
      })
      .toList
    if (shuffleTake > -1)
      Random.shuffle(list).take(shuffleTake min list.length).toArray
    else
      list.toArray
  }

  private def train(node1: Node, node2: Node, dist: Double): Unit = {
    val param = Param(dist * dist, el, mu, maxNodes, maxStarNodes, minAngle, cutEdgesOnly)
    val listener = outputFormat match {
      case "wkt" => WKTListener(outputPath)
      case "gif" => GIFListener(outputPath, datum)
      case _ => JSListener(outputPath, datum)
    }
    try {
      Train(datum, param, listener).train(node1, node2)
    } finally {
      listener.close()
    }
  }

  private def train(dist: DirDist, si: SpatialIndex, robustDist: Double): Unit = {
    val nodes = dist.majorNodes
    val n1 = si.findNearest(nodes.head, 5) match {
      case Some(n) => n.toNode
      case _ => nodes.head
    }
    val n2 = si.findNearest(nodes.last, 5) match {
      case Some(n) => n.toNode
      case _ => nodes.last
    }
    train(n1, n2, robustDist)
  }

  private def train(seq: Seq[DataXY], si: SpatialIndex, robustDist: Double): Unit = {
    DirDist(seq) match {
      case Some(dist) => {
        train(dist, si, robustDist)
      }
      case _ => printDirDistError()
    }
  }

  val datum = loadDatum()
  if (!datum.isEmpty) {
    DirDist(datum) match {
      case Some(dist) => {
        val minor = dist.sx min dist.sy
        val major = dist.sx max dist.sy
        val ratio = minor / major
        val extent = datum.foldLeft(Extent())(_ + _)

        val robustDist = if (robustDistArg.isNaN) minor / 2.0 else robustDistArg
        val indexSize = if (indexSizeArg.isNaN)
          ((extent.width max extent.height) / 10.0) max minor
        else
          indexSizeArg

        println(s"${Console.YELLOW}width=${extent.width} height=${extent.height}${Console.RESET}")
        println(f"${Console.YELLOW}major=$major%.1f minor=$minor%.1f ratio=$ratio%.3f${Console.RESET}")
        println(f"${Console.YELLOW}robustDist=$robustDist%.1f indexSize=$indexSize%.1f${Console.RESET}")

        val si = datum.foldLeft(SpatialIndex(indexSize))(_ + _)

        if (dirDistTrue) {
          train(dist, si, robustDist)
        } else {
          val cell = si.findDensestCell()
          val near = si.findData(cell)
          train(near, si, robustDist)
        }
      }
      case _ => printDirDistError()
    }
  }
  else {
    Console.println(s"${Console.RED}No input points.${Console.RESET}")
  }

  private def printDirDistError() = {
    println(s"${Console.RED}Too few input points (min of 3 is required), or all input points are in the same location.${Console.RESET}")
  }
}
