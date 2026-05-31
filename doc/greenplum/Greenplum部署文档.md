# Greenplum部署文档

## 基础配置

### 网络配置
```shell
# 查看是否需要做网络上的配置 生产的机器可能是不需要的 比如业主提供的机器 可能已经做好了这部分的配置
[root@mdw ~]# vim /etc/sysconfig/network-scripts/ifcfg-eno16777736
BOOTPROTO="static"
ONBOOT="yes"
IPADDR=192.168.198.128
NETMASK=225.225.225.0
GATEWAY=192.168.198.2
DNS1=114.114.114.114

[root@mdw ~]# systemctl restart network
[root@mdw ~]# ping www.baidu.com
PING www.baidu.com (223.109.82.212) 56(84) bytes of data.
64 bytes from 223.109.82.212 (223.109.82.212): icmp_seq=1 ttl=128 time=21.6 ms

```

### 安装yum源
```shell
# 安装yum源 这一步要看生产服务器的情况 可能业主提供的服务器已经配置好了yum源 不需要我们自行配置
[root@mdw ~]# yum install -y epel-release
# 这一步一定要执行 常用的一些命令 
[root@mdw ~]# yum install -y vim net-tools psmisc  nc  rsync  lrzsz  ntp libzstd openssl-static tree git lsof wget tmux ntpdate zip unzip telent dos2unix traceroute nmap htop iotop iftop
# 这个是安装GP数据库需要的一些依赖
[root@mdw ~]# yum install -y apr apr-util bash bzip2 curl krb5 libcurl libevent libxml2 libyaml zlib openldap openssh-client openssl openssl-libs perl readline rsync R sed tar zip krb5-devel
```

### 关闭防火墙
```shell
[root@mdw ~]# systemctl stop firewalld
# 是否需要禁用防火墙 这一步可以不做 生产环境中GP集群的防火墙是开启的 只是在部署的时候 把防火墙暂时关闭了
[root@mdw ~]# systemctl disable firewalld
```

### 卸载虚拟机自带的Jdk
```shell
[root@mdw ~]# rpm -qa | grep -i java | xargs -n1 rpm -e --nodeps 
rpm：未给出要擦除的软件包
```

### 修改hostname
```shell
# 当需要改服务器的hostname时 按照这个命令修改
[root@mdw ~]# hostnamectl set-hostname mdw
[root@mdw ~]# hostname
mdw
```

### 配置hosts文件
```shell
[root@mdw ~]# cat <<EOF>> /etc/hosts
> 192.168.1.128 mdw
> 192.168.1.129 sdw1
> 192.168.1.130 sdw2
> EOF
```

### 永久关闭SELinux
```shell
[root@mdw ~]# /bin/sed -i 's@SELINUX=enforcing@SELINUX=disabled@g' /etc/selinux/config 
[root@mdw ~]# /bin/grep SELINUX=disabled /etc/selinux/config
SELINUX=disabled
```

### 配置内核参数
```shell
# 配置
[root@mdw ~]# cat >>/etc/sysctl.conf<<EOF
# 这个参数是单个共享内存段的最大大小（单位：字节） 可以用echo $(expr $(getconf _PHYS_PAGES) / 2 \* $(getconf PAGE_SIZE))命令获取到
kernel.shmmax = 500000000
# 系统范围内共享内存段的最大数量，默认4096
kernel.shmmni = 4096
# 这个参数是系统范围内所有共享内存段的总大小上限（单位：页） 可以用echo $(expr $(getconf _PHYS_PAGES) / 2)命令获取到
kernel.shmall = 4000000000
# 信号量设置（包含4个数值，分别代表：每个信号量集的最大信号量数、系统范围内信号量的最大值、每个信号量操作的最大操作数、系统范围内信号量集的最大数量）。
kernel.sem = 500 2048000 200 40960
# 设为 1 表示启用 SysRq 键（魔术键），允许管理员在系统严重卡顿或崩溃时，通过特定的键盘组合执行底层命令（如安全重启、强制同步磁盘等）。
kernel.sysrq = 1
# 设为 1 表示在生成的 core dump（程序崩溃时的内存转储）文件名中追加进程 PID，防止多个进程崩溃时文件被覆盖，便于排查问题。
kernel.core_uses_pid = 1
# 单个消息队列的最大字节数。
kernel.msgmnb = 65536
# 单条消息的最大字节数。
kernel.msgmax = 65536
# 消息队列标识符的最大数量。
kernel.msgmni = 2048
# 设为 1 表示开启 SYN Cookies，能有效防范 SYN Flood 这种常见的 DDoS 攻击。
net.ipv4.tcp_syncookies = 1
# 设为 0 表示禁止 IP 转发（即不作为路由器使用）；如果设为 1 则允许转发。
net.ipv4.ip_forward = 0
# 设为 0 表示不接受源路由数据包，这是一种安全加固措施，防止黑客利用源路由进行 IP 欺骗。
net.ipv4.conf.default.accept_source_route = 0
# 开启 TCP 连接中 TIME-WAIT 套接字的快速回收
net.ipv4.tcp_tw_recycle = 1
# 记录那些尚未收到客户端确认信息的连接请求的最大值，调大该值可以应对高并发下的连接请求。
net.ipv4.tcp_max_syn_backlog = 4096
# 设为 1 表示开启 ARP 过滤，用于控制网卡对 ARP 请求的响应行为，常用于多网卡或负载均衡环境。
net.ipv4.conf.all.arp_filter = 1
# 设定本地端口可使用的范围（例如 10000 65535），扩大范围有助于支撑更多的对外并发连接。
net.ipv4.ip_local_port_range = 1025 65535
# 当网卡接收数据包的速度快于内核处理速度时，允许发送到队列的数据包最大数量。
net.core.netdev_max_backlog = 10000
# 以下2个参数分别为 TCP socket 接收和发送缓冲区的最大大小，调大有助于提升高带宽环境下的网络传输性能。
net.core.rmem_max = 2097152
net.core.wmem_max = 2097152
# 可以为进程分配内存的百分比，其余部分留给操作系统。默认值为50。建议设置95
vm.overcommit_ratio=95
# 内存分配策略。设为 2 表示禁止超额使用内存（即申请的虚拟内存不能超过 Swap + 物理内存 * overcommit_ratio），这能有效防止 OOM（内存溢出）导致重要进程被杀，常见于数据库服务器。
vm.overcommit_memory = 2
# 控制系统使用 Swap（交换分区）的倾向。10 表示只有在物理内存非常紧张时才使用 Swap，这有利于保持高性能应用（如数据库）的响应速度。
vm.swappiness=10
# 当本地内存不足时，内核会直接去其他 NUMA 节点（远程内存）分配内存。这是绝大多数场景下的推荐设置
vm.zone_reclaim_mode=0
# 脏页数据在内存中允许存活的最长时间。单位是厘秒（1/100秒）。任何在内存里待了超过 5 秒还没写入硬盘的脏数据，都会被内核的后台刷盘线程强制写进去。
vm.dirty_expire_centisecs=500
# 内核后台刷盘线程（pdflush/kcompactd）被唤醒的周期。单位是厘秒。系统每隔 1 秒就会醒来一次，检查有没有需要刷入硬盘的脏数据。
vm.dirty_writeback_centisecs=100
# 以下2个参数是 触发后台异步刷盘的阈值 Linux 允许通过百分比（ratio）或绝对字节数（bytes）来设定这个阈值，但两者互斥，且 bytes 的优先级更高。
# 当系统中积压的脏数据达到 1.5 GB 时，内核会在后台静默地启动刷盘操作。这个过程是异步的，不会阻塞你的应用程序，应用可以继续写入新数据。
vm.dirty_background_ratio=0
vm.dirty_background_bytes=1610612736
# 以下2个参数是 触发前台同步强制刷盘的阈值。
# 这是脏页的“红线”。当脏数据积压达到 4 GB 时，系统会认为内存压力过大，此时任何新的写入操作都会被强制阻塞（卡住），应用程序必须停下来等待内核把脏数据刷入硬盘，直到脏页比例降下来。这通常会导致应用出现明显的 I/O 延迟或卡顿。
vm.dirty_ratio=0
vm.dirty_bytes=4294967296
# 系统强制保留的最小空闲物理内存量，单位是 KB。这是系统的“保命内存”。内核会极力保证系统时刻有至少 960 MB 的空闲内存，用于应对紧急情况
vm.min_free_kbytes=983355
EOF
```

### 配置文件限制
```shell
[root@mdw ~]# cat >>/etc/security/limits.conf<<EOF
* soft nofile 65536
* hard nofile 65536
* soft nproc 131072
* hard nproc 131072
*       -       core    unlimited
*       -       nproc   655360
*       -       nofile  655360
*       -       memlock unlimited
gpadmin -       nofile  524288
gpadmin -       nproc   131072
EOF

[root@mdw ~]# cat >>/etc/security/limits.d/20-nproc.conf<<EOF
* soft nofile 65536
* hard nofile 65536
* soft nproc 131072
* hard nproc 131072
*       -       core    unlimited
*       -       nproc   655360
*       -       nofile  655360
*       -       memlock unlimited
gpadmin -       nofile  524288
gpadmin -       nproc   131072
EOF
```

### 配置ssh连接阈值
```shell
[root@mdw ~]# cat >>/etc/ssh/sshd_config<<EOF
MaxSessions 200
MaxStartups 100:30:1000
EOF
```

### 配置字符集
```shell
[root@mdw ~]# echo $LANG
zh_CN.UTF-8
[root@mdw ~]# localectl set-locale LANG=en_US.UTF-8
# 退出当前session再重新进入后 变为英文的UTF-8
[root@mdw ~]# echo $LANG
en_US.UTF-8
```

### 时钟同步
```shell
[root@mdw ~]# ntpdate cn.pool.ntp.org
```

## Greenplum部署

### 创建用户组和用户
```shell
[root@mdw ~]# groupadd gpadmin
[root@mdw ~]# useradd gpadmin -r -m -g gpadmin
# 配置gpadmin用户的密码
[root@mdw ~]# passwd gpadmin
# 查看gpadmin用户的信息
[root@mdw ~]# id gpadmin
uid=997(gpadmin) gid=1000(gpadmin) groups=1000(gpadmin)
```
### 配置sudo权限
```shell
# 这个文件需要wq! 强制保存退出 因为这个文件是只读文件
[root@mdw ~]# vim /etc/sudoers
gpadmin ALL=(ALL)       NOPASSWD:ALL
```

### 配置免密信任关系
```shell
# 按照我个人的习惯 root用户之间需要做信任关系配置 而gpadmin用户之间也要做信任关系配置 以下只展示gpadmin用户的信任关系配置操作 这里做信任关系配置主要是为了后续GP的rpm安装包可以互传
# 统一切换到gpadmin用户
[gpadmin@mdw ~]$ ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
# 把其它几台机器的公钥 统一发送到mdw机器上
[gpadmin@sdw1 ~]$ scp ~/.ssh/id_rsa.pub  mdw:~/.ssh/id_rsa.pub2
[gpadmin@sdw2 ~]$ scp ~/.ssh/id_rsa.pub  mdw:~/.ssh/id_rsa.pub3
# mdw机器上 公钥统一写入到authorized_keys文件
[gpadmin@mdw ~]$ cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
[gpadmin@mdw ~]$ cat ~/.ssh/id_rsa.pub2 >> ~/.ssh/authorized_keys
[gpadmin@mdw ~]$ cat ~/.ssh/id_rsa.pub3 >> ~/.ssh/authorized_keys
# mdw机器上 把~/.ssh/authorized_keys文件发送给其它机器
[gpadmin@mdw ~]$ scp ~/.ssh/authorized_keys sdw1:~/.ssh/
[gpadmin@mdw ~]$ scp ~/.ssh/authorized_keys sdw2:~/.ssh/
# 对各个节点上的~/.ssh/authorized_keys设置权限
[gpadmin@mdw ~]$ chmod 0600 ~/.ssh/authorized_keys
# 验证
[gpadmin@mdw ~]$ ssh mdw date
[gpadmin@mdw ~]$ ssh sdw1 date
[gpadmin@mdw ~]$ ssh sdw2 date
```

### 配置一些部署过程中需要用到的文件
```shell

```

### 上传rpm安装包
```shell
# 把rpm安装包上传到/home/gpadmin/software这个目录
[gpadmin@mdw ~]$ mkdir -p /home/gpadmin/software
```

### 正式开始安装
```shell
[gpadmin@mdw software]$ sudo yum -y install /home/gpadmin/software/open-source-greenplum-db-6.25.3-rhel7-x86_64.rpm
[gpadmin@mdw software]$ cd /usr/local/greenplum-db
# 每台机器的/usr/local/greenplum-db* 的所属用户和所属用户组 都改成gpadmin
[gpadmin@mdw greenplum-db]$ sudo chown -R gpadmin:gpadmin /usr/local/greenplum-db*

# 拷贝一些部署过程中需要用到的文件到统一的目录
# 只在mdw机器上配置
[gpadmin@mdw ~]$ mkdir -p /home/gpadmin/conf
# 这个是GP集群中的所有host 通常在检查集群通信 及其它检查时要用到
[gpadmin@mdw ~]$ cp /usr/local/greenplum-db/docs/cli_help/gpconfigs/hostfile_gpssh_allhosts /home/gpadmin/conf/ && vim /home/gpadmin/conf/hostfile_gpssh_allhosts
mdw
sdw1
sdw2

# 这个是初始化数据库的核心配置文件 先拷贝过来 后续改
[gpadmin@mdw ~]$ cp /usr/local/greenplum-db/docs/cli_help/gpconfigs/gpinitsystem_config /home/gpadmin/conf/
# 这个是初始化数据库要用到的数据节点的host  这里放了3个hostname 意味着这3个hostname都是数据节点 其中mdw还作为master所在节点 有些生产环境中机器比较富裕 master节点不作为数据节点 则这个文件中把master的host去掉就行
[gpadmin@mdw ~]$ cp /usr/local/greenplum-db/docs/cli_help/gpconfigs/hostfile_gpinitsystem /home/gpadmin/conf/  && vim /home/gpadmin/conf/hostfile_gpinitsystem
mdw
sdw1
sdw2

# 创建master的数据目录 这里创建的目录的所属用户是root 暂时不修改所属用户和所属用户组 因为这个master的目录只需要在master节点上 所以提前创建好 而不是在后续通过gpssh命令批量执行 那样每个节点上都会有master的目录
[gpadmin@mdw greenplum-db]$ sudo mkdir -p /data/gp/master/
# 在这个文件中增加MASTER_DATA_DIRECTORY和LD_PRELOAD变量
[gpadmin@mdw greenplum-db]$ vim /usr/local/greenplum-db/greenplum_path.sh
export MASTER_DATA_DIRECTORY=/data/gp/master/gpseg-1
export LD_PRELOAD=/lib64/libz.so.1 ps

# 因在mdw机器上 通常是由root用户通过su - gpadmin的命令切换到gpadmin的 以后操作时 都需要source /usr/local/greenplum-db/greenplum_path.sh 用来生效一些环境变量 所以直接把这个命令放在~/.bashrc文件中 
[gpadmin@mdw greenplum-db]$ cat >>~/.bashrc<<EOF
source /usr/local/greenplum-db/greenplum_path.sh
EOF
[gpadmin@mdw greenplum-db]$ source ~/.bashrc 
# 测试各台机器之间通不通 gpssh-exkeys这个命令本身就是用来批量配置信任关系 而我提前做了信任关系 所以这里执行起来很快 仅仅起到了检测通不通的效果
[gpadmin@mdw greenplum-db]$ gpssh-exkeys -f /home/gpadmin/conf/hostfile_gpssh_allhosts 
[gpadmin@mdw greenplum-db]$ cd
[gpadmin@mdw ~]$  gpssh -f /home/gpadmin/conf/hostfile_gpssh_allhosts
=> sudo mkdir -p /data/gp/primary/
[sdw1]
[sdw2]
[ mdw]
=> sudo mkdir -p /data/gp/mirror/
[sdw1]
[sdw2]
[ mdw]
=> sudo chown -R gpadmin:gpadmin /data/gp
[sdw1]
[sdw2]
[ mdw]
=> exit

# 通过gpcheckperf做硬件基准性能测试 在安装 Greenplum 之前或日常运维中，对服务器的磁盘 I/O、内存带宽以及网络性能进行全面的体检和基准测试，帮助你发现潜在的硬件瓶颈或配置问题。
[gpadmin@mdw ~]$ gpcheckperf -f /home/gpadmin/conf/hostfile_gpssh_allhosts -r N -d /tmp
[INFO] --buffer-size value is not specified or invalid. Using default (32 kilobytes)
/usr/local/greenplum-db-6.25.3/bin/gpcheckperf -f /home/gpadmin/conf/hostfile_gpssh_allhosts -r N -d /tmp

-------------------
--  NETPERF TEST
-------------------

====================
==  RESULT 2026-05-31T14:56:12.720487
====================
Netperf bisection bandwidth test
mdw -> sdw1 = 250.740000
sdw2 -> mdw = 141.950000
sdw1 -> mdw = 148.790000
mdw -> sdw2 = 249.690000

Summary:
sum = 791.17 MB/sec
min = 141.95 MB/sec
max = 250.74 MB/sec
avg = 197.79 MB/sec
median = 249.69 MB/sec

[Warning] connection between sdw2 and mdw is no good
[Warning] connection between sdw1 and mdw is no good
```

### 配置核心配置文件
```shell
# 修改核心配置文件 检查以下几个参数是否要改动 这里的配置文件的意思是mdw是master节点 master的目录在/data/gp/master 而每个数据节点上起2个postgresql实例 对应的数据目录是/data/gp/primary
[gpadmin@mdw ~]$ vim /home/gpadmin/conf/gpinitsystem_config
declare -a DATA_DIRECTORY=(/data/gp/primary /data/gp/primary)
MASTER_HOSTNAME=mdw
MASTER_DIRECTORY=/data/gp/master
declare -a MIRROR_DATA_DIRECTORY=(/data/gp/mirror /data/gp/mirror)

# 开始初始化数据库
[gpadmin@mdw ~]$ gpinitsystem -c /home/gpadmin/conf/gpinitsystem_config -h /home/gpadmin/conf/hostfile_gpinitsystem
```

## 部署完之后的后续操作

### 创建生产要用的用户
```shell

## 分别是创建用户 创建库 以及 赋予用户操作库的权限
[gpadmin@mdw master]$ psql postgres
psql (9.4.26)
Type "help" for help.

postgres=# create user admin superuser password 'rkpkg5kg3z9bsn8h';
CREATE ROLE
postgres=# create database my_test;
CREATE DATABASE
postgres=# GRANT ALL PRIVILEGES ON DATABASE my_test TO admin;
GRANT
postgres=# \q

```

### 授予用户远程登录
```shell
[gpadmin@mdw ~]$ echo 'host    all  admin   0.0.0.0/0  md5' >> /data/gp/master/gpseg-1/pg_hba.conf
[gpadmin@mdw ~]$ gpstop -u
```
