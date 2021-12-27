package com.tianyafu.spark.ss.utils

import com.tianyafu.spark.ss.offset.HBaseOffsetManager
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HBaseConfiguration, HConstants}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.apache.spark.SparkConf

object HBaseUtils {


  def getConnection(zk:String,port:Int):Connection = {
    val conf: Configuration = HBaseConfiguration.create()
    conf.set(HConstants.ZOOKEEPER_QUORUM,zk)
    conf.set(HConstants.ZOOKEEPER_CLIENT_PORT,port.toString)
    ConnectionFactory.createConnection(conf)
  }



  def main(args: Array[String]): Unit = {
    val topics = Array("hbase_ss")
    val groupId = "tyf_ss_kafka_2_hbase_group"
    HBaseOffsetManager.obtainOffset(topics,groupId,new SparkConf())
  }
}
