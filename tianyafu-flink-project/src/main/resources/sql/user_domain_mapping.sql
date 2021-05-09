create table user_domain_mapping(
    user_id varchar(50) comment '用户id',
    domain varchar(50) comment '域名'
) engine=innodb comment '用户域名映射表';

insert into user_domain_mapping values ('00000001','ruozedata.com'),('00000001','ruoze.ke.qq.com'),('00000002','google.com'),('00000003','twitter.com');




