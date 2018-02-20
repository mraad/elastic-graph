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
  var robustDist = 0.2
  var minAngle = 20.0
  var maxNodes = 25
  var maxStarNodes = 4
  var cutEdgesOnly = false
  var shuffleTake = -1
  var dirDistMode = false
  var spatialSizeArg = Double.NaN

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
    case Array("--robustDist", arg) => robustDist = arg.toDouble
    case Array("--minAngle", arg) => minAngle = arg.toDouble
    case Array("--cutEdgesOnly", arg) => cutEdgesOnly = arg.equalsIgnoreCase("true")
    case Array("--outputPath", arg) => outputPath = arg
    case Array("--outputFormat", arg) => outputFormat = arg.toLowerCase
    case Array("--shuffleTake", arg) => shuffleTake = arg.toInt
    case Array("--dirDistMode", arg) => dirDistMode = arg.equalsIgnoreCase("true")
    case Array("--spatialSize", arg) => spatialSizeArg = arg.toDouble
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

  private def train(node1: Node, node2: Node, dist: Double) = {
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

  val datum = loadDatum()
  if (!datum.isEmpty) {
    if (dirDistMode) {
      DirDist(datum) match {
        case Some(dist) => {
          val minor = dist.sx min dist.sy
          val major = dist.sx max dist.sy
          val si = datum.foldLeft(SpatialIndex(major / 10.0))(_ + _)
          val nodes = dist.majorNodes
          val node1 = nodes.head
          val node2 = nodes.last
          val n1 = si.findNearest(node1, 10) match {
            case Some(n) => n.toNode
            case _ => node1
          }
          val n2 = si.findNearest(node2, 10) match {
            case Some(n) => n.toNode
            case _ => node2
          }
          Console.println(f"${Console.YELLOW}Overriding robust distance with $minor%.3f.${Console.RESET}")
          train(n1, n2, minor)
        }
        case _ => printDirDistError()
      }
    } else {
      val spatialSize = if (spatialSizeArg.isNaN) robustDist * 2.0 else spatialSizeArg
      val si = datum.foldLeft(SpatialIndex(spatialSize))(_ + _)
      val cell = si.findDensestCell()
      val near = si.findData(cell)
      DirDist(near) match {
        case Some(dist) => {
          val nodes = dist.majorNodes
          train(nodes.head, nodes.last, robustDist)
        }
        case _ => printDirDistError()
      }
    }
  }
  else {
    Console.println(s"${Console.RED}Input is empty, exiting.${Console.RESET}")
  }

  private def printDirDistError() = {
    println(s"${Console.RED}Too few data points (min of 3 is required), or all data points are in the same location.${Console.RESET}")
  }
}
