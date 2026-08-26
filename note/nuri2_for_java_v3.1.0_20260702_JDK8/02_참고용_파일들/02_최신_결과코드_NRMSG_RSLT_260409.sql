-- DDL
-- mysql 용 테이블 구조 nuri2_nrmsg_rslt
--
-- DROP TABLE IF EXISTS nuri2_nrmsg_rslt;
-- CREATE TABLE IF NOT EXISTS nuri2_nrmsg_rslt (
--   RSLT_KEY int(11) NOT NULL AUTO_INCREMENT COMMENT '순번',
--   RSLT_CODE varchar(4) NOT NULL COMMENT '결과코드',
--   RSLT_TYPE varchar(3) DEFAULT NULL COMMENT '결과구분(유형)',
--   RSLT_EXPLA varchar(200) DEFAULT NULL COMMENT '설명',
--   PRIMARY KEY (RSLT_KEY),
--   KEY IDX_nuri2_nrmsg_rslt_01 (RSLT_CODE),
--   KEY IDX_nuri2_nrmsg_rslt_02 (RSLT_TYPE,RSLT_CODE)
-- ) ENGINE=InnoDB  DEFAULT CHARSET=euckr COLLATE=euckr_korean_ci  ;
--

--  테이블의 덤프 데이터 nuri2_nrmsg_rslt

###########################################################################################################################################################
#** 기능개발 유의사항[1]
###########################################################################################################################################################
1) 수신번호 필터링 적용 (*필수적용*) *중요*
   - 국내의 경우 01x, 010 번호만 발송가능, 10 ~ 11자리 전화번호 형식
   - 휴대전화 외 일반번호 발송불가 : 070, 050, 02, 042 등 일반 전화 전송불가
   - 발신자번호 : 번호는 '-'없이 붙여서 입력 ex) 0111231234, 0422505537
   - 수신자번호 : 번호는 '-'없이 붙여서 입력 ex) 0111231234
   - 수신번호와 발신번호는 문자나 기호를 포함하지 않는 문자형식의 숫자로만 표시함
       예) 0111231234, 예) 0422505537
  * 문자메시지 : 국제문자 발송불가, 로밍의 경우는 개인 설정으로 처리가능(이통사 제공)
  * 앱메시지(ALT): 국제번호로 인증한 번호에 한해서 발송 가능.
  * [♠]발신번호(회신번호) 설정 [♠]휴대전화번호 이슈사항(19-12-17)
   - 발신자 휴대전화 이통사 서비스 중 [♠][번호도용 차단서비스]에 가입시 메시지가 차단되므로 발신자는 해당번호에 부가서비스 확인 후 발송이 필요함.

2) * 발신번호(회신번호) 설정 휴대전화번호(010xxxxoooo) 이슈사항(19-12-17)
   - 알림톡 발송 실패 후 문자처리시 이슈사항 입니다.
   - 발신자 휴대전화 이통사 서비스 중 [번호도용 차단서비스]에 가입시 메시지 발송 차단, 해당 번호로 발송이 필요한 경우
   가입된 부가서비스 해제 후 약 1주일  후 발송 가능
   -[♠중요♠][번호도용 차단서비스] 해제시 스미싱에 이용당할 수 있음

3) [#매우중요]* 메시지 발송내역 조회기능 구현[담당주무관 확인][미확인시 오픈 불가]
   - 메시지 발송결과 조회 필수,[MSG_KEY], [MSG_STATE], [RSLT_CODE] 필수 조회
   - 접수 건수와 결과(Report) 수신 건수 비교 해야함.  (상황1: 기관이 접수한 메시지는 총 100건, 모바일메시지 MMS/APP G/W에서는 99건 수신되는 경우)
   - MMS G/W로 접수 하지 못하고 실패처리되는 데이터 존재(규격 미준수)

3-1)[메시지결과 수신관련]
   - 최종 메시지가 xMS(SMS/MMS) 일때, 발송 결과 대기시간(SMS=24시간, MMS/LMS=72시간)
   - 발송 결과 대기시간: 이통사가 단말기에 전송을 시도하는 시간입니다.
   - MSG_STATE='5' 인경우는 결과를 대기하는 데이터이므로 대기시간(SMS=24시간, MMS/LMS=72시간)이 끝난이 후 문의 해주세요.
   * xMS 로 처리된 메시지는 SMS=24시간 또는 MMS/LMS=72시간 지난 후 결과 수신가능.

4) [#매우중요2][타업무(시스템)] 메시지 발송기능 공유시 유의사항
   - DB 및 SQL문 입력 방식으로 하지마세요.!!!!
   - API(내부 공통규격) 방식으로 배포 권장 : 내부 공유 업무쪽에서 규격 위반시 발송 제한/차단.

5) * [중요] : 트리거(Trigger) 사용금지 -- [2025-05-13] --
 - 오동작, 성능 저하 원인
 - (NURI2_)NRMSG_DATA, (NURI2_)NRMSG_LOG 테이블과 연관된 테이블
 - (NURI2_)NRMSG_DATA테이블에 LOCK를 유발하지 않는 조건에서는 사용가능.
###########################################################################################################################################################

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
예) [성공]: MSG_STATE=6 AND RSLT_CODE IN ('0','1000')

* 실패는 아래와 같습니다.
예) [실패]: MSG_STATE=6 AND RSLT_CODE NOT IN ('0','1000')
  - RSLT_CODE는 결과코드테이블( NRMSG_RSLT OR NURI2_NRMSG_RSLT) 코드 조회

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
 # 로그 파일 위치 : 예) /home/nuri2/nuri2_log/nuri2.log
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



-- 결과코드 데이터 입력
-- truncate table nuri2_nrmsg_rslt
-- # truncate 후 데이터를 새로 입력 하세요

INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(1,  '100',  'CON', 'Success Connect:연결 성공');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(2,  '200',  'CON', 'Unknown Id:계정없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(3,  '201',  'CON', 'Invalid Password:패스워드틀림');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(4,  '202',  'CON', 'Not Enough Money:과금건수없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(5,  '203',  'CON', 'Server Busy');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(6,  '900',  'AGT', 'Attachment Error(컨텐츠 경로가 잘못되었거나 파일이 없을 경우)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(7,  '901',  'AGT', 'Duplicate Key(MSG_KEY 중복 차단, 동일키)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(8,  '902',  'AGT', 'Duplicate Phone Count(동일폰, 하루에 한 수신번호에 보낼 수 있는 메시지 수량 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(9,  '903',  'AGT', 'Spam Checked(메시지 스팸 차단, 발신번호, 수신번호 누락)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(10, '904',  'AGT', 'Receiver Ban Phone Number(수신번호 스팸 차단)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(11, '905',  'AGT', 'Callback Ban Phone Number(회신번호 스팸 차단)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(12, '908',  'AGT', 'Request Retransmission To A Missing Center(누락된 센터로 재전송 요청)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(13, '909',  'AGT', 'MSG_TYPE input not allowed(허용되지 않는 MSG_TYPE 입력)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(14, '910',  'AGT', '해당 메시지type 전송불가 발송불가');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(15, '920',  'AGT', 'The XMS_SUBJECT Exceeds the allowed Length(XMS_SUBJECT 허용된 길이를 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(16, '922',  'AGT', 'The XMS_TEXT Exceeds the allowed Length(XMS_TEXT 허용된 길이를 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(17, '923',  'AGT', 'The ALT_JSON Exceeds the allowed Length(ALT_JSON 허용된 길이를 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(18, '924',  'AGT', 'The RCS_JSON Exceeds the allowed Length(RCS_JSON 허용된 길이를 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(19, '925',  'AGT', 'Files with Extensions not allowed(허용되지 않는 확장자를 가진 파일)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(20, '926',  'AGT', 'RESEND count exceeded(재전송 횟수 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(21, '927',  'AGT', 'Message that has already been sent(이미 전송되었던 메시지)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(22, '990',  'AGT', 'GATEWAY SEND LINE 연결 실패(통신실패),(일별, 월별 건수 소진)');
-- INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(23, '100', 'SED', 'Success:접수 성공'); -- 중복값 제거
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(24, '300',  'SED', 'Invalid Receiver Phone Number(잘못된번호)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(25, '301',  'SED', 'Spam Checked(스팸, 발신, 수신번호 확인필요)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(26, '302',  'SED', 'Receiver Ban Phone Number');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(27, '303',  'SED', 'Callback Ban Phone Number');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(28, '304',  'SED', 'Spam Checked');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(29, '310',  'SED', '접수 불가, ALT, RCS 접수 불가');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(30, '400',  'SED', 'Format Error');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(31, '410',  'SED', 'Empty KEY');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(32, '500',  'SED', 'Failure');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(33, '1000', 'COM', '성공'); --
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(34, '2000', 'ALT', '메시지전문 내용오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(35, '3000', 'COM', '타임아웃');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(36, '9999', 'COM', '기타실패, 전문 내용오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(37, '0',    'XMS', '성공');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(38, '1',    'XMS', 'TIMEOUT(전송 시간 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(39, 'A',    'XMS', '핸드폰 호 처리 중');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(40, 'B',    'XMS', '음영지역');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(41, 'C',    'XMS', 'Power Off');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(42, 'D',    'XMS', '메시지 저장개수 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(43, '2',    'XMS', '잘못된 전화번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(44, 'a',    'XMS', '일시 서비스 정지');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(45, 'b',    'XMS', '기타 단말기 문제');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(46, 'c',    'XMS', '착신 거절');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(47, 'd',    'XMS', '기타, 번호도용 차단 또는 용량초과등 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(48, 'e',    'XMS', '이통사 SMC 형식 오류, 무선망 전송실패');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(49, 's',    'XMS', '메시지 스팸차단(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(50, 'n',    'XMS', '수신번호 스팸차단(Nuri2 내부) 또는 건수 제한에 걸린 경우(건수 제한 계약이 되어 있는 경우)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(51, 'r',    'XMS', '회신번호 스팸차단(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(52, 'f',    'XMS', '모바일메시지서비스 자체 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(53, 'g',    'XMS', 'SMS/LMS/MMS 서비스 불가 단말기');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(54, 'h',    'XMS', '핸드폰 호 불가 상태');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(55, 'i',    'XMS', 'SMC 운영자가 메시지 삭제');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(56, 'j',    'XMS', '이통사 내부 메시지 Que Full');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(57, 'k',    'XMS', '이통사에서 spam 처리');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(58, 'l',    'XMS', 'www.nospam.go.kr 에 등록된 번호에 대해 모바일메시지서비스에서 spam 처리한 건');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(59, 'm',    'XMS', '모바일메시지서비스에서 Spam 처리한 건');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(60, 'o',    'XMS', '메시지의 길이가 제한된 길이를 벗어난 경우(SMS:90Byte / LMS:2000Byte)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(61, 'p',    'XMS', '폰 번호가 형식에 어긋난 경우');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(62, 'Q',    'XMS', '필드 형식이 잘못된 경우(예:데이터 내용이 없는 경우)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(63, 'x',    'XMS', 'MMS 콘텐트의 정보를 참조할 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(64, 'u',    'XMS', 'BARCODE 생성 실패');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(65, 'q',    'XMS', 'MSG_KEY 중복 차단(901과 같음) 또는 접수 권한 없음.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(66, 'y',    'XMS', '하루에 한 수신번호에 보낼 수 있는 메시지 수량 초과(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(67, 'w',    'XMS', 'SMS 전송 문자에 특정 키워드가 없으면 SPAM 처리하여 메시지 전송 제한(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(68, 't',    'XMS', '스팸 차단 중 2 개 이상 중복 차단(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(69, 'Z',    'XMS', '메시지 접수시 기타 실패(Nuri2 내부)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(70, 'z',    'XMS', '처리 되지 않은 기타오류, ALT/RCS 접수불가');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(71, '4100', 'ALT', '카카오 내부 시스템 오류로 메시지 전송 실패');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(72, '4101', 'ALT', '메시지를 전송할 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(73, '4102', 'ALT', '*알림톡 차단 및 카톡 미사용[3018]');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(74, '4103', 'ALT', '메시지 전송 가능한 시간이 아님, 수신자가 카톡을 받지 않겠다고 설정한 시간에 차단됨');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(75, '4104', 'ALT', '메시지 전송 결과를 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(76, '4105', 'ALT', '알수 없는 메시지 상태');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(77, '4106', 'ALT', '잘못된 수신번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(78, '4200', 'ALT', '수신확인 안됨 (성공 불확실), 프로필키 오류[9991]');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(79, '4300', 'ALT', '발신프로필을 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(80, '4400', 'ALT', '삭제된 발신 프로필');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(81, '4401', 'ALT', '차단 상태의 발신 프로필');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(82, '4402', 'ALT', '계약 정보를 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(83, '4403', 'ALT', '유효하지 않은 app연결');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(84, '4404', 'ALT', '유효하지 않은 사업자번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(85, '4405', 'ALT', '유효하지 않은 app user id 요청');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(86, '4406', 'ALT', '사업자등록번호 불일치');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(87, '4407', 'ALT', '올바른 유저 식별자 값이 하나도 없는 경우');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(88, '4408', 'ALT', '빈 메시지');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(89, '4500', 'ALT', '최대 메시지 길이 초과 (ALT:1000자, ALI:400자 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(90, '4501', 'ALT', '메시지가 존재하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(91, '4502', 'ALT', '메시지 길이 제한 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(92, '4503', 'ALT', '변수 글자수 제한 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(93, '4504', 'ALT', '버튼의 필수 이름, 타입 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(94, '4600', 'ALT', '버튼 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(95, '4601', 'ALT', '잘못된 버튼 타입');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(96, '4602', 'ALT', 'WL 버튼 타입에 필요한 url_mobile 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(97, '4603', 'ALT', 'AL 버튼 타입에 필요한 Scheme_android 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(98, '4604', 'ALT', 'AL 버튼 타입에 필요한 Scheme_ios 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(99, '4605', 'ALT', '템플릿 코드 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(100,'4700', 'ALT', '템플릿 일치 확인 시 오류 발생');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(101,'4701', 'ALT', '템플릿을 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(102,'4702', 'ALT', '메시지 내용이 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(103,'4703', 'ALT', '버튼 내용이 템플릿과 일치 하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(104,'4704', 'ALT', '메시지 강조 표기 타이틀이 템플릿과 일치하지 않음, 링크 http/https');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(105,'4705', 'ALT', 'json 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(106,'4800', 'ALT', '잘못된 response_method');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(107,'4801', 'ALT', '이미지 누락');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(108,'4802', 'ALT', '메시지 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(109,'4900', 'ALT', '타이틀 길이 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(110,'4901', 'ALT', '메시지 일변번호가 중복됨');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(111,'4902', 'ALT', '메시지 강조 표기 타이틀 길이 제한 초과(50자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(112,'4999', 'ALT', '해당 실패건은 알림톡 수신 차단으로 인한 실패입니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(113,'5100', 'RCS', '타임아웃');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(114,'5101', 'RCS', '실시간 메시지 인입 후 10초안에 삼성으로 전달되지 못함');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(115,'5102', 'RCS', '요청을 처리할 수 없는 상태 입니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(116,'5200', 'RCS', '잘못된수신번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(117,'5201', 'RCS', 'RCS 미지원 단말');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(118,'5202', 'RCS', '자사 고객 아님');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(119,'5203', 'RCS', '자사 고객이지만, RCS메시지를 수신할 수 있는 가입자가 아닙니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(120,'5204', 'RCS', '단말기기로 RCS 메시지를 전송할 수 없습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(121,'5300', 'RCS', '챗봇 정보 오류(등록안된발신번호)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(122,'5301', 'RCS', '사용할 수 없는 챗봇 정보');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(123,'5302', 'RCS', '미등록 발신번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(124,'5303', 'RCS', '챗봇ID가 존재하지 않음, 미등록 발신번호(대화방 미등록)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(125,'5304', 'RCS', '챗봇 권한 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(126,'5305', 'RCS', '발신 가능한 챗봇 상태가 아님');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(127,'5400', 'RCS', '브랜드 아이디 불일치');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(128,'5401', 'RCS', '대행사 정보 내용이 누락된 필수 항목이 있습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(129,'5402', 'RCS', 'AgencyID가 존재하지 않습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(130,'5403', 'RCS', 'BrandID에 대행 권한이 없는 AgencyID');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(131,'5404', 'RCS', '계약 정보 내용이 부정확하거나 누락된 필수 항목이 있습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(132,'5500', 'RCS', '메시지 내용이 누락되었거나 부정확합니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(133,'5501', 'RCS', '메시지 형식이 부정확하거나 누락된 필수항목이 있습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(134,'5502', 'RCS', '메시지 기술방법이 잘못되었습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(135,'5503', 'RCS', '메시지 내용이 누락되었거나 부정확합니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(136,'5504', 'RCS', '같은 메시지 ID로 두번 이상 메시지 전송이 요청됨');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(137,'5505', 'RCS', '(광고)를 사용할 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(138,'5506', 'RCS', '허용되지 않은 header 값 사용');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(139,'5507', 'RCS', 'header 값과 일치 하지 않은 footer 사용 (ex. header가 0 인데, footer 가 있음)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(140,'5508', 'RCS', 'footer값이 누락되어 있습니다 (ex. header가 1 인데, footer 가 없음)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(141,'5509', 'RCS', 'footer validation 오류 (ex. 숫자, 하이픈만 가능. 20자리)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(142,'5510', 'RCS', '등록한 패턴과 일치 하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(143,'5511', 'RCS', 'title 최대글자수를 초과했습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(144,'5512', 'RCS', 'description 최대글자수를 초과했습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(145,'5600', 'RCS', '최대 버튼수를 초과했습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(146,'5601', 'RCS', 'Action button이 허용되지 않는 messagebaseID에서 Action button을 사용하였음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(147,'5602', 'RCS', '버튼 필드를 받을 수 없는 메시지베이스 입니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(148,'5603', 'RCS', '최대 버튼 글자수 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(149,'5604', 'RCS', '버튼 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(150,'5700', 'RCS', '메시지베이스 정보 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(151,'5701', 'RCS', '사용할 수 없는 메시지베이스 정보');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(152,'5702', 'RCS', 'messagebaseID의 number of card 와 입력이 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(153,'5703', 'RCS', '메시지베이스 파라미터의 길이가 한계값 이상');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(154,'5704', 'RCS', '메시지베이스 ID가 존재하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(155,'5800', 'RCS', '중복전송 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(156,'5801', 'RCS', '전송 한도 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(157,'5802', 'RCS', '전송Capa 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(158,'5803', 'RCS', '재전송(메시지가 중복 접수됨)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(159,'5804', 'RCS', '중복키 에러');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(160,'5805', 'RCS', '요청 건수 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(161,'5806', 'RCS', '잘못된 파일 경로');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(162,'5807', 'RCS', '스팸');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(163,'5900', 'RCS', '메시지 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(164,'5901', 'RCS', 'json 형식 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(165,'5902', 'RCS', 'RCS 메시지 TPS가 초과되었습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(166,'5903', 'RCS', 'RCS 메시지 Quota가 초과되었습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(167,'5904', 'RCS', '파일 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(168,'5905', 'RCS', '기업 정보 내용이 누락된 필수항목이 있습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(169,'5906', 'RCS', '필수 파라미터 검증 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(170,'5907', 'RCS', '최대 미디어 용량을 초과했습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(171,'5908', 'RCS', '중계사 정보가 부정확하거나 누락된 필수 항목이 있습니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(172,'5909', 'RCS', '요청을 처리할 수 없는 메시지 유형입니다.');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(173,'5910', 'RCS', '메시지 유효기간 입력값 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(174,'5911', 'RCS', '존재하지 않는 File이거나 usageType 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(175,'5912', 'RCS', 'Empty suggestions array 허용 안함');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(176,'5913', 'RCS', 'Revoked Message(데이터 꺼짐, 수신불가)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(177,'5914', 'RCS', '전송 성공 불확실함 (revocation fail 등)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(178,'5915', 'RCS', '메시지 취소되어, 전송안됨');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(179,'5916', 'RCS', '잘못된 메시지 형식으로 인해 전송 실패되었고 재시도 가능하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(180,'5917', 'RCS', '메시지 회수 실패 (삼성 에러 41002)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(181,'5918', 'RCS', 'RCS 세션 연결 전 만료되어 전송 실패');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(182,'5999', 'RCS', '기타실패');

/*

기타 추가 코드 - KAKAO 결과 코드 추가 입니다.
- 정의된 결과코드 이외의 참고용 추가 코드
- 구 APP G/W 결과코드 호환

INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1001', 'KKO', 'Request Body가 Json형식이 아님');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1002', 'KKO', '파트너 키가 유효하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1003', 'KKO', '발신 프로필 키가 유효하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1004', 'KKO', 'Request BODY(Json)에서 name을 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1005', 'KKO', '발신프로필을 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1006', 'KKO', '삭제된 발신 프로필');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1007', 'KKO', '차단 상태의 발신 프로필');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1011', 'KKO', '계약 정보를 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1012', 'KKO', '잘못된 형식의 유저 키 요청');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1013', 'KKO', '유효하지 않은 app연결');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1014', 'KKO', '유효하지 않은 사업자번호');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1015', 'KKO', '유효하지 않은 app user id 요청');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1016', 'KKO', '사업자등록번호 불일치');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1020', 'KKO', '올바른 유저 식별자 값이 하나도 없는 경우');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1021', 'KKO', '차단 상태의 카카오톡 채널(카카오톡 채널 운영툴에서 확인)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1022', 'KKO', '닫힘 상태의 카카오톡 채널(카카오톡 채널 운영툴에서 확인)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1023', 'KKO', '삭제된 카카오톡 채널(카카오톡 채널 운영툴에서 확인)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1024', 'KKO', '삭제대기 상태의 카카오톡 채널(카카오톡 채널 운영툴에서 확인)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1025', 'KKO', '메시지차단 상태의 카카오톡 채널(카카오톡 채널 운영툴에서 확인)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1026', 'KKO', '해당 msg_type에서 사용할 수 없는 response_method로 요청(이미지알림톡(AI)는 realtime으로 발송 불가)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1030', 'KKO', '잘못된 파라메터 요청');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '1033', 'KKO', '템플릿 타입과 메시지타입 불일치');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '2000', 'KKO', '특정값 최대길이 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '2002', 'KKO', '특정필드 NULL값');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '2003', 'KKO', '메시지 전송 실패(테스트 서버에서 카카오톡 채널을 추가하지 않은 경우)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '2004', 'KKO', '템플릿 일치 확인 시 오류 발생 (카카오 내부 오류)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '2006', 'KKO', '시리얼넘버 형식 불일치');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3000', 'KKO', '예기치 않은 오류 발생');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3002', 'KKO', '메시지 BODY 파싱오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3005', 'KKO', '메시지를 발송 했으나, 수신확인 안됨 (성공 불 확실)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3006', 'KKO', '카카오 내부 시스템 오류로 메시지 전송 실패');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3008', 'KKO', '전화번호 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3010', 'KKO', 'Json 파싱 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3011', 'KKO', '메시지가 존재하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3012', 'KKO', '메시지 일련번호가 중복됨- 메시지 일련 번호는 고유의 값이 부여되어야 함');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3013', 'KKO', '빈 메시지');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3014', 'KKO', '메시지 길이 제한 오류(텍스트 타입 1000자 초과, 이미지 타입 400자 초과)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3015', 'KKO', '템플릿을 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3016', 'KKO', '메시지 내용이 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3018', 'KKO', '메시지를 전송할 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3019', 'KKO', '톡 유저가 아님');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3020', 'KKO', '알림톡 수신 차단');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3021', 'KKO', '템플릿_치환변수_오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3022', 'KKO', '메시지 발송 가능한 시간이 아님(친구 톡/마케팅 메시지는 08시~ 20시까지 발송 가능)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3024', 'KKO', '메시지에 포함된 이미지를 전송할 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3025', 'KKO', '변수 글자수 제한 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3026', 'KKO', '상담/봇 전환 버튼 extra, event 글자수 제한 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3027', 'KKO', '메시지 버튼/바로연결이 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3028', 'KKO', '메시지 강조 표기 타이틀이 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3029', 'KKO', '메시지 강조 표기 타이틀 길이 제한 초과 (50자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3030', 'KKO', '메시지 타입과 템플릿 강조유형이 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3031', 'KKO', '헤더가 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3032', 'KKO', '헤더 길이 제한 초과(16자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3033', 'KKO', '아이템 하이라이트가 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3034', 'KKO', '아이템 하이라이트 타이틀 길이 제한 초과(이미지 없는 경우 30자, 이미지 있는 경우 21자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3035', 'KKO', '아이템 하이라이트 디스크립션 길이 제한 초과(이미지 없는 경우 19자, 이미지 있는 경우 14자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3036', 'KKO', '아이템 리스트가 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3037', 'KKO', '아이템 리스트의 아이템의 디스크립션 길이 제한 초과(23자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3038', 'KKO', '아이템 요약정보가 템플릿과 일치하지 않음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3039', 'KKO', '아이템 요약정보의 디스크립션 길이 제한 초과(14자)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '3040', 'KKO', '아이템 요약정보의 디스크립션에 허용되지 않은 문자 포함(통화기호/코드, 숫자, 콤마, 소수점, 공백을 제외한 문자 포함)');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '4000', 'KKO', '메시지 전송 결과를 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '4001', 'KKO', '알수 없는 메시지 상태');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '5000', 'KKO', '(테스트 발송) 관리자 혹은 일회성 인증을 받은 사용자가 아님');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '5001', 'KKO', '(테스트 발송) 일일 발송량 초과');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '7011', 'KKO', '시리얼 넘버 패턴 에러');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '7014', 'KKO', '메시지 유효 시간 초과 에러');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8512', 'KKO', '수신자 타입 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8514', 'KKO', 'request_id 찾을 수 없음');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8520', 'KKO', '지원하지 않는상품 타입 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8521', 'KKO', '지원하지 않는 메시지 타입 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8522', 'KKO', '지원하지 않는 텍스트 유형 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8523', 'KKO', '지원하지 않는 response method 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8530', 'KKO', '수신자 목록 사이즈 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '8999', 'KKO', '내부 서버 오류');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '9998', 'KKO', '시스템에 문제가 발생하여 담당자가 확인하고 있는 경우');
INSERT INTO nuri2_nrmsg_rslt (RSLT_KEY, RSLT_CODE, RSLT_TYPE, RSLT_EXPLA) VALUES(NULL, '9999', 'KKO', '시스템에 문제가 발생하여. 담당자 확인중');




*/