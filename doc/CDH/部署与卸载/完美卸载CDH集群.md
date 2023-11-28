# 完美卸载CDH集群

## 卸载前的规划
```shell
1.关闭集群及MySQL服务
2.删除部署文件夹/opt/cloudera*
3.删除数据文件夹

```

## 卸载前的准备
```shell
0.整理集群中各个组件的存储目录如:HDFS YARN ZK
1.在cm的web ui上找到对应组件的存储目录
NameNode:/dfs/nn   页面上搜索dfs.name.dir,dfs.namenode.name.dir
DataNode:/dfs/dn   页面上搜索dfs.data.dir,dfs.datanode.data.dir
SecondNameNode:/dfs/snn    页面上搜索fs.checkpoint.dir,dfs.namenode.checkpoint.dir

NodeManager:/yarn/nm      页面上搜索yarn.nodemanager.local-dirs
Zookeeper:/var/lib/zookeeper          页面上搜索dataDir
```

## 关闭集群
```shell
1.cm web ui 上先关闭cluster 再关闭 Cloudera Managerment Service
2.各个节点关闭agent
3.关闭cm server
4.关闭mysql

5.杀进程
各个agent节点都要杀cloudera相关的进程
通过 ps -ef | grep cloudera 可以查看是否存在着残留的进程
执行命令kill -9 $(pgrep -f cloudera)  执行1次可能杀不干净 要执行2次
通过pgrep -f cloudera校验是否杀干净
因为agent会有一个supervisord进程来守护NameNode NodeManager等进程防止被误杀 
关闭cm的agent后 supervisord进程可能残留 所以要杀掉


6.df -h 查看是否有cloudera相关的盘挂载在上面  如果有 要卸载该盘 通过umount卸载
假如无法卸载 夯住了 就强制kill
先yum安装lsof命令  yum install -y lsof
kill -9 $(lsof /opt/cloudera-manager/cm-4.16.1/run/cloudera-scm-agent/process | awk '{print $2}')
再通过df -h校验是否卸载掉

7.删除cloudera部署文件夹
rm -rf /opt/cloudera*

rm -rf /dfs
rm -rf /yarn
rm -rf /var/lib/zookeeper

rm -rf /usr/share/cmf
rm -rf /var/lib/cloudera*
rm -rf /var/log/cloudera* 这个是日志 可删可不删
rm -rf /run/cloudera-scm-agent
rm -rf /etc/security/limits.d/cloudera-scm.conf
rm -rf /etc/hadoop* /etc/zookeeper /etc/hive* /etc/hbase* /etc/spark /etc/impala
rm -rf /tmp/scm_*
rm -rf /tmp/.scm*
rm -rf /usr/lib64/cmf

8.全局搜索是否删干净了
find / -name '*cloudera*'
找出对应的目录  看看是否有删除的必要 如有必要 通过下面的命令删掉
find / -name '*cloudera*' | while read line; do rm -rf ${line}; done

9.启动MySQL 删除里面的amon cmf等库及对应的用户
drop database amon;
drop database cmf;

drop user cmf;
drop user amon;

# 校验库是否删除
show databases;

# 校验用户是否删除
use mysql;
select user from user; 


10.维护组件多版本动态管理
cd /etc/alternatives
这个目录下面有一堆的软连接 因集群卸载 下面有一堆的软连接是报红的
可参考博客:http://blog.itpub.net/30089851/viewspace-2128683/

如果是要删除干净 用如下命令
[root@hadoop001 alternatives]# cd /etc/alternatives
[root@hadoop001 alternatives]# ll . | grep  cloudera | awk '{print $9}' | while read line; do rm -rf ${line}; done 

```