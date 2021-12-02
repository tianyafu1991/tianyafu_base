# DolphinScheduler部署

## 文档
```
部署时2.0.0版本刚发布 集群部署文档不完善 所以参考1.3.9版本的文档
参考:https://dolphinscheduler.apache.org/zh-cn/docs/1.3.9/user_doc/cluster-deployment.html
```

## 环境准备
```
1.MySQL5.7及对应的驱动包
2.JDK1.8并配置JAVA_HOME
3.ZK3.4.6+
4.CentOS必装psmisc 通过 yum install –y psmisc 安装
5.Hadoop2.6+
```

## 下载并解压
```shell
[admin@sdw2 ~]$ mkdir app bin data dw lib log script shell software sourcecode tmp
[admin@sdw2 ~]$ cd software/
[admin@sdw2 software]$ wget https://downloads.apache.org/dolphinscheduler/2.0.0/apache-dolphinscheduler-2.0.0-bin.tar.gz
[admin@sdw2 software]$ tar -zxvf apache-dolphinscheduler-2.0.0-bin.tar.gz
[admin@sdw2 software]$ scp -r apache-dolphinscheduler-2.0.0-bin mdw:/home/admin/software/
[admin@sdw2 software]$ ln -s apache-dolphinscheduler-2.0.0-bin dolphinscheduler-bin
```

## 创建部署用户和hosts映射
```shell
# 创建用户需使用root登录，设置部署用户名，请自行修改，后面以 admin 为例 admin用户由运维创建了
# useradd admin

# 设置用户密码，请自行修改，后面以 dolphinscheduler123 为例 admin的密码也是运维设置了
# echo "dolphinscheduler123" | passwd --stdin dolphinscheduler

# 配置 sudo 免密    本次部署在/etc/sudoers中并没有Defaults    requiretty 这一行  官网中requirett少了个y
echo 'dolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers
```

## 配置hosts映射和ssh打通及修改目录权限
```shell
配置host和信任关系

```

## 数据库初始化
```shell
上传jar包并重命名
[admin@sdw2 lib]$ mv mysql-connector-java-5.1.47.jar mysql-connector-java.jar

CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
CREATE USER 'admin'@'%' IDENTIFIED BY 'dd@2016';
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'admin'@'%' IDENTIFIED BY 'dd@2016';
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'admin'@'localhost' IDENTIFIED BY 'dd@2016';
flush privileges;

[admin@sdw2 lib]$ cd 
[admin@sdw2 ~]$ vim ~/software/dolphinscheduler-bin/conf/datasource.properties  
```