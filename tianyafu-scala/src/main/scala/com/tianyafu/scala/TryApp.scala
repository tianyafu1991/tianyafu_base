package com.tianyafu.scala

import scala.util.Try


object TryApp{

  def main(args: Array[String]): Unit = {
    Try(println(1 / 1)) recover{
      case e:Exception => println("recover!!!!!!!!!!!")
    }
  }

  def tryCatchFinally: Unit = {
    try {
      val i: Int = 1 / 0
      println(i)
    } catch {
      case e: Exception => println("catch!!!!!!!!!!!")
    } finally {
      println("finally!!!!!!!!!!!")
    }
  }
}
