package com.tianyafu.flink

package object bean {

  case class  AccessLog(domain:String,userId:String,time:String)

  case class AccessLogV2(time:String, domain:String, province:String, city:String)

  case class AccessLogV3(time:String,  province:String, cnt:Long)

  case class Access(time:Long ,domain:String, biz:String)

}
