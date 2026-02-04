-- watchdog.receive_condition_criteria definition

CREATE TABLE `receive_condition_criteria_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `code` char(4) NOT NULL COMMENT '경고기준코드',
  `name` varchar(50) NOT NULL COMMENT '경고기준이름',
  `criterion` int NOT NULL COMMENT '경고 적용이 되는 누락 자료수',
  `comment` varchar(500) DEFAULT NULL COMMENT '설명',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='경고 기준 테이블 receive_condition 테이블 대응';

select * from receive_condition_criteria_1;

INSERT INTO watchdog.receive_condition_criteria_1
(code, name, criterion, comment)
(
select
	code, name, criterion, comment
from receive_condition_criteria
);

commit;

receiveConditionCriteria


