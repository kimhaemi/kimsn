-- 지도위치 업데이트: 기상청B - 소형
UPDATE watchdog.station_rdr sr
SET style_attr = CASE site_cd
	WHEN 'GJU' THEN 'top:410px; left:220px; background-size: 35% 35%;'
	WHEN 'DJN' THEN 'top:290px; left:245px; background-size: 35% 35%;'
	WHEN 'BSN' THEN 'top:410px; left:400px; background-size: 35% 35%;'
	WHEN 'BSE' THEN 'top:430px; left:370px; background-size: 35% 35%;'
	WHEN 'SEJ' THEN 'top:270px; left:230px; background-size: 35% 35%;'
	WHEN 'JJU' THEN 'top:350px; left:250px; background-size: 35% 35%;'
    WHEN 'USN' THEN 'top:370px; left:400px; background-size: 35% 35%;'
    WHEN 'TSB' THEN 'top:110px; left:80px; background-size: 35% 35%;'
    WHEN 'CHU' THEN 'top:250px; left:270px; background-size: 35% 35%;'
END
where agency_cd = 'MCEE'
and gubun = 2
and sr.site_cd not in ('SAC', 'TGS')
;

commit;

SELECT * FROM watchdog.receive_data
WHERE 1=1
                and site = 'BRI'
                and data_time >= '2023-09-01 00:00:00'  
                and data_time <  '2023-09-30 23:59:59'
                and recv_condition <> 'INIT'
                ;

EXPLAIN SELECT * FROM watchdog.receive_data
WHERE 1=1
                and site = 'BRI'
                and data_time >= '2023-09-01 00:00:00'  
                and data_time <  '2023-09-30 23:59:59'
                and recv_condition <> 'INIT'
                ;
;

SHOW INDEX FROM watchdog.receive_data;

select date_format(now(), '%Y'); 

ALTER TABLE watchdog.receive_data ADD INDEX idx_recv_site_time (site, data_time, recv_condition);


explain WITH RECURSIVE cte (n) AS ( 
          SELECT 1 
          UNION ALL 
          SELECT n + 1 FROM cte WHERE n < 12 
        ) 
          select  
            IFNULL(dt.stat_year, 0) as stat_year, 
            IFNULL(dt.stat_month, n) as stat_month, 
            IFNULL(dt.data_type, :data_type) as data_type, 
            IFNULL(dt.recv_condition, :recv_condition) as recv_condition, 
            IFNULL(dt.cnt, 0) as cnt, 
            IFNULL(dt.percent, 0) as percent 
          from cte 
            left outer join ( 
              select  
                mon.stat_year as stat_year, 
                mon.stat_month as stat_month, 
                mon.data_type as data_type, 
                mon.recv_condition as recv_condition, 
                mon.cnt as cnt, 
                ROUND((mon.cnt/total.totalcnt)*100) as percent 
              from ( 
                select  
                  DATE_FORMAT(data_time, '%Y') as stat_year, 
                  DATE_FORMAT(data_time, '%c') as stat_month, 
                  data_type as data_type,  
                  recv_condition as recv_condition,  
                  count(recv_condition) as cnt   
               from receive_data  
               where 1=1  
                 and site = 'BRI'
                 and recv_condition = 'RDR' 
                 and data_time >= '2023-09-01 00:00:00'  
                and data_time <  '2023-09-30 23:59:59'
                 and recv_condition <> 'INIT' 
               group by DATE_FORMAT(data_time, '%Y'),DATE_FORMAT(data_time, '%c'), data_type, recv_condition 
          ) mon, ( 
            SELECT  
              stat_month, 
              data_type, 
              sum(cnt) as totalcnt 
            FROM ( 
              select  
                DATE_FORMAT(data_time, '%Y') as stat_year, 
                DATE_FORMAT(data_time, '%c') as stat_month, 
                data_type as data_type, 
                recv_condition as recv_condition,  
                count(recv_condition) as cnt   
              from receive_data  
              where 1=1  
                and site = 'BRI'
                and data_time >= '2023-09-01 00:00:00'  
                and data_time <  '2023-09-30 23:59:59'
                and recv_condition <> 'INIT' 
              group by DATE_FORMAT(data_time, '%Y'), DATE_FORMAT(data_time, '%c'), data_type, recv_condition 
           ) tota 
            group by stat_month, tota.data_type  
          ) total 
          where mon.stat_month = total.stat_month 
          ) dt 
        on n = dt.stat_month
    );
    List<ReceiveDataForSiteStatMonthDto> getReceiveDataForSiteStatMonth(
        @Param(site) String site,
        @Param(data_time) String data_time,
        @Param(recv_time) String recv_time,
        @Param(recv_condition) String recv_condition,
        @Param(data_type) String data_type
    );