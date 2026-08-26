@echo off
REM REM은 주석 입니다. 실행되지 않습니다.

REM 1. JAVA 설치 경로
SET JAVA_HOME=C:/JAVA/java-1.8.0-openjdk/

REM 2. 누리2 JAR 위치 00_Nuri2_MASTER
SET NURI2_HOME=D:/NURI2/

REM 3. 누리2 서비스명
REM SET NURI2_SERVICE_NAME=Nuri2
SET NURI2_SERVICE_NAME=00_Nuri2_MASTER

REM 4. 누리2.JAR 위치 D:\NURI2\nuri2.jar
SET NURI2_NAME=nuri2.jar

REM 5. 누리2 환경설정 파일 D:\NURI2\nuri2.conf
SET NURI2_CONFING=nuri2.conf

REM 32비트
"%NURI2_HOME%\JavaService32.exe" -install "%NURI2_SERVICE_NAME%" "%JAVA_HOME%\jre\bin\server\jvm.dll" -Djava.class.path="%NURI2_HOME%\%NURI2_NAME%" -start kr.co.iheart.Nuri2Main -params %NURI2_CONFING% -current "%NURI2_HOME%" -auto -description "[Nuri2] %NURI2_SERVICE_NAME% Java Service"
REM "%NURI2_HOME%\JavaService32.exe" -install "%NURI2_SERVICE_NAME%" "%JAVA_HOME%\jre\bin\server\jvm.dll" -Djava.class.path="%NURI2_HOME%\%NURI2_NAME%" -Xms700m -Xmx1250m -start kr.co.iheart.Nuri2Main -params %NURI2_CONFING% -current "%NURI2_HOME%" -auto -description "[Nuri2] %NURI2_SERVICE_NAME% Java Service"

REM 64비트
REM "%NURI2_HOME%\JavaService64.exe" -install "%NURI2_SERVICE_NAME%" "%JAVA_HOME%\jre\bin\server\jvm.dll" -Djava.class.path="%NURI2_HOME%\%NURI2_NAME%" -start kr.co.iheart.Nuri2Main -params %NURI2_CONFING% -current "%NURI2_HOME%" -auto
REM "%NURI2_HOME%\JavaService64.exe" -install "%NURI2_SERVICE_NAME%" "%JAVA_HOME%\jre\bin\server\jvm.dll" -Djava.class.path="%NURI2_HOME%\%NURI2_NAME%" -Xms700m -Xmx1250m -start kr.co.iheart.Nuri2Main -params %NURI2_CONFING% -current "%NURI2_HOME%" -auto
