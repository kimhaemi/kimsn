-- /* Claude 주석 (2026-11-19): 새로 작성한 파일 — 기후부(MCEE) 기능 이전 구조의 로컬 DB를 현재 소스의 엔티티 구조에 맞추는 SQL */
--
-- 배경: 로컬 DB에 기후부 기능 때 추가된 컬럼 8개가 없어, 로그인 후 메인 화면에서
--       "Unknown column 'sd1_0.agency_cd'" 오류가 남 (업그레이드 전 코드 b250e18에서도 동일).
--       참고: Query/dump/dump-watchdog-202607161334.sql 에는 8개 컬럼과 기후부 데이터가 이미 있음 → 로컬 DB는 그보다 이전 상태.
--       덤프로 다시 복원하면 이 파일은 필요 없음.
-- 컬럼 정의: 저장소의 Query/기후부 등록 query.sql, note/그룹 감시자료 설정.sql 의 ALTER 문과 동일하게 작성.
-- 데이터: 2)에서 기존 행(전부 기상청 KMA 지점)의 새 컬럼을 채우고,
--         3)에서 Query/dump/dump-watchdog-202607161334.sql 에만 있는 기상청B(MCEE) 18개 지점과 기상청A 제주공항(JIA, TDWR)을 추가.
--         회원·수신 기록(receive_data)·문자 수신자(sms_target_member*)는 개인정보/운영 기록이라 가져오지 않음.
-- 실행 이력: 2026-11-19 로컬 DB에 1)~3) 적용 완료 (INSERT IGNORE 이므로 다시 실행해도 중복 추가되지 않음)
-- 운영 DB에는 실행하지 말 것 (운영 DB는 이미 위 SQL 파일들이 적용된 상태).
-- 되돌리기: 맨 아래 "되돌리기" 블록 (3) 추가 행 삭제 → 1) 추가 컬럼 삭제, 기존 행의 기존 값은 영향 없음)

-- 1) 컬럼 추가 (DDL — MySQL에서는 자동 commit 되므로 이미 있는 컬럼은 건너뛰고 실행)
ALTER TABLE watchdog.station_rdr ADD `status` tinyint DEFAULT 1 COMMENT '사용 상태';
ALTER TABLE watchdog.station_rdr ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.station_rdr ADD `style_attr` VARCHAR(255) DEFAULT NULL COMMENT '화면 배치 CSS 스타일';
ALTER TABLE watchdog.receive_condition ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.station_status ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.sms_target_group ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.sms_target_group ADD gubun tinyint(1) NULL COMMENT '0:ALL, 1:대형, 2:소형, 3:공항';
ALTER TABLE watchdog.sms_target_group_link ADD agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';

-- 2) 값 채우기 (하나의 트랜잭션)
START TRANSACTION;
UPDATE watchdog.station_rdr SET status = 1 WHERE status IS NULL;
UPDATE watchdog.station_rdr SET agency_cd = 'KMA' WHERE agency_cd IS NULL;
UPDATE watchdog.receive_condition SET agency_cd = 'KMA' WHERE agency_cd IS NULL;
UPDATE watchdog.station_status SET agency_cd = 'KMA' WHERE agency_cd IS NULL;
UPDATE watchdog.sms_target_group_link SET agency_cd = 'KMA' WHERE agency_cd IS NULL;
UPDATE watchdog.sms_target_group SET agency_cd = 'KMA' WHERE agency_cd IS NULL;
-- gubun: 지점 이름이 station_rdr.name_kr과 같으면 그 지점의 gubun(1 대형, 2 소형, 3 공항), 나머지(그룹·ADMIN·전지점·네트워크·낙뢰)는 0(ALL)
UPDATE watchdog.sms_target_group g
  LEFT JOIN watchdog.station_rdr s ON s.name_kr = g.name
   SET g.gubun = IFNULL(s.gubun, 0)
 WHERE g.gubun IS NULL;
-- style_attr: Query/기후부 등록 query.sql 32~69행의 기상청 지점 좌표와 동일
UPDATE watchdog.station_rdr
SET style_attr = CASE site_cd
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
    WHEN 'IIA' THEN 'top:160px; left:180px; background-size: 70% 70%;'
    WHEN 'MIL' THEN 'top:250px; left:200px; background-size: 35% 35%;'
    WHEN 'SRI' THEN 'top:210px; left:230px; background-size: 35% 35%;'
    WHEN 'DJK' THEN 'top:210px; left:150px; background-size: 35% 35%;'
    ELSE CASE WHEN gubun = 2 THEN 'top:170px; left:250px; background-size: 35% 35%;'
              ELSE 'top:170px; left:250px; background-size: 70% 70%;' END
END
WHERE style_attr IS NULL;
COMMIT;

-- 3) 덤프에만 있는 지점 추가: 기상청B(MCEE) 대형 7·소형 11, 기상청A 제주공항(JIA, gubun 3 공항, TDWR)
--    값은 덤프 행 그대로 (sms_target_group gid 24~33 = 기상청B 그룹, 35 = 제주공항; JIA 연결 2행 = 전지점(gid 5)·제주공항(gid 35))
START TRANSACTION;
INSERT IGNORE INTO watchdog.station_rdr
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order, gubun, status, agency_cd, style_attr)
VALUES
('JIA', 47114, '제주공항', '', 0, 0, 0, 0, '', '', '', '2026-06-22', '', '', 31, 31, 3, 1, 'KMA', 'top:540px; left:180px; background-size: 70% 70%;'),
('BSL', 61001, '비슬산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 51, 1, 1, 'MCEE', 'top:340px; left:350px; background-size: 70% 70%;'),
('SBS', 61002, '소백산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 52, 1, 1, 'MCEE', 'top:200px; left:350px; background-size: 70% 70%;'),
('GRS', 61003, '가리산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 53, 1, 1, 'MCEE', 'top:70px; left:290px; background-size: 70% 70%;'),
('YBS', 61004, '예봉산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 54, 1, 1, 'MCEE', 'top:100px; left:250px; background-size: 70% 70%;'),
('GAS', 61005, '감악산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 55, 1, 1, 'MCEE', 'top:40px; left:150px; background-size: 70% 70%;'),
('SDS', 61006, '서대산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 56, 1, 1, 'MCEE', 'top:300px; left:270px; background-size: 70% 70%;'),
('MHS', 61007, '모후산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 57, 1, 1, 'MCEE', 'top:410px; left:250px; background-size: 70% 70%;'),
('SAC', 62001, '삼척', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 61, 2, 1, 'MCEE', 'top:200px; left:410px; background-size: 35% 35%;'),
('TGS', 62002, '통고산', '', 0, 0, 0, 0, '', '', '', '2026-06-04', '', '', 1, 62, 2, 1, 'MCEE', 'top:250px; left:410px; background-size: 35% 35%;'),
('GJU', 62003, '광주', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 63, 2, 1, 'MCEE', 'top:410px; left:220px; background-size: 35% 35%;'),
('DJN', 62004, '대전', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 64, 2, 1, 'MCEE', 'top:290px; left:245px; background-size: 35% 35%;'),
('BSN', 62005, '부산', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 65, 2, 1, 'MCEE', 'top:410px; left:400px; background-size: 35% 35%;'),
('BSE', 62006, '부산EDC', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 66, 2, 1, 'MCEE', 'top:430px; left:370px; background-size: 35% 35%;'),
('SEJ', 62007, '세종', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 67, 2, 1, 'MCEE', 'top:270px; left:230px; background-size: 35% 35%;'),
('JJU', 62008, '전주', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 68, 2, 1, 'MCEE', 'top:350px; left:250px; background-size: 35% 35%;'),
('USN', 62009, '울산', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 69, 2, 1, 'MCEE', 'top:370px; left:400px; background-size: 35% 35%;'),
('TSB', 62010, '임진강', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 70, 2, 1, 'MCEE', 'top:110px; left:80px; background-size: 35% 35%;'),
('CHU', 62011, '청주', '', 0, 0, 0, 0, '', '', '', '2026-06-29', '', '', 1, 71, 2, 1, 'MCEE', 'top:250px; left:270px; background-size: 35% 35%;');

INSERT IGNORE INTO watchdog.station_status
(site_cd, site_name, site_status, sort_order, gubun, status, agency_cd)
VALUES
('JIA', '제주공항', 'RUN', 31, 3, 1, 'KMA'),
('BSL', '비슬산', 'RUN', 60, 1, 1, 'MCEE'),
('GAS', '감악산', 'RUN', 61, 1, 1, 'MCEE'),
('GRS', '가리산', 'RUN', 62, 1, 1, 'MCEE'),
('MHS', '모후산', 'RUN', 63, 1, 1, 'MCEE'),
('SBS', '소백산', 'RUN', 64, 1, 1, 'MCEE'),
('SDS', '서대산', 'RUN', 65, 1, 1, 'MCEE'),
('YBS', '예봉산', 'RUN', 66, 1, 1, 'MCEE'),
('SAC', '삼척', 'RUN', 70, 2, 1, 'MCEE'),
('TGS', '통고산', 'RUN', 71, 2, 1, 'MCEE'),
('TSB', '임진강', 'RUN', 79, 2, 1, 'MCEE');

INSERT IGNORE INTO watchdog.receive_condition
(site, data_kind, data_type, recv_condition, apply_time, last_check_time, sms_send, sms_send_activation, status, codedtl, agency_cd)
VALUES
('BSE', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('BSL', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:55:00', '2026-06-26 00:11:23', 0, 1, 1, 'ok', 'MCEE'),
('BSN', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('CHU', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 0, 1, 'ok', 'MCEE'),
('DJN', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('GAS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:10:00', '2026-06-26 00:11:23', 1, 1, 1, 'ok', 'MCEE'),
('GJU', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('GRS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:10:00', '2026-06-26 00:11:23', 1, 1, 1, 'ok', 'MCEE'),
('JIA', 'TDWR', 'NQC', 'ORDI', '2026-06-25 02:25:00', '2026-06-26 00:06:21', 0, 1, 1, 'ok', 'KMA'),
('JJU', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('MHS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:10:00', '2026-06-26 00:11:23', 1, 1, 1, 'ok', 'MCEE'),
('SAC', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('SBS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:55:00', '2026-06-26 00:11:23', 0, 1, 1, 'ok', 'MCEE'),
('SDS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:10:00', '2026-06-26 00:11:23', 1, 1, 1, 'ok', 'MCEE'),
('SEJ', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('TGS', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 0, 1, 'ok', 'MCEE'),
('TSB', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 1, 1, 'ok', 'MCEE'),
('USN', 'SDR', 'NQC', 'ORDI', '2026-06-29 15:40:00', '2026-06-29 21:21:25', 1, 0, 1, 'ok', 'MCEE'),
('YBS', 'RDR', 'NQC', 'ORDI', '2026-06-25 23:10:00', '2026-06-26 00:11:23', 1, 1, 1, 'ok', 'MCEE');

INSERT IGNORE INTO watchdog.sms_target_group
(gid, name, activation, status, sort_order, agency_cd, gubun)
VALUES
(24, '비슬산', 0, 1, 1, 'MCEE', 1),
(25, '감악산', 1, 1, 2, 'MCEE', 1),
(26, '가리산', 1, 1, 3, 'MCEE', 1),
(27, '모후산', 0, 1, 4, 'MCEE', 1),
(28, '소백산', 0, 1, 5, 'MCEE', 1),
(29, '서대산', 0, 1, 6, 'MCEE', 1),
(30, '예봉산', 0, 1, 7, 'MCEE', 1),
(31, '삼척', 0, 1, 8, 'MCEE', 2),
(32, '통고산', 0, 1, 9, 'MCEE', 2),
(33, '임진강', 0, 1, 10, 'MCEE', 2),
(35, '제주공항', 1, 1, 18, 'KMA', 3);

INSERT IGNORE INTO watchdog.sms_target_group_link
(site, data_kind, data_type, group_id, agency_cd)
VALUES
('BSL', 'RDR', 'NQC', 5, 'MCEE'),
('BSN', 'SDR', 'NQC', 5, 'MCEE'),
('JIA', 'TDWR', 'NQC', 5, 'KMA'),
('JIA', 'TDWR', 'NQC', 35, 'KMA'),
('SAC', 'SDR', 'NQC', 5, 'MCEE');
COMMIT;

-- 되돌리기 (필요할 때만 수동 실행)
-- 3) 되돌리기 (먼저 실행)
-- DELETE FROM watchdog.sms_target_group_link WHERE agency_cd = 'MCEE' OR site = 'JIA';
-- DELETE FROM watchdog.sms_target_group WHERE gid BETWEEN 24 AND 33 OR gid = 35;
-- DELETE FROM watchdog.receive_condition WHERE agency_cd = 'MCEE' OR site = 'JIA';
-- DELETE FROM watchdog.station_status WHERE agency_cd = 'MCEE' OR site_cd = 'JIA';
-- DELETE FROM watchdog.station_rdr WHERE agency_cd = 'MCEE' OR site_cd = 'JIA';
-- 1)·2) 되돌리기
-- ALTER TABLE watchdog.station_rdr DROP COLUMN `status`, DROP COLUMN agency_cd, DROP COLUMN style_attr;
-- ALTER TABLE watchdog.receive_condition DROP COLUMN agency_cd;
-- ALTER TABLE watchdog.station_status DROP COLUMN agency_cd;
-- ALTER TABLE watchdog.sms_target_group DROP COLUMN agency_cd, DROP COLUMN gubun;
-- ALTER TABLE watchdog.sms_target_group_link DROP COLUMN agency_cd;
