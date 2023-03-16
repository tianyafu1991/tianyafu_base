#!/bin/bash


function getLastMonth() {
    echo "参数总数有 $# 个!!!"
    if [ $# != 2 ] ; then
      echo "USAGE: getLastMonth date format"
      exit 1
    fi
    # 获取函数的入参
    START_DATE=$1
    FORMAT=$2
    echo "函数入参为 $1 "
    FIRST_DAY_THIS_MONTH=${START_DATE:0:7}"-01"
    echo ${FIRST_DAY_THIS_MONTH}
    LAST_MONTH=`date -d "1 month ago $FIRST_DAY_THIS_MONTH"  +${FORMAT}`
    echo "LAST_MONTH: ${LAST_MONTH}"
    return LAST_MONTH
}



#CURRENT_YEAR=`date +%Y`
#CURRENT_MONTH=`date +%m`
#
#echo ${current_year},${current_month}

#getLastMonth `date +%Y-%m-%d`
getLastMonth $1 $2
echo $?