select site,count(1) cnt from default.site group by site;
select * from ywjkq_dw.ods_country_single_champion_enterprise_yy_f where partition_day=${hiveconf:dt};