-- phpMyAdmin SQL Dump
-- version 3.2.4
-- http://www.phpmyadmin.net
--
-- 호스트: localhost:3326
-- 처리한 시간:	24-10-25 13:16
-- 서버	버전: 5.1.53
-- PHP 버전: 5.3.4

-- 권장 DB버전이 아닐 경우 테이블 생성에 실패남.
-- 원인은 구 버전에서는 'DEFAULT current_timestamp()'가 지원되지 안음
--
-- `INPUT_DATE` datetime NOT NULL DEFAULT current_timestamp(),
-- `RES_DATE`   datetime NOT NULL DEFAULT current_timestamp(),
--  대체
-- `INPUT_DATE` datetime NOT NULL ,
-- `RES_DATE`   datetime NOT NULL ,


SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- 데이터베이스: board
--

-- --------------------------------------------------------

--
-- 테이블 구조 nrmsg_data
--

DROP TABLE IF EXISTS nuri2_nrmsg_data;
CREATE TABLE IF	NOT EXISTS nuri2_nrmsg_data (
  MSG_KEY	int(11)	NOT NULL AUTO_INCREMENT	    COMMENT '고유번호',
  SUB_ID	varchar(30) DEFAULT NULL	    COMMENT 'SUB_ID(과금청구용)',
  USER_KEY	int(11)	DEFAULT	NULL		    COMMENT '사용자 고유 번호',
  USER_GROUP	varchar(30) DEFAULT NULL	    COMMENT '사용자 그룹',
  USER_ID	varchar(30) DEFAULT NULL	    COMMENT '사용자 고유 아이디',
  USER_JOBID	varchar(40) DEFAULT NULL	    COMMENT '사용자 JOB	아이디',
  CENTER_KEY int(2) NOT	NULL DEFAULT '1'	    COMMENT '발송라인',
  MSG_PRIORITY int(2) NOT NULL DEFAULT '3'	    COMMENT '메시지 전송 우선 순위',
  MSG_STATE int(2) NOT NULL DEFAULT '1'		    COMMENT '메시지 상태 값',
  INPUT_DATE timestamp NULL DEFAULT NULL	    COMMENT '메시지 입력시간',
  RES_DATE timestamp NULL DEFAULT NULL		    COMMENT '메시지 예약일시',
  QUE_DATE timestamp NULL DEFAULT NULL		    COMMENT '메시지를 수집 한 시간',
  SENT_DATE timestamp NULL DEFAULT NULL		    COMMENT '메시지를 전송 한 시간',
  RSLT_DATE timestamp NULL DEFAULT NULL		    COMMENT '핸드폰에 전달 된 시간',
  REPORT_DATE timestamp	NULL DEFAULT NULL	    COMMENT 'G/W에서 결과를 수신한 시간',
  RSLT_CODE varchar(4) DEFAULT NULL		    COMMENT '결과처리 상세코드',
  RSLT_NET varchar(3) DEFAULT NULL		    COMMENT '결과처리 통신사',
  RSLT_TYPE varchar(3) DEFAULT NULL		    COMMENT '결과처리 된 메시지	유형',
  SENT_COUNT int(2) NOT	NULL DEFAULT '0'	    COMMENT '결과처리 히스토리 재전송 횟수',
  HISTORY_MSG_TYPE varchar(23) DEFAULT NULL	    COMMENT '결과처리 히스토리 발송 타입',
  HISTORY_RSLT_CODE varchar(28)	DEFAULT	NULL	    COMMENT '결과처리 히스토리 상세코드',
  IDENTIFIER varchar(9)	DEFAULT	NULL		    COMMENT '메시지 식별자코드',
  PHONE	varchar(20) NOT	NULL			    COMMENT '수신 번호',
  CALLBACK varchar(20) NOT NULL			    COMMENT '발신 번호',
  MSG_TYPE_1 varchar(3)	NOT NULL DEFAULT 'SMS'	    COMMENT '발송 타입 1번째',
  CONTENTS_TYPE_1 varchar(3) NOT NULL DEFAULT 'SMS' COMMENT '메시지 내용에 대한	타입 1번째',
  QUE_DATE_1 timestamp NULL DEFAULT NULL	    COMMENT '1번째 메시지 수집 한 시간',
  SENT_DATE_1 timestamp	NULL DEFAULT NULL	    COMMENT '1번째 메시지 전송 한 시간',
  MSG_TYPE_2 varchar(3)	DEFAULT	NULL		    COMMENT '발송 타입 2번째',
  CONTENTS_TYPE_2 varchar(3) DEFAULT NULL	    COMMENT '메시지 내용에 대한	타입 2번째',
  QUE_DATE_2 timestamp NULL DEFAULT NULL	    COMMENT '2번째 메시지 수집 한 시간',
  SENT_DATE_2 timestamp	NULL DEFAULT NULL	    COMMENT '2번째 메시지 전송 한 시간',
  MSG_TYPE_3 varchar(3)	DEFAULT	NULL		    COMMENT '발송 타입 3번째',
  CONTENTS_TYPE_3 varchar(3) DEFAULT NULL	    COMMENT '메시지 내용에 대한	타입 3번째',
  QUE_DATE_3 timestamp NULL DEFAULT NULL	    COMMENT '3번째 메시지 수집 한 시간',
  SENT_DATE_3 timestamp	NULL DEFAULT NULL	    COMMENT '3번째 메시지 전송 한 시간',
  XMS_RSLT_CODE	varchar(4) DEFAULT NULL		    COMMENT 'XMS 결과처리 상세코드',
  XMS_RSLT_NET varchar(3) DEFAULT NULL		    COMMENT 'XMS 결과처리 통신사',
  XMS_RSLT_DATE	timestamp NULL DEFAULT NULL	    COMMENT 'XMS 핸드폰에 전달 된 시간',
  XMS_REPORT_DATE timestamp NULL DEFAULT NULL	    COMMENT 'XMS G/W에서 결과를	수신한 시간',
  ALT_RSLT_CODE	varchar(4) DEFAULT NULL		    COMMENT 'ALT 결과처리 상세코드',
  ALT_RSLT_NET varchar(3) DEFAULT NULL		    COMMENT 'ALT 결과처리 통신사',
  ALT_RSLT_DATE	timestamp NULL DEFAULT NULL	    COMMENT 'ALT 핸드폰에 전달 된 시간',
  ALT_REPORT_DATE timestamp NULL DEFAULT NULL	    COMMENT 'ALT G/W에서 결과를	수신한 시간',
  RCS_RSLT_CODE	varchar(4) DEFAULT NULL		    COMMENT 'RCS 결과처리 상세코드',
  RCS_RSLT_NET varchar(3) DEFAULT NULL		    COMMENT 'RCS 결과처리 통신사',
  RCS_RSLT_DATE	timestamp NULL DEFAULT NULL	    COMMENT 'RCS 핸드폰에 전달 된 시간',
  RCS_REPORT_DATE timestamp NULL DEFAULT NULL	    COMMENT 'RCS G/W에서 결과를	수신한 시간',
  XMS_SUBJECT varchar(60) DEFAULT NULL		    COMMENT '메시지 타이틀(LMS/MMS)',
  XMS_TEXT varchar(4000) DEFAULT NULL		    COMMENT '메시지 본문(SMS/LMS/MMS)',
  XMS_FILE_NAME_1 varchar(255) DEFAULT NULL	    COMMENT '파일경로를	포함한 파일명1(MMS)',
  XMS_FILE_NAME_2 varchar(255) DEFAULT NULL	    COMMENT '파일경로를	포함한 파일명2(MMS)',
  XMS_FILE_NAME_3 varchar(255) DEFAULT NULL	    COMMENT '파일경로를	포함한 파일명3(MMS)',
  ALT_COUNTRY_CODE varchar(3) NOT NULL DEFAULT '82' COMMENT 'ALT 국가코드(ALT,ALI)',
  ALT_SENDER_KEY varchar(40) DEFAULT NULL	    COMMENT 'ALT 사용자	아이디(ALT,ALI), 발송키(발신 프로필키)',
  ALT_TEMPLATE_CODE varchar(30)	DEFAULT	NULL	    COMMENT 'ALT 등록된	템플릿 고유키(ALT,ALI)',
  ALT_JSON varchar(4000) DEFAULT NULL		    COMMENT '메시지 본문(ALT,ALI)',
  RCS_BRAND_ID varchar(18) DEFAULT NULL		    COMMENT 'RCS ID, BR.XXXXX',
  RCS_BRAND_KEY	varchar(18) DEFAULT NULL	    COMMENT 'RCS 브랜드	키, BK.XXXXXXXXXXXXXXX',
  RCS_MESSAGE_BASE_ID varchar(100) DEFAULT NULL	    COMMENT 'RCS 메시지베이스 아이디',
  RCS_JSON varchar(4000) DEFAULT NULL		    COMMENT '메시지 본문(RCS)',
  PRIMARY KEY (MSG_KEY),
  KEY IDX_nuri2_nrmsg_data_01 (MSG_STATE,RES_DATE),
  KEY IDX_nuri2_nrmsg_data_02 (MSG_STATE,RES_DATE,MSG_PRIORITY,SENT_COUNT,MSG_TYPE_1,MSG_TYPE_2,MSG_TYPE_3),
  KEY IDX_nuri2_nrmsg_data_03 (PHONE,CALLBACK),
  KEY IDX_nuri2_nrmsg_data_04 (SUB_ID)
) ENGINE=InnoDB	 DEFAULT CHARSET=euckr;
-- --------------------------------------------------------
--
--
-- -- 기존 인덱스 삭제
-- DROP	INDEX IDX_nuri2_nrmsg_data_02 ON nuri2_nrmsg_data ;
--
-- -- 새 인덱스	생성
-- CREATE INDEX	IDX_nuri2_nrmsg_data_02	ON nuri2_nrmsg_data  (MSG_STATE,RES_DATE,MSG_PRIORITY,SENT_COUNT,MSG_TYPE_1,MSG_TYPE_2,MSG_TYPE_3);
--


--
-- 테이블 구조 nrmsg_data_log
--

DROP TABLE IF EXISTS nuri2_nrmsg_data_log;
CREATE TABLE IF	NOT EXISTS nuri2_nrmsg_data_log	(
  MSG_KEY int(11) NOT NULL,
  SUB_ID varchar(30) DEFAULT NULL,
  USER_KEY int(11) DEFAULT NULL,
  USER_GROUP varchar(30) DEFAULT NULL,
  USER_ID varchar(30) DEFAULT NULL,
  USER_JOBID varchar(40) DEFAULT NULL,
  CENTER_KEY int(2) NOT	NULL DEFAULT '1',
  MSG_PRIORITY int(2) NOT NULL DEFAULT '3',
  MSG_STATE int(2) NOT NULL DEFAULT '1',
  INPUT_DATE timestamp NOT NULL	DEFAULT	null ,
  RES_DATE timestamp NOT NULL DEFAULT NULL,
  QUE_DATE timestamp NULL DEFAULT NULL,
  SENT_DATE timestamp NULL DEFAULT NULL,
  RSLT_DATE timestamp NULL DEFAULT NULL,
  REPORT_DATE timestamp	NULL DEFAULT NULL,
  RSLT_CODE varchar(4) DEFAULT NULL,
  RSLT_NET varchar(3) DEFAULT NULL,
  RSLT_TYPE varchar(3) DEFAULT NULL,
  SENT_COUNT int(2) NOT	NULL DEFAULT '0',
  HISTORY_MSG_TYPE varchar(23) DEFAULT NULL,
  HISTORY_RSLT_CODE varchar(28)	DEFAULT	NULL,
  IDENTIFIER varchar(9)	DEFAULT	NULL,
  PHONE	varchar(20) NOT	NULL,
  CALLBACK varchar(20) NOT NULL,
  MSG_TYPE_1 varchar(3)	NOT NULL DEFAULT 'SMS',
  CONTENTS_TYPE_1 varchar(3) NOT NULL DEFAULT 'SMS',
  QUE_DATE_1 timestamp NULL DEFAULT NULL,
  SENT_DATE_1 timestamp	NULL DEFAULT NULL,
  MSG_TYPE_2 varchar(3)	DEFAULT	NULL,
  CONTENTS_TYPE_2 varchar(3) DEFAULT NULL,
  QUE_DATE_2 timestamp NULL DEFAULT NULL,
  SENT_DATE_2 timestamp	NULL DEFAULT NULL,
  MSG_TYPE_3 varchar(3)	DEFAULT	NULL,
  CONTENTS_TYPE_3 varchar(3) DEFAULT NULL,
  QUE_DATE_3 timestamp NULL DEFAULT NULL,
  SENT_DATE_3 timestamp	NULL DEFAULT NULL,
  XMS_RSLT_CODE	varchar(4) DEFAULT NULL,
  XMS_RSLT_NET varchar(3) DEFAULT NULL,
  XMS_RSLT_DATE	timestamp NULL DEFAULT NULL,
  XMS_REPORT_DATE timestamp NULL DEFAULT NULL,
  ALT_RSLT_CODE	varchar(4) DEFAULT NULL,
  ALT_RSLT_NET varchar(3) DEFAULT NULL,
  ALT_RSLT_DATE	timestamp NULL DEFAULT NULL,
  ALT_REPORT_DATE timestamp NULL DEFAULT NULL,
  RCS_RSLT_CODE	varchar(4) DEFAULT NULL,
  RCS_RSLT_NET varchar(3) DEFAULT NULL,
  RCS_RSLT_DATE	timestamp NULL DEFAULT NULL,
  RCS_REPORT_DATE timestamp NULL DEFAULT NULL,
  XMS_SUBJECT varchar(60) DEFAULT NULL,
  XMS_TEXT varchar(4000) DEFAULT NULL,
  XMS_FILE_NAME_1 varchar(255) DEFAULT NULL,
  XMS_FILE_NAME_2 varchar(255) DEFAULT NULL,
  XMS_FILE_NAME_3 varchar(255) DEFAULT NULL,
  ALT_COUNTRY_CODE varchar(3) NOT NULL DEFAULT '82',
  ALT_SENDER_KEY varchar(40) DEFAULT NULL,
  ALT_TEMPLATE_CODE varchar(30)	DEFAULT	NULL,
  ALT_JSON varchar(4000) DEFAULT NULL,
  RCS_BRAND_KEY	varchar(18) DEFAULT NULL,
  RCS_MESSAGE_BASE_ID varchar(100) DEFAULT NULL,
  RCS_JSON varchar(4000) DEFAULT NULL,
  PRIMARY KEY (MSG_KEY),
  KEY idx_nuri2_nrmsg_data_log_01 (MSG_STATE,RES_DATE),
  KEY idx_nuri2_nrmsg_data_log_02 (MSG_STATE,RES_DATE,MSG_PRIORITY,SENT_COUNT,MSG_TYPE_1,MSG_TYPE_2,MSG_TYPE_3),
  KEY idx_nuri2_nrmsg_data_log_03 (PHONE,CALLBACK),
  KEY idx_nuri2_nrmsg_data_log_04 (SUB_ID),
  KEY idx_nuri2_nrmsg_data_log_05 (USER_GROUP,USER_ID,USER_JOBID,RES_DATE)
) ENGINE=InnoDB	DEFAULT	CHARSET=euckr;

