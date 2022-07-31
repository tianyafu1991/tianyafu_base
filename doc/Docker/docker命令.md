# Docker命令

## 查看images
```shell
[root@hadoop01 ~]# docker images

```

## 只展示image id
```shell
[root@hadoop01 ~]# docker images centos -q
```

## 拉取镜像
```shell
[root@hadoop01 ~]# docker pull centos:7
```

## 查看命令
```shell
[root@hadoop01 ~]# docker image
```

## 查找镜像 很少用  直接hub上搜
```shell
[root@hadoop01 ~]# docker search centos
```

## 查看image的信息
```shell
[root@hadoop01 ~]# docker image inspect feb5d9fea6a5
```

## 查看image container 等的使用情况
```shell
[root@hadoop01 ~]# docker system df
```

## 删除镜像
```shell
[root@hadoop01 ~]# docker image rm feb5d9fea6a5
# 简写
[root@hadoop01 ~]# docker rmi feb5d9fea6a5
# 强删 如果该image有container在运行 需要使用强制删才能删除
[root@hadoop01 ~]# docker rmi -f feb5d9fea6a5
```

## 容器相关命令
```shell
# 基础命令
[root@hadoop01 ~]# docker container
# 启动容器
[root@hadoop01 ~]# docker run --name tyf_tomcat_8 tomcat:8
# 查看活着的容器
[root@hadoop01 ~]# docker ps
# 查看所有的容器
[root@hadoop01 ~]# docker ps -a
# 查看最新的容器
[root@hadoop01 ~]# docker ps -l
# 查看2条
[root@hadoop01 ~]# docker ps -n 2
# 获取所有的容器的id
[root@hadoop01 ~]# docker ps -aq
# 查看已退出的容器
[root@hadoop01 ~]# docker ps -f status=exited
# 根据容器名停容器
[root@hadoop01 ~]# docker stop tyf_tomcat_8
# 重启容器
[root@hadoop01 ~]# docker restart tyf_tomcat_8
# 停止容器
[root@hadoop01 ~]# docker stop tyf_tomcat_8
# 删除所有容器
[root@hadoop01 ~]# docker rm -f $(docker ps -aq) 
```

## docker run相关命令
```shell
[root@hadoop01 ~]# docker run --help
常用命令:
docker run
-i
-p
-t
-m 
-d
-v

# 后台启动redis
[root@hadoop01 ~]# docker run -d --name tyf_redis redis
# 查看容器的日志
[root@hadoop01 ~]# docker logs tyf_redis
# 交互式命令行进入redis
[root@hadoop01 ~]# docker exec -it tyf_redis sh
# redis-cli
127.0.0.1:6379> keys *
(empty array)
127.0.0.1:6379> set name tianyafu
OK
127.0.0.1:6379> get name
"tianyafu"
# 使用交互式命令行伪终端的方式启动ubuntu容器 并进入bash模式
[root@hadoop01 ~]# docker run -it --name tyf_ubuntu ubuntu /bin/bash
# 查看容器的详细信息
[root@hadoop01 ~]# docker inspect tyf_ubuntu
# 端口映射 -p外部映射后的端口:容器内部的端口
[root@hadoop01 ~]# docker run -d -p8890:80 --name tyf_nginx nginx
```