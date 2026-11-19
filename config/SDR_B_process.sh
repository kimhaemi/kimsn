#!/bin/bash
#################################################
#[수정][사용자 정의] 기관의 환경에 맞춰서 경로설정
# 참고용 자료입니다.
# 모듈작동이 되지 않을 수 있습니다.
#################################################
#JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.342.b07-2.el8_6.x86_64
# ===== [Claude 2026-11-19] Java 실행 환경 (OpenJDK 25 전환) 추가 시작 =====
#  - JAVA_HOME: 운영 서버의 JDK 25 설치 경로. 서버에 JAVA_HOME 환경 변수가 있으면 그 값을 우선 사용
#    (RHEL 계열 패키지 설치 시 보통 /usr/lib/jvm/java-25-openjdk, 서버마다 실제 경로 확인 필요)
#  - JAVA_MIN_VERSION: Java4Shell(CVE-2026-114938) 패치가 포함된 최소 버전. 이보다 낮으면 시작하지 않음
#  - 위의 #JAVA_HOME(Java 8) 줄은 기존 주석으로 그대로 둠
JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-25-openjdk}
JAVA_BIN=$JAVA_HOME/bin/java
JAVA_MIN_VERSION=25.0.5.1

# Java 실행 파일과 버전 확인 (start 시 호출)
check_java() {
        if [ ! -x "$JAVA_BIN" ]; then
                echo "[ERROR] Java 실행 파일을 찾을 수 없습니다: $JAVA_BIN (JAVA_HOME 경로 확인)"
                exit 1
        fi
        JAVA_VER=`"$JAVA_BIN" -XshowSettings:properties -version 2>&1 | awk -F'= ' '/ java.version = /{print $2; exit}'`
        LOWEST=`printf '%s\n%s\n' "$JAVA_MIN_VERSION" "$JAVA_VER" | sort -V | head -1`
        if [ -z "$JAVA_VER" ] || [ "$LOWEST" != "$JAVA_MIN_VERSION" ]; then
                echo "[ERROR] Java 버전($JAVA_VER)이 최소 요구 버전($JAVA_MIN_VERSION)보다 낮거나 확인할 수 없습니다. JDK 25.0.5.1 이상을 설치하세요."
                exit 1
        fi
        echo "Java: $JAVA_BIN ($JAVA_VER)"
}
# ===== [Claude 2026-11-19] Java 실행 환경 추가 끝 =====
RADAR_HOME=/home/watcher/deamon/SDR_B
RADAR_NAME_JAR=SDR_B.jar
RADAR_DAEMON_TYPE=-DDAEMON_TYPE=MCEE_SDR
RADAR_TITLE=SDR_B
RADAR_LOGS=$RADAR_HOME/logs

# 로그 디렉토리가 없으면 부팅 시점에 자동 생성
mkdir -p $RADAR_LOGS
##################
# 사용방법
##################
# # 시작
#./SDR_B_process.sh start
# # 종료
#./SDR_B_process.sh stop
# # 프로세스 확인
#./SDR_B_process.sh list
##################################################
export LANG=ko_KR.utf8
#
# HP-UX
# if [ $# != 1 ]
# linux

if [ $# == 0 ]
        then echo "Usage: SDR_B_process.sh [start | stop | list | version]"; exit;
fi

RADAR_HOME_LIST=`ls -al  | grep "$RADAR_TITLE" | awk '{print $9}'`

case "$1" in

        [Vv]ersion)
                echo "$RADAR_TITLE"
                "$JAVA_BIN" -version 2>&1 | head -1   # [Claude 2026-11-19] Java 버전 표시 (추가)
        ;;

 [Ss]tart)
        echo "START $RADAR_TITLE Process"
        check_java   # [Claude 2026-11-19] 시작 전 Java 경로·버전 검사 (추가)

        case "$2" in
                [Aa]ll)

                        for radar_list in $RADAR_HOME_LIST
                        do
                                radar_process=`ps -ef | grep "RADAR_TITLE" | grep -v grep | wc -l`

                                if [ $radar_process -gt 0 ]
                                then
                                    echo "$radar_list $RADAR_TITLE Alive."
                                else
                                {
                                        cd $RADAR_HOME
                                        nohup "$JAVA_BIN" $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAME_JAR >> "$RADAR_LOGS/$RADAR_TITLE.log" 2>&1 &   # [Claude 2026-11-19] java → "$JAVA_BIN" (정해진 JDK로 실행)
                                        echo "$radar_list $RADAR_TITLE Process Start-up."
                                }
                                fi
                        done

                        cd $RADAR_HOME/
                ;;

                *)
                        radar_process=`ps -ef | grep "$2/$RADAR_NAME_JAR" | grep -v grep | wc -l`

                        if [ $radar_process != 0 ]
                        then
                            echo "$2 $RADAR_TITLE Alive."
                        else
                        {
                                # cd $RADAR_HOME/$2
                                nohup "$JAVA_BIN" $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAME_JAR >> "$RADAR_LOGS/$RADAR_TITLE.log" 2>&1 &   # [Claude 2026-11-19] java → "$JAVA_BIN" (정해진 JDK로 실행)
                                echo "$RADAR_TITLE Process Start-up."
                        }
                        fi

                        cd $RADAR_HOME/
                ;;

        esac

        ;;

 [Ss]top)
        echo "STOP $RADAR_TITLE Process"

        case "$2" in

                [Aa]ll)

                        for radar_list in $RADAR_HOME_LIST
                        do
                                radar_process=`ps -ef | grep "$RADAR_HOME/$RADAR_NAME_JAR" | grep -v grep | wc -l`

                                if [ $radar_process -gt 0 ]
                                then
                                {
                                        kill_pid=`ps -ef | grep "$RADAR_HOME/$RADAR_NAME_JAR" | grep -v grep | awk '{print $2}'`
                                        kill $kill_pid;
                                    echo "$radar_list $RADAR_TITLE Process Stop."
                                }
                                fi
                        done
                ;;

                *)
                        radar_process=`ps -ef | grep "$2/$RADAR_NAME_JAR" | grep -v grep | wc -l`
                        if [ $radar_process != 0 ]
                        then
                        {
                                kill_pid=`ps -ef | grep "$2/$RADAR_NAME_JAR" | grep -v grep | awk '{if(1 == $3) print $2}'`
                                kill $kill_pid;
                            echo "$2 $RADAR_TITLE Process Stop."
                        }
                        fi
                ;;

        esac
;;

 [Ll]ist)
        echo "PID       PPID    STIME           COMMAND"
        for radar_name1 in $RADAR_NAME_JAR
        do
           ps -ef | grep "$radar_name1" | grep -v grep | grep -v awk | awk '{if(9==NF) printf "%s\t%s\t%s\t%s %s\n",$2,$3,$5,$8,$9; else printf "%s\t%s\t%s %4-s\t%s %s\n",$2,$3,$5,$6,$10,$11}' | sort

        done
;;

esac
