접속: mysql -u root -p
비밀번호: Watch1234!

select * from watchdog.sms_target_group_link; -- agency_cd 확인
select * from watchdog.sms_target_group; -- agency_cd, gubun 확인

-- 없으면 실행
ALTER TABLE watchdog.sms_target_group_link add agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.sms_target_group add agency_cd varchar(4) NULL COMMENT '소속기관코드 (KMA:기상청, MCEE:기후부)';
ALTER TABLE watchdog.sms_target_group add gubun tinyint(1) DEFAULT NULL COMMENT '0:ALL, 1:대형, 2:소형, 3:공항';

commit;

-- 1번 그룹
update watchdog.sms_target_group set
gubun = 0
where 1=1
and (name like '%그룹%' or name like '%전지점%' or name like '%ADMIN%');

-- 2번 기상청A 대형 
update watchdog.sms_target_group set
agency_cd = 'KMA',
gubun = 2
where 1=1
and name in ('백령도', '광덕산', '강릉', '고산', '진도', '오성산', '관악산', '면봉산', '구덕산', '성산', '용인');

-- 3번 기상청A 소형
update watchdog.sms_target_group set
agency_cd = 'KMA',
gubun = 2
where 1=1
and name in ('망일산', '덕적도', '수리산');

-- 4번 기상청A 공항
update watchdog.sms_target_group set
agency_cd = 'KMA',
gubun = 3
where 1=1
and name in ('제주공항', '인천공항');

-- 5번 기상청B 대형
update watchdog.sms_target_group set
agency_cd = 'MCEE',
gubun = 1
where 1=1
and name in ('비슬산', '감악산', '가리산', '모후산', '소백산', '서대산', '예봉산');

-- 6번 기상청B 소형
update watchdog.sms_target_group set
agency_cd = 'MCEE',
gubun = 2
where 1=1
and name in ('부산EDC', '부산', '청주', '대전', '광주', '전주', '삼척', '세종', '통고산', '임진강', '울산');

-- 7번 기상청A
update watchdog.sms_target_group_link set
agency_cd = 'KMA'
where 1=1
and site in ('BRI', 'GDK', 'GNG', 'MYN','PSN','KSN', 'JNI', 'GSN', 'SSP', 'YIT', 'MIL', 'SRI', 'DJK');

-- 8번 기상청B
update watchdog.sms_target_group_link set
agency_cd = 'MCEE'
where 1=1
and site in ('BSL','SBS','GRS','YBS','GAS','SDS','MHS','SAC','TGS','GJU','DJN','BSN','BSE','SEJ','JJU','USN','TSB','CHU');

commit;

select * from watchdog.receive_condition rc 
where rc.site = 'JIA';

update watchdog.receive_condition rc set
sms_send = 0
where rc.site = 'JIA'
;

commit;