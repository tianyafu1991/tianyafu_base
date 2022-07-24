#!/usr/bin/env bash

spark-shell \
--master yarn \
--name tianya_spark_shell \
--num-executors 2 \
--driver-memory 1g \
--executor-memory 3g \
--executor-cores 1 \
--conf "spark.sql.shuffle.partitions=10" \
--conf "spark.serializer=org.apache.spark.serializer.KryoSerializer"