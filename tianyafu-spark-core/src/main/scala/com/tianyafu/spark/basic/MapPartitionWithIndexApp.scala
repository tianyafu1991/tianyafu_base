package com.tianyafu.spark.basic

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object MapPartitionWithIndexApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val rdd1: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 2, 5))
    val rdd2: RDD[Int] = sc.parallelize(List(1, 2, 3, 6, 2, 8))
    rdd1.intersection(rdd2).collect()

    sc.parallelize(List(1, 2, 3, 4, 2, 5)).map((_,null)).cogroup(sc.parallelize(List(1, 2, 3, 6, 2, 8)).map((_,null)))

    sc.parallelize(List(1,2,3,4,5),2)
      .mapPartitionsWithIndex(
        (index,partition) => {
          println("这是一个分区.....")
          partition.map(x => {
            s"当前分区为:${index},数值为:${x}"
          })
        }
      ).foreach(println)




    sc.stop()
  }

}
