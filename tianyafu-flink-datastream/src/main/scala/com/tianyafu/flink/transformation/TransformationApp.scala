package com.tianyafu.flink.transformation

import com.tianyafu.flink.bean.Domain.Access
import com.tianyafu.flink.source.AccessSource
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._

/**
 * @Author:tianyafu
 * @Date:2021/3/31
 * @Description:
 */
object TransformationApp {

  def main(args: Array[String]): Unit = {

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val path = "data/access.log"

    /*val stream = env.readTextFile(path).map(x => {

      val splits: Array[String] = x.split(",")
      Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
    }).keyBy(x => (x.time,x.domain)).sum("traffic")
    stream.print()*/

    /**
     * a,1
     * b,3
     * a,8
     * a,4
     */
    /*env.socketTextStream("hadoop01",9527)
        .map(x => {
          val splits: Array[String] = x.split(",")
          (splits(0).trim,splits(1).trim.toInt)
        }).keyBy(_._1).max(1).print()*/


    /**
     * union 和 connect 算子
     */
    /*val stream1: DataStream[Access] = env.addSource(new AccessSource)
    val stream2: DataStream[Access] = env.addSource(new AccessSource)

    val stream3: DataStream[(String, Access)] = stream2.map(("tianyafu", _))

    // union 只能接收相同类型的流，可以多条
    val unionStream: DataStream[Access] = stream1.union(stream2)
    // connect 可以接收不同类型的流，只能合并1条流
    val connectedStream: ConnectedStreams[Access, Access] = stream1.connect(stream2)

    val connectionStream2: ConnectedStreams[Access, (String, Access)] = stream1.connect(stream3)

    connectionStream2.map(x => x,y => y)*/


    /**
     * split 算子
     * 相当于在流中给数据打个标签
     * 通过select算子可以获取某个标签的数据
     */

    val stream = env.readTextFile(path).map(x => {

      val splits: Array[String] = x.split(",")
      Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
    }).keyBy(x => (x.time,x.domain)).sum("traffic")

    val splitStream = stream.split(x => {
      if (x.traffic <= 6000) {
        Seq("小于等于6000")
      } else {
        Seq("大于6000")
      }
    })

    splitStream.select("小于等于6000").print("=========")
    splitStream.select("大于6000").print("--------------")


    env.execute(getClass.getSimpleName)

  }

}
