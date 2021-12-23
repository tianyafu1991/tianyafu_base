package com.tianyafu.spark.ss.offset

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.util.Random

object KafkaProducerDataMock {

  private val logger: Logger = LoggerFactory.getLogger(KafkaProducerDataMock.getClass)

  def main(args: Array[String]): Unit = {

    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "mdw:9092,sdw1:9092,sdw2:9092")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.RETRIES_CONFIG, "100")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384")
    props.put(ProducerConfig.LINGER_MS_CONFIG, "1")
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "1")


    val producer = new KafkaProducer[String, String](props)

    val topic = "tyfss"

    for (i <- 1.to(1000000000)) {
//      Thread.sleep(100)
      val word: String = String.valueOf((new Random().nextInt(6) + 'a').toChar)
      val partition: Int = (i % 3).toInt
      logger.error("word:{}",word)
      val record = new ProducerRecord[String, String](topic, partition, null, word)
      producer.send(record)
    }
    producer.close()

    println("生产者发送完毕......")
  }

}
