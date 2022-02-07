package com.tianyafu.spark.operator

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 *
 * 分区为:0。。。。。。元素为:2
 * 分区为:0。。。。。。元素为:4
 *
 * 分区为:1。。。。。。元素为:8
 * 分区为:1。。。。。。元素为:12
 * 分区为:1。。。。。。元素为:15
 *
 * 结果为:35
 *
 * 初始值为:10
 *
 * seqOp => math.max
 * 分区0:
 * 10 vs 2  => 10
 * 10 vs 4  => 10
 *
 * 分区1:
 * 10 vs 8  => 10
 * 10 vs 12 => 12
 * 12 vs 15 => 15
 *
 * combOp => _ + _
 * 初值 + 分区0:
 * 10 + 10 = 20
 *
 * 初值 + 分区0的结果 + 分区1
 * 20 + 15 = 35
 *
 *
 * 这个算子的初值在seqOp和combOp函数中都要使用
 * 这个算子是一个action算子
 *
 */
object AggregateApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val rdd: RDD[Int] = sc.parallelize(List(2, 4, 8, 12, 15), 2)
    // 打印出每个分区的元素
    /*rdd.mapPartitionsWithIndex((index, partition) => {
      while (partition.hasNext) {
        val value: Int = partition.next()
        println(s"分区为:$index。。。。。。元素为:$value")
      }
      partition
    }).collect()*/

    val result: Int = rdd.aggregate(10)((x, y) => {
      println(s"seqOp函数:第一个元素为:$x,第二个元素为:$y")
      math.max(x, y)
    }, (x, y) => {
      println(s"combOp函数:第一个元素为:$x,第二个元素为:$y")
      x + y
    })

    println(s"结果为:$result")

    sc.stop()
  }

}
