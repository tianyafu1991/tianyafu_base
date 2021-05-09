package com.tianyafu.flink.analysis

import java.util.concurrent.TimeUnit
import com.alibaba.fastjson.JSON
import com.tianyafu.flink.bean.AccessLogV3
import com.tianyafu.flink.sink.{TianyafuMySQLSink, TianyafuRedisSink}
import com.tianyafu.flink.utils.{DateUtils, Keys}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.concurrent.Executors
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}
import org.apache.http.util.EntityUtils

import scala.concurrent.{ExecutionContext, Future}

object ProvinceApp {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.socketTextStream("hadoop01", 9527)
    val result: DataStream[AccessLogV3] = AsyncDataStream.unorderedWait(stream,
      new TianyafuAsyncHttpRequestV3, 1000, TimeUnit.MILLISECONDS)
      .keyBy(x => (x.time, x.province)).sum("cnt")

    result.print()

    // 写入MySQL
    result.addSink(new TianyafuMySQLSink)
    // 写入 Redis
    // 这种写法是官方的 比较繁琐
    /*val conf = new FlinkJedisPoolConfig.Builder().setHost("hadoop01").setPort(16379).setDatabase(15).build()
     result.addSink(new RedisSink[AccessLogV3](conf, new RedisExampleMapper))*/

    // 这种写法是自定义的 比较简单  推荐这种写法
    result.map(x => ("province-stat", x.time+"_"+x.province, x.cnt)).addSink(new TianyafuRedisSink)

    //    result.map(x => ("province-stat", x.time+"_"+x.province, x.cnt)).addSink(new RuozedataRedisSink)

//    class RedisExampleMapper extends RedisMapper[AccessLogV3]{
//      override def getCommandDescription: RedisCommandDescription = {
//        new RedisCommandDescription(RedisCommand.HSET, "province-stat")
//      }
//
//      override def getKeyFromData(data: AccessLogV3): String = data.time + "_" + data.province
//
//      override def getValueFromData(data: AccessLogV3): String = data.cnt+""
//    }
//
//    val conf = new FlinkJedisPoolConfig.Builder()
//      .setHost("ruozedata001")
//      .setDatabase(15)
//      .setPort(16379)
//      .build()
//
//    result.addSink(new RedisSink[AccessLogV3](conf, new RedisExampleMapper))


    env.execute(getClass.getCanonicalName)

  }
}

class RedisExampleMapper extends RedisMapper[AccessLogV3] {
  override def getCommandDescription: RedisCommandDescription = {
    new RedisCommandDescription(RedisCommand.HSET, "tianyafu_flink_province_cnt")
  }

  override def getKeyFromData(data: AccessLogV3): String = data.time + "_" + data.province

  override def getValueFromData(data: AccessLogV3): String = data.cnt +""
}


class TianyafuAsyncHttpRequestV3 extends RichAsyncFunction[String, AccessLogV3] {

  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(Executors.directExecutor())

  var httpClient:CloseableHttpAsyncClient = _

  override def open(parameters: Configuration): Unit = {
    val requestConfig: RequestConfig = RequestConfig.custom().setSocketTimeout(5000)
      .setConnectTimeout(5000)
      .build()


    httpClient = HttpAsyncClients.custom().setMaxConnTotal(20).setDefaultRequestConfig(requestConfig)
      .build()

    httpClient.start()
  }

  override def close(): Unit = {

    if(null != httpClient) httpClient.close()

  }

  override def asyncInvoke(input: String, resultFuture: ResultFuture[AccessLogV3]): Unit = {
    val splits = input.split(",")
    val time = DateUtils.getTime(splits(0))
    val ip = splits(1)
    val domain = splits(2)
    var province = "-"
    var city = "-"

    // 该api文档详见：https://lbs.amap.com/api/webservice/guide/api/ipconfig/   这里的key代码中做了脱敏 需要换成自己的key
    val url = s"https://restapi.amap.com/v5/ip?ip=$ip&type=4&output=json&key=${Keys.password}"

    try {
      val httpGet = new HttpGet(url)
      val future  = httpClient.execute(httpGet, null)

      val resultFutureRequested: Future[(String, String)] = Future {
        val response = future.get()
        val status = response.getStatusLine.getStatusCode
        val entity = response.getEntity
        if (status == 200) {
          val result = EntityUtils.toString(entity)
          val json = JSON.parseObject(result)
          province = json.getString("province")
          city = json.getString("city")
        }
        (province, city)
      }

      resultFutureRequested.onSuccess {
        case (province, city) => resultFuture.complete(Iterable(AccessLogV3(time, province, 1)))
      }
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
    }
  }
}

