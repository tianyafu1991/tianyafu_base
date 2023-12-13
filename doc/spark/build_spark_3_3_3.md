# 编译Spark3.3.3
```
编译版本信息
spark.version=3.3.3
scala.version=2.12.15
maven.version=3.8.6
java.version=1.8.0_181
windows上git版本 git.version=2.17.0
hadoop.version=3.0.0-cdh6.3.1
hive.version=2.1.1-cdh6.3.1

```

##下载源码包
```
https://archive.apache.org/dist/spark/spark-3.3.3/spark-3.3.3.tgz
```

## 在 git bash中编译
```
1.修改$SPARK_HOME/pom.xml文件：在repositories标签中新增阿里云的maven仓库地址和cloudera的仓库地址 其他原有的repo地址可以去掉 也可以留着
<repository>  
	<id>alimaven</id>  
	<name>aliyun maven</name>  
	<url>http://maven.aliyun.com/nexus/content/groups/public/</url>  
	<releases>  
		<enabled>true</enabled>  
	</releases>  
	<snapshots>  
		<enabled>false</enabled>  
	</snapshots>  
</repository>
<repository>
	<id>cloudera</id>
	<url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
</repository>

2.修改$SPARK_HOME/dev/make-distribution.sh脚本
(1)显式指定一些版本信息，跳过编译时的一些比较耗时的版本检查，提高编译效率
显式指定一下版本信息
VERSION=3.3.3
SCALA_VERSION=2.12
SPARK_HADOOP_VERSION=3.0.0-cdh6.3.1
SPARK_HIVE=1

并将脚本中原来的检查给注释掉(就是下面这段)
#VERSION=$("$MVN" help:evaluate -Dexpression=project.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SCALA_VERSION=$("$MVN" help:evaluate -Dexpression=scala.binary.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SPARK_HADOOP_VERSION=$("$MVN" help:evaluate -Dexpression=hadoop.version $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | tail -n 1)
#SPARK_HIVE=$("$MVN" help:evaluate -Dexpression=project.activeProfiles -pl sql/hive $@ 2>/dev/null\
#    | grep -v "INFO"\
#    | grep -v "WARNING"\
#    | fgrep --count "<id>hive</id>";\
#    # Reset exit status to 0, otherwise the script stops here if the last grep finds nothing\
#    # because we use "set -o pipefail"
#    echo -n)

(2)增加编译时的内存设置，防止OOM
修改这一行的内存配置，我这里统一放大了一倍
export MAVEN_OPTS="${MAVEN_OPTS:--Xmx4g -XX:ReservedCodeCacheSize=2g}"

3.修改scala的版本
# ./dev/change-scala-version.sh 2.12

4.编译期间需要下载(这个是在$SPARK_HOME/build/mvn的脚本中指定的)
4.1
修改主pom文件中的 scala_version  和 scala.binary.version
因为$SPARK_HOME/build/mvn脚本中的scala_version变量是取的项目主pom中的scala.version的值 所以最好是修改主pom中的该值，否则编译出来安装在本地仓库的spark-core等jar包都是2.11.12的版本scala(spark2.4.6默认scala.version为2.11.12)
4.2
这个zinc的版本和scala的版本要看一下mvn这个脚本确认一下
https://downloads.lightbend.com/zinc/0.3.15/zinc-0.3.15.tgz
https://downloads.lightbend.com/scala/2.12.15/scala-2.12.15.tgz
有时候下载比较慢，提前下载好之后放在$SPARK_HOME/build下面就行

5.注意：如果我们需要用编译好的spark包拿去部署，则这一步不建议在这里做，毕竟依赖一个hive-site.xml进去不好。
用IDEA导入源码，找到$SPARK_HOME/sql/hive-thriftserver这个Module，在里面新建一个resources目录，并右键标记位resources，将$Hive_HOME/conf下面的hive-site.xml拷贝到resources
这个步骤是在IDEA中完成的，如果跳过此步，不影响编译，但是运行org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver时找的是内置的Hive，源数据库是Derby
而且如果编译完再在IDEA中添加hive-site.xml，即使rebuild，这个文件也没有被编译到target目录，即不生效。到时候还是需要重新编译

6.编译：
./dev/make-distribution.sh --name 3.0.0-cdh6.3.1 --tgz -Phadoop-3 -Dhadoop.version=3.0.0-cdh6.3.1 -Pscala-2.12 -Dscala.version=2.12.15 -Phive -Phive-thriftserver -Pyarn

```

## 部署
```shell
[admin@mdw conf]$ cat spark-defaults.conf

# 指定Hive Metastore版本和jar包路径
spark.sql.hive.metastore.version=2.1.1
spark.sql.hive.metastore.jars=/opt/cloudera/parcels/CDH/lib/hive/lib/*
# 开启CBO
spark.sql.cbo.enabled=true
spark.sql.cbo.starSchemaDetection=true
# ORC参数
spark.sql.orc.mergeSchema=true
# 开启动态分区
spark.sql.sources.partitionOverwriteMode=dynamic
# 开启Spark History
spark.eventLog.enabled=true
spark.eventLog.dir=hdfs:///tmp/logs/spark/applicationHistory
spark.history.ui.port=48080
spark.history.fs.logDirectory=hdfs:///tmp/logs/spark/applicationHistory
spark.eventLog.rolling.enabled=true
spark.eventLog.rolling.maxFileSize=128m
spark.eventLog.compress=true
# 开启driver的日志记录
spark.driver.log.persistToDfs.enabled=false
spark.driver.log.dfsDir=hdfs:///tmp/logs/spark/driverLogs
# 开启Spark History 日志定期删除
spark.history.fs.cleaner.enabled=true
# Spark On Yarn
spark.yarn.historyServer.address=http://mdw:48080
spark.yarn.historyServer.allowTracking=true
spark.master=yarn
# 调整序列化
spark.serializer=org.apache.spark.serializer.KryoSerializer

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
spark.sql.autoBroadcastJoinThreshold=-1

# 将Spark的jar上传到HDFS上
spark.yarn.archive=hdfs:///spark_jars/spark-jars.zip

```