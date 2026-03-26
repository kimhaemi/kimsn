/*
 * 수신상태 결과
 * - 문자 발송 여부 update 활용
 * -- 1이면 문자 전송 안함
 */
select * from watchdog.receive_condition rc 
where rc.data_kind = 'TDWR'
and rc.data_type = 'NQC';

-- 문자 전송 여부 초기화
update watchdog.receive_condition rc set
	sms_send = 0
where rc.data_kind = 'TDWR'
and rc.data_type = 'NQC';

commit;


-- site IIA
-- data_kind TDWR

/**
 * 문자 전송자 리스트
 */
select count(*)
--   DISTINCT stm.name,
--   stm.phone_num,
--   stml.warn, -- 경고 -- MISS
--   stml.tota, -- 네트워크 오류
--   stml.retr, -- 복구 -- 장애 복구, 네트워크 복구
--   stml.sms  -- 문자 발송 여부
from watchdog.sms_target_group stg
left join watchdog.sms_target_group_link stgl on stg.gid = stgl.group_id
left join watchdog.sms_target_member_link stml on stgl.group_id = stml.gid
left join watchdog.sms_target_member stm on stml.mid = stm.mid
where 1=1
and stg.status = 1
and stg.activation = 1
and stgl.data_type = 'NQC'
and stm.activation = 1
and stgl.data_kind = 'TDWR' -- param
and ('IIA' is null or stgl.site = 'IIA') -- param
-- and gid in (5, 17, 21)
order by stm.name asc
;


select * from watchdog.sms_send_pattern ssp ;

select * from nuri.app_template_code atc ;


select * from watchdog.sms_target_group stg
where 1=1
  and stg.status = 1
  and stg.activation = 1
  and gid in (5, 17, 21)
order by sort_order 
;

-- 5 17 21
 
select * from watchdog.sms_target_group_link stgl
where 1=1
  and stgl.data_type = 'NQC'
  and (:site is null or stgl.site = :site) -- param
;

select * from watchdog.sms_target_member_link stml
where 1=1
;

select * from watchdog.sms_target_member stm
where 1=1
  and stm.activation = 1
;



