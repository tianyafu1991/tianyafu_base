package com.tianyafu.flink.source

import java.sql.{Connection, PreparedStatement}

import com.tianyafu.flink.bean.Domain.Student
import com.tianyafu.flink.utils.MySQLUtils
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, RichSourceFunction, SourceFunction}
import scalikejdbc.{DB, SQL}
import scalikejdbc.config.DBs

class TianyuafuMySQLSource02 extends RichParallelSourceFunction[Student]{

  override def open(parameters: Configuration): Unit = {
    DBs.setupAll()
  }

  override def close(): Unit = {
    DBs.closeAll()
  }


  override def run(ctx: SourceFunction.SourceContext[Student]): Unit = {
    DB.readOnly {
      implicit session => {
        SQL("SELECT * FROM student").map(
          rs => {
            val student = Student(rs.int("id"),rs.string("name"),rs.int("age"))
            ctx.collect(student)
          }
        ).list().apply()
      }
    }

  }

  override def cancel(): Unit = ???
}
