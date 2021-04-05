package com.tianyafu.flink.window

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object WindowApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

//    countWindow(env)
//    countWindowKeyBy(env)
//    tumblingWindow(env)
//    tumblingWindowKeyBy(env)
//    slidingWindow(env)
    slidingWindowKeyBy(env)



    env.execute(getClass.getSimpleName)
  }


  // 按时间每10秒1个窗口，每5秒滑动一次 带keyBy
  def slidingWindowKeyBy(env: StreamExecutionEnvironment) : Unit  = {
    env.socketTextStream("hadoop01",9527)
      .map(x => {
        val splits = x.split(",")
        (splits(0).trim,splits(1).trim.toInt)
      })
      .keyBy(_._1)
      .timeWindowAll(Time.seconds(10),Time.seconds(5))
      .sum(1).print()
  }



  // 按时间每10秒1个窗口，每5秒滑动一次 不带keyBy
  def slidingWindow(env: StreamExecutionEnvironment) : Unit  = {
    env.socketTextStream("hadoop01",9527)
      .map(_.trim.toInt)
      .timeWindowAll(Time.seconds(10),Time.seconds(5))
      .sum(0).print()
  }



  // 按时间滚动 带keyBy
  def tumblingWindowKeyBy(env: StreamExecutionEnvironment) : Unit  = {
    env.socketTextStream("hadoop01",9527)
      .map(x => {
        val splits = x.split(",")
        (splits(0).trim,splits(1).trim.toInt)
      })
      .keyBy(_._1)
      .timeWindowAll(Time.seconds(5))
      .sum(1).print()
  }
  // 按时间滚动 不带keyBy
  def tumblingWindow(env: StreamExecutionEnvironment) : Unit  = {
    env.socketTextStream("hadoop01",9527)
      .map(_.trim.toInt)
      .timeWindowAll(Time.seconds(5))
      .sum(0).print()
  }

  // 按时间个数滚动 不带keyBy
  def countWindow(env:StreamExecutionEnvironment):Unit = {
    env.socketTextStream("hadoop01",9527)
      .map(_.trim.toInt)
      .countWindowAll(5)
      .sum(0).print()
  }


  // spark,1
  // flink,1
  // 按时间个数滚动 带keyBy
  def countWindowKeyBy(env:StreamExecutionEnvironment):Unit = {
    env.socketTextStream("hadoop01",9527)
      .map(x => {
        val splits = x.split(",")
        (splits(0).trim,splits(1).trim.toInt)
      })
      .keyBy(_._1)
      .countWindow(5) // 分组后的5条数据作为一个window
      .sum(1)
      .print()
  }

}
