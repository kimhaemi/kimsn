/* 
 * 26.01.16 nuri2 적용에 필요한 Query 
 */

-- 스키마 생성
create database nuri2;

drop database nuri2;

select * from watchdog.menu;

-- 누리2 메뉴 생성
INSERT INTO watchdog.menu
(id, menu_name, `path`, status, `depth`, `order`, created_at, updated_at)
VALUES(5112, '문자발송 nuri2', '/sms_send_nuri2', 1, 2, 11, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));

INSERT INTO watchdog.menu
(id, menu_name, `path`, status, `depth`, `order`, created_at, updated_at)
VALUES(5113, '문자 발송 내역 nuri2', '/sms_send_result_nuri2', 1, 2, 12, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'));

update watchdog.menu set
	`order` = 13
where id = 5111;

-- delete from watchdog.menu where id in (5112, 5113);

select * from watchdog.menu;

commit;

-- select DATE_FORMAT(NOW(), '%Y%m%d%H%i%s') from dual;

-- Error Code: 1418. This function has none of DETERMINISTIC, NO SQL, or READS SQL DATA in its declaration and binary logging is enabled (you *might* want to use the less safe log_bin_trust_function_creators variable)	0.000 sec 
-- 설정이 꺼져있어서 함수가 안만들어지는 것이였음
-- function 설정 꺼져있는지 확인
show global variables like 'log_bin_trust_function_creators';

-- OFF => ON 으로 변경 
SET GLOBAL log_bin_trust_function_creators = 1;
-- 또는
SET GLOBAL log_bin_trust_function_creators = 'ON';

commit;

/*
 * nuri2 Sequence table 만들기
 */
CREATE TABLE IF NOT EXISTS `nuri2`.`nuri2_sequence` (
  `seq_name` varchar(50) NOT NULL	      COMMENT '시퀀스명 ',
  `seq_currval` bigint(20) unsigned NOT NULL  COMMENT '현재 값 ',
  PRIMARY KEY (`seq_name`)
) ENGINE=InnoDB ;
-- -------------------------

select * from nuri2.nuri2_sequence;


/* 
 * FUNCTION nuri2 Sequence 만들기
 */ 

DELIMITER $$
CREATE FUNCTION nuri2.msg_nextval()
	RETURNS BIGINT UNSIGNED
	MODIFIES SQL DATA
	SQL SECURITY INVOKER
BEGIN
	INSERT INTO `nuri2_sequence`
	SET seq_name = 'nuri2', seq_currval=(@v_current_value:=1)
	ON DUPLICATE KEY
	UPDATE seq_currval=(@v_current_value:=seq_currval+1);
	RETURN @v_current_value;
END $$
DELIMITER ;

-- function 삭제
-- DROP FUNCTION msg_nextval ;
-- DELETE FUNCTION msg_nextval ;


-- function 만들어졌는지 확인
SELECT nuri2.msg_nextval();

select * from nuri2.nuri2_sequence;

commit;

-- 기본 테이블
-- nuri2_nrmsg_data
-- nuri2_nrmsg_log
-- nuri2_nrmsg_rslt
-- nuri2_nrmsg_duplex


SELECT 
    MSG_KEY as MSG_KEY             -- # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용) 
-- [성공]: MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') 
-- [실패]: MSG_STATE='6' AND (RSLT_CODE<>'0' OR RSLT_CODE<>'1000') : 상태 코드 NURI2_NRMSG_RSLT 코드 조회 
  , MSG_STATE as MSG_STATE           -- # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우 
  , (CASE 
      WHEN MT.MSG_STATE = 1 THEN '전송대기' 
      WHEN MT.MSG_STATE = 3 THEN '전송수집중' 
      WHEN MT.MSG_STATE = 5 THEN '전송' 
      WHEN MT.MSG_STATE = 6 THEN '처리완료' 
      ELSE '' END) as MSG_STATE_NAME 
-- 처리별 날짜 
  , INPUT_DATE as INPUT_DATE          -- # '메시지 입력시간', 실제로 메시지를 입력한 시간 
  , RES_DATE as RES_DATE            -- # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능 
-- 결과코드,   MSG_STATE='6' 성공, 실패등 결과과 확인 
  , RSLT_CODE as RSLT_CODE           -- # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'), 
  , (CASE 
      WHEN MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') THEN '성공' 
      WHEN RSLT_CODE is NULL THEN '' 
      ELSE '실패' END) as RSLT_CODE_NAME 
  , RSLT_NET as REST_NET            -- # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO 
  , RSLT_TYPE as RSLT_TYPE           -- # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS 
  , (case 
      when RSLT_TYPE = 'ALT' then '카카오' 
      when RSLT_TYPE = 'SMS' then '단문' 
      when RSLT_TYPE = 'MMS' then '장문' 
      else '기타' 
    end) as RSLT_TYPE_NAME 
  , (SELECT RSLT_EXPLA FROM NURI2.nuri2_nrmsg_rslt r where r.RSLT_CODE=MT.RSLT_CODE and r.RSLT_TYPE = mt.RSLT_TYPE) AS RSLT_EXPLA -- '결과코드 상세내용' 
  , PHONE as PHONE               -- # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX 
-- 메시지유형 전송 우선순위 설정:  메시지 우선 순위를 ALT 또는 RCS로 하고 마지막 처리 순서로 문자(XMS)로 설정, 만약 문자(xMS)먼저 입력하면 문자로 바로 처리하고 종료 됨 
-- , MSG_TYPE_1='ALT' , CONTENTS_TYPE_1='ALT' 
-- , MSG_TYPE_2='SMS' , CONTENTS_TYPE_2='SMS' -- 또는  MSG_TYPE_3='MMS' , CONTENTS_TYPE_3='LMS' 
  , MSG_TYPE_1 as MSG_TYPE          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
  , CONTENTS_TYPE_1 as CONTENTS_TYPE_1     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
-- 메시지 내용 입력 
  , XMS_SUBJECT as XMS_SUBJECT         -- # '메시지 타이틀(LMS/MMS)' 
  , XMS_TEXT as XMS_TEXT           -- # '메시지 본문(SMS/LMS/MMS)' 
  , ALT_JSON as ALT_JSON            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{'text':'입력 할 메시지 내용'}' 
FROM ( 
			SELECT * FROM nuri2.NURI2_NRMSG_DATA 
      WHERE RES_DATE between :startDate and :endDate
			UNION ALL
			SELECT * FROM nuri2.NURI2_NRMSG_DATA_LOG 
      WHERE RES_DATE between :startDate and :endDate
     ) MT 
WHERE 1=1 
  AND (:smsResult = '' OR :smsResult IS NULL ) 
   OR (case when :smsResult = '1' then MT.MSG_STATE = 6 and mt.RSLT_CODE IN ('0', '1000') 
            when :smsResult = '0' then MT.RSLT_CODE NOT IN ('0', '1000') or MT.RSLT_CODE IS NULL 
       end) 
ORDER BY RES_DATE desc 
, MSG_KEY desc; limit 0, 15
;

case when null is not null and 6 = 6  then MT.MSG_STATE = 6 and mt.RSLT_CODE not in ('0', '1000')
-- 	 else
end

select * from NURI2.NURI2_NRMSG_RSLT where RSLT_CODE in (0, 1000);

 SELECT count(*) as cnt 
FROM ( 
  SELECT * 
    FROM ( 
			SELECT * FROM nuri2.NURI2_NRMSG_DATA 
      WHERE RES_DATE between :startDate and :endDate
			UNION ALL
			SELECT * FROM nuri2.NURI2_NRMSG_DATA_LOG 
      WHERE RES_DATE between :startDate and :endDate
    ) MT 
  WHERE 1=1 
    AND (:smsResult = '' OR :smsResult IS NULL ) 
     OR (case when :smsResult = '1' then MT.MSG_STATE = 6 and mt.RSLT_CODE IN ('0', '1000') 
              when :smsResult = '0' then MT.RSLT_CODE NOT IN ('0', '1000') or MT.RSLT_CODE IS NULL 
       end) 
) total ;

SELECT * FROM nuri2.NURI2_NRMSG_DATA MT
-- where MSG_key = 1500
where 1=1
and MT.RSLT_CODE NOT IN ('0', '1000') or MT.RSLT_CODE IS NULL
order by RSLT_DATE desc, MSG_KEY desc;

SELECT * 
    FROM ( 
			SELECT * FROM nuri2.NURI2_NRMSG_DATA 
      WHERE RES_DATE between :startDate and :endDate
			UNION ALL
			SELECT * FROM nuri2.NURI2_NRMSG_DATA_LOG 
      WHERE RES_DATE between :startDate and :endDate
    ) MT 
  WHERE 1=1 
    AND (:smsResult = '' OR :smsResult IS NULL )
     OR (case when :smsResult = '1' then MT.MSG_STATE = 6 and mt.RSLT_CODE IN ('0', '1000') 
              when :smsResult = '0' then MT.RSLT_CODE NOT IN ('0', '1000') or MT.RSLT_CODE IS NULL 
       end)


insert into nuri2.nuri2_nrmsg_data_log 
SELECT * FROM nuri2.NURI2_NRMSG_DATA 
where MSG_key = 1500;

select 
MSG_STATE
, RES_DATE
, RSLT_CODE
, RSLT_NET
, RSLT_TYPE
from nuri2.nuri2_nrmsg_data_log nndl ;

update nuri2.nuri2_nrmsg_data_log set
MSG_STATE = 6
, RES_DATE = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s')
, RSLT_CODE = '1000'
, RSLT_NET = 'SKT'
, RSLT_TYPE = 'SMS'
where MSG_KEY = 1500
;

commit;

