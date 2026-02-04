-- watchdog.sms_send_pattern definition

CREATE TABLE `sms_send_pattern_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `code` char(4) NOT NULL COMMENT 'receive_condition_criteria code',
  `mode` char(3) NOT NULL COMMENT 'RUN : 동작 / MRO : 유지보수(Maintenance, Repair and Over haul)',
  `activation` tinyint(1) NOT NULL COMMENT '1 : ON / 0 : OFF',
  `pattern` varchar(300) DEFAULT NULL COMMENT '메시지 전송 패턴 ',
  PRIMARY KEY (`id`, `code`,`mode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='SMS 전송 메시지 패턴';


select * from sms_send_pattern_1;

INSERT INTO watchdog.sms_send_pattern_1
(code, mode, activation, pattern)
(select
code, mode, activation, pattern
from sms_send_pattern
);

SmsSendPattern
