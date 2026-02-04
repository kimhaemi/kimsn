-- watchdog.sms_target_group definition

CREATE TABLE `sms_target_group_1` (
  `gid` int NOT NULL AUTO_INCREMENT COMMENT 'SMS 수신그룹 ID',
  `name` varchar(20) NOT NULL COMMENT '수신자 그룹 이름',
  `activation` tinyint(1) NOT NULL COMMENT '활성화 ',
  PRIMARY KEY (`gid`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb3 COMMENT='SMS 수신자 그룹';

select * from sms_target_group_1;

INSERT INTO watchdog.sms_target_group_1
(name, activation)
(select
name, activation
from sms_target_group
);

SmsTargetGroup
