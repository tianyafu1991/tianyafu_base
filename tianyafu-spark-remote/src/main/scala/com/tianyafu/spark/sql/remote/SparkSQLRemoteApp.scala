package com.tianyafu.spark.sql.remote

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkSQLRemoteApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf
//    conf.set("spark.hive.enable", "true")
//    conf.set("spark.sql.hive.metastore.version","2.3")
//    conf.set("spark.sql.hive.metastore.jars","path")
    // 显示所有的执行计划
    conf.set("spark.sql.ui.explainMode", "extended")

    val spark = SparkSession
      .builder()
      .config(conf)
      .master("local[1]")
      .enableHiveSupport()
      .getOrCreate()

    spark.sparkContext.setLogLevel("INFO")

    // 支持方式
    val sql =
      """
        | select
        |     *
        | from
        |     test.t_name
        |""".stripMargin

    val df = spark.sql(sql)

    df.show()

    System.in.read()
  }

}
