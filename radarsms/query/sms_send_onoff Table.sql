-- watchdog.sms_send_onoff definition

CREATE TABLE `sms_send_onoff_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `code` char(7) NOT NULL DEFAULT '',
  `value` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

select * from sms_send_onoff_1;

INSERT INTO watchdog.sms_send_onoff_1
(code, value)
VALUES('SCHEDUL', 1);

INSERT INTO watchdog.sms_send_onoff_1
(code, value)
VALUES('STOPSMS', 1);


