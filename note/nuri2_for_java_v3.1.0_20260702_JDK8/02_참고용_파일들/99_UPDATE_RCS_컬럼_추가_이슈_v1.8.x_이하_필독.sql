DBMS 별 ALTER 문법
* 누리2 1.8.0 이하 버전 테이블 공통적용
* 컬럼명 추가 대상 테이블
- NRMSG_DATA 또는 NURI2_NRMSG_DATA
- NRMSG_LOG 또는 NRMSG_DATA_LOG 또는 NRMSG_LOG_YYYYMM
- NURI2_NRMSG_LOG 또는 NURI2_NRMSG_DATA_LOG 또는 NURI2_NRMSG_DATA_YYYYMM
- 별도 지정 테이블등 해당 모든 메시지 관련 테이블입니다.

* 아래 DBMS 별 쿼리를 응용해서 조치 해주세요.
* 누리2 v1.8 이하 버전에서 v2.x 업데이트시 필수 사항입니다.

========================================
*[참고][RCS]를 사용하는 기관이라면 아래 내용 참고
 - RCS사용기관은 앞으로는 'RCS_BRAND_ID'가 필수 값이 됩니다.
 - 기존 AP 개발시 브랜드 ID를 입력하지 않았을때 AP수정 없이 적용하는 방법을 설명합니다.
 - 'RCS_BRAND_ID' 브랜드ID를 컬럼 기본값으로 하면 AP 수정없이 적용 가능
 예) mysql의 경우 ALTER TABLE NRMSG_DATA ADD RCS_BRAND_ID VARCHAR(100) NULL DEFAULT 'BR.XXXXXXX' COMMENT '브랜드ID' AFTER `RCS_BRAND_KEY`;
  - APP 수정이 필요하지 않음. 하지만 여러 브랜드ID를 사용하면 당연히 AP 에서 입력가능 하도록 해야합니다.
  - XMS/ALT 와 연관이 없음.
========================================

-----------------------------------------
1. Altibase
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD RCS_BRAND_ID VARCHAR(100);

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR(100);
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영

-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD RCS_BRAND_ID VARCHAR(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD RCS_BRAND_ID VARCHAR(100);


-----------------------------------------
2. Db2
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_BRAND_ID VARCHAR(100);
-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD COLUMN RCS_BRAND_ID VARCHAR(100);
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영

-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD COLUMN RCS_BRAND_ID VARCHAR(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD COLUMN RCS_BRAND_ID VARCHAR(100);
-----------------------------------------
3. Oracle
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA  ADD RCS_BRAND_ID VARCHAR2(100);

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR2(100);


--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD RCS_BRAND_ID VARCHAR2(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506  ADD RCS_BRAND_ID VARCHAR2(100);
-- ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR2(100);

-----------------------------------------
4. Tibero
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD RCS_BRAND_ID VARCHAR2(100);

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR2(100);
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD RCS_BRAND_ID VARCHAR2(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD RCS_BRAND_ID VARCHAR2(100);
-----------------------------------------
5. CUBRID
-----------------------------------------
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;


-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
-----------------------------------------
6. MSSQL
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD RCS_BRAND_ID VARCHAR(100);

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD RCS_BRAND_ID VARCHAR(100);
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD RCS_BRAND_ID VARCHAR(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD RCS_BRAND_ID VARCHAR(100);
-----------------------------------------
7. MySQL
-----------------------------------------
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;

-----------------------------------------
8. MariaDB8. MariaDB
-- 기관의 브랜드ID를 기본값으로 하면 AP 수정없이 적용 가능
-- ** ALTER TABLE NRMSG_DATA CHANGE COLUMN RCS_BRAND_ID RCS_BRAND_ID VARCHAR(100) NULL DEFAULT 'BR.XXXXXXX' COMMENT '브랜드ID' AFTER `RCS_BRAND_KEY`;
-----------------------------------------
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;


-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD COLUMN RCS_BRAND_ID VARCHAR(100) AFTER RCS_BRAND_KEY;
-----------------------------------------
9. PostgreSQL
-----------------------------------------
-- AFTER 문법은 지원하지 않으며, 컬럼은 테이블 마지막에 추가됩니다.
-- 전송 테이블 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_BRAND_ID VARCHAR(100);

-- LOG 테이블(단일사용시)
ALTER TABLE NRMSG_LOG ADD COLUMN RCS_BRAND_ID VARCHAR(100) ;
--또는 연월단위 사용시 해당월 뒤로2개월까지 반영
-- LOG 테이블(2025년 07월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202507 ADD COLUMN RCS_BRAND_ID VARCHAR(100);
-- LOG 테이블(2025년 06월) 컬럼 추가
ALTER TABLE NRMSG_LOG_202506 ADD COLUMN RCS_BRAND_ID VARCHAR(100);
-----------------------------------------
