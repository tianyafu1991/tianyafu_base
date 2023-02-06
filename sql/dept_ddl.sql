-- Hive
drop table if exists tyf_db.dept;
create table if not exists tyf_db.dept(
deptno int comment '所在部门编号'
,dname string comment '部门名称'
,loc string comment '部门所在位置'
) comment '部门表' row format delimited fields terminated by ',' stored as textfile;