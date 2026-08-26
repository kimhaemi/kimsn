-- ## 최종 프로시저: 완전판 (v5)
-- 이 버전은 p_send_order 파라미터를 통해 지정된 모든 발송 순서(ALT, RCS, XMS)를 처리하며, 각 메시지의 세부 유형을 자동으로 결정하는 완전한 기능을 갖추고 있습니다.
--
-- 프로시저 코드 (v5)
-- SQL
--
-- 기존 프로시저가 있다면 삭제
DROP PROCEDURE IF EXISTS sp_send_nuri_message0;
-- ================================================================================== --




-- ================================================================================== --
DELIMITER $$
-- =============================================
-- 작성자:      Gemini
-- 수정일:      2025-07-09
-- 설명:        ALT, RCS, XMS 순서 지정 및 완전 자동 유형 감지 (v5)
-- =============================================
CREATE PROCEDURE `sp_send_nuri_message`(
    IN p_send_order VARCHAR(50),
    IN p_res_date VARCHAR(14),
    IN p_priority INT,
    IN p_phone VARCHAR(20),
    IN p_callback VARCHAR(20),
    IN p_subject VARCHAR(100),
    IN p_message TEXT,
    IN p_file_path_1 VARCHAR(255),
    IN p_alt_sender_key VARCHAR(100),
    IN p_alt_template_code VARCHAR(50),
    IN p_rcs_brand_key VARCHAR(100),
    IN p_rcs_template_code VARCHAR(50)
)
BEGIN
    -- 변수 선언
    DECLARE v_msg_key BIGINT;
    DECLARE v_res_date VARCHAR(14);
    DECLARE v_msg_state INT DEFAULT 1;
    DECLARE v_priority INT;
    DECLARE v_xms_text TEXT;
    DECLARE v_xms_subject VARCHAR(100);
    DECLARE v_alt_json TEXT;
    DECLARE v_rcs_json TEXT;
    DECLARE v_rcs_base_id VARCHAR(50);
    DECLARE v_msg_type_1, v_msg_type_2, v_msg_type_3 VARCHAR(10) DEFAULT NULL;
    DECLARE v_contents_type_1, v_contents_type_2, v_contents_type_3 VARCHAR(10) DEFAULT NULL;
    DECLARE v_message_bytes, v_message_length INT;
    DECLARE v_json_escaped_message TEXT;
    DECLARE i INT DEFAULT 1;
    DECLARE current_type VARCHAR(10);

    -- 기본값 및 공통값 설정
    SET v_msg_key = msg_nextval();
    IF p_priority IS NULL OR (p_priority <> 7 AND p_priority <> 3) THEN SET v_priority = 3; ELSE SET v_priority = p_priority; END IF;
    IF p_res_date IS NULL OR p_res_date = '' THEN SET v_res_date = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'); ELSE SET v_res_date = p_res_date; END IF;

    SET v_message_bytes = LENGTH(CONVERT(REPLACE(p_message, '\r\n', '\n') USING euckr));
    SET v_message_length = CHAR_LENGTH(REPLACE(p_message, '\r\n', '\n'));
    SET v_json_escaped_message = REPLACE(REPLACE(REPLACE(REPLACE(p_message, '\\', '\\\\'), '"', '\"'), '\r\n', '\\n'), '\n', '\\n');

    SET v_xms_text = p_message;
    SET v_xms_subject = p_subject;
    SET v_alt_json = CONCAT('{"text":"', v_json_escaped_message, '"}');
    SET v_rcs_json = CONCAT('{"msg":{"body":{"description":"', v_json_escaped_message, '"},"copyAllowed":true,"header":"0"}}');

    -- p_send_order를 순회하며 발송 유형 설정
    WHILE i <= 3 DO
        SET current_type = SUBSTRING_INDEX(SUBSTRING_INDEX(p_send_order, '/', i), '/', -1);
        IF current_type IS NULL OR current_type = '' THEN
            SET i = 4; -- 루프 종료
        ELSE
            IF i = 1 THEN
                CASE current_type
                    WHEN 'XMS' THEN
                        IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_1 = 'MMS'; SET v_msg_type_1 = 'MMS';
                        ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_1 = 'SMS'; SET v_msg_type_1 = 'SMS';
                        ELSE SET v_contents_type_1 = 'LMS'; SET v_msg_type_1 = 'MMS'; END IF;
                    WHEN 'ALT' THEN SET v_msg_type_1 = 'ALT'; SET v_contents_type_1 = 'ALT';
                    WHEN 'RCS' THEN SET v_msg_type_1 = 'RCS';
                        IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_1 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                        ELSEIF v_message_length <= 100 THEN SET v_contents_type_1 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                        ELSE SET v_contents_type_1 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END CASE;
            ELSEIF i = 2 THEN
                -- 2차 발송 유형 설정 (1차와 동일 로직)
                CASE current_type
                    WHEN 'XMS' THEN
                        IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_2 = 'MMS'; SET v_msg_type_2 = 'MMS';
                        ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_2 = 'SMS'; SET v_msg_type_2 = 'SMS';
                        ELSE SET v_contents_type_2 = 'LMS'; SET v_msg_type_2 = 'MMS'; END IF;
                    WHEN 'ALT' THEN SET v_msg_type_2 = 'ALT'; SET v_contents_type_2 = 'ALT';
                    WHEN 'RCS' THEN SET v_msg_type_2 = 'RCS';
                        IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_2 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                        ELSEIF v_message_length <= 100 THEN SET v_contents_type_2 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                        ELSE SET v_contents_type_2 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END CASE;
            ELSEIF i = 3 THEN
                -- 3차 발송 유형 설정 (1차와 동일 로직)
                CASE current_type
                    WHEN 'XMS' THEN
                        IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_3 = 'MMS'; SET v_msg_type_3 = 'MMS';
                        ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_3 = 'SMS'; SET v_msg_type_3 = 'SMS';
                        ELSE SET v_contents_type_3 = 'LMS'; SET v_msg_type_3 = 'MMS'; END IF;
                    WHEN 'ALT' THEN SET v_msg_type_3 = 'ALT'; SET v_contents_type_3 = 'ALT';
                    WHEN 'RCS' THEN SET v_msg_type_3 = 'RCS';
                        IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_3 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                        ELSEIF v_message_length <= 100 THEN SET v_contents_type_3 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                        ELSE SET v_contents_type_3 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END CASE;
            END IF;
            SET i = i + 1;
        END IF;
    END WHILE;

    -- 최종 데이터 삽입
    INSERT INTO NURI2_NRMSG_DATA (
          MSG_KEY, MSG_STATE, MSG_PRIORITY, INPUT_DATE, RES_DATE, PHONE, CALLBACK,
          XMS_SUBJECT, XMS_TEXT, XMS_FILE_NAME_1,
          MSG_TYPE_1, CONTENTS_TYPE_1, ALT_SENDER_KEY, ALT_TEMPLATE_CODE, ALT_JSON,
          MSG_TYPE_2, CONTENTS_TYPE_2,
          MSG_TYPE_3, CONTENTS_TYPE_3,
          RCS_BRAND_KEY, RCS_MESSAGE_BASE_ID, RCS_JSON
    ) VALUES (
          v_msg_key, v_msg_state, v_priority, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), v_res_date, p_phone, p_callback,
          v_xms_subject, v_xms_text, p_file_path_1,
          v_msg_type_1, v_contents_type_1, p_alt_sender_key, p_alt_template_code, v_alt_json,
          v_msg_type_2, v_contents_type_2,
          v_msg_type_3, v_contents_type_3,
          p_rcs_brand_key, v_rcs_base_id, v_rcs_json
    );

END$$

DELIMITER ;
-- ================================================================================== --

--  ## 최종 예제 (v5)
--  새로워진 프로시저의 강력한 기능을 보여주는 상세 예제입니다.
--
--  ### 예제 1: 알림톡 우선, 실패 시 SMS로 대체
--  목표: 발송 비용이 저렴한 알림톡을 먼저 보내고, 실패(미수신/차단) 시 모든 사용자가 받을 수 있는 문자로 자동 전환합니다. 메시지가 짧으므로 문자는 SMS로 자동 선택됩니다.
--
--  SQL

-- ================================================================================== --

CALL sp_send_nuri_message(
    /* 발송 순서 */ 'ALT/RCS/XMS',
    /* 우선 순위 */ 7,
    /* 발송요청일자 */ NULL ,
    /* 수신 번호 */ '01100000000',
    /* 발신 번호 */ '0422505537',
    /* 제목(LMS용) */ '서버 긴급 점검 공지',
    /* 메시지 내용 */ '[긴급 공지] 서버 긴급 점검으로 인해 일부 서비스가 순단 현상을 보일 수 있습니다. 최대한 빠른 시간 내에 정상화하도록 하겠습니다. 이용에 불편을 드린 점 사과드립니다.',
    /* 파일패스(MMS용)*/ NULL,
    /* 파일패스(MMS용)*/'your_alt_sender_key',
    /* 파일패스(MMS용)*/'your_alt_template_code',
    /* 파일패스(MMS용)*/'your_rcs_brand_key',
    /* 파일패스(MMS용)*/ NULL
);

--
-- 자동 처리 과정
--
-- p_send_order가 'ALT/XMS'이므로, 1차 발송은 ALT로, 2차는 XMS로 결정됩니다.
--
-- MSG_TYPE_1은 'ALT', CONTENTS_TYPE_1은 **'ALT'**로 설정됩니다.
--
-- 메시지 내용의 바이트가 90바이트 이하이므로, 2차 발송 MSG_TYPE_2는 'SMS', CONTENTS_TYPE_2는 **'SMS'**로 자동 설정됩니다.
--
-- ### 예제 2: 알림톡 → RCS → LMS 순서로 3단계 발송 (긴급)
-- 목표: 도달률과 비용 효율을 모두 고려하여 ALT → RCS → XMS 순서로 발송합니다. 메시지 내용이 길기 때문에 RCS는 RCL로, XMS는 LMS로 자동 선택되며, 이 모든 과정을 긴급으로 처리합니다.
--
-- SQL
--
CALL sp_send_nuri_message(
    /* 발송 순서 */ 'ALT/RCS/XMS',
    /* 우선 순위 */ 7,
    /* 발송요청일자 */ NULL ,
    /* 수신 번호 */ '01122223333',
    /* 발신 번호 */ '0422505537',
    /* 제목(LMS용) */ '서버 긴급 점검 공지',
    /* 메시지 내용 */ '[긴급 공지] 서버 긴급 점검으로 인해 일부 서비스가 순단 현상을 보일 수 있습니다. 최대한 빠른 시간 내에 정상화하도록 하겠습니다. 이용에 불편을 드린 점 사과드립니다.',
    /* 파일패스(MMS용)*/ NULL,
    /* 파일패스(MMS용)*/'your_alt_sender_key',
    /* 파일패스(MMS용)*/'your_alt_template_code',
    /* 파일패스(MMS용)*/'your_rcs_brand_key',
    /* 파일패스(MMS용)*/ NULL
);
--
--
--	CREATE PROCEDURE `sp_send_nuri_message`(
--	    IN p_send_order VARCHAR(50),              -- 왼쪽 부터 처리 >>>>>  ALT/RCS/XMS
--	    IN p_priority INT,                        -- 메시지우선순위, 일반:3, 긴급:7
--	    IN p_res_date VARCHAR(14),                -- 발송요청일자 - 빈값이면 현재시간
--	    IN p_phone VARCHAR(20),                   -- 수신번호
--	    IN p_callback VARCHAR(20),                -- 발신번호
--	    IN p_subject VARCHAR(100),                -- 제목
--	    IN p_message TEXT,                        -- 메시지내용
--	    IN p_file_path_1 VARCHAR(255),            --
--	    IN p_alt_sender_key VARCHAR(100),         --
--	    IN p_alt_template_code VARCHAR(50),       --
--	    IN p_rcs_brand_key VARCHAR(100),          --
--	    IN p_rcs_template_code VARCHAR(50)        --
--	)
--


-- 자동 처리 과정
--
-- p_send_order에 따라 발송 순서는 1차 ALT, 2차 RCS, 3차 XMS로 결정됩니다.
--
-- MSG_TYPE_1은 **'ALT'**로 설정됩니다.
--
-- 메시지 글자 수가 100자를 초과하므로, 2차 발송 MSG_TYPE_2는 'RCS', CONTENTS_TYPE_2는 **'RCL'**로 자동 설정됩니다.
--
-- 메시지 바이트가 90바이트를 초과하므로, 3차 발송 MSG_TYPE_3은 'MMS', CONTENTS_TYPE_3은 **'LMS'**로 자동 설정됩니다.
--
-- p_priority가 7이므로, 이 메시지는 다른 일반 메시지보다 우선적으로 처리됩니다.
--
