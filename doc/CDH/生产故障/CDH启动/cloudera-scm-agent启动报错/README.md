# /tmp/cloudera-scm-agent.lock: 只读文件系统
```shell
[root@bbj ~]# /opt/cm-5.11.1/etc/init.d/cloudera-scm-agent status
/opt/cm-5.11.1/etc/init.d/cloudera-scm-agent: line 39: /tmp/cloudera-scm-agent.lock: 只读文件系统
flock: 99: 错误的文件描述符
This script is being executed in another terminal. Exit.
flock: 99: 错误的文件描述符
flock: 99: 错误的文件描述符

[root@bbj ~]# ulimit
unlimited
[root@bbj ~]# df -Th
Filesystem           Type   Size  Used Avail Use% Mounted on
/dev/mapper/vg_ddz-lv_root
                     ext4   143G   62G   74G  46% /
tmpfs                tmpfs   48G  220K   48G   1% /dev/shm
/dev/sda1            ext4   477M   36M  416M   8% /boot
/dev/mapper/vg_ddz-lv_home
                     ext4    79G  293M   75G   1% /home
/dev/mapper/vg_ddz-lv_ddhome
                     ext4   1.6T  1.4T   86G  95% /ddhome
cm_processes         tmpfs   48G   32M   48G   1% /opt/cm-5.11.1/run/cloudera-scm-agent/process
tmpfs                tmpfs   60M     0   60M   0% /var/log/rtlog
[root@bbj ~]# ulimit -n
65535
[root@bbj ~]# find / -name cloudera-scm-agent.pid
/opt/cm-5.11.1/run/cloudera-scm-agent/cloudera-scm-agent.pid
```