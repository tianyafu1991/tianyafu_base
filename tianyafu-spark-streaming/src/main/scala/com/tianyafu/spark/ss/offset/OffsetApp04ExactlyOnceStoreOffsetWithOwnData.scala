package com.tianyafu.spark.ss.offset

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * 精确一次消费语义
 * offset存储在外部存储上
 *
 * 该示例是最终结果为聚合后的结果  可以认为结果不大  可以拉取到Driver端后 将结果同offset在同一个事务中写入到MySQL中
 */
object OffsetApp04ExactlyOnceStoreOffsetWithOwnData extends Logging {




  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName(getClass.getSimpleName)
      .setMaster("local[6]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    val ssc = new StreamingContext(conf, Seconds(5))

    val groupId = "tyf_ss_store_offset_in_mysql_group"

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "mdw:9092,sdw1:9092,sdw2:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val topics: Array[String] = Array("tyfss", "tyf_kafka_1")

    // 从MySQL中获取offset信息
    val fromOffsets = MySQLOffsetManager.obtainOffset(topics, groupId,conf)

    for (elem <- fromOffsets) {
      println(s"map:${elem._1}.......${elem._2}")
    }

    /**
     * 通过KafkaUtils.createDirectStream获取到的是一手的数据 只有一手的数据才可以获取到offset信息
     */
    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams, fromOffsets)
    )

    var offsetRanges: Array[OffsetRange] = null

    /**
     * 获取本批次数据的offset信息
     *
     * rdd.asInstanceOf[HasOffsetRanges] 是一个KafkaRDD 要想获取offset信息 必须是一个KafkaRDD
     *
     * 在Driver端执行
     */
    val transformDStream: DStream[ConsumerRecord[String, String]] = stream.transform(rdd => {
      if (!rdd.isEmpty()) {
        offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
        offsetRanges.foreach(o => println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}"))
      }
      rdd
    })


    transformDStream.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        // 业务逻辑
        val aggResult: Array[(String, Long)] = rdd.flatMap(_.value().split(",")).map((_, 1L)).reduceByKey(_ + _).collect()
        // 只是个打印
        aggResult.foreach(result => println(s"word:${result._1},cnt:${result._2}"))

        /**
         * 作业的聚合后的结果和本批次Kafka的offset信息一起存储
         */
        MySQLOffsetManager.storeResultsAndOffset(offsetRanges, groupId, aggResult)
      } else {
        logError("该批次没有数据")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
