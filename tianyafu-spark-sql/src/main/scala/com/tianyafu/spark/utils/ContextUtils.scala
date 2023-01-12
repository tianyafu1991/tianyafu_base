package com.tianyafu.spark.utils

import org.apache.hadoop.fs.FileSystem
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * SparkSession工具类
 */
object ContextUtils {

  private val sparkThreadLocal = new ThreadLocal[SparkSession]()
  private val hdfsThreadLocal = new ThreadLocal[FileSystem]()

  def set(spark: SparkSession): Unit = {
    sparkThreadLocal.set(spark)
    hdfsThreadLocal.set(FileSystem.get(spark.sparkContext.hadoopConfiguration))
  }

  def get(): SparkSession = {
    sparkThreadLocal.get()
  }

  def getFileSystem(): FileSystem = {
    hdfsThreadLocal.get
  }

  def remove(): Unit = {
    get().stop()
    sparkThreadLocal.remove()
    hdfsThreadLocal.remove()
  }

  def getSparkSessionForSupportHive(conf: SparkConf): SparkSession = {
    // 开启动态分区以及非严格模式
    conf.set("hive.exec.dynamic.partition", "true")
    conf.set("hive.exec.dynamic.partition.mode", "nonstrict")
    SparkSession.builder.config(conf).enableHiveSupport.getOrCreate
  }


  def closeSparkSession(spark: SparkSession): Unit = {
    if (null != spark) {
      spark.stop()
    }
  }

}
