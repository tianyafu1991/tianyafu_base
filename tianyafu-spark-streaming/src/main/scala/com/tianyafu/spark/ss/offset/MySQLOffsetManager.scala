package com.tianyafu.spark.ss.offset
import com.tianyafu.spark.ss.utils.{ConnectionPool}
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange

import java.sql.{Connection, PreparedStatement, ResultSet}
import scala.collection.mutable

object MySQLOffsetManager extends OffsetManager[Array[(String, Long)]] {




  /**
   * 从外部存储中获取offset
   *
   * @param topics  topics
   * @param groupId Kafka Consumer group id
   * @return
   */
  override def obtainOffset(topics: Array[String], groupId: String,obtainOffsetSql:String): mutable.HashMap[TopicPartition, Long] = {
    var conn : Connection = null
    var pstmt : PreparedStatement = null
    val fromOffsets = new mutable.HashMap[TopicPartition, Long]()

    try{
      conn = ConnectionPool.getConnection
      pstmt = conn.prepareStatement(obtainOffsetSql)
      pstmt.setString(1,groupId)
      for(i <- 1.to(topics.size)){
        pstmt.setString(i+ 1 ,topics(i-1) )
      }
      val result: ResultSet = pstmt.executeQuery()
      while (result.next()){
        val topic: String = result.getString("topic")
        val partitionId: Int = result.getInt("partition_id")
        val offset: Long = result.getLong("offset")
        println(s"从数据库中查询出的结果为:topic:${topic},partition_id:${partitionId},offset:${offset}")
        val topicPartition = new TopicPartition(topic, partitionId)
        fromOffsets(topicPartition)= offset
      }
    }catch {
      case e:Exception => {
        e.printStackTrace()
      }
    }finally {
      ConnectionPool.closeStatement(pstmt)
      ConnectionPool.returnConnection(conn)
    }
    fromOffsets
  }

  /**
   * 将作业的聚合后的结果和本批次Kafka的offset信息一起存储
   *
   * @param offsetRanges 本批次Kafka Message的offset信息
   * @param groupId      Kafka Consumer group id
   * @param result       作业的结果集
   * @tparam T
   */
  override def storeResultsAndOffset(offsetRanges: Array[OffsetRange], groupId: String, result: Array[(String, Long)]): Unit = {
    var conn : Connection = null
    var pstmt1 : PreparedStatement = null
    var pstmt2 : PreparedStatement = null
    // 输出结果的sql
    val insertSql1 = "insert into streaming_wc(word,cnt) values(?,?) on duplicate key update cnt = cnt + ?"
    // 保存offset的sql
    val insertSql2 = "insert into streaming_offset_stored(topic,group_id,partition_id,offset) values(?,?,?,?) on duplicate key update offset = ?"

    try{
      conn= ConnectionPool.getConnection
      conn.setAutoCommit(false)

      pstmt1 = conn.prepareStatement(insertSql1)
      pstmt2 = conn.prepareStatement(insertSql2)

      for (wc <- result) {
        pstmt1.setString(1,wc._1)
        pstmt1.setLong(2,wc._2)
        pstmt1.setLong(3,wc._2)
        pstmt1.addBatch()
      }


      for (offsetRange <- offsetRanges) {
        pstmt2.setString(1,offsetRange.topic)
        pstmt2.setString(2,groupId)
        pstmt2.setInt(3,offsetRange.partition)
        pstmt2.setLong(4,offsetRange.untilOffset)
        pstmt2.setLong(5,offsetRange.untilOffset)
        pstmt2.addBatch()
      }

      pstmt1.executeBatch()
      pstmt2.executeBatch()
      conn.commit()
    }catch {
      case e:Exception => {
        e.printStackTrace()
        conn.rollback()
      }
    }finally {
      ConnectionPool.closeStatement(pstmt1)
      ConnectionPool.closeStatement(pstmt2)
      ConnectionPool.returnConnection(conn)
    }
  }
}
