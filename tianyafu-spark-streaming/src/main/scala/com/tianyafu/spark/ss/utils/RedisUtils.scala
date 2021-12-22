package com.tianyafu.spark.ss.utils

import redis.clients.jedis.{JedisPool, JedisPoolConfig}

object RedisUtils {

  val config = new JedisPoolConfig
  config.setMaxIdle(10)
  config.setMaxTotal(1000)
  config.setMaxWaitMillis(1000)
  config.setTestOnBorrow(true)

  private val host = "sdw2"
  private val port = 16379


  private val jedisPool = new JedisPool(config, host, port)

  def getJedis = jedisPool.getResource


}
