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


    val stream1: DataStream[Access] = env.addSource(new AccessSource)
    val stream2: DataStream[Access] = env.addSource(new AccessSource)

    val unionStream: DataStream[Access] = stream1.union(stream2)

    val connectedStream: ConnectedStreams[Access, Access] = stream1.connect(stream2)



    env.execute(getClass.getSimpleName)

  }

}
