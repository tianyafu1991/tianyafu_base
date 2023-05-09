# git

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

## git 重置url
```
获取远程url
git remote get-url  --all origin

git remote set-url  origin new_url

usage: git remote [-v | --verbose]
   or: git remote add [-t <branch>] [-m <master>] [-f] [--tags | --no-tags] [--mirror=<fetch|push>] <name> <url>
   or: git remote rename <old> <new>
   or: git remote remove <name>
   or: git remote set-head <name> (-a | --auto | -d | --delete | <branch>)
   or: git remote [-v | --verbose] show [-n] <name>
   or: git remote prune [-n | --dry-run] <name>
   or: git remote [-v | --verbose] update [-p | --prune] [(<group> | <remote>)...]
   or: git remote set-branches [--add] <name> <branch>...
   or: git remote get-url [--push] [--all] <name>
   or: git remote set-url [--push] <name> <newurl> [<oldurl>]
   or: git remote set-url --add <name> <newurl>
   or: git remote set-url --delete <name> <url>
```