package com.tianyafu.spark.utils

import org.apache.commons.lang3.time.FastDateFormat

import java.text.SimpleDateFormat

object DateUtils {

  val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val format2 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  def parse(time:String) : Long = {
    if(""==time){
      0L
    }else{
      format2.parse(time).getTime
    }
  }

}
