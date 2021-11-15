package com.tianyafu.spark.template

import com.tianyafu.spark.utils.ContextUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait ETLTemplateV2 {


  def etl(appName : String = "ETLTemplateV2",
          master : String ="local[*]")( biz :  => Unit ): Unit = {
    val conf = new SparkConf()
    conf.setMaster(conf.get("spark.master",master))
    conf.setAppName(conf.get("spark.app.name",appName))
    // TODO 这一行是为了本地连接集群的Hive Metastore
    conf.set("hive.metastore.uris","thrift://cdh-master:9083")
    print(s"该app的name为:${conf.get("spark.app.name")},master为:${conf.get("spark.master")}")
    val spark: SparkSession = ContextUtils.getSparkSessionForSupportHive(conf)
    ContextUtils.set(spark)

    try {
      biz
    } catch {
      case ex => println(ex.getMessage)
    }

    spark.stop()

    ContextUtils.remove()
  }

}
