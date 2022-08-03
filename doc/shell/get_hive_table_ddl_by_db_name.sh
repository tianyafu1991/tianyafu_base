#! /bin/bash



DB=dw

RESULT_SQL_FILE=ddl_hive_db_tables.sql

rm -f ${RESULT_SQL_FILE}
touch ${RESULT_SQL_FILE}

hive -e "use ${DB};
    show tables;
" > hive_db_tables.txt
sleep 1
cat hive_db_tables.txt | while read eachline
do
hive -e "use ${DB}; show create table ${DB}.${eachline};" >> ${RESULT_SQL_FILE}
echo -e ';\n--------' >> ${RESULT_SQL_FILE}
done

sed -i "s#createtab_stmt#--------#g" ${RESULT_SQL_FILE}