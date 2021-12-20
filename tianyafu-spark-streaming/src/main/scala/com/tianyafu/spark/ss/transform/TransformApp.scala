package com.tianyafu.spark.ss.transform

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ListBuffer

object TransformApp {


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5))

    // 黑名单
    val blacks = List("tianyafu")
    val blacksRDD: RDD[(String, Boolean)] = ssc.sparkContext.parallelize(blacks).map((_, true))

    // 流数据是：
    // tianyafu,95535
    // tianya,65587
    // 第一条数据是黑名单中的数据 要去掉
    val lines = ssc.socketTextStream("sdw2", 9527)

    lines.map(x => {
      val splits = x.split(",")
      // 先搞成k v 类型的
      (splits(0),x)
    }).transform(rdd => {
      val joinedRDD: RDD[(String, (String, Option[Boolean]))] = rdd.leftOuterJoin(blacksRDD)
      joinedRDD.filter(x=> {
        !x._2._2.getOrElse(false)
      }).map(_._2._1)
    }).print()







    ssc.start()
    ssc.awaitTermination()
  }


}
