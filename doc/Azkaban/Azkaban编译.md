# Azkaban编译文档

## 上传tar.gz包并解压
```shell
[admin@sdw2 sourcecode]$ pwd
/home/admin/sourcecode
[admin@sdw2 sourcecode]$ ll | grep azkaban-3.81.0.tar.gz 
-rw-r--r--  1 admin admin  19271556 Dec  6 13:35 azkaban-3.81.0.tar.gz
[admin@sdw2 sourcecode]$ tar -zxvf azkaban-3.81.0.tar.gz 
```

## 部署git 编译需要有git环境 如系统有 用系统自带的git就行
```shell
[admin@sdw2 ~]$ cd ~/sourcecode/
[admin@sdw2 sourcecode]$ tar -zxvf git-2.34.1.tar.gz
[admin@sdw2 sourcecode]$ sudo su -
[root@sdw2 ~]# yum -y install zlib-devel openssl-devel cpio expat-devel gettext-devel curl-devel perl-ExtUtils-CBuilder perl-ExtUtils-MakeMaker
[root@sdw2 ~]# yum remove -y git
[root@sdw2 ~]# exit
[admin@sdw2 sourcecode]$ cd ~/sourcecode/git-2.34.1
[admin@sdw2 git-2.34.1]$ sudo make prefix=/usr/local/git all
[admin@sdw2 git-2.34.1]$ sudo make prefix=/usr/local/git install
[admin@sdw2 git-2.34.1]$ sudo vim /etc/profile
# GIT ENV
export GIT_HOME=/usr/local/git
export PATH=${GIT_HOME}/bin:${PATH}
[admin@sdw2 git-2.34.1]$ source /etc/profile
[admin@sdw2 git-2.34.1]$ git --version

```

## 修改build.gradle中的仓库地址 改为阿里云的
```shell
[admin@sdw2 sourcecode]$ cd ~/sourcecode/azkaban-3.81.0
# build.gradle文件中有两处 repositories{} 两处都需要修改,删除原先的配置  改为 maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
[admin@sdw2 azkaban-3.81.0]$ vim build.gradle
buildscript {
  repositories {
    maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
  }
  dependencies {
    classpath 'com.cinnober.gradle:semver-git:2.2.3'
 classpath 'net.ltgt.gradle:gradle-errorprone-plugin:0.0.14'
 classpath 'com.github.jengelman.gradle.plugins:shadow:4.0.0'
 }
}


allprojects {
  apply plugin: 'jacoco'

 repositories {
    maven{ url 'http://maven.aliyun.com/nexus/content/groups/public/'}
  }
}
```

## 上传gradle包
```shell
[admin@sdw2 azkaban-3.81.0]$ vim gradle/wrapper/gradle-wrapper.properties
#distributionUrl=https\://services.gradle.org/distributions/gradle-4.6-all.zip
distributionUrl=gradle-4.6-all.zip
[admin@sdw2 azkaban-3.81.0]$ cp ~/software/gradle-4.6-all.zip ~/sourcecode/azkaban-3.81.0/gradle/wrapper/

```

## 编译
```shell
[admin@sdw2 azkaban-3.81.0]$ ./gradlew distTar
```
