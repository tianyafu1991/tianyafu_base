package com.tianyafu.flink.wm

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.sql.Timestamp

object WMApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)



    env.execute(getClass.getSimpleName)
  }

  /**
   *
   *
   * 单词计数
   *
   * 时间字段,单词,次数
   * @param env
   */
  def sessionWindow(env: StreamExecutionEnvironment) : Unit = {
    env.socketTextStream("hadoop01",9527).assignTimestampsAndWatermarks(
      new BoundedOutOfOrdernessTimestampExtractor[String](Time.seconds(0)) {
        //获取到数据中的时间
        override def extractTimestamp(element: String): Long = element.split(",")(0).trim.toLong
      }
    ).map(
      x => {
        val splits = x.split(",")
        (splits(1).trim,splits(2).trim.toInt)
      }
    ).keyBy(_._1)
      .window(EventTimeSessionWindows.withGap(Time.seconds(5)))
      //这里采用先增量聚合 最后再汇总
      .reduce(new ReduceFunction[(String, Int)] {
        override def reduce(value1: (String, Int), value2: (String, Int)): (String, Int) = {
          (value1._1,value1._2 + value2._2)
        }
      },new ProcessWindowFunction[(String, Int),String,String,TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {

          for(ele <- elements) {
            out.collect(new Timestamp(context.window.getStart) + "====>" + new Timestamp(context.window.getEnd) + "," + ele._1 + "--->" + ele._2)
          }
        }
      }).print()
  }


}
