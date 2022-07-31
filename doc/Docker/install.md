# Docker部署

## 参考文档
```
https://docs.docker.com/engine/install/centos/
```

## 部署
```shell
# Uninstall old versions
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine


# Set up the repository
[hadoop@hadoop01 ~]$ sudo yum install -y yum-utils
sudo yum-config-manager \
    --add-repo \
	https://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# Install Docker Engine
[hadoop@hadoop01 ~]$ sudo yum install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start Docker
[hadoop@hadoop01 ~]$ sudo systemctl start docker

# optional setup start Docker when system start
[hadoop@hadoop01 ~]$ sudo systemctl enable docker

# cat docker version
[hadoop@hadoop01 ~]$ docker version

# Verify that Docker Engine is installed correctly by running the hello-world image.
[hadoop@hadoop01 ~]$ sudo docker run hello-world

```