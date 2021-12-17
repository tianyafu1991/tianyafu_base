package com.tianyafu.spark.ss.utils

import java.sql.{Connection, DriverManager}


object MySQLUtils {

  def getConnection() = {
    Class.forName("com.mysql.jdbc.Driver")
    DriverManager.getConnection("jdbc:mysql://sdw2:3306/tianyafu?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8","tianyafu","tianyafu123")
  }

  def closeConnection(connection:Connection): Unit = {
    if(null != connection) {
      connection.close()
    }
  }

}
