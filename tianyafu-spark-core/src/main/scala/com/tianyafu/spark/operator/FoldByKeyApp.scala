package com.tianyafu.spark.operator

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
 *
第一个元素为:13,第二个元素为:2
第一个元素为:15,第二个元素为:9
第一个元素为:24,第二个元素为:42

第一个元素为:13,第二个元素为:4
第一个元素为:17,第二个元素为:15
第一个元素为:13,第二个元素为:23

第一个元素为:66,第二个元素为:36
结果为:(b,32)
结果为:(a,102)
 *
 * foldByKey算子:
 *
 * 1.初值必须与键值对的value值是相同类型的
 * 2.底层还是使用combineByKeyWithClassTag来实现的
 */
object FoldByKeyApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val rdd: RDD[(String, Int)] = sc.parallelize(List(("a", 2), ("a", 9), ("a", 42), ("b", 4), ("b", 15), ("a", 23)), 2)

    val result: RDD[(String, Int)] = rdd.foldByKey(13)((x, y) => {
      println(s"第一个元素为:$x,第二个元素为:$y")
      x + y
    })

    result.collect().foreach(x => {
      println(s"结果为:$x")
    })

    sc.stop()
  }

}
