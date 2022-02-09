-- 查询表的数据字典的元数据信息
SELECT
a.TBL_NAME
,e.PARAM_VALUE
,b.COLUMN_NAME
,b.TYPE_NAME
,b.`COMMENT`
,b.INTEGER_IDX
FROM
tbls a
LEFT JOIN columns_v2 b ON a.TBL_ID = b.CD_ID
left join dbs c on a.DB_ID = c.DB_ID
left join (select TBL_ID,PARAM_VALUE from table_params where PARAM_KEY = 'comment') e on a.TBL_ID = e.TBL_ID
WHERE
c.`NAME` = 'ywjkq_dw'
and a.TBL_NAME = 'dm_excellent_enterprise_honor_wall_yy_f'
ORDER BY
b.INTEGER_IDX;

-- 查询表的元数据信息
SELECT
a.TBL_ID '表id'
,a.TBL_NAME '表名'
,g.PARAM_VALUE '表注释'
,j.COLUMN_NAME '字段名'
,j.TYPE_NAME '字段类型'
,j.`COMMENT` '字段注释'
,j.INTEGER_IDX '字段顺序'
,k.partition_key_name '表的分区键字段'
,a.TBL_TYPE '表的类型 内部表/外部表'
,a.CREATE_TIME '表创建时间'
,a.DB_ID '库id'
,b.`NAME` '库名'
,b.DB_LOCATION_URI '库的HDFS路径'
,b.OWNER_NAME '数据库所有者用户名'
,b.OWNER_TYPE '库的所有者角色'
,a.`OWNER` '表的所有者用户名'
,a.OWNER_TYPE '表的所有者角色'
,a.SD_ID '表的存储相关元数据对应的表的外键id'
,c.INPUT_FORMAT '读取表的inputFormat'
,c.IS_COMPRESSED '是否压缩 0否'
,c.LOCATION '表的HDFS路径'
,c.OUTPUT_FORMAT '写出表数据的outputFormat'
,c.SERDE_ID '表的序列化反序列化信息对应的表的外键id'
,d.SLIB '表序列化反序列化的类名'
,e.PARAM_VALUE '表的分隔符(表的两个列字段之间的文件中的字段分隔符)'
,f.PARAM_VALUE '文件序列化时表中两个列字段之间的文件中的字段分隔符'
,h.PARAM_VALUE '最后一次表的DDL改动时间'
FROM
tbls a
left JOIN
dbs b
on a.DB_ID = b.DB_ID
left JOIN
sds c
on a.SD_ID = c.SD_ID
LEFT JOIN
serdes d
on c.SERDE_ID = d.SERDE_ID
left JOIN
(select SERDE_ID,PARAM_KEY,PARAM_VALUE from serde_params where PARAM_KEY = 'field.delim') e
on c.SERDE_ID = e.SERDE_ID
left JOIN
(select SERDE_ID,PARAM_KEY,PARAM_VALUE from serde_params where PARAM_KEY = 'serialization.format') f
on c.SERDE_ID = f.SERDE_ID
left join
(select TBL_ID,PARAM_KEY,PARAM_VALUE from table_params where PARAM_KEY = 'comment' ) g
on a.TBL_ID = g.TBL_ID
LEFT JOIN
(select TBL_ID,PARAM_KEY,PARAM_VALUE from table_params where PARAM_KEY = 'transient_lastDdlTime') h
on a.TBL_ID = h.TBL_ID
left JOIN
columns_v2 j
on a.TBL_ID = j.CD_ID
left JOIN
(select TBL_ID,group_concat(PKEY_NAME) as partition_key_name from partition_keys group by TBL_ID) k
on a.TBL_ID = k.TBL_ID
WHERE
a.TBL_NAME = 'xxx'     -- 表名
and b.b.`NAME` = 'xxx' -- 库名
order by j.INTEGER_IDX
;

-- 查询分区信息
SELECT
a.TBL_NAME '表名'
,b.PKEY_NAME '分区键名称'
,b.PKEY_TYPE '分区键类型'
,b.PKEY_COMMENT '分区键注释'
,b.INTEGER_IDX '分区键序号'
FROM
tbls a
left JOIN
partition_keys b
on a.TBL_ID = b.TBL_ID
left JOIN
dbs c
on a.DB_ID = c.DB_ID
where
a.TBL_NAME = 'XXX'  -- 表名
and
c.`NAME` = 'xxxx'   -- 库名
order by b.INTEGER_IDX
;

-- 查看表有哪些分区
SELECT
a.TBL_NAME '表名'
,b.PART_ID '分区id'
,b.PART_NAME '分区名称'
,b.CREATE_TIME '分区创建时间'
FROM
tbls a
left JOIN
partitions b
on a.TBL_ID = b.TBL_ID
left JOIN
dbs c
on a.DB_ID = c.DB_ID
where
a.TBL_NAME = 'XXX'
and
c.`NAME` = 'xxxx'
order by b.CREATE_TIME
;
