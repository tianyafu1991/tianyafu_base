[hadoop@hadoop01 ~]$ cd ~/software/
[hadoop@hadoop01 software]$ wget https://codeload.github.com/smartloli/kafka-eagle-bin/tar.gz/v2.0.8
[hadoop@hadoop01 software]$ mv v2.0.8 kafka-eagle-bin-2.0.8.tar.gz
[hadoop@hadoop01 software]$ tar -xvf kafka-eagle-bin-2.0.8.tar.gz
[hadoop@hadoop01 software]$ tar -zxvf kafka-eagle-bin-2.0.8/efak-web-2.0.8-bin.tar.gz -C ~/app
[hadoop@hadoop01 software]$ cd ~/app/
# 这里解压出来的是一个web的tar.gz包
[hadoop@hadoop01 app]$ ln -s efak-web-2.0.8 kafka-eagle

[hadoop@hadoop01 kafka-eagle]$ echo -e '# KAFKA-EAGLE ENV\nexport KE_HOME=/home/hadoop/app/kafka-eagle\nexport PATH=$KE_HOME/bin:$PATH' >> ~/.bashrc
[hadoop@hadoop01 kafka-eagle]$ source ~/.bashrc 

[hadoop@hadoop01 kafka-eagle]$ cd ${KE_HOME}/conf
[hadoop@hadoop01 conf]$ cp system-config.properties system-config.properties.bak
[hadoop@hadoop01 conf]$ vi system-config.properties



# 启动
[hadoop@hadoop01 conf]$ cd ${KE_HOME}/bin
[hadoop@hadoop01 bin]$ chmod +x ke.sh 
[hadoop@hadoop01 bin]$ ./ke.sh start

