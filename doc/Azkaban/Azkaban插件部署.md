# Azkaban插件部署

## 备份原有的commonprivate.properties
[admin@sdw2 ~]$ mv /home/admin/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties /home/admin/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties.bak

## 创建extlib目录并软连接hadoop的jar包
```shell
# 在/home/admin/app/azkaban-exec-server/bin/internal/internal-start-executor.sh脚本中会将extlib下的所有jar放到classpath下
# 在整合CDH的Hive时 即使将hadoop的相关jar包配置到了插件的全局classpath(jobtype.global.classpath)下 任务运行时依然报找不到包
# 所以需要通过软连接将hadoop的相关jar链接到extlib目录下
## 这一步3台一起做
[admin@sdw2 ~]$ mkdir /home/admin/app/azkaban-exec-server/extlib
[admin@sdw2 ~]$ ln -s /opt/cloudera/parcels/CDH/lib/hadoop/client/*.jar /home/admin/app/azkaban-exec-server/extlib
```

## 拷贝插件包到对应的目录
```shell
# 该目录在$AZKABAN_EXECUTOR_HOME/conf/azkaban.properties的参数azkaban.jobtype.plugin.dir配置
[admin@sdw2 ~]$ cp ~/sourcecode/azkaban-plugins-3.0.0/dist/jobtype/packages/azkaban-jobtype-3.0.0.tar.gz /home/admin/app/azkaban-exec-server/plugins/jobtypes/

```

## 解压
```shell
[admin@sdw2 ~]$ cd /home/admin/app/azkaban-exec-server/plugins/jobtypes/
[admin@sdw2 jobtypes]$ tar -xvf azkaban-jobtype-3.0.0.tar.gz
# 移动所有插件文件到/home/admin/app/azkaban-exec-server/plugins/jobtypes/下
[admin@sdw2 jobtypes]$ mv azkaban-jobtype-3.0.0/* ~/app/azkaban-exec-server/plugins/jobtypes/
[admin@sdw2 jobtypes]$ rm -r azkaban-jobtype-3.0.0
# 删除pig相关的目录
[admin@sdw2 jobtypes]$ rm -rf ./pig*

```

## 配置全局插件配置
```shell
# 配置common.properties 
# 参考https://azkaban.readthedocs.io/en/latest/getStarted.html#property-overrides文档 
# common.properties文件是所有plugins的配置文件且比conf目录下的global.properties的优先级高
[admin@sdw2 jobtypes]$ vim common.properties
# 以下配置中部分property在配置文件中本身存在且没有被注释 需要先注释掉后 直接拷贝以下的配置
# add by tianyafu @20220209
hadoop.home=/opt/cloudera/parcels/CDH/lib/hadoop
hive.home=/opt/cloudera/parcels/CDH/lib/hive
spark.home=/application/spark
azkaban.should.proxy=false


# 配置commonprivate.properties
# 参考https://azkaban.readthedocs.io/en/latest/plugins.html#jobtype-plugins文档
# One can pass global settings that are needed by job types but should not be accessible by user code in commonprivate.properties
# 即这个配置文件中的配置也是作用于所有的jobtype的 但是用户自定义代码(脚本)中无法访问到这里的配置
[admin@sdw2 jobtypes]$ vim commonprivate.properties
# 以下配置中部分property在配置文件中本身存在且没有被注释 需要先注释掉后 直接拷贝以下的配置
# add by tianyafu @20220209
## hadoop的安全性版本，我使用的Hadoop为2.x所以改为2 虽然我们的hadoop没有使用kerberos等安全性验证
hadoop.security.manager.class=azkaban.security.HadoopSecurityManager_H_2_0
azkaban.native.lib=false
execute.as.user=false
azkaban.should.proxy=false
obtain.binary.token=false
hadoop.home=/opt/cloudera/parcels/CDH/lib/hadoop
hive.home=/opt/cloudera/parcels/CDH/lib/hive
spark.home=/application/spark
jobtype.global.classpath=/etc/hadoop/conf,${hadoop.home}/client/*
```

## 配置hive插件
```shell
# 参考https://azkaban.readthedocs.io/en/latest/jobTypes.html#hive-type文档
[admin@sdw2 jobtypes]$ vim hive/plugin.properties
# 以下配置中部分property在配置文件中本身存在且没有被注释 需要先注释掉后 直接拷贝以下的配置
# add by tianyafu @20220209
hive.aux.jars.path=${hive.home}/auxlib
hive.jvm.args=-Dhive.querylog.location=. -Dhive.exec.scratchdir=/tmp/hive-${user.to.proxy} -Dhive.aux.jars.path=${hive.aux.jars.path}
jobtype.jvm.args=${hive.jvm.args}

[admin@sdw2 jobtypes]$ vim hive/private.properties
# 以下配置中部分property在配置文件中本身存在且没有被注释 需要先注释掉后 直接拷贝以下的配置
# add by tianyafu @20220209
hive.aux.jar.path=${hive.home}/auxlib
jobtype.classpath=${hive.home}/lib/*,/etc/hive/conf,${hive.aux.jar.path}/*
jobtype.class=azkaban.jobtype.HadoopHiveJob

```

## 配置spark插件
```shell
# 因不配置spark插件 就需要配置hadoop.classpath 该参数在spark插件的private.properties中默认jobtype.classpath=${hadoop.classpath}:${spark.home}/conf:${spark.home}/lib/*
# 如何配置spark插件 官网没有文档 参考https://juejin.cn/post/6865542290228641805
[admin@sdw2 jobtypes]$ vim spark/private.properties
# 以下配置中部分property在配置文件中本身存在且没有被注释 需要先注释掉后 直接拷贝以下的配置
# add by tianyafu @20220209
HADOOP_CONF_DIR=/etc/hadoop/conf
YARN_CONF_DIR=/etc/hadoop/conf
jobtype.classpath=${spark.home}/conf,${spark.home}/jars/*
jobtype.class=azkaban.jobtype.HadoopSparkJob
```

## 配置系统级别的环境变量
```
# 三台都要配置
[admin@sdw2 jobtypes]$ vim ~/.bashrc
# HADOOP_CONF_DIR
export HADOOP_CONF_DIR=/etc/hadoop/conf
export YARN_CONF_DIR=/etc/hadoop/conf

[admin@sdw2 jobtypes]$ source ~/.bashrc

```

## 插件分发到其它2个节点上
```
# 先删除掉其它2个节点上的
[admin@mdw jobtypes]$ rm ~/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties 
[admin@sdw1 jobtypes]$ rm ~/app/azkaban-exec-server/plugins/jobtypes/commonprivate.properties 
# 分发
[admin@sdw2 jobtypes]$ scp -r ~/app/azkaban-exec-server/plugins/jobtypes/* mdw:~/app/azkaban-exec-server/plugins/jobtypes/
[admin@sdw2 jobtypes]$ scp -r ~/app/azkaban-exec-server/plugins/jobtypes/* sdw1:~/app/azkaban-exec-server/plugins/jobtypes/

```
## 重启Azkaban的Executor
```shell
[admin@sdw2 jobtypes]$ cd $AZKABAN_EXECUTOR_HOME
[admin@sdw2 azkaban-exec-server]$ ./bin/shutdown-exec.sh
[admin@sdw2 azkaban-exec-server]$ ./bin/start-exec.sh

# 激活
curl -G "mdw:12321/executor?action=activate" && echo
curl -G "sdw1:12321/executor?action=activate" && echo
curl -G "sdw2:12321/executor?action=activate" && echo

[admin@sdw2 azkaban-exec-server]$ curl -G "mdw:12321/executor?action=activate" && echo
{"status":"success"}
[admin@sdw2 azkaban-exec-server]$ curl -G "sdw1:12321/executor?action=activate" && echo
{"status":"success"}
[admin@sdw2 azkaban-exec-server]$ curl -G "sdw2:12321/executor?action=activate" && echo
{"status":"success"}
```

## 重启Azkaban的web
```shell
[admin@sdw2 azkaban-exec-server]$ cd $AZKABAN_WEB_HOME
[admin@sdw2 azkaban-web-server]$ ./bin/shutdown-web.sh
[admin@sdw2 azkaban-web-server]$ ./bin/start-web.sh
```

## 开发hive job 并测试
```
参考:https://azkaban.readthedocs.io/en/latest/jobTypes.html#hive-type的New Hive Jobtype章节
见:doc/Azkaban/hiveJob
```

## 开发spark job并测试
```
配置参考:https://github.com/azkaban/azkaban-plugins/issues/267 并参考源码HadoopSparkJob类和SparkJobArg类
见:doc/Azkaban/sparkJob
```

## 踩坑
```
1.跑hive job 一开始并没有配置spark插件 只是想先配置hive插件并先测试通 然后再配置spark插件 但spark插件的private.properties中默认jobtype.classpath=${hadoop.classpath}:${spark.home}/conf:${spark.home}/lib/*
需要hadoop.classpath变量，否则连executor都起不来 报以下错误
2022/02/09 15:43:10.784 +0800 ERROR [JobTypeManager] [Azkaban] pluginLoadProps to help with debugging: {jobtype.class: azkaban.jobtype.HadoopSparkJob, jobtype.classpath: ${hadoop.classpath}:${spark.home}/conf:${spark.home}/lib/*, plugin.dir: /home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/plugins/jobtypes/spark,  parent = {obtain.binary.token: false, spark.home: /application/spark, jobtype.global.classpath: ${hadoop.home}/etc/hadoop/*,${hadoop.home}/client/*, hive.home: /opt/cloudera/parcels/CDH/lib/hive, azkaban.native.lib: , execute.as.user: false, hadoop.home: /opt/cloudera/parcels/CDH/lib/hadoop, hadoop.security.manager.class: azkaban.security.HadoopSecurityManager_H_1_0, azkaban.should.proxy: false, }}
2022/02/09 15:43:10.785 +0800 ERROR [JobTypeManager] [Azkaban] Failed to load jobtype sparkFailed to get jobtype propertiesCould not find variable substitution for variable(s) [jobtype.classpath->hadoop.classpath]
azkaban.jobtype.JobTypeManagerException: Failed to get jobtype propertiesCould not find variable substitution for variable(s) [jobtype.classpath->hadoop.classpath]
	at azkaban.jobtype.JobTypeManager.loadJobTypes(JobTypeManager.java:190)
	at azkaban.jobtype.JobTypeManager.loadPluginJobTypes(JobTypeManager.java:145)
	at azkaban.jobtype.JobTypeManager.loadPlugins(JobTypeManager.java:63)
	at azkaban.jobtype.JobTypeManager.<init>(JobTypeManager.java:51)
	at azkaban.execapp.FlowRunnerManager.<init>(FlowRunnerManager.java:229)
	at azkaban.execapp.FlowRunnerManager$$FastClassByGuice$$f5329b23.newInstance(<generated>)
	at com.google.inject.internal.DefaultConstructionProxyFactory$FastClassProxy.newInstance(DefaultConstructionProxyFactory.java:89)
	at com.google.inject.internal.ConstructorInjector.provision(ConstructorInjector.java:111)
	at com.google.inject.internal.ConstructorInjector.construct(ConstructorInjector.java:90)
	at com.google.inject.internal.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:268)
	at com.google.inject.internal.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:46)
	at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1092)
	at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:40)
	at com.google.inject.internal.SingletonScope$1.get(SingletonScope.java:194)
	at com.google.inject.internal.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:41)
	at com.google.inject.internal.SingleParameterInjector.inject(SingleParameterInjector.java:38)
	at com.google.inject.internal.SingleParameterInjector.getAll(SingleParameterInjector.java:62)
	at com.google.inject.internal.ConstructorInjector.provision(ConstructorInjector.java:110)
	at com.google.inject.internal.ConstructorInjector.construct(ConstructorInjector.java:90)
	at com.google.inject.internal.ConstructorBindingImpl$Factory.get(ConstructorBindingImpl.java:268)
	at com.google.inject.internal.ProviderToInternalFactoryAdapter$1.call(ProviderToInternalFactoryAdapter.java:46)
	at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1092)
	at com.google.inject.internal.ProviderToInternalFactoryAdapter.get(ProviderToInternalFactoryAdapter.java:40)
	at com.google.inject.internal.SingletonScope$1.get(SingletonScope.java:194)
	at com.google.inject.internal.InternalFactoryToProviderAdapter.get(InternalFactoryToProviderAdapter.java:41)
	at com.google.inject.internal.InjectorImpl$2$1.call(InjectorImpl.java:1019)
	at com.google.inject.internal.InjectorImpl.callInContext(InjectorImpl.java:1085)
	at com.google.inject.internal.InjectorImpl$2.get(InjectorImpl.java:1015)
	at com.google.inject.internal.InjectorImpl.getInstance(InjectorImpl.java:1054)
	at azkaban.execapp.AzkabanExecutorServer.main(AzkabanExecutorServer.java:159)
Caused by: azkaban.utils.UndefinedPropertyException: Could not find variable substitution for variable(s) [jobtype.classpath->hadoop.classpath]
	at azkaban.utils.PropsUtils.resolveVariableReplacement(PropsUtils.java:304)
	at azkaban.utils.PropsUtils.resolveProps(PropsUtils.java:233)
	at azkaban.jobtype.JobTypeManager.loadJobTypes(JobTypeManager.java:186)
	... 29 more

解决方法:不需要配置hadoop.classpath 只需要配置spark插件的jobtype.classpath变量不引用hadoop.classpath即可 即如果没有使用spark插件 这里要将jobtype.classpath变量值改掉 否则报hadoop.classpath找不到
```
```
2.跑hive job 一开始不想将hadoop的相关jar包引入到Azkaban中 想通过将相关jar包统一引入到jobtype.global.classpath变量中 实际查看azkaban executor的日志发现如下日志:
2022/02/10 09:32:56.403 +0800 INFO [JobTypeManager] [Azkaban] Loading plugin hive
2022/02/10 09:32:56.405 +0800 INFO [JobTypeManager] [Azkaban] Adding global resources for hive
2022/02/10 09:32:56.405 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hadoop/etc/hadoop/*
2022/02/10 09:32:56.406 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hadoop/client/*
2022/02/10 09:32:56.406 +0800 INFO [JobTypeManager] [Azkaban] Adding type resources.
2022/02/10 09:32:56.406 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hadoop/etc/hadoop/
2022/02/10 09:32:56.406 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hive/lib/*
2022/02/10 09:32:56.407 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hive/conf/
2022/02/10 09:32:56.407 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/opt/cloudera/parcels/CDH/lib/hive/auxlib/*
2022/02/10 09:32:56.407 +0800 INFO [JobTypeManager] [Azkaban] Adding type override resources.
2022/02/10 09:32:56.408 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/plugins/jobtypes/hive/azkaban-hadoopsecuritymanager-3.0.0.jar
2022/02/10 09:32:56.408 +0800 INFO [JobTypeManager] [Azkaban] adding to classpath file:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/plugins/jobtypes/hive/azkaban-jobtype-3.0.0.jar
2022/02/10 09:32:56.408 +0800 INFO [JobTypeManager] [Azkaban] Classpath for plugin[dir: plugins/jobtypes/hive, JobType: hive]: [file:/opt/cloudera/parcels/CDH/lib/hadoop/etc/hadoop/*, file:/opt/cloudera/parcels/CDH/lib/hadoop/client/*, file:/opt/cloudera/parcels/CDH/lib/hadoop/etc/hadoop/, file:/opt/cloudera/parcels/CDH/lib/hive/lib/*, file:/opt/cloudera/parcels/CDH/lib/hive/conf/, file:/opt/cloudera/parcels/CDH/lib/hive/auxlib/*, file:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/plugins/jobtypes/hive/azkaban-hadoopsecuritymanager-3.0.0.jar, file:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/plugins/jobtypes/hive/azkaban-jobtype-3.0.0.jar]
2022/02/10 09:32:56.409 +0800 INFO [JobTypeManager] [Azkaban] Verifying job plugin hive
2022/02/10 09:32:56.409 +0800 INFO [JobTypeManager] [Azkaban] Loaded jobtype hive azkaban.jobtype.HadoopHiveJob

说明在使用hive的job时  /opt/cloudera/parcels/CDH/lib/hadoop/client/* 确实是加入到classpath下的 但执行hive任务时 瞬间报错 得到以下日志:
10-02-2022 11:44:49 CST hive_job_test ERROR - Job run failed!
java.lang.NoClassDefFoundError: org/apache/hadoop/conf/Configuration
	at azkaban.jobtype.AbstractHadoopJavaProcessJob.setupHadoopJobProperties(AbstractHadoopJavaProcessJob.java:62)
	at azkaban.jobtype.HadoopHiveJob.run(HadoopHiveJob.java:49)
	at azkaban.execapp.JobRunner.runJob(JobRunner.java:813)
	at azkaban.execapp.JobRunner.doRun(JobRunner.java:602)
	at azkaban.execapp.JobRunner.run(JobRunner.java:563)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: java.lang.ClassNotFoundException: org.apache.hadoop.conf.Configuration
	at java.net.URLClassLoader.findClass(URLClassLoader.java:381)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:424)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:349)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:357)
	... 10 more

明显是包找不到或者jar包冲突引起的
解决方法: 将/opt/cloudera/parcels/CDH/lib/hadoop/client/*.jar通过软连接的方式链接到extlib目录下 重启Azkaban

这样 通过ps -ef | grep azkaban就可以看到executor进程的classpath下有hadoop相关的jar包 且hive任务也能成功跑通了

```
```
3.跑hive job 任务能跑 但报错 Table not found 'site' 日志如下:
10-02-2022 13:35:42 CST hive_job_test INFO - Command: java '-Dazkaban.flowid=hive_job_test' '-Dazkaban.execid=23' '-Dazkaban.jobid=hive_job_test' -Dhive.querylog.location=. -Dhive.exec.scratchdir=/tmp/hive-admin -Dhive.aux.jars.path=/opt/cloudera/parcels/CDH/lib/hive/auxlib -Xms64M -Xmx256M -cp /home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/lib/az-core-0.1.0-SNAPSHOT.jar:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/lib/azkaban-common-0.1.0-SNAPSHOT.jar:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/lib/az-hadoop-jobtype-plugin-0.1.0-SNAPSHOT.jar:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/lib/azkaban-hadoop-security-plugin-0.1.0-SNAPSHOT.jar:/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/executions/23/_resources_hive_job_test:/opt/cloudera/parcels/CDH/lib/hive/lib/*:/etc/hive/conf/*:/opt/cloudera/parcels/CDH/lib/hive/auxlib/*:/etc/hadoop/conf/*:/opt/cloudera/parcels/CDH/lib/hadoop/client/* azkaban.jobtype.HadoopSecureHiveWrapper -f hive.hql 
10-02-2022 13:35:42 CST hive_job_test INFO - Environment variables: {JOB_OUTPUT_PROP_FILE=/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/executions/23/hive_job_test_output_240446335006512807_tmp, JOB_PROP_FILE=/home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/executions/23/hive_job_test_props_899455825094874302_tmp, KRB5CCNAME=/tmp/krb5cc__hive_job_test__hive_job_test__hive_job_test__23__admin, JOB_NAME=hive_job_test}
10-02-2022 13:35:42 CST hive_job_test INFO - Working directory: /home/admin/app/azkaban-exec-server-0.1.0-SNAPSHOT/executions/23
10-02-2022 13:35:42 CST hive_job_test INFO - Spawned process with id 27393
10-02-2022 13:35:43 CST hive_job_test INFO - log4j:WARN No appenders could be found for logger (org.apache.hadoop.util.Shell).
10-02-2022 13:35:43 CST hive_job_test INFO - log4j:WARN Please initialize the log4j system properly.
10-02-2022 13:35:43 CST hive_job_test INFO - log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
10-02-2022 13:35:50 CST hive_job_test INFO - FAILED: SemanticException Line 0:-1 Table not found 'site'
10-02-2022 13:35:50 CST hive_job_test INFO - Exception in thread "main" HiveQueryExecutionException{query='hive.hql', returnCode=40000}
10-02-2022 13:35:50 CST hive_job_test INFO - 	at azkaban.jobtype.HadoopSecureHiveWrapper.runHive(HadoopSecureHiveWrapper.java:162)
10-02-2022 13:35:50 CST hive_job_test INFO - 	at azkaban.jobtype.HadoopSecureHiveWrapper.main(HadoopSecureHiveWrapper.java:76)
10-02-2022 13:35:50 CST hive_job_test INFO - Process with id 27393 completed unsuccessfully in 7 seconds.
10-02-2022 13:35:50 CST hive_job_test ERROR - caught error running the job
java.lang.RuntimeException: azkaban.jobExecutor.utils.process.ProcessFailureException: Process exited with code 1
	at azkaban.jobExecutor.ProcessJob.run(ProcessJob.java:305)
	at azkaban.jobtype.AbstractHadoopJavaProcessJob.run(AbstractHadoopJavaProcessJob.java:43)
	at azkaban.jobtype.HadoopHiveJob.run(HadoopHiveJob.java:52)
	at azkaban.execapp.JobRunner.runJob(JobRunner.java:813)
	at azkaban.execapp.JobRunner.doRun(JobRunner.java:602)
	at azkaban.execapp.JobRunner.run(JobRunner.java:563)
	at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
	at java.util.concurrent.FutureTask.run(FutureTask.java:266)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)
Caused by: azkaban.jobExecutor.utils.process.ProcessFailureException: Process exited with code 1
	at azkaban.jobExecutor.utils.process.AzkabanProcess.run(AzkabanProcess.java:125)
	at azkaban.jobExecutor.ProcessJob.run(ProcessJob.java:297)
	... 10 more

实际hive的default库下 是有site这张表的 所以考虑任务在执行时 没有正确连接到hive的metastore 即hive-site.xml的hive.metastore.uris参数
后来发现hive/private.properties中  我配置的是/etc/hive/conf/*  后来改为/etc/hive/conf就解决了 所以是conf目录配置不正确导致的

由该问题引申到 commonprivate.properties 中配置的${hadoop.home}/client/*  
不要配置成${hadoop.home}/client/*.jar(验证过该配置也会导致包找不到 不清楚是不是与环境有关) 
也不要配置成${hadoop.home}/client(也是验证过会报包找不到 不清楚是不是与环境有关)

```

```
4.跑spark job 任务能跑 但报错 日志如下:
Exception in thread "main" org.apache.spark.SparkException: When running with master 'yarn' either HADOOP_CONF_DIR or YARN_CONF_DIR must be set in the environment.
	at org.apache.spark.deploy.SparkSubmitArguments.error(SparkSubmitArguments.scala:657)
	at org.apache.spark.deploy.SparkSubmitArguments.validateSubmitArguments(SparkSubmitArguments.scala:291)
	at org.apache.spark.deploy.SparkSubmitArguments.validateArguments(SparkSubmitArguments.scala:251)
	at org.apache.spark.deploy.SparkSubmitArguments.<init>(SparkSubmitArguments.scala:120)
	at org.apache.spark.deploy.SparkSubmit$$anon$2$$anon$3.<init>(SparkSubmit.scala:907)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.parseArguments(SparkSubmit.scala:907)
	at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:81)
	at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:920)
	at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:929)
	at azkaban.jobtype.HadoopSecureSparkWrapper.runSpark(HadoopSecureSparkWrapper.java:140)
	at azkaban.jobtype.HadoopSecureSparkWrapper.main(HadoopSecureSparkWrapper.java:100)
 
 
 
找不到HADOOP_CONF_DIR或者YARN_CONF_DIR 该参数在${SPARK_HOME}/conf/spark-env.sh中配置了HADOOP_CONF_DIR 且根据博客https://juejin.cn/post/6865542290228641805在
private.properties中也配置了 但还是报找不到
原因可能是1.当前环境为CDH集群 并没有像社区版本部署时那样去环境变量中显示的配置 2.直接使用spark-submit提交到yarn时 能够找到spark-env.sh中配置的HADOOP_CONF_DIR 而使用Azkaban spark插件运行spark的任务时 找不到该变量
解决方式: 在~/.bashrc中显示配置了HADOOP_CONF_DIR 和 YARN_CONF_DIR两个变量并重启Azkaban集群
export HADOOP_CONF_DIR=/etc/hadoop/conf
export YARN_CONF_DIR=/etc/hadoop/conf

```

```
5.跑spark job 任务能跑 但报错 日志如下: 
Caused by: java.lang.IllegalArgumentException: System memory 239075328 must be at least 471859200. Please increase heap size using the --driver-memory option or spark.driver.memory in Spark configuration.
	at org.apache.spark.memory.UnifiedMemoryManager$.getMaxMemory(UnifiedMemoryManager.scala:219)
	at org.apache.spark.memory.UnifiedMemoryManager$.apply(UnifiedMemoryManager.scala:199)
	at org.apache.spark.SparkEnv$.create(SparkEnv.scala:330)
	at org.apache.spark.SparkEnv$.createDriverEnv(SparkEnv.scala:185)
	at org.apache.spark.SparkContext.createSparkEnv(SparkContext.scala:257)
	at org.apache.spark.SparkContext.<init>(SparkContext.scala:424)
	at org.apache.spark.SparkContext$.getOrCreate(SparkContext.scala:2520)
	at org.apache.spark.sql.SparkSession$Builder.$anonfun$getOrCreate$1(SparkSession.scala:930)

内存当前只有228M 最少要450M 提示通过配置--driver-memory或spark.driver.memory进行调参
通过翻阅Spark UnifiedMemoryManager类的源码 去conf中获取spark.testing.memory参数 没配置就通过Runtime.getRuntime.maxMemory获取内存 所以通过配置job的spark.testing.memory参数 改变内存
```