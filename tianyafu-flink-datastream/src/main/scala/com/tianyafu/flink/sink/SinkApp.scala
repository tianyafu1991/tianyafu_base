package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}



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
    val stream: DataStream[Access] = env.readTextFile(path)
      .map(x => {
        val splits = x.split(",")
        Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
      }).keyBy(_.domain).sum("traffic")

//    stream.print()

    /**
     * 输出到文件
     */

//    stream.writeAsText("out/text",FileSystem.WriteMode.OVERWRITE).setParallelism(1)
//    stream.writeAsCsv("out/csv",FileSystem.WriteMode.OVERWRITE).setParallelism(1)

    /*stream.addSink(StreamingFileSink.forRowFormat(
    new Path("out/text2"),
      new SimpleStringEncoder[Access]()
    ).build())*/




    val conf = new FlinkJedisPoolConfig.Builder().setHost("127.0.0.1").setPort(16379).setDatabase(4).build()
    stream.addSink(new RedisSink[Access](conf, new RedisExampleMapper))


    env.execute(getClass.getSimpleName)
  }


  class RedisExampleMapper extends RedisMapper[Access]{
    override def getCommandDescription: RedisCommandDescription = {
      new RedisCommandDescription(RedisCommand.HSET, "tianyafu_flink_traffics")
    }

    override def getKeyFromData(data: Access): String = data.domain

    override def getValueFromData(data: Access): String = data.traffic.toString
  }


}
