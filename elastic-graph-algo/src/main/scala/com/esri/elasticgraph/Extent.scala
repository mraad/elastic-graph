package com.esri.elasticgraph

/**
  * Extent of a data set.
  *
  * @param xmin the lower left horizontal value.
  * @param ymin the lower left vertical value.
  * @param xmax the upper right horizontal value.
  * @param ymax the upper right vertical value.
  */
case class Extent(xmin: Double = Double.PositiveInfinity,
                  ymin: Double = Double.PositiveInfinity,
                  xmax: Double = Double.NegativeInfinity,
                  ymax: Double = Double.NegativeInfinity
                 ) {

  /**
    * Expand the extent with a Euclid instance.
    *
    * @param e a Euclid instance.
    * @return a new Extent instance.
    */
  def +(e: Euclid) = {
    Extent(
      xmin min e.x,
      ymin min e.y,
      xmax max e.x,
      ymax max e.y
    )
  }

  /**
    * @return the width of the extent.
    */
  def width(): Double = xmax - xmin

  /**
    * @return the height of the extent.
    */
  def height(): Double = ymax - ymin

}
