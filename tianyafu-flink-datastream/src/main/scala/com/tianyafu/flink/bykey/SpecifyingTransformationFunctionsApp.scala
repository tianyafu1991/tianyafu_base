package com.tianyafu.flink.bykey

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.api.common.functions.{FilterFunction, RichMapFunction, RuntimeContext}
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

/**
 * @Author:tianyafu
 * @Date:2021/3/30
 * @Description:
 */
object SpecifyingTransformationFunctionsApp {

  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val path = "data/access.log"

    /*val stream: DataStream[Access] = env.readTextFile(path).map(x => {
      val splits = x.split(",")
      Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
    })*/

    env.setParallelism(2)
    val stream: DataStream[Access] = env.readTextFile(path).map(new TianyafuMap)
    stream.print()

//    functionFilter(stream)

//    stream.filter(new TianyafuFilter).print()

    env.execute(getClass.getSimpleName)
  }

  def functionFilter(stream: DataStream[Access] ):Unit = {
    stream.filter(_.traffic > 4000).print()
  }

  class TianyafuFilter extends FilterFunction[Access] {
    override def filter(t: Access): Boolean = t.traffic > 4000
  }

  class TianyafuMap extends RichMapFunction[String,Access] {

    override def open(parameters: Configuration): Unit = {
      println("~~~~~~~~~~~open invoke ~~~~~~~~~~~~~")
    }

    override def close(): Unit = {
      println("!!!!!!!!!!!!!close invoke!!!!!!!!!!!!!!!")
    }



    override def map(in: String): Access = {
      println("------map Function invoke----------")
      val splits = in.split(",")
      Access(splits(0).trim.toLong, splits(1).trim, splits(2).trim.toDouble)
    }
  }



}
