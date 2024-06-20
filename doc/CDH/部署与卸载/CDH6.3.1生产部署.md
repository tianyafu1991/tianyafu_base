# 20231129CDH6.3.1生产部署

## 

## 创建 CDH 相关的元数据库
```
# 进入docker
[root@data_server ~]# docker exec -it mysqldb /bin/bash
# 进入MySQL shell
root@ae85d53577be:/# mysql -uroot -p'xxx'

create database cmf DEFAULT CHARACTER SET utf8;
create database amon DEFAULT CHARACTER SET utf8;
grant all on cmf.* TO 'cmf'@'%' IDENTIFIED BY 'xxx';
grant all on amon.* TO 'amon'@'%' IDENTIFIED BY 'xxx';
flush privileges;
```

## 部署MySQL驱动包
```shell
# 生产环境原有CDH5.16.1 所以该驱动包已存在
[root@mdw cdh6.3.1]# ll /usr/share/java/
total 984
-rw-r--r-- 1 root root 1007505 May  4  2023 mysql-connector-java.jar
# 考虑到mdw上服务有点多 在sdw2上也放一份 将CDH自身的监控进程 可以部署在sdw2上
[root@sdw2 cloudera-scm-agent]# mkdir -p /usr/share/java/
[root@mdw java]# scp /usr/share/java/mysql-connector-java.jar sdw2:/usr/share/java/
```



## 部署CM Server
```shell
# 3台节点一起创建目录并解压
[root@mdw ~]# cd /root/cdh6.3.1
[root@mdw cdh6.3.1]# mkdir /opt/cloudera-manager
[root@mdw cdh6.3.1]# tar -xzvf cm6.3.1-redhat7.tar.gz -C /opt/cloudera-manager/

# 选取mdw机器作为cm server
[root@mdw cdh6.3.1]# cd /opt/cloudera-manager/cm6.3.1/RPMS/x86_64
# 这个daemons包中 是一堆包的集合 比较大 所以安装稍慢 
[root@mdw x86_64]# rpm -ivh cloudera-manager-daemons-6.3.1-1466458.el7.x86_64.rpm --nodeps --force
warning: cloudera-manager-daemons-6.3.1-1466458.el7.x86_64.rpm: Header V3 RSA/SHA256 Signature, key ID b0b19c9f: NOKEY
Verifying...                          ################################# [100%]
Preparing...                          ################################# [100%]
Updating / installing...
   1:cloudera-manager-daemons-6.3.1-14################################# [100%]
[root@mdw x86_64]# rpm -ivh cloudera-manager-server-6.3.1-1466458.el7.x86_64.rpm --nodeps --force
warning: cloudera-manager-server-6.3.1-1466458.el7.x86_64.rpm: Header V3 RSA/SHA256 Signature, key ID b0b19c9f: NOKEY
Verifying...                          ################################# [100%]
Preparing...                          ################################# [100%]
Updating / installing...
   1:cloudera-manager-server-6.3.1-146################################# [100%]
Created symlink /etc/systemd/system/multi-user.target.wants/cloudera-scm-server.service → /usr/lib/systemd/system/cloudera-scm-server.service.

# 修改cm server的配置文件
[root@mdw x86_64]# vim /etc/cloudera-scm-server/db.properties
com.cloudera.cmf.db.type=mysql
com.cloudera.cmf.db.host=xxx:3306
com.cloudera.cmf.db.name=cmf
com.cloudera.cmf.db.user=cmf
com.cloudera.cmf.db.password=xxx
com.cloudera.cmf.db.setupType=EXTERNAL

[root@mdw x86_64]# systemctl start cloudera-scm-server
[root@mdw x86_64]# tail -200f /var/log/cloudera-scm-server/cloudera-scm-server.log

# 只要7180端口起了就行 因为还没有部署agent 所以web页面上先不着急操作
[root@mdw x86_64]# systemctl status cloudera-scm-server
● cloudera-scm-server.service - Cloudera CM Server Service
   Loaded: loaded (/usr/lib/systemd/system/cloudera-scm-server.service; enabled; vendor preset: disabled)
   Active: active (running) since Wed 2023-11-29 11:59:46 CST; 4min 8s ago

```

## 部署CM Agent
```shell
[root@sdw2 cdh6.3.1]# cd /opt/cloudera-manager/cm6.3.1/RPMS/x86_64
# mdw上该daemons包在部署CM Server时 已经安装了 所以只要在sdw1和sdw2上部署即可 同样需要耐心等一会
[root@sdw1 x86_64]# rpm -ivh cloudera-manager-daemons-6.3.1-1466458.el7.x86_64.rpm --nodeps --force
# 3台机器 部署agent
[root@mdw x86_64]# rpm -ivh cloudera-manager-agent-6.3.1-1466458.el7.x86_64.rpm --nodeps --force
warning: cloudera-manager-agent-6.3.1-1466458.el7.x86_64.rpm: Header V3 RSA/SHA256 Signature, key ID b0b19c9f: NOKEY
Verifying...                          ################################# [100%]
Preparing...                          ################################# [100%]
Failed to stop cloudera-scm-agent.service: Unit cloudera-scm-agent.service not loaded.
Failed to disable unit: Unit file cloudera-scm-agent.service does not exist.
Updating / installing...
   1:cloudera-manager-agent-6.3.1-1466################################# [100%]
[29/Nov/2023 12:11:45 +0000] 3634252 MainThread upgrade      WARNING  No existing supervisor found: No supervisor config present
Created symlink /etc/systemd/system/multi-user.target.wants/cloudera-scm-agent.service → /usr/lib/systemd/system/cloudera-scm-agent.service.
Created symlink /etc/systemd/system/multi-user.target.wants/supervisord.service → /usr/lib/systemd/system/supervisord.service.
# 3台机器 调整配置文件 将server指向mdw 以下命令中的cloudera server hostname需要替换成具体的hostname
[root@mdw x86_64]# sed -i "s/server_host=localhost/server_host={cloudera server hostname}/g" /etc/cloudera-scm-agent/config.ini
# 3台机器 启动agent
[root@mdw x86_64]# systemctl start cloudera-scm-agent
# 查看状态
[root@mdw x86_64]# systemctl status cloudera-scm-agent
● cloudera-scm-agent.service - Cloudera Manager Agent Service
   Loaded: loaded (/usr/lib/systemd/system/cloudera-scm-agent.service; enabled; vendor preset: disabled)
   Active: active (running) since Wed 2023-11-29 12:11:45 CST; 3min 9s ago
   
[root@mdw cdh6.3.1]# tail -200f /var/log/cloudera-scm-agent/cloudera-scm-agent.log

```
## 配置离线parcel源
```shell
# 选择mdw作为httpd的服务器
[root@mdw x86_64]# yum install -y httpd
[root@mdw x86_64]# mkdir -p /var/www/html/cdh6_parcel

[root@mdw x86_64]# cd /root/cdh6.3.1/
[root@mdw cdh6.3.1]#  cp CDH-6.3.1-1.cdh6.3.1.p0.1470567-el7.parcel /var/www/html/cdh6_parcel/CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel
[root@mdw cdh6.3.1]# cp CDH-6.3.1-1.cdh6.3.1.p0.1470567-el7.parcel.sha1 /var/www/html/cdh6_parcel/CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel.sha
[root@mdw cdh6.3.1]# cp manifest.json /var/www/html/cdh6_parcel/
[root@mdw cdh6.3.1]# vim /var/www/html/cdh6_parcel/manifest.json
将CDH-6.3.1-1.cdh6.3.1.p0.1470567-el7.parcel 改为 CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel

[root@mdw cdh6.3.1]# cd /var/www/html/cdh6_parcel/
[root@mdw cdh6_parcel]# sha1sum CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel
9955cf590d67baa4fe7462bf319db695cae35efc  CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel
[root@mdw cdh6_parcel]# cat CDH-6.3.1-1.cdh6.3.1.p0.1470567-unknown.parcel.sha
9955cf590d67baa4fe7462bf319db695cae35efc
# 启动httpd服务
[root@mdw cdh6.3.1]# systemctl start httpd
# 浏览器访问http://mdw/cdh6_parcel 可以看到对应的3个文件
```

## WEB UI 上操作
```shell
其中一步是将parcel源改为http://mdw/cdh6_parcel/
```

## Hive元数据相关操作
```sql
-- 修改建库时的库注释的字符集
alter table DBS default character set utf8 COLLATE utf8_general_ci; -- 库信息相关表
alter table DBS modify `DESC` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复表字段注释中文乱码问题

-- 修改表的字符集
alter table COLUMNS_V2 default character set utf8 COLLATE utf8_general_ci; -- 表字段信息相关表
alter table TABLE_PARAMS default character set utf8 COLLATE utf8_general_ci; -- 表属性相关表
alter table PARTITION_KEYS default character set utf8 COLLATE utf8_general_ci; -- 分区key相关表
alter table TBLS default character set utf8 COLLATE utf8_general_ci; -- Hive表信息相关表



-- 修改表字段的字符集
alter table COLUMNS_V2 modify `COMMENT` varchar(256) character set utf8 COLLATE utf8_general_ci; -- 修复表字段注释中文乱码问题
alter table TABLE_PARAMS modify `PARAM_VALUE` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复表注释中文乱码问题
alter table PARTITION_KEYS modify `PKEY_COMMENT` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复分区字段注释中文乱码问题

-- 视图DDL语句的字符集
alter table TBLS modify `VIEW_EXPANDED_TEXT` mediumtext character set utf8 COLLATE utf8_general_ci; -- 修复视图DDL中文乱码问题
alter table TBLS modify `VIEW_ORIGINAL_TEXT` mediumtext character set utf8 COLLATE utf8_general_ci; -- 修复视图DDL中文乱码问题



```

## Bug修复

### 修复alternatives目录下的软链接错误的问题
```shell
# 因是先卸载了CDH5.16.1 再重新部署了CDH6.3.1 部署完后 没有hdfs  hive等命令 参考https://blog.51cto.com/u_15349750/3706537 解决问题
[root@mdw alternatives]# ll /etc/alternatives/ | grep CDH
[root@mdw alternatives]# cd /etc/alternatives/
alternatives --set avro-tools  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/avro-tools
alternatives --set beeline  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/beeline
alternatives --set bigtop-detect-javahome  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/bigtop-detect-javahome
alternatives --set catalogd  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/catalogd
alternatives --set cli_mt  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/cli_mt
alternatives --set cli_st  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/cli_st
alternatives --set flume-ng  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/flume-ng
alternatives --set flume-ng-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/flume-ng/conf.empty
alternatives --set hadoop  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hadoop
alternatives --set hadoop-0.20  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hadoop-0.20
alternatives --set hadoop-fuse-dfs  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hadoop-fuse-dfs
alternatives --set hadoop-httpfs-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hadoop-httpfs/conf.empty
alternatives --set hadoop-kms-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hadoop-kms/conf.dist
alternatives --set hbase  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hbase
alternatives --set hbase-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hbase/conf.dist
alternatives --set hbase-indexer  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hbase-indexer
alternatives --set hbase-indexer-sentry  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hbase-indexer-sentry
alternatives --set hbase-solr-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hbase-solr/conf.dist
alternatives --set hcat  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hcat
alternatives --set hdfs  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hdfs
alternatives --set hive  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hive
alternatives --set hive-hcatalog-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hive-hcatalog/conf.dist
alternatives --set hiveserver2  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/hiveserver2
alternatives --set hive-webhcat-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hive-webhcat/conf.dist
alternatives --set hue-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/hue/conf.empty
alternatives --set impala-collect-diagnostics  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/impala-collect-diagnostics
alternatives --set impala-collect-minidumps  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/impala-collect-minidumps
alternatives --set impala-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/impala/conf.dist
alternatives --set impalad  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/impalad
alternatives --set impala-shell  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/impala-shell
alternatives --set kafka-acls  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-acls
alternatives --set kafka-broker-api-versions  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-broker-api-versions
alternatives --set kafka-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/kafka/conf.dist
alternatives --set kafka-configs  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-configs
alternatives --set kafka-console-consumer  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-console-consumer
alternatives --set kafka-console-producer  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-console-producer
alternatives --set kafka-consumer-groups  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-consumer-groups
alternatives --set kafka-consumer-perf-test  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-consumer-perf-test
alternatives --set kafka-delegation-tokens  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-delegation-tokens
alternatives --set kafka-delete-records  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-delete-records
alternatives --set kafka-dump-log  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-dump-log
alternatives --set kafka-log-dirs  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-log-dirs
alternatives --set kafka-mirror-maker  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-mirror-maker
alternatives --set kafka-preferred-replica-election  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-preferred-replica-election
alternatives --set kafka-producer-perf-test  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-producer-perf-test
alternatives --set kafka-reassign-partitions  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-reassign-partitions
alternatives --set kafka-replica-verification  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-replica-verification
alternatives --set kafka-run-class  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-run-class
alternatives --set kafka-sentry  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-sentry
alternatives --set kafka-streams-application-reset  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-streams-application-reset
alternatives --set kafka-topics  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-topics
alternatives --set kafka-verifiable-consumer  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-verifiable-consumer
alternatives --set kafka-verifiable-producer  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kafka-verifiable-producer
alternatives --set kite-dataset  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kite-dataset
alternatives --set kudu  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kudu
alternatives --set kudu-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/kudu/conf.dist
alternatives --set kudu-master  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kudu-master
alternatives --set kudu-tserver  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/kudu-tserver
alternatives --set load_gen  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/load_gen
alternatives --set mapred  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/mapred
alternatives --set oozie  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/oozie
alternatives --set parquet-tools  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/parquet-tools
alternatives --set pig  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/pig
alternatives --set pig-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/pig/conf.dist
alternatives --set pyspark  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/pyspark
alternatives --set sentry  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sentry
alternatives --set sentry-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/sentry/conf.dist
alternatives --set solr-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/solr/conf.dist
alternatives --set solrctl  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/solrctl
alternatives --set spark-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/spark/conf.dist
alternatives --set spark-executor  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/spark-executor
alternatives --set spark-shell  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/spark-shell
alternatives --set spark-submit  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/spark-submit
alternatives --set sqoop  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop
alternatives --set sqoop-codegen  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-codegen
alternatives --set sqoop-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/sqoop/conf.dist
alternatives --set sqoop-create-hive-table  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-create-hive-table
alternatives --set sqoop-eval  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-eval
alternatives --set sqoop-export  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-export
alternatives --set sqoop-help  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-help
alternatives --set sqoop-import  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-import
alternatives --set sqoop-import-all-tables  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-import-all-tables
alternatives --set sqoop-job  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-job
alternatives --set sqoop-list-databases  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-list-databases
alternatives --set sqoop-list-tables  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-list-tables
alternatives --set sqoop-merge  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-merge
alternatives --set sqoop-metastore  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-metastore
alternatives --set sqoop-version  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/sqoop-version
alternatives --set statestored  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/statestored
alternatives --set yarn  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/yarn
alternatives --set zookeeper-client  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/zookeeper-client
alternatives --set zookeeper-conf  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/etc/zookeeper/conf.dist
alternatives --set zookeeper-security-migration  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/zookeeper-security-migration
alternatives --set zookeeper-server  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/zookeeper-server
alternatives --set zookeeper-server-cleanup  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/zookeeper-server-cleanup
alternatives --set zookeeper-server-initialize  /opt/cloudera/parcels/CDH-6.3.1-1.cdh6.3.1.p0.1470567/bin/zookeeper-server-initialize
```

### 修复 Hive 不能读取高版本 ORC 写入的数据
```shell
# 当使用 Hive 读取由 Presto 或者 Spark 等写入的 ORC 文件时，会出现以下错误
Caused by: java.lang.RuntimeException: ORC split generation failed with exception: java.lang.ArrayIndexOutOfBoundsException: 6
# 参考：https://my.oschina.net/u/4565392/blog/5264848
# 从https://github.com/pan3793/cdh-hive/releases/tag/v2.1.1-cdh6.3.1-fix上拉取对应的jar包
# 修复步骤:
# 1.CDH 页面先停止cluster 再停止Cloudera Management Service 再停各个节点的CM Agent 最后停 CM Server
[root@mdw ~]# systemctl stop cloudera-scm-agent
[root@mdw ~]# systemctl status cloudera-scm-agent
● cloudera-scm-agent.service - Cloudera Manager Agent Service
   Loaded: loaded (/usr/lib/systemd/system/cloudera-scm-agent.service; enabled; vendor preset: disabled)
   Active: inactive (dead) since Thu 2023-11-30 09:37:34 CST; 10s ago
[root@mdw ~]# systemctl stop cloudera-scm-server
[root@mdw ~]# systemctl status cloudera-scm-server
● cloudera-scm-server.service - Cloudera CM Server Service
   Loaded: loaded (/usr/lib/systemd/system/cloudera-scm-server.service; enabled; vendor preset: disabled)
   Active: failed (Result: exit-code) since Thu 2023-11-30 09:38:54 CST; 33s ago

# 2. 备份CDH本身的jar
mv /opt/cloudera/parcels/CDH/jars/hive-exec-2.1.1-cdh6.3.1.jar /root/tianyafu/bak/
mv /opt/cloudera/parcels/CDH/jars/hive-orc-2.1.1-cdh6.3.1.jar /root/tianyafu/bak/
# 2.1 发送到其他几台机器
scp /root/tianyafu/hive-exec-2.1.1-cdh6.3.1.jar sdw1:/root/tianyafu
scp /root/tianyafu/hive-exec-2.1.1-cdh6.3.1.jar sdw2:/root/tianyafu
scp /root/tianyafu/hive-orc-2.1.1-cdh6.3.1.jar sdw1:/root/tianyafu
scp /root/tianyafu/hive-orc-2.1.1-cdh6.3.1.jar sdw2:/root/tianyafu
# 查看
ll /root/tianyafu/hive-*
# 将对应的jar复制到CDH对应的目录
cp /root/tianyafu/hive-exec-2.1.1-cdh6.3.1.jar /opt/cloudera/parcels/CDH/jars/
cp /root/tianyafu/hive-orc-2.1.1-cdh6.3.1.jar /opt/cloudera/parcels/CDH/jars/

```

### 修复CDH log4j的漏洞
```
参考:https://github.com/cloudera/cloudera-scripts-for-log4j
https://blog.csdn.net/weixin_41760085/article/details/122219419
```
