package com.tianyafu.spark.basic

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object TakeSampleApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)
    val numRDD: RDD[Int] = sc.parallelize(1 to 200, 5)

    numRDD.takeSample(false,2,10).foreach(println)

    Thread.sleep(Int.MaxValue)


    sc.stop()
  }

}
