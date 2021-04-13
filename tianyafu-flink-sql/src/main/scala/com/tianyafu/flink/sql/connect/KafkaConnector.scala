package com.tianyafu.flink.sql.connect

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.descriptors.{Csv, FileSystem, Kafka, Schema}
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._

object KafkaConnector {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    //创建table的环境
    val tableEnvironment = StreamTableEnvironment.create(env)

    val path = "data/access.log"
    // API形式
    tableEnvironment.connect(new Kafka()
      .version("0.10")
      .topic("flinktopic")
      .property("bootstrap.servers", "hadoop01:9092")
    )
      .withFormat(new Csv()) // 这个Csv需要引入专门的依赖
      .withSchema(
        new Schema()
          .field("timestamp",DataTypes.BIGINT())
          .field("domain",DataTypes.STRING())
          .field("traffic",DataTypes.DOUBLE())
      ).createTemporaryTable("kafkatable")

    val resultTable = tableEnvironment.from("kafkatable")
      .select('domain,'traffic)
      .filter('domain === "ruozedata.com")
//      .toAppendStream[Row].print("-------------API---------")

    tableEnvironment.connect(new Kafka()
      .version("0.10")
      .topic("flinktopictest")
      .property("bootstrap.servers", "hadoop01:9092")
    )
      .withFormat(new Csv()) // 这个Csv需要引入专门的依赖
      .withSchema(
        new Schema()
          .field("domain",DataTypes.STRING())
          .field("traffic",DataTypes.DOUBLE())
      ).createTemporaryTable("kafkaoutputtable")

    resultTable.executeInsert("kafkaoutputtable")





    env.execute("")
  }

}
