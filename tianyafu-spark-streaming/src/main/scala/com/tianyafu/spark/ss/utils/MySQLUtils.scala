package com.tianyafu.spark.ss.utils

import java.sql.{Connection, DriverManager, Statement}


object MySQLUtils {

  def getConnection() = {
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection("jdbc:mysql://sdw2:3306/tianyafu?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8","tianyafu","tianyafu123")
  }

  def closeStatement(statement: Statement): Unit = {
    if(null != statement) {
      statement.close()
    }
  }

  def closeConnection(connection:Connection): Unit = {
    if(null != connection) {
      connection.close()
    }
  }

}
