package com.tianyafu.scala

/**
 * aggregate 这个方法的combOp函数  没有使用 因为scala 的List 没有分区
 */
object AggregateApp {

  def main(args: Array[String]): Unit = {
    val list = List(2, 18, 13, 12, 15)
    val result: Int = list.aggregate(10)((x, y) => {
      println(s"seqOp函数:第一个元素为:$x,第二个元素为:$y")
      math.max(x, y)
    }, (x, y) => {
      println(s"combOp函数:第一个元素为:$x,第二个元素为:$y")
      x + y
    })
    println(result)
  }

}
