-- db dump
mysqldump -u [사용자계정] -p [패스워드] --all-databases > [생성할 백업명].sql
ex) mysqldump -u root -pWatch1234! --all-databases > /home/watcher/dbDump_260708.sql

-- 문자발송 대상자 조회
select
  DISTINCT stm.name,
  stm.phone_num,
  stml.warn, -- 경고 -- MISS
  stml.tota, -- 네트워크 오류
  stml.retr, -- 복구 -- 장애 복구, 네트워크 복구
  stml.sms  -- 문자 발송 여부
from watchdog.sms_target_group stg
left outer join watchdog.sms_target_group_link stgl
  on stg.gid = stgl.group_id
left outer join watchdog.sms_target_member_link stml
  on stgl.group_id = stml.gid
left outer join watchdog.sms_target_member stm
on stml.mid = stm.mid
where 1=1
and stg.status = 1
and stg.activation = 1
and stgl.data_type = 'NQC'
and stm.activation = 1
and stgl.data_kind = 'TDWR' -- param
and stgl.agency_cd = 'KMA' -- param
and ('JIA' is null or stgl.site = 'JIA') -- param
order by stm.name asc;

-- SMS 수신자 그룹
select * from watchdog.sms_target_group stg 
where 1=1
and stg.status = 1
and stg.activation = 1
and stg.gid in (
	select group_id from watchdog.sms_target_group_link stgl where stgl.data_type = 'NQC' and site = 'JIA' and stgl.data_kind = 'TDWR' and stgl.agency_cd = 'KMA'
) and name <> '전지점';

-- 문자 발송 대상 그룹 설정
select * from watchdog.sms_target_group_link stgl where stgl.data_type = 'NQC' and site = 'JIA' and stgl.data_kind = 'TDWR' and stgl.agency_cd = 'KMA';

-- 사용자가 어느 그룹에 속하는지 연결
select * from watchdog.sms_target_member_link stml
where stml.gid in (
	select gid from watchdog.sms_target_group stg 
	where 1=1
	and stg.status = 1
	and stg.activation = 1
	and stg.gid in (
		select group_id from watchdog.sms_target_group_link stgl where stgl.data_type = 'NQC' and site = 'JIA' and stgl.data_kind = 'TDWR' and stgl.agency_cd = 'KMA'
	) and name <> '전지점'
);

-- 문자발송 대상자
select * from watchdog.sms_target_member stm
where stm.activation = 1
and mid in (
	select mid from watchdog.sms_target_member_link stml
	where stml.gid in (
		select gid from watchdog.sms_target_group stg 
		where 1=1
		and stg.status = 1
		and stg.activation = 1
		and stg.gid in (
			select group_id from watchdog.sms_target_group_link stgl where stgl.data_type = 'NQC' and site = 'JIA' and stgl.data_kind = 'TDWR' and stgl.agency_cd = 'KMA'
		) and name <> '전지점'
	)
);