package com.tianyafu.flink.window

import com.tianyafu.flink.bean.Domain.Temperature

import java.sql.Timestamp
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object WindowFunctionApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment


    //https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/windows.html#reducefunction
    //reduce 是增量的 来一个计算一个
    /*env.socketTextStream("hadoop01", 9527)
      .map(x => (1,x.trim.toInt))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .reduce((x,y) => {
        println("执行reduce代码:" + x + "," + y) // 这里打印可以看出reduce是来一个计算一个
        (x._1,x._2+ y._2)
      }).print()*/


    //https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/windows.html#aggregatefunction

    //aggregate函数 是增量的  这里我们要做的是求平均数
    // 进去的第一个参数是(String,Long)
    // 第二个参数表示中间状态 (Long,Long)
    // 第三个参数是出来的结果类型，是Double
    /*env.socketTextStream("hadoop01", 9527)
      .map(x => {
        val splits = x.split(",")
        (splits(0).trim, splits(1).trim.toLong)
      })
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .aggregate(new TianyafuAvgAggregateFunction)
      .print()*/

    // https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/stream/operators/windows.html#processwindowfunction
    // ProcessWindowFunction 是全量的  拿到一个窗口内的所有的 elements 再计算
    // 求窗口内的最大值
    /**
     * ProcessWindowFunction[IN, OUT, KEY, W <: Window]
     * IN:(Int,Int)
     * OUT: String
     * KEY:Tuple
     * W:TimeWindow
     */
    /*env.socketTextStream("hadoop01", 9527)
      .map(x => (1,x.trim.toInt))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .process(new TianyafuProcessWindowFunction)
      .print()*/

    //大于39度的人通过sideOutput输出：pk,1000,37.6
    val stream: DataStream[Temperature] = env.socketTextStream("hadoop01", 9527)
      .map(x => {
        val splits = x.split(",")
        Temperature(splits(0).trim, splits(1).trim.toLong, splits(2).trim.toFloat)
      })
      .process(new TemperatureProcessor(39.0F))

    stream.print("正常的....")
    stream.getSideOutput(new OutputTag[(String,Long,Float)]("high")).print("高的")

    env.execute(getClass.getSimpleName)
  }

  class TemperatureProcessor(threshold:Float) extends ProcessFunction[Temperature,Temperature] {
    override def processElement(value: Temperature, ctx: ProcessFunction[Temperature, Temperature]#Context, out: Collector[Temperature]): Unit = {
      if(value.temperature <= threshold){
        out.collect(value)
      }else { // 不符合预期的通过sideOutput的方式输出
        ctx.output(new OutputTag[(String,Long,Float)]("high"),(value.name,value.time,value.temperature))
      }

    }
  }


  class TianyafuProcessWindowFunction extends ProcessWindowFunction[(Int,Int), String, Int, TimeWindow]{
    override def process(key: Int, context: Context, elements: Iterable[(Int, Int)], out: Collector[String]): Unit = {
      println("======process.invoked========")

      var maxValue = Int.MinValue
      for (ele <- elements) {
        maxValue = ele._2.max(maxValue)
      }

      val currentWindow = context.window
      val start = new Timestamp(currentWindow.getStart)
      val end = new Timestamp(currentWindow.getEnd)

      out.collect(s"最大值是:${maxValue},窗口开始时间为：${start},窗口结束时间为：${end}" )
    }
  }


  class TianyafuAvgAggregateFunction extends AggregateFunction[(String, Long), (Long, Long), Double] {
    // 初始化一个累加器(赋初值)
    override def createAccumulator(): (Long, Long) = (0L, 0l)

    //当前进来的元素添加到累加器中，并返回一个新的累加器
    override def add(value: (String, Long), accumulator: (Long, Long)): (Long, Long) = {
      println(".....add invoked......." + value._1 + "----->" + value._2)
      (accumulator._1 + value._2, accumulator._2 + 1L)
    }
    // 从累加器中获取最终的结果
    override def getResult(accumulator: (Long, Long)): Double = {
      //总和/个数
      accumulator._1 / accumulator._2.toDouble
    }
    // 多个作业之间的累加器的合并
    override def merge(a: (Long, Long), b: (Long, Long)): (Long, Long) = {
      println("=================merge invoked====================" + a._1 + ","+ a._2 + "--" + b._1 + "," + b._2)
      (a._1 +b._1,a._2+b._2)
    }
  }


}
