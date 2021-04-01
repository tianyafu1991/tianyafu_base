package com.tianyafu.flink.transformation

import org.apache.flink.api.common.functions.Partitioner

class TianyafuPartitioner extends Partitioner[String] {
  override def partition(key: String, numPartitions: Int): Int = {

    println("。。。。。。。partitions。。。。。。。。" + numPartitions)

    if(key == "ruozedata.com"){
      0
    }else if (key == "ruoze.ke.qq.com"){
      1
    }else {
      2
    }
  }
}
