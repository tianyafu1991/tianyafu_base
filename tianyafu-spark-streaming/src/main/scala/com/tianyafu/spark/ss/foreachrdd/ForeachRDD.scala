package com.tianyafu.spark.ss.foreachrdd

import com.alibaba.druid.pool.DruidPooledConnection
import com.tianyafu.spark.ss.utils.ConnectionPool
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import java.sql.{Connection, PreparedStatement}

object ForeachRDD extends Logging {

  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setAppName(getClass.getSimpleName).setMaster("local[4]")
    val ssc = new StreamingContext(conf, Seconds(5))

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("sdw2", 9527)

    val result: DStream[(String, Long)] = lines.flatMap(_.split(",")).countByValue()

    save2MySQL(result)


    ssc.start()
    ssc.awaitTermination()
  }

  def save2MySQL(result: DStream[(String, Long)]): Unit = {
    result.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        rdd.foreachPartition(partition => {
          // 每个分区获取一次连接
          val conn: Connection = ConnectionPool.getConnection
          logError(s"获取到连接:${conn}")
          // 关闭自动提交
          conn.setAutoCommit(false)
          // 预编译sql获取statement对象
          val statement: PreparedStatement = conn.prepareStatement("insert into wc(word,cnt) values(?,?)")
          // 使用zipWithIndex给partition中的每个元素一个序号
          partition.zipWithIndex.foreach {
            case ((word, cnt), index) => {
              statement.setString(1, word)
              statement.setLong(2, cnt)
              statement.addBatch()
              if (index != 0 && index % 10000 == 0) {
                // 每10000条向MySQL中提交一次
                statement.executeBatch()
                conn.commit()
              }
            }

          }
          // 如果有剩余的没到10000条的 就再提交一次
          statement.executeBatch()
          conn.commit()
          // 关闭statement 并将连接归还到连接池中
          statement.close()
          ConnectionPool.returnConnection(conn)
        })
      }
    })
  }

}
