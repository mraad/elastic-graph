package com.esri.elasticgraph

import scala.io.Source
import scala.util.Random

/**
  * The main application for this project
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
  var datumOffset = 10

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
    case Array("--datumOffset", arg) => datumOffset = arg.toInt
    case Array("--outputPath", arg) => outputPath = arg
    case Array("--outputFormat", arg) => outputFormat = arg.toLowerCase
    case rest => {
      val text = rest.mkString
      println(s"${Console.RED}Invalid command line argument ($text), exiting.${Console.RESET}")
      sys.exit(-1)
    }
  }

  def loadDatum(): Array[DataXY] = {
    Source
      .fromFile(inputPath)
      .getLines
      .drop(inputDrop)
      .map(line => {
        val tokens = line.split(inputSep)
        val x = tokens(inputX).toDouble
        val y = tokens(inputY).toDouble
        DataXY(x, y)
      })
      .toArray
  }

  val datum = loadDatum()
  val i = Random.nextInt(datum.length)
  val j = (i + datumOffset) min (datum.length - 1)

  // Find the first node randomly
  val data1 = datum(i)
  /*
  // Find the second node that is furthest away
  val data2 = datum.map(data => {
    ArgMin(data, data distSqr data1)
  }).max.arg
  */
  val data2 = datum(j)

  val node1 = data1.toNode
  val node2 = data2.toNode

  val param = Param(robustDist * robustDist, el, mu, maxNodes, maxStarNodes, minAngle, cutEdgesOnly)

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
