package com.tianyafu.flink.sql.connect

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.descriptors.{Csv, Elasticsearch, FileSystem, Json, Kafka, Schema}
import org.apache.flink.types.Row
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
object ESConnector {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    //创建table的环境
    val tableEnvironment = StreamTableEnvironment.create(env)

    val path = "data/access.log"
    // API形式
    tableEnvironment.connect(new FileSystem().path(path))
      .withFormat(new Csv()) // 这个Csv需要引入专门的依赖
      .withSchema(
        new Schema()
          .field("timestamp",DataTypes.BIGINT())
          .field("domain",DataTypes.STRING())
          .field("traffic",DataTypes.DOUBLE())
      ).createTemporaryTable("access_ods")

    val resultTable = tableEnvironment.from("access_ods")
      .groupBy('domain)
      .aggregate('traffic.sum().as("traffics"))
      .select('domain,'traffics)

    resultTable.toRetractStream[Row].print("..........")

    tableEnvironment.connect(new Elasticsearch()
      .version("7")
      .host("hadoop01",9200,"http")
      .index("tianyafu_access_es")
      .documentType("_doc")
    )
      .inUpsertMode()
      .withFormat(new Json()) // 这个Csv需要引入专门的依赖
      .withSchema(
        new Schema()
          .field("domain",DataTypes.STRING())
          .field("traffics",DataTypes.DOUBLE())
      ).createTemporaryTable("esoutputtable")

    resultTable.executeInsert("esoutputtable")





//    env.execute("")
  }

}
