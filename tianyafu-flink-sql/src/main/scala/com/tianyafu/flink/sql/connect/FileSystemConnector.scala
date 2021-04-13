package com.tianyafu.flink.sql.connect

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.descriptors.{Csv, FileSystem, OldCsv, Schema}
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row

object FileSystemConnector {

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
      .select('domain,'traffic)
      .filter('domain === "ruozedata.com")
//      .toAppendStream[Row].print("-------------API---------")

    tableEnvironment.connect(new FileSystem().path("out"))
      .withFormat(new Csv())
      .withSchema(new Schema()
        .field("domain",DataTypes.STRING())
        .field("traffic",DataTypes.DOUBLE())
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
