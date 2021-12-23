#!/usr/bin/env bash

# 生效对应的环境变量
export TYF_SPARK_JOB_JAR_PATH=/home/admin/tmp/tianyafu/lib/tianyafu-spark-streaming-1.0.jar
export JOB_MYSQL_LIB_PATH=/home/admin/tmp/tianyafu/lib/mysql-connector-java-5.1.47.jar
export JOB_SPARK_STREAMING_KAFKA_LIB_PATH=/home/admin/tmp/tianyafu/lib/spark-streaming-kafka-0-10_2.12-2.4.6.jar
export JOB_KAFKA_LIB_PATH=/home/admin/tmp/tianyafu/lib/kafka-clients-2.0.0.jar
export JOB_C3P0_LIB_PATH=/home/admin/tmp/tianyafu/lib/c3p0-0.9.1.1.jar
export JOB_DRUID_LIB_PATH=/home/admin/tmp/tianyafu/lib/druid-1.1.20.jar


spark-submit \
--name offsetApp04ExactlyOnceStoreOffsetWithOwnDataClusterApp \
--class com.tianyafu.spark.ss.offset.OffsetApp04ExactlyOnceStoreOffsetWithOwnDataClusterApp \
--num-executors 2 \
--driver-memory 1g \
--executor-memory 2g \
--executor-cores 3 \
--master yarn \
--deploy-mode client \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer" \
--jars ${JOB_MYSQL_LIB_PATH},${JOB_SPARK_STREAMING_KAFKA_LIB_PATH},${JOB_KAFKA_LIB_PATH},${JOB_C3P0_LIB_PATH},${JOB_DRUID_LIB_PATH} \
${TYF_SPARK_JOB_JAR_PATH} \
5 mdw:9092,sdw1:9092,sdw2:9092 tyf_ss_store_offset_in_mysql_group tyfss,tyf_kafka_1
