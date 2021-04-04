package com.tianyafu.flink.parameter

import org.apache.flink.api.java.utils.ParameterTool

object ParameterToolApp {

  def main(args: Array[String]): Unit = {
    val parameters: ParameterTool = ParameterTool.fromArgs(args)

    parameters.getRequired("host") // 必填
    parameters.get("port","9527") // 选填
  }

}
