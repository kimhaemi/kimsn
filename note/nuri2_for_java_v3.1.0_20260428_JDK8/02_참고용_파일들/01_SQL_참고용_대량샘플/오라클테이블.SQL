/*------------------------------------------------------------------------------
-- 객체명 : NRMSG_DATA
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP TABLE NRMSG_DATA CASCADE CONSTRAINTS;

CREATE TABLE NRMSG_DATA (
  MSG_KEY                NUMBER(11)         NOT NULL,
  SUB_ID                 VARCHAR2(30)           NULL,
  USER_KEY               NUMBER(11)             NULL,
  USER_GROUP             VARCHAR2(30)           NULL,
  USER_ID                VARCHAR2(30)           NULL,
  USER_JOBID             VARCHAR2(40)           NULL,
  CENTER_KEY             NUMBER(2)         DEFAULT 1          NOT NULL,
  MSG_PRIORITY           NUMBER(2)         DEFAULT 3          NOT NULL,
  MSG_STATE              NUMBER(2)         DEFAULT 1          NOT NULL,
  INPUT_DATE             DATE              DEFAULT SYSDATE               NOT NULL,
  RES_DATE               DATE              DEFAULT SYSDATE               NOT NULL,
  QUE_DATE               DATE                   NULL,
  SENT_DATE              DATE                   NULL,
  RSLT_DATE              DATE                   NULL,
  REPORT_DATE            DATE                   NULL,
  RSLT_CODE              VARCHAR2(4)            NULL,
  RSLT_NET               VARCHAR2(3)            NULL,
  RSLT_TYPE              VARCHAR2(3)            NULL,
  SENT_COUNT             NUMBER(2)         DEFAULT 0          NOT NULL,
  HISTORY_MSG_TYPE       VARCHAR2(23)           NULL,
  HISTORY_RSLT_CODE      VARCHAR2(28)           NULL,
  IDENTIFIER             VARCHAR2(9)            NULL,
  PHONE                  VARCHAR2(20)       NOT NULL,
  CALLBACK               VARCHAR2(20)       NOT NULL,
  MSG_TYPE_1             VARCHAR2(3)       DEFAULT 'SMS'        NOT NULL,
  CONTENTS_TYPE_1        VARCHAR2(3)       DEFAULT 'SMS'        NOT NULL,
  QUE_DATE_1             DATE                   NULL,
  SENT_DATE_1            DATE                   NULL,
  MSG_TYPE_2             VARCHAR2(3)            NULL,
  CONTENTS_TYPE_2        VARCHAR2(3)            NULL,
  QUE_DATE_2             DATE                   NULL,
  SENT_DATE_2            DATE                   NULL,
  MSG_TYPE_3             VARCHAR2(3)            NULL,
  CONTENTS_TYPE_3        VARCHAR2(3)            NULL,
  QUE_DATE_3             DATE                   NULL,
  SENT_DATE_3            DATE                   NULL,
  XMS_RSLT_CODE          VARCHAR2(4)            NULL,
  XMS_RSLT_NET           VARCHAR2(3)            NULL,
  XMS_RSLT_DATE          DATE                   NULL,
  XMS_REPORT_DATE        DATE                   NULL,
  ALT_RSLT_CODE          VARCHAR2(4)            NULL,
  ALT_RSLT_NET           VARCHAR2(3)            NULL,
  ALT_RSLT_DATE          DATE                   NULL,
  ALT_REPORT_DATE        DATE                   NULL,
  RCS_RSLT_CODE          VARCHAR2(4)            NULL,
  RCS_RSLT_NET           VARCHAR2(3)            NULL,
  RCS_RSLT_DATE          DATE                   NULL,
  RCS_REPORT_DATE        DATE                   NULL,
  XMS_SUBJECT            VARCHAR2(60)           NULL,
  XMS_TEXT               VARCHAR2(4000)         NULL,
  XMS_FILE_NAME_1        VARCHAR2(255)          NULL,
  XMS_FILE_NAME_2        VARCHAR2(255)          NULL,
  XMS_FILE_NAME_3        VARCHAR2(255)          NULL,
  ALT_COUNTRY_CODE       VARCHAR2(3)       DEFAULT '82'        NOT NULL,
  ALT_SENDER_KEY         VARCHAR2(40)           NULL,
  ALT_TEMPLATE_CODE      VARCHAR2(30)           NULL,
  ALT_JSON               VARCHAR2(4000)         NULL,
  RCS_BRAND_KEY          VARCHAR2(18)           NULL,
  RCS_MASSAGE_BASE_ID    VARCHAR2(100)          NULL,
  RCS_JSON               VARCHAR2(4000)         NULL
)
TABLESPACE 테이블스페이스
PCTFREE 10
PCTUSED 0
INITRANS 1
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
)
LOGGING
NOCACHE
MONITORING
NOPARALLEL
;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_DATA_01
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_DATA_01;

CREATE INDEX IDX_NRMSG_DATA_01
ON NRMSG_DATA (MSG_STATE, RES_DATE)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_DATA_02
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_DATA_02;

CREATE INDEX IDX_NRMSG_DATA_02
ON NRMSG_DATA (MSG_STATE, RES_DATE, SENT_COUNT, MSG_TYPE_1, MSG_TYPE_2, MSG_TYPE_3)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_DATA_03
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_DATA_03;

CREATE INDEX IDX_NRMSG_DATA_03
ON NRMSG_DATA (PHONE, CALLBACK)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_DATA_04
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_DATA_04;

CREATE INDEX IDX_NRMSG_DATA_04
ON NRMSG_DATA (SUB_ID)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

ALTER TABLE NRMSG_DATA ADD
(
    CONSTRAINT PK_NRMSG_DATA
    PRIMARY KEY ( MSG_KEY )
        USING INDEX
        TABLESPACE 테이블스페이스
        PCTFREE 10
        INITRANS 2
        MAXTRANS 255
        STORAGE (
            INITIAL 64 K
            NEXT 1024 K
            MINEXTENTS 1
            MAXEXTENTS UNLIMITED
        )
);


/*------------------------------------------------------------------------------
-- 객체명 : NRMSG_RSLT
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP TABLE NRMSG_RSLT CASCADE CONSTRAINTS;

CREATE TABLE NRMSG_RSLT (
  RSLT_KEY      NUMBER(11)        NOT NULL,
  RSLT_CODE     VARCHAR2(4)       NOT NULL,
  RSLT_TYPE     VARCHAR2(3)           NULL,
  RSLT_EXPLA    VARCHAR2(200)         NULL
)
TABLESPACE 테이블스페이스
PCTFREE 10
PCTUSED 0
INITRANS 1
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
)
LOGGING
NOCACHE
MONITORING
NOPARALLEL
;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_RSLT_01
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_RSLT_01;

CREATE INDEX IDX_NRMSG_RSLT_01
ON NRMSG_RSLT (RSLT_CODE)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_RSLT_02
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_RSLT_02;

CREATE INDEX IDX_NRMSG_RSLT_02
ON NRMSG_RSLT (RSLT_TYPE, RSLT_CODE)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

ALTER TABLE NRMSG_RSLT ADD
(
    CONSTRAINT PK_NRMSG_RSLT
    PRIMARY KEY ( RSLT_KEY )
        USING INDEX
        TABLESPACE 테이블스페이스
        PCTFREE 10
        INITRANS 2
        MAXTRANS 255
        STORAGE (
            INITIAL 64 K
            NEXT 1024 K
            MINEXTENTS 1
            MAXEXTENTS UNLIMITED
        )
);



/*------------------------------------------------------------------------------
-- 객체명 : NRMSG_LOG
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP TABLE NRMSG_LOG CASCADE CONSTRAINTS;

CREATE TABLE NRMSG_LOG (
  MSG_KEY                NUMBER(11)         NOT NULL,
  SUB_ID                 VARCHAR2(30)           NULL,
  USER_KEY               NUMBER(11)             NULL,
  USER_GROUP             VARCHAR2(30)           NULL,
  USER_ID                VARCHAR2(30)           NULL,
  USER_JOBID             VARCHAR2(40)           NULL,
  CENTER_KEY             NUMBER(2)         DEFAULT 1          NOT NULL,
  MSG_PRIORITY           NUMBER(2)         DEFAULT 3          NOT NULL,
  MSG_STATE              NUMBER(2)         DEFAULT 1          NOT NULL,
  INPUT_DATE             DATE              DEFAULT SYSDATE               NOT NULL,
  RES_DATE               DATE              DEFAULT SYSDATE               NOT NULL,
  QUE_DATE               DATE                   NULL,
  SENT_DATE              DATE                   NULL,
  RSLT_DATE              DATE                   NULL,
  REPORT_DATE            DATE                   NULL,
  RSLT_CODE              VARCHAR2(4)            NULL,
  RSLT_NET               VARCHAR2(3)            NULL,
  RSLT_TYPE              VARCHAR2(3)            NULL,
  SENT_COUNT             NUMBER(2)         DEFAULT 0          NOT NULL,
  HISTORY_MSG_TYPE       VARCHAR2(23)           NULL,
  HISTORY_RSLT_CODE      VARCHAR2(28)           NULL,
  IDENTIFIER             VARCHAR2(9)            NULL,
  PHONE                  VARCHAR2(20)       NOT NULL,
  CALLBACK               VARCHAR2(20)       NOT NULL,
  MSG_TYPE_1             VARCHAR2(3)       DEFAULT 'SMS'        NOT NULL,
  CONTENTS_TYPE_1        VARCHAR2(3)       DEFAULT 'SMS'        NOT NULL,
  QUE_DATE_1             DATE                   NULL,
  SENT_DATE_1            DATE                   NULL,
  MSG_TYPE_2             VARCHAR2(3)            NULL,
  CONTENTS_TYPE_2        VARCHAR2(3)            NULL,
  QUE_DATE_2             DATE                   NULL,
  SENT_DATE_2            DATE                   NULL,
  MSG_TYPE_3             VARCHAR2(3)            NULL,
  CONTENTS_TYPE_3        VARCHAR2(3)            NULL,
  QUE_DATE_3             DATE                   NULL,
  SENT_DATE_3            DATE                   NULL,
  XMS_RSLT_CODE          VARCHAR2(4)            NULL,
  XMS_RSLT_NET           VARCHAR2(3)            NULL,
  XMS_RSLT_DATE          DATE                   NULL,
  XMS_REPORT_DATE        DATE                   NULL,
  ALT_RSLT_CODE          VARCHAR2(4)            NULL,
  ALT_RSLT_NET           VARCHAR2(3)            NULL,
  ALT_RSLT_DATE          DATE                   NULL,
  ALT_REPORT_DATE        DATE                   NULL,
  RCS_RSLT_CODE          VARCHAR2(4)            NULL,
  RCS_RSLT_NET           VARCHAR2(3)            NULL,
  RCS_RSLT_DATE          DATE                   NULL,
  RCS_REPORT_DATE        DATE                   NULL,
  XMS_SUBJECT            VARCHAR2(60)           NULL,
  XMS_TEXT               VARCHAR2(4000)         NULL,
  XMS_FILE_NAME_1        VARCHAR2(255)          NULL,
  XMS_FILE_NAME_2        VARCHAR2(255)          NULL,
  XMS_FILE_NAME_3        VARCHAR2(255)          NULL,
  ALT_COUNTRY_CODE       VARCHAR2(3)       DEFAULT '82'        NOT NULL,
  ALT_SENDER_KEY         VARCHAR2(40)           NULL,
  ALT_TEMPLATE_CODE      VARCHAR2(30)           NULL,
  ALT_JSON               VARCHAR2(4000)         NULL,
  RCS_BRAND_KEY          VARCHAR2(18)           NULL,
  RCS_MASSAGE_BASE_ID    VARCHAR2(100)          NULL,
  RCS_JSON               VARCHAR2(4000)         NULL
)
TABLESPACE 테이블스페이스
PCTFREE 10
PCTUSED 0
INITRANS 1
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
)
LOGGING
NOCACHE
MONITORING
NOPARALLEL
;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_LOG_01
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_LOG_01;

CREATE INDEX IDX_NRMSG_LOG_01
ON NRMSG_LOG (MSG_STATE, RES_DATE)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_LOG_02
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_LOG_02;

CREATE INDEX IDX_NRMSG_LOG_02
ON NRMSG_LOG (MSG_STATE, RES_DATE, SENT_COUNT, MSG_TYPE_1, MSG_TYPE_2, MSG_TYPE_3)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_LOG_03
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_LOG_03;

CREATE INDEX IDX_NRMSG_LOG_03
ON NRMSG_LOG (PHONE, CALLBACK)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_LOG_04
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_LOG_04;

CREATE INDEX IDX_NRMSG_LOG_04
ON NRMSG_LOG (SUB_ID)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;

/*------------------------------------------------------------------------------
-- 객체명 : IDX_NRMSG_LOG_05
-- 생성일 : 2024-11-04 오전 10:03:29
-- 최종수정일 : 2024-11-04 오전 10:03:29
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP INDEX IDX_NRMSG_LOG_05;

CREATE INDEX IDX_NRMSG_LOG_05
ON NRMSG_LOG (USER_GROUP, USER_ID, USER_JOBID, RES_DATE)
PCTFREE 10
INITRANS 2
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    NEXT 1024 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    PCTINCREASE 0
)
TABLESPACE 테이블스페이스
LOGGING
NOPARALLEL;
