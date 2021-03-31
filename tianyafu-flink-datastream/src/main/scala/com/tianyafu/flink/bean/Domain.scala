package com.tianyafu.flink.bean

object Domain {

  case class Access(time:Long,domain:String,traffic:Double)

  case class Student(id:Int,name:String,age:Int)

}
