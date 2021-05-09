CREATE TABLE `access_province_cnt` (
`time` varchar(20) NOT NULL COMMENT '时间',
`province` varchar(20) NOT NULL COMMENT '省份',
`cnt` bigint(20) DEFAULT NULL COMMENT '次数',
PRIMARY KEY (`time`,`province`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问ip的省份的统计次数表';