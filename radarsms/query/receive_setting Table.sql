-- watchdog.receive_setting definition

CREATE TABLE `receive_setting_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `data_kind` char(3) NOT NULL COMMENT '자료종류',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류',
  `data_name` varchar(50) NOT NULL COMMENT '데이터 이름',
  `time_zone` char(1) NOT NULL COMMENT '시간대 설정 (U:UTC,K:KST)',
  `filename_pattern` varchar(50) NOT NULL COMMENT '파일 이름 패턴',
  `filename_regex` varchar(50) NOT NULL COMMENT '파일 이름 정규식',
  `recv_interval` int NOT NULL COMMENT '수집 간격',
  `delay_tolerance` int NOT NULL COMMENT '누락처리하지 않고 기다려주는 시간 / 분 단위로 기록 ',
  `permitted_watch` tinyint(1) NOT NULL DEFAULT '1' COMMENT '자료 감시여부 설정 1:true, 0:false',
  PRIMARY KEY (`id`, `data_kind`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='자료 수신 처리 설정 테이블';

select * from receive_setting_1;

INSERT INTO watchdog.receive_setting_1
(data_kind, data_type, data_name, time_zone, filename_pattern, filename_regex, recv_interval, delay_tolerance, permitted_watch)
(
select
data_kind, data_type, data_name, time_zone, filename_pattern, filename_regex, recv_interval, delay_tolerance, permitted_watch
from receive_setting
);


receiveSetting




