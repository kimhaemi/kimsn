DBMS 별 ALTER 문법
* 누리2 1.2.0 이하 버전 테이블
 -  1.2.X 버전 이용기관은 모두 적용
 -  99_UPDATE_RCS_컬럼_추가_이슈_v1.8.x_이하.sql 추가 확인 해주세요

* 컬럼명 변경 대상 테이블
- NRMSG_DATA, NURI2_NRMSG_DATA
- NRMSG_LOG, NRMSG_DATA_LOG, NRMSG_DATA_YYYYMM
- NURI2_NRMSG_LOG, NURI2_NRMSG_DATA_LOG, NURI2_NRMSG_DATA_YYYYMM
- 별도 지정 테이블등 해당 모든 메시지 관련 테이블입니다.


* 아래 DBMS 별 쿼리를 응용해서 조치 해주세요.

-----------------------------------------
1. Altibase
-----------------------------------------
-- 새 컬럼 추가
ALTER TABLE NRMSG_DATA ADD RCS_MESSAGE_BASE_ID
VARCHAR(100);
-- 데이터 복사
UPDATE NRMSG_DATA SET RCS_MESSAGE_BASE_ID =
RCS_MASSAGE_BASE_ID;
-- 기존 컬럼 삭제
ALTER TABLE NRMSG_DATA DROP COLUMN
RCS_MASSAGE_BASE_ID;

-----------------------------------------
2. Db2
-----------------------------------------
-- 새 컬럼 추가
ALTER TABLE NRMSG_DATA ADD COLUMN RCS_MESSAGE_BASE_ID
VARCHAR(100);
-- 데이터 복사
UPDATE NRMSG_DATA SET RCS_MESSAGE_BASE_ID =
RCS_MASSAGE_BASE_ID;
-- 기존 컬럼 삭제
ALTER TABLE NRMSG_DATA DROP COLUMN
RCS_MASSAGE_BASE_ID;

-----------------------------------------
3. Oracle
-----------------------------------------
ALTER TABLE NRMSG_DATA RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;
ALTER TABLE NRMSG_LOG  RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;
-----------------------------------------
4. Tibero
-----------------------------------------
ALTER TABLE NRMSG_DATA RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;
ALTER TABLE NRMSG_LOG RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;

-----------------------------------------
5. CUBRID
-----------------------------------------
ALTER TABLE NRMSG_DATA RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;
ALTER TABLE NRMSG_LOG  RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;

-----------------------------------------
6. MSSQL
-----------------------------------------
MSSQL에서는 sp_rename 시스템 저장 프로시저를 사용하여 컬럼명을 변경합
니다.
EXEC sp_rename 'NRMSG_DATA.RCS_MASSAGE_BASE_ID',
'RCS_MESSAGE_BASE_ID', 'COLUMN';

-----------------------------------------
7. MySQL
-----------------------------------------
ALTER TABLE NRMSG_DATA CHANGE COLUMN
RCS_MASSAGE_BASE_ID RCS_MESSAGE_BASE_ID VARCHAR(100);
ALTER TABLE NRMSG_LOG  CHANGE COLUMN
RCS_MASSAGE_BASE_ID RCS_MESSAGE_BASE_ID VARCHAR(100);

-----------------------------------------
8. MariaDB
-----------------------------------------
ALTER TABLE NRMSG_DATA CHANGE COLUMN
RCS_MASSAGE_BASE_ID RCS_MESSAGE_BASE_ID VARCHAR(100);
ALTER TABLE NRMSG_LOG  CHANGE COLUMN
RCS_MASSAGE_BASE_ID RCS_MESSAGE_BASE_ID VARCHAR(100);

-----------------------------------------
9. PostgreSQL
-----------------------------------------
ALTER TABLE NRMSG_DATA RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;
ALTER TABLE NRMSG_LOG RENAME COLUMN
RCS_MASSAGE_BASE_ID TO RCS_MESSAGE_BASE_ID;