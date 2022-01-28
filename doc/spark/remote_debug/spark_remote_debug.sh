#!/usr/bin/env bash

spark-submit \
--name spark_remote_debug \
--class com.tianyafu.spark.remote.RemoteWCApp \
--num-executors 2 \
--driver-memory 1g \
--executor-memory 1g \
--executor-cores 1 \
--master yarn \
--deploy-mode client \
--driver-java-options "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=6666" \
--verbose \
--conf "spark.data.input.path=hdfs:///tmp/tianyafu/tianyafu.txt" \
--conf "spark.yarn.historyServer.allowTracking=true" \
/home/admin/lib/tianyafu-spark-remote-1.0.jar