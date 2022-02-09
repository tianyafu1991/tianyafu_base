# CDH集群上部署Spark2

## 准备SPARK2的parcel等3个安装包及1个描述符文件
```shell
[root@hadoop001 ~]# mkdir ~/spark2_parcel
上传4个文件
SPARK2-2.4.0.cloudera2-1.cdh5.13.3.p0.1041012-el7.parcel
SPARK2-2.4.0.cloudera2-1.cdh5.13.3.p0.1041012-el7.parcel.sha1
manifest.json
SPARK2_ON_YARN-2.4.0.cloudera2.jar

# 修改sha1文件的后缀
[root@hadoop001 ~]# cd ~/spark2_parcel
[root@hadoop001 spark2_parcel]# mv SPARK2-2.4.0.cloudera2-1.cdh5.13.3.p0.1041012-el7.parcel.sha1 SPARK2-2.4.0.cloudera2-1.cdh5.13.3.p0.1041012-el7.parcel.sha
[root@hadoop001 spark2_parcel]# cd 
```

## 拷贝描述符文件到CDH的描述符目录下
```shell
# CDH默认的描述符目录为/opt/cloudera/csd
[root@hadoop001 ~]# mkdir /opt/cloudera/csd
[root@hadoop001 ~]# mv ~/spark2_parcel/SPARK2_ON_YARN-2.4.0.cloudera2.jar /opt/cloudera/csd
[root@hadoop001 ~]# chown cloudera-scm:cloudera-scm /opt/cloudera/csd/SPARK2_ON_YARN-2.4.0.cloudera2.jar 
[root@hadoop001 ~]# chmod 644 /opt/cloudera/csd/SPARK2_ON_YARN-2.4.0.cloudera2.jar
# 重启 cm server
[root@hadoop001 ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-server restart


```

## 页面配置SPARK2的parcel的离线源
```shell
# 部署SPARK2的离线源 移动~/spark2_parcel到/var/www/html/
[root@hadoop001 ~]# mv ~/spark2_parcel /var/www/html/

1.页面点击Hosts->Parcels
2.点击页面右上角Configuration
3.找到对应的SPARK2的parcel repository url 更改为:http://hadoop001/spark2_parcel/ 保存
4.点击Download进行安装 分发 最后激活
5.查看下面的路径中是否有软连接KAFKA
```

## 页面添加SPARK2的service


## 运行SparkPI
```shell
[root@hadoop002 ~]# su - hdfs
spark2-submit \
--master yarn \
--num-executors 1 \
--executor-cores 1 \
--executor-memory 1G \
--class org.apache.spark.examples.SparkPi \
/opt/cloudera/parcels/SPARK2/lib/spark2/examples/jars/spark-examples_2.11-2.4.0.cloudera2.jar
```