package com.tianyafu.rich

import org.apache.spark.rdd.RDD

class RichRDD[T](val rdd: RDD[T]) {

  def printInfo(): Unit = {
    rdd.collect().foreach(println)
  }
}
