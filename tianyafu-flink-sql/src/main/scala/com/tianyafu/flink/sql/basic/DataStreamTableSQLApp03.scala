package com.tianyafu.flink.sql.basic

import com.tianyafu.flink.sql.bean.Domain.Access
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row

object DataStreamTableSQLApp03 {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //创建table的环境
    val tableEnvironment = StreamTableEnvironment.create(env)
    val stream = env.fromElements("ruoze,ruoze,ruoze","pk,pk","xingxing")
      .flatMap(_.split(",")).map(WC(_))

    // SQL 的写法 把流转换成表
    /*val wcTable: Table = tableEnvironment.fromDataStream(stream)

    tableEnvironment.sqlQuery(
      s"""
        |select
        |word,count(word)
        |from ${wcTable}
        |group by word
        |""".stripMargin)
      .toRetractStream[Row]
      .filter(_._1)
      .print()*/


    // API 写法
    tableEnvironment.fromDataStream(stream,'word)
      .groupBy('word)
      .select('word,'word.count())
      .toRetractStream[Row]
      .filter(_._1)
      .print()





    env.execute(getClass.getSimpleName)
  }

}

case class WC(word:String)
