package com.tianyafu.flink.sink

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import redis.clients.jedis.Jedis

class TianyafuRedisSink extends RichSinkFunction[(String,String,Long)]{

  var jedis :Jedis = _

  override def invoke(value: (String, String, Long), context: SinkFunction.Context[_]): Unit = {
    if(!jedis.isConnected){
        jedis.connect()
    }
    jedis.hset(value._1,value._2,value._3+"")
  }

  override def open(parameters: Configuration): Unit = {
    jedis = new Jedis("hadoop01",16379)
    jedis.select(14)
  }

  override def close(): Unit = {
    if(null != jedis){
      jedis.close()
    }
  }

}
