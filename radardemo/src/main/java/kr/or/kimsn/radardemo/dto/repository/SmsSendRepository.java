package kr.or.kimsn.radardemo.dto.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radardemo.dto.SmsSendDto;

public interface SmsSendRepository extends JpaRepository<SmsSendDto, Long> {
	// app contents seq
	@Query(nativeQuery = true, value = "SELECT nuri.appContentNextval() from dual"
	// value = "select seq_currval+1 as seq from nuri.app_contents_sequence"
	)
	String getAppContentNextval();

	@Query(nativeQuery = true, value = "INSERT INTO nuri.NURI_MSG_DATA ( \n" +
			"  MSG_SEQ, \n" +
			"  REQ_DATE, \n" +
			"  CUR_STATE, \n" +
			"  CALL_TO, \n" +
			"  CALL_FROM, \n" +
			"  SMS_TXT, \n" +
			"  MSG_TYPE \n" +
			") VALUES( \n" +
			"  nuri.nextval(), -- MSG_SEQ  \n" +
			"  STR_TO_DATE(:req_date, '%Y%m%d %H%i%s'), -- REQ_DATE \n" +
			"  0, -- CUR_STATE \n" +
			"  :call_to, -- CALL_TO \n" +
			"  :call_from, -- CALL_FROM \n" +
			"  :sms_txt, -- SMS_TXT \n" +
			"  :msg_type -- MSG_TYPE \n" +
			") \n")
	// 문자발송
	@Transactional
	@Modifying
	Integer nuriSmsSendSave(
			@Param("req_date") String req_date,
			@Param("call_to") String call_to,
			@Param("call_from") String call_from,
			@Param("sms_txt") String sms_txt,
			@Param("msg_type") Integer msg_type);

	@Query(nativeQuery = true, value = "INSERT INTO nuri.app_send_contents (\n" +
			"  REQ_SEND_DATE \n" +
			", PACK_UNIQUEKEY\n" +
			", MSG_SUBJECT\n" +
			", MSG_DATA\n" +
			", MSG_TYPE\n" +
			") VALUES (\n" +
			"  DATE_FORMAT(NOW(),'%Y%m%d%H%i%S')  -- = REQ_SEND_DATE : 메시지를 DB에 넣은 시간 (yyyymmddHHMMSS)\n" +
			", :appnextval  -- = PACK_UNIQUEKEY : 컨텐츠 일련번호\n" +
			", NULL        -- = 제목 실패시  NURI 모듈에서 사용할 값 NULL 가능\n" +
			", :sms_txt    -- = 템플릿 자리수 포함 전체 1000자(실제는 980자)\n" +
			", 'AT'        -- = 고정값\n" +
			")\n")
	@Transactional
	@Modifying
	// 카카오톡 발송(내용)
	Integer gaonAppSendContentsSave(
			@Param("appnextval") Long appnextval,
			@Param("sms_txt") String sms_txt);

	@Query(nativeQuery = true, value = "INSERT INTO nuri.app_send_data (\n" +
			"  MSG_SEQ\n" +
			", PACK_UNIQUEKEY\n" +
			", REQ_SEND_DATE\n" +
			", CALL_BACK\n" +
			", PHONE_NUM\n" +
			", CUR_STATE\n" +
			", APP_GUBUN\n" +
			", TEMPLATE_CODE\n" +
			", GAON_MSG_TYPE\n" +
			") VALUES (\n" +
			"  nuri.nextval()  -- MSG_SEQ = 메시지 일련번호(고유값)\n" +
			", :appNextval     -- = PACK_UNIQUEKEY(메시지 내용 key)\n" +
			", DATE_FORMAT(NOW(),'%Y%m%d%H%i%S')  -- :req_date       -- = 접수날짜(발송요청시간 : 미래시간은 예약발송, 과거시간은 3시간이내이면 즉시발송.)\n"
			+
			", :call_from        -- CALL_TO = 발신번호, 숫자형태의 문자(숫자만 입력)\n" +
			", :call_to      -- CALL_FROM  = 수신번호 (숫자만 입력)\n" +
			", '0'             -- = 접수요청 = 0 (반드시 0으로만 입력)\n" +
			", 'KAKAO'         -- = 앱메시지 구분 : KAKAO(카카오), NAVER(네이버) 대문자로 입력\n" +
			// ", 'radar_0001' -- :templateCode -- = 사용할 템플릿코드\n" +
			", (select TEMPLATE_CODE from nuri.app_template_code atc where USE_BUTTON = 'Y')\n" +
			// ", 'template_0001' -- = 사용할 템플릿코드\n" +
			", 'L'             -- = GAON_MSG_TYPE ='L' 입력 고정 (실패시 자동 바이트 계산 후 SMS/LMS 재접수 처리)\n" +
			")\n")
	@Transactional
	@Modifying
	// 카카오톡 발송(전화번호)
	Integer gaonAppSendDataSave(
			@Param("appNextval") Long appNextval,
			// @Param("req_date") String req_date,
			@Param("call_to") String call_to,
			@Param("call_from") String call_from
	// @Param("templateCode") String templateCode
	);

}
