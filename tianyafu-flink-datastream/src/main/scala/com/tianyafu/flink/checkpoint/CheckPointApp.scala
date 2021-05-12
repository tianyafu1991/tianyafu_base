package com.tianyafu.flink.checkpoint

import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.common.time.Time
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.api.scala._
import org.apache.flink.runtime.state.filesystem.FsStateBackend

import java.util.concurrent.TimeUnit

/**
 * 参考 : https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/stream/state/checkpointing.html
 */
object CheckPointApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.enableCheckpointing(5000)
    val path = "file:///D:/personal/code/tianyafu_base/state"
    val stateBackend = new FsStateBackend(path)
    // 这里设置重启2次，每次间隔5秒     即允许挂2次，第3次挂的时候，程序彻底挂了
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(
      2, // number of restart attempts
      Time.of(5, TimeUnit.SECONDS) // delay
    ))

    env.socketTextStream("hadoop01",9527)
      .map(x => {
        if(x.contains("tianyafu")){
          throw new RuntimeException("tianya哥来了,快跑....")
        }else {
          x.toLowerCase()
        }
      })

    env.execute(getClass.getCanonicalName)
  }

}
