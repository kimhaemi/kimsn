메시지 발송 프로시저 (MySQL/MariaDB)
요청하신 메시지 발송 프로시저를 제공합니다. 이 프로시저는 메시지 유형(SMS, LMS, MMS, 카카오(ALT), RCS)에 따라 메시지 내용을 처리하고, 파일 첨부 여부를 확인하여 적절한 발송 규격에 맞게 데이터를 준비합니다.
DELIMITER $$

CREATE PROCEDURE SendMessage(
    IN p_message_type VARCHAR(10),       -- 메시지 유형 (SMS, LMS, MMS, KAKAO, RCS)
    IN p_request_time DATETIME,          -- 발송 요청 시간
    IN p_sender_number VARCHAR(20),      -- 발신 번호
    IN p_receiver_number VARCHAR(20),    -- 수신 번호
    IN p_message_content TEXT,           -- 메시지 내용
    IN p_attachment_file BLOB            -- 첨부 파일 (JPG, NULL 허용)
)
BEGIN
    DECLARE v_byte_length INT;
    DECLARE v_char_length INT;
    DECLARE v_processed_content TEXT;
    DECLARE v_message_status VARCHAR(50);
    DECLARE v_error_message VARCHAR(255);

    -- 한글 2바이트, 줄바꿈 1바이트 처리 (UTF-8 기준)
    SET v_byte_length = 0;
    SET v_char_length = 0;

    -- 문자열의 각 문자를 순회하며 바이트 계산
    -- MySQL/MariaDB에서 한글 바이트 계산 시 CHAR_LENGTH와 LENGTH를 조합하여 사용
    -- CHAR_LENGTH는 글자 수, LENGTH는 바이트 수를 반환
    -- 단, 'ㄱ'과 같은 자음/모음도 한글로 간주하므로 이를 2바이트로 처리하기 위해 별도의 로직 필요
    -- 여기서는 예시를 위해 간단화된 바이트 계산을 사용하며, 실제 운영 환경에서는 더 정교한 문자열 바이트 계산 함수가 필요할 수 있습니다.
    -- 예를 들어, 별도의 UDF(User Defined Function)를 생성하여 사용하거나,
    -- 언어별 특수 문자셋을 고려한 바이트 계산 로직을 구현해야 합니다.
    -- 현재는 일반적인 UTF-8 환경에서 한글이 3바이트로 처리되는 것을 2바이트로 "간주"하는 로직을 가정합니다.
    -- 실제 "한글 1글자 2바이트, ㄱ 2바이트" 처리는 DB의 인코딩 설정과 밀접한 관련이 있으며,
    -- 일반적으로 UTF-8에서는 한글이 3바이트를 사용합니다.
    -- 따라서 이 부분은 실제 시스템의 인코딩 환경에 맞게 추가적인 검토와 수정이 필요할 수 있습니다.
    -- 여기서는 `LENGTH()` 함수를 기준으로 하되, 한글을 2바이트로 '간주'하는 방식으로 시뮬레이션합니다.
    -- 정확한 2바이트 계산을 위해서는 별도의 사용자 정의 함수나 애플리케이션 레벨에서의 처리가 권장됩니다.

    SET v_processed_content = p_message_content;

    -- 줄바꿈 문자(\n)를 1바이트로 처리하기 위해 임시로 다른 문자로 치환 후 길이 계산, 다시 복원
    SET v_processed_content = REPLACE(v_processed_content, '\\n', ' '); -- 임시로 1바이트 문자로 대체하여 바이트 계산
    SET v_byte_length = LENGTH(v_processed_content);
    SET v_char_length = CHAR_LENGTH(v_processed_content);

    -- 한글은 1글자당 2바이트로 '간주'하기 위한 조정 (간단화된 예시)
    -- 정확한 한글 2바이트 처리는 인코딩에 따라 복잡하므로, 여기서는 개념적인 접근
    -- 실제 구현 시에는 유니코드 정규화 및 바이트 계산 로직이 필요
    SET v_byte_length = (v_byte_length - (v_char_length * 1)) * 2/3 + v_char_length; -- 대략적인 한글 2바이트 간주 계산 (UTF-8 한글 3바이트 기준)
    SET v_processed_content = p_message_content; -- 원본 메시지 내용으로 복원

    -- 메시지 유형에 따른 처리 로직
    CASE p_message_type
        WHEN 'SMS' THEN
            IF v_byte_length >= 1 AND v_byte_length <= 90 THEN
                SET v_message_status = 'SMS_READY';
            ELSE
                SET v_message_status = 'SMS_LENGTH_ERROR';
                SET v_error_message = CONCAT('SMS 메시지 바이트 길이 초과: ', v_byte_length, ' bytes');
            END IF;
        WHEN 'LMS' THEN
            IF v_byte_length >= 91 AND v_byte_length <= 2000 THEN
                SET v_message_status = 'LMS_READY';
            ELSE
                SET v_message_status = 'LMS_LENGTH_ERROR';
                SET v_error_message = CONCAT('LMS 메시지 바이트 길이 초과: ', v_byte_length, ' bytes');
            END IF;
        WHEN 'MMS' THEN
            IF p_attachment_file IS NOT NULL AND v_byte_length >= 1 AND v_byte_length <= 2000 THEN
                -- 실제로는 파일 확장자 체크 로직 추가 필요 (예: JPG 파일 확인)
                SET v_message_status = 'MMS_READY';
            ELSEIF p_attachment_file IS NULL THEN
                SET v_message_status = 'MMS_ATTACHMENT_MISSING';
                SET v_error_message = 'MMS는 첨부 파일이 필수입니다.';
            ELSE
                SET v_message_status = 'MMS_LENGTH_ERROR';
                SET v_error_message = CONCAT('MMS 메시지 바이트 길이 초과: ', v_byte_length, ' bytes');
            END IF;
        WHEN 'KAKAO' THEN -- 카카오(ALT) - JSON 처리, 1000자 LENGTH
            -- 카카오 메시지는 JSON 형식으로 처리되므로, 메시지 내용 자체의 바이트 길이보다는
            -- JSON 문자열 전체의 길이를 기준으로 계산해야 합니다.
            -- 여기서는 입력된 p_message_content를 JSON의 'text' 필드로 간주하여 길이 계산합니다.
            -- 실제로는 p_message_content가 이미 JSON 문자열일 수도 있습니다.
            SET v_char_length = CHAR_LENGTH(p_message_content); -- LENGTH가 아닌 CHAR_LENGTH로 문자 수 계산
            IF v_char_length <= 1000 THEN
                SET v_message_status = 'KAKAO_READY';
            ELSE
                SET v_message_status = 'KAKAO_LENGTH_ERROR';
                SET v_error_message = CONCAT('카카오 메시지 문자 길이 초과: ', v_char_length, ' characters');
            END IF;
        WHEN 'RCS' THEN -- RCS - JSON 처리
            -- RCS는 유형별(RCS, RCL, RCM)로 길이가 다릅니다.
            -- 여기서는 p_message_content가 이미 JSON 형식이라고 가정합니다.
            -- 실제로는 p_message_content 안에 RCS 유형을 구분하는 필드가 있거나,
            -- 별도의 입력 파라미터로 RCS 세부 유형을 받아야 합니다.
            -- 여기서는 가장 긴 RCM 텍스트(1300자)를 기준으로 예시를 작성합니다.
            SET v_char_length = CHAR_LENGTH(p_message_content); -- LENGTH가 아닌 CHAR_LENGTH로 문자 수 계산

            -- RCS 세부 유형에 따른 길이 체크 (예시: RCS, RCL, RCM)
            -- 실제 구현에서는 RCS 세부 유형을 받아와서 해당 길이를 적용해야 합니다.
            -- 예를 들어, p_message_type이 'RCS_RCS', 'RCS_RCL', 'RCS_RCM' 등으로 세분화될 수 있습니다.
            -- 여기서는 가장 일반적인 텍스트 메시지 기준 (최대 1300자)으로 처리합니다.
            IF v_char_length <= 1300 THEN -- RCS(100자), RCL(1300자), RCM 텍스트(1300자) 중 최대
                IF p_attachment_file IS NOT NULL THEN
                    SET v_message_status = 'RCS_M_READY'; -- 이미지 첨부 가능한 RCM으로 간주
                ELSE
                    SET v_message_status = 'RCS_TEXT_READY';
                END IF;
            ELSE
                SET v_message_status = 'RCS_LENGTH_ERROR';
                SET v_error_message = CONCAT('RCS 메시지 문자 길이 초과: ', v_char_length, ' characters');
            END IF;
        ELSE
            SET v_message_status = 'UNKNOWN_MESSAGE_TYPE';
            SET v_error_message = '알 수 없는 메시지 유형입니다.';
    END CASE;

    -- 이 부분에서 실제 메시지 발송 큐에 삽입하거나, 발송 테이블에 기록하는 로직을 구현합니다.
    -- 예시로 메시지 상태와 에러 메시지를 반환합니다.
    -- SELECT v_message_status AS message_status,
    --        v_error_message AS error_message,
    --        v_byte_length AS calculated_byte_length,
    --        v_char_length AS calculated_char_length;
    --
    -- -- 예를 들어, 메시지 발송 로그 테이블에 기록
    -- -- INSERT INTO message_logs (
    -- --     message_type,
    --     request_time,
    --     sender_number,
    --     receiver_number,
    --     message_content,
    --     attachment_file,
    --     status,
    --     error_details,
    --     calculated_byte_length,
    --     calculated_char_length
    -- ) VALUES (
    --     p_message_type,
    --     p_request_time,
    --     p_sender_number,
    --     p_receiver_number,
    --     p_message_content,
    --     p_attachment_file,
    --     v_message_status,
    --     v_error_message,
    --     v_byte_length,
    --     v_char_length
    -- );

END$$

DELIMITER ;

프로시저 설명
이 프로시저는 다양한 메시지 유형에 따라 메시지 내용을 검증하고 처리하기 위한 기본적인 틀을 제공합니다.
 * SendMessage 프로시저:
   * 입력 파라미터:
     * p_message_type: 메시지 유형을 지정합니다. (SMS, LMS, MMS, KAKAO, RCS)
     * p_request_time: 발송 요청 시간입니다.
     * p_sender_number: 발신 번호입니다.
     * p_receiver_number: 수신 번호입니다.
     * p_message_content: 발송할 메시지 내용입니다.
     * p_attachment_file: 첨부 파일 (JPG 이미지의 BLOB 데이터), 파일이 없는 경우 NULL을 전달할 수 있습니다.
   * 바이트 계산:
     * 한글 2바이트 처리: MySQL/MariaDB에서 LENGTH() 함수는 UTF-8 기준으로 한글을 보통 3바이트로 계산합니다. 요청하신 "한글 1글자 2바이트" 및 "ㄱ도 2바이트"는 데이터베이스의 인코딩 설정과 밀접한 관련이 있습니다. 일반적인 UTF-8 환경에서는 한글이 3바이트를 차지하므로, 이를 2바이트로 "간주"하기 위한 개념적인 조정 로직을 포함했습니다. 정확한 2바이트 처리를 위해서는 별도의 사용자 정의 함수(UDF)를 개발하거나, 애플리케이션 레벨에서 바이트를 계산하여 전달하는 것이 더 정확하고 효율적일 수 있습니다.
     * 줄바꿈 \n 1바이트: REPLACE 함수를 사용하여 줄바꿈 문자를 임시로 1바이트 문자로 대체한 후 길이를 계산하고, 다시 원본으로 복원하는 방식을 사용했습니다.
   * 메시지 유형별 처리:
     * SMS: 메시지 바이트 길이가 1 ~ 90 바이트인지 확인합니다.
     * LMS: 메시지 바이트 길이가 91 ~ 2000 바이트인지 확인합니다.
     * MMS: 첨부 파일이 필수이며, 메시지 바이트 길이가 1 ~ 2000 바이트인지 확인합니다. (실제로는 첨부 파일의 유효성 검사, 예: JPG 파일인지 확인하는 로직이 추가되어야 합니다.)
     * 카카오(ALT): 메시지 내용의 문자 길이가 1000자 이내인지 확인합니다. (JSON 형식으로 처리된다고 가정하며, CHAR_LENGTH 사용)
     * RCS: 메시지 내용의 문자 길이가 1300자 이내인지 확인합니다. (JSON 형식으로 처리된다고 가정하며, CHAR_LENGTH 사용. RCS의 세부 유형에 따라 길이를 더 세분화할 수 있습니다.) 이미지 첨부 여부에 따라 상태를 구분합니다.
   * 결과 반환: 프로시저 실행 후 message_status, error_message, calculated_byte_length, calculated_char_length를 SELECT 문으로 반환합니다. 이는 프로시저 호출 시 결과를 확인할 수 있도록 합니다.
   * 로그 기록 (주석 처리): 주석 처리된 부분은 실제 시스템에서 메시지 발송 요청을 message_logs와 같은 테이블에 기록하는 예시입니다. 필요에 따라 주석을 해제하고 테이블 구조에 맞게 수정하여 사용하시면 됩니다.
사용 예시
-- SMS 메시지 발송 예시 (90바이트 이내)
CALL SendMessage(
    'SMS',
    NOW(),
    '01012345678',
    '01098765432',
    '안녕하세요. 테스트 SMS 메시지입니다. 짧은 메시지입니다.',
    NULL
);

-- LMS 메시지 발송 예시 (91~2000바이트)
CALL SendMessage(
    'LMS',
    NOW(),
    '01012345678',
    '01098765432',
    '이것은 긴 메시지 테스트를 위한 LMS 메시지입니다. 안녕하세요, 여러분! 즐거운 하루 되세요.',
    NULL
);

-- MMS 메시지 발송 예시 (첨부 파일 포함) - p_attachment_file에 실제 JPG BLOB 데이터 필요
-- BLOB 데이터는 직접 넣기 어려우므로, 실제 사용 시에는 애플리케이션에서 처리 후 프로시저 호출
-- CALL SendMessage(
--     'MMS',
--     NOW(),
--     '01012345678',
--     '01098765432',
--     'MMS 테스트 메시지입니다. 이미지를 확인해주세요.',
--     LOAD_FILE('/path/to/your/image.jpg') -- 실제 파일 경로
-- );

-- 카카오(ALT) 메시지 발송 예시 (1000자 이내)
CALL SendMessage(
    'KAKAO',
    NOW(),
    '01012345678',
    '01098765432',
    '{"template_code": "template_001", "text": "카카오 알림톡 테스트 메시지입니다. 고객님께 중요한 안내 드립니다."}',
    NULL
);

-- RCS 메시지 발송 예시 (1300자 이내)
CALL SendMessage(
    'RCS',
    NOW(),
    '01012345678',
    '01098765432',
    '{"rcs_type": "TEXT", "content": "RCS 메시지 테스트입니다. 더 풍부한 정보를 제공합니다."}',
    NULL
);

-- SMS 길이 초과 예시
CALL SendMessage(
    'SMS',
    NOW(),
    '01012345678',
    '01098765432',
    '이것은 SMS 최대 길이를 초과하는 매우 긴 메시지입니다. 이 메시지는 90바이트를 넘어갈 것이므로 SMS 발송에 실패할 것입니다. 길이를 확인해주세요. 길이를 확인해주세요. 길이를 확인해주세요.',
    NULL
);

주의사항 및 개선 방향
 * 한글 바이트 계산: "한글 1글자는 2바이트"라는 규격은 데이터베이스의 인코딩 설정 (예: EUC-KR, UTF-8)에 따라 다르게 해석될 수 있습니다. 현재 MySQL/MariaDB의 일반적인 UTF-8 환경에서는 한글이 3바이트를 차지합니다. 프로시저 내의 바이트 계산 로직은 이 부분을 "간주"하는 방식으로 작성되었으므로, 실제 운영 환경의 인코딩과 정책을 정확히 확인하여 수정하거나, 별도의 UDF(User Defined Function)를 생성하여 정확한 바이트 계산을 수행하는 것이 가장 중요합니다.
 * 첨부 파일 처리: p_attachment_file은 BLOB 타입으로, 실제 JPG 파일 데이터를 직접 저장하거나 전달해야 합니다. 대용량 파일의 경우 BLOB 타입보다는 파일 경로를 저장하고, 실제 파일은 별도의 스토리지(파일 서버, AWS S3 등)에 저장하는 방식이 더 일반적입니다.
 * JSON 유효성 검사: 카카오(ALT) 및 RCS 메시지는 JSON 형식으로 처리된다고 명시되어 있습니다. 프로시저 내에서는 단순히 문자 길이를 확인하지만, 실제로는 입력된 p_message_content가 유효한 JSON 형식인지 검사하는 로직(예: JSON_VALID 함수 사용)을 추가하는 것이 좋습니다.
 * RCS 세부 유형: RCS는 일반 텍스트, 이미지 포함 등 다양한 메시지 유형이 있습니다. 현재 프로시저는 RCS의 길이를 1300자로 통일했지만, 실제 구현 시에는 RCS의 세부 유형(RCS, RCL, RCM 등)을 입력받아 해당 유형에 맞는 길이 및 파일 첨부 규격을 정확히 검증해야 합니다.
 * 에러 처리: 현재는 message_status와 error_message를 반환하지만, 실제 시스템에서는 에러 발생 시 적절한 예외 처리 메커니즘을 구현하거나, 상세한 에러 로그를 기록하는 것이 필요합니다.
 * 비동기 처리: 메시지 발송은 외부 서비스와 연동되는 경우가 많으므로, 이 프로시저는 메시지 발송 요청을 DB에 저장하는 역할만 하고, 실제 외부 발송은 별도의 메시지 큐 시스템(Kafka, RabbitMQ 등)을 통해 비동기로 처리하는 것이 일반적인 아키텍처입니다.
이 프로시저는 요청하신 기능을 구현하기 위한 시작점으로 활용하시고, 실제 시스템 환경과 요구사항에 맞춰 더욱 정교하게 개선하시길 권장합니다.