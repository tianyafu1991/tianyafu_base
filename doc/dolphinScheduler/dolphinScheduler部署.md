# DolphinScheduler部署

## 下载
```shell

[admin@sdw2 ~]$ cd software/
[admin@sdw2 software]$ wget https://downloads.apache.org/dolphinscheduler/2.0.0/apache-dolphinscheduler-2.0.0-bin.tar.gz

```

## 文档
```
部署时2.0.0版本刚发布 集群部署文档不完善 所以参考1.3.9版本的文档
参考:https://dolphinscheduler.apache.org/zh-cn/docs/1.3.9/user_doc/cluster-deployment.html
```

## 环境准备
```aidl
1.MySQL5.7及对应的驱动包
2.JDK1.8并配置JAVA_HOME
3.ZK3.4.6+
4.CentOS必装psmisc 通过 yum install –y psmisc 安装
5.Hadoop2.6+
```