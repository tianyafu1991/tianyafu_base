package com.tianyafu.spark.listener

import com.tianyafu.utils.ContextUtils
import org.apache.spark.internal.Logging

object MonitorApp extends Logging{


  def main(args: Array[String]): Unit = {
    val conf = ContextUtils.getSparkConf(getClass.getSimpleName)
    conf.set("spark.extraListeners","com.tianyafu.spark.listener.TianYaFuSparkListerApp")
    val sc = ContextUtils.getSparkContext(conf)
    sc.parallelize(List(1,2,3,4,5,3,2)).map((_,1)).reduceByKey(_+_).foreach(println)



    sc.stop()
  }

}
