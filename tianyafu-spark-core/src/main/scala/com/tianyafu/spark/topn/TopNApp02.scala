package com.tianyafu.spark.topn

import com.tianyafu.utils.ContextUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

object TopNApp02 {

  def main(args: Array[String]): Unit = {
    val sc: SparkContext = ContextUtils.getSparkContext(this.getClass.getSimpleName)

    val path = "data/site.log"
    val lines: RDD[String] = sc.textFile(path)
    val process: RDD[((String, String), Int)] = lines.map(x => {
      val splits: Array[String] = x.split(",")
      val site = splits(0)
      val url = splits(1)
      ((site, url), 1)
    })
    // 先将所有的site通过distinct获取到 形成一个Array
    val sites: Array[String] = process.map(_._1._1).distinct().collect()
    // 通过map作用到每个site上，从process中过滤出对应的site后，对每个site做reduceByKey后，做排序取TopN
    // TODO 暂时不理解sortBy为什么会产生job  难道sortBy既是Transformation又是Action
    sites.map(x=> {
      process
        .filter(_._1._1 == x)
        .reduceByKey(_+_)
        .sortBy(-_._2)
        .take(2)
        .foreach(println)
      println(".............")
    })

    Thread.sleep(Integer.MAX_VALUE)

    sc.stop()
  }

}
