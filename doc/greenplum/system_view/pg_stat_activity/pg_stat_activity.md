# pg_stat_activity

## 视图说明
```text
One row per server process, showing information related to the current activity of that process, such as state and current query。

每一行都表示一个系统进程，显示与当前会话的活动进程的一些信息，比如当前回话的状态和查询等。

pg_stat_activity是一张系统统计视图，可以用于任务分析诊断。这是非常有用且功能强大的视图，其价值很难衡量。简而言之，pg_stat_activity的主要作用是显示GP中的当前活动。
```
## 参考文档
```text
https://cloud.tencent.com/developer/article/1786776?areaSource=102001.3&traceId=aLn7xtq-G4kaFLcGpglW9
https://z.itpub.net/article/detail/8A7AC8E03CFD3B847A001475DFCB627D
```

## 表结构及注释
```sql
app=# \d+ pg_stat_activity
                        View "pg_catalog.pg_stat_activity"
      Column      |           Type           | Modifiers | Storage  | Description 
------------------+--------------------------+-----------+----------+-------------
 datid            | oid                      |           | plain    | 
 datname          | name                     |           | plain    | 
 procpid          | integer                  |           | plain    | 
 sess_id          | integer                  |           | plain    | 
 usesysid         | oid                      |           | plain    | 
 usename          | name                     |           | plain    | 
 current_query    | text                     |           | extended | 
 waiting          | boolean                  |           | plain    | 
 query_start      | timestamp with time zone |           | plain    | 
 backend_start    | timestamp with time zone |           | plain    | 
 client_addr      | inet                     |           | main     | 
 client_port      | integer                  |           | plain    | 
 application_name | text                     |           | extended | 
 xact_start       | timestamp with time zone |           | plain    | 
 waiting_reason   | text                     |           | extended | 
 rsgid            | oid                      |           | plain    | 
 rsgname          | text                     |           | extended | 
 rsgqueueduration | interval                 |           | plain    | 
View definition:
SELECT s.datid, d.datname, s.procpid, s.sess_id, s.usesysid, u.rolname AS usename, s.current_query, s.waiting, s.query_start, s.backend_start, s.client_addr, s.client_port, s.application_name, s.xact_start, s.waiting_reason, s.rsgid, s.rsgname, s.rsgqueueduration
FROM pg_database d, pg_stat_get_activity(NULL::integer) s(datid, procpid, usesysid, application_name, current_query, waiting, xact_start, query_start, backend_start, client_addr, client_port, sess_id, waiting_reason, rsgid, rsgname, rsgqueueduration), pg_authid u
WHERE s.datid = d.oid AND s.usesysid = u.oid;
```

## 字段说明
```text

```