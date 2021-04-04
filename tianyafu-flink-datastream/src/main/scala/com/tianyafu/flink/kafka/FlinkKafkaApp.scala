package com.tianyafu.flink.kafka


import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}

import java.util.Properties

/**
 * 文档来自官网：https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/connectors/kafka.html
 */
object FlinkKafkaApp {


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment


    /**
     * 消费kafka
     */

    /* val properties = new Properties()
     properties.setProperty("bootstrap.servers", "hadoop01:9092")
     properties.setProperty("group.id", "tianyafu-flink-kafka-11")
     val kafkaSource: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String]("tianyafu11", new SimpleStringSchema(), properties)
 //    kafkaSource.setStartFromEarliest()

     val stream = env
       .addSource(kafkaSource)

     println(stream.parallelism)

     stream.flatMap(_.split(",")).map((_,1)).keyBy(_._1).sum(1).print()*/

    /**
     * 生产消息
     */

    /*val path = "data/access.log"
    val stream: DataStream[String] = env.readTextFile(path)
      .map(x => {
        val splits = x.split(",")
        Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble).toString
      })

    val brokerList = "hadoop01:9092"
    val topic = "tianyafu11"
    val properties = new Properties
    properties.setProperty("bootstrap.servers", "hadoop01:9092")

    val myProducer = new FlinkKafkaProducer[String](
      brokerList,
      topic,
      new SimpleStringSchema())

    stream.addSink(myProducer)*/


    /**
     * 从Kafka 到 Kafka
     */


    val brokerList = "hadoop01:9092"

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", brokerList)
    properties.setProperty("group.id", "tianyafu-flink-kafka-11")
    val kafkaSource: FlinkKafkaConsumer[String] = new FlinkKafkaConsumer[String]("tianyafu11", new SimpleStringSchema(), properties)

    val stream = env
      .addSource(kafkaSource)


    val topic2 = "tianyafu112"
    val myProducer = new FlinkKafkaProducer[String](
      brokerList,
      topic2,
      new SimpleStringSchema())

    stream.addSink(myProducer)




    env.execute(getClass.getSimpleName)
  }


}
