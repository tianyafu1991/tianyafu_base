package com.tianyafu.spark.basic

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TakeApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val numRDD: RDD[Int] = sc.parallelize(1 to 20, 10)

    numRDD.mapPartitionsWithIndex((index,partition) => {
      partition.map(x => {
        println(s"当前分区为${index},x值为${x}")
        x
      })
    }).take(4).foreach(println)

    Thread.sleep(Int.MaxValue)
    sc.stop()
  }

}
