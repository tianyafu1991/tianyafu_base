package com.tianyafu.implicitPackage

import com.tianyafu.rich.RichRDD
import org.apache.spark.rdd.RDD

object ImplicitAspect {

  implicit def rdd2RichRDD[T](rdd: RDD[T]): RichRDD[T] = {
    new RichRDD[T](rdd)
  }

}
