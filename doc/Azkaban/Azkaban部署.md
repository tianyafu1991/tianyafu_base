# Azkaban部署

## 拷贝编译好的包到~/software目录
```shell
[admin@sdw2 ~]$ cp ~/sourcecode/azkaban-3.81.0/azkaban-exec-server/build/distributions/azkaban-exec-server-0.1.0-SNAPSHOT.tar.gz ~/software/
[admin@sdw2 ~]$ cp ~/sourcecode/azkaban-3.81.0/azkaban-web-server/build/distributions/azkaban-web-server-0.1.0-SNAPSHOT.tar.gz ~/software/
[admin@sdw2 ~]$ cp ~/sourcecode/azkaban-3.81.0/azkaban-db/build/distributions/azkaban-db-0.1.0-SNAPSHOT.tar.gz ~/software/
[admin@sdw2 ~]$ cp ~/sourcecode/azkaban-3.81.0/azkaban-solo-server/build/distributions/azkaban-solo-server-0.1.0-SNAPSHOT.tar.gz ~/software/
```

## 解压并创建软连接
```shell
[admin@sdw2 ~]$ cd ~/software/
[admin@sdw2 software]$ tar -zxvf azkaban-web-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[admin@sdw2 software]$ tar -zxvf azkaban-exec-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[admin@sdw2 software]$ tar -xvf azkaban-solo-server-0.1.0-SNAPSHOT.tar.gz -C ~/app/
[admin@sdw2 software]$ tar -xvf azkaban-db-0.1.0-SNAPSHOT.tar.gz -C ~/app/

[admin@sdw2 software]$ cd ~/app/
[admin@sdw2 app]$ ln -s azkaban-exec-server-0.1.0-SNAPSHOT azkaban-exec-server
[admin@sdw2 app]$ ln -s azkaban-web-server-0.1.0-SNAPSHOT azkaban-web-server
[admin@sdw2 app]$ ln -s azkaban-solo-server-0.1.0-SNAPSHOT azkaban-solo-server
[admin@sdw2 app]$ ln -s azkaban-db-0.1.0-SNAPSHOT azkaban-db

```

## 拷贝Azkaban的元数据表的ddl sql文件到/tmp目录下
```shell
[admin@sdw2 app]$ cp ~/app/azkaban-db/create-all-sql-0.1.0-SNAPSHOT.sql /tmp
```

## 创建Azkaban元数据库
```
[admin@sdw2 app]$ sudo su - mysqladmin
mysql> create database azkaban default character set utf8 DEFAULT COLLATE utf8_general_ci;
Query OK, 1 row affected (0.00 sec)

mysql> grant all privileges on azkaban.* to azkaban@'%' identified by 'azkaban';
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> flush PRIVILEGES;
Query OK, 0 rows affected (0.01 sec)

mysql> use azkaban
Database changed

mysql> source /tmp/create-all-sql-0.1.0-SNAPSHOT.sql

mysql> show tables;
+-----------------------------+
| Tables_in_azkaban           |
+-----------------------------+
| active_executing_flows      |
| active_sla                  |
| execution_dependencies      |
| execution_flows             |
| execution_jobs              |
| execution_logs              |
| executor_events             |
| executors                   |
| project_events              |
| project_files               |
| project_flow_files          |
| project_flows               |
| project_permissions         |
| project_properties          |
| project_versions            |
| projects                    |
| properties                  |
| qrtz_blob_triggers          |
| qrtz_calendars              |
| qrtz_cron_triggers          |
| qrtz_fired_triggers         |
| qrtz_job_details            |
| qrtz_locks                  |
| qrtz_paused_trigger_grps    |
| qrtz_scheduler_state        |
| qrtz_simple_triggers        |
| qrtz_simprop_triggers       |
| qrtz_triggers               |
| ramp                        |
| ramp_dependency             |
| ramp_exceptional_flow_items |
| ramp_exceptional_job_items  |
| ramp_items                  |
| triggers                    |
| validated_dependencies      |
+-----------------------------+
35 rows in set (0.00 sec)

mysql> quit;
Bye
```

## 配置第一台机器的环境变量
```shell
[admin@sdw2 ~]$ vim ~/.bashrc

# AZKABAN_ENV
export AZKABAN_EXECUTOR_HOME=/home/admin/app/azkaban-exec-server
export AZKABAN_WEB_HOME=/home/admin/app/azkaban-web-server
export PATH=${AZKABAN_EXECUTOR_HOME}/bin:${AZKABAN_WEB_HOME}/bin:$PATH
[admin@sdw2 ~]$ source ~/.bashrc 
```

## 配置Azkaban Executor
```shell
[admin@sdw2 ~]$ cd $AZKABAN_EXECUTOR_HOME/
[admin@sdw2 azkaban-exec-server]$ mkdir logs extlib
[admin@sdw2 azkaban-exec-server]$ vim conf/azkaban.properties
azkaban.name=test_env_azkaban
azkaban.label=this is test env azkaban
default.timezone.id=Asia/Shanghai
azkaban.webserver.url=http://sdw2:8081
database.type=mysql
mysql.port=3306
mysql.host=sdw2
mysql.database=azkaban
mysql.user=root
mysql.password=root
executor.port=12321

[admin@sdw2 azkaban-exec-server]$ vim plugins/jobtypes/commonprivate.properties
# add by tianyafu
azkaban.native.lib=false
# 修改启动脚本 重定向日志文件到$AZKABAN_EXECUTOR_HOME/logs目录中
[admin@sdw2 azkaban-exec-server]$ vim bin/start-exec.sh
#!/bin/bash

script_dir=$(dirname $0)

# pass along command line arguments to the internal launch script.
${script_dir}/internal/internal-start-executor.sh "$@" >${script_dir}/../logs/executorServerLog__`date +%F+%T`.out 2>&1 &
```

## 部署 Azkaban Web
```shell
[admin@sdw2 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME/
[admin@sdw2 azkaban-web-server]$ mkdir logs extlib plugins temp
[admin@sdw2 azkaban-web-server]$ vim conf/azkaban.properties
azkaban.name=test_env_azkaban
azkaban.label=this is test env azkaban
default.timezone.id=Asia/Shanghai
database.type=mysql
mysql.port=3306
mysql.host=sdw2
mysql.database=azkaban
mysql.user=root
mysql.password=root
# 修改启动脚本 重定向日志文件到$AZKABAN_WEB_HOME/logs目录中
[admin@sdw2 azkaban-web-server]$ vim bin/start-web.sh
#!/bin/bash

script_dir=$(dirname $0)

${script_dir}/internal/internal-start-web.sh >${script_dir}/../logs/webServerLog_`date +%F+%T`.out 2>&1 &
```

## 分发 executor 的目录到其他节点上并配置软连接
```shell
[admin@sdw2 azkaban-web-server]$ cd ~/app/
[admin@sdw2 app]$ scp -r azkaban-exec-server-0.1.0-SNAPSHOT mdw:~/app/
[admin@sdw2 app]$ scp -r azkaban-exec-server-0.1.0-SNAPSHOT sdw1:~/app/
[admin@mdw ~]$ ln -s ~/app/azkaban-exec-server-0.1.0-SNAPSHOT ~/app/azkaban-exec-server
[admin@sdw1 ~]$ ln -s ~/app/azkaban-exec-server-0.1.0-SNAPSHOT ~/app/azkaban-exec-server

```

## 配置其他2个节点的环境变量
```shell
[admin@mdw ~]$ vim ~/.bashrc 
# AZKABAN_ENV
export AZKABAN_EXECUTOR_HOME=/home/admin/app/azkaban-exec-server
export PATH=${AZKABAN_EXECUTOR_HOME}/bin:$PATH
[admin@mdw ~]$ source ~/.bashrc
```

## 启动Executor
```shell
# 各个节点启动executor
[admin@sdw2 app]$ cd $AZKABAN_EXECUTOR_HOME
[admin@sdw2 azkaban-exec-server]$ ./bin/start-exec.sh
```


## 激活executor
```shell
curl -G "mdw:12321/executor?action=activate" && echo
curl -G "sdw1:12321/executor?action=activate" && echo
curl -G "sdw2:12321/executor?action=activate" && echo
```

## 启动web
```shell
[admin@sdw2 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME
[admin@sdw2 azkaban-web-server]$ ./bin/start-web.sh
```

## 关闭Azkaban集群
```shell
[admin@hadoop001 ~]$ cd $AZKABAN_WEB_HOME
[admin@hadoop001 azkaban-web-server]$ ./bin/shutdown-web.sh
[admin@hadoop001 azkaban-web-server]$ cd $AZKABAN_EXECUTOR_HOME
[admin@hadoop001 azkaban-exec-server]$ ./bin/shutdown-exec.sh 

```

## 踩坑
```shell
# 参考:https://www.jianshu.com/p/f539585a4612
https://github.com/azkaban/azkaban/issues/2124

```