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
[root@mdw ~]# yum install -y net-tools.x86_64 vim lrzsz lsof wget screen tree ntpdate zip unzip telent dos2unix traceroute nmap htop iotop iftop telnet
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
有时候会碰到ping网关 能通  ping DNS不通  这个是缺少指向网关的路由 参考https://blog.csdn.net/qq_40572277/article/details/106517502
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

## 添加hosts文件
```
[root@tianyafu ~]# echo "192.168.198.200 tianyafu" >> /etc/hosts
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

[root@tianyafu ~]# useradd hadoop
[root@tianyafu ~]# passwd hadoop
密码设置为hadoop即可
[root@tianyafu ~]# echo "hadoop   ALL=(ALL)       NOPASSWD: ALL" >> /etc/sudoers
```


# 切换到admin后 创建相应的目录
```
[root@base ~]# su - admin
[admin@base ~]$ mkdir tmp sourcecode software shell log lib  data app dw 
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
[root@base ~]# yum -y install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel libffi-devel gcc gcc-c++
[root@base src]# cd /usr/local/src/
[root@base src]# wget https://www.python.org/ftp/python/3.6.8/Python-3.6.8.tgz
[root@base src]# tar xf /usr/local/src/Python-3.6.8.tgz
[root@base src]# cd Python-3.6.8
[root@base Python-3.6.8]# ./configure --prefix=/usr/local/python3.6.8
[root@base Python-3.6.8]# make -j 4 && make install
[root@base Python-3.6.8]# ln -s /usr/local/python3.6.8 /usr/local/python3
[root@base Python-3.6.8]# echo -e '# Python PATH\nexport PATH=$PATH:/usr/local/python3/bin' >>/etc/profile
[root@base Python-3.6.8]# source /etc/profile
[root@base Python-3.6.8]# python3 -m pip install --upgrade pip

# 优化PIP源
[root@base Python-3.6.8]# vim /etc/pip.conf
[global]
trusted-host =  pypi.douban.com
index-url = http://pypi.douban.com/simple

# 安装必要的包
yum -y install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gdbm-devel db4-devel libpcap-devel xz-devel libffi-devel python-devel.x86_64 cyrus-sasl-devel.x86_64 mysql-devel python3-devel.x86_64
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ pandas==0.23.4
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ interval3==2.0.0
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ PyHive==0.6.1
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ pymssql==2.1.4
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ PyMySQL==0.9.3
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ thrift==0.11.0
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ psycopg2-binary==2.8.2
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ thrift-sasl==0.3.0
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ sasl==0.2.1
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ requests==2.19.1
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ sqlalchemy==1.2.11
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ pyquery==1.4.3
pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple httpx==0.22.0
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ SQLAlchemy==1.4.39
pip3 install -i https://pypi.tuna.tsinghua.edu.cn/simple pyspark==2.4.6

算法用的库:
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ tqdm==4.62.3
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ sshtunnel==0.4.0
pip3 install --upgrade nni --ignore-installed -i https://mirrors.aliyun.com/pypi/simple/ PyYAML==5.4.1
pip3 install --upgrade nni --ignore-installed -i https://mirrors.aliyun.com/pypi/simple/ cn2an==0.5.11

pip3 install selenium -i http://pypi.douban.com/simple/ --trusted-host pypi.douban.com

其他常用的库:
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ python-docx==0.8.11
pip3 install -i https://mirrors.aliyun.com/pypi/simple/ openpyxl==3.0.10
```





## 部署MySQL
```

```

## 部署Hadoop
```
[root@tianyafu ~]# su - admin
[admin@tianyafu ~]$ tar -zxvf ~/software/hadoop-2.6.0-cdh5.16.2.tar.gz -C ~/app/
# 设置软连接
[admin@tianyafu ~]$ cd ~/app/
[admin@tianyafu app]$ ln -s hadoop-2.6.0-cdh5.16.2 hadoop
# 配置信任关系
[admin@tianyafu ~]$ cd
[admin@tianyafu ~]$ rm -rf ~/.ssh
[admin@tianyafu ~]$ ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
[admin@tianyafu ~]$ cd ~/.ssh/
[admin@tianyafu .ssh]$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
[admin@tianyafu .ssh]$ chmod 0600 ~/.ssh/authorized_keys
[admin@tianyafu .ssh]$ ssh tianyafu date
The authenticity of host 'tianyafu (192.168.198.200)' can't be established.
ECDSA key fingerprint is 1e:e2:6d:86:80:f4:dc:ca:69:0e:68:7d:96:20:2d:2e.
Are you sure you want to continue connecting (yes/no)? yes
Warning: Permanently added 'tianyafu,192.168.198.200' (ECDSA) to the list of known hosts.
2021年 10月 22日 星期五 12:31:34 CST

# 修改配置文件
[admin@tianyafu .ssh]$ cd ~/app/hadoop/etc/hadoop
# hadoop-env.sh 显式指定JAVA_HOME
[admin@tianyafu hadoop]$ vim hadoop-env.sh
export JAVA_HOME=/usr/java/jdk1.8.0_181
# core-site.xml
[admin@tianyafu hadoop]$ vim core-site.xml
<configuration>
<property>
<name>fs.defaultFS</name>
<value>hdfs://tianyafu:9000</value>
</property>
<property>
<name>hadoop.tmp.dir</name>
<value>/home/admin/tmp/</value>
</property>
</configuration>

# hdfs-site.xml
[admin@tianyafu hadoop]$ vim hdfs-site.xml
<configuration>
<property>
<name>dfs.replication</name>
<value>1</value>
</property>
<property>
<name>dfs.namenode.secondary.http-address</name>
<value>tianyafu:9868</value>
</property>
<property>
<name>dfs.namenode.secondary.https-address</name>
<value>tianyafu:9869</value>
</property>
</configuration>

# slaves文件
[admin@tianyafu hadoop]$ vim slaves
tianyafu

# 格式化Namenode
[admin@tianyafu hadoop]$ cd ~/app/hadoop/
[admin@tianyafu hadoop]$ bin/hdfs namenode -format
# 启动HDFS
[admin@tianyafu hadoop]$ sbin/start-dfs.sh

# 部署yarn
[admin@tianyafu hadoop]$ cd ~/app/hadoop/etc/hadoop
[admin@tianyafu hadoop]$ cp mapred-site.xml.template mapred-site.xml
[admin@tianyafu hadoop]$ vim mapred-site.xml
<configuration>
<property>
<name>mapreduce.framework.name</name>
<value>yarn</value>
</property>
</configuration>

# yarn-site.xml 注意：要修改yarn的web页面的默认端口，默认为8088；防止被挖矿
[admin@tianyafu hadoop]$ vim yarn-site.xml
<configuration>
<property>
<name>yarn.nodemanager.aux-services</name>
<value>mapreduce_shuffle</value>
</property>
<property>
<name>yarn.resourcemanager.webapp.address</name>
<value>tianyafu:18088</value>
</property>
</configuration>

# 启动
[admin@tianyafu hadoop]$ cd ~/app/hadoop
[admin@tianyafu hadoop]$ sbin/start-yarn.sh

# 配置环境变量
[admin@tianyafu hadoop]$ cd
[admin@tianyafu ~]$ echo -e '# HADOOP_HOME\nexport HADOOP_HOME=/home/admin/app/hadoop\nexport PATH=$HADOOP_HOME/bin:$HADOOP_HOME/sbin:$PATH' >>~/.bashrc
[admin@tianyafu ~]$ source ~/.bashrc
# 验证
[admin@tianyafu ~]$ which hdfs
~/app/hadoop/bin/hdfs
```

## 部署Hive
```
[admin@tianyafu ~]$ tar -xvf ~/software/hive-1.1.0-cdh5.16.2.tar.gz -C ~/app/
[admin@tianyafu ~]$ ln -s ~/app/hive-1.1.0-cdh5.16.2/ ~/app/hive
# 参数配置参考：https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties
[admin@tianyafu ~]$ cd ~/app/hive/conf
# 修改hive-env.sh
[admin@tianyafu conf]$ cp hive-env.sh.template hive-env.sh
# 显式配置hadoop的home
[admin@tianyafu conf]$ vim hive-env.sh
HADOOP_HOME=$HADOOP_HOME
export HADOOP_HEAPSIZE=1024 # 生产上面这个参数是要调大的，1个G是不够的
# 编辑hive-site.xml，这个配置文件默认是没有提供模板的，所以要自己创建
[admin@tianyafu conf]$ vim hive-site.xml
<?xml version="1.0"?>

<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
<property>
<name>javax.jdo.option.ConnectionURL</name>
<value>jdbc:mysql://localhost:3306/hive?createDatabaseIfNotExist=true&amp;useSSL=false&amp;useUnicode=true&amp;characterEncoding=utf8</value>
</property>
<property>
<name>javax.jdo.option.ConnectionDriverName</name>
<value>com.mysql.jdbc.Driver</value>
</property>
<property>
<name>javax.jdo.option.ConnectionUserName</name>
<value>root</value>
</property>
<property>
<name>javax.jdo.option.ConnectionPassword</name>
<value>root</value>
</property>
<property>
<name>hive.cli.print.current.db</name>
<value>true</value>
<description>打印当前hive库名</description>
</property>
<property>
<name>hive.cli.print.header</name>
<value>true</value>
<description>打印当前hive表字段名</description>
</property>
<property>
<name>hive.server2.webui.host</name>
<value>tianyafu</value>
<description>hiveserver2的web ui</description>
</property>
<property>
<name>hive.server2.webui.port</name>
<value>19990</value>
<description>hiveserver2的web ui 的 port</description>
</property>
<property>
<name>hive.metastore.uris</name>
<value>thrift://tianyafu:9083</value>
</property>
</configuration>

# 配置环境变量
[admin@tianyafu conf]$ echo -e '# HIVE ENV\nexport HIVE_HOME=/home/admin/app/hive\nexport HIVE_CONF_DIR=$HIVE_HOME/conf\nexport PATH=$HIVE_HOME/bin:$PATH' >> ~/.bashrc
[admin@tianyafu conf]$ source ~/.bashrc
# 验证
[admin@tianyafu conf]$ which hive
~/app/hive/bin/hive

# 上传MySQL驱动包
# 将mysql-connector-java-5.1.47.jar包上传到/home/hadoop/lib/目录下
[admin@tianyafu conf]$ cp ~/lib/mysql-connector-java-5.1.47.jar ~/app/hive/lib/

# 启动MySQL
[admin@tianyafu ~]$ sudo su -
[root@tianyafu ~]# su - mysqladmin
tianyafu:mysqladmin:/usr/local/mysql:>service mysql start

# MySQL中新建hive库 设置库的编码为utf8mb4
create database IF NOT EXISTS hive default charset utf8mb4 COLLATE utf8mb4_general_ci;

# 通过schematool创建元数据表
[admin@tianyafu hive]$ schematool -dbType mysql -initSchema

#修改hive元数据库部分表的编码
避免表字段注释及表注释乱码

-- 修改建库时的库注释的字符集
alter table dbs default character set utf8 COLLATE utf8_general_ci; -- 库信息相关表
alter table dbs modify `DESC` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复表字段注释中文乱码问题

-- 修改表的字符集
alter table columns_v2 default character set utf8 COLLATE utf8_general_ci; -- 表字段信息相关表
alter table table_params default character set utf8 COLLATE utf8_general_ci; -- 表属性相关表
alter table partition_keys default character set utf8 COLLATE utf8_general_ci; -- 分区key相关表
alter table tbls default character set utf8 COLLATE utf8_general_ci; -- Hive表信息相关表



-- 修改表字段的字符集
alter table columns_v2 modify `COMMENT` varchar(256) character set utf8 COLLATE utf8_general_ci; -- 修复表字段注释中文乱码问题
alter table table_params modify `PARAM_VALUE` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复表注释中文乱码问题
alter table partition_keys modify `PKEY_COMMENT` varchar(4000) character set utf8 COLLATE utf8_general_ci; -- 修复分区字段注释中文乱码问题

-- 视图DDL语句的字符集
alter table tbls modify `VIEW_EXPANDED_TEXT` mediumtext character set utf8 COLLATE utf8_general_ci; -- 修复视图DDL中文乱码问题
alter table tbls modify `VIEW_ORIGINAL_TEXT` mediumtext character set utf8 COLLATE utf8_general_ci; -- 修复视图DDL中文乱码问题


# 启动hive
[admin@tianyafu hive]$ nohup ~/app/hive/bin/hive --service metastore &
[admin@tianyafu ~]$ hive
```


