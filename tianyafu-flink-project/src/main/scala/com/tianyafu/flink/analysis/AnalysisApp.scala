package com.tianyafu.flink.analysis

import java.lang
import java.sql.Timestamp

import org.apache.flink.api.common.functions.{AggregateFunction, MapFunction}
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala.function.{AllWindowFunction, WindowFunction}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import scala.util.Random


object AnalysisApp {
  def main(args: Array[String]): Unit = {
      val env = StreamExecutionEnvironment.getExecutionEnvironment
      env.setParallelism(4)
      env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

//      pv(env)
//      pvSkew(env)

    uv(env)
      env.execute(this.getClass.getCanonicalName)
  }

  def uv(env: StreamExecutionEnvironment): Unit = {
    val stream = env.readTextFile("data/black.txt")
      .map(x => {
        val splits = x.split(",")
        AccessPage(splits(0).trim, splits(1).trim, splits(2).trim, splits(3).trim.toLong)
      }).assignAscendingTimestamps(_.time)
      .timeWindowAll(Time.hours(1))
      .aggregate(new AggregateFunction[AccessPage,Set[String],Long] { // 布隆过滤器 + 第三方存储(redis/hbase/...)
        override def createAccumulator(): Set[String] = Set[String]()

        override def add(value: AccessPage, accumulator: Set[String]): Set[String] = accumulator + value.userId

        override def getResult(accumulator: Set[String]): Long = accumulator.size

        override def merge(a: Set[String], b: Set[String]): Set[String] = ???
      }, new AllWindowFunction[Long, UVCount, TimeWindow] {
        override def apply(window: TimeWindow, input: Iterable[Long], out: Collector[UVCount]): Unit = {
          out.collect(UVCount(window.getEnd, input.head))
        }
      }).print()
//      .apply(new AllWindowFunction[AccessPage, UVCount, TimeWindow] {
//        override def apply(window: TimeWindow, input: Iterable[AccessPage], out: Collector[UVCount]): Unit = {
//          val userIds = scala.collection.mutable.Set[String]()
//
//          for(ele <- input) {
//            userIds.add(ele.userId)
//          }
//
//          out.collect(UVCount(window.getEnd, userIds.size))
//        }
//      }).print()
  }


  def pvSkew(env: StreamExecutionEnvironment): Unit = {
    val stream = env.readTextFile("data/black.txt")
      .map(x => {
        val splits = x.split(",")
        AccessPage(splits(0).trim, splits(1).trim, splits(2).trim, splits(3).trim.toLong)
      }).assignAscendingTimestamps(_.time)
      .map(new MapFunction[AccessPage, (String,Long)] {
        override def map(value: AccessPage): (String, Long) = {
          (Random.nextString(10), 1L)
        }
      })
      .keyBy(_._1)
      .timeWindow(Time.seconds(1))
      .aggregate(new AggregateFunction[(String,Long), Long, Long] {
        override def createAccumulator(): Long = 0L

        override def add(value: (String, Long), accumulator: Long): Long = accumulator+1

        override def getResult(accumulator: Long): Long = accumulator

        override def merge(a: Long, b: Long): Long = ???
      }, new WindowFunction[Long, PVCount, String, TimeWindow] {
        override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[PVCount]): Unit = {
          out.collect(PVCount(window.getEnd, input.head))
        }
      })

    stream.keyBy(x => x.end)
      .process(new KeyedProcessFunction[Long,PVCount, PVCount] {
        // state
        var pvState:ValueState[Long] = null

        override def open(parameters: Configuration): Unit = {
          pvState = getRuntimeContext.getState(new ValueStateDescriptor[Long]("pvs", classOf[Long]))

        }


        override def processElement(value: PVCount, ctx: KeyedProcessFunction[Long, PVCount, PVCount]#Context, out: Collector[PVCount]): Unit = {
          pvState.update(pvState.value() + value.cnts)

          // 定时器
          ctx.timerService().registerEventTimeTimer(value.end + 1)
        }

        override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[Long, PVCount, PVCount]#OnTimerContext, out: Collector[PVCount]): Unit = {
          out.collect(PVCount(ctx.getCurrentKey, pvState.value()))
          pvState.clear() // TODO... 一定要的
        }
      }).print()
  }

  def pv(env: StreamExecutionEnvironment): Unit = {
    env.readTextFile("data/black.txt")
      .map(x => {
        val splits = x.split(",")
        AccessPage(splits(0).trim, splits(1).trim, splits(2).trim, splits(3).trim.toLong)
      }).assignAscendingTimestamps(_.time)
      .map(x => ("pv",1L))
      .keyBy(_._1)
      .timeWindow(Time.seconds(1))
      .aggregate(new AggregateFunction[(String,Long), Long, Long] {
        override def createAccumulator(): Long = 0L

        override def add(value: (String, Long), accumulator: Long): Long = accumulator+1

        override def getResult(accumulator: Long): Long = accumulator

        override def merge(a: Long, b: Long): Long = ???
      }, new WindowFunction[Long, PVCount, String, TimeWindow] {
        override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[PVCount]): Unit = {
          out.collect(PVCount(window.getEnd, input.head))
        }
      }).print()
  }

  def byProvince(env: StreamExecutionEnvironment): Unit = {
    env.readTextFile("data/black.txt")
      .map(x => {
        val splits = x.split(",")
        AccessPage(splits(0).trim, splits(1).trim, splits(2).trim, splits(3).trim.toLong)
      }).assignAscendingTimestamps(_.time)
      .keyBy(x => x.province)
      .timeWindow(Time.hours(1), Time.minutes(10))
      .aggregate(new AggregateFunction[AccessPage,Long, Long] {
        override def createAccumulator(): Long = 0L

        override def add(value: AccessPage, accumulator: Long): Long = accumulator + 1

        override def getResult(accumulator: Long): Long = accumulator

        override def merge(a: Long, b: Long): Long = ???
      }, new WindowFunction[Long, ProvinceCount, String, TimeWindow] {
        override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[ProvinceCount]): Unit = {
          out.collect(ProvinceCount(new Timestamp(window.getEnd).toString,key,input.iterator.next()))
        }
      }).print()
  }
}

case class AccessPage(userId:String, province:String, domain:String, time:Long)
case class ProvinceCount(end:String, province:String, cnts:Long)
case class PVCount(end:Long, cnts:Long)
case class UVCount(end:Long, cnts:Long)