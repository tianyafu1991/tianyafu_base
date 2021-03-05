# VNC部署

## 安装图形桌面

```bash
#安装图形用户接口X Window System
yum groupinstall "X Window System" -y
#安装GNOME
yum groupinstall "GNOME Desktop" -y
```



## 安装vnc

```bash
yum install tigervnc-server -y
```



## 将vnc-server启动在5904端口

该步需要输入客户端登录的密码（密码不得小于6位）：

```bash
vnc-server :4
```



## 检查开启的端口

```bash
ss -tunlp | grep X
```



## 备份配置文件

```bash
cp /lib/systemd/system/vncserver@.service ~/tmp/vncserver@.service.bak
```



## 编辑配置文件

```bash
vim /lib/systemd/system/vncserver@.service

添加下面两行配置

 VNCSERVERS="1:admin" // admin为当前用户

 VNCSERVERARGS[1]="-geometry 1024x768"  //此处是分辨率设置 根据自己的情况设定

vim ~/.vnc/xstartup
将最后一行改为
gnome &
表示使用GNOME桌面
```





## 开启VNC

```bash
vncserver :2 # :2前面一定要有空格。
```



## 关闭VNC

```bash
vncserver -kill :1 # :1前面有个空格
```

