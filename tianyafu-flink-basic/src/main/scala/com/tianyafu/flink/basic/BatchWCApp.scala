package com.tianyafu.flink.basic

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}
import org.apache.flink.api.scala._
/**
 * 使用flink完成批处理
 */
object BatchWCApp {

  def main(args: Array[String]): Unit = {

    val env = ExecutionEnvironment.getExecutionEnvironment
    val path = "data/ruozedata.txt"
    val text: DataSet[String] = env.readTextFile(path)
    text
      .flatMap(_.split(","))
      .filter(_.nonEmpty)
      .map((_,1))
      .groupBy(0)
      .sum(1)
      .print()

  }

}
