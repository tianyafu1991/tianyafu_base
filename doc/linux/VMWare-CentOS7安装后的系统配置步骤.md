# VMWare-CentOS7安装后的系统配置步骤

## 关闭防火墙和selinux
```
# 查看防火墙状态
[root@mdw ~]# systemctl status firewalld
# 临时关闭防火墙
[root@mdw ~]# systemctl stop firewalld
# 永久防火墙关闭
[root@mdw ~]# systemctl disable firewalld

# 先临时关闭(不方便重启时 先临时关闭下 并设置永久关闭 下次重启机器时 自动生效)
[root@mdw ~]# /usr/sbin/setenforce 0
# 永久关闭SELinux
[root@mdw ~]# /bin/sed -i 's@SELINUX=enforcing@SELINUX=disabled@g' /etc/selinux/config 
[root@mdw ~]# /bin/grep SELINUX=disabled /etc/selinux/config

```


## yum安装一些必要的工具
```
[root@mdw ~]# yum install -y net-tools.x86_64 vim lrzsz lsof wget screen tree ntpdate 
```

## 固定IP
```
[root@mdw ~]# vim /etc/sysconfig/network-scripts/ifcfg-eno16777736
BOOTPROTO="static"
ONBOOT="yes"
IPADDR=192.168.198.128
NETMASK=225.225.225.0
GATEWAY=192.168.198.2
DNS1=114.114.114.114

[root@mdw ~]# systemctl restart network
```

## 开机自启服务优化
```
# 保留crond rsyslog sshd network systemd stsatst
[root@mdw ~]# systemctl list-unit-files|grep enable|grep -Ev "crond|rsyslog|sshd|network|systemd|sysatst"|awk '{print "systemctl disable "$1}'|bash
```

## 调整系统文件描述符
```
[root@base ~]# /bin/echo '*       -       nofile  65535' >>/etc/security/limits.conf
[root@base ~]# /usr/bin/tail -1 /etc/security/limits.conf
[root@base ~]# ulimit -n 65535
```

## 清除系统敏感信息
```
[root@mdw ~]# >/etc/issue
[root@mdw ~]# >/etc/motd
```

## 优化yum源
```
# Base源
[root@base ~]# wget -O /etc/yum.repos.d/CentOS-Base.repo http://mirrors.aliyun.com/repo/Centos-7.repo
# Epel源
[root@base ~]# wget -O /etc/yum.repos.d/epel.repo http://mirrors.aliyun.com/repo/epel-7.repo
```

## 时间同步
```
# 加入定时任务-注意要在root用户下
[root@base ~]# crontab -e
# Sync time by tianyafu at 20210816
*/5 * * * * /usr/sbin/ntpdate ntp1.aliyun.com &>/dev/null
[root@base ~]# crontab -l
# Sync time by tianyafu at 20210816
*/5 * * * * /usr/sbin/ntpdate ntp1.aliyun.com &>/dev/null
```

# 添加用户并赋予密码
```
[root@mdw ~]# useradd admin
[root@mdw ~]# id admin
uid=1000(admin) gid=1000(admin) 组=1000(admin)
[root@mdw ~]# echo "admin   ALL=(ALL)       NOPASSWD: ALL" >> /etc/sudoers
[root@mdw ~]# passwd admin
admin
```

## 安装JDK
```
[admin@base ~]$ sudo mkdir  /usr/java
[admin@base ~]$ sudo tar -zxvf ~/software/jdk-8u181-linux-x64.tar.gz -C /usr/java
[admin@base ~]$ sudo su -
[root@base ~]# cd /usr/java
[root@base java]# chown -R root:root jdk1.8.0_181/
[root@base java]# echo -e '# JAVA ENV\nexport JAVA_HOME=/usr/java/jdk1.8.0_181\nexport PATH=$JAVA_HOME/bin:$PATH' >>/etc/profile
[root@base java]# source /etc/profile
[root@base java]# which java
/usr/java/jdk1.8.0_181/bin/java

```

## 安装Python3
```
# 切换用户
[admin@base ~]$ sudo su -
# 安装依赖
[root@base ~]# yum -y install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel libffi-devel gcc
[root@base src]# cd /usr/local/src/
[root@base src]# wget https://www.python.org/ftp/python/3.6.5/Python-3.6.5.tgz
[root@base src]# tar xf /usr/local/src/Python-3.6.5.tgz
[root@base src]# cd Python-3.6.5
[root@base Python-3.6.5]# ./configure --prefix=/usr/local/python3.6.5
[root@base Python-3.6.5]# make -j 4 && make install
[root@base Python-3.6.5]# ln -s /usr/local/python3.6.5 /usr/local/python3
[root@base Python-3.6.5]# echo -e '# Python PATH\nexport PATH=$PATH:/usr/local/python3/bin' >>/etc/profile
[root@base Python-3.6.5]# source /etc/profile
[root@base Python-3.6.5]# python3 -m pip install --upgrade pip

# 优化PIP源
[root@base Python-3.6.5]# vim /etc/pip.conf
[global]
trusted-host =  pypi.douban.com
index-url = http://pypi.douban.com/simple
```