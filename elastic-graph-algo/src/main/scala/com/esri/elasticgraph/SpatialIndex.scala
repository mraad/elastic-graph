package com.esri.elasticgraph

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scalaxy.loops._

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
    * Index supplied data.
    *
    * @param data the data to index.
    * @return this spatial index.
    */
  def +(data: DataXY): SpatialIndex = {
    val c = (data.x / size).floor.toInt
    val r = (data.y / size).floor.toInt
    grid.getOrElseUpdate(Cell(r, c), ArrayBuffer[DataXY]()) += data
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

  /**
    * Find all the data in the given cell and the surrounding cells.
    *
    * @param cell the center cell.
    * @return Sequence of data in the given cell and the surrounding cells.
    */
  def findData(cell: Cell): Seq[DataXY] = {
    val arr = ArrayBuffer[DataXY]()
    val r1 = cell.r - 1
    val r2 = cell.r + 1
    val c1 = cell.c - 1
    val c2 = cell.c + 1
    for (i <- r1 to r2 optimized) {
      for (j <- c1 to c2 optimized) {
        arr ++= grid.getOrElse(Cell(i, j), Seq.empty)
      }
    }
    arr
  }

}
