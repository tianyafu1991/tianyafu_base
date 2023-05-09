# CDH部署文档

## 安装包准备
```
JDK:jdk-8u181-linux-x64.tar.gz
MySQL:mysql-5.7.26-el7-x86_64.tar.gz
驱动包:mysql-connector-java-5.1.47.jar
CM tar包:cloudera-manager-centos7-cm5.16.1_x86_64.tar.gz
parcel等3个文件:
CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel
CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel.sha1
manifest.json

CDH官方是对JDK的版本有推荐  推荐版本是CDH经过测试的 最佳实践是使用官方推荐的版本
详见:https://docs.cloudera.com/documentation/enterprise/release-notes/topics/rn_consolidated_pcm.html#concept_ihg_vf4_j1b
```

## 创建对应的目录 并上传安装包
```
[root@mdw ~]# mkdir ~/cdh5.16.1
[root@mdw ~]# cd ~/cdh5.16.1/
# 将所需要的安装包上传到该目录下

```

## 安装必要的包
```shell
# 3台一起执行 部分机器上碰到启动CM Server和 CM Agent 时 报pstree command not found 需要先在服务器上安装psmisc
[root@mdw cdh5.16.1]# yum install -y psmisc compat-openssl10
[root@sdw1 ~]# yum install -y psmisc compat-openssl10
[root@sdw2 ~]# yum install -y psmisc compat-openssl10
```

## 集群节点初始化配置
```
# 配置hosts文件
[root@mdw cdh5.16.1]# cd
[root@mdw ~]# vim /etc/hosts
172.24.88.143 mdw
172.24.88.142 sdw1
172.24.88.141 sdw2
[root@mdw ~]# scp /etc/hosts sdw1:/etc/hosts
[root@mdw ~]# scp /etc/hosts sdw2:/etc/hosts
# 配置信任关系 部署CDH不是必须要配置信任关系 但配置信任关系后 集群间scp方便 且后续DolphinScheduler部署是必须要打通信任关系的
[root@mdw ~]# ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
# 将hadoop002和hadoop003上的公钥文件拷贝到hadoop001上
[root@sdw1 ~]# scp ~/.ssh/id_rsa.pub  mdw:~/.ssh/id_rsa.pub2
[root@sdw2 ~]# scp ~/.ssh/id_rsa.pub  mdw:~/.ssh/id_rsa.pub3
# 将公钥添加到~/.ssh/authorized_keys文件中
[root@mdw ~]# cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
[root@mdw ~]# cat ~/.ssh/id_rsa.pub2 >> ~/.ssh/authorized_keys
[root@mdw ~]# cat ~/.ssh/id_rsa.pub3 >> ~/.ssh/authorized_keys 
# 分发到各个节点上
[root@mdw ~]# scp ~/.ssh/authorized_keys sdw1:~/.ssh/
[root@mdw ~]# scp ~/.ssh/authorized_keys sdw2:~/.ssh/
# 3台机器上 对各个节点上的~/.ssh/authorized_keys设置权限
[root@mdw ~]# chmod 0600 ~/.ssh/authorized_keys
# 验证 如信任关系打通 第一次需要输入yes 但无需输入密码了
[root@mdw ~]# ssh mdw date
[root@mdw ~]# ssh sdw1 date
[root@mdw ~]# ssh sdw2 date

# 关闭防火墙 各个节点都关闭
[root@mdw ~]# systemctl stop firewalld
[root@mdw ~]# systemctl disable firewalld
# 清空防火墙规则
[root@mdw ~]# iptables -F

# 关闭selinux  各个节点都关闭
[root@mdw ~]# /usr/sbin/setenforce 0
[root@mdw ~]# /bin/sed -i 's@SELINUX=enforcing@SELINUX=disabled@g' /etc/selinux/config 
[root@mdw ~]# /bin/grep SELINUX=disabled /etc/selinux/config

# 时区 时钟的同步
# 设置时区为上海 各个节点都执行
[root@mdw ~]# timedatectl set-timezone Asia/Shanghai
# 设置时钟同步 各个节点安装ntp服务
[root@mdw ~]# yum install -y ntp
# 选择mdw为主节点 其它2台的时间与mdw同步
[root@mdw ~]# yum install -y vim
[root@mdw ~]# vim /etc/ntp.conf
# 添加以下内容
# time
server 0.asia.pool.ntp.org
server 2.asia.pool.ntp.org
server 3.asia.pool.ntp.org
server 4.asia.pool.ntp.org
# 当外部时间不可用，可以使用当前本地硬件时间
server 127.127.1.0 iburst local clock
#允许哪些网段的机器来同步时间 网段要根据自己的网段设置
restrict 172.24.88.0 mask 255.255.255.0 nomodify notrap

# 设置开机自启 并启动ntp服务
[root@mdw ~]# systemctl enable ntpd.service
[root@mdw ~]# systemctl start ntpd.service
[root@mdw ~]# systemctl status ntpd.service
# 验证ntp服务
[root@mdw ~]# ntpq -p

# 2个从节点都需要关闭ntpd服务
[root@mdw ~]# systemctl stop ntpd
[root@mdw ~]# systemctl disable ntpd
# 设置crontab 每天0点定时向主节点同步即可
[root@mdw ~]# crontab -e
00 00 * * * /usr/sbin/ntpdate mdw
# 每5分钟同步一次
*/5 * * * * /usr/sbin/ntpdate mdw & >/dev/null 2>&1
```
## 部署JDK
```shell
# JDK必须统一部署在/usr/java/目录下 这个是CDH官网要求的 且每个节点上的JDK版本要一致
# 详见:https://docs.cloudera.com/documentation/enterprise/5-16-x/topics/cdh_ig_jdk_installation.html#topic_29

# 各个节点都要创建该目录
[root@mdw ~]# mkdir /usr/java
# 分发JDK到各个节点的/tmp目录下  JDK事先上传到了mdw的/root/cdh5.16.1目录下
[root@mdw ~]# mv /root/cdh5.16.1/jdk-8u181-linux-x64.tar.gz /tmp
[root@mdw ~]# scp /tmp/jdk-8u181-linux-x64.tar.gz sdw1:/tmp/
[root@mdw ~]# scp /tmp/jdk-8u181-linux-x64.tar.gz sdw2:/tmp/

# 各个节点解压
[root@mdw ~]# tar -zxvf /tmp/jdk-8u181-linux-x64.tar.gz -C /usr/java/
# 各个节点修正/usr/java/下的JDK目录的所属用户和用户组
[root@mdw ~]# chown  -R root:root /usr/java/jdk1.8.0_181
# 各个节点配置环境变量并source环境变量
[root@mdw ~]# echo -e '# JAVA ENV\nexport JAVA_HOME=/usr/java/jdk1.8.0_181\nexport PATH=$JAVA_HOME/bin:$PATH' >>/etc/profile
[root@mdw ~]# source /etc/profile
[root@mdw ~]# which java
/usr/java/jdk1.8.0_181/bin/java
```

## Python
```
# Anolis OS 8.4服务器没有python 需要部署python2   3台一起部署
[root@mdw cdh5.16.1]# yum install -y python2
[root@mdw cdh5.16.1]# cd /usr/bin/
[root@mdw bin]# rm -rf python
[root@mdw bin]# ln -s /usr/bin/python2.7 /usr/bin/python

```

## 离线部署MySQL
```shell
# MySQL选择部署在hadoop001上 已事先上传到了hadoop001的/root/cdh5.16.1
[root@mdw ~]#  mv /root/cdh5.16.1/mysql-5.7.26-el7-x86_64.tar.gz /usr/local/
[root@hadoop001 ~]# cd /usr/local/
[root@hadoop001 local]# tar -xvf /usr/local/mysql-5.7.26-el7-x86_64.tar.gz -C /usr/local/
[root@hadoop001 local]# mv mysql-5.7.26-el7-x86_64 mysql
# 创建binlog归档目录、数据目录、临时目录
[root@hadoop001 local]# mkdir mysql/arch mysql/data mysql/tmp
[root@hadoop001 local]# cp /etc/my.cnf my.cnf.bak
# 先清空/etc/my.cnf 再将内容拷贝到/etc/my.cnf中
[root@hadoop001 local]# >/etc/my.cnf
[root@hadoop001 local]# vi /etc/my.cnf

[client]
port            = 3306
socket          = /usr/local/mysql/data/mysql.sock
default-character-set=utf8mb4

[mysqld]

port            = 3306
socket          = /usr/local/mysql/data/mysql.sock
skip-slave-start
skip-external-locking
key_buffer_size = 256M
sort_buffer_size = 2M
read_buffer_size = 2M
read_rnd_buffer_size = 4M
query_cache_size= 32M
max_allowed_packet = 16M
myisam_sort_buffer_size=128M
tmp_table_size=32M
table_open_cache = 512
thread_cache_size = 8
wait_timeout = 86400
interactive_timeout = 86400
max_connections = 600

# Try number of CPU's*2 for thread_concurrency

#thread_concurrency = 32

#isolation level and default engine
default-storage-engine = INNODB
transaction-isolation = READ-COMMITTED
server-id  = 1739
basedir    = /usr/local/mysql
datadir    = /usr/local/mysql/data
pid-file    = /usr/local/mysql/data/hostname.pid

#open performance schema
log-warnings
sysdate-is-now
binlog_format = ROW
log_bin_trust_function_creators=1
log-error  = /usr/local/mysql/data/hostname.err
log-bin = /usr/local/mysql/arch/mysql-bin
expire_logs_days = 7
innodb_write_io_threads=16
relay-log  = /usr/local/mysql/relay_log/relay-log
relay-log-index = /usr/local/mysql/relay_log/relay-log.index
relay_log_info_file= /usr/local/mysql/relay_log/relay-log.info
log_slave_updates=1
gtid_mode=OFF
enforce_gtid_consistency=OFF

sql_mode=STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION
# slave

slave-parallel-type=LOGICAL_CLOCK
slave-parallel-workers=4
master_info_repository=TABLE
relay_log_info_repository=TABLE
relay_log_recovery=ON

#other logs

#general_log =1

#general_log_file  = /usr/local/mysql/data/general_log.err

#slow_query_log=1

#slow_query_log_file=/usr/local/mysql/data/slow_log.err
#for replication slave
sync_binlog = 500
#for innodb options
innodb_data_home_dir = /usr/local/mysql/data/
innodb_data_file_path = ibdata1:1G;ibdata2:1G:autoextend
innodb_log_group_home_dir = /usr/local/mysql/arch
innodb_log_files_in_group = 4
innodb_log_file_size = 1G
innodb_log_buffer_size = 200M
#根据生产需要，调整pool size
innodb_buffer_pool_size = 2G
#innodb_additional_mem_pool_size = 50M #deprecated in 5.6
tmpdir = /usr/local/mysql/tmp
innodb_lock_wait_timeout = 1000
#innodb_thread_concurrency = 0
innodb_flush_log_at_trx_commit = 2
innodb_locks_unsafe_for_binlog=1
#innodb io features: add for mysql5.5.8
performance_schema
innodb_read_io_threads=4
innodb-write-io-threads=4
innodb-io-capacity=200
#purge threads change default(0) to 1 for purge
innodb_purge_threads=1
innodb_use_native_aio=on
#case-sensitive file names and separate tablespace
innodb_file_per_table = 1
lower_case_table_names=1

[mysqldump]
quick
max_allowed_packet = 128M

[mysql]
no-auto-rehash
default-character-set=utf8mb4

[mysqlhotcopy]
interactive-timeout

[myisamchk]
key_buffer_size = 256M
sort_buffer_size = 256M
read_buffer = 2M
write_buffer = 2M



# 创建用户组及用户
[root@hadoop001 local]# groupadd -g 101 mysql
[root@hadoop001 local]# useradd -u 514 -g mysql -G root -d /usr/local/mysql mysqladmin
useradd: warning: the home directory already exists.
Not copying any file from skel directory into it.
[root@hadoop001 local]#  id mysqladmin
uid=514(mysqladmin) gid=101(mysql) groups=101(mysql),0(root)

# copy 环境变量配置文件至mysqladmin用户的home目录中,为了以下步骤配置个人环境变量
[root@hadoop001 local]# cp /etc/skel/.* /usr/local/mysql

# 编辑mysql/.bashrc 在文件后追加以下内容
[root@hadoop001 local]# vi mysql/.bashrc

export MYSQL_BASE=/usr/local/mysql
export PATH=${MYSQL_BASE}/bin:$PATH
unset USERNAME
#stty erase ^H
set umask to 022
umask 022
PS1=`uname -n`":"'$USER'":"'$PWD'":>"; export PS1
## end


# 赋权限和用户组，切换用户mysqladmin，安装
[root@hadoop001 local]# chown mysqladmin:mysql /etc/my.cnf
[root@hadoop001 local]# chmod  640 /etc/my.cnf
[root@hadoop001 local]# chown -R mysqladmin:mysql /usr/local/mysql
[root@hadoop001 local]# chmod -R 755 /usr/local/mysql

# 设置开机自启
[root@hadoop001 local]# cd /usr/local/mysql
[root@hadoop001 mysql]# cp support-files/mysql.server /etc/rc.d/init.d/mysql
[root@hadoop001 mysql]# chmod +x /etc/rc.d/init.d/mysql
[root@hadoop001 mysql]# chkconfig --del mysql
[root@hadoop001 mysql]# chkconfig --add mysql
[root@hadoop001 mysql]# chkconfig --level 345 mysql on
# 编辑/etc/rc.local 追加以下内容
[root@hadoop001 mysql]# vi /etc/rc.local
su - mysqladmin -c "/etc/init.d/mysql start --federated"

# 赋权 该文件如果没有执行权限 MySQL开机无法自启动
[root@hadoop001 mysql]# chmod +x /etc/rc.d/rc.local

# 安装libaio及安装mysql的初始db
[root@hadoop001 mysql]# yum -y install libaio
# 切换到mysqladmin用户并执行以下命令
[root@hadoop001 mysql]# su - mysqladmin
bin/mysqld \
--defaults-file=/etc/my.cnf \
--user=mysqladmin \
--basedir=/usr/local/mysql/ \
--datadir=/usr/local/mysql/data/ \
--initialize

# 查看临时密码
hadoop001:mysqladmin:/usr/local/mysql:>cd /usr/local/mysql/data
hadoop001:mysqladmin:/usr/local/mysql/data:>cat hostname.err |grep password
2021-12-02T14:55:15.025740Z 1 [Note] A temporary password is generated for root@localhost: uhbkgijW%4yJ

# 启动
hadoop001:mysqladmin:/usr/local/mysql/data:>/usr/local/mysql/bin/mysqld_safe --defaults-file=/etc/my.cnf &
# 使用临时密码登入到MySQL的命令行中
hadoop001:mysqladmin:/usr/local/mysql/data:>mysql -uroot -p'uhbkgijW%4yJ'

# 添加root用户并赋权
mysql> alter user root@localhost identified by 'root';
Query OK, 0 rows affected (0.00 sec)

mysql> GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'root';
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql>  flush privileges;
Query OK, 0 rows affected (0.01 sec)
# 重启MySQL
hadoop001:mysqladmin:/usr/local/mysql/data:>service mysql restart
```

## 创建CDH需要的MySQL库和用户并赋权
```shell
# cmf是cm server需要用的元数据库 amon是Activity Monitor服务需要用的存储库
# 其实官方推荐的cm server的元数据库名称为scm 详见:https://docs.cloudera.com/documentation/enterprise/5-16-x/topics/cm_ig_mysql.html#concept_dsg_3mq_bl
# 但是cm server的db.properties文件中 配置db的时候参数值默认就是cmf 所以cmf也是官方推荐的
mysql> create database cmf default character set utf8 DEFAULT COLLATE utf8_general_ci;
Query OK, 1 row affected (0.00 sec)

mysql> grant all privileges on cmf.* to 'cmf'@'%' identified by 'cmf';
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> create database amon default character set utf8 DEFAULT COLLATE utf8_general_ci;
Query OK, 1 row affected (0.00 sec)

mysql> grant all privileges on amon.* to 'amon'@'%' identified by 'amon';
Query OK, 0 rows affected, 1 warning (0.00 sec)

mysql> flush privileges;
Query OK, 0 rows affected (0.00 sec)

```

## 部署MySQL的驱动包
```shell
# MySQL的驱动包一定要放在/usr/share/java目录下  且必须重命名为mysql-connector-java.jar 且官方推荐使用5.1.x系列的jar包:Cloudera recommends using only version 5.1 of the JDBC driver.
# 参见:https://docs.cloudera.com/documentation/enterprise/5-16-x/topics/cm_ig_mysql.html#cmig_topic_5_5_3
[root@hadoop001 mysql]# cd
[root@hadoop001 ~]# mkdir -p /usr/share/java
[root@hadoop001 ~]# mv /root/cdh5.16.1/mysql-connector-java-5.1.47.jar /usr/share/java/mysql-connector-java.jar
```

## 部署CM
```shell
# 移动cm的安装包到/tmp并分发到各个节点
[root@mdw ~]# mv /root/cdh5.16.1/cloudera-manager-centos7-cm5.16.1_x86_64.tar.gz /tmp/
[root@mdw ~]# scp /tmp/cloudera-manager-centos7-cm5.16.1_x86_64.tar.gz sdw1:/tmp/
[root@mdw ~]# scp /tmp/cloudera-manager-centos7-cm5.16.1_x86_64.tar.gz sdw2:/tmp/

# 各个节点都要创建目录
[root@mdw ~]# mkdir /opt/cloudera-manager
[root@mdw tmp]# cd /tmp/
[root@mdw tmp]# tar -zxvf cloudera-manager-centos7-cm5.16.1_x86_64.tar.gz -C /opt/cloudera-manager/


[root@mdw tmp]# cd /opt/cloudera-manager/cm-5.16.1/etc/cloudera-scm-agent/
# 配置各个节点上的配置文件 cm server的host要指向hadoop001
[root@mdw cloudera-scm-agent]# vim config.ini
server_host=mdw

# cm server节点上 去修改cm server的元数据库信息
[root@mdw cloudera-scm-agent]# cd ../cloudera-scm-server/
[root@mdw cloudera-scm-server]# vim db.properties
com.cloudera.cmf.db.host=mdw
com.cloudera.cmf.db.name=cmf
com.cloudera.cmf.db.user=cmf
com.cloudera.cmf.db.password=cmf
com.cloudera.cmf.db.setupType=EXTERNAL

# 各个节点都要创建一个cloudera-scm这个用户
[root@mdw cloudera-scm-server]# useradd --system --home=/opt/cloudera-manager/cm-5.16.1/run/cloudera-scm-server --no-create-home --shell=/bin/false --comment "cloudera scm user" cloudera-scm
[root@mdw cloudera-scm-server]# chown -R cloudera-scm:cloudera-scm /opt/cloudera-manager
[root@mdw cloudera-scm-server]# cd /opt/cloudera-manager
[root@mdw cloudera-manager]# ll

```

# parcel离线部署
```shell
[root@mdw cm-5.16.1]# cd /root/cdh5.16.1/
# 去除CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel.sha1该文件末尾的1 如果不去掉 在部署过程中 cm会认为该parcel文件还未下载完成 会继续下载
[root@mdw cdh5.16.1]# mv CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel.sha1 CDH-5.16.1-1.cdh5.16.1.p0.3-unknown.parcel.sha
[root@mdw cdh5.16.1]# mv CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel CDH-5.16.1-1.cdh5.16.1.p0.3-unknown.parcel
# 将"parcelName": "CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel" 也改为unknow
[root@mdw cdh5.16.1]# vim manifest.json
# 校验parcel文件 用sha1sum计算parcel包 要 与CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel.sha中的一致
[root@mdw cdh5.16.1]# sha1sum CDH-5.16.1-1.cdh5.16.1.p0.3-unknown.parcel
703728dfa7690861ecd3a9bcd412b04ac8de7148  CDH-5.16.1-1.cdh5.16.1.p0.3-el7.parcel
[root@mdw cdh5.16.1]# cat CDH-5.16.1-1.cdh5.16.1.p0.3-unknown.parcel.sha
703728dfa7690861ecd3a9bcd412b04ac8de7148
# 在mdw上部署parcel的离线源
# 创建并移动parcel等3个文件到parcel-repo目录中 并设置所属用户和用户组
[root@mdw cdh5.16.1]# mkdir -p /opt/cloudera/parcel-repo
[root@mdw cdh5.16.1]# mv CDH-5.16.1-1.cdh5.16.1.p0.3-unknown.parcel* /opt/cloudera/parcel-repo
[root@mdw cdh5.16.1]# mv manifest.json /opt/cloudera/parcel-repo
[root@mdw cdh5.16.1]# chown -R cloudera-scm:cloudera-scm /opt/cloudera
[root@mdw cdh5.16.1]# cd /opt/cloudera
[root@mdw cloudera]# ll
total 4
drwxr-xr-x 2 cloudera-scm cloudera-scm 4096 Dec  3 06:37 parcel-repo
```

## 正式集群部署
```shell
# 创建各个节点的软件安装目录 用户和用户组的权限
[root@mdw cloudera]# mkdir -p /opt/cloudera/parcels
[root@mdw cloudera]# chown -R cloudera-scm:cloudera-scm /opt/cloudera

# 启动 cm server
[root@mdw ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-server start
# 查看日志
[root@mdw ~]# tail -200f /opt/cloudera-manager/cm-5.16.1/log/cloudera-scm-server/cloudera-scm-server.log


# 各个节点启动agent
[root@hadoop001 ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-agent start
[root@hadoop001 ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-agent status
cloudera-scm-agent (pid  4288) is running...
[root@hadoop001 ~]# tail -200f /opt/cloudera-manager/cm-5.16.1/log/cloudera-scm-agent/cloudera-scm-agent.log

# agent启动时 依次报错:
1.ImportError: libssl.so.10: cannot open shared object file: No such file or directory
CentOS8 中缺少这个依赖:compat-openssl10 通过yum install -y compat-openssl10解决
参考:https://blog.csdn.net/Shockang/article/details/126238210
```

## 按页面提示关闭大页面
```shell
echo never > /sys/kernel/mm/transparent_hugepage/defrag
echo never > /sys/kernel/mm/transparent_hugepage/enabled
```

## 关闭集群
```shell
# 各个节点关闭agent
[root@hadoop001 ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-agent stop

[root@hadoop001 ~]# /opt/cloudera-manager/cm-5.16.1/etc/init.d/cloudera-scm-server stop
[root@hadoop001 ~]# su - mysqladmin
hadoop001:mysqladmin:/usr/local/mysql:>service mysql stop
```

## 增加Hive Service
```shell
# 1. 页面部署
# 2. Hive元数据库表的字符集修改

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

# 3. 参数优化
3.1 metastore 和 hiveServer2的java heap默认只有50M  改为为1G
3.2 hive.fetch.task.conversion参数设置为more
3.3 hive-site.xml的高级代码段 Hive Client Advanced Configuration Snippet (Safety Valve) for hive-site.xml中
hive.cli.print.current.db
true

hive.cli.print.header
true

```

## linux用户与HDFS用户打通
```shell
# admin用户在hdfs上创建目录或者上传文件是没有权限的，
# 在HDFS上，hdfs是最大权限用户，supergroup是最大权限用户组。HDFS的权限是共用了Linux系统的权限。
#所以在Linux上创建supergroup 组，将admin用户添加superadmin附属组，并同步HDFS的权限即可



# 增加supergroup用户组
groupadd supergroup
# 将admin添加supergroup附属组
usermod -a -g admin -G supergroup admin
# 同步系统的信息到HDFS
su - hdfs -s /bin/bash -c "hdfs dfsadmin -refreshUserToGroupsMappings"
# admin用户增加sudo权限 需要root用户添加
echo 'admin  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
```