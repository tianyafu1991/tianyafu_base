# dolphinScheduler整合CDHhive

## 移除dolphinScheduler自带的hive的jar
```shell
[admin@sdw2 ~]$ cd ~/app/dolphinscheduler/lib
[admin@sdw2 lib]$ mv ./hive* /tmp

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
