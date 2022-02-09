# CDH集群上部署Kafka


## 准备KAFKA的parcel等3个安装包
```shell
[root@hadoop001 ~]# mkdir ~/kafka_parcel
上传以下3个文件到~/kafka_parcel下
KAFKA-3.1.0-1.3.1.0.p0.35-el7.parcel
KAFKA-3.1.0-1.3.1.0.p0.35-el7.parcel.sha1
manifest.json

CDK对应的版本:https://docs.cloudera.com/documentation/kafka/4-1-x/topics/kafka_packaging.html#concept_fzg_phl_br
本次部署的Kafka对应Apache kafka的1.0.1版本  在CDH的内部版本为3.1.0 patch_level为35
```

## 安装httpd服务
```shell
[root@hadoop001 ~]# yum install -y httpd
[root@hadoop001 ~]# service httpd start
[root@hadoop001 ~]# service httpd status

[root@hadoop001 ~]# ll /var/www/html/
[root@hadoop001 ~]# cd ~/kafka_parcel
[root@hadoop001 kafka_parcel]# mv KAFKA-3.1.0-1.3.1.0.p0.35-el7.parcel.sha1 KAFKA-3.1.0-1.3.1.0.p0.35-el7.parcel.sha
[root@hadoop001 kafka_parcel]# cd ../
[root@hadoop001 ~]# mv  ~/kafka_parcel /var/www/html/

```

## 页面配置KAFKA的parcel的离线源
```shell
1.页面点击Hosts->Parcels
2.点击页面右上角Configuration
3.找到对应的Kafka的parcel repository url 更改为:http://hadoop001/kafka_parcel/ 保存
4.点击Download进行安装 分发 最后激活
5.查看下面的路径中是否有软连接KAFKA
[root@hadoop001 ~]# ll /opt/cloudera/parcels/

```

## 页面添加Kafka的service
```shell
在页面配置中 需要配置zookeeper.chroot这个参数为/kafka  这个是kafka在zk的元数据目录 统一放在/kafka下 而不是放在/目录下 后续如果有问题 方便定位

可能部署完之后kafka启动会失败 这个是因为默认的kafka的java heap只有50M 改为1G即可启动
Java Heap Size of Broker
broker_max_heap_size
```

## 创建topic
```shell
/opt/cloudera/parcels/KAFKA/lib/kafka/bin/kafka-topics.sh \
--create \
--zookeeper hadoop001:2181,hadoop002:2181,hadoop003:2181/kafka \
--replication-factor 3 \
--partitions 3 \
--topic tianyafu_topic
```

## 启动生产者和消费者
```shell
/opt/cloudera/parcels/KAFKA/lib/kafka/bin/kafka-console-producer.sh \
--broker-list hadoop001:9092,hadoop002:9092,hadoop003:9092 \
--topic tianyafu_topic

/opt/cloudera/parcels/KAFKA/lib/kafka/bin/kafka-console-consumer.sh \
--bootstrap-server hadoop001:9092,hadoop002:9092,hadoop003:9092 \
--from-beginning \
--topic tianyafu_topic
```