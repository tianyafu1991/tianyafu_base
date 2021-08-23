# Jrebel部署

```
[admin@web ~]$ cd ~/software/ && unzip jrebel-2021.2.2-nosetup.zip

[admin@web software]$ mv ~/software/jrebel ~/app/

[admin@web software]$ cd ~/app/jrebel/bin

######### 这个激活地址参考简书：https://www.jianshu.com/p/704b1164a1c1
[admin@web bin]$ sh activate.sh http://jrebel.cicoding.cn/B791863C-10D6-76FE-F808-4E773DB00409 root@qq.com
JRebel successfully activated!
License type:  License server
Licensee name: admin
[admin@web bin]$ 
[admin@web bin]$ cd ~/app/jrebel/
######### 设置密码
[admin@web jrebel]$ java -jar jrebel.jar -set-remote-password 123456789
SUCCESS: Remote server password set to '123456789'
[admin@web jrebel]$ cd

[admin@web ~]$ nohup java "-agentpath:/home/admin/app/jrebel/lib/libjrebel64.so" -Drebel.remoting_plugin=true -jar -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=5006,suspend=n /home/admin/lib/yw-interface-data-collect-2.0.jar > ~/log/yw_log.out 2>&1 & 
```