package com.tianyafu.spark.basic

import com.tianyafu.spark.basic.SparkWCApp.getClass
import com.tianyafu.spark.utils.MySQLUtils
import org.apache.spark.internal.Logging
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import java.sql.{Connection, PreparedStatement}

object DataSourceApp extends Logging{

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local").setAppName(getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val rdd: RDD[(String, Int)] = sc.parallelize(List(("zhangsan", 20), ("lisi", 18), ("wangwu", 39)))
    val path = "out"
    //    rdd.saveAsTextFile(path)




    rdd.foreachPartition(partition => {
      val conn: Connection = MySQLUtils.getConnection()
      logError(s"创建MySQL连接:${conn}")
      conn.setAutoCommit(false)
      val statement: PreparedStatement = conn.prepareStatement("insert into user_info(name,age) values(?,?)")
      partition.foreach(x => {
        statement.setString(1, x._1)
        statement.setInt(2, x._2)
        statement.addBatch()
      })
      statement.executeBatch()
      conn.commit()
      conn.close()

    })

    sc.stop()
  }

}
