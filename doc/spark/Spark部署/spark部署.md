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








