#!/usr/bin/env bash

spark-sql \
--master yarn \
--name tianya_spark_sql \
--conf "spark.sql.shuffle.partitions=10" \
--conf "spark.hadoop.hive.exec.dynamic.partition=true" \
--conf "spark.hadoop.hive.exec.dynamic.partition.mode=nonstrict"