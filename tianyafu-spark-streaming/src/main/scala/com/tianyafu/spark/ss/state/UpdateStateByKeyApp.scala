package com.tianyafu.spark.ss.state

import com.tianyafu.spark.ss.utils.RedisUtils
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

object UpdateStateByKeyApp {

  def updateFunc(newValue:Seq[Int],oldValue:Option[Int]): Option[Int] = {

    val newCount = newValue.sum
    val pre = oldValue.getOrElse(0)
    Some(newCount+pre)
  }

  def functionToCreateContext(checkpointDirectory:String): StreamingContext = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf,Seconds(5))   // new context
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("sdw2", 9527) // create DStreams
    lines.flatMap(_.split(",")).map((_,1)).updateStateByKey(updateFunc).print()
    //TODO 小文件会扎堆
    ssc.checkpoint(checkpointDirectory)   // set checkpoint directory
    ssc
  }

  def functionToCreateContext2(): StreamingContext = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf,Seconds(5))   // new context
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("sdw2", 9527) // create DStreams
    lines.flatMap(_.split(",")).map((_,1)).foreachRDD(rdd => {
      if(!rdd.isEmpty()){
        rdd.foreachPartition(partition => {
          val jedis: Jedis = RedisUtils.getJedis
          jedis.select(5)
          partition.foreach(pair => {
            jedis.hincrBy("streaming_wc",pair._1,pair._2)
          })
          // 工具类中是使用了pool 这里是还jedis到池中
          jedis.close()
        })
      }
    })
    ssc
  }

  def main(args: Array[String]): Unit = {
    //TODO  任务挂掉后 可以恢复状态  但updateStateByKey这个算子需要checkpoint 小文件会扎堆 生产慎用
//    val checkpointDirectory = "chk"
//    val ssc: StreamingContext = StreamingContext.getOrCreate(checkpointDirectory, () => functionToCreateContext(checkpointDirectory))

    // TODO 这种是将状态值存储在redis中 利用hash的形式  以无状态的方式解决带状态的需求 这样可以避免小文件扎堆 而且任务挂掉后重启 不影响结果 且代码更改也可以读到以前的结果
    //  推荐用这种
    val ssc: StreamingContext = functionToCreateContext2

    ssc.start()
    ssc.awaitTermination()
  }


}
