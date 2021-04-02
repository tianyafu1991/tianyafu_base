package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.Domain.Access
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction

/**
 * @Author:tianyafu
 * @Date:2021/4/2
 * @Description:
 */
class TianyafuMySQLSink extends RichSinkFunction[Access]{

}
