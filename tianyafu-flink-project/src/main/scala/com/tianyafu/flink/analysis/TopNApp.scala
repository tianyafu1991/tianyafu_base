package com.tianyafu.flink.analysis

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

import java.sql.Timestamp
import scala.collection.mutable.ListBuffer

/**
 * 分组TopN: 按照某个/些规则分组，组内的结果排序，最后取出TopN
 * 用户,商品,类别,访问行为,访问时间
 * u001,p1001,c11,pv,2021-05-08 13:11:11
 *
 * 需求： 求某个时间段/window内，维度：商品，访问行为  求 组内TopN
 *
 * 1） 涉及窗口的处理
 * 2) 涉及 EventTime
 * 3) 涉及组内TopN
 */
object TopNApp {

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val path = "data/click.txt"
    val format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
    env.readTextFile(path)
      .map(x => {
        val splits = x.split(",")
        Access(splits(0).trim,splits(1).trim,splits(2).trim,splits(3).trim,format.parse(splits(4).trim).getTime)
      })
      .assignTimestampsAndWatermarks(new BoundedOutOfOrdernessTimestampExtractor[Access](Time.seconds(0)) {
        // 抽取事件时间
      override def extractTimestamp(element: Access): Long = element.timestamp
    })
      .keyBy(x => (x.productId,x.behavior)) // 按照商品和用户访问行为分组
      .timeWindow(Time.minutes(10),Time.minutes(1)) // 定义窗口
      //.apply(new TianyafuWindowFunction) // apply是全量的  也就是说等窗口的数据都到了后，才会去计算，这样性能不是很好，要用增量的方式比较好
      .aggregate(new TianyafuAggregateFunction, new TianyafuWindowFunction2)
      .keyBy(x => (x.behavior, x.end))
      .process(new KeyedProcessFunction[(String,Timestamp),AccessCount, String] {
        var valueState: ValueState[ListBuffer[AccessCount]] = _

        override def open(parameters: Configuration): Unit = {
          val stateDesc = new ValueStateDescriptor[ListBuffer[AccessCount]]("statedesc", classOf[ListBuffer[AccessCount]])
          valueState = getRuntimeContext.getState(stateDesc)
        }


        override def processElement(value: AccessCount, ctx: KeyedProcessFunction[(String, Timestamp), AccessCount, String]#Context, out: Collector[String]): Unit = {
          var buffer: ListBuffer[AccessCount] = valueState.value()
          if(null == buffer)  buffer = ListBuffer[AccessCount]()
          buffer += value

          valueState.update(buffer)

          // 注册定时器
          ctx.timerService().registerEventTimeTimer(value.end.getTime + 1)  // 这里用了窗口时间 + 1 是为了使时间比窗口结束时间大  这样就可以触发下面的omTimer方法的执行
        }

        override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[(String, Timestamp), AccessCount, String]#OnTimerContext, out: Collector[String]): Unit = {
          val buffer = valueState.value()
          val result = buffer.sortBy(_.cnt)(Ordering.Long.reverse).take(3)

          valueState.clear()

          //            out.collect(result.toString())

          val sb = new StringBuilder
          sb.append("时间:").append(new Timestamp(timestamp-1)).append("\n")
          for(i <- result.indices) {
            val bean: AccessCount = result(i)
            sb.append("编号:").append(i+1)
              .append(" 商品:").append(bean.productId)
              .append(" 行为:").append(bean.behavior)
              .append(" 访问量:").append(bean.cnt)
              .append("\n")
          }

          out.collect(sb.toString())

        }
      }).print()





    env.execute("")

  }

}

  class  TianyafuWindowFunction2 extends  WindowFunction[Long,AccessCount,(String, String),TimeWindow] {
    override def apply(key: (String, String), window: TimeWindow, input: Iterable[Long], out: Collector[AccessCount]): Unit = {
      val productId = key._1
      val behavior = key._2

      val start: Long = window.getStart
      val end: Long = window.getEnd

      val cnt = input.iterator.next()

      out.collect(AccessCount(productId,behavior,new Timestamp(start),new Timestamp(end),cnt))
    }
  }

class TianyafuAggregateFunction extends AggregateFunction[Access,Long,Long] {
  override def createAccumulator(): Long = 0L

  override def add(value: Access, accumulator: Long): Long = accumulator + 1L

  override def getResult(accumulator: Long): Long = accumulator

  override def merge(a: Long, b: Long): Long = ???
}



class TianyafuWindowFunction extends WindowFunction[Access,AccessCount,(String, String),TimeWindow] {
  override def apply(key: (String, String), window: TimeWindow, input: Iterable[Access], out: Collector[AccessCount]): Unit = {
    val productId = key._1
    val behavior = key._2

    val start: Long = window.getStart
    val end: Long = window.getEnd

    val cnt = input.size

    out.collect(AccessCount(productId,behavior,new Timestamp(start),new Timestamp(end),cnt))
  }
}


//输入的数据
case class Access(userId:String
                 ,productId:String
                ,categoryId:String
                ,behavior:String
                ,timestamp:Long
                )

// 输出的数据
case class AccessCount(
                      productId:String
                      ,behavior:String
                      ,start:Timestamp    // 窗口开始时间
                      ,end:Timestamp      // 窗口结束时间
                      ,cnt : Long
                      )
