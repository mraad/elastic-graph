package com.esri.elasticgraph

import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class DirDistSpec extends FlatSpec with Matchers {

  case class DataSpec(caseID: String, x: Double, y: Double) extends Euclid

  it should "test directional distribution" in {
    val stream1 = this.getClass.getClassLoader.getResourceAsStream("points.csv")
    stream1 shouldNot be(null)
    val orig = try {
      Source
        .fromInputStream(stream1)
        .getLines
        .drop(1)
        .map(line => {
          line.split(',') match {
            case Array(caseID, xText, yText) => {
              DataSpec(caseID, xText.toDouble, yText.toDouble)
            }
          }
        })
        .toSeq
        .groupBy(_.caseID)
        .map {
          case (id, seq) => {
            (id, DirDist(seq).get)
          }
        }
    } finally {
      stream1.close()
    }
    val stream2 = this.getClass.getClassLoader.getResourceAsStream("dirdist.csv")
    stream2 shouldNot be(null)
    val dest = try {
      Source
        .fromInputStream(stream2)
        .getLines
        .drop(1)
        .map(line => {
          line.split(',') match {
            case Array(_, _, _, mx, my, sx, sy, deg, caseID) => {
              caseID -> DirDist(mx.toDouble, my.toDouble, sx.toDouble, sy.toDouble, deg.toDouble, 0)
            }
          }
        })
        .toMap
    } finally {
      stream2.close()
    }
    orig.foreach {
      case (caseOrig, dirOrig) => {
        val dirDest = dest(caseOrig)
        dirOrig.mx shouldBe dirDest.mx +- 0.0001
        dirOrig.my shouldBe dirDest.my +- 0.0001
        dirOrig.sx shouldBe dirDest.sx +- 0.0001
        dirOrig.sy shouldBe dirDest.sy +- 0.0001
        dirOrig.deg shouldBe dirDest.deg +- 0.01
      }
    }
  }
}
