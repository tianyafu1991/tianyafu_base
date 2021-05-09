package com.tianyafu.flink.etl.async

import com.alibaba.fastjson.JSON
import com.tianyafu.flink.bean.AccessLogV2
import com.tianyafu.flink.utils.Keys
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{ HttpGet}
import org.apache.http.impl.nio.client.{CloseableHttpAsyncClient, HttpAsyncClients}
import org.apache.http.util.EntityUtils

import java.util.concurrent.{ TimeUnit}
import scala.concurrent.{ExecutionContext, Future}

object HttpETLAsyncApp {

  /**
   * 2022-07-10 18:10:13,222.55.57.83,ruozedata.com
   * 2022-07-10 18:10:13,114.246.50.2,ruoze.ke.qq.com
   * 2022-07-10 18:10:13,222.55.57.83,google.com
   *
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val stream = env.socketTextStream("hadoop01", 9527)

    val resultStream: DataStream[AccessLogV2] = AsyncDataStream.unorderedWait(stream, new AsyncHttpRequest(), 1000, TimeUnit.MILLISECONDS)

    resultStream.print()

    env.execute("")
  }

}

class AsyncHttpRequest extends RichAsyncFunction[String, AccessLogV2] {

  var httpClient: CloseableHttpAsyncClient = _

  /** The context used for the future callbacks */
  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(
    org.apache.flink.runtime.concurrent.Executors.directExecutor()
  )

  override def open(parameters: Configuration): Unit = {
    val config: RequestConfig = RequestConfig.custom()
      .setSocketTimeout(5000)
      .setConnectTimeout(5000)
      .build()


    httpClient= HttpAsyncClients.custom().setMaxConnTotal(20).setDefaultRequestConfig(config).build()

    httpClient.start()
  }

  override def close(): Unit = {
    if(null != httpClient){
      httpClient.close()
    }
  }


  override def asyncInvoke(input: String, resultFuture: ResultFuture[AccessLogV2]): Unit = {
    val splits = input.split(",")
    val time = splits(0)
    val ip = splits(1)
    val domain = splits(2)
    var province = "-"
    var city = "-"

    // 该api文档详见：https://lbs.amap.com/api/webservice/guide/api/ipconfig/   这里的key代码中做了脱敏 需要换成自己的key
    val url = s"https://restapi.amap.com/v5/ip?ip=$ip&type=4&output=json&key=${Keys.password}"


    try {
      val httpGet = new HttpGet(url)
      val future: java.util.concurrent.Future[HttpResponse] = httpClient.execute(httpGet, null)
      // val resultFutureRequested: scala.concurrent.Future[(String,String)] =
      val resultFutureRequested: scala.concurrent.Future[(String, String)] = Future {
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
        case (province,city) => resultFuture.complete(Iterable(AccessLogV2(time, domain, province, city)))
      }

    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
    }

  }
}
