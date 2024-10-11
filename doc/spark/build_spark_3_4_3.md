# 编译Spark3.4.3
```
编译版本信息
spark.version=3.4.3
scala.version=2.12.17
maven.version=3.8.6
java.version=1.8.0_181
windows上git版本 git.version=2.17.0
hadoop.version=2.6.0-cdh5.16.1
hive.version=1.1.0-cdh5.16.1

```

##下载源码包
```
https://archive.apache.org/dist/spark/spark-3.4.3/spark-3.4.3.tgz
```

## 在 git bash中编译
```
1.修改$SPARK_HOME/pom.xml文件：在repositories标签中新增阿里云的maven仓库地址和cloudera的仓库地址
<repository>
  <id>aliyun</id>
  <name>cloudera Repository</name>
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
  <name>cloudera Repository</name>
  <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
    <releases>
    <enabled>true</enabled>
  </releases>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
</repository>


<pluginRepository>
  <id>aliyun</id>
  <name>aliyun mirror</name>
  <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
  <releases>
    <enabled>true</enabled>
  </releases>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
</pluginRepository>

<pluginRepository>
  <id>cloudera</id>
  <name>cloudera mirror</name>
  <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
  <releases>
    <enabled>true</enabled>
  </releases>
  <snapshots>
    <enabled>false</enabled>
  </snapshots>
</pluginRepository>



2.修改$SPARK_HOME/dev/make-distribution.sh脚本
(1)显式指定一些版本信息，跳过编译时的一些比较耗时的版本检查，提高编译效率
显式指定一下版本信息
VERSION=3.4.3
SCALA_VERSION=2.12
SPARK_HADOOP_VERSION=2.6.0-cdh5.16.1
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
export MAVEN_OPTS="${MAVEN_OPTS:-Xss128m --Xmx4g -XX:ReservedCodeCacheSize=2g}"

3.修改scala的版本
# ./dev/change-scala-version.sh 2.12

4.编译期间需要下载(这个是在$SPARK_HOME/build/mvn的脚本中指定的)
4.1
修改主pom文件中的 scala_version  和 scala.binary.version
因为$SPARK_HOME/build/mvn脚本中的scala_version变量是取的项目主pom中的scala.version的值 所以最好是修改主pom中的该值，否则编译出来安装在本地仓库的spark-core等jar包都是2.11.12的版本scala(spark2.4.6默认scala.version为2.11.12)
4.2
https://downloads.lightbend.com/scala/2.12.17/scala-2.12.17.tgz
有时候下载比较慢，提前下载好之后放在$SPARK_HOME/build下面就行

5.注意：如果我们需要用编译好的spark包拿去部署，则这一步不建议在这里做，毕竟依赖一个hive-site.xml进去不好。
用IDEA导入源码，找到$SPARK_HOME/sql/hive-thriftserver这个Module，在里面新建一个resources目录，并右键标记位resources，将$Hive_HOME/conf下面的hive-site.xml拷贝到resources
这个步骤是在IDEA中完成的，如果跳过此步，不影响编译，但是运行org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver时找的是内置的Hive，源数据库是Derby
而且如果编译完再在IDEA中添加hive-site.xml，即使rebuild，这个文件也没有被编译到target目录，即不生效。到时候还是需要重新编译

6.编译：
./dev/make-distribution.sh --name 2.6.0-cdh5.16.1 --tgz -Phadoop-2 -Dhadoop.version=2.6.0-cdh5.16.1 -Dscala.version=2.12.17 -Phive -Phive-thriftserver -Pyarn

```
## 编译报错
```
编译到yarn module 的org.apache.spark.deploy.yarn.Client会报错
参考：
https://www.cnblogs.com/chuijingjing/p/14660893.html
https://github.com/apache/spark/pull/16884/files
https://blog.csdn.net/monkeyboy_tech/article/details/109334869
```