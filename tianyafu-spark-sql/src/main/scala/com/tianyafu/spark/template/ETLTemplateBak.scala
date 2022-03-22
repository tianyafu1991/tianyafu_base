package com.tianyafu.spark.template

import com.tianyafu.spark.utils.ContextUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * 模板模式封装的ETLTemplate
 */
trait ETLTemplateBak {

  def setup(): Unit = {
    println("set up...................")
    val conf = new SparkConf()
    val spark: SparkSession = ContextUtils.getSparkSessionForSupportHive(conf)
    ContextUtils.set(spark)
  }
  def etl()
  def cleanup(): Unit = {
    println("clean up...................")
    ContextUtils.get().stop()
    ContextUtils.remove()
  }
}
