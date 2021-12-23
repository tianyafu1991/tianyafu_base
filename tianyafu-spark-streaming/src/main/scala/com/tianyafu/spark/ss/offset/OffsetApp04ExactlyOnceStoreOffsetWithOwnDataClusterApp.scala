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
 *
 * 服务器上通过spark-submit脚本提交
 */
object OffsetApp04ExactlyOnceStoreOffsetWithOwnDataClusterApp extends Logging {


  def generateSelectOffsetSql(selectOffsetSqlPrefix: String, selectOffsetSqlSuffix: String, topics: Array[String]): String = {
    var selectOffsetSql = selectOffsetSqlPrefix
    // 有多少topic 就要拼多少 ?
    topics.map(x => {
      selectOffsetSql = selectOffsetSql + "?,"
    })
    // 去除最后的,
    if (selectOffsetSql.endsWith(",")) {
      selectOffsetSql = selectOffsetSql.dropRight(1)
    }
    // 加上最后一个括号
    selectOffsetSql + selectOffsetSqlSuffix
  }

  def main(args: Array[String]): Unit = {

    if (args.length < 4) {
      System.err.println(
        s"""
        |Usage: OffsetApp04ExactlyOnceStoreOffsetWithOwnDataClusterApp <batch> <brokers> <groupId> <topics>
        |  <batch> is a batch time of spark streaming
        |  <brokers> is a list of one or more Kafka brokers
        |  <groupId> is a consumer group name to consume from topics
        |  <topics> is a list of one or more kafka topics to consume from
        |
        """.stripMargin)
      System.exit(1)
    }

    val Array(batch,brokers, groupId, topic) = args

    logError(s"接收到的参数为:batch:${batch} , brokers:${brokers} , groupId:${groupId} , topic:${topic}")


    val conf: SparkConf = new SparkConf()
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String, String]]))
    val ssc = new StreamingContext(conf, Seconds(batch.toInt))


    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokers,
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> groupId,
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )


    val topics: Array[String] = topic.split(",")


    val selectOffsetSqlPrefix = "select * from streaming_offset_stored where group_id = ? and topic in ("
    val selectOffsetSqlSuffix = ")"
    // 生成查询offset的sql
    val selectOffsetSql = generateSelectOffsetSql(selectOffsetSqlPrefix, selectOffsetSqlSuffix, topics)
    logError(s"从数据库中查询的sql语句为:${selectOffsetSql}")
    // 从MySQL中获取offset信息
    val fromOffsets = MySQLOffsetManager.obtainOffset(topics, groupId, selectOffsetSql)

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
