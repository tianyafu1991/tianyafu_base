package com.tianyafu.spark.app

import com.tianyafu.spark.constants.Constants
import com.tianyafu.spark.template.ETLTemplate
import com.tianyafu.spark.utils.ContextUtils
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

class DemoAppV1 extends ETLTemplate with Logging{

  override def etl(): Unit = {
    println("。。。。etl.......")
    val spark: SparkSession = ContextUtils.get()
    val sc: SparkContext = spark.sparkContext
    import spark.implicits._
    val conf: SparkConf = sc.getConf
    // 从conf中获取一堆的参数
    val hiveDbName: String = conf.get(Constants.SPARK_HIVE_DATABASE, "")
    val numPartitions = conf.get(Constants.SPARK_CUSTOM_NUM_PARTITIONS, "2").toInt
    val targetTableName: String = conf.get(Constants.SPARK_TARGET_TABLE_NAME, "")



    sc.parallelize(List(1,2,3,4,5)).collect().foreach(println)


  }
}
