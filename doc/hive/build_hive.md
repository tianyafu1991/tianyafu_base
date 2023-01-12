# 编译Hive

## 参考博客
```
官方文档：https://cwiki.apache.org/confluence/display/Hive//GettingStarted#GettingStarted-BuildingHivefromSource
https://blog.csdn.net/qq_43081842/article/details/105728262
https://juejin.cn/post/6859995459503685639
```
```
编译版本信息
maven.version=3.6.3
java.version=1.8.0_181
windows上git版本 git.version=2.31.1
hadoop.version=2.6.0-cdh5.16.2
hive.version=1.1.0-cdh5.16.2

```

## 下载源码包
```
链接：https://pan.baidu.com/s/1E7qaC5p2t3k4bfwRAFpZsQ 
提取码：2vcv 
```

## linux服务器上编译(因windows本地的maven仓库太大太杂 所以选择在服务器上编译)
```
1.修改$HIVE_SRC_HOME/pom.xml文件：在repositories标签中新增阿里云的maven仓库地址和cloudera的仓库地址
<repository>
  <id>aliyun</id>
  <name>cloudera Repository</name>
  <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
</repository>

<repository>
  <id>cloudera</id>
  <name>cloudera Repository</name>
  <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
</repository>

2.执行编译命令
mvn clean package -DskipTests -Dmaven.javadoc.skip=true -Pdist -Phadoop-2
编译没有坑 
期间因网络原因 部分jar包可能拉取不下来
本次编译遇到如下报错
[ERROR] Failed to execute goal on project hive-serde: Could not resolve dependencies for project org.apache.hive:hive-serde:jar:1.1.0-cdh5.16.2: Could not transfer artifact com.twitter:parquet-hadoop-bundle:jar:1.5.0-cdh5.16.2 from/to cloudera (https://repository.cloudera.com/artifactory/cloudera-repos/): Transfer failed for https://repository.cloudera.com/artifactory/cloudera-repos/com/twitter/parquet-hadoop-bundle/1.5.0-cdh5.16.2/parquet-hadoop-bundle-1.5.0-cdh5.16.2.jar: Connect to jfrog-prod-use1-dedicated-virginia-main.s3.amazonaws.com:443 [jfrog-prod-use1-dedicated-virginia-main.s3.amazonaws.com/52.217.67.148] failed: Connection timed out (Connection timed out) -> [Help 1]
这个是包拉不下来 直接下载好 放到maven仓库即可

3.编译后的maven仓库分享(仅适用于编译hive为1.1.0-cdh5.16.2版本的)
链接：https://pan.baidu.com/s/1_VRdF2rT1daQKS9rhNOwBw 
提取码：wfbw 

```

## 源码导入IDEA并利用antlr生成代码
```
1.解决IDEA右侧maven工具栏依赖报红
源码导入IDEA后 IDEA右侧maven工具栏中Hive Service模块会报红 具体为org.apache.directory.server:apacheds-server-integ:1.5.6中缺少一个依赖jar(org.apache.directory.client.ldap:ldap-client-api:0.1-SNAPSHOT)
该依赖是下不下来的
解决方式:因org.apache.directory.server:apacheds-server-integ:1.5.6的scope本身即为test的 所以直接利用exclusions标签 将缺少的org.apache.directory.client.ldap:ldap-client-api排掉

打开Hive Service模块的pom.xml文件 
搜索apacheds-server-integ
在该依赖下添加:
<exclusions>
    <exclusion>
      <groupId>org.apache.directory.client.ldap</groupId>
      <artifactId>ldap-client-api</artifactId>
    </exclusion>
</exclusions>
然后刷新依赖 即可发现maven工具栏不再报红

2.生成源代码
IDEA右侧maven工具栏 
1).找到Hive Metastore 右键 Generate Sources and Update Folders
会根据Filter.g文件生成FilterLexer、FilterParser 生成的文件在target目录中 这个是Antlr生成的 前提是IDEA中 安装Antlr插件
2).找到Hive Query Language 右键 Generate Sources and Update Folders
会根据HiveParser.g、HiveLexer.g等文件生成HiveParser等类 生成的文件在target目录中， 这个是Antlr生成的 前提是IDEA中 安装Antlr插件
```

## 远程调试
```
调试的入口类 CliDriver
```



## 源码导入IDEA并本地调试
```
1.服务器上编译好的源码 拉取下来后 最好在windows本地也编译一次 
2.启动Hadoop
3.启动Hive的metastore nohup ~/app/hive/bin/hive --service metastore &
4.在Hive源码的cli子module中 创建resources目录并标记为source root
5.将服务器上的hdfs-site.xml、core-site.xml、yarn-site.xml、mapred-site.xml、hive-site.xml放到创建的resources目录下
6.在hdfs-site.xml中 加上如下配置 该参数是为了客户端使用hostname去访问datanode 不加该参数 会出现在本地执行sql语句时 卡住没有任何结果
<property>
<name>dfs.client.use.datanode.hostname</name>
<value>true</value>
</property>
7.服务器上的配置文件中 很多配置在本地调试过程中都可以删掉 因为很多是服务器端的配置 具体保留的配置在下方单独记录
8.找到org.apache.hadoop.hive.cli.CliDriver这个类 找到main方法 在main方法启动的参数中 加入-Djline.WindowsTerminal.directConsole=false
9.启动CliDriver的main方法 这样是起不来的 会报org.apache.hadoop.hive.conf.HiveConf类中 有个import org.apache.hadoop.mapred.JobConf;无法导入
这个是需要在idea右侧的maven中的Profiles中勾选hadoop-2这个profile
10.再次启动 还是起不来 org.apache.hive.service.servlet.QueryProfileServlet类中 import org.apache.hive.tmpl.QueryProfileTmpl; 无法导入
这个网上没有找到博客或issue对该问题进行说明 暂时不清楚是否有影响 这里直接将报错的import注释掉 下面的代码中用到该import的那行代码也注释掉
new QueryProfileTmpl().render(response.getWriter(), queryInfo, hiveConf);
11.再次启动 可以正常启动了
show databases;
use dw;
select * from dw.dm_enterprise_list_dd_f limit 1;
select status_code,count(1) cnt from dw.dm_enterprise_list_dd_f group by status_code; 
12.可以执行select * from dw.dm_enterprise_list_dd_f limit 1;这类没有shuffle的语句 但不能执行select status_code,count(1) cnt from dw.dm_enterprise_list_dd_f group by status_code; 
debug跟踪源码 发现是本地的yarn-site.xml需要添加yarn.resourcemanager.hostname参数 这个参数不加 默认是从yarn-default.xml中获取 默认值为0.0.0.0 而我现在是idea连接vmware上的ResourceManager
13.本地的yarn-site.xml添加yarn.resourcemanager.hostname后 再次启动 执行select status_code,count(1) cnt from dw.dm_enterprise_list_dd_f group by status_code; 
日志报错：FAILED: Execution Error, return code 2 from org.apache.hadoop.hive.ql.exec.mr.MapRedTask
通过ResourceManager的web页面看到报错Exception message: /bin/bash: 第 0 行:fg: 无任务控制
参考：https://blog.csdn.net/weixin_40453404/article/details/103383330博客 在mapred-site.xml中添加参数 这个要在服务器端的mapred-site.xml中添加 添加后重启Hadoop。 顺便本地也加一下 不加依然包这个错
14.再次启动 又报错
通过ResourceManager的web页面看到报错java.lang.RuntimeException: java.lang.ClassNotFoundException: Class org.apache.hadoop.hive.ql.io.HiveFileFormatUtils$NullOutputCommitter not found
暂未解决
```
### hdfs-site.xml
```
<configuration>
<property>
<name>dfs.replication</name>
<value>1</value>
</property>
<property>
<name>dfs.namenode.secondary.http-address</name>
<value>tianyafu:9868</value>
</property>
<property>
<name>dfs.namenode.secondary.https-address</name>
<value>tianyafu:9869</value>
</property>
<property>
<name>dfs.client.use.datanode.hostname</name>
<value>true</value>
</property>
</configuration>
```

### core-site.xml
```
<configuration>
<property>
<name>fs.defaultFS</name>
<value>hdfs://tianyafu:9000</value>
</property>
</configuration>
```

### yarn-site.xml
```
<configuration>

<!-- Site specific YARN configuration properties -->

<property>
<name>yarn.nodemanager.aux-services</name>
<value>mapreduce_shuffle</value>
</property>
<property>
<name>yarn.resourcemanager.hostname</name>
<value>tianyafu</value>
</property>
<property>
<name>yarn.resourcemanager.webapp.address</name>
<value>tianyafu:18088</value>
</property>


</configuration>
```

### mapred-site.xml
```
<configuration>
<property>
<name>mapreduce.framework.name</name>
<value>yarn</value>
</property>

</configuration>
```


### hive-site.xml
```
<configuration>
<property>
<name>hive.cli.print.current.db</name>
<value>true</value>
<description>打印当前hive库名</description>
</property>
<property>
<name>hive.cli.print.header</name>
<value>true</value>
<description>打印当前hive表字段名</description>
</property>
<property>
<name>hive.metastore.uris</name>
<value>thrift://tianyafu:9083</value>
</property>
</configuration>
```