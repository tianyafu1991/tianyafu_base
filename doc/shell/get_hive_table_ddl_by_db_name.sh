#! /bin/bash



DB=dw

RESULT_SQL_FILE=ddl_hive_db_tables.sql
HIVE_TABLES_FILE=hive_db_tables.txt

rm -f ${RESULT_SQL_FILE}
touch ${RESULT_SQL_FILE}

hive -e "use ${DB};
    show tables;
" > ${HIVE_TABLES_FILE}
sleep 1
cat ${HIVE_TABLES_FILE} | while read eachline
do
  if [ ${eachline} = tab_name ]
  then
    echo "the first line named tab_name is the column name "
    continue
  fi
  echo "开始处理"${eachline}
  hive -e "use ${DB}; show create table ${DB}.${eachline};" >> ${RESULT_SQL_FILE}
  echo -e ';\n--------\n' >> ${RESULT_SQL_FILE}
  echo ${eachline}"处理结束"
done

sed -i "s#createtab_stmt#--------#g" ${RESULT_SQL_FILE}