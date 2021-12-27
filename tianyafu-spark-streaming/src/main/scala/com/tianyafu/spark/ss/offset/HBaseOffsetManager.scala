package com.tianyafu.spark.ss.offset

import com.tianyafu.spark.ss.utils.HBaseUtils
import com.tianyafu.spark.ss.utils.HBaseUtils.getConnection
import org.apache.hadoop.hbase.{Cell, CellUtil, TableName}
import org.apache.hadoop.hbase.client.{Connection, Put, Result, ResultScanner, Scan, Table}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.TopicPartition
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010.OffsetRange

import java.util
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object HBaseOffsetManager extends OffsetManager[RDD[Sales]] {

  case class HBaseBean(rowKey: String, cf: String, qualifier: String, value: String)

  case class HBaseOffsetBean(var rowKey: String, var cf: String, var topic: String, var groupId: String, var partitionId: Int, var offset: Long)

  /**
   * 从外部存储中获取offset
   *
   * @param topics  topics
   * @param groupId Kafka Consumer group id
   * @return
   */
  override def obtainOffset(topics: Array[String], groupId: String, conf: SparkConf): mutable.HashMap[TopicPartition, Long] = {

    val zk = conf.get("spark.streaming.hbase.zookeeper.quorum", "mdw,sdw1,sdw2")
    val port = conf.getInt("spark.streaming.hbase.zookeeper.property.clientPort", 2181)
    val tableName = conf.get("spark.streaming.hbase.table.name", "tyf:tyf_hbase_offset")
    val groupId = conf.get("spark.streaming.kafka.consumer.group.id", "tyf_ss_kafka_2_hbase_group")
    val topic = conf.get("spark.streaming.kafka.topic", "hbase_ss")
    val topics = topic.split(",")

    val conn: Connection = getConnection(zk, port)
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val scan = new Scan()
    scan.addFamily(Bytes.toBytes("offset"))
    val scanner: ResultScanner = table.getScanner(scan)
    val iter: util.Iterator[Result] = scanner.iterator()
    val hbaseOffsetBeanList = ListBuffer[HBaseOffsetBean]()
    while (iter.hasNext) {
      println("....................................")
      val result: Result = iter.next()
      val cells: Array[Cell] = result.rawCells()
      val hbaseOffsetBean = HBaseOffsetBean("", "", "", "", -1, -1L)
      cells.map(cell => {
        val rowKey: String = new String(CellUtil.cloneRow(cell))
        val qualifier: String = new String(CellUtil.cloneQualifier(cell))
        val cf: String = new String(CellUtil.cloneFamily(cell))
        val value: String = new String(CellUtil.cloneValue(cell))
        println(s"rowKey:${rowKey}========cf:${cf}=========qualifier:${qualifier}===========offset:${value}")
        HBaseBean(rowKey, cf, qualifier, value)
      }).map(x => {
        val qualifier: String = x.qualifier
        qualifier match {
          case "topic" => {
            hbaseOffsetBean.rowKey = x.rowKey
            hbaseOffsetBean.cf = x.cf
            hbaseOffsetBean.topic = x.value
          }
          case "groupId" => hbaseOffsetBean.groupId = x.value
          case "partitionId" => hbaseOffsetBean.partitionId = x.value.toInt
          case "offset" => hbaseOffsetBean.offset = x.value.toLong
        }
      })
      hbaseOffsetBeanList.append(hbaseOffsetBean)
    }
    println("~~~~~~~~~~~~~~~~~~~~~~")
    // 先过滤掉groupId不是本消费者组的 或者topic不是本任务所消费的
    hbaseOffsetBeanList.filter(x=> {
      x.groupId == groupId && topics.contains(x.topic)
    }).map(x=>{

    })
    null
  }

  /**
   * 将作业的聚合后的结果和本批次Kafka的offset信息一起存储
   *
   * @param offsetRanges 本批次Kafka Message的offset信息
   * @param groupId      Kafka Consumer group id
   * @param result       作业的结果集
   * @tparam T
   */
  override def storeResultsAndOffset(offsetRanges: Array[OffsetRange], groupId: String, result: RDD[Sales]): Unit = {

    val partitionIdOffsetRangeMap: Map[Int, OffsetRange] = offsetRanges.map(x => (x.partition, x)).toMap
    result.foreachPartition(partition => {
      if (partition.nonEmpty) {
        val partitionId: Int = TaskContext.get.partitionId()
        val offsetRange: OffsetRange = partitionIdOffsetRangeMap.get(partitionId).get
        // TODO 将业务数据和offset信息写出到HBase中  利用HBase的行事务特性 将offset放在一行数据的另一个columnFamily中

        val conn: Connection = HBaseUtils.getConnection("mdw,sdw1,sdw2", 2181)
        val table: Table = conn.getTable(TableName.valueOf("tyf:tyf_hbase_offset"))

        val puts = new util.ArrayList[Put]()

        partition.foreach(sales => {
          val put = new Put(Bytes.toBytes(sales.id))
          put.addColumn(Bytes.toBytes("o"), Bytes.toBytes("name"), Bytes.toBytes(sales.name))
          put.addColumn(Bytes.toBytes("o"), Bytes.toBytes("money"), Bytes.toBytes(sales.money))

          // TODO 如果分区中没有下一条了 就表明当前是最后一条 最后一条数据维护一下当前批次的offset信息即可  没必要每条数据都维护
          if (!partition.hasNext) {
            val topic: String = offsetRange.topic
            val offset: Long = offsetRange.untilOffset

            put.addColumn(Bytes.toBytes("offset"), Bytes.toBytes("topic"), Bytes.toBytes(topic))
            put.addColumn(Bytes.toBytes("offset"), Bytes.toBytes("groupId"), Bytes.toBytes(groupId))
            put.addColumn(Bytes.toBytes("offset"), Bytes.toBytes("partitionId"), Bytes.toBytes(partitionId + ""))
            put.addColumn(Bytes.toBytes("offset"), Bytes.toBytes("offset"), Bytes.toBytes(offset + ""))
          }

          puts.add(put)
        })

        table.put(puts)
        table.close()
        conn.close()

      }
    })
  }
}
