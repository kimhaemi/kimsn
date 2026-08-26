#!/bin/bash

# chmod 755 launcher.sh 권한을 주어야 합니다.
# JAVA_HOME 은 java 실행파일이 있는 전체 경로를 의미합니다.
# 실행이 안 될 땐 launcher.sh 파일에서 아랫부분의 java 실행 경로가 맞는지 확인하시기 바랍니다.
# nohup $JAVA_HOME/java
#System(user)/home/nuri2>which java
#/usr/bin/java - java hoem path
JAVA_HOME=/usr/java/bin
# nuri2 path
NURI2_HOME=/home/nuri2
NURI2_NAMES=nuri2.jar
# NURI2_CONF=nuri2.conf
#NURI2_CONF=./nuri2_conf/nuri2.conf
NURI2_CONF=nuri2.conf
if [ $# == 0 ]
	then echo "Usage: launcher.sh [start | stop | list | version]"; exit;
fi
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

# 위 export LANG=ko_KR.euckr 를 설정하여도 euckr 한글이 셋팅되지 않을 때 아래 LC_ALL 주석을 해제하여 기동하세요
#export LC_ALL=ko_KR.euckr
#export LC_ALL=ko_KR.utf8
###################################
# heap 설정
###################################
# 대량메시지 발송 전
## -Xms750m -Xmx1250m
# 일반은 원하는 최대 값 설정
## -Xms10m -Xmx500m
###################################

case "$1" in

	[Vv]ersion)
		echo "Nuri2 Solution v1.0"
	;;

	[Ss]tart)
		echo "START Nuri2 Process"

		nuri2_process=$(ps -ef | grep "$NURI2_NAMES" | grep -v grep | wc -l)

		if [ "$nuri2_process" -gt 0 ]

		then
			echo "Nuri2 Alive."
		else
		{
			cd "$NURI2_HOME" || exit
			nohup "$JAVA_HOME/java" -Xms10m -Xmx512m  -jar "$NURI2_HOME/$NURI2_NAMES" "$NURI2_HOME/$NURI2_CONF" > /dev/null &
			echo "Nuri2 Process Start-up."
		}
		fi

		cd "$NURI2_HOME" || exit
	;;

	[Ss]top)
		echo "STOP Nuri2 Process"

		nuri2_process=$(ps -ef | grep "$NURI2_HOME/$NURI2_NAMES" | grep -v grep | wc -l)

		if [ "$nuri2_process" -gt 0 ]
		then
		{
			kill_pid=$(ps -ef | grep "$NURI2_HOME/$NURI2_NAMES" | grep -v grep | awk '{print $2}')
			kill "$kill_pid";
			echo "Nuri2 Process Stop."
		}
		fi
	;;

	[Ll]ist)
		echo "PID		PPID		STIME			COMMAND"
		for nuri2_name in $NURI2_NAMES
		do
#			ps -ef | grep "$nuri2_name" | grep -v grep | grep -v awk | awk '{if(9==NF) printf "%s\t%s\t%s\t%s %s\n",$2,$3,$5,$8,$9; else printf "%s\t%s\t%s %4-s\t%s %s\n",$2,$3,$5,$6,$9,$10}' | sort
      ps -ef | grep "$nuri2_name" | grep -v grep | grep -v awk | sort
		done
	;;

esac
