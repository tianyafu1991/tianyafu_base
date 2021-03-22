package com.tianyafu.spark

import org.apache.spark.{SparkConf, SparkContext}

object SparkWCApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val path = "data/ruozedata.txt"

    val lines = sc.textFile(path)

    val value = lines.flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _)


    value.foreach(println)


    sc.stop()
  }

}
