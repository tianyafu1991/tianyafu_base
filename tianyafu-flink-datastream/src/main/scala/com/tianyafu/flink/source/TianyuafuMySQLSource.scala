package com.tianyafu.flink.source

import com.tianyafu.flink.bean.Domain.{Access, Student}
import com.tianyafu.flink.utils.MySQLUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichSourceFunction, SourceFunction}

import java.sql.{Connection, PreparedStatement}
import scala.util.Random

class TianyuafuMySQLSource extends RichSourceFunction[Student]{


  var conn :Connection= _

  var stat:PreparedStatement = _


  override def open(parameters: Configuration): Unit = {
    conn = MySQLUtils.getConnection()
    stat = conn.prepareStatement("select * from student")
  }

  override def run(ctx: SourceFunction.SourceContext[Student]): Unit = {
    val rs = stat.executeQuery()

    while (rs.next()){
      val student = Student(rs.getInt("id"), rs.getString("name"), rs.getInt("age"))
      ctx.collect(student)
    }
  }

  override def cancel(): Unit = ???
}
