package com.tianyafu.flink.basic

import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._

object StreamingWCApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text: DataStream[String] = env.socketTextStream("hadoop01", 9527)

    text.flatMap(_.split(","))
      .filter(_.nonEmpty)
      .map((_,1))
      .keyBy(_._1)
      .sum(1)
      .print()

    env.execute(getClass.getCanonicalName)
  }

}
