package com.tianyafu.spark.utils

import com.tianyafu.spark.constants.Constants
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * SparkSession工具类
 */
object ContextUtils {

  private val sparkThreadLocal = new ThreadLocal[SparkSession]()

  def set(spark : SparkSession ): Unit = {
    sparkThreadLocal.set(spark)
  }

  def get(): SparkSession = {
    val spark = sparkThreadLocal.get()
    spark
  }

  def remove(): Unit = {
    sparkThreadLocal.remove()
  }

  /**
   * 获取SparkSession
   *
   * @param conf
   * @return
   */
  def getSparkSession(conf: SparkConf): SparkSession = {
    var builder: SparkSession.Builder = SparkSession.builder.config(conf)
    if (!conf.get(Constants.SPARK_LOCAL_FLAG,"true").toBoolean) builder = builder.enableHiveSupport
    builder.getOrCreate
  }

  def getSparkSessionForSupportHive(conf: SparkConf): SparkSession = {
    conf.set("hive.exec.dynamic.partition", "true")
    conf.set("hive.exec.dynamic.partition.mode", "nonstrict")
    SparkSession.builder.config(conf).enableHiveSupport.getOrCreate
  }


  def closeSparkSession(spark:SparkSession): Unit ={
    if(null != spark){
      spark.stop()
    }
  }

}
