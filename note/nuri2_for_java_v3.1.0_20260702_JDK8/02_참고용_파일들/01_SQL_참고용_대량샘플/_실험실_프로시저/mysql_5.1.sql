DELIMITER //

CREATE PROCEDURE SendMessage (
    IN p_request_datetime DATETIME,
    IN p_message_type VARCHAR(10),
    IN p_sender_number VARCHAR(20),
    IN p_receiver_number VARCHAR(20),
    IN p_message_content TEXT,
    IN p_file_path VARCHAR(255),
    OUT p_error_message VARCHAR(255) -- 오류 메시지 출력 파라미터 추가
)
BEGIN
    DECLARE v_calculated_type VARCHAR(10);
    DECLARE v_content_length INT;
    DECLARE v_byte_length INT;
    DECLARE v_alt_success BOOLEAN DEFAULT FALSE;

    SET p_error_message = NULL; -- 오류 메시지 초기화

    -- 1. 메시지 타입 및 내용 검증
    IF p_file_path IS NOT NULL AND p_file_path <> '' THEN
        SET v_calculated_type = 'MMS';
    ELSEIF p_message_type = 'ALT' THEN
        SET v_calculated_type = 'ALT';
    ELSE
        -- SMS, LMS 바이트 계산 (한글 2, 줄바꿈 1)
        SET v_byte_length = LENGTH(REPLACE(p_message_content, '\n', '_')) + (LENGTH(p_message_content) - LENGTH(REPLACE(p_message_content, '가', 'A')));
        IF v_byte_length BETWEEN 1 AND 90 THEN
            SET v_calculated_type = 'SMS';
        ELSEIF v_byte_length BETWEEN 91 AND 2000 THEN
            SET v_calculated_type = 'LMS';
        ELSE
            SET p_error_message = '메시지 내용이 규격에 맞지 않습니다. (SMS: 1~90바이트, LMS: 91~2000바이트)';
        END IF;
    END IF;

    IF p_error_message IS NULL THEN
        -- 2. ALT 메시지 처리 (우선 시도)
        IF p_message_type = 'ALT' THEN
            IF LENGTH(p_message_content) <= 1000 THEN
                -- TODO: 실제 ALT 발송 API 호출 및 성공 여부 확인 로직 구현
                SET v_alt_success = TRUE; -- 임시 성공 처리
                IF v_alt_success THEN
                    SELECT 'ALT 메시지 발송 성공';
                ELSE
                    SELECT 'ALT 메시지 발송 실패, 문자 메시지로 대체 시도';
                    SET p_message_type = 'SMS'; -- 타입 변경
                    SET v_calculated_type = 'SMS';
                    -- 바이트 계산 (ALT 실패 후 SMS로 처리 시)
                    SET v_byte_length = LENGTH(REPLACE(p_message_content, '\n', '_')) + (LENGTH(p_message_content) - LENGTH(REPLACE(p_message_content, '가', 'A')));
                    IF v_byte_length BETWEEN 1 AND 90 THEN
                        -- TODO: SMS 발송 API 호출 로직 구현
                        SELECT 'SMS 메시지 발송 시도 (ALT 실패 후)';
                        -- TODO: 발송 성공/실패 로그 기록
                    ELSE
                        SET p_error_message = 'ALT 발송 실패 후 문자 메시지 길이가 규격에 맞지 않습니다. (SMS: 1~90바이트)';
                    END IF;
                END IF;
            ELSE
                SET p_error_message = 'ALT 메시지 내용이 규격에 맞지 않습니다. (최대 1000자)';
            END IF;
        ELSEIF v_calculated_type = 'SMS' THEN
            -- TODO: SMS 발송 API 호출 로직 구현
            SELECT 'SMS 메시지 발송 시도';
            -- TODO: 발송 성공/실패 로그 기록
        ELSEIF v_calculated_type = 'LMS' THEN
            -- TODO: LMS 발송 API 호출 로직 구현
            SELECT 'LMS 메시지 발송 시도';
            -- TODO: 발송 성공/실패 로그 기록
        ELSEIF v_calculated_type = 'MMS' THEN
            -- 3. MMS 메시지 처리
            -- 파일 확장자 검사
            IF p_file_path IS NOT NULL AND p_file_path <> '' AND SUBSTRING(p_file_path, LENGTH(p_file_path) - 3) NOT IN ('jpg', 'JPEG') THEN
                SET p_error_message = 'MMS 첨부 파일은 JPEG(JPG) 형식만 가능합니다.';
            END IF;

            -- 내용 길이 검사 (MMS도 2000바이트까지 허용)
            SET v_byte_length = LENGTH(REPLACE(p_message_content, '\n', '_')) + (LENGTH(p_message_content) - LENGTH(REPLACE(p_message_content, '가', 'A')));
            IF v_byte_length > 2000 THEN
                SET p_error_message = 'MMS 메시지 내용이 규격에 맞지 않습니다. (최대 2000바이트)';
            END IF;

            IF p_error_message IS NULL THEN
                -- TODO: MMS 발송 API 호출 로직 구현 (파일 경로 포함)
                SELECT 'MMS 메시지 발송 시도 (파일: ' + p_file_path + ')';
                -- TODO: 발송 성공/실패 로그 기록
            END IF;
        END IF;
    END IF;

    -- 4. 발송 요청 기록 저장 (선택 사항)
    -- TODO: 발송 요청 정보를 저장하는 테이블이 있다면 여기에 INSERT INTO 구문 추가
    INSERT INTO 00_0_nrmsg_data( MSG_KEY, MSG_STATE, INPUT_DATE, RES_DATE, PHONE, CALLBACK, MSG_TYPE_1, CONTENTS_TYPE_1, XMS_TEXT, XMS_FILE_NAME_1, SUB_ID, USER_ID )
      VALUES ( IN_MSG_KEY, 1, NOW(), trim(IN_RequestDate), trim(IN_ReciverPhone), trim(IN_CallBack), trim(OUT_MsgType1), trim(OUT_MsgType2), trim(p_message_content), trim(IN_IN_FilePathName1Name1), trim(IN_Sub_id), trim(IN_User_id)) ;


END //

DELIMITER ;


/*


*/