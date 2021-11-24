package com.tianyafu.spark.topn

import com.tianyafu.implicitPackage.ImplicitAspect.rdd2RichRDD
import com.tianyafu.utils.ContextUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

import scala.collection.mutable


object TopNApp05 extends Serializable {

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
    val sitePartitioner = new SitePartitioner(sites)
    // TODO 对这里的toList进行优化 假设我们要取的是分组top 2 则应该每组值保留2个  当第3个进来时  要去除1个  始终只保留2个
    process.reduceByKey(sitePartitioner,_+_).mapPartitions(partition => {
      val treeSet = new mutable.TreeSet[((String,String),Int)]()(Ordering[(Int,String)].on[((String,String),Int)](
        // 这里采用cnt先倒序  如果cnt相同 按照url升序 因cnt才是我们关心的 而同一个partition中 site是相同的，所以只能按照url排序 所以Ordering[(Int,String)] 这里的泛型是(Int,String)
        x => (-x._2,x._1._2)
      ))
      partition.foreach(x => {
        treeSet.add(x)
        if(treeSet.size > 2){
          treeSet.remove(treeSet.last)
        }
      })
      treeSet.iterator
    }).printInfo()

    Thread.sleep(Int.MaxValue)

    sc.stop()
  }

}




/**
 * 自定义site的分区器
 * @param sites
 */
/*class SitePartitioner(sites:Array[String]) extends Partitioner{

  val siteMap = sites.zipWithIndex.toMap

  override def numPartitions: Int = sites.length

  override def getPartition(key: Any): Int = {
    val site: String = key.asInstanceOf[(String, String)]._1
    siteMap(site)
  }
}*/
