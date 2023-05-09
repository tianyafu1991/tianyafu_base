# GIT常用操作


## git 关联到远程仓库
```
git init

git add .

git commit -m "这里填写本地修改的注释内容"

git remote add origin [这里是git仓库地址]

git pull origin master --allow-unrelated-histories

git branch --set-upstream-to=origin/master master

git push -u origin master -f
    
```

## git 去除不需要提交的文件目录
```
git rm -r --cached .idea
rm -r .idea
git add .
git status
git commit -m 'other:remove .idea'
git push
```



## Tag相关操作


### 打轻量Tag
```
git tag 标签名
or 
git tag 标签名 提交版本
```


### 打附注Tag
```
# 打一个附注标签
git tag -a v20230424 -m '20230424上线'

-a : 理解为 annotated 的首字符，表示 附注标签
-m : 指定附注信息

git tag -a 标签名称 提交版本号 -m 附注信息 ：给指定的提交版本创建一个【附注标签】
```

### 提交Tag
```
# 推送到远程仓库(将所有不在远程仓库中的标签上传到远程仓库)
git push origin --tags
# 推送到远程仓库(将指定的标签上传到远程仓库)
git push origin 标签名称

```

### 查看Tag
```
git tag
or 
git tag -l [标签名称筛选字符串*] 或者 git tag --list [标签名称筛选字符串*]
```

### 查看Tag的提交信息
```
git show 标签名称
```


### 删除Tag
```
# 删除指定名称的标签
git tag -d 标签名称
```

### 删除远程Tag
```
git push origin  :regs/tags/标签名称
or
git push origin --delete 标签名称

```

### 检出标签
```
git checkout -b 分支名称 标签名称
```