package com.tianyafu.spark.ss.kafka2kafka

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object Kafka2KafkaApp extends Logging{

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName(getClass.getSimpleName)
      .setMaster("local[4]")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.registerKryoClasses(Array(classOf[ConsumerRecord[String,String]]))
    val ssc = new StreamingContext(conf, Seconds(1))

    val kafkaConsumerParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "mdw:9092,sdw1:9092,sdw2:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG  -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "tyf_kafka_2_kafka_group",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val KafkaProducerParams = Map[String, Object](
      ProducerConfig.BOOTSTRAP_SERVERS_CONFIG -> "mdw:9092,sdw1:9092,sdw2:9092",
      ProducerConfig.ACKS_CONFIG -> "all",
      //      ProducerConfig.RETRIES_CONFIG -> props.getProperty("kafka1.retries"),
      //      ProducerConfig.BATCH_SIZE_CONFIG -> props.getProperty("kafka1.batch.size"),
      //      ProducerConfig.LINGER_MS_CONFIG -> props.getProperty("kafka1.linger.ms"),
      //      ProducerConfig.BUFFER_MEMORY_CONFIG -> props.getProperty("kafka1.buffer.memory"),
      ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer],
      ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG -> classOf[StringSerializer]
    )


    val topics = Array("tyf_kafka_1")
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaConsumerParams)
    )
    val topic="tyf_kafka_2"
    stream.foreachRDD(rdd => {
      if(!rdd.isEmpty()){
        rdd.foreachPartition(partition => {
          val kafkaProducer: KafkaSink[String, String] = KafkaSink[String, String](KafkaProducerParams)
          partition.foreach(record => {
            kafkaProducer.send(topic,record.key(),record.value())
          })
        })
      }
    })




    ssc.start()
    ssc.awaitTermination()
  }

}
