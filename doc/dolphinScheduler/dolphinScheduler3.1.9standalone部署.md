# DolphinScheduler部署

## 文档
```
参考:
https://dolphinscheduler.apache.org/en-us/docs/3.1.9/guide/installation/standalone
https://github.com/apache/dolphinscheduler/blob/3.1.9-release/docs/docs/en/guide/howto/datasource-setting.md
```

## 环境准备
```
1.MySQL5.7
2.对应的驱动包
3.JDK1.8并配置JAVA_HOME
```

## 下载并解压
```shell
[admin@sdw2 ~]$ cd ~/software/
[admin@sdw2 software]$ wget https://dlcdn.apache.org/dolphinscheduler/3.1.9/apache-dolphinscheduler-3.1.9-src.tar.gz
[admin@sdw2 software]$ tar -zxvf ~/software/apache-dolphinscheduler-3.1.9-bin.tar.gz -C ~/app/
[admin@sdw2 software]$ cd ~/app/
[admin@sdw2 app]$ ln -s apache-dolphinscheduler-3.1.9-bin dolphinscheduler-bin
```

## 拷贝MySQL驱动包
```
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/standalone-server/libs/standalone-server/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/tools/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/worker-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/master-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/alert-server/libs/
[admin@sdw2 app]$ cp ~/lib/mysql-connector-java-8.0.17.jar ~/app/dolphinscheduler-bin/api-server/libs/
```

### 创建数据库
```sql
mysql -uroot -p

CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'dolphinscheduler'@'%' IDENTIFIED BY 'dolphinscheduler123';
GRANT ALL PRIVILEGES ON dolphinscheduler.* TO 'dolphinscheduler'@'localhost' IDENTIFIED BY 'dolphinscheduler123';
flush privileges;
```

## 修改配置文件
```shell
[admin@sdw2 app]$ vim ~/app/dolphinscheduler-bin/bin/env/dolphinscheduler_env.sh
# for mysql
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://sdw2:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME=dolphinscheduler
export SPRING_DATASOURCE_PASSWORD=dolphinscheduler123
```

## 初始化数据库
```shell 
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/tools/bin/upgrade-schema.sh
```

## 修改standalone中的application.yaml
```shell 
# 把其中H2的配置(spring.datasource) 改成MySQL的连接信息 否则standalone启动报错
[admin@sdw2 app]$ vim ~/app/dolphinscheduler-bin/standalone-server/conf/application.yaml
spring:
  jackson:
    time-zone: UTC
    date-format: "yyyy-MM-dd HH:mm:ss"
  banner:
    charset: UTF-8
  cache:
    # default enable cache, you can disable by `type: none`
    type: none
    cache-names:
      - tenant
      - user
      - processDefinition
      - processTaskRelation
      - taskDefinition
    caffeine:
      spec: maximumSize=100,expireAfterWrite=300s,recordStats
  sql:
    init:
      schema-locations: classpath:sql/dolphinscheduler_h2.sql
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://sdw2:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false
    username: dolphinscheduler
    password: dolphinscheduler123
```

## 启停standalone
```shell 
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/bin/dolphinscheduler-daemon.sh start standalone-server
Begin start standalone-server......
starting standalone-server, logging to /home/admin/app/dolphinscheduler-bin/standalone-server/logs
Overwrite standalone-server/conf/dolphinscheduler_env.sh using bin/env/dolphinscheduler_env.sh.
End start standalone-server.
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/bin/dolphinscheduler-daemon.sh status standalone-server
Begin status standalone-server......
standalone-server  [  RUNNING  ]
End status standalone-server.
[admin@sdw2 app]$ bash ~/app/dolphinscheduler-bin/bin/dolphinscheduler-daemon.sh stop standalone-server
Begin stop standalone-server......
stopping standalone-server
End stop standalone-server.
```