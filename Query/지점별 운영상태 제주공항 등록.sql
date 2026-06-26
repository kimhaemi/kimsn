터미널 접속: mysql -u root -p
비밀번호: Watch1234!

-- 지점별 운영상태 설정: 제주공항
INSERT INTO watchdog.station_status
(site_cd, site_name, site_status, sort_order, gubun, status, agency_cd)
values
('JIA', '제주공항', 'RUN', 31, 3, 1, 'KMA');

-- * 꼭 해야합니다.
commit;