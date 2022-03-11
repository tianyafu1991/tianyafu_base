package com.tianyafu.spark.join

import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

object CoPartitionJoinApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val rdd1: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 5), 3)
    val rdd2: RDD[Int] = sc.parallelize(List(1, 2, 3, 4, 6), 3)

    val pairedRDD1: RDD[(Int, Int)] = rdd1.map(x => (x,x))
    val pairedRDD2: RDD[(Int, Int)] = rdd2.map(x => (x,x))

    val partitionedRDD1 = pairedRDD1.partitionBy(new HashPartitioner(2))
    val partitionedRDD2 = pairedRDD2.partitionBy(new HashPartitioner(2))

    partitionedRDD1.join(partitionedRDD2).collect.foreach(println)

    Thread.sleep(Int.MaxValue)



    sc.stop()
  }

}
