package com.tianyafu.flink.source

import com.tianyafu.flink.bean.Domain.Access
import com.tianyafu.flink.bykey.SpecifyingTransformationFunctionsApp.TianyafuMap
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.NumberSequenceIterator

/**
 * @Author:tianyafu
 * @Date:2021/3/30
 * @Description:
 */
object SourceApp {

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    /**
     * fromCollection
     * fromElements
     * socketTextStream
     * 这些都是单并行度的数据源
     */

    /*val stream = env.fromCollection(List(
      Access(202112120010L, "ruozedata.com", 2000),
      Access(202112120010L, "ruoze.ke.qq.com", 6000),
      Access(202112120010L, "github.com/ruozedata", 5000),
      Access(202112120010L, "ruozedata.com", 4000)
    ))

    println(stream.parallelism)

    println(stream.filter(_.traffic > 4000).parallelism)*/

    /*val stream: DataStream[Any] = env.fromElements(1, "2", 3.14D, 4F, 5L, true)

    println(stream.parallelism)

    println(stream.map(x => x).parallelism)*/

    /*val stream: DataStream[String] = env.socketTextStream("hadoop01", 9527)
    println(stream.parallelism)

    println(stream.map(x => x).parallelism)*/


    /**
     * readTextFile
     * fromParallelCollection
     * 是多并行度的数据源
     */


    /*val path = "data/access.log"
    val stream = env.readTextFile(path)
    println(stream.parallelism)
    println(stream.map(new TianyafuMap).parallelism)*/


    /*val stream = env.fromParallelCollection(new NumberSequenceIterator(1, 20))
    println(stream.parallelism)

    println(stream.map(x => x).parallelism)*/


    /**
     * 自定义单并行度的source
     */

    /*val stream: DataStream[Access] = env.addSource(new AccessSource)
    println(stream.parallelism)

    println(stream.map(x => x).parallelism)

    stream.print()*/


    /**
     * 自定义多并行度的source
     * 数据源也可以设置并行度
     */

    /*val stream: DataStream[Access] = env.addSource(new AccessSource02).setParallelism(2)
    println(stream.parallelism)

    println(stream.map(x => x).parallelism)

    stream.print()*/


    /**
     * 自定义增强的多并行度的source
     */
    val stream: DataStream[Access] = env.addSource(new AccessSource03).setParallelism(2)
    println(stream.parallelism)

    println(stream.map(x => x).setParallelism(3).parallelism)

    stream.print().setParallelism(2)


    env.execute(getClass.getSimpleName)
  }



}
