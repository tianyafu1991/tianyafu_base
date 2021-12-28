package com.tianyafu.spark.ss.offset

import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.OffsetRange

import scala.collection.mutable

trait OffsetManager[T] {

  /**
   * 从外部存储中获取offset
   * @param topics topics
   * @param groupId Kafka Consumer group id
   * @return
   */
  def obtainOffset(topics: Array[String],groupId:String,conf:SparkConf):mutable.HashMap[TopicPartition, Long]


  /**
   *  将作业的聚合后的结果和本批次Kafka的offset信息一起存储
   * @param offsetRanges 本批次Kafka Message的offset信息
   * @param groupId Kafka Consumer group id
   * @param result 作业的结果集
   * @tparam T
   */
  def storeResultsAndOffset(offsetRanges: Array[OffsetRange],groupId:String,result:T,conf:SparkConf):Unit




}
