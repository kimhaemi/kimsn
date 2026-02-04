-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: 175.197.48.213    Database: watchdog
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
-- Table structure for table `menu`
--

DROP TABLE IF EXISTS `menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu` (
  `id` bigint NOT NULL,
  `menu_name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `status` bit(1) DEFAULT NULL,
  `depth` int DEFAULT '0',
  `order` int DEFAULT '0',
  `created_at` varchar(255) DEFAULT NULL,
  `updated_at` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu`
--

LOCK TABLES `menu` WRITE;
/*!40000 ALTER TABLE `menu` DISABLE KEYS */;
INSERT INTO `menu` VALUES (1000,'전체 감시','/',_binary '',1,1,'20230617125100','20230617125100'),(2000,'지점별 감시','#',_binary '',1,2,'20230617125100','20230617125100'),(3000,'과거자료 검색','#',_binary '',1,3,'20230617125100','20230617125100'),(4000,'지점별 통계','#',_binary '',1,4,'20230617125100','20230617125100'),(5000,'관리','#',_binary '',1,5,'20230617125100','20230617125100'),(5101,'지점/자료별 문자 발송 설정','/station_set_rc',_binary '',2,1,'20230617125100','20230617125100'),(5102,'경고 기준 설정','/station_set_rcc',_binary '',2,2,'20230617125100','20230617125100'),(5103,'자료 수신 감시 설정','/station_set_rs',_binary '',2,3,'20230617125100','20230617125100'),(5104,'문자 메시지 패턴 설정','/sms_set_msg',_binary '',2,4,'20230617125100','20230617125100'),(5105,'지점별 운영상태 설정','/station_status',_binary '',2,5,'20230617125100','20230617125100'),(5106,'문자 수신 그룹 관리','/sms_target_group',_binary '',2,6,'20230617125100','20230617125100'),(5107,'문자 수신자 관리','/sms_target_member',_binary '',2,7,'20230617125100','20230617125100'),(5108,'상시 문자 수신 그룹 관리','/sms_target_monitorgroup',_binary '',2,8,'20230617125100','20230617125100'),(5109,'문자발송','/sms_send',_binary '',2,9,'20230617125100','20230617125100'),(5110,'문자 발송 대기 내역','/sms_send_result',_binary '',2,10,'20230617125100','20230617125100'),(5111,'문자 발송 기능 ON/OFF 설정','/sms_send_onoff',_binary '',2,11,'20230617125100','20230617125100'),(6000,'사용자 관리','/users/admin_user',_binary '',1,6,'20230617125100','20230617125100');
/*!40000 ALTER TABLE `menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receive_condition`
--

DROP TABLE IF EXISTS `receive_condition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receive_condition` (
  `site` char(3) NOT NULL COMMENT '관측 지점',
  `data_kind` char(3) NOT NULL COMMENT '자료 종류(RDR,LGT)',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류',
  `recv_condition` char(4) NOT NULL COMMENT '항목별 자료 수신 상태',
  `apply_time` datetime DEFAULT NULL COMMENT '상태 적용 시각',
  `last_check_time` datetime DEFAULT NULL COMMENT '최종 확인 시각',
  `sms_send` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'SMS 발송 여부',
  `sms_send_activation` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'SMS 발송 기능 ON/OFF',
  `status` tinyint(1) DEFAULT '1' COMMENT '1:사용,0:미사용',
  PRIMARY KEY (`site`,`data_kind`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='자료 수신 상태 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receive_condition`
--

LOCK TABLES `receive_condition` WRITE;
/*!40000 ALTER TABLE `receive_condition` DISABLE KEYS */;
INSERT INTO `receive_condition` VALUES ('BRI','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('BRI','RDR','NQC','ORDI','2021-12-15 12:26:00','2021-12-21 13:11:00',0,1,1),('BRI','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('BRI','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('DJK','RDR','NQC','ORDI','2021-12-10 02:16:00','2021-12-10 02:16:00',0,1,1),('GDK','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GDK','RDR','NQC','ORDI','2021-12-20 16:46:00','2021-12-21 13:11:00',0,1,1),('GDK','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GDK','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GNG','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GNG','RDR','NQC','ORDI','2021-11-12 11:56:00','2021-12-21 13:11:00',0,1,1),('GNG','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GNG','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GSN','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GSN','RDR','NQC','ORDI','2021-12-16 17:36:00','2021-12-21 13:11:00',0,1,1),('GSN','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('GSN','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('IIA','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('IIA','RDR','NQC','WARN','2016-01-22 05:00:00','2021-12-21 13:11:00',1,0,1),('IIA','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('IIA','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('JNI','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('JNI','RDR','NQC','ORDI','2021-12-15 16:06:00','2021-12-21 13:11:00',0,1,1),('JNI','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('JNI','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KSN','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KSN','RDR','NQC','ORDI','2021-12-20 17:06:00','2021-12-21 13:11:00',0,1,1),('KSN','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KSN','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KWK','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KWK','RDR','NQC','ORDI','2021-12-03 15:46:00','2021-12-21 13:11:00',0,1,1),('KWK','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('KWK','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('LGT','LGT','10M','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('LGT','LGT','KMA','WARN','2021-12-09 10:01:00','2021-12-21 13:11:00',0,1,0),('LGT','LGT','RDR','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('LGT','LGT','SAT','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('MIL','RDR','NQC','ORDI','2021-12-10 02:16:00','2021-12-10 02:16:00',0,1,1),('MYN','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('MYN','RDR','NQC','ORDI','2021-12-14 23:36:00','2021-12-21 13:11:00',0,1,1),('MYN','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('MYN','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('net','all','not','TORE','2020-08-07 13:47:00','2020-08-07 13:47:00',1,0,0),('PSN','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('PSN','RDR','NQC','ORDI','2021-12-02 17:16:00','2021-12-21 13:11:00',0,1,1),('PSN','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('PSN','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('SRI','RDR','NQC','ORDI','2021-12-10 02:16:00','2021-12-10 02:16:00',0,1,1),('SSP','RDR','LNG','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('SSP','RDR','NQC','ORDI','2021-12-10 02:16:00','2021-12-21 13:11:00',0,1,1),('SSP','RDR','PCP','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0),('SSP','RDR','QCD','TOTA','2016-01-22 03:10:00','2016-01-22 04:59:00',1,0,0);
/*!40000 ALTER TABLE `receive_condition` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receive_condition_criteria`
--

DROP TABLE IF EXISTS `receive_condition_criteria`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receive_condition_criteria` (
  `code` char(4) NOT NULL COMMENT '경고기준코드',
  `name` varchar(50) NOT NULL COMMENT '경고기준이름',
  `criterion` int NOT NULL COMMENT '경고 적용이 되는 누락 자료수',
  `comment` varchar(500) DEFAULT NULL COMMENT '설명',
  `gubun` tinyint NOT NULL COMMENT '1:대형, 2:소형, 3:공항',
  PRIMARY KEY (`code`,`gubun`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='경고 기준 테이블\r\nreceive_condition 테이블 대응';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receive_condition_criteria`
--

LOCK TABLES `receive_condition_criteria` WRITE;
/*!40000 ALTER TABLE `receive_condition_criteria` DISABLE KEYS */;
INSERT INTO `receive_condition_criteria` VALUES ('NOTI','주의',3,'자료가 연속으로 3회 이상  자료 미수신일 때',1),('NOTI','주의',3,'자료가 연속으로 3회 이상  자료 미수신일 때',2),('ORDI','정상',0,'정상',1),('ORDI','정상',0,'정상',2),('RETR','복구',3,'비 정상에서 복구되어 정상의 범위에 들 경우',1),('RETR','복구',3,'비 정상에서 복구되어 정상의 범위에 들 경우',2),('TORE','네트워크 복구',1,'네트워크 장애 상태에서 복구되어 정상의 범위에 들때',1),('TORE','네트워크 복구',1,'네트워크 장애 상태에서 복구되어 정상의 범위에 들때',2),('TOTA','네트워크 장애',3,'전 사이트 자료 미수신 일 때',1),('TOTA','네트워크 장애',3,'전 사이트 자료 미수신 일 때',2),('WARN','경고',6,'자료가 연속으로 6회 이상 자료 미수신일 때',1),('WARN','경고',6,'자료가 연속으로 6회 이상 자료 미수신일 때',2);
/*!40000 ALTER TABLE `receive_condition_criteria` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `receive_condition_view`
--

DROP TABLE IF EXISTS `receive_condition_view`;
