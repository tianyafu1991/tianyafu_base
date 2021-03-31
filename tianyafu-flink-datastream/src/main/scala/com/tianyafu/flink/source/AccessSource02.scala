package com.tianyafu.flink.source

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.streaming.api.functions.source.{ParallelSourceFunction, SourceFunction}

import scala.util.Random

class AccessSource02 extends ParallelSourceFunction[Access]{

  var isRunning = true

  override def run(ctx: SourceFunction.SourceContext[Access]): Unit = {
    val random = new Random()
    val domains = Array("ruozedata.com","ruoze.ke.qq.com","github.com/ruozedata")
    while (isRunning){
      val timestamp = System.currentTimeMillis()
      1.to(10) .map {
        x => ctx.collect(Access(timestamp,domains(random.nextInt(domains.length)),random.nextInt(1000)+x))
      }

      Thread.sleep(5000)
    }
  }

  override def cancel(): Unit = isRunning= false
}
