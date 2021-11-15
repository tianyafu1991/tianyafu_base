package com.tianyafu.spark.t

import org.apache.spark.sql.catalyst.catalog.CatalogTypes.TablePartitionSpec

object TablePartitionSpecApp {

  def main(args: Array[String]): Unit = {


    val specs: List[TablePartitionSpec] = List(Map("partition_day" -> "20211103")
      , Map("partition_day" -> "20211104")
      , Map("partition_day" -> "20211107")
      , Map("partition_day" -> "20211105")
      , Map("partition_day" -> "20211108")
      , Map("partition_day" -> "20211106")
      , Map("partition_day" -> "20211109")
    )

    val stringToStrings: List[Map[String, String]] = specs.map(x => {

      val tuples: Set[(String, String)] = x.keySet.map(key => {
        val value = x.getOrElse(key, "")
        (value, key)
      })
      tuples.toMap
    })

    println(specs)
    println(stringToStrings)
  }

}
