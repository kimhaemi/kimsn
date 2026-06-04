-- 기후부 등록 query


/**
 * station: 기후부 대형
 */
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('BSL', 61001, '비슬산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 51, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SBS', 61002, '소백산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 52, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('GRS', 61003, '가리산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 53, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('YBS', 61004, '예봉산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 54, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('GAS', 61005, '감악산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 55, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SDS', 61006, '서대산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 56, 1);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('MHS', 61007, '모후산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 57, 1);

/**
 * station: 기후부 소형
 */
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SAC', 62001, '삼척', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 61, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('TGS', 62002, '통고산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 62, 2);

INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62003, '광주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 63, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62004, '부산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 64, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62005, '부산EDC', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 65, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62006, '세종', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 66, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62007, '전주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 67, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62008, '울산', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 68, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('TSB', 62009, '임진강', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 69, 2);
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('', 62010, '청주', '', 0, 0, 0, 0, '', '', '', '', '', '', 1, 70, 2);

commit;

select * from watchdog.station_rdr sr order by sort_order, site_num;


-- =======================================================================
-- 환경부 > 대형 강우레이더 관측소 (21XXX 시리즈 / 관측반경 125km)
-- =======================================================================

-- 비슬산 (BSL)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('BSL', 21001, '비슬산', 'Biseulsan', 2, 125, 250, 500, '0.1~100', '대구 달성군 가창면 정대리 산 135-2', '강우레이더', '2009-06-01', '', '', 1, 1, 3);

-- 소백산 (SBS)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SBS', 21002, '소백산', 'Sobaeksan', 2, 125, 250, 500, '0.1~100', '충북 단양군 가곡면 어의곡리 산 86-1', '강우레이더', '2011-11-01', '', '', 1, 2, 3);

-- 서대산 (SDS)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SDS', 21003, '서대산', 'Seodaesan', 2, 125, 250, 500, '0.1~100', '충남 금산군 추부면 성당리 산 22-1', '강우레이더', '2013-10-01', '', '', 1, 3, 3);

-- 모후산 (MHS)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('MHS', 21004, '모후산', 'Mohusan', 2, 125, 250, 500, '0.1~100', '전남 화순군 사평면 유마로 565-100', '강우레이더', '2014-12-01', '', '', 1, 4, 3);

-- 가리산 (GRS)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('GRS', 21005, '가리산', 'Garisan', 2, 125, 250, 500, '0.1~100', '강원 홍천군 화촌면 풍천리 산 223', '강우레이더', '2015-11-01', '', '', 1, 5, 3);

-- 예봉산 (YBS)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('YBS', 21006, '예봉산', 'Yebongsan', 2, 125, 250, 500, '0.1~100', '경기 남양주시 와부읍 팔당리 산 9-2', '강우레이더', '2019-10-01', '', '', 1, 6, 3);

-- 임진강 (IJJ)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('IJJ', 21007, '임진강', 'Imjingang', 2, 125, 250, 500, '0.1~100', '경기 연천군 군남면 선곡리 산 101', '강우레이더', '2011-04-01', '', '', 1, 7, 3);


-- =======================================================================
-- 환경부 > 소형 강우레이더 관측소 (22XXX 시리즈 / 관측반경 40km)
-- =======================================================================

-- 삼척 (SCC)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('SCC', 22001, '삼척', 'Samcheok', 0, 40, 250, 160, '0.1~100', '강원 삼척시', '소형강우레이더', '2019-12-01', '', '', 1, 1, 3);

-- 울진 (UJN)
INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun)
VALUES('UJN', 22002, '울진', 'Uljin', 0, 40, 250, 160, '0.1~100', '경북 울진군 금강송면 통고산', '소형강우레이더', '2019-12-01', '', '', 1, 2, 3);

