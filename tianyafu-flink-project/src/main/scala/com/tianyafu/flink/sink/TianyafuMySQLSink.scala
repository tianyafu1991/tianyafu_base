package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.AccessLogV3
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import scalikejdbc.config.DBs
import scalikejdbc.{DB, SQL}

/**
 * @Author:tianyafu
 * @Date:2021/4/2
 * @Description:
 */
class TianyafuMySQLSink extends RichSinkFunction[AccessLogV3]{


  override def open(parameters: Configuration): Unit = {
    DBs.setupAll()
  }

  override def close(): Unit = {
    DBs.closeAll()
  }

  /**
   * 这里用了replace  into  要求domain字段是唯一性约束
   * @param value
   * @param context
   */
  override def invoke(value: AccessLogV3, context: SinkFunction.Context[_]): Unit = {
    DBs.setupAll()
    DB.localTx{
      implicit session => {
        SQL("insert into access_province_cnt(time,province,cnt) values(?,?,?) on duplicate key update cnt = ?")
          .bind(value.time,value.province,value.cnt,value.cnt)
          .update().apply()
      }
    }
  }

}
