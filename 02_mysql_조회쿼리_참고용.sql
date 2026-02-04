  -- ### MT #########################################
  -- # MT ALT,SMS/MMS, RCS 공통쿼리
  -- ### MT #########################################
SELECT
	    'MT'  AS MSG
	  , MSG_KEY             -- # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용)
	  , SUB_ID              -- # 'SUB_ID(과금청구용)', [*][중요]절대로 임의사용 하지 마세요. 모바일메시지 운영과 협의된 기관만 가능 합니다.
	  -- 	자유롭게 사용가능 컬럼 입니다. 이외 추가가 필요하시면 nuri2.conf 에 컬럼 추가 옵션을 추가 하실 수 있습니다.
	  , USER_KEY            -- # '사용자 고유 번호'   , 자유롭게 사용가능 컬럼  컬럼이 더 필요하면 nuri2.conf에서  db_field를 추가 후 사용.
	  , USER_GROUP          -- # '사용자 그룹'        , 자유롭게 사용가능 컬럼
	  , USER_ID             -- # '사용자 고유 아이디' , 자유롭게 사용가능 컬럼
	  , USER_JOBID          -- # '사용자 JOB 아이디'  , 자유롭게 사용가능 컬럼
	  -- [조회불필요] , CENTER_KEY          -- # '발송라인' (변경불가)
	  , MSG_PRIORITY        -- # '메시지 전송 우선 순위'  - 일반은 기본값 '3', 긴급성은 '7'로 입력
	  -- 메시지 상태, 입력? 진행중? 완료 기준
	  , MSG_STATE           -- # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우
	  -- 처리별 날짜
	  , INPUT_DATE          -- # '메시지 입력시간', 실제로 메시지를 입력한 시간
	  , RES_DATE            -- # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능
	  , QUE_DATE            -- # '메시지를 수집 한 시간', 메시지 처리 과정 중 시간
	  , SENT_DATE           -- # '메시지를 전송(접수) 한 시간', 모바일메시지 통계기준일 입니다. 접수시간으로 문의 필요
	  , RSLT_DATE           -- # '핸드폰에 전달 된 시간', 이통사 결과 중  SKT SMS의 경우 초단위를 생략해서 결과 회신, 12:59:00
	  , REPORT_DATE         -- # 'G/W에서 결과를 수신한 시간', 모바일메시지서비스 G/W에서 결과를 받은 시간
	  -- 결과코드,   MSG_STATE='6' 성공, 실패등 결과과 확인
	  , RSLT_CODE           -- # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'), -- , (SELECT RSLT_EXPLA FROM nuri2_nrmsg_rslt r where r.RSLT_CODE=MT.RSLT_CODE ) AS '결과코드 상세내용'
	  , RSLT_NET            -- # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO
	  , RSLT_TYPE           -- # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS
	  , SENT_COUNT          -- # '결과처리 히스토리 재전송 횟수',  MSG_TYPE_1~3 재전 송시도 횟수 최종결과
	-- 입력 MSG_TYP1~3 처리순서와 결과코드 회신순서표시
	  , HISTORY_MSG_TYPE    -- # '결과처리 히스토리 발송 타입', ALT->RCS->XMS
	  , HISTORY_RSLT_CODE   -- # '결과처리 히스토리 상세코드', 9999->9999->d
	  -- [조회불필요] , IDENTIFIER          -- # '메시지 식별자코드'
	  , PHONE               -- # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
	  , CALLBACK            -- # '발신 번호', [*][중요]휴대폰번호 사용시에는 발신도용 해제 여부 필요, 스미싱 악용 방지용 '번호도용 차단서비스' 가입자는 해제 후 설정가능 합니다.
	  -- 메시지유형 전송 우선순위 설정:  메시지 우선 순위를 ALT 또는 RCS로 하고 마지막 처리 순서로 문자(XMS)로 설정, 만약 문자(xMS)먼저 입력하면 문자로 바로 처리하고 종료 됨
	  , MSG_TYPE_1          -- # '발송 타입 1번째'
	  , CONTENTS_TYPE_1     -- # '메시지 내용에 대한 타입 1번째'
	  , QUE_DATE_1          -- # '1번째 메시지 수집 한 시간'
	  , SENT_DATE_1         -- # '1번째 메시지 전송 한 시간'
	  , MSG_TYPE_2          -- # '발송 타입 2번째'
	  , CONTENTS_TYPE_2     -- # '메시지 내용에 대한 타입 2번째'
	  , QUE_DATE_2          -- # '2번째 메시지 수집 한 시간'
	  , SENT_DATE_2         -- # '2번째 메시지 전송 한 시간'
	  , MSG_TYPE_3          -- # '발송 타입 3번째'
	  , CONTENTS_TYPE_3     -- # '메시지 내용에 대한 타입 3번째'
	  , QUE_DATE_3          -- # '3번째 메시지 수집 한 시간'
	  , SENT_DATE_3         -- # '3번째 메시지 전송 한 시간'
	  -- 우선순위로 처리, 각 유형별 결과코드를 별도 저장.
	  , XMS_RSLT_CODE       -- # 'XMS 결과처리 상세코드'
	  , XMS_RSLT_NET        -- # 'XMS 결과처리 통신사'
	  , XMS_RSLT_DATE       -- # 'XMS 핸드폰에 전달 된 시간'
	  , XMS_REPORT_DATE     -- # 'XMS G/W에서 결과를 수신한 시간'
	  , ALT_RSLT_CODE       -- # 'ALT 결과처리 상세코드'
	  , ALT_RSLT_NET        -- # 'ALT 결과처리 통신사'
	  , ALT_RSLT_DATE       -- # 'ALT 핸드폰에 전달 된 시간'
	  , ALT_REPORT_DATE     -- # 'ALT G/W에서 결과를 수신한 시간'
	  , RCS_RSLT_CODE       -- # 'RCS 결과처리 상세코드'
	  , RCS_RSLT_NET        -- # 'RCS 결과처리 통신사'
	  , RCS_RSLT_DATE       -- # 'RCS 핸드폰에 전달 된 시간'
	  , RCS_REPORT_DATE     -- # 'RCS G/W에서 결과를 수신한 시간'
	  -- 메시지 내용 입력
	  , XMS_SUBJECT         -- # '메시지 타이틀(LMS/MMS)'
	  , XMS_TEXT            -- # '메시지 본문(SMS/LMS/MMS)'
	  , XMS_FILE_NAME_1     -- # '파일경로를 포함한 파일명1(MMS)'
	  , XMS_FILE_NAME_2     -- # '파일경로를 포함한 파일명2(MMS)'
	  , XMS_FILE_NAME_3     -- # '파일경로를 포함한 파일명3(MMS)'
	  , ALT_COUNTRY_CODE    -- # 'ALT 국가코드(ALT,ALI)', 해외번호로 메시지 발송시 입력, 기본값은 '82' 대한민국 입니다.
	  , ALT_SENDER_KEY      -- # 'ALT 사용자 아이디(ALT,ALI)', 발송키(발신 프로필키), 발송키는 채널을 의미합니다. 채널이 다르면 다른 발송키를 설정
	  , ALT_TEMPLATE_CODE   -- # 'ALT 등록된 템플릿 고유키(ALT,ALI)'
	  , ALT_JSON            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{"text":"입력 할 메시지 내용"}'
	  , RCS_BRAND_KEY       -- # 'RCS 브랜드 키'
	  , RCS_MESSAGE_BASE_ID -- # 'RCS 메시지베이스 아이디'
	  , RCS_JSON            -- # '메시지 본문(RCS)', JSON 형식, 필수 형식 '{"msg":{"body":{"description":"입력 할 메시지 내용"},"copyAllowed":true,"header":"0"}}'
 FROM (
        SELECT * FROM nuri2.NURI2_NRMSG_DATA
          WHERE  RES_DATE between '20241128000000' and  '20261128235959'
    UNION ALL 
        SELECT * FROM nuri2.nuri2_nrmsg_data_log
          WHERE  RES_DATE between '20241128000000' and  '20261128235959'
 	  ) MT
 ORDER BY RES_DATE desc, RES_DATE limit 0, 15;
  
  create table nuri2.nuri2_nrmsg_data_log
  as
  SELECT * FROM nuri2.NURI2_NRMSG_DATA
  ORDER BY RES_DATE desc, RES_DATE limit 0, 1;
  
  commit;
  
  select * from nuri2.nuri2_nrmsg_data_log;