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
1.IDEA中新增一个Remote JVM Debug ,给个name , 填写Host 和 Port , Port填写8000
2.服务器上通过hive --debug -hiveconf hive.root.logger=WARN,CONSOLE命令 启动hive 的 shell命令客户端, 此时会停留在监听8000端口的地方
3.IDEA中debug启动第1步新增的Remote JVM Debug即可
4.在调试的入口类中 找到main方法 即可远程调试

```

## 本地调试
```
2.启动Hadoop
3.启动Hive的metastore nohup ~/app/hive/bin/hive --service metastore &
4.在Hive源码的cli子module中 创建resources目录并标记为resource root (这一步可不做)
5.将服务器上的hdfs-site.xml、core-site.xml、hive-site.xml放到创建的resources目录下 (这一步可不做)
6.如果第4步和第5步不做 则直接将hdfs-site.xml、core-site.xml、hive-site.xml这3个文件 加入到工程ql子Module的target目录下的classes目录中
hdfs-site.xml
<property>
    <name>dfs.client.use.datanode.hostname</name>
    <value>true</value>
</property>
<property>
    <name>dfs.replication</name>
    <value>3</value>
</property>

core-site.xml
<property>
    <name>fs.defaultFS</name>
    <value>hdfs://mdw:8020</value>
</property>

hive-site.xml
<property>
    <name>hive.metastore.uris</name>
    <value>thrift://mdw:9083</value>
</property>
7.找到org.apache.hadoop.hive.cli.CliDriver这个类 运行main方法
8.此时第7步无法启动 因org.apache.hive.service.servlet.QueryProfileServlet中导入的包import org.apache.hive.tmpl.QueryProfileTmpl;无法找到
在工程的service子Module的中，将src下的jamon目录Mark Directory as Sources Root 通过右侧maven导航栏中对Hive Service进行右键Generate Sources 
即可在target目录下 找到generated-jamon目录 将该目录下的QueryProfileTmpl等2个java文件 拷贝到src下的jamon目录中 即可解决org.apache.hive.service.servlet.QueryProfileServlet包导入报错的问题
9.再次运行org.apache.hadoop.hive.cli.CliDriver这个类的main方法 能启动
10.执行show databases; 控制台不会出结果。需要在main方法启动的参数中 加入-Djline.WindowsTerminal.directConsole=false 表示在Windows下不使用jline
11.执行show databases;出结果
12.执行show tables;出结果
13.执行select * from tyf_db.dim_date_ids_yy_i limit 1;出结果
14.执行select flag_of_holiday_id,count(1) cnt from tyf_db.dim_date_ids_yy_i group by flag_of_holiday_id;出结果
15.部分机器中 第14步可能会报错org.apache.hadoop.io.nativeio.NativeIO$Windows.access0(Ljava/lang/String;I)Z 
参考https://stackoverflow.com/questions/41851066/exception-in-thread-main-java-lang-unsatisfiedlinkerror-org-apache-hadoop-io 
将winutils解压到$HADOOP_HOME/bin目录下并将hadoop.dll放到C:\Windows\System32
16.执行select a.empno,a.ename,a.deptno,b.dname from tyf_db.emp a left join tyf_db.dept b on a.deptno = b.deptno; 报错
java.lang.NoClassDefFoundError: org/apache/commons/io/IOUtils
ERROR ql.Driver: FAILED: Execution Error, return code -101 from org.apache.hadoop.hive.ql.exec.mr.MapredLocalTask. org/apache/commons/io/IOUtils
```