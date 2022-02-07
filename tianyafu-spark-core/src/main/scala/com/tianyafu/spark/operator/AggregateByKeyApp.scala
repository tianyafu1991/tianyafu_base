package com.tianyafu.spark.operator

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 *
seqOp函数:当前分区为0,第一个元素为:13,第二个元素为2
seqOp函数:当前分区为0,第一个元素为:15,第二个元素为9
seqOp函数:当前分区为0,第一个元素为:24,第二个元素为42

seqOp函数:当前分区为1,第一个元素为:13,第二个元素为4
seqOp函数:当前分区为1,第一个元素为:17,第二个元素为15
seqOp函数:当前分区为1,第一个元素为:13,第二个元素为23

combOp函数:第一个元素为:66,第二个元素为36
结果为:(b,32)
结果为:(a,102)

分区0:("a", 2), ("a", 9), ("a", 42)
分区1:("b", 4), ("b", 15), ("a", 23)

初值为:13

seqOp函数作用:
seqOp函数是分区内的函数
初值与分区中的相同key的第一个元素 进行seqOp函数运算 得到第一个结果
第一个结果与相同key的第二个元素 进行seqOp函数运算 得到第二个结果
第二个结果与相同key的第三个元素 进行seqOp函数运算 。。。。。。
以此类推得到该key在该分区中的最终结果值
对于相同分区中的相同key的元素 初值只使用一次

combOp函数作用:
combOp函数是分区间的函数
将第一个分区和第二个分区的相同key的seqOp函数的最终结果值 进行combOp函数运算 得到第一个结果
将第一个结果与下一个分区的相同key的seqOp函数的最终结果值 进行combOp函数运算 得到第二个结果
一次类推得到该key的最终结果值

需要注意:初值在combOp函数中没有参与运算
 */
object AggregateByKeyApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val rdd: RDD[(String, Int)] = sc.parallelize(List(("a", 2), ("a", 9), ("a", 42), ("b", 4), ("b", 15), ("a", 23)), 2)

    val result: RDD[(String, Int)] = rdd.mapPartitionsWithIndex((index, partition) => {
      partition.map(x => (x._1, (x._2, index)))
    }).aggregateByKey(13)(
      (x, y) => {
        println(s"seqOp函数:当前分区为${y._2},第一个元素为:$x,第二个元素为${y._1}")
        x + y._1
      }, (x, y) => {
        println(s"combOp函数:第一个元素为:$x,第二个元素为${y}")
        x + y
      }
    )

    result.collect().foreach(x => {
      println(s"结果为:$x")
    })

    sc.stop()
  }

}
