create table if not exists streaming_wc(
word varchar(255) primary key comment '单词'
,cnt bigint comment '计数'
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流处理单词计数表';

create table if not exists streaming_offset_stored(
topic varchar(255) comment 'Kafka topic'
,group_id varchar(255) comment 'Kafka Consumer Group Id'
,partition_id int comment 'partition id'
,offset bigint comment 'offset'
,primary key(topic,group_id,partition_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流处理offset存储表';