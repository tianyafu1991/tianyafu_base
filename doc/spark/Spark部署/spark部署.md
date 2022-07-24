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

[root@hadoop001 conf]# vim spark-env.sh
SPARK_LOCAL_IP=hadoop001
HADOOP_CONF_DIR=/etc/hadoop/conf
[root@hadoop001 conf]# ln -s /etc/hive/conf/hive-site.xml hive-site.xml
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








