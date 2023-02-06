-- Hive
drop table if exists tyf_db.emp;
create table if not exists tyf_db.emp(
empno int comment '员工编号'
,ename string comment '员工姓名'
,job string comment '员工职位'
,mgr int comment '员工对应领导的编号'
,hiredate date comment '员工入职日期'
,sal int comment '基本工资'
,comm int comment '奖金'
,deptno int comment '所在部门编号'
) comment '员工表' row format delimited fields terminated by ',' stored as textfile;