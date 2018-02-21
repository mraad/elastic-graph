package com.esri.elasticgraph

import scalaxy.loops._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * A cell in the spatial index grid.
  *
  * @param r the grid row.
  * @param c the grid col.
  */
case class Cell(r: Int, c: Int)

/**
  * Spatial index to quickly location neighbors of a location.
  * The implementation is based on a grid, where all the indexed points are grouped together based on the cell in the grid that they fall into.
  *
  * @param size the cell size.
  */
case class SpatialIndex(size: Double) {

  val grid = mutable.Map[Cell, ArrayBuffer[DataXY]]()

  /**
    * Convert a Euclid instance to a Cell instance.
    *
    * @param euclid A Euclid instance.
    * @return a Cell instance.
    */
  def toCell(euclid: Euclid): Cell = {
    val r = (euclid.y / size).floor.toInt
    val c = (euclid.x / size).floor.toInt
    Cell(r, c)
  }

  /**
    * Index supplied data.
    *
    * @param data the data to index.
    * @return this spatial index.
    */
  def +(data: DataXY): SpatialIndex = {
    grid.getOrElseUpdate(toCell(data), ArrayBuffer[DataXY]()) += data
    this
  }

  /**
    * Find the cell with the most data.
    *
    * @return the cell with the most data.
    */
  def findDensestCell(): Cell = {
    grid
      .map {
        case (cell, arr) => ArgMin(cell, arr.length)
      }
      .max
      .arg
  }

  @inline
  private def getCellData(r: Int, c: Int): Seq[DataXY] = {
    grid.getOrElse(Cell(r, c), Seq.empty)
  }

  /**
    * Find all the data in a given center cell and its surrounding cells.
    *
    * @param cell the center cell.
    * @return Sequence of data in the given center cell and its surrounding cells.
    */
  def findData(cell: Cell): Seq[DataXY] = {
    val arr = ArrayBuffer[DataXY]()
    val r1 = cell.r - 1
    val r2 = cell.r + 1
    val c1 = cell.c - 1
    val c2 = cell.c + 1
    for (r <- r1 to r2 optimized) {
      for (c <- c1 to c2 optimized) {
        arr ++= getCellData(r, c)
      }
    }
    arr
  }

  /**
    * Find nearest DataXY to a given Euclid.
    *
    * @param euclid   a Euclid instance.
    * @param maxRings the max number of rings to search, default is 3.
    * @return a DataXY option.
    */
  def findNearest(euclid: Euclid, maxRings: Int = 3): Option[DataXY] = {
    var found = Option.empty[DataXY]
    val q = (euclid.x / size).floor.toInt
    val r = (euclid.y / size).floor.toInt
    val datum = new ArrayBuffer[DataXY](32)
    var n = 0
    while (found.isEmpty && n < maxRings) {
      datum.clear()

      val qmin = q - n
      val qmax = q + n
      val rmin = r - n
      val rmax = r + n

      if (n > 0) {
        var x = qmin
        while (x <= qmax) {
          datum ++= getCellData(rmin, x)
          datum ++= getCellData(rmax, x)
          x += 1
        }
      } else {
        datum ++= getCellData(rmin, qmin)
      }
      var y = rmin + 1
      while (y < rmax) {
        datum ++= getCellData(y, qmin)
        datum ++= getCellData(y, qmax)
        y += 1
      }
      if (!datum.isEmpty) {
        found = Some(datum.map(_ toArgMin euclid).min.arg)
      }
      n += 1
    }
    found
  }

}
