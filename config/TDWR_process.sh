#!/bin/bash
##/bin/ksh

#################################################
#[수정][사용자 정의] 기관의 환경에 맞춰서 경로설정
# 참고용 자료입니다.
# 모듈작동이 되지 않을 수 있습니다.
#################################################
#JAVA_HOME=/opt/java11
#RADAR_HOME=/home/watcher/deamon/TDWR
#RADAR_NAMES=TDWR.jar
#RADAR_CONF=/home/watcher/deamon/TDWR/conf/siteInfoSetting.conf



#!/bin/bash
##/bin/ksh

#################################################
#[수정][사용자 정의] 기관의 환경에 맞춰서 경로설정
# 참고용 자료입니다.
# 모듈작동이 되지 않을 수 있습니다.
#################################################
#JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.342.b07-2.el8_6.x86_64
RADAR_HOME=/home/watcher/deamon/TDWR
RADAR_NAMES=TDWR.jar
RADAR_DAEMON_TYPE=-DDAEMON_TYPE=KMA_TDWR
##################
# 사용방법
##################
# # 시작
#./TDWR_process.sh start
# # 종료
#./TDWR_process.sh stop
# # 프로세스 확인
#./TDWR_process.sh list
##################################################
export LANG=ko_KR.utf8
#
# HP-UX
# if [ $# != 1 ]
# linux

if [ $# == 0 ]
        then echo "Usage: TDWR_process.sh [start | stop | list | version]"; exit;
fi

RADAR_HOME_LIST=`ls -al  | grep 'TDWR' | awk '{print $9}'`

case "$1" in

        [Vv]ersion)
                echo "TDWR"
        ;;

 [Ss]tart)
        echo "START TDWR Process"

        case "$2" in
                [Aa]ll)

                        for radar_list in $RADAR_HOME_LIST
                        do
                                radar_process=`ps -ef | grep "$RADAR_NAMES" | grep -v grep | wc -l`

                                if [ $radar_process -gt 0 ]
                                then
                                    echo "$radar_list TDWR Alive."
                                else
                                {
                                        cd $RADAR_HOME
                                        nohup java $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAMES > /dev/null 2>&1 &
                                        echo "$radar_list TDWR Process Start-up."
                                }
                                fi
                        done

                        cd $RADAR_HOME/
                ;;

                *)
                        radar_process=`ps -ef | grep "$2/$RADAR_NAMES" | grep -v grep | wc -l`

                        if [ $radar_process != 0 ]
                        then
                            echo "$2 TDWR Alive."
                        else
                        {
                                # cd $RADAR_HOME/$2
                                nohup java $RADAR_DAEMON_TYPE -jar $RADAR_HOME/$RADAR_NAMES > /dev/null 2>&1 &
                                echo "TDWR Process Start-up."
                        }
                        fi

                        cd $RADAR_HOME/
                ;;

        esac

        ;;

 [Ss]top)
        echo "STOP TDWR Process"

        case "$2" in

                [Aa]ll)

                        for radar_list in $RADAR_HOME_LIST
                        do
                                radar_process=`ps -ef | grep "$RADAR_HOME/$RADAR_NAMES" | grep -v grep | wc -l`

                                if [ $radar_process -gt 0 ]
                                then
                                {
                                        kill_pid=`ps -ef | grep "$RADAR_HOME/$RADAR_NAMES" | grep -v grep | awk '{print $2}'`
                                        kill $kill_pid;
                                    echo "$radar_list TDWR Process Stop."
                                }
                                fi
                        done
                ;;

                *)
                        radar_process=`ps -ef | grep "$2/$RADAR_NAMES" | grep -v grep | wc -l`
                        if [ $radar_process != 0 ]
                        then
                        {
                                kill_pid=`ps -ef | grep "$2/$RADAR_NAMES" | grep -v grep | awk '{if(1 == $3) print $2}'`
                                kill $kill_pid;
                            echo "$2 TDWR Process Stop."
                        }
                        fi
                ;;

        esac
;;

 [Ll]ist)
        echo "PID       PPID    STIME           COMMAND"
        for radar_name1 in $RADAR_NAMES
        do
           ps -ef | grep "$radar_name1" | grep -v grep | grep -v awk | awk '{if(9==NF) printf "%s\t%s\t%s\t%s %s\n",$2,$3,$5,$8,$9; else printf "%s\t%s\t%s %4-s\t%s %s\n",$2,$3,$5,$6,$10,$11}' | sort

        done
;;

esac

