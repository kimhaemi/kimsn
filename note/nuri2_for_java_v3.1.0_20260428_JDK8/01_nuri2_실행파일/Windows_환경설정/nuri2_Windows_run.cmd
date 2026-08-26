@echo off
REM REM은 주석 입니다. 현재 실행되지 않습니다.
REM JAVA 홈경로
PATH=C:/JAVA/java-1.8.0-openjdk/;C:/JAVA/java-1.8.0-openjdk/bin;
REM 한글 EUCK-KR(949) 또는 UTF8(65001) 설정
REM EUCKR(MS949)
REM chcp 949
REM UTF8
REM chcp 65001
echo Nuri2 dummy
java -jar nuri2.jar nuri2.conf

pause
