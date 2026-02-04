-- watchdog.receive_condition definition

CREATE TABLE `receive_condition_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `site` char(3) NOT NULL COMMENT '관측 지점',
  `data_kind` char(3) NOT NULL COMMENT '자료 종류(RDR,LGT)',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류',
  `recv_condition` char(4) NOT NULL COMMENT '항목별 자료 수신 상태',
  `apply_time` datetime DEFAULT NULL COMMENT '상태 적용 시각',
  `last_check_time` datetime DEFAULT NULL COMMENT '최종 확인 시각',
  `sms_send` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'SMS 발송 여부',
  `sms_send_activation` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'SMS 발송 기능 ON/OFF',
  PRIMARY KEY (`id`,`site`,`data_kind`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='자료 수신 상태 테이블';



insert into watchdog.receive_condition_1
(site, data_kind, data_type, recv_condition, apply_time, last_check_time, sms_send, sms_send_activation)
(
SELECT site, data_kind, data_type, recv_condition, apply_time, last_check_time, sms_send, sms_send_activation
FROM watchdog.receive_condition
)