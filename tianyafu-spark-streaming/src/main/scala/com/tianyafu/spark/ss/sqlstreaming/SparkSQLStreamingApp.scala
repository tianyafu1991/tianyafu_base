package com.tianyafu.spark.ss.sqlstreaming

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkSQLStreamingApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[2]")
    val ssc = new StreamingContext(conf, Seconds(5))
    val lines = ssc.socketTextStream("sdw2", 9527)

    lines.flatMap(_.split(",")).foreachRDD(rdd => {
      if(!rdd.isEmpty()){
        val spark = SparkSession.builder().config(rdd.sparkContext.getConf).getOrCreate()
        import spark.implicits._
        val df = rdd.toDF("word")
        df.createOrReplaceTempView("words")
        spark.sql(
          """
            |select word,count(1) cnt from words group by word
            |""".stripMargin).show(false)

      }
    })









    ssc.start()
    ssc.awaitTermination()

  }

}
