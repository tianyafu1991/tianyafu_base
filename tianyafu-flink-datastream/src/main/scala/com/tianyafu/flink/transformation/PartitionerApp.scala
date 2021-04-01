package com.tianyafu.flink.transformation

import com.tianyafu.flink.bean.Domain
import com.tianyafu.flink.source.AccessSource
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._


object PartitionerApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(3)

    val stream: DataStream[(String, Domain.Access)] = env.addSource(new AccessSource).map(x => (x.domain, x))

    stream.partitionCustom(new TianyafuPartitioner,_._1)
      .map(x => {
        println("Thread-id:" + Thread.currentThread().getId + " ,value:" + x)
        x._2
      }).print()



    env.execute(getClass.getSimpleName)
  }

}
