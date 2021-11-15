package com.tianyafu.spark.app

import com.tianyafu.spark.template.ETLTemplateV2
import com.tianyafu.spark.utils.ContextUtils
import org.apache.spark.sql.SparkSession

object DemoAppV2 extends App with ETLTemplateV2{
  etl(){
    val spark: SparkSession = ContextUtils.get()
    spark.sql(
      """
        |show databases
        |""".stripMargin).show(false)
  }
}
