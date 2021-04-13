package com.tianyafu.flink.sql.basic

import com.tianyafu.flink.sql.bean.Domain.Access
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.types.Row

object DataStreamTableSQLApp02 {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //创建table的环境
    val tableEnvironment = StreamTableEnvironment.create(env)

    val path = "data/access.log"
    val stream = env.readTextFile(path).map(x => {
      val splits = x.split(",")
      Access(splits(0).trim.toLong,splits(1).trim,splits(2).trim.toDouble)
    })

    // 把流转换成表
    val accessTable: Table = tableEnvironment.fromDataStream(stream)

    // 第一种操作: 使用SQL进行处理
   /* tableEnvironment.createTemporaryView("access",accessTable)
    val resultTable: Table = tableEnvironment.sqlQuery(
      """
        |select domain,sum(traffic) as traffics from access group by domain
        |""".stripMargin)

    resultTable.toRetractStream[Row].filter(_._1).print("=====================")*/

    // 第二种操作：Api操作
    accessTable.groupBy('domain).aggregate('traffic.sum().as("traffics"))
      .select('domain,'traffics)
      .toRetractStream[Row]
      .filter(_._1)
      .print("----api -----")




    env.execute(getClass.getSimpleName)
  }

}
