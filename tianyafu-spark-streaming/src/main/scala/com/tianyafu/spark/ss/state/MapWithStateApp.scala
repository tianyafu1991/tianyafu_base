package com.tianyafu.spark.ss.state

import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.streaming.dstream.{DStream, MapWithStateDStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, State, StateSpec, StreamingContext}

object MapWithStateApp extends Logging{

  val mappingFunc = (word: String, one: Option[Int], state: State[Int]) => {
    if(state.isTimingOut()){
      logError(s"${word} is time out")
    }else{
      val sum = one.getOrElse(0) + state.getOption.getOrElse(0)
      val output = (word, sum)
      state.update(sum)
      output
    }
  }

  def functionToCreateContext(checkpointDirectory: String): StreamingContext = {
    val conf = new SparkConf().setMaster("local[2]").setAppName(getClass.getSimpleName)
    val ssc = new StreamingContext(conf, Seconds(5)) // new context
    //TODO 小文件会扎堆
    ssc.checkpoint(checkpointDirectory) // set checkpoint directory

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("sdw2", 9527) // create DStreams
    val stateResult: DStream[(String, Int)] = lines.flatMap(_.split(",")).map((_, 1)).mapWithState(
      StateSpec.function(mappingFunc).timeout(Seconds(100))).stateSnapshots()
    stateResult.print()
    ssc
  }

  def main(args: Array[String]): Unit = {
    val checkpointDirectory = "chk2"
    // TODO 这个任务挂掉后重启 状态不能恢复(不清楚是不是这么用的) 生产上也不用这个  用无状态的方式解决有状态的需求
    val ssc = StreamingContext.getActiveOrCreate(checkpointDirectory,()=> functionToCreateContext(checkpointDirectory))

    ssc.start()
    ssc.awaitTermination()
  }


}
