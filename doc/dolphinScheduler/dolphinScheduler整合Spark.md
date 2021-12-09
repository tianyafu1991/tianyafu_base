# dolphinScheduler整合Spark

## 需要启动Spark的thriftserver
```shell
sh /application/spark/sbin/start-thriftserver.sh \
--hiveconf hive.server2.thrift.port=10001 \
--master yarn \
--deploy-mode client \
--name spark_thriftserver_4_test_env \
--driver-memory 1G \
--executor-memory 1G \
--executor-cores 1 \
--num-executors 2

```

## 拷贝CDH hive下的jar包到~/app/dolphinscheduler/lib 并赋权
```shell
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-jdbc-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-metastore-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-serde-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-service-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-0.23-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-scheduler-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-beeline-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ chown -R admin:admin ~/app/dolphinscheduler/lib/*

```

## 重启集群
```shell
[admin@sdw2 lib]$ sh ~/app/dolphinscheduler/bin/stop-all.sh
[admin@sdw2 lib]$ sh ~/app/dolphinscheduler/bin/start-all.sh
```

## 坑
```shell
因测试环境中配置了SPARK_HOME   如果要使用hive 的beeline 需要指定全路径 否则默认是使用SPARK_HOME/bin/下的beeline  如下:


#/bin/bash

SQL="SELECT flag_of_holiday_id,count(1) cnt FROM jhd_dw.dim_date_ids_yy_i group by flag_of_holiday_id order by cnt desc"

echo "hive cli......."
hive -e "${SQL}"

echo "hive beeline........"
/opt/cloudera/parcels/CDH/lib/hive/bin/beeline -n hive -u "jdbc:hive2://mdw:10000/jhd_dw"  hive -e "${SQL}"
```