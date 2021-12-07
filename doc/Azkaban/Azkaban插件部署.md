# Azkaban插件部署

## 拷贝插件包到对应的目录
```shell
# 该目录在$AZKABAN_EXECUTOR_HOME/conf/azkaban.properties的参数azkaban.jobtype.plugin.dir配置
[admin@sdw2 packages]$ cp ~/sourcecode/azkaban-plugins-3.0.0/dist/jobtype/packages/azkaban-jobtype-3.0.0.tar.gz /home/admin/app/azkaban-exec-server/plugins/jobtypes/

```

## 解压
```shell
[admin@sdw2 packages]$ cd
[admin@sdw2 ~]$ cd /home/admin/app/azkaban-exec-server/plugins/jobtypes/
[admin@sdw2 jobtypes]$ tar -xvf azkaban-jobtype-3.0.0.tar.gz
# 备份commonprivate.properties
[admin@sdw2 jobtypes]$ mv commonprivate.properties commonprivate.properties.bak
# 移动所有插件文件到/home/admin/app/azkaban-exec-server/plugins/jobtypes/下
[admin@sdw2 jobtypes]$ mv azkaban-jobtype-3.0.0/* ~/app/azkaban-exec-server/plugins/jobtypes/

# 删除pig相关的目录
[admin@sdw2 jobtypes]$ rm -rf ./pig*

```

## 配置全局插件配置
```shell
[admin@sdw2 jobtypes]$ vim commonprivate.properties
hadoop.home=/opt/cloudera/parcels/CDH/lib/hadoop
execute.as.user=false
azkaban.native.lib=false
# add by tianyafu @20211206
azkaban.should.proxy=false
obtain.binary.token=false
hive.home=/opt/cloudera/parcels/CDH/lib/hive
spark.home=/application/spark
hadoop.classpath=${hadoop.home}/etc/hadoop
jobtype.global.classpath=${hadoop.home}/etc/hadoop
```

## 配置Hive 插件
```shell
[admin@sdw2 jobtypes]$ vim hive/plugin.properties
# add by tianyafu @20211206
hive.aux.jars.path=${hive.home}/auxlib

[admin@sdw2 jobtypes]$ vim hive/private.properties
# add by tianyafu @20211206
hive.aux.jar.path=${hive.home}/auxlib
jobtype.classpath=${hadoop.home}/etc/hadoop,${hadoop.home}/share/hadoop/common/*,${hadoop.home}/share/hadoop/common/lib/*,${hadoop.home}/share/hadoop/hdfs/*,${hadoop.home}/share/hadoop/hdfs/lib/*,${hadoop.home}/share/hadoop/yarn/*,${hadoop.home}/share/hadoop/yarn/lib/*,${hadoop.home}/share/hadoop/mapreduce/*,${hadoop.home}/share/hadoop/mapreduce/lib/*,${hive.home}/lib/*,${hive.home}/conf,${hive.aux.jar.path}/*

```

## 插件分发到其它2个节点上
```
# 先删除掉其它2个节点上的
[admin@mdw jobtypes]$ rm ~/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties 
[admin@sdw1 jobtypes]$ rm ~/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties 
# 分发
[admin@mdw ~]$ scp -r ~/app/azkaban-exec-server/plugins/jobtypes/* mdw:~/app/azkaban-exec-server/plugins/jobtypes/
[admin@sdw1 ~]$ scp -r ~/app/azkaban-exec-server/plugins/jobtypes/* sdw1:~/app/azkaban-exec-server/plugins/jobtypes/

```