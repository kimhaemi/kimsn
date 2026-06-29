터미널 접속: mysql -u root -p
비밀번호: Watch1234!

-- 지점별 운영상태 설정: 제주공항
INSERT INTO watchdog.station_status
(site_cd, site_name, site_status, sort_order, gubun, status, agency_cd)
values
('JIA', '제주공항', 'RUN', 31, 3, 1, 'KMA');

-- * 꼭 해야합니다.
commit;

select * from watchdog.station_rdr sr 
where site_cd = 'TGS';

INSERT INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, agency_cd, style_attr)
values
('GJU', 62003, '광주', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 63, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('DJN', 62004, '대전', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 64, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('BSN', 62005, '부산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 65, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('BSE', 62006, '부산EDC', '', 0, 0, 0, 0, '', '', '', NOW(),'', '', 1, 66, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('SEJ', 62007, '세종', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 67, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('JJU', 62008, '전주', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 68, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('USN', 62009, '울산', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 69, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('TSB', 62010, '임진강', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 70, 2, 'MCEE', 'top:50px; left:220px; background-size: 35% 35%;'),
('CHU', 62011, '청주', '', 0, 0, 0, 0, '', '', '', NOW(), '', '', 1, 71, 2, 'MCEE', 'top:50px; left:120px; background-size: 35% 35%;')

;

commit;

UPDATE watchdog.station_rdr sr
SET style_attr = CASE site_cd
	WHEN 'GJU' THEN 'top:410px; left:220px; background-size: 35% 35%;'
	WHEN 'DJN' THEN 'top:290px; left:245px; background-size: 35% 35%;'
	WHEN 'BSN' THEN 'top:420px; left:390px; background-size: 35% 35%;'
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

select * from watchdog.station_rdr sr 
where agency_cd = 'MCEE'
and gubun = 2
and sr.site_cd not in ('SAC', 'TGS');

INSERT INTO watchdog.receive_condition
(site, data_kind, data_type, recv_condition, apply_time, last_check_time, sms_send, sms_send_activation, status, codedtl, agency_cd)
VALUES 
('GJU', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 광주
('DJN', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 대전
('BSN', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 부산
('BSE', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 부산EDC
('SEJ', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 세종
('JJU', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 전주
('USN', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 울산
-- ('TSB', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE'), -- 임진강
('CHU', 'SDR', 'NQC', 'ORDI', now(), now(), 0, 1, 1, '', 'MCEE') -- 청주
;

commit;
