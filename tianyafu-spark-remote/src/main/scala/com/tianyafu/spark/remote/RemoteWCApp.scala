package com.tianyafu.spark.remote

import org.apache.spark.{SparkConf, SparkContext}

/**
 * 远程调试Spark代码
 */
object RemoteWCApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
    val sc = new SparkContext(conf)

    println("tianyafu@SparkContext启动成功。。。。。。。。。。。。。。。。。")

    val path: String = conf.get("spark.data.input.path", "hdfs:///tmp/tianyafu/tianyafu.txt")
    sc.textFile(path).flatMap(_.split(",")).map((_,1)).reduceByKey(_+_).collect().foreach(println)

    System.in.read()


    sc.stop()
  }

}
