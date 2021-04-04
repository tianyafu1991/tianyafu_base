package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.{FileSystem, Path}
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch7.ElasticsearchSink
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.http.HttpHost
import org.elasticsearch.client.Requests

import java.util


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


    /**
     * Flink 输出到Redis
     */

    /*val conf = new FlinkJedisPoolConfig.Builder().setHost("hadoop01").setPort(16379).setDatabase(4).build()
    stream.addSink(new RedisSink[Access](conf, new RedisExampleMapper))*/


    /**
     * 写入到MySQL中
     */
    //    stream.addSink(new TianyafuMySQLSink)


    //开启容错
    env.enableCheckpointing(5000)

    val httpHosts = new util.ArrayList[HttpHost]()
    httpHosts.add(new HttpHost("hadoop01", 9200))

    val esSinkFunction = new ElasticsearchSinkFunction[Access] {
      override def process(t: Access, runtimeContext: RuntimeContext, requestIndexer: RequestIndexer): Unit = {
        val json = new util.HashMap[String, String]()
        json.put("domain", t.domain)
        json.put("traffics", t.traffic + "")

        val request = Requests.indexRequest()
          .index("tianyafu_flink_access")
          .source(json)
          .id(t.domain) // 流处理这块要注意 要自定义id  因为流处理这里是带状态的 我们只要最后的结果

        requestIndexer.add(request)

      }
    }

    stream.addSink(new ElasticsearchSink.Builder[Access](httpHosts, esSinkFunction).build())


    env.execute(getClass.getSimpleName)
  }


  class RedisExampleMapper extends RedisMapper[Access] {
    override def getCommandDescription: RedisCommandDescription = {
      new RedisCommandDescription(RedisCommand.HSET, "tianyafu_flink_traffics")
    }

    override def getKeyFromData(data: Access): String = data.domain

    override def getValueFromData(data: Access): String = data.traffic.toString
  }


}
