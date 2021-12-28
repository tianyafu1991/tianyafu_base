package com.tianyafu.spark.ss.offset


import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

case class Sales(id: String, name: String, money: Double)

object HBaseOffsetApp extends Logging {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName(getClass.getSimpleName)
      .setMaster("local[4]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    // 手动写死一个topic 方便调试
    conf.set("spark.streaming.kafka.topic","hbase_ss_1")

    val ssc = new StreamingContext(conf, Seconds(5))

    val groupId = conf.get("spark.streaming.kafka.consumer.group.id", "tyf_ss_kafka_2_hbase_group")
    val topic = conf.get("spark.streaming.kafka.topic", "hbase_ss")

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "mdw:9092,sdw1:9092,sdw2:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val topics = topic.split(",")

    // 从外部存储中获取offset
    val fromOffsets: mutable.HashMap[TopicPartition, Long] = HBaseOffsetManager.obtainOffset(topics, groupId, conf)
    fromOffsets.foreach(x => {
      println(s"!!!!${x._1}!!!!!!!!!!!!！！！！！！！！！！！${x._2}！！！！！！")
    })

    /**
     * 通过KafkaUtils.createDirectStream获取到的是一手的数据 只有一手的数据才可以获取到offset信息
     */
    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams, fromOffsets)
    )

    stream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        /**
         * 获取本批次数据的offset信息
         *
         * rdd.asInstanceOf[HasOffsetRanges] 是一个KafkaRDD 要想获取offset信息 必须是一个KafkaRDD
         */
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        offsetRanges.foreach(o => println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}"))
        /**
         * 一堆业务逻辑代码
         */
        val result: RDD[Sales] = rdd.map(x => {
          val splits: Array[String] = x.value().split(",")
          if (splits.length == 3) {
            Sales(splits(0).trim, splits(1).trim, splits(2).trim.toDouble)
          } else {
            Sales("0", "0", 0.0)
          }
        }).filter(_.id != "0")

        /**
         * 存储结果和offset信息到HBase
         */
        HBaseOffsetManager.storeResultsAndOffset(offsetRanges, groupId, result, conf)
      } else {
        logError("该批次没有数据")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }


}
