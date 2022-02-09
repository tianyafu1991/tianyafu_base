package com.tianyafu.spark.join

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object CoPartitionJoinApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val rdd1: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 5), 2)
    val rdd2: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 6), 2)

    val rdd3: RDD[Int] = rdd1.coalesce(1)
    val rdd4: RDD[Int] = rdd2.coalesce(1)

    val pairedRDD1: RDD[(Int, Int)] = rdd3.map(x => (x,x))
    val pairedRDD2: RDD[(Int, Int)] = rdd4.map(x => (x,x))

    pairedRDD1.join(pairedRDD2).collect.foreach(println)

    Thread.sleep(Int.MaxValue)



    sc.stop()
  }

}
