# DolphinScheduler部署

## 文档
```
参考:
https://dolphinscheduler.apache.org/en-us/docs/3.1.9/guide/installation/pseudo-cluster
https://github.com/apache/dolphinscheduler/blob/3.1.9-release/docs/docs/en/guide/howto/datasource-setting.md
https://dolphinscheduler.apache.org/en-us/docs/3.1.9/guide/resource/configuration
https://www.cnblogs.com/route/p/17294234.html
https://blog.csdn.net/DolphinScheduler/article/details/136149408?spm=1001.2101.3001.6650.3&utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7Ebaidujs_baidulandingword%7ECtr-3-136149408-blog-135149352.235%5Ev43%5Epc_blog_bottom_relevance_base5&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7Ebaidujs_baidulandingword%7ECtr-3-136149408-blog-135149352.235%5Ev43%5Epc_blog_bottom_relevance_base5&utm_relevant_index=4
https://stonecoding.net/bigdata/dolphinscheduler/dolphinscheduler.html
```

## 环境准备
```
1.MySQL5.7
2.对应的驱动包
3.JDK1.8并配置JAVA_HOME
4.ZK
```

# 前置工作
```shell 
# 添加用户
[root@sdw2 ~]# useradd admin
# 配置admin用户的密码
[root@sdw2 ~]# echo "admin" | passwd --stdin admin
# 配置sudo权限并无需密码
[root@sdw2 ~]# echo 'admin  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
[root@sdw2 ~]# sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers
# 配置ssh信任关系
[root@sdw2 ~]# su - admin
[admin@sdw2 ~]$ ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
[admin@sdw2 ~]$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
[admin@sdw2 ~]$ chmod 600 ~/.ssh/authorized_keys
```


## 安装依赖下载并解压
```shell
[admin@sdw2 ~]$ sudo yum install –y psmisc
[admin@sdw2 ~]$ mkdir app bin data dw lib log script shell software sourcecode tmp
[admin@sdw2 ~]$ cd ~/software/
[admin@sdw2 software]$ wget https://dlcdn.apache.org/dolphinscheduler/3.2.1/apache-dolphinscheduler-3.2.1-bin.tar.gz
[admin@sdw2 software]$ tar -zxvf ~/software/apache-dolphinscheduler-3.2.1-bin.tar.gz -C ~/app/
[admin@sdw2 software]$ cd ~/app/
[admin@sdw2 app]$ ln -s apache-dolphinscheduler-3.2.1-bin dolphinscheduler-bin
```

## 拷贝MySQL驱动包
```
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/standalone-server/libs/standalone-server/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/tools/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/alert-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/api-server/libs/
```

### 创建数据库
```sql
mysql -uroot -p
drop database if exists dolphinscheduler;
CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'dolphinscheduler'@'%' IDENTIFIED BY 'dolphinscheduler123';
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'dolphinscheduler'@'localhost' IDENTIFIED BY 'dolphinscheduler123';
flush privileges;
```

## 修改配置文件
```shell
[admin@sdw2 app]$ vim ~/app/dolphinscheduler-bin/bin/env/install_env.sh
ips=${ips:-"sdw2"}
sshPort=${sshPort:-"22"}
masters=${masters:-"sdw2"}
workers=${workers:-"sdw2:default"}
alertServer=${alertServer:-"sdw2"}
apiServers=${apiServers:-"sdw2"}
installPath=${installPath:-"/home/admin/app/dolphinscheduler"}
deployUser=${deployUser:-"admin"}
zkRoot=${zkRoot:-"/dolphinscheduler"}

[admin@sdw2 app]$ vim ~/app/dolphinscheduler-bin/bin/env/dolphinscheduler_env.sh
export JAVA_HOME=${JAVA_HOME:-/usr/java/jdk1.8.0_181}
# for mysql
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://sdw2:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME=dolphinscheduler
export SPRING_DATASOURCE_PASSWORD=dolphinscheduler123
# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}
export MASTER_FETCH_COMMAND_NUM=${MASTER_FETCH_COMMAND_NUM:-10}
# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-sdw2:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/cloudera/parcels/CDH/lib/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/etc/hadoop/conf}
#export SPARK_HOME1=${SPARK_HOME1:-/opt/soft/spark1}
export SPARK_HOME2=${SPARK_HOME2:-/application/spark}
export PYTHON_LAUNCHER=${PYTHON_HOME:-/usr/bin/python}
export HIVE_HOME=${HIVE_HOME:-/opt/cloudera/parcels/CDH/lib/hive}
#export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_LAUNCHER=${DATAX_HOME:-/application/data-center/datax/bin/datax.py}
#export SEATUNNEL_HOME=${SEATUNNEL_HOME:-/opt/soft/seatunnel}
#export CHUNJUN_HOME=${CHUNJUN_HOME:-/opt/soft/chunjun}

#export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$SEATUNNEL_HOME/bin:$CHUNJUN_HOME/bin:$PATH
export PATH=$HADOOP_HOME/bin:$SPARK_HOME2/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$DATAX_LAUNCHER:$PATH
```

## 初始化数据库
```shell 
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/tools/bin/upgrade-schema.sh
```

## 调整jar包版本
```shell 
# 参考 https://www.cnblogs.com/route/p/17294234.html
# Caused by: java.lang.ClassNotFoundException: org.apache.commons.cli.DefaultParser 该报错要调整zk的jar包
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/api-server/libs/zookeeper-3.8.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/master-server/libs/zookeeper-3.8.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/worker-server/libs/zookeeper-3.8.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/alert-server/libs/zookeeper-3.8.0.jar
[admin@sdw2 app]$ cp ~/lib/zookeeper-3.4.6.jar ~/app/dolphinscheduler-bin/api-server/libs/
[admin@sdw2 app]$ cp ~/lib/zookeeper-3.4.6.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/zookeeper-3.4.6.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/zookeeper-3.4.6.jar ~/app/dolphinscheduler-bin/alert-server/libs/

# Caused by: org.apache.zookeeper.KeeperException$UnimplementedException: KeeperErrorCode = Unimplemented for /dolphinscheduler/nodes/master
# 该报错要调整curator的jar包
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/api-server/libs/curator-client-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/api-server/libs/curator-framework-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/api-server/libs/curator-recipes-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/master-server/libs/curator-client-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/master-server/libs/curator-framework-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/master-server/libs/curator-recipes-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/worker-server/libs/curator-client-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/worker-server/libs/curator-framework-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/worker-server/libs/curator-recipes-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/alert-server/libs/curator-client-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/alert-server/libs/curator-framework-5.3.0.jar
[admin@sdw2 app]$ rm ~/app/dolphinscheduler-bin/alert-server/libs/curator-recipes-5.3.0.jar

[admin@sdw2 app]$ cp ~/lib/curator-client-4.2.0.jar ~/app/dolphinscheduler-bin/api-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-framework-4.2.0.jar ~/app/dolphinscheduler-bin/api-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-recipes-4.2.0.jar ~/app/dolphinscheduler-bin/api-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-client-4.2.0.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-framework-4.2.0.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-recipes-4.2.0.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-client-4.2.0.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-framework-4.2.0.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-recipes-4.2.0.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-client-4.2.0.jar ~/app/dolphinscheduler-bin/alert-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-framework-4.2.0.jar ~/app/dolphinscheduler-bin/alert-server/libs/
[admin@sdw2 app]$ cp ~/lib/curator-recipes-4.2.0.jar ~/app/dolphinscheduler-bin/alert-server/libs/
```

## 第一次安装伪分布式
```shell 
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/bin/install.sh
```

## 后续启停伪分布式
```shell 
[admin@sdw2 app]$ bash ~/app/dolphinscheduler/bin/start-all.sh
[admin@sdw2 app]$ jps -l
32160 org.apache.dolphinscheduler.server.worker.WorkerServer
32433 org.apache.dolphinscheduler.api.ApiApplicationServer
32024 org.apache.dolphinscheduler.server.master.MasterServer
32297 org.apache.dolphinscheduler.alert.AlertServer
32796 sun.tools.jps.Jps
[admin@sdw2 app]$ bash ~/app/dolphinscheduler/bin/status-all.sh


====================== dolphinscheduler server config =============================
1.dolphinscheduler server node config hosts:[  sdw2  ]
2.master server node config hosts:[  sdw2  ]
3.worker server node config hosts:[  sdw2:default  ]
4.alert server node config hosts:[  sdw2  ]
5.api server node config hosts:[  sdw2  ]


====================== dolphinscheduler server status =============================
node server state


sdw2  Begin status master-server......
master-server  [  RUNNING  ]
End status master-server.
sdw2  Begin status worker-server......
worker-server  [  RUNNING  ]
End status worker-server.
sdw2  Begin status alert-server......
alert-server  [  RUNNING  ]
End status alert-server.
sdw2  Begin status api-server......
api-server  [  RUNNING  ]
End status api-server.

[admin@sdw2 app]$ bash ~/app/dolphinscheduler/bin/stop-all.sh
```

## 配置资源中心
```shell
# 参考:https://dolphinscheduler.apache.org/en-us/docs/3.1.9/guide/resource/configuration
[admin@sdw2 app]$ vim ~/app/dolphinscheduler/api-server/conf/common.properties
resource.storage.type=HDFS
resource.storage.upload.base.path=/dolphinscheduler_tyf
resource.hdfs.root.user=hdfs
resource.hdfs.fs.defaultFS=hdfs://mdw:8020
resource.manager.httpaddress.port=8088
yarn.resourcemanager.ha.rm.ids=
yarn.application.status.address=http://mdw:%s/ws/v1/cluster/apps/%s
yarn.job.history.status.address=http://mdw:19888/ws/v1/history/mapreduce/jobs/%s

[admin@sdw2 app]$ vim ~/app/dolphinscheduler/worker-server/conf/common.properties
resource.storage.type=HDFS
resource.storage.upload.base.path=/dolphinscheduler_tyf
resource.hdfs.root.user=hdfs
resource.hdfs.fs.defaultFS=hdfs://mdw:8020
resource.manager.httpaddress.port=8088
yarn.resourcemanager.ha.rm.ids=
yarn.application.status.address=http://mdw:%s/ws/v1/cluster/apps/%s
yarn.job.history.status.address=http://mdw:19888/ws/v1/history/mapreduce/jobs/%s

```

## 整合CDH Hive
```shell 
# 在添加Hive数据源时 报Required field 'client_protocol' is unset错误 是dolphinscheduler 的Hive 相关的jar包与CDH 中Hive相关的jar包版本冲突引起的 
[admin@sdw2 app]$ mv ~/app/dolphinscheduler/api-server/libs/hive-* /tmp
[admin@sdw2 app]$ mv ~/app/dolphinscheduler/worker-server/libs/hive-* /tmp
# 拷贝CDH hive下的jar包到对应的lib目录 并赋权

[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-jdbc-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-metastore-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-serde-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-service-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-0.23-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-scheduler-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-beeline-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/api-server/libs
[admin@sdw2 app]$ chown -R admin:admin ~/app/dolphinscheduler/api-server/libs/*

[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-jdbc-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-metastore-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-serde-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-service-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-0.23-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-common-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-shims-scheduler-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ cp /opt/cloudera/parcels/CDH/lib/hive/lib/hive-beeline-1.1.0-cdh5.16.1.jar ~/app/dolphinscheduler/worker-server/libs
[admin@sdw2 app]$ chown -R admin:admin ~/app/dolphinscheduler/worker-server/libs/*
```