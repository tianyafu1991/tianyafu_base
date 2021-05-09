package com.tianyafu.flink.utils

import org.apache.commons.lang3.time.FastDateFormat

object DateUtils {

  val format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  val target = FastDateFormat.getInstance("yyyyMMddHH")

  def getTime(time:String) : String = {
    target.format(format.parse(time))
  }

}
