package com.tianyafu.spark.ss.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionPool {

    public static DruidDataSource dataSource = new DruidDataSource();

    public static ComboPooledDataSource dataSource2 = new ComboPooledDataSource();

    public static final String URL = "jdbc:mysql://sdw2:3306/tianyafu?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8";

    public static final String USERNAME = "tianyafu";
    public static final String PASSWORD = "tianyafu123";

    static {
        dataSource.setUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setMaxActive(40);
        dataSource.setMinIdle(5);
        dataSource.setInitialSize(10);
        dataSource.setMaxOpenPreparedStatements(100);

        dataSource2.setJdbcUrl(URL);
        dataSource2.setUser(USERNAME);
        dataSource2.setPassword(PASSWORD);
        dataSource2.setMaxPoolSize(40);
        dataSource2.setMinPoolSize(5);
        dataSource2.setInitialPoolSize(10);
        dataSource2.setMaxStatements(100);
    }

    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection2() {
        try {
            return dataSource2.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    public static void returnConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void closeStatement(Statement statement) {
        if (null != statement) {
            try {
                statement.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                statement = null;
            }
        }
    }

    public static void returnConnection2(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}
