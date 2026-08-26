## 최종 프로시저: 버튼 기능 탑재 (v6)
이 버전은 p_send_order를 통한 자동 유형 감지 기능과 알림톡/RCS 버튼 생성 기능을 모두 갖춘 완성형 프로시저입니다.

프로시저 코드 (v6)
SQL

-- =============================================
-- 작성자:      Gemini
-- 수정일:      2025-07-09
-- 설명:        버튼 생성 기능을 포함한 최종 완전판 (v6)
-- =============================================

-- 기존 프로시저가 있다면 삭제
DROP PROCEDURE IF EXISTS z_sp_send_nuri_message;

DELIMITER $$
CREATE PROCEDURE `z_sp_send_nuri_message`(
    -- 기본 파라미터
    IN p_send_order VARCHAR(50),
    IN p_phone VARCHAR(20),
    IN p_callback VARCHAR(20),
    IN p_message TEXT,
    IN p_priority INT,
    IN p_subject VARCHAR(100),
    IN p_res_date VARCHAR(14),
    IN p_file_path_1 VARCHAR(255),
    -- ALT 파라미터 (+버튼)
    IN p_alt_sender_key VARCHAR(100),
    IN p_alt_template_code VARCHAR(50),
    IN p_alt_button_name VARCHAR(50),
    IN p_alt_button_url VARCHAR(255),
    -- RCS 파라미터 (+버튼)
    IN p_rcs_brand_key VARCHAR(100),
    IN p_rcs_template_code VARCHAR(50),
    IN p_rcs_button_text VARCHAR(50),
    IN p_rcs_button_url VARCHAR(255)
)
BEGIN
    -- 변수 선언
    DECLARE v_msg_key BIGINT;
    DECLARE v_res_date VARCHAR(14);
    DECLARE v_msg_state INT DEFAULT 1;
    DECLARE v_priority INT;
    DECLARE v_xms_text TEXT;
    DECLARE v_xms_subject VARCHAR(100);
    DECLARE v_alt_json, v_rcs_json TEXT;
    DECLARE v_rcs_base_id VARCHAR(50);
    DECLARE v_msg_type_1, v_msg_type_2, v_msg_type_3 VARCHAR(10) DEFAULT NULL;
    DECLARE v_contents_type_1, v_contents_type_2, v_contents_type_3 VARCHAR(10) DEFAULT NULL;
    DECLARE v_message_bytes, v_message_length INT;
    DECLARE v_json_escaped_message, v_alt_button_json, v_rcs_button_json TEXT;
    DECLARE i INT DEFAULT 1;
    DECLARE current_type VARCHAR(10);

    -- 기본값 및 공통값 설정
    SET v_msg_key = msg_nextval();
    IF p_priority IS NULL THEN SET v_priority = 3; ELSE SET v_priority = p_priority; END IF;
    IF p_res_date IS NULL OR p_res_date = '' THEN SET v_res_date = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'); ELSE SET v_res_date = p_res_date; END IF;
    SET v_message_bytes = LENGTH(CONVERT(REPLACE(p_message, '\r\n', '\n') USING euckr));
    SET v_message_length = CHAR_LENGTH(REPLACE(p_message, '\r\n', '\n'));
    SET v_json_escaped_message = REPLACE(REPLACE(REPLACE(REPLACE(p_message, '\\', '\\\\'), '"', '\"'), '\r\n', '\\n'), '\n', '\\n');

    -- 버튼 JSON 생성
    IF p_alt_button_name IS NOT NULL AND p_alt_button_url IS NOT NULL THEN
        SET v_alt_button_json = CONCAT(',"button":[{"name":"', p_alt_button_name, '","type":"WL","url_mobile":"', p_alt_button_url, '","url_pc":"', p_alt_button_url, '"}]');
    ELSE SET v_alt_button_json = ''; END IF;
    IF p_rcs_button_text IS NOT NULL AND p_rcs_button_url IS NOT NULL THEN
        SET v_rcs_button_json = CONCAT(',"buttons":[{"suggestions":[{"action":{"displayText":"', p_rcs_button_text, '","urlAction":{"openUrl":{"url":"', p_rcs_button_url, '"}}}}]}');
    ELSE SET v_rcs_button_json = ''; END IF;

    -- 최종 JSON 본문 조합
    SET v_xms_text = p_message;
    SET v_xms_subject = p_subject;
    SET v_alt_json = CONCAT('{"text":"', v_json_escaped_message, '"', v_alt_button_json, '}');
    SET v_rcs_json = CONCAT('{"msg":{"body":{"description":"', v_json_escaped_message, '"}', v_rcs_button_json, ',"copyAllowed":true,"header":"0"}}');

    -- p_send_order를 순회하며 발송 유형 설정 (v5와 동일 로직)
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
          v_msg_key, v_msg_state, v_priority, DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'), DATE_FORMAT(v_res_date, '%Y%m%d%H%i%s'), p_phone, p_callback,
          v_xms_subject, v_xms_text, p_file_path_1,
          v_msg_type_1, v_contents_type_1, p_alt_sender_key, p_alt_template_code, v_alt_json,
          v_msg_type_2, v_contents_type_2,
          v_msg_type_3, v_contents_type_3,
          p_rcs_brand_key, v_rcs_base_id, v_rcs_json
    );

END$$
DELIMITER ;








핵심 로직(WHILE, INSERT)은 이전 답변(v5)과 동일하므로, 버튼 JSON을 생성하고 조합하는 부분이 추가된 것으로 이해하시면 됩니다.

##  버튼 포함 최종 예제 (v6)
### 예제 1: 버튼 있는 알림톡 우선, 실패 시 LMS로 대체
목표: "자세히 보기" 버튼이 포함된 알림톡을 먼저 보내고, 실패 시 자동으로 LMS를 발송합니다.

SQL

CALL z_sp_send_nuri_message(
    /* 발송 순서 */ 'ALT/XMS',
    /* 수신 번호 */ '01112345678',
    /* 발신 번호 */ '0422505537',
    /* 메시지 내용 */ '[개인정보 처리 방침 개정 안내] 2025년 8월 1일부터 새로운 개인정보 처리 방침이 적용됩니다. 변경되는 내용을 확인하시고 서비스 이용에 참고해주시기 바랍니다.',
    /* 우선 순위 */ 3,  -- 3:일반, 7:최우선 처리
    /* 제       목 */ '개인정보 처리 방침 개정 안내',
    NULL, -- 예약 시간 -- YYYYMMDDHH24MISS -- 연원일시분초
    NULL, -- 첨부 파일
    /* ALT 정보 */ 'your_alt_sender_key', 'your_template_code',
    /* ALT 버튼 */ '자세히 보기', 'https://mgov.go.kr/privacy.html',
    /* RCS 정보 */ NULL, NULL, NULL, NULL
);
자동 처리 과정

p_send_order가 'ALT/XMS'이므로 1차 ALT, 2차 XMS 발송을 준비합니다.

p_alt_button_name과 p_alt_button_url 값이 있으므로, 버튼이 포함된 ALT_JSON을 생성합니다.

메시지 바이트가 90바이트를 초과하므로, 2차 발송될 XMS는 LMS로 자동 결정됩니다.

### 예제 2: 버튼 있는 RCS 단독 발송 (RCL로 자동 선택)
목표: 100자가 넘는 메시지를 "설문 참여" 버튼과 함께 RCS로만 발송합니다. 프로시저는 자동으로 이 메시지를 RCS-LMS(RCL) 타입으로 처리합니다.

SQL

CALL z_sp_send_nuri_message(
    /* 발송 순서 */ 'RCS',
    /* 수신 번호 */ '01122223333',
    /* 발신 번호 */ '0422505537',
    /* 메시지 내용 */ '모바일 메시지 서비스 만족도 조사에 참여해주세요. 고객님의 소중한 의견은 서비스 개선에 큰 도움이 됩니다. 참여해주신 분들께는 추첨을 통해 소정의 상품을 드립니다.',
    /* 우선 순위 */ 3,
    /* 제       목 */ '서비스 만족도 조사 참여 요청',
    /* 예약/파일 */ NULL, NULL,
    /* ALT 정보 */ NULL, NULL, NULL, NULL,
    /* RCS 정보 */ 'your_rcs_brand_key', NULL,
    /* RCS 버튼 */ '설문 참여하기', 'https://mgov.go.kr/survey'
);

CALL z_sp_send_nuri_message(
    'ALT/XMS', -- 발송 순서
    '01112345678', -- 수신 번호
    '0422505537', -- 발신 번호
    'CUBRID에서 발송하는 알림톡-문자 대체발송 테스트 (버튼 포함)', -- 메시지
    3, -- 우선 순위
    'CUBRID 테스트', -- 제목
    NULL, -- 예약 시간
    NULL, -- 첨부 파일
    'your_alt_key', -- ALT 발신 키
    'template_cubrid_01', -- ALT 템플릿 코드
    '사이트 방문', -- ALT 버튼 이름
    'https://www.cubrid.com', -- ALT 버튼 링크
    NULL, -- RCS 브랜드 키
    NULL, -- RCS 템플릿 코드
    NULL, -- RCS 버튼 문구
    NULL  -- RCS 버튼 링크
);







자동 처리 과정

p_send_order가 'RCS'이므로 RCS 단독 발송을 준비합니다.

p_rcs_button_text와 p_rcs_button_url 값이 있으므로, 버튼이 포함된 RCS_JSON을 생성합니다.

메시지 글자 수가 100자를 초과하고, p_rcs_template_code가 없으므로, RCS-LMS(RCL) 타입으로 자동 결정하고 RCS_MESSAGE_BASE_ID를 'SL000000'으로 설정합니다.