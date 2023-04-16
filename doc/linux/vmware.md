# VmWare

## 虚拟机网络不可达
```shell
https://blog.csdn.net/qq_18769269/article/details/85200704
https://www.linuxprobe.com/linux-route-check.html
# 本次是使用了以下命令解决的问题
[root@tianyafu ~]# route -n
[root@tianyafu ~]# route add -net 0.0.0.0 netmask 0.0.0.0 gateway 192.168.26.2 dev eno16777736

```