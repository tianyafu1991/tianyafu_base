package com.tianyafu.flink.etl

import com.tianyafu.flink.bean.Access
import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.streaming.api.scala.{DataStream, OutputTag, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaProducer}
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.apache.flink.streaming.connectors.fs.StringWriter
import org.apache.flink.streaming.connectors.fs.bucketing.{BucketingSink, DateTimeBucketer}
import org.apache.flink.util.Collector

import java.time.ZoneId
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * 如果一个kafka的topic中 是有多个不同业务的日志数据，日志数据中带有那个业务线的标识
 * 可以使用测流输出的方式，将不同的日志分开后， 写入到不同的topic中
 * 分流的demo
 */
object SplitEtlApp {

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    val kafkaSource = new FlinkKafkaConsumer[String]("",new SimpleStringSchema(),properties)
    val stream = env.addSource(kafkaSource).map(toAccess(_))

    val aProducer = new FlinkKafkaProducer[String]("","topica",new SimpleStringSchema())
    val bProducer = new FlinkKafkaProducer[String]("","topicb",new SimpleStringSchema())

    val aOutputTag = new OutputTag[Access]("a")
    val bOutputTag = new OutputTag[Access]("b")

    val allStream: DataStream[Access] = stream.process(new ProcessFunction[Access, Access] {
      override def processElement(value: Access, ctx: ProcessFunction[Access, Access]#Context, out: Collector[Access]): Unit = {
        val biz = value.biz

        if ("a" == biz) {
          ctx.output(aOutputTag, value)
        } else if ("b" == biz) {
          ctx.output(bOutputTag, value)
        } else {
          out.collect(value)
        }
      }
    })

    allStream.getSideOutput(aOutputTag).map(_.toString).addSink(aProducer)
    allStream.getSideOutput(bOutputTag).map(_.toString).addSink(bProducer)

    // 写出到HDFS 上 这个代码详见：https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/connectors/filesystem_sink.html
    // 这种写法是过时写法
    /*val sink = new BucketingSink[String]("/base/path")
    sink.setBucketer(new DateTimeBucketer("yyyy-MM-dd--HHmm", ZoneId.of("Asia/Shanghai")))
    sink.setWriter(new StringWriter[String])
    sink.setBatchSize(1024 * 1024 * 400) // this is 400 MB,
    sink.setBatchRolloverInterval(20 * 60 * 1000); // this is 20 mins

    allStream.map(_.toString).addSink(sink)*/


    /*https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/connectors/streamfile_sink.html*/
    val outputPath = "/base/path"
    val sink: StreamingFileSink[String] = StreamingFileSink
      .forRowFormat(new Path(outputPath), new SimpleStringEncoder[String]("UTF-8"))
      .withRollingPolicy(
        DefaultRollingPolicy.builder()
          .withRolloverInterval(TimeUnit.MINUTES.toMillis(15))
          .withInactivityInterval(TimeUnit.MINUTES.toMillis(5))
          .withMaxPartSize(1024 * 1024 * 1024)
          .build())
      .build()

    allStream.map(_.toString).addSink(sink)




    env.execute(getClass.getSimpleName)
  }

  def toAccess(x:String):Access = {
    Access(1L,"","")
  }

}
