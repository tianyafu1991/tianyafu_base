# Dockerfile编写

## 官方文档
```
https://docs.docker.com/engine/reference/builder/
```

```shell
[root@sdw2 tianyafu]# pwd
/tmp/tianyafu

[root@sdw2 tianyafu]# vim index.html
<h1>好好学习 天天向上</h1>

[root@sdw2 tianyafu]# vim Dockerfile
FROM nginx:stable-alpine
LABEL author="tianyafu"
LABEL desc="这个是我的练习用的Dockerfile 功能是自定义添加index.html到nginx中"
ADD index.html /usr/share/nginx/html/index.html

[root@sdw2 tianyafu]# docker image build -t guaren2009/tyf_nginx .

[root@sdw2 tianyafu]# docker run -p 8765:80 -d --name tianyafu_new_nginx guaren2009/tyf_nginx:latest
[root@sdw2 tianyafu]# docker ps -l
CONTAINER ID   IMAGE                         COMMAND                  CREATED          STATUS          PORTS                                   NAMES
1e8cebe24bcd   guaren2009/tyf_nginx:latest   "/docker-entrypoint.…"   25 seconds ago   Up 24 seconds   0.0.0.0:8765->80/tcp, :::8765->80/tcp   tianyafu_new_nginx


[root@sdw2 tianyafu]# vim Dockerfile2
FROM centos:7
RUN yum install -y tree vim

[root@sdw2 tianyafu]# docker build -f Dockerfile2 -t guaren2009/tyf_centos:1.0 .
[root@sdw2 tianyafu]# docker images
REPOSITORY              TAG             IMAGE ID       CREATED          SIZE
guaren2009/tyf_centos   1.0             5cb0e89c354a   14 seconds ago   436MB
guaren2009/tyf_nginx    latest          380809be4531   54 minutes ago   23.5MB

```

# 使用Dockerfile编成一个centos中带有vim和安装好jdk的镜像
```shell
[root@sdw2 ~]# cd /tmp/tianyafu/
[root@sdw2 tianyafu]# mkdir centos-jdk-vim
[root@sdw2 tianyafu]# cd centos-jdk-vim/
[root@sdw2 centos-jdk-vim]# ll
total 181296
-rw-r--r-- 1 root root 185646832 Jul 31 17:18 jdk-8u181-linux-x64.tar.gz
[root@sdw2 centos-jdk-vim]# vim Dockerfile-centos-jdk-vim
FROM centos:7
RUN yum install -y vim

ENV SYSTEM_ENV=/usr/local

WORKDIR $SYSTEM_ENV
ADD jdk-8u181-linux-x64.tar.gz $SYSTEM_ENV/java/

ENV JAVA_HOME=$SYSTEM_ENV/java/jdk1.8.0_181
ENV PATH=$JAVA_HOME/bin:$PATH


[root@sdw2 centos-jdk-vim]# docker build -f Dockerfile-centos-jdk-vim -t guaren2009/tyf_centos:1.1 .

[root@sdw2 centos-jdk-vim]# docker run -it --name tyf_centos_1.1 guaren2009/tyf_centos:1.1
[root@1d391694a9e0 local]# pwd
/usr/local
[root@1d391694a9e0 local]# cd
[root@1d391694a9e0 ~]# java -version
java version "1.8.0_181"
Java(TM) SE Runtime Environment (build 1.8.0_181-b13)
Java HotSpot(TM) 64-Bit Server VM (build 25.181-b13, mixed mode)
[root@1d391694a9e0 ~]# vim a.txt 
[root@1d391694a9e0 ~]# cat a.txt 
aaa
[root@1d391694a9e0 ~]# 

```