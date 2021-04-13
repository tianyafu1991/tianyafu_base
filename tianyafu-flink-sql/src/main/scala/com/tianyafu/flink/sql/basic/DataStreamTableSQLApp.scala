package com.tianyafu.flink.sql.basic

import com.tianyafu.flink.sql.bean.Domain.Access
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
// Flink table SQL 编程，第一件事就是导入这3个
import org.apache.flink.api.scala._
import org.apache.flink.table.api.bridge.scala._
import org.apache.flink.table.api._

import org.apache.flink.types.Row

object DataStreamTableSQLApp {

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
    /*tableEnvironment.createTemporaryView("access",accessTable)
    val resultTable: Table = tableEnvironment.sqlQuery(
      """
        |select * from access where domain = 'ruozedata.com'
        |""".stripMargin)

    // 再把表转成流进行输出
    //第一种方式
    tableEnvironment.toAppendStream[Row](resultTable).print("--------1-------")
    // 第二种方式
    tableEnvironment.toAppendStream[(Long,String,Double)](resultTable).print("---------2------")
    // 第三种方式
    resultTable.toAppendStream[Row].print("--------3-------")*/

    // 第二种操作: 使用API进行处理
    accessTable.select("*").toAppendStream[Row].print("--------所有字段-------")
    accessTable.select($"domain").toAppendStream[Row].print("--------单个字段写法-------")
    accessTable.select('domain).toAppendStream[Row].print("--------单个字段推荐写法-------")
    accessTable.select('domain,'traffic).toAppendStream[Row].print("--------多个字段推荐写法-------")



    env.execute(getClass.getSimpleName)
  }

}
