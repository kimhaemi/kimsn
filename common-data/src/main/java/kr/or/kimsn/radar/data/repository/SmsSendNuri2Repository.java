package kr.or.kimsn.radar.data.repository;

import javax.transaction.Transactional;
import kr.or.kimsn.radar.data.dto.SmsSendNuri2Dto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SmsSendNuri2Repository extends JpaRepository<SmsSendNuri2Dto, Long> {
	// app contents seq
	@Query(nativeQuery = true, value = "SELECT nuri2.msg_nextval() from dual"
	// value = "select seq_currval+1 as seq from nuri.app_contents_sequence"
	)
	Long getMsgNextval();

	@Query(nativeQuery = true,
			   value = "SELECT COUNT(*) as cnt \n"
				  		 + "FROM information_schema.tables \n"
					  	 + "WHERE table_schema = 'nuri2' \n"
						   + "AND TABLE_NAME LIKE concat('%', :yearMonth, '%')"
	      )
	Long getShowTableYn(@Param("yearMonth") String yearMonth);

	@Query(nativeQuery = true,
			countQuery =
				"SELECT count(*) as cnt \n"
				+ "FROM ( \n"
				+ "  SELECT * \n"
				+ "    FROM ( \n"
				+ "			SELECT * FROM nuri2.NURI2_NRMSG_DATA \n"
				+ "      WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') \n"
				+ "			UNION ALL\n"
				+ "			SELECT * FROM nuri2.NURI2_NRMSG_LOG_:yearMonth \n"
				+ "      WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') \n"
				+ "    ) MT \n"
				+ "  WHERE 1=1 \n"
				+ "    AND (:smsResult = '' OR :smsResult IS NULL ) \n"
				+ "     OR (case when :smsResult = '1' then MT.MSG_STATE = 6 and MT.RSLT_CODE IN ('0', '1000') \n"
				+ "              when :smsResult = '0' then MT.RSLT_CODE NOT IN ('0', '1000') or MT.RSLT_CODE IS NULL \n"
				+ "              when :smsResult = '9' then MT.RSLT_CODE IS NULL \n"
				+ "         end) \n"
				+ ") total \n"
			,
			value =
				"SELECT \n"
				+ "    MSG_KEY as MSG_KEY             -- # '고유번호', 메시지 접수번호(일련번호), 접수시 필수값, 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용) \n"
				+ "-- [성공]: MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') \n"
				+ "-- [실패]: MSG_STATE='6' AND (RSLT_CODE<>'0' OR RSLT_CODE<>'1000') : 상태 코드 NURI2_NRMSG_RSLT 코드 조회 \n"
				+ "  , MSG_STATE as MSG_STATE  -- # '메시지 상태 값', 1:입력(전송대기),  3:전송수집중(QUE 수집), 5:전송(결과대기, SMS:24시간, MMS:72시간 대기), 6:처리완료  회신(결과회신), MSG_STATE='6' 성공, 실패 중 결과 확인된 경우 \n"
				+ "  , (CASE \n"
				+ "      WHEN MT.MSG_STATE = 1 THEN '전송대기' \n"
				+ "      WHEN MT.MSG_STATE = 3 THEN '전송수집중' \n"
				+ "      WHEN MT.MSG_STATE = 5 THEN '전송' \n"
				+ "      WHEN MT.MSG_STATE = 6 THEN '처리완료' \n"
				+ "      ELSE '' "
				+ "     END) as MSG_STATE_NAME \n"
				+ "-- 처리별 날짜 \n"
				+ "  , INPUT_DATE as INPUT_DATE          -- # '메시지 입력시간', 실제로 메시지를 입력한 시간 \n"
				+ "  , RES_DATE as RES_DATE            -- # '메시지 예약일시', 미래시간, 과거시간(3시간이내 입력시간데이터 조회,  실시간 발송처리 함) 가능 \n"
				+ "-- 결과코드,   MSG_STATE='6' 성공, 실패등 결과과 확인 \n"
				+ "  , RSLT_CODE as RSLT_CODE           -- # '*결과처리 상세코드', 성공 : MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000'), \n"
				+ "  , (CASE \n"
				+ "      WHEN MSG_STATE='6' AND (RSLT_CODE='0' OR RSLT_CODE='1000') THEN '성공' \n"
				+ "      WHEN RSLT_CODE is NULL THEN '' \n"
				+ "      ELSE '실패' END) as RSLT_CODE_NAME \n"
				+ "  , RSLT_NET as REST_NET            -- # '*결과처리 통신사', SKT, KT, LGU, KKO=KAKAO \n"
				+ "  , RSLT_TYPE as RSLT_TYPE           -- # '*결과처리 된 메시지 유형', XMS(또는 MMS,SMS), ALT, RCS \n"
				+ "  , (CASE \n"
				+ "      WHEN MSG_TYPE_1 = 'ALT' THEN '카카오' \n"
				+ "      WHEN MSG_TYPE_1 = 'SMS' THEN '단문' \n"
				+ "      WHEN MSG_TYPE_1 = 'MMS' THEN '장문' \n"
				+ "      ELSE '기타' \n"
				+ "    END) as RSLT_TYPE_NAME \n"
				+ "  , (CASE \n"
				+ "      WHEN MSG_TYPE_2 = 'ALT' THEN '카카오' \n"
				+ "      WHEN MSG_TYPE_2 = 'SMS' THEN '단문' \n"
				+ "      WHEN MSG_TYPE_2 = 'MMS' THEN '장문' \n"
				+ "      ELSE '기타' \n"
				+ "    END) AS RSLT_TYPE_NAME_2 \n"
				+ "  , ALT_RSLT_CODE as ALT_RSLT_CODE         -- '카카오 결과코드', 카카오 알림톡 메시지 발송 결과코드, NURI2_NRMSG_RSLT 코드 조회 \n"
				+ "  , XMS_RSLT_CODE as XMS_RSLT_CODE         -- 'sms 결과코드', 문자 메시지 발송 결과코드, NURI2_NRMSG_RSLT 코드 조회 \n"
				+ "  , (SELECT max(RSLT_EXPLA) FROM nuri2.NURI2_NRMSG_RSLT r where r.RSLT_CODE=MT.ALT_RSLT_CODE and r.RSLT_TYPE IN ('ALT', 'CON', 'SED', 'AGT', 'COM')) AS ALT_RSLT_EXPLA -- 'kakao 결과코드 상세내용' \n"
				+ "  , (SELECT max(RSLT_EXPLA) FROM nuri2.NURI2_NRMSG_RSLT r where binary r.RSLT_CODE=MT.XMS_RSLT_CODE and r.RSLT_TYPE IN ('XMS', 'CON', 'SED', 'AGT', 'COM')) AS XMS_RSLT_EXPLA -- 'sms 결과코드 상세내용' \n"
				+ "  , (SELECT max(RSLT_EXPLA) FROM nuri2.NURI2_NRMSG_RSLT r where binary r.RSLT_CODE=MT.RSLT_CODE and r.RSLT_TYPE = MT.RSLT_TYPE) AS RSLT_EXPLA -- '결과코드 상세내용' \n"
				+ "  , PHONE as PHONE               -- # '수신 번호', [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX \n"
				+ "-- 메시지유형 전송 우선순위 설정:  메시지 우선 순위를 ALT 또는 RCS로 하고 마지막 처리 순서로 문자(XMS)로 설정, 만약 문자(xMS)먼저 입력하면 문자로 바로 처리하고 종료 됨 \n"
				+ "-- , MSG_TYPE_1='ALT' , CONTENTS_TYPE_1='ALT' \n"
				+ "-- , MSG_TYPE_2='SMS' , CONTENTS_TYPE_2='SMS' -- 또는  MSG_TYPE_3='MMS' , CONTENTS_TYPE_3='LMS' \n"
				+ "  , MSG_TYPE_1 as MSG_TYPE_1          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 \n"
				+ "  , CONTENTS_TYPE_1 as CONTENTS_TYPE_1     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 \n"
				+ "  , MSG_TYPE_2 as MSG_TYPE_2          -- # '발송 타입 1번째'  SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 \n"
				+ "  , CONTENTS_TYPE_2 as CONTENTS_TYPE_2     -- # '메시지 내용에 대한 타입 1번째' SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자 \n"
				+ "-- 메시지 내용 입력 \n"
				+ "  , XMS_SUBJECT as XMS_SUBJECT         -- # '메시지 타이틀(LMS/MMS)' \n"
				+ "  , XMS_TEXT as XMS_TEXT           -- # '메시지 본문(SMS/LMS/MMS)' \n"
				+ "  , ALT_JSON as ALT_JSON            -- # '메시지 본문(ALT,ALI)', JSON 형식, --, 필수 형식 '{'text':'입력 할 메시지 내용'}' \n"
				+ "FROM ( \n"
				+ "			SELECT * FROM nuri2.NURI2_NRMSG_DATA \n"
				+ "      WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') \n"
				+ "			UNION ALL\n"
				+ "			SELECT * FROM nuri2.NURI2_NRMSG_LOG_:yearMonth \n"
				+ "      WHERE RES_DATE between DATE_FORMAT(:startDate, '%Y%m01000000') and DATE_FORMAT(LAST_DAY(:startDate), '%Y%m%d235959') \n"
				+ "     ) MT \n"
				+ "WHERE 1=1 \n"
				+ "  AND (:smsResult = '' OR :smsResult IS NULL ) \n"
				+ "   OR (case when :smsResult = '1' then MT.MSG_STATE = 6 and MT.RSLT_CODE IN ('0', '1000') \n"
				+ "            when :smsResult = '0' then MSG_STATE='6' AND (RSLT_CODE<>'0' OR RSLT_CODE<>'1000') \n"
				+ "            when :smsResult = '9' then MT.RSLT_CODE IS NULL \n"
				+ "       end) \n"
				+ "ORDER BY RES_DATE desc \n"
			)
		// 문자 전송 내역
	Page<SmsSendNuri2Dto> getSmsSendNuri2List(
			Pageable pageable,
			// @Param("limitStart") Integer limitStart,
			// @Param("pageSize") Integer pageSize,
			@Param("yearMonth") Integer yearMonth,
//			@Param("smsSUC") String smsSUC,
//			@Param("smsFail") String smsFail,
			@Param("startDate" ) String startDate,
			@Param("smsResult" ) String smsResult
	);

	//nuri2 문자 발송
	@Query(nativeQuery = true,
			value =
				"INSERT INTO nuri2.NURI2_NRMSG_DATA (\n"
				+ "          MSG_KEY            -- 숫자 11자리, 메시지 일련번호, 반드시 고유값, 중복시 발송실패, 시퀀스(시리얼번호사용)\n"
				+ "        , MSG_STATE          -- 1:전송대기,  3:전송수집중(QUE 수집), 5:전송완료(결과대기), 6:결과처리 완료(결과회신)\n"
				+ "        , INPUT_DATE         -- DB 입력시간(DB서버 시간 기준)\n"
				+ "        , RES_DATE           -- 발송요청시간, 예약전송:미래시간, 즉시전송: now()\n"
				+ "        , ALT_COUNTRY_CODE   -- 국가코드 : 기본 값 82, 해외 카톡 발송시 해당국가코드 입력\n"
				+ "        , PHONE              -- 수신번호(숫자형태의 문자, 11~12자리),     [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX\n"
				+ "        , CALLBACK           -- 발신번호(숫자형태의 문자, 지역번호 필수), [*][중요]휴대폰번호 사용시에는 발신도용 해제 여부 필요, 스미싱 악용 방지용 '번호도용 차단서비스' 가입자는 해제 후 설정가능 합니다.\n"
				+ " -- 첫번째 컨텐츠(1차): 카카오 \n"
				+ "        , MSG_TYPE_1         -- [대분류] :SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , CONTENTS_TYPE_1    -- [소분류] :SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , ALT_SENDER_KEY     -- 발송키(발신 프로필키), 발송키는 채널을 의미합니다. 채널이 다르면 다른 발송키를 설정\n"
				+ "        , ALT_TEMPLATE_CODE  -- 템플릿코드\n"
				+ "        , ALT_JSON           -- 발송할 내용을 JSON 형태(한줄로입력)로 직접 입력, '{\"text\":\"모바일메시지서비스 운영 및 발송 가이드 안내\\n\\n카카오톡\\n모바일메시지 테스트입니다\\n\\n042-250-5537\\n감사합니다.\"}'\n"
				+ "                             -- 전체 1000자리(Length), 줄바꿈 치환 필수 '\\n'(1 Length로 계산), 실제로 1줄로 입력처리 일부db에서 '\\n'만 입력하게되면 줄바꿈처리로 에러, 실제 줄바꿈 기호를 메시지를 db에 입력시 '\\\\n' 처리해서 입력(db 마다 다를수 있음)\n"
				+ " -- 두번째 컨텐츠(첫번째 컨텐츠 실패시 수행)(2차): 문자\n"
				+ "        , MSG_TYPE_2         -- [대분류] :SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , CONTENTS_TYPE_2    -- [소분류] :SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , XMS_SUBJECT        -- 생략가능, LMS에서만 사용 최대 30Byte 이하 한글(2Byte)로 계산, 줄바꿈은 '\\n' 처리 1Byte 처리, 1Byte로 계산\n"
				+ "        , XMS_TEXT           -- SMS: 90byte 까지 입력(줄바꿈은 '\\n' 처리 1Byte 처리, 1Byte로 계산), LMS:2000 Byte 이하 \n"
				+ ") VALUES(\n"
				+ "          nuri2.msg_nextval() \n"
				+ "        , 1 -- 1:전송대기,  3:전송수집중(QUE 수집), 5:전송완료(결과대기), 6:결과처리 완료(결과회신)\n"
				+ "        , DATE_FORMAT(now(), '%Y%m%d%H%i%s') -- 날짜형식 : YYYYMMDDHH24MISS, 날짜 포멧으로 입력 권장\n"
				+ "        , DATE_FORMAT(:res_date, '%Y%m%d%H%i%s') -- 발송요청시간, 예약전송:미래시간, 즉시전송: now()\n"
				+ "        , '82' -- 국가코드 : 기본 값 82, 해외 카톡 발송시 해당국가코드 입력\n"
				+ "        , :call_to -- 수신번호(숫자형태의 문자, 11~12자리),     [*][중요]반드시 휴대폰 번호 형식으로만 입력, 01X0000XXXX\n"
				+ "        , :call_from -- 발신번호(숫자형z태의 문자, 지역번호 필수), [*][중요]휴대폰번호 사용시에는 발신도용 해제 여부 필요, 스미싱 악용 방지용 '번호도용 차단서비스' 가입자는 해제 후 설정가능 합니다.\n"
				+ " -- 첫번째 컨텐츠 \n"
				+ "        , 'ALT' -- [대분류] :SMS:단문 메시지, MMS:멀티메시지(장문, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , 'ALT' -- [소분류] :SMS:단문 메시지, LMS:장문, MMS:멀티메시지(장문+첨부, 첨부), ALT:카카오 알림톡 메시지, RCS: 안심문자\n"
				+ "        , '0c1a557a0f48c9db7d67217c9fb03d50f7c6d218' -- [필수] -- 발송키(발신 프로필키), 발송키는 채널을 의미합니다. 채널이 다르면 다른 발송키를 설정\n"
				+ "        , :templateCode  -- [필수] -- KR001~3 템플릿은 사전등록(예약) 기관만 사용 가능합니다, 센터에 문의 필요.\n"
				+ "        , JSON_OBJECT(\"text\", :smsText) \n"
				+ " -- 두번째 컨텐츠(첫번째 컨텐츠 실패시 수행) \n"
				+ "        , (CASE WHEN LENGTH(CONVERT(:smsText USING euckr)) > 90 THEN 'MMS' ELSE 'SMS' END) -- SMS/MMS/ALT/RCS 만 있음 \n"
				+ "        , (CASE WHEN LENGTH(CONVERT(:smsText USING euckr)) > 90 THEN 'LMS' \n"
				+ "                WHEN LENGTH(CONVERT(:smsText USING euckr)) > 2000 THEN 'MMS' \n" 
				+ "                ELSE 'SMS' END)  -- SMS(1~90byte), LMS(91~2000byte)로 처리 해야함, 메시지 내용을 바이트(Byte)로 계산하여 SMS/MMM(LMS)로 처리 해야함 \n"
				+ "        , :smsTitle -- 제목30바이트 이하: 생략 가능 \n"
				+ "        , :smsText \n"
				+ ") \n"
			)
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	// 카카오톡 발송
	Integer nuri2SendContentsSave(
//			@Param("msgKey") Long msgKey,
			@Param("res_date") String res_date,
			@Param("call_to") String call_to,
			@Param("call_from") String call_from,
			@Param("templateCode") String templateCode,
//			@Param("altJson") String altJson,
			@Param("smsTitle") String smsTitle,
			@Param("smsText") String smsText
	);

}
