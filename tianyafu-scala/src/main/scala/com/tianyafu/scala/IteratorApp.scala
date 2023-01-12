package com.tianyafu.scala

object IteratorApp {

  def main(args: Array[String]): Unit = {
    val l = List(1, 2, 3, 4, 5)
    val iter1: Iterator[Int] = l.iterator

    val iter2: Iterator[Int] = iter1.map(x => {
      println("iter1中的map算子")
      x + 2
    })

    // 所有的基于迭代器上的算子 都是lazy操作 仅仅只知道自己是从哪来的(即知道依赖关系)
    val iter3: Iterator[Int] = iter2.map(x => {
      println("iter2中的map算子")
      x * 4
    })
    // 必须要执行行动算子 否则不会执行的
    iter3.foreach(println)




  }

}
