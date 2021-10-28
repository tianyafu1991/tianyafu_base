package com.tianyafu.spark.ser

import com.tianyafu.spark.ser.SerApp.getClass
import com.tianyafu.spark.utils.{DateUtils, FileUtils}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object ETLApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
    val sc = new SparkContext(conf)

    val textPath = "data/ck.log"
    val outputPath = "out"
    FileUtils.delete(sc.hadoopConfiguration, outputPath)

    val logs: RDD[String] = sc.textFile(textPath, 2)

    /*logs.map(x => {
      (x,DateUtils.parse(x))
    }).foreach(println)*/

    logs.mapPartitions(partition => {
      val format: FastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
      partition.map(x => {
        (x,format.parse(x).getTime)
      })
    }).foreach(println)

    sc.stop()
  }

}
