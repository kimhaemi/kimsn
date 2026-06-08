-- lock search
SHOW PROCESSLIST;

SELECT * 
FROM information_schema.innodb_trx;

KILL 368;

commit;

-- 기후부 등록 query
ALTER TABLE watchdog.station_rdr modify agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';

-- update watchdog.station_rdr set
-- agency_cd = 'KMA'
-- agency_cd = 'MCEE'
select * from watchdog.station_rdr sr
where site_num > 60000;

commit;

/**
 * station: 기후부 대형
 */
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('BSL', 61001, '비슬산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 51, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('SBS', 61002, '소백산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 52, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('GRS', 61003, '가리산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 53, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('YBS', 61004, '예봉산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 54, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('GAS', 61005, '감악산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 55, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('SDS', 61006, '서대산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 56, 1, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('MHS', 61007, '모후산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 57, 1, 'MCEE');

/**
 * station: 기후부 소형
 */
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('SAC', 62001, '삼척', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 61, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('TGS', 62002, '통고산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 62, 2, 'MCEE');

INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62003, '광주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 63, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62004, '부산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 64, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62005, '부산EDC', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 65, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62006, '세종', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 66, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62007, '전주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 67, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62008, '울산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 68, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('TSB', 62009, '임진강', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 69, 2, 'MCEE');
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd)
VALUES('', 62010, '청주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 70, 2, 'MCEE');

commit;

select * from watchdog.station_rdr sr order by sort_order, site_num;

