-- lock search
SHOW PROCESSLIST;

SELECT * 
FROM information_schema.innodb_trx;

KILL 3017;

commit;

ALTER TABLE watchdog.receive_data ADD INDEX idx_recv_site_time (site, data_time, recv_condition);

ALTER TABLE `station_rdr` ADD `status` tinyint DEFAULT 1 COMMENT '사용 상태';

update watchdog.station_rdr sr set
status = 1;

select * from watchdog.station_rdr sr ;

-- 기후부 등록 query
ALTER TABLE watchdog.station_rdr add agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
-- update watchdog.station_rdr set
-- agency_cd = 'KMA'
-- agency_cd = 'MCEE'
select * from watchdog.station_rdr sr
where site_num > 60000;

-- 2. 현재 웹 화면 배치용 데이터 (현재 UI 유지보수 제로화용)
ALTER TABLE `station_rdr` ADD `style_attr` VARCHAR(255) DEFAULT NULL COMMENT '화면 배치 CSS 스타일';

-- 기상청: 대형
UPDATE watchdog.station_rdr
SET style_attr = CASE site_cd
    -- [기상청 대형]
    WHEN 'KWK' THEN 'top:170px; left:250px; background-size: 70% 70%;'
    WHEN 'BRI' THEN 'top:100px; left:25px; background-size: 70% 70%;'
    WHEN 'GDK' THEN 'top:90px; left:260px; background-size: 70% 70%;'
    WHEN 'GNG' THEN 'top:130px; left:370px; background-size: 70% 70%;'
    WHEN 'MYN' THEN 'top:320px; left:380px; background-size: 70% 70%;'
    WHEN 'PSN' THEN 'top:420px; left:390px; background-size: 70% 70%;'
    WHEN 'KSN' THEN 'top:340px; left:180px; background-size: 70% 70%;'
    WHEN 'JNI' THEN 'top:470px; left:160px; background-size: 70% 70%;'
    WHEN 'GSN' THEN 'top:565px; left:120px; background-size: 70% 70%;'
    WHEN 'SSP' THEN 'top:565px; left:230px; background-size: 70% 70%;'
    WHEN 'YIT' THEN 'top:210px; left:270px; background-size: 70% 70%;'
    -- [기상청 공항]
    WHEN 'IIA' THEN 'top:160px; left:180px; background-size: 70% 70%;'
    -- [기상청 소형]
    WHEN 'MIL' THEN 'top:250px; left:200px; background-size: 35% 35%;'
    WHEN 'SRI' THEN 'top:210px; left:230px; background-size: 35% 35%;'
    WHEN 'DJK' THEN 'top:210px; left:150px; background-size: 35% 35%;'
    -- [기후부 대형]
    WHEN 'BSL' THEN 'top:340px; left:350px; background-size: 70% 70%;'
    WHEN 'SBS' THEN 'top:200px; left:350px; background-size: 70% 70%;'
    WHEN 'GRS' THEN 'top:70px; left:290px; background-size: 70% 70%;'
    WHEN 'YBS' THEN 'top:100px; left:250px; background-size: 70% 70%;'
    WHEN 'GAS' THEN 'top:40px; left:150px; background-size: 70% 70%;'
    WHEN 'SDS' THEN 'top:300px; left:270px; background-size: 70% 70%;'
    WHEN 'MHS' THEN 'top:410px; left:250px; background-size: 70% 70%;'
    -- [기후부 소형]
    WHEN 'SAC' THEN 'top:200px; left:410px; background-size: 35% 35%;'
    WHEN 'TGS' THEN 'top:250px; left:410px; background-size: 35% 35%;'
    WHEN 'TSB' THEN 'top:50px; left:220px; background-size: 35% 35%;'    
    -- 데이터가 비어있던 나머지 소형 레이더(광주, 부산, 세종, 울산 등)의 예외 처리 기본값 정의
    ELSE 
        CASE WHEN gubun = 2 THEN 'top:170px; left:250px; background-size: 35% 35%;'
             ELSE 'top:170px; left:250px; background-size: 70% 70%;'
        END
END;


select
sr.site_cd , sr.site_num, sr.name_kr , SR.style_attr
from watchdog.station_rdr sr
-- where site_cd = 'KWK'
order by sr.sort_order 
;

commit;

INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES
-- 대형
('BSL', 61001, '비슬산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 51, 1, 'MCEE'),
('SBS', 61002, '소백산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 52, 1, 'MCEE'),
('GRS', 61003, '가리산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 53, 1, 'MCEE'),
('YBS', 61004, '예봉산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 54, 1, 'MCEE'),
('GAS', 61005, '감악산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 55, 1, 'MCEE'),
('SDS', 61006, '서대산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 56, 1, 'MCEE'),
('MHS', 61007, '모후산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 57, 1, 'MCEE')
-- 소형
('SAC', 62001, '삼척', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 61, 2, 'MCEE'),
('TGS', 62002, '통고산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 62, 2, 'MCEE'),
-- ('', 62003, '광주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 63, 2, 'MCEE'),
-- ('', 62004, '부산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 64, 2, 'MCEE'),
-- ('', 62005, '부산EDC', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 65, 2, 'MCEE'),
-- ('', 62006, '세종', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 66, 2, 'MCEE'),
-- ('', 62007, '전주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 67, 2, 'MCEE'),
-- ('', 62008, '울산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 68, 2, 'MCEE'),
('TSB', 62009, '임진강', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 69, 2, 'MCEE'),
-- ('', 62010, '청주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 70, 2, 'MCEE')
;


commit;

select * from watchdog.station_rdr sr order by sort_order, site_num;

select * from watchdog.receive_condition rc where rc.data_type = 'NQC' and rc.agency_cd = 'KMA';

-- 기후부 등록 query
ALTER TABLE watchdog.receive_condition add agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';

-- update watchdog.receive_condition set
-- agency_cd = 'KMA'
-- agency_cd = 'MCEE'
select * from watchdog.receive_condition rc
where data_type = 'NQC'
and site in (
	select site_cd from watchdog.station_rdr sr
where site_num < 60000
);

commit;


/**
 * receive_condition: 기후부 대형
 */

INSERT INTO watchdog.receive_condition
(site, data_kind, data_type, recv_condition, apply_time, last_check_time, sms_send, sms_send_activation, status, codedtl, agency_cd)
VALUES 
-- 대형
('BSL', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 비슬산
('SBS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 소백산
('GRS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 가리산
('YBS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 예봉산
('GAS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 감악산
('SDS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 서대산
('MHS', 'RDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 모후산
-- 소형
('SAC', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 삼척
('TGS', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 통고산
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 광주
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 부산
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 부산EDC
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 세종
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 전주
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 울산
('TSB', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 임진강
-- ('', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 청주
;



-- 기후부 등록 query
ALTER TABLE `station_status` ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';

-- update watchdog.station_status set
-- agency_cd = 'KMA'
select * from watchdog.station_status ss 
order by sort_order 
;

commit;

INSERT INTO watchdog.station_status
(site_cd, site_name, site_status, sort_order, gubun, status, agency_cd)
VALUES
-- 대형
('BSL', '비슬산', 'RUN', 60, 1, 1, 'MCEE'),
('GAS', '감악산', 'RUN', 61, 1, 1, 'MCEE'),
('GRS', '가리산', 'RUN', 62, 1, 1, 'MCEE'),
('MHS', '모후산', 'RUN', 63, 1, 1, 'MCEE'),
('SBS', '소백산', 'RUN', 64, 1, 1, 'MCEE'),
('SDS', '서대산', 'RUN', 65, 1, 1, 'MCEE'),
('YBS', '예봉산', 'RUN', 66, 1, 1, 'MCEE'),
-- 소형
('SAC', '삼척', 'RUN', 70, 2, 1, 'MCEE'),
('TGS', '통고산', 'RUN', 71, 2, 1, 'MCEE'),
-- ('', '광주', 'RUN', 73, 2, 1, 'MCEE'),
-- ('', '부산', 'RUN', 74, 2, 1, 'MCEE'),
-- ('', '부산EDC', 'RUN', 75, 2, 1, 'MCEE'),
-- ('', '세종', 'RUN', 76, 2, 1, 'MCEE'),
-- ('', '전주', 'RUN', 77, 2, 1, 'MCEE'),
-- ('', '울산', 'RUN', 78, 2, 1, 'MCEE'),
('TSB', '임진강', 'RUN', 79, 2, 1, 'MCEE')
-- ('', '청주', 'RUN', 80, 2, 1, 'MCEE'),
;


ALTER TABLE `sms_target_group` ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE `sms_target_group` ADD gubun tinyint(1) NULL COMMENT '0:ALL, 1:대형, 2:소형, 3:공항';

update watchdog.sms_target_group set
gubun = 0,
agency_cd = 'KMA'
where name like '[%'
or name in ('ADMIN', '전지점') -- 그룹
;

update watchdog.sms_target_group set
gubun = 3,
agency_cd = 'KMA'
-- where gid in (19, 18, 20) -- 기상청 소형
where gid = 21 -- 공항
where gid not in (1, 2, 3, 5, 16, 17, 19, 18, 20, 21) -- 기상청 대형
;

select * from watchdog.sms_target_group stg
-- where gid = 21 -- 공항
-- where gid in (19, 18, 20) -- 기상청 소형
-- where gid in (1, 2, 3, 5, 16, 17) -- 그룹
-- where gid not in (1, 2, 3, 5, 16, 17, 19, 18, 20, 21)
order by agency_cd, gubun, sort_order;

commit;

-- 기후부 data insert
INSERT INTO watchdog.sms_target_group
(name, activation, status, sort_order, agency_cd, gubun)
VALUES
-- 대형
('[5그룹] 비슬산 감악산 가리산 모후산 소백산 소대산 예봉산', 0, 1, 6, 'MCEE', 0),
-- ('[6그룹] 삼척, 통고산, 임진강', 0, 1, 0, 'MCEE', 0),
('비슬산', 0, 1, 1, 'MCEE', 1),
('감악산', 0, 1, 2, 'MCEE', 1),
('가리산', 0, 1, 3, 'MCEE', 1),
('모후산', 0, 1, 4, 'MCEE', 1),
('소백산', 0, 1, 5, 'MCEE', 1),
('서대산', 0, 1, 6, 'MCEE', 1),
('예봉산', 0, 1, 7, 'MCEE', 1),
-- 소형
('삼척', 0, 1, 8, 'MCEE', 2),
('통고산', 0, 1, 9, 'MCEE', 2),
-- ('광주', 0, 1, 10, 'MCEE', 2),
-- ('부산', 0, 1, 12, 'MCEE', 2),
-- ('부산EDC', 0, 1, 13, 'MCEE', 2),
-- ('세종', 0, 1, 14, 'MCEE', 2),
-- ('전주', 0, 1, 15, 'MCEE', 2),
-- ('울산', 0, 1, 16, 'MCEE', 2),
('임진강', 0, 1, 17, 'MCEE', 2)
-- ('청주', 0, 1, 18, 'MCEE', 2),
;

commit;
