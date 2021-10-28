package com.tianyafu.spark.ser

import com.tianyafu.spark.utils.FileUtils
import org.apache.spark.internal.Logging
import org.apache.spark.{SparkConf, SparkContext, TaskContext}

import java.net.InetAddress

object SerApp extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val textPath = "data/province.txt"
    val outputPath = "out"
    FileUtils.delete(sc.hadoopConfiguration, outputPath)
    //这句在driver端执行 如果ProvinceApp不序列化 就会产生Task not serializable 的报错
    // 如果provinceMap是object 则provinceMap是一个executor一份 如果provinceMap是通过class new出来的 则provinceMap是一个task一份
//    val provinceMap = ProvinceApp
    val provinceMap = new ProvinceClass


    sc.textFile(textPath,2).map(// 在executor端执行
      x => {
      val provinceName: String = provinceMap.provinceMap.getOrElse(x, "-")
      val taskId: Int = TaskContext.getPartitionId()
      val threadId: Long = Thread.currentThread().getId
      val host: String = InetAddress.getLocalHost.getHostName
      (provinceName, taskId, threadId, host, provinceMap.toString())
    }).saveAsTextFile(outputPath)


    sc.stop()
  }


}
