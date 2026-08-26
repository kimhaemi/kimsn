#!/bin/bash
JAVA_HOME=/opt/java8
NURI2_HOME=/home/nuri2/
NURI2_NAMES=nuri2.jar
NURI2_CONF=nuri2.conf
##############################################
# chmod 755 launcher.sh 권한을 주어야 합니다.
# JAVA_HOME 은 java 실행파일이 있는 전체 경로를 의미합니다.
# 실행이 안 될 땐 launcher.sh 파일에서 아랫부분의 java 실행 경로가 맞는지 확인하시기 바랍니다.
# * 응용 해서 사용.
##############################################
# 리눅스용.
##############################################
if [ $# == 0 ]
	then echo "Usage: NURI2_process.sh [start | stop | list | version]"; exit;
fi
##############################################

##############################################
# HP-UX
##############################################
#if [ $# != 1 ]
#        then echo "Usage: NURI2_process.sh [start | stop | list | version]"; exit;
#fi
##############################################

###################################
# heap 설정
###################################
# 대량메시지 발송 전
## -Xms750m -Xmx1250m
# 일반은 원하는 최대 값 설정
## -Xms10m -Xmx500m
###################################

#################################################
#* 필수 설정 한글(utf8 설정)
# [사용자 정의] 서버에 한글 인코딩 설정(locale -a|grep ko)
# 시스템에 맞는 locale 설정 필요(대소문자 식별)
#################################################
# Unix , Linux 계열 공통 서버에 locale 확인방법
# System(user)/home/nuri2> locale -a|grep ko
#       ko_KR
#       ko_KR.euckr
#       ko_KR.utf8
#       ......
# csh 일 경우 setenv LANG ko_KR.utf8, AIX 의 경우 ko_KR.IBM-utf8
export LANG=ko_KR.utf8

NURI2_HOME_LIST=`ls -al  | grep "$NURI2_NAMES" | awk '{print $9}'`

case "$1" in

	[Vv]ersion)
		echo "Nuri2 Solution v1.0"
	;;

	[Ss]tart)
	echo "START Nuri Process"

	case "$2" in
		[Aa]ll)

			for NURI2_list in $NURI2_HOME_LIST
			do
				NURI2_process=`ps -ef | grep "$NURI2_list/$NURI2_NAMES" | grep -v grep | wc -l`

				if [ $NURI2_process -gt 0 ]

				then
				    echo "$NURI2_list Nuri Alive."
				else
				{
					cd $NURI2_HOME/$NURI2_list
					nohup $JAVA_HOME/bin/java  -Xms10m -Xmx512m -jar $NURI2_HOME/$NURI2_list/$NURI2_NAMES $NURI2_HOME/$NURI2_list/$NURI2_CONF > /dev/null &
					echo "$NURI2_list Nuri Process Start-up."
				}
				fi
			done

			cd $NURI2_HOME/
		;;

		*)
			NURI2_process=`ps -ef | grep "$2/$NURI2_NAMES" | grep -v grep | wc -l`
			if [ $NURI2_process != 0 ]
			then
			    echo "$2 Nuri Alive."
			else
			{
				cd $NURI2_HOME/$2
				nohup $JAVA_HOME/bin/java  -Xms10m -Xmx200m  -jar $NURI2_HOME/$2/$NURI2_NAMES $NURI2_HOME/$2/$NURI2_CONF > /dev/null &
				echo "$2 Nuri Process Start-up."
			}
			fi

			cd $NURI2_HOME/
		;;

	esac

	;;

[Ss]top)
	echo "STOP Nuri Process"

	case "$2" in

		[Aa]ll)

			for NURI2_list in $NURI2_HOME_LIST
			do
				NURI2_process=`ps -ef | grep "$NURI2_HOME/$NURI2_list/$NURI2_NAMES" | grep -v grep | wc -l`

				if [ $NURI2_process -gt 0 ]
				then
				{
					kill_pid=`ps -ef | grep "$NURI2_HOME/$NURI2_list/$NURI2_NAMES" | grep -v grep | awk '{print $2}'`
					kill $kill_pid;
				    echo "$NURI2_list Nuri Process Stop."
				}
				fi
			done
		;;

		*)
			NURI2_process=`ps -ef | grep "$2/$NURI2_NAMES" | grep -v grep | wc -l`
			if [ $NURI2_process != 0 ]
			then
			{
				kill_pid=`ps -ef | grep "$2/$NURI2_NAMES" | grep -v grep | awk '{if(1 == $3) print $2}'`
				kill $kill_pid;
			    echo "$2 Nuri Process Stop."
			}
			fi
		;;

	esac
;;


	[Ll]ist)
		echo "PID		PPID		STIME			COMMAND"
		for nuri2_name1 in $NURI2_NAMES
		do
         #			ps -ef | grep "$nuri2_name1" | grep -v grep | grep -v awk | awk '{if(9==NF) printf "%s\t%s\t%s\t%s %:Wq!s\n",$2,$3,$5,$8,$9; else printf "%s\t%s\t%s %4-s\t%s %s\n",$2,$3,$5,$6,$9,$10}' | sort
         ps -ef | grep "$nuri2_name1" | grep -v grep | grep -v awk | sort
		done
	;;

esac
