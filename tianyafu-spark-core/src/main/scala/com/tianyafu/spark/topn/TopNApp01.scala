package com.tianyafu.spark.topn


import com.tianyafu.implicitPackage.ImplicitAspect._
import com.tianyafu.utils.ContextUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object TopNApp01 {

  def main(args: Array[String]): Unit = {
    val sc: SparkContext = ContextUtils.getSparkContext(this.getClass.getSimpleName)

    val path = "data/site.log"
    val lines: RDD[String] = sc.textFile(path)
    val process: RDD[((String, String), Int)] = lines.map(x => {
      val splits: Array[String] = x.split(",")
      val site = splits(0)
      val url = splits(1)
      ((site, url), 1)
    }).reduceByKey(_+_)

    process.printInfo()


    sc.stop()
  }

}
