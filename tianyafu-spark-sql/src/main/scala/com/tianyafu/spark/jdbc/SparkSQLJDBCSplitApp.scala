package com.tianyafu.spark.jdbc

import scala.collection.mutable.ArrayBuffer

case class JDBCPartition(whereClause: String, partitionIndex: Int)

object SparkSQLJDBCSplitApp {

  def main(args: Array[String]): Unit = {
    val numPartitions = 10
    val lowerBound = 1
    val upperBound = 25660023
    val column = "id"
    val numPartitionss =
      if ((upperBound - lowerBound) >= numPartitions || /* check for overflow */
        (upperBound - lowerBound) < 0) {
        numPartitions
      } else {
        upperBound - lowerBound
      }
    println("numPartitionss:"+numPartitionss)

    // Overflow and silliness can happen if you subtract then divide.
    // Here we get a little roundoff, but that's (hopefully) OK.
    val stride: Long = (upperBound / numPartitions - lowerBound / numPartitions)
    println("stride:"+stride)
    var i: Int = 0
    var currentValue: Long = lowerBound
    var ans = new ArrayBuffer[JDBCPartition]()
    while (i < numPartitions) {
      val lowerBound = if (i != 0) s"$column >= $currentValue" else null
      currentValue += stride
      val upperBound = if (i != numPartitions - 1) s"$column < $currentValue" else null
      val whereClause =
        if (upperBound == null) {
          lowerBound
        } else if (lowerBound == null) {
          upperBound
        } else {
          s"$lowerBound AND $upperBound"
        }
      ans += JDBCPartition(whereClause, i)
      i = i + 1
    }
    ans.toArray.map(println(_))
  }

}
