-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: 175.197.48.213    Database: nuri
-- ------------------------------------------------------
-- Server version	8.0.33

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `NURI_MMS_CONTENTS_INFO`
--

DROP TABLE IF EXISTS `NURI_MMS_CONTENTS_INFO`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MMS_CONTENTS_INFO` (
  `CONT_SEQ` int NOT NULL AUTO_INCREMENT,
  `FILE_CNT` int NOT NULL,
  `BUILD_YN` char(1) DEFAULT NULL,
  `MMS_BODY` text,
  `MMS_SUBJECT` varchar(40) DEFAULT NULL,
  `FILE_TYPE1` varchar(3) DEFAULT NULL,
  `FILE_TYPE2` varchar(3) DEFAULT NULL,
  `FILE_TYPE3` varchar(3) DEFAULT NULL,
  `FILE_TYPE4` varchar(3) DEFAULT NULL,
  `FILE_TYPE5` varchar(3) DEFAULT NULL,
  `FILE_NAME1` varchar(100) DEFAULT NULL,
  `FILE_NAME2` varchar(100) DEFAULT NULL,
  `FILE_NAME3` varchar(100) DEFAULT NULL,
  `FILE_NAME4` varchar(100) DEFAULT NULL,
  `FILE_NAME5` varchar(100) DEFAULT NULL,
  `SERVICE_DEP1` varchar(3) DEFAULT NULL,
  `SERVICE_DEP2` varchar(3) DEFAULT NULL,
  `SERVICE_DEP3` varchar(3) DEFAULT NULL,
  `SERVICE_DEP4` varchar(3) DEFAULT NULL,
  `SERVICE_DEP5` varchar(3) DEFAULT NULL,
  `SKN_FILE_DATE` datetime DEFAULT NULL,
  `SKN_FILE_NAME` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`CONT_SEQ`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MMS_CONTENTS_INFO`
--

LOCK TABLES `NURI_MMS_CONTENTS_INFO` WRITE;
/*!40000 ALTER TABLE `NURI_MMS_CONTENTS_INFO` DISABLE KEYS */;
/*!40000 ALTER TABLE `NURI_MMS_CONTENTS_INFO` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NURI_MSG_DATA`
--

DROP TABLE IF EXISTS `NURI_MSG_DATA`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MSG_DATA` (
  `USERDATA` int DEFAULT NULL,
  `MSG_SEQ` int NOT NULL AUTO_INCREMENT,
  `SUB_ID` varchar(20) DEFAULT NULL,
  `CUR_STATE` int NOT NULL,
  `SENT_DATE` datetime DEFAULT NULL,
  `RSLT_DATE` datetime DEFAULT NULL,
  `REQ_DATE` datetime NOT NULL,
  `RSLT_CODE` int DEFAULT NULL,
  `RSLT_CODE2` char(1) DEFAULT NULL,
  `RSLT_NET` char(3) DEFAULT NULL,
  `CALL_TO` varchar(12) DEFAULT NULL,
  `CALL_FROM` varchar(12) DEFAULT NULL,
  `SMS_TXT` varchar(160) DEFAULT NULL,
  `MSG_TYPE` int NOT NULL,
  `CONT_SEQ` int DEFAULT NULL,
  PRIMARY KEY (`MSG_SEQ`),
  KEY `IDX_NURI_MSG_DATA_01` (`CUR_STATE`,`REQ_DATE`),
  KEY `IDX_NURI_MSG_DATA_02` (`CALL_TO`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MSG_DATA`
--

LOCK TABLES `NURI_MSG_DATA` WRITE;
/*!40000 ALTER TABLE `NURI_MSG_DATA` DISABLE KEYS */;
/*!40000 ALTER TABLE `NURI_MSG_DATA` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NURI_MSG_LOG_202306`
--

DROP TABLE IF EXISTS `NURI_MSG_LOG_202306`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MSG_LOG_202306` (
  `USERDATA` int DEFAULT NULL,
  `MSG_SEQ` int NOT NULL,
  `SUB_ID` varchar(20) DEFAULT NULL,
  `CUR_STATE` int NOT NULL,
  `SENT_DATE` datetime DEFAULT NULL,
  `RSLT_DATE` datetime DEFAULT NULL,
  `REQ_DATE` datetime NOT NULL,
  `RSLT_CODE` int DEFAULT NULL,
  `RSLT_CODE2` char(1) DEFAULT NULL,
  `RSLT_NET` char(3) DEFAULT NULL,
  `CALL_TO` varchar(12) DEFAULT NULL,
  `CALL_FROM` varchar(12) DEFAULT NULL,
  `SMS_TXT` varchar(160) DEFAULT NULL,
  `MSG_TYPE` int NOT NULL,
  `CONT_SEQ` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MSG_LOG_202306`
--

LOCK TABLES `NURI_MSG_LOG_202306` WRITE;
/*!40000 ALTER TABLE `NURI_MSG_LOG_202306` DISABLE KEYS */;
/*!40000 ALTER TABLE `NURI_MSG_LOG_202306` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NURI_MSG_LOG_202307`
--

DROP TABLE IF EXISTS `NURI_MSG_LOG_202307`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MSG_LOG_202307` (
  `USERDATA` int DEFAULT NULL,
  `MSG_SEQ` int NOT NULL,
  `SUB_ID` varchar(20) DEFAULT NULL,
  `CUR_STATE` int NOT NULL,
  `SENT_DATE` datetime DEFAULT NULL,
  `RSLT_DATE` datetime DEFAULT NULL,
  `REQ_DATE` datetime NOT NULL,
  `RSLT_CODE` int DEFAULT NULL,
  `RSLT_CODE2` char(1) DEFAULT NULL,
  `RSLT_NET` char(3) DEFAULT NULL,
  `CALL_TO` varchar(12) DEFAULT NULL,
  `CALL_FROM` varchar(12) DEFAULT NULL,
  `SMS_TXT` varchar(160) DEFAULT NULL,
  `MSG_TYPE` int NOT NULL,
  `CONT_SEQ` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MSG_LOG_202307`
--

LOCK TABLES `NURI_MSG_LOG_202307` WRITE;
/*!40000 ALTER TABLE `NURI_MSG_LOG_202307` DISABLE KEYS */;
INSERT INTO `NURI_MSG_LOG_202307` VALUES (NULL,1,NULL,3,'2023-07-30 13:16:57','2023-07-30 13:17:08','2023-07-30 13:16:53',410,'2','SKT','01100000000','0420000000','메세지발송테스트 입니다 1',4,NULL),(NULL,2,NULL,3,'2023-07-30 13:37:02','2023-07-30 13:37:09','2023-07-30 13:36:56',410,'d','SKT','01031248577','0420000000','메세지발송테스트 입니다 1',4,NULL);
/*!40000 ALTER TABLE `NURI_MSG_LOG_202307` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NURI_MSG_LOG_202308`
--

DROP TABLE IF EXISTS `NURI_MSG_LOG_202308`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MSG_LOG_202308` (
  `USERDATA` int DEFAULT NULL,
  `MSG_SEQ` int NOT NULL,
  `SUB_ID` varchar(20) DEFAULT NULL,
  `CUR_STATE` int NOT NULL,
  `SENT_DATE` datetime DEFAULT NULL,
  `RSLT_DATE` datetime DEFAULT NULL,
  `REQ_DATE` datetime NOT NULL,
  `RSLT_CODE` int DEFAULT NULL,
  `RSLT_CODE2` char(1) DEFAULT NULL,
  `RSLT_NET` char(3) DEFAULT NULL,
  `CALL_TO` varchar(12) DEFAULT NULL,
  `CALL_FROM` varchar(12) DEFAULT NULL,
  `SMS_TXT` varchar(160) DEFAULT NULL,
  `MSG_TYPE` int NOT NULL,
  `CONT_SEQ` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MSG_LOG_202308`
--

LOCK TABLES `NURI_MSG_LOG_202308` WRITE;
/*!40000 ALTER TABLE `NURI_MSG_LOG_202308` DISABLE KEYS */;
INSERT INTO `NURI_MSG_LOG_202308` VALUES (NULL,1,NULL,3,'2023-08-01 11:14:54',NULL,'2023-08-01 11:14:00',NULL,'q',NULL,'01042794214','027337365','web 테스트입니다. 1',4,NULL),(NULL,2,NULL,3,'2023-08-01 11:22:48',NULL,'2023-08-01 11:14:00',NULL,'q',NULL,'01042794214','027337365','web 테스트입니다. 1',4,NULL),(NULL,3,NULL,3,'2023-08-01 11:29:10','2023-08-01 11:29:20','2023-08-01 11:14:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 1',4,NULL),(NULL,4,NULL,3,'2023-08-01 11:30:38','2023-08-01 11:30:49','2023-08-01 11:30:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 2',4,NULL),(NULL,5,NULL,3,'2023-08-01 11:31:54','2023-08-01 11:32:03','2023-08-01 11:30:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 3',4,NULL),(NULL,6,NULL,3,'2023-08-01 11:31:55','2023-08-01 11:32:04','2023-08-01 11:30:00',410,'d','KT','01062776545','027337365','web 테스트입니다. 3',4,NULL),(NULL,7,NULL,3,'2023-08-03 09:42:28','2023-08-03 09:42:39','2023-08-03 09:41:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 8.3 09:42',4,NULL),(NULL,16,NULL,3,'2023-08-03 14:52:43','2023-08-03 14:52:53','2023-08-03 14:52:39',410,'d','KT','01042794214','027337365','web 테스트입니다. 10',4,NULL),(NULL,26,NULL,3,'2023-08-03 16:47:49','2023-08-03 16:48:00','2023-08-03 16:47:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 10',4,NULL),(NULL,27,NULL,3,'2023-08-03 16:52:09','2023-08-03 16:52:19','2023-08-03 16:47:00',410,'d','KT','01042794214','027337365','web 테스트입니다. 10',4,NULL),(NULL,31,NULL,3,'2023-08-03 17:37:58','2023-08-03 17:38:08','2023-08-03 17:37:00',410,'d','LGU','01087651276','027337365','web 테스트입니다.',4,NULL);
/*!40000 ALTER TABLE `NURI_MSG_LOG_202308` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `NURI_MSG_SPAM`
--

DROP TABLE IF EXISTS `NURI_MSG_SPAM`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `NURI_MSG_SPAM` (
  `SPAM_SEQ` int NOT NULL AUTO_INCREMENT,
  `SPAM_FLAG` char(1) NOT NULL,
  `SPAM_ID` varchar(20) DEFAULT NULL,
  `SPAM_DATE` datetime NOT NULL,
  `SPAM_CALL_TO` varchar(15) DEFAULT NULL,
  `SPAM_CALL_FROM` varchar(15) DEFAULT NULL,
  `SPAM_SMS_TXT` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`SPAM_SEQ`),
  KEY `IDX_NURI_MSG_SPAM_01` (`SPAM_ID`,`SPAM_CALL_TO`,`SPAM_CALL_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `NURI_MSG_SPAM`
--

LOCK TABLES `NURI_MSG_SPAM` WRITE;
/*!40000 ALTER TABLE `NURI_MSG_SPAM` DISABLE KEYS */;
/*!40000 ALTER TABLE `NURI_MSG_SPAM` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_error_code`
--

DROP TABLE IF EXISTS `app_error_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_error_code` (
  `APP_GUBUN` varchar(8) NOT NULL DEFAULT '' COMMENT '앱구분',
  `APP_CODE` varchar(4) NOT NULL DEFAULT '' COMMENT '에러코드',
  `MESSAGE` varchar(100) DEFAULT NULL COMMENT '에레메시지',
  `DESCRIPTION` varchar(300) DEFAULT NULL COMMENT '에러_설명',
  KEY `APP_GUBUN` (`APP_GUBUN`),
  KEY `APP_CODE` (`APP_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=euckr COMMENT='에러코드 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_error_code`
--

LOCK TABLES `app_error_code` WRITE;
/*!40000 ALTER TABLE `app_error_code` DISABLE KEYS */;
INSERT INTO `app_error_code` VALUES ('KAKAO','0000','SUCCESS','성공'),('KAKAO','1001','NoJsonBody','Request Body가 Json형식이 아님'),('KAKAO','1002','InvalidHubPartnerKey','허브 파트너 키가 유효하지 않음'),('KAKAO','1003','InvalidSenderKey','발신 프로필 키가 유효하지 않음'),('KAKAO','1004','NoValueJsonElement','Request Body(Json)에서 name을 찾을 수 없음'),('KAKAO','1006','DeletedSender','삭제된 발신프로필. (메시지 사업 담당자에게 문의)'),('KAKAO','1007','StoppedSender','차단 상태의 발신프로필. (메시지 사업 담당자에게 문의)'),('KAKAO','1011','ContractNotFound','계약정보를 찾을 수 없음. (메시지 사업 담당자에게 문의)'),('KAKAO','1012','InvalidUserKeyException','잘못된 형식의 유저키 요청'),('KAKAO','1013','InvalidAppLink','유효하지 않은 app연결'),('KAKAO','1014','InvalidBizNum','유효하지 않은 사업자번호'),('KAKAO','1015','TalkUserIdNotFonud','유효하지 않은 app user id 요청'),('KAKAO','1016','BizNumNotEqual','사업자등록번호 불일치'),('KAKAO','1020','InvalidReceiveUser','전화번호 or app user id가 유효하지 않거나 미입력 요청'),('KAKAO','1021','BlockedProfile','차단 상태의 카카오톡 채널 (카카오톡 채널 운영툴에서 확인)'),('KAKAO','1022','DeactivatedProfile','닫힘 상태의 카카오톡 채널 (카카오톡 채널 운영툴에서 확인)'),('KAKAO','1023','DeletedProfile','삭제된 카카오톡 채널 (카카오톡 채널 운영툴에서 확인)'),('KAKAO','1024','DeletingProfile','삭제대기 상태의 카카오톡 채널 (카카오톡 채널 운영툴에서 확인)'),('KAKAO','1025','SpammedProfile','메시지차단 상태의 카카오톡 채널 (카카오톡 채널 운영툴에서확인)'),('KAKAO','1026','UnableUseMessageType','메시지차단 상태의 카카오톡 채널 (카카오톡 채널 운영툴에서확인)'),('KAKAO','1030','InvalidParameterException','잘못된 파라메터 요청'),('KAKAO','2003','FailedToSendMessageByNoFriendshipException','(테스트 발송) 카카오톡 채널을 추가하지 않았음'),('KAKAO','2004','FailedToMatchTemplateException','템플릿 일치 확인시 오류 발생(내부 오류 발생)'),('KAKAO','2006','FailedToMatchSerialNumber','시리얼넘버 형식 불일치'),('KAKAO','3000','UnexpectedException','예기치 않은 오류 발생'),('KAKAO','3005','AckTimeoutException','메시지를 발송했으나 수신확인 안됨 (성공불확실)'),('KAKAO','3006','FailedToSendMessageException','내부 시스템 오류로 메시지 전송 실패'),('KAKAO','3008','InvalidPhoneNumberException','전화번호 오류'),('KAKAO','3010','JsonParseException','Json 파싱 오류'),('KAKAO','3011','MessageNotFoundException','메시지가 존재하지 않음'),('KAKAO','3012','SerialNumberDuplicatedException','메시지 일련번호가 중복됨'),('KAKAO','3013','MessageEmptyException','메시지가 비어 있음'),('KAKAO','3014','MessageLengthOverLimitException','메시지 길이 제한 오류 (템플릿별 제한 길이 또는 1000자 초과)'),('KAKAO','3015','TemplateNotFoundException','템플릿을 찾을 수 없음'),('KAKAO','3016','NoMatchedTemplateException','메시지 내용이 템플릿과 일치하지 않음'),('KAKAO','3018','NoSendAvailableException','메시지를 전송할 수 없음, 카톡 설치 안됨, 알림톡 차단'),('KAKAO','3025','ExceedMaxVariableLengthException','변수 글자수 제한 초과'),('KAKAO','3026','Button chat_extra(event)-InvalidExtra(EventName)Exception','상담/봇 전환 버튼 extra,  event 글자수 제한 초과'),('KAKAO','3027','NoMatchedTemplateButtonException / NoMatchedTemplateQuickReplyException','메시지 버튼/바로연결이 템플릿과 일치하지 않음'),('KAKAO','3028','NoMatchedTemplateTitleException','메시지 강조 표기 타이틀이 템플릿과 일치하지 않음'),('KAKAO','3029','ExceedMaxTitleLengthException','메시지 강조 표기 타이틀 길이 제한 초과 (50자)'),('KAKAO','3030','NoMatchedTemplateWithMessageTypeException','메시지 타입과 템플릿 강조유형이 일치하지 않음'),('KAKAO','3031','NoMatchedTemplateHeaderException','헤더가 템플릿과 일치하지 않음'),('KAKAO','3032','ExceedMaxHeaderLengthException','헤더 길이 제한 초과(16자)'),('KAKAO','3033','NoMatchedTemplateItemHighlightException','아이템 하이라이트가 템플릿과 일치하지 않음'),('KAKAO','3034','ExceedMaxItemHighlightTitleLengthException','아이템 하이라이트 타이틀 길이 제한 초과(이미지 없는 경우 30자, 이미지 있는 경우 21자)'),('KAKAO','3035','ExceedMaxItemHighlightDescriptionLengthException','아이템 하이라이트 디스크립션 길이 제한 초과(이미지 없는경우 19자, 이미지 있는 경우 14자)'),('KAKAO','3036','NoMatchedTemplateItemListException','아이템 리스트가 템플릿과 일치하지 않음'),('KAKAO','3037','ExceedMaxItemDescriptionLengthException','아이템 리스트의 아이템의 디스크립션 길이 제한 초과(23자)'),('KAKAO','3038','NoMatchedTemplateItemSummaryException','아이템 요약정보가 템플릿과 일치하지 않음'),('KAKAO','3039','ExceedMaxItemSummaryDescriptionLengthException','아이템 요약정보의 디스크립션 길이 제한 초과(14자)'),('KAKAO','3040','InvalidItemSummary','아이템 요약정보의 디스크립션에 허용되지 않은 문자 포함(통화기호/코드, 숫자,  콤마,  소수점, 공백을 제외한 문자 포함)'),('KAKAO','4000','ResponseHistoryNotFoundException','메시지 전송 결과를 찾을 수 없음'),('KAKAO','4001','UnknownMessageStatusError','알 수 없는 메시지 상태'),('KAKAO','5000','InvalidTestUser','(테스트 발송) 관리자 혹은 일회성 인증을 받은 사용자가 아님'),('KAKAO','5001','DailyTestLimitExceeded','(테스트 발송) 일일 발송량 초과'),('KAKAO','9991','fail','발송실패(발송정보알수없음), 모바일데이터가 꺼져있음'),('KAKAO','9998','noservice','현재 서비스를 제공하고 있지 않습니다.\n시스템에 문제가 발생하여 담당자가 확인하고 있는 경우'),('KAKAO','9999','unkownerror','시스템에서 알 수 없는 문제가 발생하였습니다.\n담당자가 확인 중입니다.\n시스템에 문제가 발생하여 담당자가 확인하고 있는 경우'),('KAKAO','K991','connectFail','APP G/W >> KAKAO 서버 연결실패'),('NAVER','0000','SUCCESS','메세지가 유저에게 앱과 웹으로 성공적으로전송되었습니다.'),('NAVER','0001','','메세지가 유저에게 웹으로 전송되었으나 앱전송에는 실패하였습니다.'),('NAVER','1001','','AgentKey나 PartnerKey가 누락되었습니다.'),('NAVER','1002','','보내기 API의 요청 Body에 문제가있습니다.'),('NAVER','1003','','전화번호나 액세스 토큰 중 적어도 하나의 파라미터가 필요합니다.'),('NAVER','1004','','전화번호 패턴이 잘못되었습니다.'),('NAVER','1005','','파트너키나 발송그룹키가 반드시 있어야 합니다.'),('NAVER','2001','','템플릿에 필요한 파라미터가 부족합니다.'),('NAVER','2002','','템플릿 아이디가 존재하지않습니다.'),('NAVER','2003','','템플릿에 필요한 파라미터가 일치하지 않습니다.'),('NAVER','2004','','템플릿에 필요한 버튼 갯수와 요청한 버튼 갯수가 일치하지 않습니다.'),('NAVER','2005','','템플릿에 필요한 파라미터가 일치하지 않습니다. 버튼코드를 확인하세요.'),('NAVER','2006','','등록된 템플릿과 일치하지 않는 메세지(message) 파라미터입니다.'),('NAVER','2007','','승인되지 않은 템플릿은 발송할 수 없습니다. 템플릿 승인 상태를 확인하세요.'),('NAVER','2008','','등록된 템플릿과 메세지 파라미터를 비교하던 중 오류가 발생하였습니다. 2006 응답코드와 거의 동일한 상황이나, 특수문자에 의해 불일치가 있는경우입니다.'),('NAVER','2009','','관리자에 의해 차단된 템플릿입니다. 자세한 내용은 문의 하세요.'),('NAVER','2010','','템플릿에 사용하는 샘플 이미지가 등록되지 않았습니다. 샘플 이미지를 등록 하세요.'),('NAVER','2011','','이미지 해시아이디에 해당하는 파트너키나 템플릿그룹키를 사용해야합니다.'),('NAVER','2101','','템플릿에 저장된 템플릿그룹 아이디와 일치하지 않는 템플릿 요청입니다. 템플릿 설정을 확인하세요.'),('NAVER','2102','','유효하지 않은 템플릿그룹키 입니다. 그룹키를 확인하세요.'),('NAVER','2103','','템플릿그룹의 파트너 목록에 존재하지 않는 요청입니다.'),('NAVER','2104','','이미지 해시 아이디에 해당하는 템플릿그룹키를 확인하세요.'),('NAVER','3001','','에이전트 키에 해당하는 파트너가 매핑되어 있지 않습니다.'),('NAVER','3002','','전화번호에 해당하는 유저가 존재하지않습니다.'),('NAVER','3003','','매핑된 파트너가 존재하지 않습니다.'),('NAVER','3004','','이미 존재하는 메세지키 입니다.'),('NAVER','3005','','전화번호에 해당하는 서로 다른 유저가 존재합니다.'),('NAVER','4001','','유저가 정보성 알림을 받을 수 있는 상태가 아닙니다.'),('NAVER','4002','','유저가 스마트알림 수신을 차단하였습니다.'),('NAVER','4003','','유저가 파트너계정을 차단하였습니다.'),('NAVER','9051','','메세지 시스템의 오류로 정보성 알림 송신이 불가합니다. 주로 톡톡메세지를 보내다가 발생하는 오류이며, 자세한 오류코드는 점차 업데이트 하겠습니다.'),('NAVER','9999','','알 수 없는 이유로 메세지가 전송되지 않았습니다.'),('GAON','G100','','APP_GUBUN에KAKAO, NAVER 이외의값이입력됨(대문자만입력가능)'),('GAON','G101','','전화번호에러'),('GAON','G200','','TEMPLATE_CODE 테이블에템플릿이존재하지않음'),('GAON','G201','','버튼의JSON형식이맞지않음'),('GAON','G202','','버튼이존재하나url이http:// 혹은https://로시작하지않음'),('GAON','G300','','메시지내용이빈값'),('GAON','G301','','GAON_MSG_TYPE이S인경우내용이90Byte 초과'),('GAON','G400','','각번호별일일발송량초과'),('GAON','G777','','TEST BED'),('GAON','G900','','서버연결실패'),('GAON','G901','','인증실패'),('GAON','G902','','타임아웃'),('GAON','G903','','메시지내용이제한길이를초과하였으나euc-kr기준으로2000byte 이하로문자발송'),('NURI','0','','성공'),('NURI','f','','MMS G/W 자체 형식 오류'),('NURI','1','','TIMEOUT'),('NURI','g','','SMS/LMS/MMS 서비스 불가 단말기'),('NURI','A','','핸드폰 호 처리 중'),('NURI','h','','핸드폰 호 불가 상태'),('NURI','B','','음영지역'),('NURI','i','','SMC 운영자가 메시지 삭제'),('NURI','C','','power off'),('NURI','j','','이통사 내부 메시지 Que Full'),('NURI','D','','메시지 저장개수 초과'),('NURI','k','','이통사에서 spam 처리'),('NURI','2','','잘못된 전화번호'),('NURI','l','','www.nospam.go.kr 에 등록된 번호에 대해  G/W에서 spam 처리한 건'),('NURI','a','','일시 서비스 정지'),('NURI','m','','MMS G/W에서 Spam 처리한 건(1.2.x: 90초과시)'),('NURI','b','','기타 단말기 문제'),('NURI','n','','건수제안에 걸린 경우 (건수제안 계약이 되어 있는 경우)'),('NURI','c','','착신 거절'),('NURI','o','','메시지의 길이가 제안된 길이를 벗어난 경우'),('NURI','d','','기타'),('NURI','p','','폰 번호가 형식에 어긋난 경우'),('NURI','e','','이통사 SMC 형식 오류'),('NURI','Q','','필드 형식이 잘못된 경우 (예:데이터 내용이 없는 경우)'),('NURI','s','','메시지 스팸차단(Nuri 내부)'),('NURI','x','','MMS 콘텐트의 정보를 참조할 수 없음'),('NURI','u','','BARCODE 생성 실패'),('NURI','r','','회신번호 스팸차단(Nuri 내부)'),('NURI','q','','메시지 중복키 체크(Nuri 내부)'),('NURI','y','','하루에 한 수신번호에 보낼수 있는 메시지 수량초과(Nuri 내부)'),('NURI','w','','SMS 전송문자에 특정키워드가 없으면 SPAM 처리하여 메시지 전송제한(Nuri 내부)'),('NURI','t','','스팸차단 중 2개 이상 중복 차단(Nuri 내부)'),('NURI','z','','처리 되지 않은 기타오류'),('NURI','Z','','메시지 접수시 기타 실패');
/*!40000 ALTER TABLE `app_error_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_send_contents`
--

DROP TABLE IF EXISTS `app_send_contents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_send_contents` (
  `REQ_SEND_DATE` char(14) DEFAULT NULL COMMENT '메시지를 DB에 넣은 시간 (yyyymmddHHMMSS)',
  `PACK_UNIQUEKEY` int NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `MSG_SUBJECT` varchar(50) DEFAULT NULL COMMENT '메시지 제목',
  `MSG_DATA` varchar(3000) NOT NULL COMMENT '메시지 내용',
  `MSG_TYPE` char(2) NOT NULL COMMENT '메시지 타입 AT로 고정',
  `HEADER` varchar(32) DEFAULT NULL COMMENT '카카오알림톡 헤더',
  `BTN_CNT` int DEFAULT '0' COMMENT '버튼 개수 (현재 최대 1개)',
  `ATTACHMENT` varchar(5000) DEFAULT NULL COMMENT '버튼 내용',
  `LINK_CNT` int DEFAULT '0' COMMENT '카카오 바로연결 개수(최대 1개)',
  `SUPPLEMENT` varchar(5000) DEFAULT NULL COMMENT '카카오 바로연결 내용',
  `PHONE_CNT` int NOT NULL DEFAULT '0' COMMENT '연락처 개수',
  `CUR_STATE` int DEFAULT '0' COMMENT '메시지 상태',
  PRIMARY KEY (`PACK_UNIQUEKEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_send_contents`
--

LOCK TABLES `app_send_contents` WRITE;
/*!40000 ALTER TABLE `app_send_contents` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_send_contents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_send_data`
--

DROP TABLE IF EXISTS `app_send_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_send_data` (
  `MSG_SEQ` int NOT NULL AUTO_INCREMENT COMMENT '일련번호',
  `REQ_SEND_DATE` char(14) NOT NULL COMMENT '메시지를 DB에 넣은 시간 (yyyymmddHHMMSS) 미래 시간을 넣으면 예약발송',
  `PACK_UNIQUEKEY` int DEFAULT NULL COMMENT 'APP_SEND_CONTENTS의 PACK_UNIQUEKEY GAON_MSG_TYPE이 S인 경우 불필요',
  `PHONE_NUM` varchar(12) NOT NULL COMMENT '수신 연락처',
  `NURI_MSG_SEQ` int DEFAULT NULL COMMENT '발송 실패시 누리 테이블에 입력된 MSG_SEQ값',
  `USER_SEQ` int DEFAULT NULL,
  `CUR_STATE` int DEFAULT '0' COMMENT '메시지 상태',
  `APP_GUBUN` varchar(8) NOT NULL COMMENT '앱구분 KAKAO, NAVER, TOSS',
  `CALL_BACK` varchar(12) NOT NULL COMMENT '발신 연락처',
  `SUB_ID` varchar(30) DEFAULT NULL COMMENT 'SUB_ID',
  `GAON_MSG_TYPE` char(1) NOT NULL COMMENT '메시지 타입 L(장문), S(단문)',
  `SMS_MSG_DATA` varchar(135) DEFAULT NULL COMMENT '메시지 타입이 S(단문)인 경우 메시지 내용',
  `TEMPLATE_CODE` varchar(40) NOT NULL COMMENT '템플릿 코드',
  `RSLT_CODE_APP` char(4) DEFAULT NULL COMMENT '발신 후 결과 코드',
  `MODULE_SEND_TIME` char(14) DEFAULT NULL COMMENT '모듈에서 서버로 발송한 시간',
  `SVR_RECV_TIME` char(14) DEFAULT NULL COMMENT '서버로부터 레포트 결과를 받은 시간',
  `APP_SEND_TIME` char(14) DEFAULT NULL COMMENT '서버에서 앱메시지를 보낸 시간',
  `APP_RECV_TIME` char(14) DEFAULT NULL COMMENT '서버에서 앱메시지 결과를 받은 시간',
  `SERIAL_NUMBER` varchar(39) DEFAULT NULL COMMENT '시리얼 넘버',
  `SEND_PACK_UKEY` int DEFAULT NULL,
  PRIMARY KEY (`MSG_SEQ`),
  UNIQUE KEY `app_send_data_SERIAL_IDX` (`SERIAL_NUMBER`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_send_data`
--

LOCK TABLES `app_send_data` WRITE;
/*!40000 ALTER TABLE `app_send_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_send_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_send_data_log`
--

DROP TABLE IF EXISTS `app_send_data_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_send_data_log` (
  `MSG_SEQ` int NOT NULL,
  `REQ_SEND_DATE` char(14) NOT NULL,
  `PACK_UNIQUEKEY` int DEFAULT NULL,
  `PHONE_NUM` varchar(12) NOT NULL,
  `NURI_MSG_SEQ` int DEFAULT NULL,
  `USER_SEQ` int DEFAULT NULL,
  `CUR_STATE` int DEFAULT '0',
  `APP_GUBUN` varchar(8) DEFAULT NULL,
  `CALL_BACK` varchar(12) NOT NULL,
  `SUB_ID` varchar(30) DEFAULT NULL,
  `GAON_MSG_TYPE` char(1) NOT NULL,
  `SMS_MSG_DATA` varchar(135) DEFAULT NULL,
  `TEMPLATE_CODE` varchar(40) NOT NULL,
  `RSLT_CODE_APP` char(4) DEFAULT NULL,
  `MODULE_SEND_TIME` char(14) DEFAULT NULL,
  `SVR_RECV_TIME` char(14) DEFAULT NULL,
  `APP_SEND_TIME` char(14) DEFAULT NULL,
  `APP_RECV_TIME` char(14) DEFAULT NULL,
  `SERIAL_NUMBER` varchar(39) DEFAULT NULL,
  `SEND_PACK_UKEY` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_send_data_log`
--

LOCK TABLES `app_send_data_log` WRITE;
/*!40000 ALTER TABLE `app_send_data_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_send_data_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `app_template_code`
--

DROP TABLE IF EXISTS `app_template_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `app_template_code` (
  `APP_GUBUN` varchar(8) NOT NULL,
  `TEMPLATE_CODE` varchar(40) NOT NULL,
  `HEAD` varchar(500) NOT NULL,
  `FOOT` varchar(500) DEFAULT NULL,
  `TITLE` varchar(100) DEFAULT NULL,
  `USE_BUTTON` char(1) DEFAULT 'N',
  PRIMARY KEY (`APP_GUBUN`,`TEMPLATE_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `app_template_code`
--

LOCK TABLES `app_template_code` WRITE;
/*!40000 ALTER TABLE `app_template_code` DISABLE KEYS */;
/*!40000 ALTER TABLE `app_template_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `nuri_sequence`
--

DROP TABLE IF EXISTS `nuri_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `nuri_sequence` (
  `seq_name` varchar(50) NOT NULL COMMENT '시퀀스명 ',
  `seq_currval` bigint unsigned NOT NULL COMMENT '현재 값 ',
  PRIMARY KEY (`seq_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `nuri_sequence`
--

LOCK TABLES `nuri_sequence` WRITE;
/*!40000 ALTER TABLE `nuri_sequence` DISABLE KEYS */;
INSERT INTO `nuri_sequence` VALUES ('msg',31);
/*!40000 ALTER TABLE `nuri_sequence` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'nuri'
--
/*!50003 DROP FUNCTION IF EXISTS `nextval` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`175.116.202.68` FUNCTION `nextval`() RETURNS bigint unsigned
    MODIFIES SQL DATA
    SQL SECURITY INVOKER
BEGIN

	INSERT INTO `nuri_sequence`

	SET seq_name = 'msg', seq_currval=(@v_current_value:=1)

	ON DUPLICATE KEY

	UPDATE seq_currval=(@v_current_value:=seq_currval+1);

	RETURN @v_current_value;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-08-09 13:20:39
