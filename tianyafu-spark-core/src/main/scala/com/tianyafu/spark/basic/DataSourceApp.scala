package com.tianyafu.spark.basic

import com.tianyafu.spark.basic.SparkWCApp.getClass
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object DataSourceApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val rdd: RDD[(String, Int)] = sc.parallelize(List(("zhangsan", 1), ("lisi", 2), ("wangwu", 3)))
    val path = "out"
//    rdd.saveAsTextFile(path)

    rdd.foreachPartition(partition => {

    })

    sc.stop()
  }

}
