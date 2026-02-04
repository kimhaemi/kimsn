-- watchdog.sms_target_group_link definition

CREATE TABLE `sms_target_group_link_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `site` char(3) NOT NULL COMMENT '관측 지점',
  `data_kind` char(3) NOT NULL COMMENT '자료 종류(RDR,LGT)',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류',
  `group_id` int NOT NULL COMMENT 'sms_target_group의 gid',
  PRIMARY KEY (`id`, `site`,`data_kind`,`data_type`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='누락 이벤트 문자 발송 대상 그룹 설정';

select * from sms_target_group_link_1;

INSERT INTO watchdog.sms_target_group_link_1
(site, data_kind, data_type, group_id)
(select
site, data_kind, data_type, group_id
from sms_target_group_link
);

SmsTargetGroupLink
