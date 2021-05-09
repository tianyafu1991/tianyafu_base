package com.tianyafu.flink.etl.async

import com.alibaba.druid.pool.DruidDataSource
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala.{AsyncDataStream, DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.async.{ResultFuture, RichAsyncFunction}

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.concurrent.{Callable, ExecutorService, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}

object UserDomainMappingAsyncApp {

  /**
   * 2022-07-10 18:10:13,222.55.57.83,ruozedata.com
     2022-07-10 18:10:13,114.246.50.2,ruoze.ke.qq.com
     2022-07-10 18:10:13,222.55.57.83,google.com
   * @param args
   */
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    env.setParallelism(1)

    val stream = env.socketTextStream("hadoop01", 9527)

    // apply the async I/O transformation
    val resultStream: DataStream[String] = AsyncDataStream.unorderedWait(stream, new AsyncMySQLRequest(), 1000, TimeUnit.MILLISECONDS)

    resultStream.print()

    env.execute("")
  }
}

// https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/stream/operators/asyncio.html
class AsyncMySQLRequest extends RichAsyncFunction[String, String] {

  var executorService: ExecutorService = _
  var dataSource:DruidDataSource = _

  /** The context used for the future callbacks */
  implicit lazy val executor: ExecutionContext = ExecutionContext.fromExecutor(
    org.apache.flink.runtime.concurrent.Executors.directExecutor()
  )

  override def open(parameters: Configuration): Unit = {
    executorService = java.util.concurrent.Executors.newFixedThreadPool(20)
    dataSource = new DruidDataSource()
    dataSource.setDriverClassName("com.mysql.jdbc.Driver")
    dataSource.setUsername("root")
    dataSource.setPassword("root")
    dataSource.setUrl("jdbc:mysql://hadoop01:3306/tianyafu?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8")
    dataSource.setInitialSize(5)
    dataSource.setMinIdle(10)
    dataSource.setMaxActive(20)
  }

  override def close(): Unit = {
    if(null != dataSource){
      dataSource.close()
    }

    if(null != executorService){
      executorService.shutdown()
    }
  }

  override def asyncInvoke(input: String, resultFuture: ResultFuture[String]): Unit = {
    val future: java.util.concurrent.Future[String] = executorService.submit(new Callable[String] {
      override def call(): String = {
        query(input)
      }
    })

    val resultFutureRequested: Future[String] = Future {
      future.get()
    }

    resultFutureRequested.onSuccess {
      case result: String => resultFuture.complete(Iterable(result))
    }
  }

  def query(domain:String):String =  {
    var connection: Connection = null
    var pstat : PreparedStatement = null
    var rs :ResultSet = null
    var result = "-"

    try{
      connection = dataSource.getConnection
      pstat= connection.prepareStatement("select user_id from user_domain_mapping where domain = ?")
      pstat.setString(1,domain)
      rs = pstat.executeQuery()
      if(rs.next()){
        result = rs.getString(1)
      }
    }finally {
      if(null != rs) rs.close()
      if(null != pstat) pstat.close()
      if(null != connection) connection.close()

    }

    result
  }
}
