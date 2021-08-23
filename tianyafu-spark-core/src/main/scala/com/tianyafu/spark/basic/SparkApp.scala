package com.tianyafu.spark.basic

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkApp {

  def main(args: Array[String]): Unit = {
    val conf:SparkConf = new SparkConf().setMaster("local").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    sc.parallelize(List(
      "100000,一起看|电视剧|军旅|士兵突击,1,1",
      "100000,一起看|电视剧|军旅|士兵突击,1,0",
      "100001,一起看|电视剧|军旅|我的团长我的团,1,1"
    )).flatMap(x => {
      val splits: Array[String] = x.split(",")
      val id: String = splits(0)
      val nav: String = splits(1)
      val imp: Int = splits(2).toInt
      val click: Int = splits(3).toInt
      val navs: Array[String] = nav.split("\\|")
      navs
        .map(x => ((id,x),(imp,click)))
    }).reduceByKey((x,y) => (x._1+y._1,x._2 + y._2))
      .foreach(println)






    sc.stop()
  }

}
