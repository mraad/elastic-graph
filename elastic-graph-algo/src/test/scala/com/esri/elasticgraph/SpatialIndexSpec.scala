package com.esri.elasticgraph

import org.scalatest.{FlatSpec, Matchers}

class SpatialIndexSpec extends FlatSpec with Matchers {

  it should "test spatial index" in {
    val d0 = DataXY(1.0, 1.0)
    val d1 = DataXY(10.0, 10.0)
    val d2 = DataXY(19.0, 19.0)
    val d3 = DataXY(21.0, 21.0)
    val d4 = DataXY(41.0, 41.0)

    val datum = Array(d0, d1, d2, d3, d4)

    val si = datum.foldLeft(SpatialIndex(20.0))(_ + _)
    val cell = si.findDensestCell
    cell shouldBe Cell(0, 0)
    si.findData(cell) should contain theSameElementsAs Array(d0, d1, d2, d3)
  }

  it should "test findNearest" in {
    val d0 = DataXY(1.0, 1.0)
    val d1 = DataXY(3.0, 1.0)
    val d2 = DataXY(3.0, 3.0)

    val datum = Array(d0, d1, d2)
    val si = datum.foldLeft(SpatialIndex(1.0))(_ + _)
    si.findNearest(DataXY(0.5, 0.5)) shouldBe Some(d0)
    si.findNearest(DataXY(2.5, 1.5)) shouldBe Some(d1)
    si.findNearest(DataXY(4.0, 4.0)) shouldBe Some(d2)
  }

}
