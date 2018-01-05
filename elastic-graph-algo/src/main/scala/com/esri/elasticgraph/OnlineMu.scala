package com.esri.elasticgraph

/**
  * Online mean calculation.
  * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
  *
  * @param n  the current count.
  * @param mx the current mean of x values.
  * @param my the current mean of y values.
  */
case class OnlineMu(var n: Int = 0, var mx: Double = 0.0, var my: Double = 0.0) {

  /**
    * Add a Euclid instance.
    *
    * @param euclid the Euclid instance to add.
    * @return this OnlineMu instance.
    */
  def +(euclid: Euclid): OnlineMu = {
    this + (euclid.x, euclid.y)
  }

  /**
    * Add an x and y value.
    *
    * @param x the x value to add.
    * @param y the y value to add.
    * @return this OnlineMu instance.
    */
  def +(x: Double, y: Double): OnlineMu = {
    n += 1

    val dx = x - mx
    mx += dx / n

    val dy = y - my
    my += dy / n

    this
  }

  def deviations(datum: Iterable[Euclid]): (Array[(Double, Double)], Double, Double, Double) = {
    // TODO - Modify to use ArrayBuffer !
    datum.foldLeft((Array.empty[(Double, Double)], 0.0, 0.0, 0.0)) {
      case ((arr, x2, y2, xy), data) => {
        val dx = data.x - mx
        val dy = data.y - my
        (arr :+ (dx, dy), x2 + dx * dx, y2 + dy * dy, xy + dx * dy)
      }
    }
  }
}
