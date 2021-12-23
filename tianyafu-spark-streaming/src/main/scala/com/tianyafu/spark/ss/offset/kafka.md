# 学习SparkStreaming时使用到的Kafka的相关操作

## 创建topic
```shell
kafka-topics --create \
--topic tyfss \
--zookeeper mdw:2181,sdw1:2181,sdw2:2181/kafka \
--partitions 3 \
--replication-factor 3
```

## 查看topics
```shell
kafka-topics --list \
--zookeeper mdw:2181,sdw1:2181,sdw2:2181/kafka
```


## 描述topic
```shell
kafka-topics --describe \
--topic tyfss \
--zookeeper mdw:2181,sdw1:2181,sdw2:2181/kafka
```

## 创建producer console
```shell
kafka-console-producer \
--broker-list mdw:9092,sdw1:9092,sdw2:9092 \
--topic tyfss
```

## 创建consumer console
```shell
kafka-console-consumer \
--bootstrap-server mdw:9092,sdw1:9092,sdw2:9092 \
--from-beginning \
--topic tyfss
```