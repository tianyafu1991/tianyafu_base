package com.tianyafu.flink.etl.db

import com.tianyafu.flink.bean.AccessLog
import com.tianyafu.flink.utils.MySQLUtils
import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration

import java.sql.{Connection, ResultSet}

object UserDomainMappingApp {

  /**
   * 2022-07-10 18:10:13,222.55.57.83,ruozedata.com
     2022-07-10 18:10:13,114.246.50.2,ruoze.ke.qq.com
     2022-07-10 18:10:13,222.55.57.83,google.com
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val stream = env.socketTextStream("hadoop01", 9527)

    stream.map(new RichMapFunction[String,AccessLog] {
      var connection: Connection = _

      override def open(parameters: Configuration): Unit = {
        connection= MySQLUtils.getConnection()
      }

      override def close(): Unit = {
        if(null != connection){
          connection.close()
        }
      }

      override def map(value: String): AccessLog = {
        val splits = value.split(",")
        val time = splits(0)
        val domain = splits(2)
        var userId = "-"
        /**
         * 这种方式性能比较差  应该采用异步的方式去查
         */
        val stat = connection.prepareStatement("select user_id from user_domain_mapping where domain = ?")
        stat.setString(1,domain)
        val rs: ResultSet = stat.executeQuery()
        if(rs.next()){
          userId = rs.getString("user_id")
        }
        AccessLog(domain,userId,time)
      }
    }).print()






















































    env.execute("")
  }

}
