package com.tianyafu.spark.basic

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}


object BasicApp {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    /*val path = "data/ruozedata.txt"
    val lines = sc.textFile(path)
    val value = lines.flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _)
    value.foreach(println)*/

    /*sc.parallelize(List(1,2,3,4,5),2).mapPartitions(partition => {
      println("这个是一个分区")
      partition.map(x => x*2)
    }).foreach(println)

    sc.parallelize(List(1,2,3,4,5),2).mapPartitionsWithIndex((index,partition) => {
      println("这个是一个分区")
      partition.map(x => s"分区:${index},数据:${x}")
    }).foreach(println)*/

//    val allDataRDD: RDD[Array[Int]] = sc.parallelize(1 to 20, 4).glom()
    /*val rdd1: RDD[Int] = sc.parallelize(1 to 20)
    val rdd2: RDD[Int] = sc.parallelize(1 to 20)

    rdd1.zip(rdd2).foreach(println)

    rdd1.zipWithIndex()*/


    val left: RDD[(String, String)] = sc.makeRDD(List(("a", "A"), ("b", "B"), ("c", "C")))
    val right: RDD[(String, Int)] = sc.makeRDD(List(("a", 1), ("b", 2), ("d", 4)))

    val joinRDD: RDD[(String, (String, Int))] = left.join(right)

    val leftJoinRDD: RDD[(String, (String, Option[Int]))] = left.leftOuterJoin(right)

    val fullJoinRDD: RDD[(String, (Option[String], Option[Int]))] = left.fullOuterJoin(right)

    joinRDD.foreach(println)

    leftJoinRDD.foreach(println)

    fullJoinRDD.foreach(println)


    val cogroupRDD: RDD[(String, (Iterable[String], Iterable[Int]))] = left.cogroup(right)

    cogroupRDD.foreach(println)

    /**
     * Action
     */

    sc.makeRDD(List(1,2,3,4,5)).collect()
    sc.makeRDD(List(1,2,3,4,5)).zipWithIndex().collectAsMap()

    sc.makeRDD(List(1,2,3,4,5)).first()
    sc.makeRDD(List(1,2,3,4,5)).take(3)
    sc.makeRDD(List(1,2,3,4,5)).takeOrdered(3)(Ordering.by(x => -x))
    sc.makeRDD(List(1,2,3,4,5)).top(3)
    sc.makeRDD(List("aa","bb","cc","dd")).map((_,1)).countByKey()
    sc.makeRDD(List("aa","bb","cc","dd")).countByValue()
    sc.makeRDD(List(("a","1"),("b","1"),("a","2"))).lookup("a")
    sc.makeRDD(1 to 100).fold(0)(_+_)



    sc.stop()
  }

}
