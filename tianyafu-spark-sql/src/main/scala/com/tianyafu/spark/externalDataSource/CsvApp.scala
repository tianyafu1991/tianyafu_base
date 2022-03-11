package com.tianyafu.spark.externalDataSource

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * data/csv_demo1.csv数据来源于:
 * https://cloud.tencent.com/developer/ask/sof/806369
 * 并稍加修改
 *
 * CSV相关的Issue:
 * https://issues.apache.org/jira/browse/SPARK-24540
 * https://issues.apache.org/jira/browse/SPARK-19610
 *
 * Data Source V2相关的Issue:
 * https://issues.apache.org/jira/browse/SPARK-15689
 * https://issues.apache.org/jira/browse/SPARK-20960
 *
 * JSON相关的Issue:
 * https://issues.apache.org/jira/browse/SPARK-13764
 * https://issues.apache.org/jira/browse/SPARK-18352
 */
object CsvApp {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()

    val path = "data/csv_demo1.csv"

    val df: DataFrame = getDF1(spark,path)

    df.printSchema()

    df.show(20, false)


    spark.stop()
  }

  def getDF2(spark: SparkSession, path: String): DataFrame = {
    spark
      .read
      .format("csv")
      .option("sep", ",")
      .option("encoding", "UTF-8")
      .option("header", "true")
//      .option("quote", "\"")
//      .option("escape", "\"")
//      .option("ignoreLeadingWhiteSpace", "true")
//      .option("ignoreTrailingWhiteSpace", "true")
      .option("mode","PERMISSIVE")
      .load(path)
  }


  def getDF1(spark: SparkSession, path: String): DataFrame = {
    spark
      .read
      .format("csv")
      .option("sep", ",")
      .option("encoding", "UTF-8")
      .option("header", "true")
      .option("quote", "\"")
      .option("escape", "\"")
      .option("ignoreLeadingWhiteSpace", "true")
      .option("ignoreTrailingWhiteSpace", "true")
      .option("multiLine", "true")
      .load(path)
  }

}
