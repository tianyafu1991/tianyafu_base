package com.tianyafu.flink.sink

import com.tianyafu.flink.bean.Domain.Access
import com.tianyafu.flink.utils.MySQLUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

import java.sql.Connection

/**
 * @Author:tianyafu
 * @Date:2021/4/2
 * @Description:
 */
class TianyafuMySQLSink extends RichSinkFunction[Access]{


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
  override def invoke(value: Access, context: SinkFunction.Context[_]): Unit = {
    DBs.setupAll()
    DB.localTx{
      implicit session => {
        SQL("replace into tianyafu_traffic(domain,traffics) values(?,?)")
          .bind(value.domain,value.traffic)
          .update().apply()
      }
    }
  }

}
