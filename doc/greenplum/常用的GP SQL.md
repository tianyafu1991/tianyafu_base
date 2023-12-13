

## 查询正在运行的sql，session
```sql
SELECT
tt.procpid                                      -- pid
,usename user_name                              -- 执行的用户
,backend_start                                  -- 会话开始时间
,query_start                                    -- 查询开始时间
,waiting                                        -- 是否等待执行
,now() - query_start AS current_query_time      -- 累计执行时间
,now() - backend_start AS current_session_time  -- session累计执行时间
,current_query                                  -- 执行的sql
,client_addr                                    -- 客户端的ip
,datname                                        -- 数据库名
FROM
pg_stat_activity tt
WHERE 
current_query != '<IDLE>'
ORDER BY current_query_time DESC
;
```