/*
 * 문자 발송 내역 nuri2
 * - 카카오, 문자 분리 리스트
 * */
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
      ELSE ''      END) as MSG_STATE_NAME 
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
  , (CASE 
      WHEN MSG_TYPE = 'ALT' THEN '카카오' 
      WHEN MSG_TYPE = 'SMS' THEN '단문' 
      WHEN MSG_TYPE = 'MMS' THEN '장문' 
      ELSE '기타' 
    END) AS RSLT_TYPE_NAME
  , RSLT_EXPLA as RSLT_EXPLA  
  , SEND_RSLT_EXPLA as SEND_RSLT_EXPLA
-- 메시지유형 전송 우선순위 설정:  메시지 우선 순위를 ALT 또는 RCS로 하고 마지막 처리 순서로 문자(XMS)로 설정, 만약 문자(xMS)먼저 입력하면 문자로 바로 처리하고 종료 됨 
-- , MSG_TYPE_1='ALT' , CONTENTS_TYPE_1='ALT' 
-- , MSG_TYPE_2='SMS' , CONTENTS_TYPE_2='SMS' -- 또는  MSG_TYPE_3='MMS' , CONTENTS_TYPE_3='LMS' 
  , MSG_TYPE as MSG_TYPE          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
  , CONTENTS_TYPE as CONTENTS_TYPE     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
-- 메시지 내용 
  , SMS_TEXT as SMS_TEXT            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{'text':'입력 할 메시지 내용'}' 
FROM
	(SELECT
			ALT.MSG_KEY as MSG_KEY             -- # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용) 
		-- [성공]: MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') 
		-- [실패]: MSG_STATE='6' AND (RSLT_CODE<>'0' OR RSLT_CODE<>'1000') : 상태 코드 NURI2_NRMSG_RSLT 코드 조회 
		  , ALT.MSG_STATE as MSG_STATE           -- # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우 
		-- 처리별 날짜 
		  , ALT.INPUT_DATE as INPUT_DATE          -- # '메시지 입력시간', 실제로 메시지를 입력한 시간 
		  , ALT.RES_DATE as RES_DATE            -- # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능 
		-- 결과코드,   MSG_STATE='6' 성공, 실패등 결과과 확인 
		  , ALT.RSLT_CODE as RSLT_CODE           -- # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'), 
		  , ALT.RSLT_NET as RSLT_NET            -- # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO 
		  , ALT.RSLT_TYPE as RSLT_TYPE           -- # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS 
		  , (SELECT RSLT_EXPLA FROM NURI2.nuri2_nrmsg_rslt r where binary r.RSLT_CODE=ALT.RSLT_CODE and r.RSLT_TYPE = ALT.RSLT_TYPE) AS RSLT_EXPLA -- '결과코드 상세내용'  , PHONE as PHONE               -- # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
		  , (SELECT RSLT_EXPLA FROM NURI2.nuri2_nrmsg_rslt r where r.RSLT_CODE=ALT.ALT_RSLT_CODE and r.RSLT_TYPE IN ('ALT', 'COM')) AS SEND_RSLT_EXPLA -- 'kakao 결과코드 상세내용' 
		  , ALT.MSG_TYPE_1 as MSG_TYPE          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
		  , ALT.CONTENTS_TYPE_1 as CONTENTS_TYPE     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자  
		-- 메시지 내용
		  , ALT.ALT_JSON as SMS_TEXT            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{'text':'입력 할 메시지 내용'}' 
	FROM ( 
			SELECT * FROM nuri2.NURI2_NRMSG_DATA 
	--       WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') 
			UNION ALL
			SELECT * FROM nuri2.NURI2_NRMSG_LOG_202603
	--       WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') 
	     ) ALT
	WHERE 1=1 
	  AND ALT.MSG_TYPE_1 = 'ALT'
	  AND (:result = '' OR :result IS NULL ) 
	   OR (CASE WHEN :result = '1' THEN ALT.MSG_STATE = 6 AND ALT.RSLT_CODE IN ('0', '1000') 
	            WHEN :result = '0' THEN ALT.MSG_STATE='6' AND (ALT.RSLT_CODE<>'0' OR ALT.RSLT_CODE<>'1000') 
	            WHEN :result = '9' THEN ALT
	            .RSLT_CODE IS NULL 
	       END)
	UNION ALL
	SELECT 
			XMS.MSG_KEY as MSG_KEY             -- # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용) 
		-- [성공]: MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') 
		-- [실패]: MSG_STATE='6' AND (RSLT_CODE<>'0' OR RSLT_CODE<>'1000') : 상태 코드 NURI2_NRMSG_RSLT 코드 조회 
		  , XMS.MSG_STATE as MSG_STATE           -- # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우 
		-- 처리별 날짜 
		  , XMS.INPUT_DATE as INPUT_DATE          -- # '메시지 입력시간', 실제로 메시지를 입력한 시간 
		  , XMS.RES_DATE as RES_DATE            -- # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능 
		-- 결과코드,   MSG_STATE='6' 성공, 실패등 결과과 확인 
		  , XMS.RSLT_CODE as RSLT_CODE           -- # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'),  
		  , XMS.RSLT_NET as RSLT_NET            -- # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO 
		  , XMS.RSLT_TYPE as RSLT_TYPE           -- # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS  
		  , (SELECT RSLT_EXPLA FROM NURI2.nuri2_nrmsg_rslt r where binary r.RSLT_CODE=XMS.RSLT_CODE and r.RSLT_TYPE = XMS.RSLT_TYPE) AS RSLT_EXPLA -- '결과코드 상세내용'  , PHONE as PHONE               -- # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
		  , (SELECT RSLT_EXPLA FROM NURI2.nuri2_nrmsg_rslt r where binary r.RSLT_CODE=XMS.XMS_RSLT_CODE and r.RSLT_TYPE IN ('XMS', 'COM')) AS XMS_RSLT_EXPLA -- 'sms 결과코드 상세내용' 
		  , XMS.MSG_TYPE_2 as MSG_TYPE          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 
		  , XMS.CONTENTS_TYPE_2 as CONTENTS_TYPE     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자  
		-- 메시지 내용
		  , XMS_TEXT as SMS_TEXT            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{'text':'입력 할 메시지 내용'}' 
	FROM ( 
			SELECT * FROM nuri2.NURI2_NRMSG_DATA 
	--       WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') 
			UNION ALL
			SELECT * FROM nuri2.NURI2_NRMSG_LOG_202603
	--       WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') 
	 ) XMS
	WHERE 1=1 
	  AND XMS.MSG_TYPE_2 = 'SMS'
	  AND (:result = '' OR :result IS NULL ) 
	   OR (CASE WHEN :result = '1' THEN XMS.MSG_STATE = 6 AND XMS.RSLT_CODE IN ('0', '1000') 
	            WHEN :result = '0' THEN XMS.MSG_STATE='6' AND (XMS.RSLT_CODE<>'0' OR XMS.RSLT_CODE<>'1000') 
	            WHEN :result = '9' THEN XMS.RSLT_CODE IS NULL 
	       END) 
) MT
WHERE 1=1
  AND MT.RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959');
ORDER BY MT.RES_DATE desc, MT.MSG_KEY desc limit 15
