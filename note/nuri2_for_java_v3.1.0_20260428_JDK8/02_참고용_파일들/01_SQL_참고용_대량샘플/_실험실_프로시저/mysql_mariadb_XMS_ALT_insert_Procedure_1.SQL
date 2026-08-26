-- ** MySql, MariaDB 용 Xms(SMS/LMS/MMS) 바이트 계산 분기 입력
-- 발신번호 수신번호 메시지내용, 입력요청
DROP PROCEDURE AltXmsInsert

DELIMITER //

CREATE PROCEDURE AltXmsInsert(
        IN IN_MsgType      VARCHAR(3),   -- ALT, XMS
        IN IN_TemplateCode VARCHAR(100),  -- 템플릿코드
        IN IN_RequestDate  DATETIME,      -- 발송요청 시간
        IN IN_CallBack     VARCHAR(30),   -- 발신자 번호
        IN IN_ReciverPhone VARCHAR(30),   -- 수신자 번호
        IN IN_Subject      VARCHAR(255),  -- 제목
        IN IN_Message      TEXT,          -- 메시지내용(본문내용을 기준으로 함)
        IN IN_FilePathName1 VARCHAR(255), -- 첨부파일 패스
        IN IN_Sub_id VARCHAR(100),        -- 과금계정
        IN IN_User_id VARCHAR(100)        -- 사용자키
)
BEGIN
        DECLARE totalBytes INT DEFAULT 0; -- 총 바이트 수
        DECLARE charLength INT; -- 메시지의 총 길이
        DECLARE i INT DEFAULT 1; -- 반복문 인덱스
        DECLARE currentChar CHAR(1); -- 현재 문자
        DECLARE msgType ENUM('SMS', 'LMS', 'MMS','ALT'); -- 메시지 유형

        -- 메시지 내용을 줄바꿈과 기타 특수 문자를 이스케이프 처리

--        SET IN_Message = REPLACE(IN_Message, '\r\n', '\\n');
--        SET IN_Message = REPLACE(IN_Message, '\r', '\\n');
--        SET IN_Message = REPLACE(IN_Message, '\n', '\\n');
--        SET IN_Message = REPLACE(IN_Message, '\t', '\\t');
--        SET IN_Message = REPLACE(IN_Message, '\\', '\\\\');
--        SET IN_Message = REPLACE(IN_Message, '"', '\\"');
--        SET IN_Message = REPLACE(IN_Message, '''', '\\'');

	SET IN_Message = REPLACE(IN_Message, '\r\n', '\n');


        -- '메시지의 총 길이
        SET charLength = CHAR_LENGTH(IN_Message);

        -- 메시지 내용을 한 글자씩 바이트 계산
        WHILE i <= charLength DO
                SET currentChar = SUBSTRING(IN_Message, i, 1);
                -- 특수 문자 이스케이프 처리 체크
                IF currentChar = '\\' THEN
                SET totalBytes = totalBytes + 1;
                SET i = i + 1; -- 이스케이프 처리 후 문자 스킵
                ELSEIF LENGTH(currentChar) = CHAR_LENGTH(currentChar) THEN
                SET totalBytes = totalBytes + 1; -- 영문, 숫자 등은 1바이트
                ELSE
                SET totalBytes = totalBytes + 2; -- 한글은 2바이트
                END IF;
                SET i = i + 1;
        END WHILE;

        -- 메시지 유형 결정
        IF IN_FilePathName1 IS NOT NULL AND LENGTH(IN_FilePathName1) < 0 THEN
                SET msgType = 'MMS';
        ELSEIF totalBytes > 90 THEN
                SET msgType = 'LMS';
        ELSE
                SET msgType = 'SMS';
        END IF;



	IF IN_MsgType = 'ALT' THEN

		-- 메시지 입력
		INSERT INTO 00_0_nrmsg_data( MSG_KEY, MSG_STATE, INPUT_DATE, RES_DATE, PHONE, CALLBACK, MSG_TYPE_1, CONTENTS_TYPE_1, XMS_SUBJECT, XMS_TEXT, XMS_FILE_NAME_1, SUB_ID, USER_ID )
		VALUES ( IN_MSG_KEY, 1, NOW(), trim(IN_RequestDate), trim(IN_ReciverPhone), trim(IN_CallBack), trim(OUT_MsgType1), trim(OUT_MsgType2), trim(IN_Subject), trim(IN_Message), trim(IN_IN_FilePathName1Name1), trim(IN_Sub_id), trim(IN_User_id)) ;
	ELSE
		 -- 메시지 정보를 messages 테이블에 삽입
		INSERT INTO messages (IN_CallBack, IN_ReciverPhone, messageType, messageContent, byteCount, IN_FilePathName1)
		VALUES (IN_CallBack, IN_ReciverPhone, msgType, IN_Message, totalBytes, IN_FilePathName1);

	END IF;
        -- 결과 출력
        -- SELECT IN_CallBack AS Sender, IN_ReciverPhone AS Receiver, msgType AS MessageType, totalBytes AS ByteCount;
END //

DELIMITER ;


/*

CALL AltXmsInsert('01012345678', '01087654321', '안녕하세요\n홍길동님 반갑습니다\n즐거운 하루 보내세요', NULL);
CALL AltXmsInsert('01012345678', '01087654321', 'Hello \\t World \&quot;Message\&quot;', '/path/to/image.jpg');

*/


