#!/bin/bash

#        sh standard.sh db table where
#---------------------------------------------
#FileName:		standard.sh
#Version:		1.0
#Date:			2021-01-04
#Author:		tianyafu
#Description:		example of shell script
#Notes:			脚本迭代日记
#---------------------------------------------

# 防止使用为定义过的变量
set -u

USAGE="Usage : $0 db table where"
[ $# -ne 3 ] && echo "$USAGE"  && exit 1

#start
source /root/shell/mysqlconn.sh
echo "$URL"
 
echo "hello world"

#end 


exit 0
