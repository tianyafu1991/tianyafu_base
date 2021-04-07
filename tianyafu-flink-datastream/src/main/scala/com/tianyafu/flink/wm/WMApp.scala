package com.tianyafu.flink.wm

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.{OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.function.{ProcessWindowFunction, RichWindowFunction}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.assigners.{EventTimeSessionWindows, SlidingEventTimeWindows, TumblingEventTimeWindows}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.sql.Timestamp

object WMApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //    sessionWindow(env)
    //    tumblingWindow(env)
    //    slidingWindow(env)

    wm(env)

    /**
     * 窗口触发的规则：
     * 1.数据所带的时间 >= 上一个窗口的结束边界
     * 2.窗口内要有数据
     */

    env.execute(getClass.getSimpleName)
  }


  case class XX(id:String, temperature:Double, name:String, time:Long, location:String)

  class TianyafuAssignerWithPeriodicWatermarks(maxAllowedUnOrderedTime:Long) extends AssignerWithPeriodicWatermarks[XX] {
    var currentMaxTimestamp: Long = _

    override def getCurrentWatermark: Watermark = new Watermark(currentMaxTimestamp - maxAllowedUnOrderedTime)

    override def extractTimestamp(element: XX, recordTimestamp: Long): Long = {
      val nowTime = element.time * 1000
      currentMaxTimestamp = currentMaxTimestamp.max(nowTime)

      println("数据时间:" +new Timestamp(nowTime)
        + " ,当前窗口内元素最大时间:" + new Timestamp(currentMaxTimestamp)
        + ", WM的时间:" + new Timestamp(getCurrentWatermark.getTimestamp))

      nowTime
    }
  }

  /**
   * 1,36.8,a,1582641128,xx
   * 1,37.8,a,1582641129,yy
   * 1,38.8,a,1582641130,zz
   * 1,38.8,a,1582641139,aa
   *
   * @param env
   */
  def wm(env: StreamExecutionEnvironment): Unit = {
    val maxOutOfOrderness = 10 * 1000L // 3.5 seconds

    env.socketTextStream("dsf", 9527)
      .map(x => {
        val splits = x.split(",")
        XX(splits(0).trim, splits(1).trim.toDouble, splits(2).trim, splits(3).trim.toLong, splits(4).trim)
      }).assignTimestampsAndWatermarks(new TianyafuAssignerWithPeriodicWatermarks(maxOutOfOrderness))
      .keyBy(_.id) // 按照监测点id进行分组
      .window(TumblingEventTimeWindows.of(Time.seconds(3)))
      .apply(new RichWindowFunction[XX,String,String,TimeWindow] {
        override def apply(key: String, window: TimeWindow, input: Iterable[XX], out: Collector[String]): Unit = {
          //求一个监测点的平均温度
          val totalCnt = input.size // 监测点的总人数

          var totalTemp = 0.0
          // 获取检测点的总温度和
          input.foreach(x => totalTemp += x.temperature)

          val avgTemp = totalTemp / totalCnt

          val start = new Timestamp(window.getStart)
          val end = new Timestamp(window.getEnd)
          out.collect(s"监测点id:${key},平均温度:${avgTemp},窗口开始时间：${start},窗口结束时间: ${end}")
        }
      }).print()


  }


  /**
   *
   *
   * 单词计数
   *
   * 时间字段,单词,次数
   *
   * @param env
   */
  def slidingWindow(env: StreamExecutionEnvironment): Unit = {


    val window = env.socketTextStream("dsf", 9527).assignTimestampsAndWatermarks(
      new BoundedOutOfOrdernessTimestampExtractor[String](Time.seconds(0)) {
        //获取到数据中的时间
        override def extractTimestamp(element: String): Long = element.split(",")(0).trim.toLong
      }
    ).map(
      x => {
        val splits = x.split(",")
        (splits(1).trim, splits(2).trim.toInt)
      }
    ).keyBy(_._1)
      .window(SlidingEventTimeWindows.of(Time.seconds(6), Time.seconds(2)))
      //这里采用先增量聚合 最后再汇总
      .reduce(new ReduceFunction[(String, Int)] {
        override def reduce(value1: (String, Int), value2: (String, Int)): (String, Int) = {
          (value1._1, value1._2 + value2._2)
        }
      }, new ProcessWindowFunction[(String, Int), String, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {

          for (ele <- elements) {
            out.collect(new Timestamp(context.window.getStart) + "====>" + new Timestamp(context.window.getEnd) + "," + ele._1 + "--->" + ele._2)
          }
        }
      })
    window.print()


  }


  /**
   *
   *
   * 单词计数
   *
   * 时间字段,单词,次数
   *
   * @param env
   */
  def tumblingWindow(env: StreamExecutionEnvironment): Unit = {

    val outputTag = new OutputTag[(String, Int)]("late-data")

    val window = env.socketTextStream("dsf", 9527).assignTimestampsAndWatermarks(
      new BoundedOutOfOrdernessTimestampExtractor[String](Time.seconds(0)) {
        //获取到数据中的时间
        override def extractTimestamp(element: String): Long = element.split(",")(0).trim.toLong
      }
    ).map(
      x => {
        val splits = x.split(",")
        (splits(1).trim, splits(2).trim.toInt)
      }
    ).keyBy(_._1)
      .window(TumblingEventTimeWindows.of(Time.seconds(5)))
      .sideOutputLateData(outputTag)
      //这里采用先增量聚合 最后再汇总
      .reduce(new ReduceFunction[(String, Int)] {
        override def reduce(value1: (String, Int), value2: (String, Int)): (String, Int) = {
          (value1._1, value1._2 + value2._2)
        }
      }, new ProcessWindowFunction[(String, Int), String, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {

          for (ele <- elements) {
            out.collect(new Timestamp(context.window.getStart) + "====>" + new Timestamp(context.window.getEnd) + "," + ele._1 + "--->" + ele._2)
          }
        }
      })
    window.print()

    window.getSideOutput(outputTag).print("-----late_data-----")


  }

  /**
   *
   *
   * 单词计数
   *
   * 时间字段,单词,次数
   *
   * @param env
   */
  def sessionWindow(env: StreamExecutionEnvironment): Unit = {
    env.socketTextStream("dsf", 9527).assignTimestampsAndWatermarks(
      new BoundedOutOfOrdernessTimestampExtractor[String](Time.seconds(0)) {
        //获取到数据中的时间
        override def extractTimestamp(element: String): Long = element.split(",")(0).trim.toLong
      }
    ).map(
      x => {
        val splits = x.split(",")
        (splits(1).trim, splits(2).trim.toInt)
      }
    ).keyBy(_._1)
      .window(EventTimeSessionWindows.withGap(Time.seconds(5)))
      //这里采用先增量聚合 最后再汇总
      .reduce(new ReduceFunction[(String, Int)] {
        override def reduce(value1: (String, Int), value2: (String, Int)): (String, Int) = {
          (value1._1, value1._2 + value2._2)
        }
      }, new ProcessWindowFunction[(String, Int), String, String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[String]): Unit = {

          for (ele <- elements) {
            out.collect(new Timestamp(context.window.getStart) + "====>" + new Timestamp(context.window.getEnd) + "," + ele._1 + "--->" + ele._2)
          }
        }
      }).print()
  }


}
