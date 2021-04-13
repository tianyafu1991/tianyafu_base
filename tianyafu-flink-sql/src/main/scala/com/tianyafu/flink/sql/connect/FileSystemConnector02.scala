package com.tianyafu.flink.sql.connect

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.descriptors.{Csv, FileSystem, Schema}
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._

object FileSystemConnector02 {

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

    // TODO 这个使用了聚合的方式，这个写出到CSV格式的文件，是不能写出的  因为CsvTableSink是AppendStreamTableSink，不支持聚合
    val resultTable = tableEnvironment.from("access_ods")
      .groupBy('domain)
      .aggregate('traffic.sum().as("traffics"))
      .select('domain,'traffics)
//      .toAppendStream[Row].print("-------------API---------")

    tableEnvironment.connect(new FileSystem().path("out2"))
      .withFormat(new Csv())
      .withSchema(new Schema()
        .field("domain",DataTypes.STRING())
        .field("traffics",DataTypes.DOUBLE())
      ).createTemporaryTable("fileoutput")

    resultTable.executeInsert("fileoutput")

    // SQL形式
    /*tableEnvironment.sqlQuery(
      """
        |select
        |domain
        |,traffic
        |from
        |access_ods
        |where domain = 'ruozedata.com'
        |""".stripMargin)
      .toAppendStream[Row].print("----SQL形式-----")*/





//    env.execute(getClass.getSimpleName)
  }

}
