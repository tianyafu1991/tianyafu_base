package com.tianyafu.spark.basic

import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapred.TextInputFormat
import org.apache.spark.{SparkConf, SparkContext}

import java.io.{File, FileOutputStream, OutputStreamWriter}

object GBKApp {

  def main(args: Array[String]): Unit = {
    //    generateGBKFile
    val conf: SparkConf = new SparkConf().setMaster("local").setAppName("GBKApp")
    val sc = new SparkContext(conf)

    val path = "data/GBK.txt"
    readGBKUseSparkContext(sc, path)



//    sc.stop()
  }

  def readGBKUseSparkContext(sc: SparkContext, path: String): Unit = {
    sc.hadoopFile(path,classOf[TextInputFormat],classOf[LongWritable],classOf[Text],1)
      .map(p => new String(p._2.getBytes, 0, p._2.getLength, "GBK")).foreach(println)
  }

  def generateGBKFile: Unit = {
    val writer = new OutputStreamWriter(new FileOutputStream(new File("data/GBK.txt")), "GBK")
    writer.write("你好,张三@2023")
    writer.flush()
    writer.close()
  }
}
