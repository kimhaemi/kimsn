# DB 접속 정보: localhost(ip 입력)
url: jdbc:mysql://localhost:3306&characterEncoding=UTF-8
user: root
password: Watch1234!
-- user: guest01
-- password: guest01!

# 테이블
receive_condition -- 최종 자료 수신 상태 테이블
# 컬럼
site
data_kind
data_type
recvCondition -- 자료 수신 상태
ORDI -- 정상
TORE -- 정상(전체 네트워크 복구)
RETR -- 정상(파일 장애(파일유무, 파일 크기) 복구)
TOTA -- 장애(전체 네트워크 장애)
WARN -- 장애(파일 장애(파일유무, 파일 크기) 장애)
apply_time
last_check_time


# 추출 쿼리
select * from watchdog.receive_condition rc 
where rc.data_type = 'NQC'
order by site;

CREATE TABLE `receive_condition` (
  `site` char(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '관측 지점',
  `data_kind` char(4) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '자료 종류(RDR,SDR,TDWR,LGT)',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류', -- NQC	이진자료
  `recv_condition` char(4) NOT NULL COMMENT '항목별 자료 수신 상태',
  `apply_time` datetime DEFAULT NULL COMMENT '상태 적용 시각',
  `last_check_time` datetime DEFAULT NULL COMMENT '최종 확인 시각',
  `sms_send` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'SMS 발송 여부',
  `sms_send_activation` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'SMS 발송 기능 ON/OFF',
  `status` tinyint(1) DEFAULT '1' COMMENT '1:사용,0:미사용',
  `codedtl` varchar(100) DEFAULT NULL COMMENT '경고기준코드상세',
  `agency_cd` varchar(4) DEFAULT NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)',
  PRIMARY KEY (`site`,`data_kind`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='자료 수신 상태 테이블';


-- ====================================== 유저 등록 및 권한 부여 ======================================
# 사용자 등록:
-- localhost: 로컬접속 허용 
-- %: 외부 접속 허용
-- CREATE USER '아이디'@'호스트' IDENTIFIED BY '비밀번호';
create user 'guest01@%' IDENTIFIED BY 'guest01!';

# 특정 DB 권한 부여: 
-- grant select on 데이터베이스.* to '사용자'@'호스트';
GRANT select ON watchdog.* TO 'guest01'@'%';

# 권한 적용: 
FLUSH PRIVILEGES;