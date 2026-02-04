-- MySQL dump 10.13  Distrib 8.0.21, for Win64 (x86_64)
--
-- Host: localhost    Database: watchdog
-- ------------------------------------------------------
-- Server version	8.0.21

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
-- Table structure for table `receive_setting`
--

DROP TABLE IF EXISTS `receive_setting`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receive_setting` (
  `data_kind` char(3) NOT NULL COMMENT '자료종류',
  `data_type` char(3) NOT NULL COMMENT '데이터 종류',
  `data_name` varchar(50) NOT NULL COMMENT '데이터 이름',
  `time_zone` char(1) NOT NULL COMMENT '시간대 설정 (U:UTC,K:KST)',
  `filename_pattern` varchar(50) NOT NULL COMMENT '파일 이름 패턴',
  `filename_regex` varchar(50) NOT NULL COMMENT '파일 이름 정규식',
  `recv_interval` int NOT NULL COMMENT '수집 간격',
  `delay_tolerance` int NOT NULL COMMENT '누락처리하지 않고 기다려주는 시간 / 분 단위로 기록 ',
  `permitted_watch` tinyint(1) NOT NULL DEFAULT '1' COMMENT '자료 감시여부 설정 1:true, 0:false',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1:사용/0:미사용',
  PRIMARY KEY (`data_kind`,`data_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='자료 수신 처리 설정 테이블';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receive_setting`
--

LOCK TABLES `receive_setting` WRITE;
/*!40000 ALTER TABLE `receive_setting` DISABLE KEYS */;
INSERT INTO `receive_setting` VALUES ('all','not','네트워크 장애 여부','','','',0,0,0,0),('LGT','10M','10분 영상','K','LGT_10M_%yyyyMMddHHmm%.png','LGT_10M_[0-9]+\\.png',10,15,0,0),('LGT','KMA','관측자료','K','LGT_KMA_%yyyyMMddHHmm%.asc','LGT_KMA_[0-9]+\\.asc',10,10,1,0),('LGT','RDR','낙뢰-레이더 합성','K','LGT_RDR_%yyyyMMddHHmm%.png','LGT_RDR_[0-9]+\\.png',10,10,0,0),('LGT','SAT','낙뢰-위성 합성','K','LGT_SAT_%yyyyMMddHHmm%.png','LGT_SAT_[0-9]+\\.png',10,10,0,0),('RDR','LNG','강수량 자료','K','RDR_%site%_LNG_%yyyyMMddHHmm%.uf','RDR_[A-Z]+_LNG_[0-9]+\\.uf',10,10,0,0),('RDR','NQC','이진자료','K','RDR_%site%_RAW_%yyyyMMddHHmm%.nc','RDR_[A-Z]+_RAW_[0-9]+\\.nc',10,15,1,1),('RDR','PCP','1시간 추정 강수량 자료','K','RDR_%site%_%yyyyMMddHHmm%.pcp.gz','RDR_[A-Z]+_[0-9]+\\.pcp\\.gz',10,10,0,0),('RDR','PNG','이미지 자료','K','RDR_%site%_%yyyyMMddHHmm%.png','RDR_[A-Z]+_[0-9]+\\.png',10,10,0,0),('RDR','QCD','품질관리 자료','K','RDR_%site%_QCD_%yyyyMMddHHmm%.uf','RDR_[A-Z]+_QCD_[0-9]+\\.uf',10,10,0,0);
/*!40000 ALTER TABLE `receive_setting` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-07-07 20:39:12
