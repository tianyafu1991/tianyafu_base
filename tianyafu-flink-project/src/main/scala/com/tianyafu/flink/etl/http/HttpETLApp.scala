package com.tianyafu.flink.etl.http

import com.alibaba.fastjson.JSON
import com.tianyafu.flink.bean.{AccessLog, AccessLogV2}
import com.tianyafu.flink.utils.{Keys, MySQLUtils}
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils

import java.sql.{Connection, ResultSet}

object HttpETLApp {

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

    stream.map(new RichMapFunction[String, AccessLogV2] {


      var httpClient: CloseableHttpClient = _

      override def open(parameters: Configuration): Unit = {
        httpClient = HttpClients.createDefault()
      }

      override def close(): Unit = {
        if (null != httpClient) {
          httpClient.close()
        }
      }

      override def map(value: String): AccessLogV2 = {
        val splits = value.split(",")
        val time = splits(0)
        val ip = splits(1)
        val domain = splits(2)
        var province = "-"
        var city = "-"

        // 该api文档详见：https://lbs.amap.com/api/webservice/guide/api/ipconfig/   这里的key代码中做了脱敏 需要换成自己的key
        val url = s"https://restapi.amap.com/v5/ip?ip=$ip&type=4&output=json&key=${Keys.password}"

        var response: CloseableHttpResponse = null
        try {
          val httpGet = new HttpGet(url)
          response = httpClient.execute(httpGet)
          val status = response.getStatusLine.getStatusCode
          val entity = response.getEntity
          if (status == 200) {
            val result = EntityUtils.toString(entity)
            val json = JSON.parseObject(result)
            province = json.getString("province")
            city = json.getString("city")
          }
        } catch {
          case e: Exception => e.printStackTrace()
        } finally {
          if (null != response) {
            response.close()
          }
        }
        AccessLogV2(time, domain, province, city)
      }
    }).print()

    env.execute("")
  }

}
