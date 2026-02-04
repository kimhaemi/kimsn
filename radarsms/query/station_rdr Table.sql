-- watchdog.station_rdr definition

CREATE TABLE `station_rdr_1` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `site_cd` char(3) NOT NULL COMMENT '관측지점코드',
  `site_num` int NOT NULL COMMENT '관측지점번호',
  `name_kr` varchar(10) NOT NULL COMMENT '관측지점명(한글)',
  `name_en` varchar(20) NOT NULL COMMENT '관측지점명(영문)',
  `height` int DEFAULT NULL COMMENT '관측지점고도(km*10)',
  `max_range` int DEFAULT NULL COMMENT 'MaxRange (km)',
  `gate_size` int DEFAULT NULL COMMENT 'GateSize (m)',
  `gates` int DEFAULT NULL COMMENT 'Gates',
  `rain_intensity` varchar(10) DEFAULT NULL COMMENT 'mm/hr',
  `addr` varchar(40) DEFAULT NULL COMMENT '주소',
  `model` varchar(20) DEFAULT NULL COMMENT '모델',
  `install_date` date DEFAULT NULL COMMENT '설치일자',
  `prod_company` varchar(20) DEFAULT NULL COMMENT '제작사',
  `prod_country` varchar(20) DEFAULT NULL COMMENT '제작국가',
  `permitted_watch` tinyint(1) NOT NULL DEFAULT '1' COMMENT '자료 감시여부 설정 1:true, 0:false',
  `sort_order` tinyint(1) NOT NULL DEFAULT '1' COMMENT '웹 표출시 순서',
  `ip` varchar(255) DEFAULT NULL COMMENT '접속ip',
  `user_id` varchar(255) DEFAULT NULL COMMENT '접속 user id',
  `pwd` varchar(255) DEFAULT NULL COMMENT '접속비밀번호',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='레이더 지점 정보1';

commit;

select * from watchdog.station_rdr_1;

insert into watchdog.station_rdr_1
(site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order)
(
SELECT site_cd, site_num, name_kr, name_en, height, max_range, gate_size, gates, rain_intensity, addr, model, install_date, prod_company, prod_country, permitted_watch, sort_order
FROM watchdog.station_rdr
)