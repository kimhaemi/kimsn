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
-- Table structure for table `station_rdr`
--

DROP TABLE IF EXISTS `station_rdr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `station_rdr` (
  `site_cd` char(3) NOT NULL COMMENT '관측지점코드',
  `site_num` int NOT NULL COMMENT '관측지점번호',
  `name_kr` varchar(10) NOT NULL COMMENT '관측지점명(한글)',
  `name_en` varchar(20) NOT NULL COMMENT '관측지점명(영문)',
  `height` int DEFAULT NULL COMMENT '관측지점고도(km*10)',
  `max_range` int DEFAULT NULL COMMENT 'MaxRange (km)',
  `gate_size` int DEFAULT NULL COMMENT 'GateSize (m)',
  `gates` int DEFAULT NULL COMMENT 'Gates',
  `rain_intensity` varchar(10) DEFAULT NULL COMMENT 'mm/hr',
  `addr` varchar(40) DEFAULT NULL COMMENT '주소',
  `model` varchar(20) DEFAULT NULL COMMENT '모델',
  `install_date` date DEFAULT NULL COMMENT '설치일자',
  `prod_company` varchar(20) DEFAULT NULL COMMENT '제작사',
  `prod_country` varchar(20) DEFAULT NULL COMMENT '제작국가',
  `permitted_watch` tinyint(1) NOT NULL DEFAULT '1' COMMENT '자료 감시여부 설정 1:true, 0:false',
  `sort_order` tinyint(1) NOT NULL DEFAULT '1' COMMENT '웹 표출시 순서',
  `gubun` tinyint NOT NULL DEFAULT '1' COMMENT '대형, 소형, 공항',
  PRIMARY KEY (`site_cd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='레이더 지점 정보';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `station_rdr`
--

LOCK TABLES `station_rdr` WRITE;
/*!40000 ALTER TABLE `station_rdr` DISABLE KEYS */;
INSERT INTO `station_rdr` VALUES ('BRI',47102,'백령도','',2,256,500,512,'0.1~100','인천  옹진군  백령면  연화리  산  242-1','TDR-4384-C ','1970-01-01','RADTEC/KAVOURAS','미국',1,11,1),('DJK',52000,'덕적도',' ',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,22,2),('GDK',47094,'광덕산','',2,240,250,1000,'0.1~100','강원  화천군  사내면  광덕리  산  273-92','METEOR-1500S ','1970-01-01','Gematronik','독일',1,3,1),('GNG',47105,'강릉','',2,279,250,1117,'0.1~100','강원  강릉시  사천면  방동리 ','WSR-98D/S ','1970-01-01','Metstar','중국',1,4,1),('GSN',47185,'고산','',2,250,250,1000,'0.1~100','제주  제주시  한경면  고산리 3762  (노을해안로  1013-70)','METEOR-1500S ','1970-01-01','Gematronik','독일',1,9,1),('IIA',47113,'인천공항','',2,130,250,521,'0.1~100','인천  중구  운서동   2172-1 인천공항  우체국  사서함 43호','','1970-01-01','','',1,30,3),('JNI',47175,'진도','',2,240,250,960,'0.1~100','전남  진도군  의신면  사천리  산  1-6','METEOR-1500S ','1970-01-01','Gematronik','독일',1,8,1),('KSN',47144,'오성산','',2,239,250,957,'0.1~100','전라북도  군산시  성산면  성덕리  60-13','WSR-98D/S ','1970-01-01','Metstar','중국',1,7,1),('KWK',47116,'관악산','',2,240,250,960,'0.1~100','경기  과천시  중앙동  산  12-1','DWSR-8501S/K ','1970-01-01','EEC','미국',1,1,1),('MIL',50000,'망일산',' ',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,20,2),('MYN',47148,'면봉산','',2,200,250,800,'0.1~100','경북  청송군  현서면  무계리  산  21-4','WSR-98D/C ','1970-01-01','Metstar','중국',1,5,1),('PSN',47160,'구덕산','',2,240,250,960,'0.1~100','부산  서구  서대신동3가  산  32-10  구덕산  기상레이더','DWSR-8501S/K ','1970-01-01','EEC','미국',1,6,1),('SRI',51000,'수리산',' ',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1,21,2),('SSP',47188,'성산','',2,250,250,1000,'0.1~100','제주  서귀포시  성산읍  신산리  685-4','METEOR-1500S ','1970-01-01','Gematronik','독일',1,10,1);
/*!40000 ALTER TABLE `station_rdr` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-07-07 20:34:10
