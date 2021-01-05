#!/bin/bash -x

#basic config
EMAIL=2643854124@qq.com
JOB_NAME="SparkALS"
CDHUSER=admin
CDHPASSWORD=admin
CDHURL=http://39.104.97.138:7180
JSON=/tmp/yarnApplications_${JOB_NAME}.json
LOG=/tmp/applications.log

#check job is or not running?
curl -u $CDHUSER:$CDHPASSWORD $CDHURL/api/v16/clusters/RZCluster/services/yarn/yarnApplications > $JSON
cat $JSON | grep -A 4 $JOB_NAME > $LOG

RUNNINGNUM=`cat $LOG | grep "state" | grep "RUNNING"  | wc -l`

echo "The running $JOB_NAME job num is $RUNNINGNUM"

if [ $RUNNINGNUM -gt 0  ]
then

        echo -e "`date "+%Y-%m-%d %H:%M:%S"` : The current running $JOB_NAME job num is $RUNNINGNUM." | mail \
        -r "From: alertAdmin <${EMAIL}>" \
        -s "Warn: Skip the new $JOB_NAME spark job." ${EMAIL}
        exit 0
fi



#spark parameters
CLASS_NAME=org.apache.spark.examples.SparkPi
SPARK_HOME=/var/lib/hadoop-hdfs/datawarehouse/app/spark
JAR_PATH=${SPARK_HOME}/examples/jars/spark-examples_2.11-2.1.1.jar

#submit spark job
${SPARK_HOME}/bin/spark-submit \
--master yarn \
--class $CLASS_NAME \
$JAR_PATH

exit 0


