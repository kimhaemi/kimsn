-- ### MT #########################################
-- # MT ALT,SMS/MMS, RCS 공통쿼리
-- ### MT #########################################
-- 조회 페이지 개발시 필수컬럼 정보
-- [필수]: 결과 조회시 확인 해야할 값
-- [선택]: 기관 조회시 선택적으로 표시해야할 값
--


###########################################################################################################################################################
#** 결과코드 분석 : MSG_STATE, RSLT_CODE
###########################################################################################################################################################
1. 메시지 상태(MSG_STATE) 안내
-----------------------------------------
* MSG_STATE = 1 : [입력] : 메시지 입력 상태, 메시지 입력 기본 값 다른값 입력시 아무런 동작을 하지않음
* MSG_STATE = 3 : [큐잉] : 메시지를 Queue에 적재, 발송처리 대상 조회 후 발송 QUEUE에 적재 한 상태
* MSG_STATE = 5 : [대기] : 메시지 G/W로 접수완료, 발송 결과 대기 상태
 - xMS(문자:SMS/MMS)로 처리된 경우 대기 시간이 SMS는 24시간, MMS(LMS):72시간 대기 시간이 필요함.
 - 이통사로부터 결과가 아직 회신되지 않은 상태이므로 성공도 아니고, 실패도 아님
* MSG_STATE= 6 : [완료] : 이통사 및 GateWay로 부터 결과 회시, 성공 또는 실패등 결과가 확정됨
-----------------------------------------
2. 결과 분석 : 성공 실패는 'MSG_STATE' 와 'RSLT_CODE' 값을 조합해서 분석이 필요
-----------------------------------------
* 성공은 아래와 같습니다.
예) [성공]: MSG_STATE='6' AND RSLT_CODE IN ('0','1000')

* 실패는 아래와 같습니다.
예) [실패]: MSG_STATE='6' AND RSLT_CODE NOT IN ('0','1000')
  - RSLT_CODE는 결과코드테이블( NRMSG_RSLT OR NURI2_NRMSG_RSLT) 코드 조회

* 이외 진행중(결과대기) 성공도 실패도 아님
예) [결과대기]: MSG_STATE='5' 마지막 메시지 Type에 대해
  - 메시지 상태 조회가 필요함.
* 참고용 SQL입니다.(mysql용)
  설명:  nuri2_nrmsg_data 테이블에 현재 메시지의 상태를 조회,
  MSG_STATE, RSLT_CODE 를 확인.
  MSG_REAL_STATE 현재상태 확인 LAST_MSG_TYPE로 마지막 처리된 메시지

	SELECT
	  MSG_STATE
	, (CASE MSG_STATE WHEN 1 THEN '입력' WHEN 3 THEN '수집' WHEN 5 THEN '결과대기' ELSE '처리완료' END) AS  MSG_REAL_STATE -- 서비스 상태
	, RSLT_CODE
	, (SELECT RSLT_EXPLA FROM nuri2_nrmsg_rslt r where r.RSLT_CODE=MT.RSLT_CODE) AS RSLT_TEXT -- 결과 코드 상세 내용.
	, (CASE SENT_COUNT WHEN 1 THEN MSG_TYPE_1 WHEN 2 THEN MSG_TYPE_2 WHEN 3 THEN MSG_TYPE_3 ELSE MSG_TYPE_1 END) AS LAST_MSG_TYPE -- 마지막 전송된 메시지
	FROM nuri2_nrmsg_data MT


-----------------------------------------
3. 결과코드 분석 방법[기타] :  MSG_STATE=6 인경우 RSLT_CODE 자릿 수로 분석진행
-----------------------------------------
 * RSLT_CODE는 3자리코드, 1리 코드, 4자리 코드등이 있습니다
 1) LENGTH(RSLT_CODE)=3 : 3자리 코드
  - 1XX, 2XX, 3XX, 4XX, 5XX, 9XX가 있습니다. 이 경우는 누리2 내부실패로 발송이되지 않은 상태
 2) LENGTH(RSLT_CODE)=1 : 1자리 코드
  - xMS(SMS/MMS/LMS)로 처리된 결과, 이통사로부터 회신된 결과 코드입니다.
 3) LENGTH(RSLT_CODE)=4 : 4자리 코드
  - 알림톡(ALT, 카카오) 및 RCS 서버로 부터 받은 결과코드
###########################################################################################################################################################

###########################################################################################################################################################
##★유의사항 : 제발 읽어보시고 확인 하세요.
###########################################################################################################################################################
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
===================================================================================
* 반드시 기관내부 결과 조회 후 문의 요청
[★]1.DB 상태값 확인
 - MSG_KEY, MSG_STATE , RSLT_CODE, MSG_TYPE_1~3 등 확인등 각 상태 값을 조회 하고 문의 하세요.
[★]2.발송로그 확인
 * MMS G/W 인증 실패: 연동계정, 패스워드, IP 정보 확인
 1) Unknown Id : 존재 하지 않은 계정, 계정 생성 안됨.
        [2025-03-04 14:25:37.022][INFO ][Nuri2Receiver???] READY TO CREATE SOCKET[10.xxx.xxx.235:20000]
        [2025-03-04 14:25:37.076][INFO ][Nuri2Receiver???] SUCCESS TO CREATE SOCKET
        [2025-03-04 14:25:37.100][ERROR][Nuri2Receiver???] M-Gov GATEWAY CONNECT FAIL -> CODE[200] DATA[Unknown Id]
 2) Invalid password : 패스워드 틀림, 영문의 경우 소문자로만 사용가능
        [2025-03-04 14:32:40.896][INFO ][Nuri2Receiver???] READY TO CREATE SOCKET[10.xxx.xxx.235:40000]
        [2025-03-04 14:32:40.944][INFO ][Nuri2Receiver???] SUCCESS TO CREATE SOCKET
        [2025-03-04 14:32:40.993][ERROR][Nuri2Receiver???] M-Gov GATEWAY CONNECT FAIL -> CODE[200] DATA[Invalid password]
 3) Unknown IP : 접속허용 IP가 아님, 계정=IP 같아야 함.
        [2025-03-04 14:32:40.896][INFO ][Nuri2Receiver???] READY TO CREATE SOCKET[10.xxx.xxx.235:40000]
        [2025-03-04 14:32:40.944][INFO ][Nuri2Receiver???] SUCCESS TO CREATE SOCKET
        [2025-03-04 14:32:40.993][ERROR][Nuri2Receiver???] M-Gov GATEWAY CONNECT FAIL -> CODE[200] DATA[Unknown IP]
[★]3.수신번호 및 발신시간
 - 수신번호 확인, 01x 시작하는 11자리 형식인지 확인
 - 발신번호 확인, 지역번호 포함 전화번호, 01x 시작하는 11자리 형식인지 확인
 - 요청시간(RES_DATE) 확인

[★]4.관리원에 문의
  - 위의 3가지 항목 확인시
===================================================================================
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
**[★★★] 메시지 전송 오류 문의시  유의사항[★★★]
###########################################################################################################################################################

###########################################################################################################################################################
[NURI2_NRMSG_RSLT]결과코드테이블 [RSLT_TYPE] 설명
###########################################################################################################################################################
'XMS' = XMS(문자) Gateway 결과 일때
'ALT' = ALT(카카오) Gateway 결과 일때
'RCS' = RCS Gateway 결과
'CON' = Connection(연결) 시도 중 발생
'SED' = Sending(접수) 중 발생
'AGT' = Nuri2(자체) 내부에러
'COM' = Complete(처리완료) 결과 완료
###########################################################################################################################################################




-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --

-- ##############################################
-- # MT ALT,SMS/MMS, RCS 공통쿼리 Ver2 Simple
-- ##############################################

SELECT
  'MT'  AS MSG
, MSG_KEY             -- [필수] # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용)
-- [조회불필요] , CENTER_KEY          -- # '발송라인' (변경불가)
, MSG_PRIORITY        -- # '메시지 전송 우선 순위'  - 일반은 기본값 '3', 긴급성은 '7'로 입력
-- 메시지 상태, 입력? 진행중? 완료 기준
, MSG_STATE           -- [필수] # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송중(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우
-- 현재 메시지 상태를 표시
, (CASE MSG_STATE WHEN 1 THEN '입력' WHEN 3 THEN '수집' WHEN 5 THEN '결과대기' ELSE '완료' END) AS  MSG_REAL_STATE
, RSLT_NET            -- [필수] # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO
-- 마지막으로 처리된 메시지
, (CASE SENT_COUNT WHEN 1 THEN CONTENTS_TYPE_1 WHEN 2 THEN CONTENTS_TYPE_2 WHEN 3 THEN CONTENTS_TYPE_3 ELSE CONTENTS_TYPE_1 END) AS LAST_MSG_TYPE
--, RSLT_TYPE           -- [필수] # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS

-- 결과코드
, (CASE WHEN MSG_STATE=6 THEN RSLT_CODE   ELSE '' END) AS RSLT_CODE -- [필수] # ' 최종 결과코드 *결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'),
, (CASE WHEN MSG_STATE=6 THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.RSLT_CODE) ELSE '' END) AS RSLT_TEXT -- " 최종 결과코드 상세내용"

-- 컨텐츠별 결과코드
, ALT_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN ALT_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.ALT_RSLT_CODE) ELSE '' END) AS ALT_RSLTSLT_TEXT -- "ALT 결과코드 상세내용"
, XMS_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN XMS_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.XMS_RSLT_CODE) ELSE '' END) AS XMS_RSLTSLT_TEXT -- "XMS 결과코드 상세내용"
, RCS_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN RCS_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.RCS_RSLT_CODE) ELSE '' END) AS RCS_RSLTSLT_TEXT -- "RCS 결과코드 상세내용"

-- 입력 MSG_TYP1~3 처리순서와 결과코드 회신순서표시
, HISTORY_MSG_TYPE    -- [필수] # '결과처리 히스토리 발송 타입', ALT->RCS->XMS
, HISTORY_RSLT_CODE   -- [필수] # '결과처리 히스토리 상세코드', 9999->9999->d


-- 전화번호 : 발/수신번호
, PHONE               -- [필수][선택] # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
, CALLBACK            -- [필수][선택] # '발신 번호', [*][중요]휴대폰번호 사용시에는 발신도용 해제 여부 필요, 스미싱 악용 방지용 '번호도용 차단서비스' 가입자는 해제 후 설정가능 합니다.

-- 처리 날짜시간
, INPUT_DATE          --        # '메시지 입력시간', 실제로 메시지를 입력한 시간
, RES_DATE            -- [필수] # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능
, QUE_DATE            --        # '메시지를 수집 한 시간', 메시지 처리 과정 중 시간
, SENT_DATE           -- [필수] # '메시지를 전송(접수) 한 시간', 모바일메시지 접수(통계일자)기준 입니다. 접수시간으로 문의 필요
, RSLT_DATE           --        # '핸드폰에 전달 된 시간', 이통사 결과 중  SKT SMS의 경우 초단위를 생략해서 결과 회신, 12:59:00
, REPORT_DATE         --        # 'G/W에서 결과를 수신한 시간', 모바일메시지서비스 G/W에서 결과를 받은 시간

-- 메시지 내용 - 결과 및 규격 준수 여부를 확인 하기위해 메시지 내용도 표시하기를 권장.
, XMS_SUBJECT         -- # '메시지 타이틀(LMS/MMS)'
, XMS_TEXT            -- [선택] # '메시지 본문(SMS/LMS/MMS)'

, ALT_COUNTRY_CODE    -- [선택] # 'ALT 국가코드(ALT,ALI)', 해외번호로 메시지 발송시 입력, 기본값은 '82' 대한민국 입니다.
, ALT_SENDER_KEY      -- [선택] # 'ALT 사용자 아이디(ALT,ALI)', 발송키(발신 프로필키), 발송키는 채널을 의미합니다. 채널이 다르면 다른 발송키를 설정
, ALT_TEMPLATE_CODE   -- [선택] # 'ALT 등록된 템플릿 고유키(ALT,ALI)'
, ALT_JSON            -- [선택] # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{"text":"입력 할 메시지 내용"}'

, RCS_BRAND_KEY       -- [선택] # 'RCS 브랜드 키'
, RCS_MESSAGE_BASE_ID -- [선택] # 'RCS 메시지베이스 아이디'
, RCS_JSON            -- [선택] # '메시지 본문(RCS)', JSON 형식, 필수 형식 '{"msg":{"body":{"description":"입력 할 메시지 내용"},"copyAllowed":true,"header":"0"}}' //--반드시 이 형식을 지켜야 합니다.

FROM (
        -- 배포자는 발송량이 많지 않아서 로그테이블을 단일(One) 테이블로 사용
        SELECT * FROM nuri2_nrmsg_data
        WHERE RES_DATE between '20260501000000' and  '20260513235959'
        -- AND  RSLT_TYPE='SMS'
        UNION ALL
        SELECT * FROM nuri2_nrmsg_log
        WHERE RES_DATE between '20260501000000' and  '20260513235959'
        -- AND  RSLT_TYPE='SMS'
) MT
ORDER BY RES_DATE DESC
-- ##############################################
-- # MT ALT,SMS/MMS, RCS 공통쿼리 Ver2 Simple
-- ##############################################







-- ##############################################
-- # MT ALT,SMS/MMS, RCS 공통쿼리 Ver.1
-- ##############################################

SELECT
  'MT'  AS MSG
-- ---------------------------------------------------------------------------------
-- 주요정보 ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼
-- ---------------------------------------------------------------------------------
, MSG_KEY             -- [필수] # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용)
-- 빌링전용계정 사용기관만 - 모바일메시지서비스 와 협의된 기관만 사용
, SUB_ID              -- [선택] # 'SUB_ID(과금청구용)', [*][중요]절대로 임의사용 하지 마세요. 모바일메시지 운영과 협의된 기관만 가능 합니다.

-- 자유롭게 사용가능 컬럼 입니다. 이외 추가가 필요하시면 nuri2.conf 에 컬럼 추가 옵션을 추가 하실 수 있습니다.
, USER_KEY            -- # '사용자 고유 번호'   , 자유롭게 사용가능 컬럼  컬럼이 더 필요하면 nuri2.conf에서  db_field를 추가 후 사용.
, USER_GROUP          -- # '사용자 그룹'        , 자유롭게 사용가능 컬럼
, USER_ID             -- # '사용자 고유 아이디' , 자유롭게 사용가능 컬럼
, USER_JOBID          -- # '사용자 JOB 아이디'  , 자유롭게 사용가능 컬럼

-- [조회불필요] , CENTER_KEY          -- # '발송라인' (변경불가)
, MSG_PRIORITY        -- # '메시지 전송 우선 순위'  - 일반은 기본값 '3', 긴급성은 '7'로 입력

-- 메시지 상태, 입력? 진행중? 완료 기준
, MSG_STATE           -- [필수] # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송중(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우

-- 현재 메시지 상태를 표시
, (CASE MSG_STATE WHEN 1 THEN '입력' WHEN 3 THEN '수집' WHEN 5 THEN '결과대기' ELSE '완료' END) AS  MSG_REAL_STATE


--  [RSLT_CODE: 결과코드 안내]
--  [누리2 내부에러]* 3자리 코드:1XX, 2XX, 3XX, 4XX, 5XX, 9XX 이 있음, 9XX 코드는 모바일메시지서비스 메시지 G/W
--  [메시지 GateWay]* 1자리 코드: XMS(SMS/MMS) 코드이며 G/W 또는 이통사에서 회신된 결과 코드
--  [메시지 GateWay]* 4자리 코드: 카카오, RCS 결과코드
-- 결과코드 MSG_STATE=6 일때 결과코드 조회
, (CASE WHEN MSG_STATE=6 THEN RSLT_CODE   ELSE '' END) AS RSLT_CODE -- [필수] # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'),
, (CASE WHEN MSG_STATE=6 THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.RSLT_CODE) ELSE '' END) AS RSLT_TEXT -- "결과코드 상세내용"

-- 컨텐츠별 결과코드
, ALT_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN ALT_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.ALT_RSLT_CODE) ELSE '' END) AS ALT_RSLTSLT_TEXT -- "결과코드 상세내용"
, XMS_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN XMS_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.XMS_RSLT_CODE) ELSE '' END) AS XMS_RSLTSLT_TEXT -- "결과코드 상세내용"
, RCS_RSLT_CODE  -- ALT(알림톡) 결과코드
, (CASE WHEN RCS_RSLT_CODE IS NOT NULL THEN (SELECT RSLT_EXPLA FROM nrmsg_rslt r where r.RSLT_CODE=MT.RCS_RSLT_CODE) ELSE '' END) AS RCS_RSLTSLT_TEXT -- "결과코드 상세내용"


, RSLT_NET            -- [필수] # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO
, RSLT_TYPE           -- [필수] # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS
, SENT_COUNT          -- # '결과처리 히스토리 재전송 횟수',  MSG_TYPE_1~3 재전 송시도 횟수 최종결과

-- [*]중요 MSG_TYPE_1~3 수행시 SENT_COUNT 확인이 필요.
-- 마지막으로 전송시도 한 메시지 TYPE
, (CASE SENT_COUNT WHEN 1 THEN CONTENTS_TYPE_1 WHEN 2 THEN CONTENTS_TYPE_2 WHEN 3 THEN CONTENTS_TYPE_3 ELSE CONTENTS_TYPE_1 END) AS LAST_MSG_TYPE

-- , (CASE SENT_COUNT WHEN 1 THEN MSG_TYPE_1 WHEN 2 THEN MSG_TYPE_2 WHEN 3 THEN MSG_TYPE_3 ELSE MSG_TYPE_1 END) AS LAST_MSG_TYPE


-- 입력 MSG_TYP1~3 처리순서와 결과코드 회신순서표시
, HISTORY_MSG_TYPE    -- [필수] # '결과처리 히스토리 발송 타입', ALT->RCS->XMS
, HISTORY_RSLT_CODE   -- [필수] # '결과처리 히스토리 상세코드', 9999->9999->d

-- [조회불필요] , IDENTIFIER          -- # '메시지 식별자코드'
, PHONE               -- [필수][선택] # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX
, CALLBACK            -- [필수][선택] # '발신 번호', [*][중요]휴대폰번호 사용시에는 발신도용 해제 여부 필요, 스미싱 악용 방지용 '번호도용 차단서비스' 가입자는 해제 후 설정가능 합니다.

-- 처리별 날짜
, INPUT_DATE          --        # '메시지 입력시간', 실제로 메시지를 입력한 시간
, RES_DATE            -- [필수] # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능
, QUE_DATE            --        # '메시지를 수집 한 시간', 메시지 처리 과정 중 시간
, SENT_DATE           -- [필수] # '메시지를 전송(접수) 한 시간', 모바일메시지 통계기준일 입니다. 접수시간으로 문의 필요
, RSLT_DATE           --        # '핸드폰에 전달 된 시간', 이통사 결과 중  SKT SMS의 경우 초단위를 생략해서 결과 회신, 12:59:00
, REPORT_DATE         --        # 'G/W에서 결과를 수신한 시간', 모바일메시지서비스 G/W에서 결과를 받은 시간


-- ---------------------------------------------------------------------------------
-- 주요정보 ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
-- ---------------------------------------------------------------------------------

-- 메시지유형 전송 우선순위 설정:  메시지 우선 순위를 ALT 또는 RCS로 하고 마지막 처리 순서로 문자(XMS)로 설정, 만약 문자(xMS)먼저 입력하면 문자로 바로 처리하고 종료 됨
-- [중요] ALT, RCS 발송기관은 반드시 MSG_TYPE_2 이상 출력.
, MSG_TYPE_1          -- [필수]# '발송 타입 1번째'
, CONTENTS_TYPE_1     -- [필수]# '메시지 내용에 대한 타입 1번째'
, QUE_DATE_1          -- [필수]# '1번째 메시지 수집 한 시간'
, SENT_DATE_1         -- [필수]# '1번째 메시지 전송 한 시간'

-- [중요] ALT, RCS, xMS 발송기관은 반드시 MSG_TYPE_2 이상 출력.
, MSG_TYPE_2          -- [필수][선택] # '발송 타입 2번째'
, CONTENTS_TYPE_2     -- [필수][선택] # '메시지 내용에 대한 타입 2번째'
, QUE_DATE_2          -- [필수][선택] # '2번째 메시지 수집 한 시간'
, SENT_DATE_2         -- [필수][선택] # '2번째 메시지 전송 한 시간'

-- [중요] ALT와 RCS, xMS까지 보내는 기관은 발송기관은 반드시 MSG_TYPE_2, MSG_TYPE_3를 출력.
, MSG_TYPE_3          -- [필수][선택] # '발송 타입 3번째'
, CONTENTS_TYPE_3     -- [필수][선택] # '메시지 내용에 대한 타입 3번째'
, QUE_DATE_3          -- # '3번째 메시지 수집 한 시간'
, SENT_DATE_3         -- # '3번째 메시지 전송 한 시간'

-- 우선순위로 처리, 각 유형별 결과코드를 별도 저장.
-- , XMS_RSLT_CODE       -- [선택]# 'XMS 결과처리 상세코드'
, XMS_RSLT_NET        -- [선택]# 'XMS 결과처리 통신사'
, XMS_RSLT_DATE       -- # 'XMS 핸드폰에 전달 된 시간'
, XMS_REPORT_DATE     -- # 'XMS G/W에서 결과를 수신한 시간'

-- , ALT_RSLT_CODE       -- [선택] # 'ALT 결과처리 상세코드'
, ALT_RSLT_NET        -- [선택] # 'ALT 결과처리 통신사'
, ALT_RSLT_DATE       -- # 'ALT 핸드폰에 전달 된 시간'
, ALT_REPORT_DATE     -- # 'ALT G/W에서 결과를 수신한 시간'

-- , RCS_RSLT_CODE       -- [선택] # 'RCS 결과처리 상세코드'
, RCS_RSLT_NET        -- [선택] # 'RCS 결과처리 통신사'
, RCS_RSLT_DATE       -- # 'RCS 핸드폰에 전달 된 시간'
, RCS_REPORT_DATE     -- # 'RCS G/W에서 결과를 수신한 시간'

-- 메시지 내용 - 결과 및 규격 준수 여부를 확인 하기위해 메시지 내용도 표시하기를 권장.
, XMS_SUBJECT         -- # '메시지 타이틀(LMS/MMS)'
, XMS_TEXT            -- [선택] # '메시지 본문(SMS/LMS/MMS)'
, XMS_FILE_NAME_1     -- # '파일경로를 포함한 파일명1(MMS)'
, XMS_FILE_NAME_2     -- # '파일경로를 포함한 파일명2(MMS)'
, XMS_FILE_NAME_3     -- # '파일경로를 포함한 파일명3(MMS)'

, ALT_COUNTRY_CODE    -- [선택] # 'ALT 국가코드(ALT,ALI)', 해외번호로 메시지 발송시 입력, 기본값은 '82' 대한민국 입니다.
, ALT_SENDER_KEY      -- [선택] # 'ALT 사용자 아이디(ALT,ALI)', 발송키(발신 프로필키), 발송키는 채널을 의미합니다. 채널이 다르면 다른 발송키를 설정
, ALT_TEMPLATE_CODE   -- [선택] # 'ALT 등록된 템플릿 고유키(ALT,ALI)'
, ALT_JSON            -- [선택] # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{"text":"입력 할 메시지 내용"}'

, RCS_BRAND_KEY       -- [선택] # 'RCS 브랜드 키'
, RCS_MESSAGE_BASE_ID -- [선택] # 'RCS 메시지베이스 아이디'
, RCS_JSON            -- [선택] # '메시지 본문(RCS)', JSON 형식, 필수 형식 '{"msg":{"body":{"description":"입력 할 메시지 내용"},"copyAllowed":true,"header":"0"}}' //--반드시 이 형식을 지켜야 합니다.

FROM (
        -- 배포자는 발송량이 많지 않아서 로그테이블을 단일(One) 테이블로 사용
        SELECT * FROM nuri2_nrmsg_data
        WHERE RES_DATE between '20241128000000' and  '20241128235959'
        UNION ALL
        SELECT * FROM nuri2_nrmsg_log
        WHERE RES_DATE between '20241128000000' and  '20241128235959'
) MT

-- ##############################################
-- # MT ALT,SMS/MMS, RCS 공통쿼리 Ver.1
-- ##############################################

-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --
-- ######################################################################################################################################################################### --




-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- 참고용 : 통계 쿼리
-- * XMS/ALT/RCS 통계 조회
-- * 특정기간 또는 해당월 데이터 접수건수와, 성공, 실패 구분
-- * NURI2_NRMSG_DATA 테이블과 NURI2_NRMSG_LOG(_YYYYMM) 테이블 UNION
-- * SENT_DATE: 실제 모바일메시지로 접수한 시간, RES_DATE: 자체 접수시간
-- 성공: SMS/MMS/ALT/RCS 성공
-- 실패: SMS/MMS/ALT/RCS 실패
-- 대기: XMS(SMS/MMS)의 경우 이통사 결과 대기시간이 있음, 최대 72시간 미경과 데이터
-- ================================================================================================== --
SELECT
  SUM(1)                                                                                                                             AS ALL_TOTAL    -- 전체 접수
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')                                                       THEN 1 ELSE 0 END) AS ALL_SUCCESS  -- 전체 성공
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE NOT IN ('0','1000')                                                   THEN 1 ELSE 0 END) AS ALL_FAIL     -- 전체 실패
, SUM(CASE WHEN MSG_STATE<>'6'                                                                                    THEN 1 ELSE 0 END) AS ALL_WAIT     -- 전체 대기
-- xMS 통계
, SUM(CASE WHEN RSLT_TYPE='XMS'                                                                                   THEN 1 ELSE 0 END) AS XMS_TOTAL
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS'                               THEN 1 ELSE 0 END) AS XMS_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE NOT IN ('0','1000') AND RSLT_TYPE='XMS'                               THEN 1 ELSE 0 END) AS XMS_FAIL
, SUM(CASE WHEN (MSG_STATE<>'6' OR MSG_STATE IS NULL  )         AND SUBSTR(MSG_TYPE1,2,3)='MS'                    THEN 1 ELSE 0 END) AS XMS_WAIT
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND SUBSTR(RSLT_NET,1,2)='SK' THEN 1 ELSE 0 END) AS XMS_SK_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND SUBSTR(RSLT_NET,1,2)='KT' THEN 1 ELSE 0 END) AS XMS_KT_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND SUBSTR(RSLT_NET,1,2)='LG' THEN 1 ELSE 0 END) AS XMS_LG_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND MSG_TYPE2='SMS'           THEN 1 ELSE 0 END) AS XMS_SMS_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND MSG_TYPE2='LMS'           THEN 1 ELSE 0 END) AS XMS_LMS_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='XMS' AND MSG_TYPE2='MMS'           THEN 1 ELSE 0 END) AS XMS_MMS_SUCCESS
-- ALT 통계
, SUM(CASE WHEN MSG_TYPE1='ALT'                                                                                   THEN 1 ELSE 0 END) AS ALT_TOTAL
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='ALT'                               THEN 1 ELSE 0 END) AS ALT_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE NOT IN ('0','1000') AND RSLT_TYPE='ALT'                               THEN 1 ELSE 0 END) AS ALT_FAIL
, SUM(CASE WHEN (MSG_STATE<>'6' OR MSG_STATE IS NULL  )           AND MSG_TYPE1='ALT'                             THEN 1 ELSE 0 END) AS ALT_WAIT
-- RCS 통계
, SUM(CASE WHEN MSG_TYPE1='RCS'                                                                                   THEN 1 ELSE 0 END) AS RCS_TOTAL
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS'                               THEN 1 ELSE 0 END) AS RCS_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE NOT IN ('0','1000') AND RSLT_TYPE='RCS'                               THEN 1 ELSE 0 END) AS RCS_FAIL
, SUM(CASE WHEN (MSG_STATE<>'6' OR MSG_STATE IS NULL  )           AND MSG_TYPE1='RCS'                             THEN 1 ELSE 0 END) AS RCS_WAIT
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND SUBSTR(RSLT_NET,1,2)='SK' THEN 1 ELSE 0 END) AS RCS_SK_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND SUBSTR(RSLT_NET,1,2)='KT' THEN 1 ELSE 0 END) AS RCS_KT_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND SUBSTR(RSLT_NET,1,2)='LG' THEN 1 ELSE 0 END) AS RCS_LG_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND MSG_TYPE2='RCS'           THEN 1 ELSE 0 END) AS RCS_RCS_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND MSG_TYPE2='RCL'           THEN 1 ELSE 0 END) AS RCS_RCL_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND MSG_TYPE2='RCM'           THEN 1 ELSE 0 END) AS RCS_RCM_SUCCESS
, SUM(CASE WHEN MSG_STATE='6' AND RSLT_CODE IN ('0','1000')     AND RSLT_TYPE='RCS' AND MSG_TYPE2='RCT'           THEN 1 ELSE 0 END) AS RCS_RCT_SUCCESS
FROM (
        -- NURI2_NRMSG_DATA 발송결과 미수신데이터 결과조회시 필수
        SELECT
          MSG_STATE
        , RSLT_CODE
        , RSLT_NET
        , RSLT_TYPE
        , SENT_COUNT
        , (CASE WHEN SENT_COUNT<=1 THEN MSG_TYPE_1 ELSE CASE WHEN SENT_COUNT=2 THEN MSG_TYPE_2 ELSE  MSG_TYPE_3 END END) AS MSG_TYPE1
        , (CASE WHEN SENT_COUNT<=1 THEN CONTENTS_TYPE_1 ELSE CASE WHEN SENT_COUNT=2 THEN CONTENTS_TYPE_2 ELSE  CONTENTS_TYPE_3 END END) AS MSG_TYPE2
        , HISTORY_MSG_TYPE
        , HISTORY_RSLT_CODE
        FROM NURI2_NRMSG_DATA
        WHERE SENT_DATE BETWEEN '20250808000000' AND  '20250808150127'
        UNION ALL
        SELECT
          MSG_STATE
        , RSLT_CODE
        , RSLT_NET
        , RSLT_TYPE
        , SENT_COUNT
        , (CASE WHEN SENT_COUNT<=1 THEN MSG_TYPE_1 ELSE CASE WHEN SENT_COUNT=2 THEN MSG_TYPE_2 ELSE  MSG_TYPE_3 END END) AS MSG_TYPE1
        , (CASE WHEN SENT_COUNT<=1 THEN CONTENTS_TYPE_1 ELSE CASE WHEN SENT_COUNT=2 THEN CONTENTS_TYPE_2 ELSE  CONTENTS_TYPE_3 END END) AS MSG_TYPE2
        , HISTORY_MSG_TYPE
        , HISTORY_RSLT_CODE
        FROM NURI2_NRMSG_LOG -- 연단위, 연월단위, 단일 테이블
        -- SENT_DATE: 실제 모바일메시지로 접수한 시간, RES_DATE: 자체 접수시간
        WHERE SENT_DATE BETWEEN '20250808000000' AND  '20250808150127'

) MSG
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
-- ================================================================================================== --
/*

CREATE TABLE IF NOT EXISTS nuri2_nrmsg_data (
  MSG_KEY       int(11) NOT NULL AUTO_INCREMENT     COMMENT '고유번호',
  SUB_ID        varchar(30) DEFAULT NULL            COMMENT 'SUB_ID(과금청구용)',
  USER_KEY      int(11) DEFAULT NULL                COMMENT '사용자 고유 번호',
  USER_GROUP    varchar(30) DEFAULT NULL            COMMENT '사용자 그룹',
  USER_ID       varchar(30) DEFAULT NULL            COMMENT '사용자 고유 아이디',
  USER_JOBID    varchar(40) DEFAULT NULL            COMMENT '사용자 JOB 아이디',
  CENTER_KEY int(2) NOT NULL DEFAULT '1'            COMMENT '발송라인',
  MSG_PRIORITY int(2) NOT NULL DEFAULT '3'          COMMENT '메시지 전송 우선 순위',
  MSG_STATE int(2) NOT NULL DEFAULT '1'             COMMENT '메시지 상태 값',
  INPUT_DATE timestamp NULL DEFAULT NULL            COMMENT '메시지 입력시간',
  RES_DATE timestamp NULL DEFAULT NULL              COMMENT '메시지 예약일시',
  QUE_DATE timestamp NULL DEFAULT NULL              COMMENT '메시지를 수집 한 시간',
  SENT_DATE timestamp NULL DEFAULT NULL             COMMENT '메시지를 전송 한 시간',
  RSLT_DATE timestamp NULL DEFAULT NULL             COMMENT '핸드폰에 전달 된 시간',
  REPORT_DATE timestamp NULL DEFAULT NULL           COMMENT 'G/W에서 결과를 수신한 시간',
  RSLT_CODE varchar(4) DEFAULT NULL                 COMMENT '결과처리 상세코드',
  RSLT_NET varchar(3) DEFAULT NULL                  COMMENT '결과처리 통신사',
  RSLT_TYPE varchar(3) DEFAULT NULL                 COMMENT '결과처리 된 메시지 유형',
  SENT_COUNT int(2) NOT NULL DEFAULT '0'            COMMENT '결과처리 히스토리 재전송 횟수',
  HISTORY_MSG_TYPE varchar(23) DEFAULT NULL         COMMENT '결과처리 히스토리 발송 타입',
  HISTORY_RSLT_CODE varchar(28) DEFAULT NULL        COMMENT '결과처리 히스토리 상세코드',
  IDENTIFIER varchar(9) DEFAULT NULL                COMMENT '메시지 식별자코드',
  PHONE varchar(20) NOT NULL                        COMMENT '수신 번호',
  CALLBACK varchar(20) NOT NULL                     COMMENT '발신 번호',
  MSG_TYPE_1 varchar(3) NOT NULL DEFAULT 'SMS'      COMMENT '발송 타입 1번째',
  CONTENTS_TYPE_1 varchar(3) NOT NULL DEFAULT 'SMS' COMMENT '메시지 내용에 대한 타입 1번째',
  QUE_DATE_1 timestamp NULL DEFAULT NULL            COMMENT '1번째 메시지 수집 한 시간',
  SENT_DATE_1 timestamp NULL DEFAULT NULL           COMMENT '1번째 메시지 전송 한 시간',
  MSG_TYPE_2 varchar(3) DEFAULT NULL                COMMENT '발송 타입 2번째',
  CONTENTS_TYPE_2 varchar(3) DEFAULT NULL           COMMENT '메시지 내용에 대한 타입 2번째',
  QUE_DATE_2 timestamp NULL DEFAULT NULL            COMMENT '2번째 메시지 수집 한 시간',
  SENT_DATE_2 timestamp NULL DEFAULT NULL           COMMENT '2번째 메시지 전송 한 시간',
  MSG_TYPE_3 varchar(3) DEFAULT NULL                COMMENT '발송 타입 3번째',
  CONTENTS_TYPE_3 varchar(3) DEFAULT NULL           COMMENT '메시지 내용에 대한 타입 3번째',
  QUE_DATE_3 timestamp NULL DEFAULT NULL            COMMENT '3번째 메시지 수집 한 시간',
  SENT_DATE_3 timestamp NULL DEFAULT NULL           COMMENT '3번째 메시지 전송 한 시간',
  XMS_RSLT_CODE varchar(4) DEFAULT NULL             COMMENT 'XMS 결과처리 상세코드',
  XMS_RSLT_NET varchar(3) DEFAULT NULL              COMMENT 'XMS 결과처리 통신사',
  XMS_RSLT_DATE timestamp NULL DEFAULT NULL         COMMENT 'XMS 핸드폰에 전달 된 시간',
  XMS_REPORT_DATE timestamp NULL DEFAULT NULL       COMMENT 'XMS G/W에서 결과를 수신한 시간',
  ALT_RSLT_CODE varchar(4) DEFAULT NULL             COMMENT 'ALT 결과처리 상세코드',
  ALT_RSLT_NET varchar(3) DEFAULT NULL              COMMENT 'ALT 결과처리 통신사',
  ALT_RSLT_DATE timestamp NULL DEFAULT NULL         COMMENT 'ALT 핸드폰에 전달 된 시간',
  ALT_REPORT_DATE timestamp NULL DEFAULT NULL       COMMENT 'ALT G/W에서 결과를 수신한 시간',
  RCS_RSLT_CODE varchar(4) DEFAULT NULL             COMMENT 'RCS 결과처리 상세코드',
  RCS_RSLT_NET varchar(3) DEFAULT NULL              COMMENT 'RCS 결과처리 통신사',
  RCS_RSLT_DATE timestamp NULL DEFAULT NULL         COMMENT 'RCS 핸드폰에 전달 된 시간',
  RCS_REPORT_DATE timestamp NULL DEFAULT NULL       COMMENT 'RCS G/W에서 결과를 수신한 시간',
  XMS_SUBJECT varchar(60) DEFAULT NULL              COMMENT '메시지 타이틀(LMS/MMS)',
  XMS_TEXT varchar(4000) DEFAULT NULL               COMMENT '메시지 본문(SMS/LMS/MMS)',
  XMS_FILE_NAME_1 varchar(255) DEFAULT NULL         COMMENT '파일경로를 포함한 파일명1(MMS)',
  XMS_FILE_NAME_2 varchar(255) DEFAULT NULL         COMMENT '파일경로를 포함한 파일명2(MMS)',
  XMS_FILE_NAME_3 varchar(255) DEFAULT NULL         COMMENT '파일경로를 포함한 파일명3(MMS)',
  ALT_COUNTRY_CODE varchar(3) NOT NULL DEFAULT '82' COMMENT 'ALT 국가코드(ALT,ALI)',
  ALT_SENDER_KEY varchar(40) DEFAULT NULL           COMMENT 'ALT 사용자 아이디(ALT,ALI), 발송키(발신 프로필키)',
  ALT_TEMPLATE_CODE varchar(30) DEFAULT NULL        COMMENT 'ALT 등록된 템플릿 고유키(ALT,ALI)',
  ALT_JSON varchar(4000) DEFAULT NULL               COMMENT '메시지 본문(ALT,ALI)',
  RCS_BRAND_ID varchar(18) DEFAULT NULL             COMMENT 'RCS ID, BR.XXXXX',
  RCS_BRAND_KEY varchar(18) DEFAULT NULL            COMMENT 'RCS 브랜드 키, BK.XXXXXXXXXXXXXXX',
  RCS_MESSAGE_BASE_ID varchar(100) DEFAULT NULL     COMMENT 'RCS 메시지베이스 아이디',
  RCS_JSON varchar(4000) DEFAULT NULL               COMMENT '메시지 본문(RCS)',
  PRIMARY KEY (MSG_KEY),
  KEY IDX_nuri2_nrmsg_data_01 (MSG_STATE,RES_DATE),
  KEY IDX_nuri2_nrmsg_data_02 (MSG_STATE,RES_DATE,MSG_PRIORITY,SENT_COUNT,MSG_TYPE_1,MSG_TYPE_2,MSG_TYPE_3),
  KEY IDX_nuri2_nrmsg_data_03 (PHONE,CALLBACK),
  KEY IDX_nuri2_nrmsg_data_04 (SUB_ID)
) ENGINE=InnoDB  DEFAULT CHARSET=euckr;


*/
