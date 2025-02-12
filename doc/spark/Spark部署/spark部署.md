# 部署spark
## 解压并创建软连接
```shell
[root@hadoop001 ~]# cd 
[root@hadoop001 ~]# tar -xvf ~/software/spark-2.4.6-bin-2.6.0-cdh5.16.2.tgz -C ~/app/
[root@hadoop001 ~]# cd app/
[root@hadoop001 app]# ln -s spark-2.4.6-bin-2.6.0-cdh5.16.2 spark
[root@hadoop001 app]# cd ~/app/spark/conf/
```

## 配置
```shell
[root@hadoop001 conf]# cp spark-defaults.conf.template spark-defaults.conf
[root@hadoop001 conf]# cp spark-env.sh.template spark-env.sh
[root@hadoop001 conf]# cp log4j.properties.template log4j.properties
# 配置spark-env.sh
[root@hadoop001 conf]# vim spark-env.sh
SPARK_LOCAL_IP=hadoop001
HADOOP_CONF_DIR=/etc/hadoop/conf
# 软连接hive-site.xml
[root@hadoop001 conf]# ln -s /etc/hive/conf/hive-site.xml hive-site.xml

# 将spark的jars目录下的jar 打成一个压缩包 并上传到hdfs上
[root@hadoop001 conf]# cd ~/app/spark/jars/
[root@hadoop001 jars]# zip -r spark-jars.zip ./*.jar
[root@hadoop001 jars]# hdfs dfs -mkdir /spark_jars
[root@hadoop001 jars]# hdfs dfs -put spark-jars.zip /spark_jars
[root@hadoop001 jars]# cd ~/app/spark/conf/
# 配置spark-defaults.conf
[root@hadoop001 conf]# vim spark-defaults.conf
# add by tianyafu@20221213  这是一种优化 spark任务在执行时 会将$SPARK_HOME/jars下面的jar包 打成一个临时zip包 上传到HDFS上 如果每个任务执行时 都需要这一步 浪费网络资源 所以将$SPARK_HOME/jars下面的jar包 统一打成一个zip包 上传到HDFS上 这样Spark任务在检测到有该参数后 就不会再将jar包打成临时zip包并上传了
# 也可以通过配置spark.yarn.jars参数实现该优化 spark.yarn.jars这个参数要的是一个hdfs的路径 所以把jar包上传到该hdfs路径下即可
spark.yarn.archive hdfs:///spark_jars/spark-jars.zip
#spark.yarn.jars    hdfs:///tmp/tianyafu/spark-jars/*.jar

```

## 分发
```shell
[root@hadoop001 ~]# scp -r ~/app/spark-2.4.6-bin-2.6.0-cdh5.16.2 hadoop001:/root/app/
[root@hadoop001 ~]# scp -r ~/app/spark-2.4.6-bin-2.6.0-cdh5.16.2 hadoop003:/root/app/
```

## 配置环境变量
```shell
[root@hadoop001 conf]# echo -e '# SPARK ENV\nexport SPARK_HOME=/root/app/spark\nexport PATH=$SPARK_HOME/bin:$PATH\nexport PYSPARK_PYTHON=python3' >> /etc/profile
[root@hadoop001 conf]# source /etc/profile
```

## 测试
```shell
[root@hadoop003 ~]# spark-sql --master yarn
spark-sql> select * from hadoop.test_spark;
20/10/20 13:41:56 INFO scheduler.TaskSetManager: Finished task 0.0 in stage 0.0 (TID 0) in 5421 ms on hadoop002 (executor 2) (2/2)
20/10/20 13:41:56 INFO cluster.YarnScheduler: Removed TaskSet 0.0, whose tasks have all completed, from pool 
20/10/20 13:41:56 INFO scheduler.DAGScheduler: ResultStage 0 (processCmd at CliDriver.java:376) finished in 5.513 s
20/10/20 13:41:56 INFO scheduler.DAGScheduler: Job 0 finished: processCmd at CliDriver.java:376, took 5.579704 s
1       ruoze
2       pk
3       xingxing
4       jepson
5       hadoop
Time taken: 7.565 seconds, Fetched 5 row(s)
20/10/20 13:41:56 INFO thriftserver.SparkSQLCLIDriver: Time taken: 7.565 seconds, Fetched 5 row(s)
```

## 配置Spark History Server
```
# 参考https://spark.apache.org/docs/2.4.6/monitoring.html、https://spark.apache.org/docs/2.4.6/running-on-yarn.html、https://spark.apache.org/docs/2.4.6/configuration.html
# spark-defaults.conf文件中

# 指定Hive Metastore版本和jar包路径
spark.sql.hive.metastore.version=1.1.0
spark.sql.hive.metastore.jars=/opt/cloudera/parcels/CDH/lib/hive/lib/*

# 将Spark的jar上传到HDFS上
spark.yarn.archive=hdfs:///etl/lib/spark-jars.zip

# 开启动态分区
spark.sql.sources.partitionOverwriteMode=dynamic

# 开启Spark History
spark.eventLog.enabled=true
# Spark Application日志写入到该目录 该目录要手动创建 并设置目录权限为drwxrwxrwxt
spark.eventLog.dir=hdfs:///tmp/logs/spark/applicationHistory
# Spark History从该目录中读取Spark Application的日志 该目录应与spark.eventLog.dir的一致
spark.history.fs.logDirectory=hdfs:///tmp/logs/spark/applicationHistory
# 调整 Spark History的web ui 端口
spark.history.ui.port=28080
# 开启Spark History 日志定期删除
spark.history.fs.cleaner.enabled=true
# 日志开启压缩
spark.eventLog.compress=true

# Spark On Yarn
# 打通Yarn ResourceManager UI和Spark History Server
spark.yarn.historyServer.address=sdw2:28080
# 如果spark web ui被禁用 就使用history server的web ui
spark.yarn.historyServer.allowTracking=true
spark.master=yarn

# 调整序列化
spark.serializer=org.apache.spark.serializer.KryoSerializer

# 开启CBO
spark.sql.cbo.enabled=true
spark.sql.cbo.starSchemaDetection=true

# 以下几个参数是3.x版本才支持
spark.eventLog.rolling.enabled=true
spark.eventLog.rolling.maxFileSize=128m
# 开启driver的日志记录
spark.driver.log.persistToDfs.enabled=true
spark.driver.log.dfsDir=hdfs:///tmp/logs/spark/driverLogs

# adaptive相关参数
spark.sql.adaptive.enabled=true
spark.sql.adaptive.forceApply=false
spark.sql.adaptive.logLevel=info
spark.sql.adaptive.advisoryPartitionSizeInBytes=256m
spark.sql.adaptive.coalescePartitions.enabled=true
spark.sql.adaptive.coalescePartitions.initialPartitionNum=1024
spark.sql.adaptive.fetchShuffleBlocksInBatch=true
spark.sql.adaptive.localShuffleReader.enabled=true
spark.sql.adaptive.skewJoin.enabled=true
spark.sql.adaptive.skewJoin.skewedPartitionFactor=5
spark.sql.adaptive.skewJoin.skewedPartitionThresholdInBytes=128m
spark.sql.adaptive.nonEmptyPartitionRatioForBroadcastJoin=0.2
spark.sql.autoBroadcastJoinThreshold=209715200

# ORC参数
spark.sql.orc.mergeSchema=true
```






