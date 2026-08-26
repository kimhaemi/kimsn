-- ** MySql & MaraiDB 용 Sequence ** 사용법
--  현재 query 내용을 실행 , 특별한 오류가 없으면 정상 생성완료
-- sequence(시퀀스) 관리 테이블 생성
-- db 재기동을  해도 시퀀스 값을 유지함.
CREATE TABLE IF NOT EXISTS `nuri2_sequence` (
  `seq_name` varchar(50) NOT NULL	      COMMENT '시퀀스명 ',
  `seq_currval` bigint(20) unsigned NOT NULL  COMMENT '현재 값 ',
  PRIMARY KEY (`seq_name`)
) ENGINE=InnoDB ;
-- ------------------------------------------------------------------------------
-- -- create function --nuri2 SEQ 용
DELIMITER $$
CREATE FUNCTION msg_nextval()
	RETURNS BIGINT UNSIGNED
	MODIFIES SQL DATA
	SQL SECURITY INVOKER
BEGIN
	INSERT INTO `nuri2_sequence`
	SET seq_name = 'nuri2', seq_currval=(@v_current_value:=1)
	ON DUPLICATE KEY
	UPDATE seq_currval=(@v_current_value:=seq_currval+1);
	RETURN @v_current_value;
END $$
DELIMITER ;

-- ------------------------------------------------------------------------------
-- 시퀀스 함수   삭제시
-- DROP FUNCTION msg_nextval ;
-- DELETE FUNCTION msg_nextval ;
-- ------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------
-- 메시지 시퀀스  용
SELECT msg_nextval() ;
-- ------------------------------------------------------------------------------
-- --  실제 사용 예   msg_nextval() 로 auto_increment 속성을 대체
-- ------------------------------------------------------------------------------
-- ------------------------------------------------------------------------------
-- SELECT msg_nextval() FROM dual;
-- insert into 테이블 (MSG_SEQ) VALUES( msg_nextval() );
