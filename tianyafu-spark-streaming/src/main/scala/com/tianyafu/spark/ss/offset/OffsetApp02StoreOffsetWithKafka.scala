package com.tianyafu.spark.ss.offset

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

object OffsetApp02StoreOffsetWithKafka extends Logging{


  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName(getClass.getSimpleName)
      .setMaster("local[4]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    val ssc = new StreamingContext(conf, Seconds(5))

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "mdw:9092,sdw1:9092,sdw2:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG  -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "tyf_ss_group",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val topics = Array("tyfss")
    /**
     * 通过KafkaUtils.createDirectStream获取到的是一手的数据 只有一手的数据才可以获取到offset信息
     */
    val stream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    stream.foreachRDD(rdd => {
      if(!rdd.isEmpty()){
        /**
         * 获取本批次数据的offset信息
         *
         * rdd.asInstanceOf[HasOffsetRanges] 是一个KafkaRDD 要想获取offset信息 必须是一个KafkaRDD
         *
         * 在Driver端执行
         */
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        /**
         * 业务逻辑
         *
         * 在executor中执行
         */
        offsetRanges.foreach(o => println(s"${o.topic} ${o.partition} ${o.fromOffset} ${o.untilOffset}"))
        rdd.flatMap(_.value().split(",")).countByValue().foreach(println)

        /**
         * 提交offset
         * 因为kafka是没有事务的 所以要用这种方式提交offset 则 业务逻辑中的结果输出必须是幂等性的
         * 例如使用Phoenix的upsert语法写入HBase upsert语法是有则更新 没有则插入 可以实现幂等性 此时将offset提交到Kafka自身的topic中没有风险
         * 否则就需要将offset存储在外部存储中
         *
         * 在Driver端执行
         *
         * 至少一次语义
         */
        stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      }else {
        logError("该批次没有数据")
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
