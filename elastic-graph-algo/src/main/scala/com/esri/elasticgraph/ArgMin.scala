package com.esri.elasticgraph

/**
  * Class to host an object and its min (most of the time) weight value, in such it can be compared to another ArgMin instance.
  *
  * @param arg   the instance to hold.
  * @param min   the instance min weight.
  * @param index an optional index position.
  * @tparam T the instance type.
  */
case class ArgMin[T](arg: T, min: Double, index: Int = 0) extends Ordered[ArgMin[T]] {
  override def compare(that: ArgMin[T]) = this.min compareTo that.min
}
