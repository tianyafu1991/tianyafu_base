package com.tianyafu.spark.topn

import com.tianyafu.implicitPackage.ImplicitAspect.rdd2RichRDD
import com.tianyafu.utils.ContextUtils
import org.apache.spark.{Partitioner, SparkContext}
import org.apache.spark.rdd.RDD

import scala.collection.mutable


object TopNApp06 extends Serializable {

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

    val sites: Array[String] = process.map(_._1._1).distinct().collect()
    // 通过自定义分区器实现相同site的数据分到同一个分区中
    val sitePartitioner = new SitePartitioner2(sites)

    implicit val ordering = Ordering[(Int,String)].on[(String,String,Int)](x=> {
      (-x._3,x._2)
    })
    // TODO 终极优化 使用repartitionAndSortWithinPartitions算子 高逼格的保持分区有序 并使用treeSet来控制数量 防止内存炸掉
    process.reduceByKey(_+_)
      .map(x=> ((x._1._1,x._1._2,x._2),null))
      .repartitionAndSortWithinPartitions(sitePartitioner)
      .map(_._1)
      .mapPartitions(partition => {
        val treeSet = new mutable.TreeSet[(String,String,Int)]()
        partition.foreach(x => {
          treeSet.add(x)
          if(treeSet.size > 2){
            treeSet.remove(treeSet.last)
          }
        })
        treeSet.iterator
      })
      .printInfo()



    Thread.sleep(Int.MaxValue)

    sc.stop()
  }

}


/**
 * 自定义site的分区器
 * @param sites
 */
class SitePartitioner2(sites:Array[String]) extends Partitioner{

  val siteMap = sites.zipWithIndex.toMap

  override def numPartitions: Int = sites.length

  override def getPartition(key: Any): Int = {
    val site: String = key.asInstanceOf[(String, String,Int)]._1
    siteMap(site)
  }
}
