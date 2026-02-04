package kr.or.kimsn.radarsms.repository;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import kr.or.kimsn.radarsms.dto.SmsSendDto;

public interface SmsSendRepository extends JpaRepository<SmsSendDto, Long> {
	// app contents seq
	@Query(nativeQuery = true, value = "SELECT nuri.appContentNextval() from dual"
	// value = "select seq_currval+1 as seq from nuri.app_contents_sequence"
	)
	Long getAppContentNextval();

	@Query(nativeQuery = true, value = "SELECT count(*) as cnt \n" +
			"FROM (\n" +
			"  SELECT\n" +
			"    'MT'        AS MSG\n" +
			"    -- ,CASE WHEN MSG_TYPE='4' THEN 'SMS' ELSE CASE WHEN MSG_TYPE2 IS NOT NULL THEN  MSG_TYPE2 ELSE 'LMS' END END  AS MSG_TYPE1\n"
			+
			"    ,MSG_TYPE   AS MSG_TYPE\n" +
			"    ,CALL_FROM  AS CALLBACK\n" +
			"    ,CALL_TO    AS RECIVER\n" +
			"    ,CASE WHEN RSLT_NET='SKT' THEN 'SK' ELSE CASE WHEN  RSLT_NET='KT' THEN 'KT' ELSE CASE WHEN RSLT_NET='LGU' THEN 'LG' ELSE '' END END END AS TELCO -- 이통사\n"
			+
			"    ,CUR_STATE     AS CUR_STATE\n" +
			"    ,RSLT_CODE2    AS RSLT_CODE2\n" +
			"    ,CASE WHEN MSG_TYPE=4 THEN SMS_TXT\n" +
			"    ELSE\n" +
			"    ( SELECT CONCAT(CASE WHEN MMS_SUBJECT IS NOT NULL THEN MMS_SUBJECT ELSE''END, '', MMS_BODY) AS MSG_TXT FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"    END AS MSG_TXT\n" +
			"    ,CASE WHEN MSG_TYPE=4 THEN length(SMS_TXT)\n" +
			"    ELSE\n" +
			"    ( SELECT length(MMS_BODY) AS MSG_TXT  FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"    END AS MSG_BYTE\n" +
			"    --          ,(CASE WHEN MSG_IDATE IS NULL THEN REQ_DATE ELSE MSG_IDATE END) AS MSG_IDATE                           -- MT 입력시간  MSG_IDATE 입력시간\n"
			+
			"    ,REQ_DATE   AS REQ_DATE                                                                                -- MT 요청시간  REQ_DATE\n"
			+
			"    ,SENT_DATE  AS SENT_DATE                                                                               -- MT 발송시간  SENT_DATE(접수)\n"
			+
			"    ,RSLT_DATE  AS RSLT_DATE                                                                               -- MT 결과수신  RSLT_DATE(결과수신)\n"
			+
			"    ,MSG_SEQ    AS MSG_KEY                                                                                 -- KEY\n"
			+
			"    ,CONT_SEQ   AS CONT_SEQ                                                                                -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
			+
			"    -- ********************************************************************** --\n" +
			"    -- 가온에 별도 시퀀스 또는 별도 시퀀스사용시\n" +
			"    -- setting.yaml 파일\n" +
			"    -- msgDataSeq: AUTO, 시퀀스\n" +
			"    -- msgContentsInfoSeq: AUTO, 시퀀스\n" +
			"    -- *********************** --\n" +
			"    ,( SELECT APP_GUBUN     FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS APP_CHK         -- APP 처리 후 실패인지 확인\n"
			+
			"    ,( SELECT RSLT_CODE_APP FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS RSLT_CODE_APP   -- APP 처리 후 실패인지 확인\n"
			+
			"    -- ********************************************************************** --\n" +
			"    ,''   AS TEMPLATE_CODE                                                                                 -- TEMPLATE_CODE : MT, MO는 빈값\n"
			+
			"  FROM (\n" +
			"    SELECT * FROM nuri.NURI_MSG_DATA\n" +
			"    WHERE REQ_DATE between :startDate and :endDate \n" +
			"    UNION ALL\n" +
			"    SELECT * FROM nuri.NURI_MSG_LOG_:yearMonth\n" +
			"    WHERE REQ_DATE between :startDate and :endDate \n" +
			"  ) AS msg \n" +
			") MT\n")
	Integer getsmsSendTotalCount(
			@Param("yearMonth") Integer yearMonth,
			@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	@Query(nativeQuery = true, countQuery = "SELECT\n" +
			"  count(*) \n" +
			"FROM (\n" +
			"  SELECT\n" +
			"    'MT'        AS MSG\n" +
			"    -- ,CASE WHEN MSG_TYPE='4' THEN 'SMS' ELSE CASE WHEN MSG_TYPE2 IS NOT NULL THEN  MSG_TYPE2 ELSE 'LMS' END END  AS MSG_TYPE1\n"
			+
			"    ,MSG_TYPE   AS MSG_TYPE\n" +
			"    ,CALL_FROM  AS CALLBACK\n" +
			"    ,CALL_TO    AS RECIVER\n" +
			"    ,CASE WHEN RSLT_NET='SKT' THEN 'SK' ELSE CASE WHEN  RSLT_NET='KT' THEN 'KT' ELSE CASE WHEN RSLT_NET='LGU' THEN 'LG' ELSE '' END END END AS TELCO -- 이통사\n"
			+
			"    ,CUR_STATE     AS CUR_STATE\n" +
			"    ,RSLT_CODE2    AS RSLT_CODE2\n" +
			"    ,CASE WHEN MSG_TYPE=4 THEN SMS_TXT\n" +
			"     ELSE \n" +
			"       ( SELECT CONCAT(CASE WHEN MMS_SUBJECT IS NOT NULL THEN MMS_SUBJECT ELSE''END, '', MMS_BODY) AS MSG_TXT FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"     END AS MSG_TXT\n" +
			"    ,CASE WHEN MSG_TYPE=4 THEN length(SMS_TXT)\n" +
			"     ELSE \n" +
			"      ( SELECT length(MMS_BODY) AS MSG_TXT  FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"     END AS MSG_BYTE\n" +
			"    --          ,(CASE WHEN MSG_IDATE IS NULL THEN REQ_DATE ELSE MSG_IDATE END) AS MSG_IDATE                           -- MT 입력시간  MSG_IDATE 입력시간\n"
			+
			"    ,REQ_DATE   AS REQ_DATE                                                                                -- MT 요청시간  REQ_DATE\n"
			+
			"    ,SENT_DATE  AS SENT_DATE                                                                               -- MT 발송시간  SENT_DATE(접수)\n"
			+
			"    ,RSLT_DATE  AS RSLT_DATE                                                                               -- MT 결과수신  RSLT_DATE(결과수신)\n"
			+
			"    ,MSG_SEQ    AS MSG_KEY                                                                                 -- KEY\n"
			+
			"    ,CONT_SEQ   AS CONT_SEQ                                                                                -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
			+
			"    -- ********************************************************************** --\n" +
			"    -- 가온에 별도 시퀀스 또는 별도 시퀀스사용시\n" +
			"    -- setting.yaml 파일\n" +
			"    -- msgDataSeq: AUTO, 시퀀스\n" +
			"    -- msgContentsInfoSeq: AUTO, 시퀀스\n" +
			"    -- *********************** --\n" +
			"    ,( SELECT APP_GUBUN     FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS APP_CHK         -- APP 처리 후 실패인지 확인\n"
			+
			"    ,( SELECT RSLT_CODE_APP FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS RSLT_CODE_APP   -- APP 처리 후 실패인지 확인\n"
			+
			"    -- ********************************************************************** --\n" +
			"    ,''   AS TEMPLATE_CODE                                                                                 -- TEMPLATE_CODE : MT, MO는 빈값\n"
			+
			"  FROM (\n" +
			"    SELECT * FROM nuri.NURI_MSG_DATA\n" +
			"    WHERE REQ_DATE between :startDate and :endDate \n" +
			"    UNION ALL\n" +
			"    SELECT * FROM nuri.NURI_MSG_LOG_:yearMonth\n" +
			"    WHERE REQ_DATE between :startDate and :endDate \n" +
			"  ) AS msg \n" +
			") MT\n", value = "SELECT\n" +
					"  MSG_KEY as msg_seq,\n" +
					"  REQ_DATE as req_date,\n" +
					"  CUR_STATE as cur_state,\n" +
					"  RECIVER as call_to,\n" +
					"  CALLBACK as call_from,\n" +
					"  MSG_TXT as sms_txt,\n" +
					"  MSG_TYPE as msg_type, \n" +
					"  RSLT_CODE2 as rslt_code2 \n" +
					"FROM (\n" +
					"  SELECT\n" +
					"    'MT'        AS MSG\n" +
					"    -- ,CASE WHEN MSG_TYPE='4' THEN 'SMS' ELSE CASE WHEN MSG_TYPE2 IS NOT NULL THEN  MSG_TYPE2 ELSE 'LMS' END END  AS MSG_TYPE1\n"
					+
					"    ,MSG_TYPE   AS MSG_TYPE\n" +
					"    ,CALL_FROM  AS CALLBACK\n" +
					"    ,CALL_TO    AS RECIVER\n" +
					"    ,CASE WHEN RSLT_NET='SKT' THEN 'SK' ELSE CASE WHEN  RSLT_NET='KT' THEN 'KT' ELSE CASE WHEN RSLT_NET='LGU' THEN 'LG' ELSE '' END END END AS TELCO -- 이통사\n"
					+
					"    ,CUR_STATE     AS CUR_STATE\n" +
					"    ,RSLT_CODE2    AS RSLT_CODE2\n" +
					"    ,CASE WHEN MSG_TYPE=4 THEN SMS_TXT\n" +
					"     ELSE \n" +
					"       ( SELECT CONCAT(CASE WHEN MMS_SUBJECT IS NOT NULL THEN MMS_SUBJECT ELSE''END, '', MMS_BODY) AS MSG_TXT FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
					+
					"     END AS MSG_TXT\n" +
					"    ,CASE WHEN MSG_TYPE=4 THEN length(SMS_TXT)\n" +
					"     ELSE \n" +
					"      ( SELECT length(MMS_BODY) AS MSG_TXT  FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
					+
					"     END AS MSG_BYTE\n" +
					"    --          ,(CASE WHEN MSG_IDATE IS NULL THEN REQ_DATE ELSE MSG_IDATE END) AS MSG_IDATE                           -- MT 입력시간  MSG_IDATE 입력시간\n"
					+
					"    ,REQ_DATE   AS REQ_DATE                                                                                -- MT 요청시간  REQ_DATE\n"
					+
					"    ,SENT_DATE  AS SENT_DATE                                                                               -- MT 발송시간  SENT_DATE(접수)\n"
					+
					"    ,RSLT_DATE  AS RSLT_DATE                                                                               -- MT 결과수신  RSLT_DATE(결과수신)\n"
					+
					"    ,MSG_SEQ    AS MSG_KEY                                                                                 -- KEY\n"
					+
					"    ,CONT_SEQ   AS CONT_SEQ                                                                                -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
					+
					"    -- ********************************************************************** --\n" +
					"    -- 가온에 별도 시퀀스 또는 별도 시퀀스사용시\n" +
					"    -- setting.yaml 파일\n" +
					"    -- msgDataSeq: AUTO, 시퀀스\n" +
					"    -- msgContentsInfoSeq: AUTO, 시퀀스\n" +
					"    -- *********************** --\n" +
					"    ,( SELECT APP_GUBUN     FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS APP_CHK         -- APP 처리 후 실패인지 확인\n"
					+
					"    ,( SELECT RSLT_CODE_APP FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS RSLT_CODE_APP   -- APP 처리 후 실패인지 확인\n"
					+
					"    -- ********************************************************************** --\n" +
					"    ,''   AS TEMPLATE_CODE                                                                                 -- TEMPLATE_CODE : MT, MO는 빈값\n"
					+
					"  FROM (\n" +
					"    SELECT * FROM nuri.NURI_MSG_DATA\n" +
					"    WHERE REQ_DATE between :startDate and :endDate \n" +
					"    UNION ALL\n" +
					"    SELECT * FROM nuri.NURI_MSG_LOG_:yearMonth\n" +
					"    WHERE REQ_DATE between :startDate and :endDate \n" +
					"  ) AS msg \n" +
					") MT\n"
	// "ORDER BY REQ_DATE desc \n"
	// "limit :limitStart, :pageSize \n"
	)
	// List<SmsSendDto> getSmsSendData(
	Page<SmsSendDto> getSmsSendData(
			Pageable pageable,
			// @Param("limitStart") Integer limitStart,
			// @Param("pageSize") Integer pageSize,
			@Param("yearMonth") Integer yearMonth,
			@Param("startDate") String startDate,
			@Param("endDate") String endDate);

	@Query(nativeQuery = true, countQuery = "SELECT count(*) FROM (\n" +
			"  SELECT\n" +
			"     srow.MSG                       -- MT,MO 구분\n" +
			"    --  ,srow.MSG_TYPE1                 -- 구분 텍스트\n" +
			"    ,srow.MSG_TYPE                  -- 구분 코드\n" +
			"    ,srow.CALLBACK                  -- 회신번호 (MO는 발신자)\n" +
			"    ,srow.RECIVER                   -- 발신번호 (MO는 회신번호)\n" +
			"    ,srow.TELCO                     -- 이통사\n" +
			"    ,srow.CUR_STATE                 -- 메시지상태(MT용)\n" +
			"    ,srow.RSLT_CODE2 -- 결과코드(MT용)\n" +
			"    ,srow.MSG_TXT                   -- 메시지내용\n" +
			"    ,srow.MSG_BYTE                  -- 메시지내용\n" +
			"    --  ,srow.MSG_IDATE                 -- MT 입력시간\n" +
			"    ,srow.REQ_DATE                  -- MT 발송요청\n" +
			"    ,srow.SENT_DATE                 -- MT 발송시간\n" +
			"    ,srow.RSLT_DATE                 -- MT 결과수신\n" +
			"    ,srow.MSG_SEQ                   -- KEY\n" +
			"    ,srow.CONT_SEQ                  -- MT에서 MMS 텍스트 조회용 ,MO는 빈값\n" +
			"    ,srow.APP_CHK                   -- APP▶MT 처리 메시지 확인 MO는 빈값\n" +
			"    ,srow.RSLT_CODE_APP             -- APP▶MT 에러코드\n" +
			"    ,srow.TEMPLATE_CODE             -- APP▶TEMPLATE_CODE\n" +
			"  FROM (\n" +
			"-- -- -- MT -- *********************** --\n" +
			"-- MT 용 공통쿼리\n" +
			"-- -- -- MT -- *********************** --\n" +
			"    SELECT * FROM (\n" +
			"      SELECT\n" +
			"        'MT'        AS MSG\n" +
			"        -- ,CASE WHEN MSG_TYPE='4' THEN 'SMS' ELSE CASE WHEN MSG_TYPE2 IS NOT NULL THEN  MSG_TYPE2 ELSE 'LMS' END END  AS MSG_TYPE1\n"
			+
			"        ,MSG_TYPE   AS MSG_TYPE\n" +
			"        ,CALL_FROM  AS CALLBACK\n" +
			"        ,CALL_TO    AS RECIVER\n" +
			"        ,CASE WHEN RSLT_NET='SKT' THEN 'SK' ELSE CASE WHEN  RSLT_NET='KT' THEN 'KT' ELSE CASE WHEN RSLT_NET='LGU' THEN 'LG' ELSE '' END END END AS TELCO -- 이통사\n"
			+
			"        ,CUR_STATE     AS CUR_STATE\n" +
			"        ,RSLT_CODE2    AS RSLT_CODE2\n" +
			"        ,CASE WHEN MSG_TYPE=4 THEN SMS_TXT\n" +
			"        ELSE\n" +
			"        ( SELECT CONCAT(CASE WHEN MMS_SUBJECT IS NOT NULL THEN MMS_SUBJECT ELSE''END, '\n', MMS_BODY) AS MSG_TXT FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"        END AS MSG_TXT\n" +
			"        ,CASE WHEN MSG_TYPE=4 THEN length(SMS_TXT)\n" +
			"        ELSE\n" +
			"        ( SELECT length(MMS_BODY) AS MSG_TXT  FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
			+
			"        END AS MSG_BYTE\n" +
			"        --          ,(CASE WHEN MSG_IDATE IS NULL THEN REQ_DATE ELSE MSG_IDATE END) AS MSG_IDATE                           -- MT 입력시간  MSG_IDATE 입력시간\n"
			+
			"        ,REQ_DATE   AS REQ_DATE                                                                                -- MT 요청시간  REQ_DATE\n"
			+
			"        ,SENT_DATE  AS SENT_DATE                                                                               -- MT 발송시간  SENT_DATE(접수)\n"
			+
			"        ,RSLT_DATE  AS RSLT_DATE                                                                               -- MT 결과수신  RSLT_DATE(결과수신)\n"
			+
			"        ,MSG_SEQ    AS MSG_SEQ                                                                                 -- KEY\n"
			+
			"        ,CONT_SEQ   AS CONT_SEQ                                                                                -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
			+
			"        -- ********************************************************************** --\n" +
			"        -- 가온에 별도 시퀀스 또는 별도 시퀀스사용시\n" +
			"        -- setting.yaml 파일\n" +
			"        -- msgDataSeq: AUTO, 시퀀스\n" +
			"        -- msgContentsInfoSeq: AUTO, 시퀀스\n" +
			"        -- *********************** --\n" +
			"        ,( SELECT APP_GUBUN     FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS APP_CHK         -- APP 처리 후 실패인지 확인\n"
			+
			"        ,( SELECT RSLT_CODE_APP FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS RSLT_CODE_APP   -- APP 처리 후 실패인지 확인\n"
			+
			"        -- ********************************************************************** --\n" +
			"        ,''   AS TEMPLATE_CODE                                                                                 -- TEMPLATE_CODE : MT, MO는 빈값\n"
			+
			"      FROM (\n" +
			"        SELECT * FROM nuri.NURI_MSG_DATA\n" +
			"        WHERE REQ_DATE between :startDate and :endDate\n" +
			"        UNION ALL\n" +
			"        SELECT * FROM nuri.NURI_MSG_LOG_:yearMonth\n" +
			"        WHERE REQ_DATE between :startDate and :endDate\n" +
			"      ) AS msg\n" +
			"    ) MT\n" +
			"-- -- -- MT -- *********************** --\n" +
			"-- MT 용 공통쿼리\n" +
			"-- -- -- MT -- *********************** --\n" +
			"-- ****************************************************************\n" +
			"    UNION ALL\n" +
			"-- ****************************************************************\n" +
			"-- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
			"-- APP G/W  공통쿼리(LMS)\n" +
			"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
			"    SELECT * FROM (\n" +
			"      SELECT\n" +
			"        'AP'               AS MSG\n" +
			"        -- ,s.APP_GUBUN        AS MSG_TYPE1\n" +
			"        ,s.APP_GUBUN        AS MSG_TYPE\n" +
			"        ,s.CALL_BACK        AS CALLBACK\n" +
			"        ,s.PHONE_NUM        AS RECIVER\n" +
			"        ,s.APP_GUBUN        AS TELCO\n" +
			"        ,s.CUR_STATE        AS CUR_STATE\n" +
			"        ,s.RSLT_CODE_APP    AS RSLT_CODE2\n" +
			"        ,(CASE WHEN s.GAON_MSG_TYPE='L' THEN c.MSG_DATA ELSE s.SMS_MSG_DATA END) AS MSG_TXT\n" +
			"        ,LENGTH(c.MSG_DATA) AS MSG_BYTE\n" +
			"        --     ,date_format(s.MSG_IDATE,'%Y-%m-%d %H:%i:%s')        AS MSG_IDATE           -- MT 입력시간 MSG_IDATE 입력시간\n"
			+
			"        ,date_format(s.REQ_SEND_DATE,'%Y-%m-%d %H:%i:%s')    AS REQ_DATE            -- MT 요청시간 REQ_DATE\n"
			+
			"        ,date_format(s.MODULE_SEND_TIME,'%Y-%m-%d %H:%i:%s') AS SENT_DATE           -- MT 발송시간 SENT_DATE(접수)\n"
			+
			"        ,date_format(s.APP_RECV_TIME,'%Y-%m-%d %H:%i:%s')    AS RSLT_DATE           -- MT 결과수신 RSLT_DATE(결과수신)\n"
			+
			"        ,s.MSG_SEQ          AS MSG_SEQ                                              -- KEY\n" +
			"        ,s.PACK_UNIQUEKEY   AS CONT_SEQ                                             -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
			+
			"        ,'' AS APP_CHK                                                              -- APP 처리 후 실패인지 확인\n"
			+
			"        ,'' AS RSLT_CODE_APP                                                        -- APP 처리 후 실패인지 확인\n"
			+
			"        ,s.TEMPLATE_CODE AS TEMPLATE_CODE                                           -- TEMPLATE_CODE : MT, MO는 빈값\n"
			+
			"      FROM(\n" +
			"        SELECT * FROM nuri.app_send_data\n" +
			"        WHERE  date_format(REQ_SEND_DATE,'%Y%m%d%H%i%s') between :startDate and :endDate -- AND CUR_STATE>2\n"
			+
			"        UNION ALL\n" +
			"        SELECT * FROM nuri.app_send_data_log\n" +
			"        WHERE  date_format(REQ_SEND_DATE,'%Y%m%d%H%i%s') between :startDate and :endDate -- AND CUR_STATE>2\n"
			+
			"      ) s\n" +
			"      LEFT OUTER JOIN nuri.app_send_contents c\n" +
			"      ON (s.PACK_UNIQUEKEY=c.PACK_UNIQUEKEY)\n" +
			"      ORDER BY c.REQ_SEND_DATE DESC, s.MSG_SEQ desc\n" +
			"    ) applog_lms\n" +
			"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
			"-- APP G/W  공통쿼리\n" +
			"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
			"  ) srow\n" +
			") chat\n" +
			"where 1=1 \n" +
			"and (:smsSUC is null or chat.rslt_code2 in ('0', '0000')) \n" +
			"and (:smsFail is null or chat.rslt_code2 not in ('0', '0000')) \n", value = "SELECT * FROM (\n" +
					"  SELECT\n" +
					"     srow.MSG                      -- MT,MO 구분\n" +
					"    --  ,srow.MSG_TYPE1                 -- 구분 텍스트\n" +
					"    ,srow.MSG_TYPE as msg_type                 -- 구분 코드\n" +
					"    ,srow.CALLBACK as call_to                 -- 회신번호 (MO는 발신자)\n" +
					"    ,srow.RECIVER as call_from                  -- 발신번호 (MO는 회신번호)\n" +
					"    ,srow.TELCO                     -- 이통사\n" +
					"    ,srow.CUR_STATE as cur_state                -- 메시지상태(MT용)\n" +
					"    ,srow.RSLT_CODE2 as rslt_code2                -- 결과코드(MT용)\n" +
					// " ,(select DESCRIPTION from nuri.app_error_code where CAST(app_code AS
					// BINARY) = srow.RSLT_CODE2) as rslt_code2 -- 결과코드(MT용) \n" +
					"    ,srow.MSG_TXT as sms_txt                  -- 메시지내용\n" +
					"    ,srow.MSG_BYTE                  -- 메시지내용\n" +
					"    --  ,srow.MSG_IDATE                 -- MT 입력시간\n" +
					"    ,srow.REQ_DATE as req_date                 -- MT 발송요청\n" +
					"    ,srow.SENT_DATE                 -- MT 발송시간\n" +
					"    ,srow.RSLT_DATE                 -- MT 결과수신\n" +
					"    ,srow.MSG_SEQ AS msg_seq                   -- KEY\n" +
					"    ,srow.CONT_SEQ                  -- MT에서 MMS 텍스트 조회용 ,MO는 빈값\n" +
					"    ,srow.APP_CHK                   -- APP▶MT 처리 메시지 확인 MO는 빈값\n" +
					"    ,srow.RSLT_CODE_APP             -- APP▶MT 에러코드\n" +
					"    ,srow.TEMPLATE_CODE             -- APP▶TEMPLATE_CODE\n" +
					"  FROM (\n" +
					"-- -- -- MT -- *********************** --\n" +
					"-- MT 용 공통쿼리\n" +
					"-- -- -- MT -- *********************** --\n" +
					"    SELECT * FROM (\n" +
					"      SELECT\n" +
					"        'MT'        AS MSG\n" +
					"        -- ,CASE WHEN MSG_TYPE='4' THEN 'SMS' ELSE CASE WHEN MSG_TYPE2 IS NOT NULL THEN  MSG_TYPE2 ELSE 'LMS' END END  AS MSG_TYPE1\n"
					+
					"        ,MSG_TYPE   AS MSG_TYPE\n" +
					"        ,CALL_FROM  AS CALLBACK\n" +
					"        ,CALL_TO    AS RECIVER\n" +
					"        ,CASE WHEN RSLT_NET='SKT' THEN 'SK' ELSE CASE WHEN  RSLT_NET='KT' THEN 'KT' ELSE CASE WHEN RSLT_NET='LGU' THEN 'LG' ELSE '' END END END AS TELCO -- 이통사\n"
					+
					"        ,CUR_STATE     AS CUR_STATE\n" +
					"        ,RSLT_CODE2    AS RSLT_CODE2\n" +
					"        ,CASE WHEN MSG_TYPE=4 THEN SMS_TXT\n" +
					"        ELSE\n" +
					"        ( SELECT CONCAT(CASE WHEN MMS_SUBJECT IS NOT NULL THEN MMS_SUBJECT ELSE''END, '\n', MMS_BODY) AS MSG_TXT FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
					+
					"        END AS MSG_TXT\n" +
					"        ,CASE WHEN MSG_TYPE=4 THEN length(SMS_TXT)\n" +
					"        ELSE\n" +
					"        ( SELECT length(MMS_BODY) AS MSG_TXT  FROM nuri.NURI_MMS_CONTENTS_INFO mms WHERE mms.CONT_SEQ=msg.CONT_SEQ )\n"
					+
					"        END AS MSG_BYTE\n" +
					"        --          ,(CASE WHEN MSG_IDATE IS NULL THEN REQ_DATE ELSE MSG_IDATE END) AS MSG_IDATE                           -- MT 입력시간  MSG_IDATE 입력시간\n"
					+
					"        ,REQ_DATE   AS REQ_DATE                                                                                -- MT 요청시간  REQ_DATE\n"
					+
					"        ,SENT_DATE  AS SENT_DATE                                                                               -- MT 발송시간  SENT_DATE(접수)\n"
					+
					"        ,RSLT_DATE  AS RSLT_DATE                                                                               -- MT 결과수신  RSLT_DATE(결과수신)\n"
					+
					"        ,MSG_SEQ    AS MSG_SEQ                                                                                 -- KEY\n"
					+
					"        ,CONT_SEQ   AS CONT_SEQ                                                                                -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
					+
					"        -- ********************************************************************** --\n" +
					"        -- 가온에 별도 시퀀스 또는 별도 시퀀스사용시\n" +
					"        -- setting.yaml 파일\n" +
					"        -- msgDataSeq: AUTO, 시퀀스\n" +
					"        -- msgContentsInfoSeq: AUTO, 시퀀스\n" +
					"        -- *********************** --\n" +
					"        ,( SELECT APP_GUBUN     FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS APP_CHK         -- APP 처리 후 실패인지 확인\n"
					+
					"        ,( SELECT RSLT_CODE_APP FROM nuri.app_send_data_log a where a.NURI_MSG_SEQ=msg.MSG_SEQ ) AS RSLT_CODE_APP   -- APP 처리 후 실패인지 확인\n"
					+
					"        -- ********************************************************************** --\n" +
					"        ,''   AS TEMPLATE_CODE                                                                                 -- TEMPLATE_CODE : MT, MO는 빈값\n"
					+
					"      FROM (\n" +
					"        SELECT * FROM nuri.NURI_MSG_DATA\n" +
					"        WHERE REQ_DATE between :startDate and :endDate\n" +
					"        UNION ALL\n" +
					"        SELECT * FROM nuri.NURI_MSG_LOG_:yearMonth\n" +
					"        WHERE REQ_DATE between :startDate and :endDate\n" +
					"      ) AS msg\n" +
					"    ) MT\n" +
					"-- -- -- MT -- *********************** --\n" +
					"-- MT 용 공통쿼리\n" +
					"-- -- -- MT -- *********************** --\n" +
					"-- ****************************************************************\n" +
					"    UNION ALL\n" +
					"-- ****************************************************************\n" +
					"-- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
					"-- APP G/W  공통쿼리(LMS)\n" +
					"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
					"    SELECT * FROM (\n" +
					"      SELECT\n" +
					"        'AP'               AS MSG\n" +
					"        -- ,s.APP_GUBUN        AS MSG_TYPE1\n" +
					"        ,s.APP_GUBUN        AS MSG_TYPE\n" +
					"        ,s.CALL_BACK        AS CALLBACK\n" +
					"        ,s.PHONE_NUM        AS RECIVER\n" +
					"        ,s.APP_GUBUN        AS TELCO\n" +
					"        ,s.CUR_STATE        AS CUR_STATE\n" +
					"        ,s.RSLT_CODE_APP    AS RSLT_CODE2\n" +
					"        ,(CASE WHEN s.GAON_MSG_TYPE='L' THEN c.MSG_DATA ELSE s.SMS_MSG_DATA END) AS MSG_TXT\n" +
					"        ,LENGTH(c.MSG_DATA) AS MSG_BYTE\n" +
					"        --     ,date_format(s.MSG_IDATE,'%Y-%m-%d %H:%i:%s')        AS MSG_IDATE           -- MT 입력시간 MSG_IDATE 입력시간\n"
					+
					"        ,date_format(s.REQ_SEND_DATE,'%Y-%m-%d %H:%i:%s')    AS REQ_DATE            -- MT 요청시간 REQ_DATE\n"
					+
					"        ,date_format(s.MODULE_SEND_TIME,'%Y-%m-%d %H:%i:%s') AS SENT_DATE           -- MT 발송시간 SENT_DATE(접수)\n"
					+
					"        ,date_format(s.APP_RECV_TIME,'%Y-%m-%d %H:%i:%s')    AS RSLT_DATE           -- MT 결과수신 RSLT_DATE(결과수신)\n"
					+
					"        ,s.MSG_SEQ          AS MSG_SEQ                                              -- KEY\n" +
					"        ,s.PACK_UNIQUEKEY   AS CONT_SEQ                                             -- MT: 첨부파일참조용 텍스트 조회용 ,MO는 빈값\n"
					+
					"        ,'' AS APP_CHK                                                              -- APP 처리 후 실패인지 확인\n"
					+
					"        ,'' AS RSLT_CODE_APP                                                        -- APP 처리 후 실패인지 확인\n"
					+
					"        ,s.TEMPLATE_CODE AS TEMPLATE_CODE                                           -- TEMPLATE_CODE : MT, MO는 빈값\n"
					+
					"      FROM(\n" +
					"        SELECT * FROM nuri.app_send_data\n" +
					"        WHERE  date_format(REQ_SEND_DATE,'%Y%m%d%H%i%s') between :startDate and :endDate -- AND CUR_STATE>2\n"
					+
					"        UNION ALL\n" +
					"        SELECT * FROM nuri.app_send_data_log\n" +
					"        WHERE  date_format(REQ_SEND_DATE,'%Y%m%d%H%i%s') between :startDate and :endDate -- AND CUR_STATE>2\n"
					+
					"      ) s\n" +
					"      LEFT OUTER JOIN nuri.app_send_contents c\n" +
					"      ON (s.PACK_UNIQUEKEY=c.PACK_UNIQUEKEY)\n" +
					"      ORDER BY c.REQ_SEND_DATE DESC, s.MSG_SEQ desc\n" +
					"    ) applog_lms\n" +
					"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
					"-- APP G/W  공통쿼리\n" +
					"-- -- -- APP 알림톡,네이버 토스 -- *********************** --\n" +
					"  ) srow\n" +
					") chat\n" +
					"where 1=1 \n" +
					"and (:smsSUC is null or chat.rslt_code2 in ('0', '0000')) \n" +
					"and (:smsFail is null or chat.rslt_code2 not in ('0', '0000')) \n"
	// "-- ORDER BY REQ_DATE desc, REQ_DATE -- limit 0, 10\n"
	)
	// 문자 전송 내역
	Page<SmsSendDto> getAppSendData(
			Pageable pageable,
			// @Param("limitStart") Integer limitStart,
			// @Param("pageSize") Integer pageSize,
			@Param("yearMonth") Integer yearMonth,
			@Param("startDate") String startDate,
			@Param("endDate") String endDate,
			@Param("smsSUC") String smsSUC,
			@Param("smsFail") String smsFail);

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
			"  PACK_UNIQUEKEY\n" +
			", MSG_SUBJECT\n" +
			", MSG_DATA\n" +
			", MSG_TYPE\n" +
			") VALUES (\n" +
			" :appnextval  -- = PACK_UNIQUEKEY : 컨텐츠 일련번호\n" +
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
			", :req_date       -- = 접수날짜(발송요청시간 : 미래시간은 예약발송, 과거시간은 3시간이내이면 즉시발송.)\n" +
			", :call_from        -- CALL_TO = 발신번호, 숫자형태의 문자(숫자만 입력)\n" +
			", :call_to      -- CALL_FROM  = 수신번호 (숫자만 입력)\n" +
			", '0'             -- = 접수요청 = 0 (반드시 0으로만 입력)\n" +
			", 'KAKAO'         -- = 앱메시지 구분 : KAKAO(카카오), NAVER(네이버) 대문자로 입력\n" +
			", :templateCode   -- = 사용할 템플릿코드\n" +
			// ", 'template_0001' -- = 사용할 템플릿코드\n" +
			", 'L'             -- = GAON_MSG_TYPE ='L' 입력 고정 (실패시 자동 바이트 계산 후 SMS/LMS 재접수 처리)\n" +
			")\n")
	@Transactional
	@Modifying
	// 카카오톡 발송(전화번호)
	Integer gaonAppSendDataSave(
			@Param("appNextval") Long appNextval,
			@Param("req_date") String req_date,
			@Param("call_to") String call_to,
			@Param("call_from") String call_from,
			@Param("templateCode") String templateCode);

}
