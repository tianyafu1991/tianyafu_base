package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.streaming.api.scala.{KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink



object SinkApp {


  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    /**
     * print sink
     */

    /*val stream = env.socketTextStream("hadoop01", 9527)
    println(stream.parallelism)
    stream.print("=======print=======").setParallelism(2)
    stream.printToErr("-----------printToError---------").setParallelism(1)*/


    val path = "data/access.log"
    val stream = env.readTextFile(path)
      .map(x => {
        val splits = x.split(",")
        Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
      }).keyBy(_.domain).sum("traffic")

//    stream.print()

//    stream.writeAsText("out/text",FileSystem.WriteMode.OVERWRITE).setParallelism(1)
//    stream.writeAsCsv("out/csv",FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    stream.addSink(StreamingFileSink.forRowFormat(
    new Path("out/text2"),
      new SimpleStringEncoder[Access]()
    ).build())


    env.execute(getClass.getSimpleName)
  }

}
