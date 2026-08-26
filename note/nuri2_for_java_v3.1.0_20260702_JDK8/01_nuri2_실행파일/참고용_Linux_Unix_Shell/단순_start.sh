#!/bin/bash
#####################
# JAVA 경로
JAVA_HOME=/opt/java8
# Nuri2 경로
NURI2_HOME=/home/nuri2/
# Nuri2 파일명
NURI2_NAMES=nuri2.jar
# Nuri2 환경설정
NURI2_CONF=nuri2.conf
cd $NURI2_HOME
nohup $JAVA_HOME/bin/java  -Xms10m -Xmx512m  -jar $NURI2_HOME/$NURI2_NAMES $NURI2_HOME/$NURI2_CONF > /dev/null &
echo Nuri Process Start-up."

