CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `username` varchar(255) NOT NULL COMMENT '사용자명',
  `password` varchar(255) NOT NULL COMMENT '비밀번호',
  `email` varchar(255),
  `roles` varchar(255),
  `provider` varchar(255),
  `provider_id` varchar(255),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '생성일시',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT '수정일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='사용자 테이블';
;

select * from users ;