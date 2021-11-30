package com.tianyafu.spark.listener

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.scheduler.{SparkListener, SparkListenerTaskEnd}
import org.json4s.DefaultFormats
import org.json4s.jackson.Json

import scala.collection.mutable

/**
 * 自定义的Spark作业监听
 */
class TianYaFuSparkListerApp(conf:SparkConf) extends SparkListener with Logging{

  /**
   * 每个task完成后都会触发的
   * @param taskEnd
   */
  override def onTaskEnd(taskEnd: SparkListenerTaskEnd): Unit = {
    val appName = conf.get("spark.app.name")
    logError(s"作业的名字为${appName}")
    val metrics = taskEnd.taskMetrics

    val taskMetricsMap = mutable.HashMap(
      "executorDeserializeTime"->metrics.executorDeserializeTime
        ,"executorDeserializeCpuTime"->metrics.executorDeserializeCpuTime
        ,"executorRunTime"->metrics.executorRunTime
        ,"executorCpuTime"->metrics.executorCpuTime
        ,"resultSize"->metrics.resultSize
        ,"jvmGCTime"->metrics.jvmGCTime
        ,"resultSerializationTime"->metrics.resultSerializationTime
        ,"memoryBytesSpilled"->metrics.memoryBytesSpilled
        ,"diskBytesSpilled"->metrics.diskBytesSpilled
        ,"peakExecutionMemory"->metrics.peakExecutionMemory
        ,"shuffleReadMetrics"->metrics.shuffleReadMetrics
        ,"shuffleWriteMetrics"->metrics.shuffleWriteMetrics
    )

    logError(s"作业的metrics:${Json(DefaultFormats).writePretty(taskMetricsMap)}" )
  }

}
