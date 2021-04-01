package com.tianyafu.flink.transformation

import com.tianyafu.flink.bean.Domain.SplitAccess
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.util.Collector

/**
 * 对于分流之后的流再分流  split算子是做不到的  而且split算子已经过时了  要用sideOutput
 */
object SideApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val path = "data/splits.log"

    val stream: DataStream[SplitAccess] = env.readTextFile(path).map(
      x => {
        val splits = x.split(",")
        SplitAccess(splits(0).trim, splits(1).trim, splits(2).trim.toLong)
      }
    )

    val guandongTag :OutputTag[SplitAccess] = new OutputTag[SplitAccess]("guangdong")
    val anhuiTag :OutputTag[SplitAccess] = new OutputTag[SplitAccess]("anhui")

    val splitStream: DataStream[SplitAccess] = stream.process(new ProcessFunction[SplitAccess, SplitAccess] {
      override def processElement(value: SplitAccess, ctx: ProcessFunction[SplitAccess, SplitAccess]#Context, out: Collector[SplitAccess]): Unit = {
        if (value.province == "广东省") {
          ctx.output(guandongTag, value)
        } else if (value.province == "安徽省") {
          ctx.output(anhuiTag, value)
        }
      }
    })

    val guangdong = splitStream.getSideOutput(guandongTag)
    guangdong.print("=======广东省分流结果=========")
    val anhui = splitStream.getSideOutput(anhuiTag)
    anhui.print("!!!!!!!!!!!!!!安徽省分流结果!!!!!!!!!!!!!!")


    val guangzhouTag: OutputTag[SplitAccess] = new OutputTag[SplitAccess]("广州市")
    val shenzhenTag: OutputTag[SplitAccess] = new OutputTag[SplitAccess]("深圳市")

    val guangdongSplitStream = guangdong.process(new ProcessFunction[SplitAccess, SplitAccess] {
      override def processElement(value: SplitAccess, ctx: ProcessFunction[SplitAccess, SplitAccess]#Context, out: Collector[SplitAccess]): Unit = {
        if (value.city == "深圳市") {
          ctx.output(shenzhenTag, value)
        } else if (value.city == "广州市") {
          ctx.output(guangzhouTag, value)
        }
      }
    })


    val guangzhou: DataStream[SplitAccess] = guangdongSplitStream.getSideOutput(guangzhouTag)
    guangzhou.print("~~~~~~~~~~~广州市分流结果~~~~~~~~~~~~")
    val shenzhen: DataStream[SplitAccess] = guangdongSplitStream.getSideOutput(shenzhenTag)

    shenzhen.print("@@@@@@@@@@@@@深圳市分流结果@@@@@@@@@@@@@")






    env.execute(getClass.getSimpleName)
  }

}
