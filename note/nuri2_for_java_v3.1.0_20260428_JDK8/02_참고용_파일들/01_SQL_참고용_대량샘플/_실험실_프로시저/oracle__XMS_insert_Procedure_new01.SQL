CREATE OR REPLACE PROCEDURE CheckAndInsertMessage(
	senderPhone IN VARCHAR2, -- 발신자 번호
	receiverPhone IN VARCHAR2, -- 수신자 번호
	msgContent IN CLOB, -- 메시지 내용
	filePath IN VARCHAR2 -- 첨부 파일 경로
)
IS
	totalBytes NUMBER := 0; -- 총 바이트 수
	charLength NUMBER; -- 메시지의 총 길이
	i NUMBER := 1; -- 반복문 인덱스
	currentChar VARCHAR2(1); -- 현재 문자
	msgType VARCHAR2(10); -- 메시지 유형
BEGIN
	-- 메시지 내용을 줄바꿈과 기타 특수 문자를 이스케이프 처리
	msgContent := REPLACE(msgContent, CHR(13) || CHR(10), '\\n'); -- 윈도우 줄바꿈 CRLF
	msgContent := REPLACE(msgContent, CHR(13), '\\n'); -- 맥 줄바꿈 CR
	msgContent := REPLACE(msgContent, CHR(10), '\\n'); -- 유닉스 줄바꿈 LF
	msgContent := REPLACE(msgContent, CHR(9), '\\t'); -- 탭 문자
	msgContent := REPLACE(msgContent, '\', '\\\\'); -- 백슬래시
	msgContent := REPLACE(msgContent, '"', '\\"'); -- 쌍따옴표
	msgContent := REPLACE(msgContent, '''', '\\'''); -- 홑따옴표

	-- 메시지의 총 길이
	charLength := LENGTH(msgContent);
	-- 메시지 내용을 한 글자씩 바이트 계산
	WHILE i <= charLength LOOP
		currentChar := SUBSTR(msgContent, i, 1);
		-- 특수 문자 이스케이프 처리 체크
		IF currentChar = '\' THEN
			-- 이스케이프 문자 후의 실제 문자 스킵
			totalBytes := totalBytes + 1;
			i := i + 1; -- 이스케이프 처리된 문자 스킵
		ELSIF LENGTHB(currentChar) = 1 THEN
			totalBytes := totalBytes + 1; -- 영문, 숫자 등은 1바이트
		ELSE
			totalBytes := totalBytes + 2; -- 한글은 2바이트
		END IF;
		i := i + 1;
	END LOOP;

	-- 메시지 유형 결정
	IF filePath IS NOT NULL AND LENGTH(filePath) > 0 THEN
		msgType := 'MMS';
	ELSIF totalBytes > 90 THEN
		msgType := 'LMS';
	ELSE
		msgType := 'SMS';
	END IF;

	-- 메시지 정보를 messages 테이블에 삽입
	INSERT INTO messages (senderPhone, receiverPhone, messageType, messageContent, byteCount, filePath)
	VALUES (senderPhone, receiverPhone, msgType, msgContent, totalBytes, filePath);

	-- 결과 출력
	DBMS_OUTPUT.PUT_LINE('Sender: ' || senderPhone || ', Receiver: ' || receiverPhone || ', Type: ' || msgType || ', Byte Count: ' || totalBytes);
END;
/

/*

BEGIN
	CheckAndInsertMessage('01012345678', '01087654321', '안녕하세요\n홍길동님 반갑습니다\n즐거운 하루 보내세요', NULL);
	CheckAndInsertMessage('01012345678', '01087654321', 'Hello \\t World "Message"', '/path/to/image.jpg');
END;
/
*/