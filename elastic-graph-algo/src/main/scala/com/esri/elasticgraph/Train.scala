package com.esri.elasticgraph

import scala.annotation.tailrec

/**
  * The training process.
  *
  * @param datum        the input data.
  * @param param        the learning parameters.
  * @param iterListener the iteration listener.
  */
case class Train(datum: Array[DataXY], param: Param, iterListener: IterListener = NoopListener()) {

  /**
    * Recursive training process. Stop when the input graph has max nodes.
    *
    * If the input graph has less than max nodes, it is mapped to a set of sub graphs.
    * A sub graph is based on edge split and add node grammars.
    * The sub graph with the smallest energy is selected as the input to the next learning process.
    *
    * @param graphOld the input graph.
    * @return Graph instance with `param.maxNodes` nodes.
    */
  @tailrec
  private def trainRec(graphOld: Graph): Graph = {
    iterListener.onGraph(graphOld)
    if (graphOld.numNodes == param.maxNodes) {
      graphOld
    }
    else {
      val graphNew = graphOld
        .subGraphs(param, datum)
        .par
        .map(Optimize(param, datum, _).optimize())
        .filter(_.arg.isValid(param))
        .min
        .arg
      trainRec(graphNew)
    }
  }

  /**
    * Start the learning process given two nodes.
    *
    * @param node1 first node.
    * @param node2 second node.
    * @return Graph instance.
    */
  def train(node1: Node, node2: Node): Graph = {
    trainRec(Graph(node1, node2))
  }
}
