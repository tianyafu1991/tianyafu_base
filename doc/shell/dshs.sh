#!/bin/bash -x


#ENV Parameters
SPARK_HOME=/opt/software/spark2.1.1

#Spark Job Parameters
JOB_NAME=DSHS-0.4-${env}

CLASS_NAME=com.ruozedata.DSHS
JAR_PATH="$SPARK_HOME/spark_on_yarn/DSHS-1.0-SNAPSHOT.jar"
DRIVER_MEMORY=2g
EXECUTOR_MEMORY=2g
EXECUTOR_CORES=1
NUM_EXECUTORS=3

ARCHIVE=hdfs://nameservice1/spark/spark-archive20171220-bms.zip

?????



#Submit Job
$SPARK_HOME/bin/spark-submit \
--name $JOB_NAME \
--class $CLASS_NAME     \
--master yarn     \
--deploy-mode cluster     \
--driver-memory $DRIVER_MEMORY     \
--executor-memory $EXECUTOR_MEMORY     \
--executor-cores   $EXECUTOR_CORES   \
--num-executors $NUM_EXECUTORS \

--conf "spark.files=SPARK_HOME/resources/core-site.xml,$SPARK_HOME/resources/hdfs-site.xml,$SPARK_HOME/resources/yarn-site.xml" \
--conf "spark.yarn.archive=$ARCHIVE" \

--conf "spark.ui.showConsoleProgress=false" \
--conf "spark.yarn.am.memory=1024m" \
--conf "spark.yarn.am.memoryOverhead=1024m" \
--conf "spark.yarn.driver.memoryOverhead=1024m" \
--conf "spark.yarn.executor.memoryOverhead=1024m" \
--conf "spark.yarn.am.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35  -XX:G1ReservePercent=15 -XX:+DisableExplicitGC -Dcdh.version=5.12.0 -Duser.timezone=Asia/Shanghai" \
--conf "spark.driver.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35   -XX:G1ReservePercent=15 -XX:+DisableExplicitGC -Dcdh.version=5.12.0 -Duser.timezone=Asia/Shanghai" \
--conf "spark.executor.extraJavaOptions=-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:InitiatingHeapOccupancyPercent=35 -XX:G1ReservePercent=15 -XX:+DisableExplicitGC -Dcdh.version=5.12.0 -Duser.timezone=Asia/Shanghai" \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer" \
$JAR_PATH

exit
