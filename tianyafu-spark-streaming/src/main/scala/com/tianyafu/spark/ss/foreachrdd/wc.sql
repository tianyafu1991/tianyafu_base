drop table if exists wc;
create table wc(
word varchar(255) comment '单词'
,cnt bigint comment '个数'
) engine=innodb charset utf8mb4 comment '单词计数表';