# Kafka常用操作

## 创建topic
```shell
kafka-topics --create \
--topic tianyafu \
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
--topic tianyafu \
--zookeeper mdw:2181,sdw1:2181,sdw2:2181/kafka
```

## 创建producer console
```shell
kafka-console-producer \
--broker-list mdw:9092,sdw1:9092,sdw2:9092 \
--topic tianyafu
```

## 创建consumer console
```shell
kafka-console-consumer \
--bootstrap-server mdw:9092,sdw1:9092,sdw2:9092 \
--from-beginning \
--topic tianyafu
```

## 查看Kafka的数据和index 将二进制的转成可读的
```shell
kafka-run-class kafka.tools.DumpLogSegments \
--files /var/local/kafka/data/tianyafu-0/00000000000000000000.log \
--print-data-log  > ~/tmp/1.txt
```