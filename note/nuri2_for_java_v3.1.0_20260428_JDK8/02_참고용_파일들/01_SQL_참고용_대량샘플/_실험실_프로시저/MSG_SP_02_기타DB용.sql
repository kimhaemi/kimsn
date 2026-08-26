공통 가정
테이블: NURI2_NRMSG_DATA 테이블이 동일한 컬럼명으로 존재합니다.

시퀀스: MSG_KEY 생성을 위한 시퀀스 객체가 NURI2_MSG_KEY_SEQ라는 이름으로 존재합니다.

권한: 프로시저를 생성할 수 있는 권한이 있습니다.







=======================================================================================================================================
## 1. Oracle (PL/SQL)
=======================================================================================================================================
SQL
=======================================================================================================================================

CREATE OR REPLACE PROCEDURE sp_send_nuri_message (
    p_send_order         IN VARCHAR2,
    p_phone              IN VARCHAR2,
    p_callback           IN VARCHAR2,
    p_message            IN CLOB,
    p_priority           IN NUMBER,
    p_subject            IN VARCHAR2,
    p_res_date           IN VARCHAR2,
    p_file_path_1        IN VARCHAR2,
    p_alt_sender_key     IN VARCHAR2,
    p_alt_template_code  IN VARCHAR2,
    p_alt_button_name    IN VARCHAR2,
    p_alt_button_url     IN VARCHAR2,
    p_rcs_brand_key      IN VARCHAR2,
    p_rcs_template_code  IN VARCHAR2,
    p_rcs_button_text    IN VARCHAR2,
    p_rcs_button_url     IN VARCHAR2
)
IS
    v_msg_key            NUMBER;
    v_res_date           VARCHAR2(14);
    v_msg_state          NUMBER := 1;
    v_priority           NUMBER;
    v_xms_text           CLOB;
    v_xms_subject        VARCHAR2(100);
    v_alt_json           CLOB;
    v_rcs_json           CLOB;
    v_rcs_base_id        VARCHAR2(50);
    v_msg_type_1         VARCHAR2(10) := NULL;
    v_contents_type_1    VARCHAR2(10) := NULL;
    v_msg_type_2         VARCHAR2(10) := NULL;
    v_contents_type_2    VARCHAR2(10) := NULL;
    v_msg_type_3         VARCHAR2(10) := NULL;
    v_contents_type_3    VARCHAR2(10) := NULL;
    v_message_bytes      NUMBER;
    v_message_length     NUMBER;
    v_json_escaped_message CLOB;
    v_alt_button_json    VARCHAR2(1000);
    v_rcs_button_json    VARCHAR2(1000);
    v_order_str          VARCHAR2(100);
    v_pos                NUMBER;
    v_i                  NUMBER := 1;
    current_type         VARCHAR2(10);
BEGIN
    SELECT NURI2_MSG_KEY_SEQ.NEXTVAL INTO v_msg_key FROM DUAL;
    v_priority := NVL(p_priority, 3);
    v_res_date := NVL(p_res_date, TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS'));

    v_message_bytes := LENGTHB(REPLACE(p_message, CHR(13)||CHR(10), CHR(10)));
    v_message_length := LENGTH(REPLACE(p_message, CHR(13)||CHR(10), CHR(10)));
    v_json_escaped_message := REPLACE(REPLACE(REPLACE(p_message, '\', '\\'), '"', '\"'), CHR(13)||CHR(10), '\n');

    IF p_alt_button_name IS NOT NULL AND p_alt_button_url IS NOT NULL THEN
        v_alt_button_json := ',"button":[{"name":"' || p_alt_button_name || '","type":"WL","url_mobile":"' || p_alt_button_url || '","url_pc":"' || p_alt_button_url || '"}]';
    ELSE v_alt_button_json := ''; END IF;

    IF p_rcs_button_text IS NOT NULL AND p_rcs_button_url IS NOT NULL THEN
        v_rcs_button_json := ',"buttons":[{"suggestions":[{"action":{"displayText":"' || p_rcs_button_text || '","urlAction":{"openUrl":{"url":"' || p_rcs_button_url || '"}}}}]}';
    ELSE v_rcs_button_json := ''; END IF;

    v_xms_text := p_message;
    v_xms_subject := p_subject;
    v_alt_json := '{"text":"' || v_json_escaped_message || '"' || v_alt_button_json || '}';
    v_rcs_json := '{"msg":{"body":{"description":"' || v_json_escaped_message || '"}' || v_rcs_button_json || ',"copyAllowed":true,"header":"0"}}';

    v_order_str := p_send_order || '/';
    LOOP
        v_pos := INSTR(v_order_str, '/', 1, v_i);
        EXIT WHEN v_pos = 0;

        current_type := SUBSTR(v_order_str, INSTR(v_order_str, '/', 1, v_i-1) + 1, v_pos - (INSTR(v_order_str, '/', 1, v_i-1) + 1));

        IF v_i = 1 THEN
            IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_1 := 'MMS'; v_msg_type_1 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_1 := 'SMS'; v_msg_type_1 := 'SMS';
                ELSE v_contents_type_1 := 'LMS'; v_msg_type_1 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_1 := 'ALT'; v_contents_type_1 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_1 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_1 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_1 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_1 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        ELSIF v_i = 2 THEN
             IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_2 := 'MMS'; v_msg_type_2 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_2 := 'SMS'; v_msg_type_2 := 'SMS';
                ELSE v_contents_type_2 := 'LMS'; v_msg_type_2 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_2 := 'ALT'; v_contents_type_2 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_2 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_2 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_2 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_2 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        ELSIF v_i = 3 THEN
             IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_3 := 'MMS'; v_msg_type_3 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_3 := 'SMS'; v_msg_type_3 := 'SMS';
                ELSE v_contents_type_3 := 'LMS'; v_msg_type_3 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_3 := 'ALT'; v_contents_type_3 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_3 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_3 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_3 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_3 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        END IF;
        v_i := v_i + 1;
    END LOOP;

    INSERT INTO NURI2_NRMSG_DATA (
          MSG_KEY, MSG_STATE, MSG_PRIORITY, INPUT_DATE, RES_DATE, PHONE, CALLBACK,
          XMS_SUBJECT, XMS_TEXT, XMS_FILE_NAME_1,
          MSG_TYPE_1, CONTENTS_TYPE_1, ALT_SENDER_KEY, ALT_TEMPLATE_CODE, ALT_JSON,
          MSG_TYPE_2, CONTENTS_TYPE_2, MSG_TYPE_3, CONTENTS_TYPE_3,
          RCS_BRAND_KEY, RCS_MESSAGE_BASE_ID, RCS_JSON
    ) VALUES (
          v_msg_key, v_msg_state, v_priority, TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS'), v_res_date, p_phone, p_callback,
          v_xms_subject, v_xms_text, p_file_path_1,
          v_msg_type_1, v_contents_type_1, p_alt_sender_key, p_alt_template_code, v_alt_json,
          v_msg_type_2, v_contents_type_2, v_msg_type_3, v_contents_type_3,
          p_rcs_brand_key, v_rcs_base_id, v_rcs_json
    );
    COMMIT;
END;
/
실행 예제

SQL

BEGIN
    sp_send_nuri_message(
        p_send_order => 'ALT/XMS',
        p_phone => '01112345678',
        p_callback => '0422505537',
        p_message => '오라클에서 발송하는 알림톡-문자 대체발송 테스트',
        p_priority => 3,
        p_subject => '테스트',
        p_res_date => NULL,
        p_file_path_1 => NULL,
        p_alt_sender_key => 'your_alt_key',
        p_alt_template_code => 'template01',
        p_alt_button_name => '바로가기',
        p_alt_button_url => 'https://mgov.go.kr',
        p_rcs_brand_key => NULL,
        p_rcs_template_code => NULL,
        p_rcs_button_text => NULL,
        p_rcs_button_url => NULL
    );
END;

BEGIN
    sp_send_nuri_message(
        p_send_order => 'XMS',
        p_phone => '01112345678',
        p_callback => '0422505537',
        p_message => '오라클에서 발송하는 알림톡-문자 대체발송 테스트',
        p_priority => 3,
        p_subject => '테스트',
        p_res_date => NULL,
        p_file_path_1 => NULL,
        p_alt_sender_key => 'your_alt_key',
        p_alt_template_code => 'template01',
        p_alt_button_name => '바로가기',
        p_alt_button_url => 'https://mgov.go.kr',
        p_rcs_brand_key => NULL,
        p_rcs_template_code => NULL,
        p_rcs_button_text => NULL,
        p_rcs_button_url => NULL
    );
END;



/






=======================================================================================================================================
## 2. MS-SQL (T-SQL)
=======================================================================================================================================
SQL
=======================================================================================================================================

CREATE OR ALTER PROCEDURE sp_send_nuri_message
    @p_send_order        NVARCHAR(50),
    @p_phone             NVARCHAR(20),
    @p_callback          NVARCHAR(20),
    @p_message           NVARCHAR(MAX),
    @p_priority          INT = 3,
    @p_subject           NVARCHAR(100) = NULL,
    @p_res_date          NVARCHAR(14) = NULL,
    @p_file_path_1       NVARCHAR(255) = NULL,
    @p_alt_sender_key    NVARCHAR(100) = NULL,
    @p_alt_template_code NVARCHAR(50) = NULL,
    @p_alt_button_name   NVARCHAR(50) = NULL,
    @p_alt_button_url    NVARCHAR(255) = NULL,
    @p_rcs_brand_key     NVARCHAR(100) = NULL,
    @p_rcs_template_code NVARCHAR(50) = NULL,
    @p_rcs_button_text   NVARCHAR(50) = NULL,
    @p_rcs_button_url    NVARCHAR(255) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @v_msg_key           BIGINT = NEXT VALUE FOR NURI2_MSG_KEY_SEQ;
    DECLARE @v_res_date          NVARCHAR(14) = ISNULL(@p_res_date, FORMAT(GETDATE(), 'yyyyMMddHHmmss'));
    DECLARE @v_msg_state         INT = 1;
    DECLARE @v_priority          INT = ISNULL(@p_priority, 3);
    DECLARE @v_xms_text          NVARCHAR(MAX) = @p_message;
    DECLARE @v_xms_subject       NVARCHAR(100) = @p_subject;
    DECLARE @v_alt_json          NVARCHAR(MAX);
    DECLARE @v_rcs_json          NVARCHAR(MAX);
    DECLARE @v_rcs_base_id       NVARCHAR(50);
    DECLARE @v_msg_type_1        NVARCHAR(10), @v_contents_type_1 NVARCHAR(10);
    DECLARE @v_msg_type_2        NVARCHAR(10), @v_contents_type_2 NVARCHAR(10);
    DECLARE @v_msg_type_3        NVARCHAR(10), @v_contents_type_3 NVARCHAR(10);
    DECLARE @v_message_bytes     INT = DATALENGTH(REPLACE(@p_message, CHAR(13)+CHAR(10), CHAR(10)));
    DECLARE @v_message_length    INT = LEN(REPLACE(@p_message, CHAR(13)+CHAR(10), CHAR(10)));
    DECLARE @v_json_escaped_message NVARCHAR(MAX);
    DECLARE @v_alt_button_json   NVARCHAR(1000) = '';
    DECLARE @v_rcs_button_json   NVARCHAR(1000) = '';
    DECLARE @order_table TABLE (id INT IDENTITY(1,1), type_val NVARCHAR(10));

    SET @v_json_escaped_message = REPLACE(REPLACE(REPLACE(@p_message, '\', '\\'), '"', '\"'), CHAR(13)+CHAR(10), '\n');

    IF @p_alt_button_name IS NOT NULL AND @p_alt_button_url IS NOT NULL
        SET @v_alt_button_json = CONCAT(',"button":[{"name":"', @p_alt_button_name, '","type":"WL","url_mobile":"', @p_alt_button_url, '","url_pc":"', @p_alt_button_url, '"}]');

    IF @p_rcs_button_text IS NOT NULL AND @p_rcs_button_url IS NOT NULL
        SET @v_rcs_button_json = CONCAT(',"buttons":[{"suggestions":[{"action":{"displayText":"', @p_rcs_button_text, '","urlAction":{"openUrl":{"url":"', @p_rcs_button_url, '"}}}}]}');

    SET @v_alt_json = CONCAT('{"text":"', @v_json_escaped_message, '"', @v_alt_button_json, '}');
    SET @v_rcs_json = CONCAT('{"msg":{"body":{"description":"', @v_json_escaped_message, '"}', @v_rcs_button_json, ',"copyAllowed":true,"header":"0"}}');

    INSERT INTO @order_table (type_val) SELECT value FROM STRING_SPLIT(@p_send_order, '/');

    DECLARE @i INT = 1;
    DECLARE @current_type NVARCHAR(10);
    WHILE @i <= 3
    BEGIN
        SELECT @current_type = type_val FROM @order_table WHERE id = @i;
        IF @@ROWCOUNT = 0 BREAK;

        IF @i = 1
        BEGIN
            IF @current_type = 'XMS' BEGIN
                IF @p_file_path_1 IS NOT NULL BEGIN SET @v_contents_type_1 = 'MMS'; SET @v_msg_type_1 = 'MMS'; END
                ELSE IF @v_message_bytes <= 90 BEGIN SET @v_contents_type_1 = 'SMS'; SET @v_msg_type_1 = 'SMS'; END
                ELSE BEGIN SET @v_contents_type_1 = 'LMS'; SET @v_msg_type_1 = 'MMS'; END
            END
            ELSE IF @current_type = 'ALT' BEGIN SET @v_msg_type_1 = 'ALT'; SET @v_contents_type_1 = 'ALT'; END
            ELSE IF @current_type = 'RCS' BEGIN SET @v_msg_type_1 = 'RCS';
                IF @p_rcs_template_code IS NOT NULL BEGIN SET @v_contents_type_1 = 'RCT'; SET @v_rcs_base_id = @p_rcs_template_code; END
                ELSE IF @v_message_length <= 100 BEGIN SET @v_contents_type_1 = 'RCS'; SET @v_rcs_base_id = 'SS000000'; END
                ELSE BEGIN SET @v_contents_type_1 = 'RCL'; SET @v_rcs_base_id = 'SL000000'; END
            END
        END
        ELSE IF @i = 2
        BEGIN
             IF @current_type = 'XMS' BEGIN
                IF @p_file_path_1 IS NOT NULL BEGIN SET @v_contents_type_2 = 'MMS'; SET @v_msg_type_2 = 'MMS'; END
                ELSE IF @v_message_bytes <= 90 BEGIN SET @v_contents_type_2 = 'SMS'; SET @v_msg_type_2 = 'SMS'; END
                ELSE BEGIN SET @v_contents_type_2 = 'LMS'; SET @v_msg_type_2 = 'MMS'; END
            END
            ELSE IF @current_type = 'ALT' BEGIN SET @v_msg_type_2 = 'ALT'; SET @v_contents_type_2 = 'ALT'; END
            ELSE IF @current_type = 'RCS' BEGIN SET @v_msg_type_2 = 'RCS';
                IF @p_rcs_template_code IS NOT NULL BEGIN SET @v_contents_type_2 = 'RCT'; SET @v_rcs_base_id = @p_rcs_template_code; END
                ELSE IF @v_message_length <= 100 BEGIN SET @v_contents_type_2 = 'RCS'; SET @v_rcs_base_id = 'SS000000'; END
                ELSE BEGIN SET @v_contents_type_2 = 'RCL'; SET @v_rcs_base_id = 'SL000000'; END
            END
        END
        ELSE IF @i = 3
        BEGIN
            IF @current_type = 'XMS' BEGIN
                IF @p_file_path_1 IS NOT NULL BEGIN SET @v_contents_type_3 = 'MMS'; SET @v_msg_type_3 = 'MMS'; END
                ELSE IF @v_message_bytes <= 90 BEGIN SET @v_contents_type_3 = 'SMS'; SET @v_msg_type_3 = 'SMS'; END
                ELSE BEGIN SET @v_contents_type_3 = 'LMS'; SET @v_msg_type_3 = 'MMS'; END
            END
            ELSE IF @current_type = 'ALT' BEGIN SET @v_msg_type_3 = 'ALT'; SET @v_contents_type_3 = 'ALT'; END
            ELSE IF @current_type = 'RCS' BEGIN SET @v_msg_type_3 = 'RCS';
                IF @p_rcs_template_code IS NOT NULL BEGIN SET @v_contents_type_3 = 'RCT'; SET @v_rcs_base_id = @p_rcs_template_code; END
                ELSE IF @v_message_length <= 100 BEGIN SET @v_contents_type_3 = 'RCS'; SET @v_rcs_base_id = 'SS000000'; END
                ELSE BEGIN SET @v_contents_type_3 = 'RCL'; SET @v_rcs_base_id = 'SL000000'; END
            END
        END

        SET @i = @i + 1;
    END

    INSERT INTO NURI2_NRMSG_DATA (
        MSG_KEY, MSG_STATE, MSG_PRIORITY, INPUT_DATE, RES_DATE, PHONE, CALLBACK,
        XMS_SUBJECT, XMS_TEXT, XMS_FILE_NAME_1,
        MSG_TYPE_1, CONTENTS_TYPE_1, ALT_SENDER_KEY, ALT_TEMPLATE_CODE, ALT_JSON,
        MSG_TYPE_2, CONTENTS_TYPE_2, MSG_TYPE_3, CONTENTS_TYPE_3,
        RCS_BRAND_KEY, RCS_MESSAGE_BASE_ID, RCS_JSON
    ) VALUES (
        @v_msg_key, @v_msg_state, @v_priority, FORMAT(GETDATE(), 'yyyyMMddHHmmss'), @v_res_date, @p_phone, @p_callback,
        @v_xms_subject, @v_xms_text, @p_file_path_1,
        @v_msg_type_1, @v_contents_type_1, @p_alt_sender_key, @p_alt_template_code, @v_alt_json,
        @v_msg_type_2, @v_contents_type_2, @v_msg_type_3, @v_contents_type_3,
        @p_rcs_brand_key, @v_rcs_base_id, @v_rcs_json
    );
END;
GO
실행 예제

SQL

EXEC sp_send_nuri_message
    @p_send_order = 'RCS/XMS',
    @p_phone = '01122223333',
    @p_callback = '0422505537',
    @p_message = 'MS-SQL에서 발송하는 RCS 테스트 메시지';
GO







=======================================================================================================================================
## 3. PostgreSQL (PL/pgSQL)
SQL
=======================================================================================================================================

CREATE OR REPLACE FUNCTION sp_send_nuri_message(
    p_send_order VARCHAR,
    p_phone VARCHAR,
    p_callback VARCHAR,
    p_message TEXT,
    p_priority INT,
    p_subject VARCHAR,
    p_res_date VARCHAR,
    p_file_path_1 VARCHAR,
    p_alt_sender_key VARCHAR,
    p_alt_template_code VARCHAR,
    p_alt_button_name VARCHAR,
    p_alt_button_url VARCHAR,
    p_rcs_brand_key VARCHAR,
    p_rcs_template_code VARCHAR,
    p_rcs_button_text VARCHAR,
    p_rcs_button_url VARCHAR
) RETURNS void AS $$
DECLARE
    v_msg_key           BIGINT := nextval('nuri2_msg_key_seq');
    v_res_date          VARCHAR(14);
    v_msg_state         INT := 1;
    v_priority          INT;
    v_xms_text          TEXT := p_message;
    v_xms_subject       VARCHAR(100) := p_subject;
    v_alt_json          TEXT;
    v_rcs_json          TEXT;
    v_rcs_base_id       VARCHAR(50);
    v_msg_type_1        VARCHAR(10); v_contents_type_1 VARCHAR(10);
    v_msg_type_2        VARCHAR(10); v_contents_type_2 VARCHAR(10);
    v_msg_type_3        VARCHAR(10); v_contents_type_3 VARCHAR(10);
    v_message_bytes     INT;
    v_message_length    INT;
    v_json_escaped_message TEXT;
    v_alt_button_json   TEXT := '';
    v_rcs_button_json   TEXT := '';
    i                   INT := 1;
    current_type        VARCHAR(10);
BEGIN
    v_priority := COALESCE(p_priority, 3);
    v_res_date := COALESCE(p_res_date, TO_CHAR(NOW(), 'YYYYMMDDHH24MISS'));

    v_message_bytes := octet_length(REPLACE(p_message, E'\r\n', E'\n'));
    v_message_length := char_length(REPLACE(p_message, E'\r\n', E'\n'));
    v_json_escaped_message := REPLACE(REPLACE(REPLACE(p_message, '\', '\\'), '"', '\"'), E'\r\n', '\n');

    IF p_alt_button_name IS NOT NULL AND p_alt_button_url IS NOT NULL THEN
        v_alt_button_json := ',"button":[{"name":"' || p_alt_button_name || '","type":"WL","url_mobile":"' || p_alt_button_url || '","url_pc":"' || p_alt_button_url || '"}]';
    END IF;
    IF p_rcs_button_text IS NOT NULL AND p_rcs_button_url IS NOT NULL THEN
        v_rcs_button_json := ',"buttons":[{"suggestions":[{"action":{"displayText":"' || p_rcs_button_text || '","urlAction":{"openUrl":{"url":"' || p_rcs_button_url || '"}}}}]}';
    END IF;

    v_alt_json := '{"text":"' || v_json_escaped_message || '"' || v_alt_button_json || '}';
    v_rcs_json := '{"msg":{"body":{"description":"' || v_json_escaped_message || '"}' || v_rcs_button_json || ',"copyAllowed":true,"header":"0"}}';

    WHILE i <= 3 LOOP
        current_type := split_part(p_send_order, '/', i);
        IF current_type = '' THEN EXIT; END IF;

        IF i = 1 THEN
            IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_1 := 'MMS'; v_msg_type_1 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_1 := 'SMS'; v_msg_type_1 := 'SMS';
                ELSE v_contents_type_1 := 'LMS'; v_msg_type_1 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_1 := 'ALT'; v_contents_type_1 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_1 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_1 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_1 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_1 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        ELSIF i = 2 THEN
            IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_2 := 'MMS'; v_msg_type_2 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_2 := 'SMS'; v_msg_type_2 := 'SMS';
                ELSE v_contents_type_2 := 'LMS'; v_msg_type_2 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_2 := 'ALT'; v_contents_type_2 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_2 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_2 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_2 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_2 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        ELSIF i = 3 THEN
             IF current_type = 'XMS' THEN
                IF p_file_path_1 IS NOT NULL THEN v_contents_type_3 := 'MMS'; v_msg_type_3 := 'MMS';
                ELSIF v_message_bytes <= 90 THEN v_contents_type_3 := 'SMS'; v_msg_type_3 := 'SMS';
                ELSE v_contents_type_3 := 'LMS'; v_msg_type_3 := 'MMS'; END IF;
            ELSIF current_type = 'ALT' THEN v_msg_type_3 := 'ALT'; v_contents_type_3 := 'ALT';
            ELSIF current_type = 'RCS' THEN v_msg_type_3 := 'RCS';
                IF p_rcs_template_code IS NOT NULL THEN v_contents_type_3 := 'RCT'; v_rcs_base_id := p_rcs_template_code;
                ELSIF v_message_length <= 100 THEN v_contents_type_3 := 'RCS'; v_rcs_base_id := 'SS000000';
                ELSE v_contents_type_3 := 'RCL'; v_rcs_base_id := 'SL000000'; END IF;
            END IF;
        END IF;
        i := i + 1;
    END LOOP;

    INSERT INTO nuri2_nrmsg_data (
        MSG_KEY, MSG_STATE, MSG_PRIORITY, INPUT_DATE, RES_DATE, PHONE, CALLBACK,
        XMS_SUBJECT, XMS_TEXT, XMS_FILE_NAME_1,
        MSG_TYPE_1, CONTENTS_TYPE_1, ALT_SENDER_KEY, ALT_TEMPLATE_CODE, ALT_JSON,
        MSG_TYPE_2, CONTENTS_TYPE_2, MSG_TYPE_3, CONTENTS_TYPE_3,
        RCS_BRAND_KEY, RCS_MESSAGE_BASE_ID, RCS_JSON
    ) VALUES (
        v_msg_key, v_msg_state, v_priority, TO_CHAR(NOW(), 'YYYYMMDDHH24MISS'), v_res_date, p_phone, p_callback,
        v_xms_subject, v_xms_text, p_file_path_1,
        v_msg_type_1, v_contents_type_1, p_alt_sender_key, p_alt_template_code, v_alt_json,
        v_msg_type_2, v_contents_type_2, v_msg_type_3, v_contents_type_3,
        p_rcs_brand_key, v_rcs_base_id, v_rcs_json
    );
END;
$$ LANGUAGE plpgsql;
실행 예제

SQL

SELECT sp_send_nuri_message(
    'ALT/RCS/XMS',
    '01133334444',
    '0422505537',
    'PostgreSQL에서 발송하는 3단 대체발송 테스트',
    3, NULL, NULL, NULL,
    'your_alt_key', 'template01', NULL, NULL,
    'your_rcs_key', NULL, NULL, NULL
);








=======================================================================================================================================
## 4. Tibero (PL/SQL), 5. CUBRID, 6. IBM DB2 (SQL PL)
Tibero, CUBRID, IBM DB2는 각각 Oracle, MySQL, 독자적인 문법을 따릅니다. 위에서 보여드린 Oracle, MySQL(최초 v6 버전), 그리고 DB2용 변환 로직을 생략 없이 전체 코드로 적용하면 각 데이터베이스에 맞는 완전한 프로시저가 완성됩니다.



=======================================================================================================================================
## CUBRID (MySQL 호환)
-- CUBRID는 MySQL과 문법이 매우 유사하여, SUBSTRING_INDEX와 같은 편리한 함수를 포함한 v6의 모든 기능(자동 유형 감지, 버튼 생성 등)을 완벽하게 지원합니다.
SQL
=======================================================================================================================================

-- 기존 프로시저가 있다면 삭제
DROP PROCEDURE IF EXISTS sp_send_nuri_message;

-- CUBRID는 MySQL과 동일한 프로시저 생성 문법을 사용합니다.
CREATE PROCEDURE sp_send_nuri_message(
    IN p_send_order VARCHAR(50),
    IN p_phone VARCHAR(20),
    IN p_callback VARCHAR(20),
    IN p_message STRING,
    IN p_priority INTEGER,
    IN p_subject VARCHAR(100),
    IN p_res_date VARCHAR(14),
    IN p_file_path_1 VARCHAR(255),
    IN p_alt_sender_key VARCHAR(100),
    IN p_alt_template_code VARCHAR(50),
    IN p_alt_button_name VARCHAR(50),
    IN p_alt_button_url VARCHAR(255),
    IN p_rcs_brand_key VARCHAR(100),
    IN p_rcs_template_code VARCHAR(50),
    IN p_rcs_button_text VARCHAR(50),
    IN p_rcs_button_url VARCHAR(255)
)
AS
BEGIN
    -- 변수 선언
    DECLARE v_msg_key BIGINT;
    DECLARE v_res_date VARCHAR(14);
    DECLARE v_msg_state INTEGER := 1;
    DECLARE v_priority INTEGER;
    DECLARE v_xms_text STRING;
    DECLARE v_xms_subject VARCHAR(100);
    DECLARE v_alt_json STRING;
    DECLARE v_rcs_json STRING;
    DECLARE v_rcs_base_id VARCHAR(50);
    DECLARE v_msg_type_1 VARCHAR(10) := NULL;
    DECLARE v_contents_type_1 VARCHAR(10) := NULL;
    DECLARE v_msg_type_2 VARCHAR(10) := NULL;
    DECLARE v_contents_type_2 VARCHAR(10) := NULL;
    DECLARE v_msg_type_3 VARCHAR(10) := NULL;
    DECLARE v_contents_type_3 VARCHAR(10) := NULL;
    DECLARE v_message_bytes INTEGER;
    DECLARE v_message_length INTEGER;
    DECLARE v_json_escaped_message STRING;
    DECLARE v_alt_button_json VARCHAR(1000);
    DECLARE v_rcs_button_json VARCHAR(1000);
    DECLARE i INTEGER := 1;
    DECLARE current_type VARCHAR(10);

    -- msg_nextval() 함수가 스키마에 정의되어 있다고 가정합니다.
    SET v_msg_key = msg_nextval();

    -- 기본값 설정
    IF p_priority IS NULL OR (p_priority <> 7 AND p_priority <> 3) THEN SET v_priority = 3; ELSE SET v_priority = p_priority; END IF;
    IF p_res_date IS NULL OR p_res_date = '' THEN SET v_res_date = DATE_FORMAT(NOW(), '%Y%m%d%H%i%s'); ELSE SET v_res_date = p_res_date; END IF;

    -- 바이트/문자 길이 계산
    SET v_message_bytes = OCTET_LENGTH(REPLACE(p_message, CHR(13)||CHR(10), CHR(10)));
    SET v_message_length = CHAR_LENGTH(REPLACE(p_message, CHR(13)||CHR(10), CHR(10)));
    SET v_json_escaped_message = REPLACE(REPLACE(REPLACE(p_message, '\', '\\'), '"', '\"'), CHR(13)||CHR(10), '\n');

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

    -- p_send_order를 순회하며 발송 유형 설정
    WHILE i <= 3 DO
        SET current_type = SUBSTRING_INDEX(SUBSTRING_INDEX(p_send_order, '/', i), '/', -1);
        IF i > 1 AND current_type = SUBSTRING_INDEX(SUBSTRING_INDEX(p_send_order, '/', i-1), '/', -1) THEN
             SET current_type = '';
        END IF;

        IF current_type = '' THEN
            SET i = 4; -- 루프 종료
        ELSE
            IF i = 1 THEN
                IF current_type = 'XMS' THEN
                    IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_1 = 'MMS'; SET v_msg_type_1 = 'MMS';
                    ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_1 = 'SMS'; SET v_msg_type_1 = 'SMS';
                    ELSE SET v_contents_type_1 = 'LMS'; SET v_msg_type_1 = 'MMS'; END IF;
                ELSEIF current_type = 'ALT' THEN SET v_msg_type_1 = 'ALT'; SET v_contents_type_1 = 'ALT';
                ELSEIF current_type = 'RCS' THEN SET v_msg_type_1 = 'RCS';
                    IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_1 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                    ELSEIF v_message_length <= 100 THEN SET v_contents_type_1 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                    ELSE SET v_contents_type_1 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END IF;
            ELSEIF i = 2 THEN
                IF current_type = 'XMS' THEN
                    IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_2 = 'MMS'; SET v_msg_type_2 = 'MMS';
                    ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_2 = 'SMS'; SET v_msg_type_2 = 'SMS';
                    ELSE SET v_contents_type_2 = 'LMS'; SET v_msg_type_2 = 'MMS'; END IF;
                ELSEIF current_type = 'ALT' THEN SET v_msg_type_2 = 'ALT'; SET v_contents_type_2 = 'ALT';
                ELSEIF current_type = 'RCS' THEN SET v_msg_type_2 = 'RCS';
                    IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_2 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                    ELSEIF v_message_length <= 100 THEN SET v_contents_type_2 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                    ELSE SET v_contents_type_2 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END IF;
            ELSEIF i = 3 THEN
                IF current_type = 'XMS' THEN
                    IF p_file_path_1 IS NOT NULL THEN SET v_contents_type_3 = 'MMS'; SET v_msg_type_3 = 'MMS';
                    ELSEIF v_message_bytes <= 90 THEN SET v_contents_type_3 = 'SMS'; SET v_msg_type_3 = 'SMS';
                    ELSE SET v_contents_type_3 = 'LMS'; SET v_msg_type_3 = 'MMS'; END IF;
                ELSEIF current_type = 'ALT' THEN SET v_msg_type_3 = 'ALT'; SET v_contents_type_3 = 'ALT';
                ELSEIF current_type = 'RCS' THEN SET v_msg_type_3 = 'RCS';
                    IF p_rcs_template_code IS NOT NULL THEN SET v_contents_type_3 = 'RCT'; SET v_rcs_base_id = p_rcs_template_code;
                    ELSEIF v_message_length <= 100 THEN SET v_contents_type_3 = 'RCS'; SET v_rcs_base_id = 'SS000000';
                    ELSE SET v_contents_type_3 = 'RCL'; SET v_rcs_base_id = 'SL000000'; END IF;
                END IF;
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
END;
실행 예제 (CUBRID)
CUBRID에서 프로시저를 호출하는 방식은 MySQL과 동일합니다.

SQL

CALL sp_send_nuri_message(
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