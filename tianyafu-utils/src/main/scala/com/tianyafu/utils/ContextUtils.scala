package com.tianyafu.utils

import org.apache.spark.{SparkConf, SparkContext}

object ContextUtils {

  /**
   * 获取SparkContext
   *
   * @param appName 应用名称
   * @param master  master
   * @return
   */
  def getSparkContext(appName: String, master: String = "local[2]"): SparkContext = {
    val conf: SparkConf = new SparkConf().setMaster(master).setAppName(appName)
    new SparkContext(conf)
  }

  /**
   * 通过SparkConf 获取 SparkContext
   * @param conf
   * @return
   */
  def getSparkContext(conf: SparkConf): SparkContext = {
    new SparkContext(conf)
  }

  /**
   * 获取SparkConf
   * @param appName
   * @param master
   * @return
   */
  def getSparkConf(appName: String, master: String = "local[2]"): SparkConf = {
    new SparkConf().setMaster(master).setAppName(appName)
  }
}
