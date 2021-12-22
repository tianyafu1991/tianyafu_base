package com.tianyafu.spark.ss.utils;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 **/
public class JavaRedisUtils {

    private static JedisPool jedisPool = null;

    private static String host = "sdw2";
    private static int port = 16379;

    public static Jedis getJedis(){
        if(null == jedisPool) {
            GenericObjectPoolConfig config = new JedisPoolConfig();
            config.setMaxIdle(10);
            config.setMaxTotal(1000);
            config.setMaxWaitMillis(1000);
            config.setTestOnBorrow(true);
            jedisPool = new JedisPool(config, host, port);
        }

        return jedisPool.getResource();
    }

}
