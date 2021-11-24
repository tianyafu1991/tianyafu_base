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

    process.groupBy(_._1._1).flatMapValues(
      x=> {
        // 先将compactBuffer转成list 对list中的元素的第二位(即次数)进行倒序排序 然后取前2个
        // TODO 这种方式有弊端 如groupBy之后的某个compactBuffer 元素非常多 toList会导致内存炸掉 toList和toSet这种操作都是要慎用的
        x.toList.sortBy(-_._2).map(x=> (x._1._2,x._2)).take(2)
      }
    ).map(x=> (x._1,x._2._1,x._2._2)).printInfo()


    sc.stop()
  }

}
