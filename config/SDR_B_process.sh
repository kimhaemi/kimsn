#!/bin/bash
#################################################
#[수정][사용자 정의] 기관의 환경에 맞춰서 경로설정
# 참고용 자료입니다.
# 모듈작동이 되지 않을 수 있습니다.
#################################################
#JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.342.b07-2.el8_6.x86_64
RADAR_HOME=/home/watcher/deamon/SDR_B
RADAR_NAME_JAR=SDR_B.jar
RADAR_DAEMON_TYPE=-DDAEMON_TYPE=MCEE_SDR
RADAR_TITLE=SDR_B
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
        ;;

 [Ss]tart)
        echo "START $RADAR_TITLE Process"

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
                                        nohup java $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAME_JAR > /dev/null 2>&1 &
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
                                nohup java $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAME_JAR > /dev/null 2>&1 &
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
