package com.tianyafu.flink.bykey

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._

/**
 * @Author:tianyafu
 * @Date:2021/3/30
 * @Description:
 */
object SpecifyingKeysApp {

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.socketTextStream("hadoop",9527)
        .flatMap(_.split(","))
        .filter(_.nonEmpty)
        .map(WC(_,1))
        .keyBy(_.word)
        .sum("count")
        .print()


    env.execute(getClass.getSimpleName)
  }


  case class WC(word:String,count:Int)

}
