-- ============================================================================= --
-- [ORACLE][SEQUENCE] 생성
CREATE SEQUENCE NURI2_MSG_SEQ
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 99999999999999999
    MINVALUE 1
    NOCYCLE
    NOCACHE
    NOORDER;
-- *조회
SELECT NURI2_MSG_SEQ.NEXT_VALUE FROM DUAL;
-- ============================================================================= --
